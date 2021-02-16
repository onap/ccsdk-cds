package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.template

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.convertValue
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sConnectionPluginConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance.K8sConfigValueRequest
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance.K8sPluginInstanceApi
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.returnNullIfMissing
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

@Component("component-k8s-config-value")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class K8sConfigValueComponent(
    private var bluePrintPropertiesService: BlueprintPropertiesService
) : AbstractComponentFunction() {

    private val log = LoggerFactory.getLogger(K8sConfigValueComponent::class.java)!!

    companion object {
        const val INPUT_RESOURCE_ASSIGNMENT_MAP = "resource-assignment-map"
        const val INPUT_ARTIFACT_PREFIX_NAMES = "artifact-prefix-names"
        const val INPUT_K8S_TEMPLATE_NAME = "k8s-template-name"
        const val INPUT_K8S_CONFIG_NAME = "k8s-config-name"
        const val INPUT_K8S_INSTANCE_ID = "k8s-instance-id"
        const val INPUT_K8S_TEMPLATE_VALUE_SOURCE = "k8s-rb-template-value-source"

        const val OUTPUT_STATUSES = "statuses"
        const val OUTPUT_SKIPPED = "skipped"
        const val OUTPUT_UPLOADED = "uploaded"
        const val OUTPUT_ERROR = "error"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("Triggering K8s Config Value component logic.")
        val inputParameterNames = arrayOf(
            INPUT_K8S_TEMPLATE_NAME,
            INPUT_K8S_CONFIG_NAME,
            INPUT_K8S_INSTANCE_ID,
            INPUT_K8S_TEMPLATE_VALUE_SOURCE,
            INPUT_ARTIFACT_PREFIX_NAMES
        )
        val outputPrefixStatuses = mutableMapOf<String, String>()
        val inputParamsMap = mutableMapOf<String, JsonNode?>()

        inputParameterNames.forEach {
            inputParamsMap[it] = getOptionalOperationInput(it)?.returnNullIfMissing()
        }

        log.info("Getting the template prefixes")
        val prefixList: ArrayList<String> = getTemplatePrefixList(inputParamsMap[INPUT_ARTIFACT_PREFIX_NAMES])

        log.info("Iterating over prefixes in resource assignment map.")
        for (prefix in prefixList) {
            outputPrefixStatuses[prefix] = OUTPUT_SKIPPED
            val prefixNode: JsonNode = operationInputs[INPUT_RESOURCE_ASSIGNMENT_MAP]?.get(prefix) ?: continue
            val assignmentMapPrefix = JacksonUtils.jsonNode(prefixNode.toPrettyString()) as ObjectNode
            val prefixInputParamsMap = inputParamsMap.toMutableMap()
            prefixInputParamsMap.forEach { (inputParamName, value) ->
                if (value == null) {
                    val mapValue = assignmentMapPrefix.get(inputParamName)
                    log.debug("$inputParamName value was $value so we fetch $mapValue")
                    prefixInputParamsMap[inputParamName] = mapValue
                }
            }

            val templateName: String? = prefixInputParamsMap[INPUT_K8S_TEMPLATE_NAME]?.returnNullIfMissing()?.asText()
            val configName: String? = prefixInputParamsMap[INPUT_K8S_CONFIG_NAME]?.returnNullIfMissing()?.asText()
            val instanceId: String? = prefixInputParamsMap[INPUT_K8S_INSTANCE_ID]?.returnNullIfMissing()?.asText()
            val valueSource: String? = prefixInputParamsMap[INPUT_K8S_TEMPLATE_VALUE_SOURCE]?.returnNullIfMissing()?.asText()
            if (templateName == null || instanceId == null || valueSource == null) {
                log.warn("$INPUT_K8S_TEMPLATE_NAME or $INPUT_K8S_TEMPLATE_NAME or $INPUT_K8S_TEMPLATE_VALUE_SOURCE or $INPUT_K8S_CONFIG_NAME is null")
            } else if (templateName.isEmpty()) {
                log.warn("$INPUT_K8S_TEMPLATE_NAME is empty")
            } else {
                log.info("Uploading K8s template value..")
                outputPrefixStatuses[prefix] = OUTPUT_ERROR
                val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
                val artifact: ArtifactDefinition = bluePrintContext.nodeTemplateArtifact(nodeTemplateName, valueSource)
                if (artifact.type != BlueprintConstants.MODEL_TYPE_ARTIFACT_K8S_PROFILE)
                    throw BlueprintProcessorException(
                        "Unexpected profile artifact type for profile source $valueSource. Expecting: $artifact.type"
                    )
                // Creating API connector
                val api = K8sPluginInstanceApi(K8sConnectionPluginConfiguration(bluePrintPropertiesService))
                val configValueRequest = K8sConfigValueRequest()
                configValueRequest.templateName = templateName
                configValueRequest.configName = configName
                configValueRequest.description = valueSource
                configValueRequest.values = parseResult(valueSource)
                api.createConfigurationValues(configValueRequest, instanceId)
            }
        }
    }

    private fun parseResult(templateValueSource: String): Any {
        val ymlSourceFile = getYmlSourceFile(templateValueSource)
        val yamlReader = ObjectMapper(YAMLFactory())
        val obj: Any = yamlReader.readValue(ymlSourceFile, Any::class.java)

        val jsonWriter = ObjectMapper()
        return jsonWriter.convertValue(obj)
    }

    private fun getYmlSourceFile(templateValueSource: String): File {
        val bluePrintBasePath: String = bluePrintRuntimeService.bluePrintContext().rootPath
        val profileSourceFileFolderPath: Path = Paths.get(bluePrintBasePath.plus(File.separator).plus(templateValueSource))

        if (profileSourceFileFolderPath.toFile().exists() && !profileSourceFileFolderPath.toFile().isDirectory)
            return profileSourceFileFolderPath.toFile()
        else
            throw BlueprintProcessorException("Template value $profileSourceFileFolderPath is missing in CBA folder")
    }

    private fun getTemplatePrefixList(node: JsonNode?): ArrayList<String> {
        val result = ArrayList<String>()
        when (node) {
            is ArrayNode -> {
                val arrayNode = node.toList()
                for (prefixNode in arrayNode)
                    result.add(prefixNode.asText())
            }
            is ObjectNode -> {
                result.add(node.asText())
            }
        }
        return result
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBlueprintError().addError(runtimeException.message!!)
    }
}

package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.template

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.convertValue
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sConnectionPluginConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance.K8sConfigValueRequest
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance.K8sPluginInstanceApi
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.returnNullIfMissing
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintVelocityTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
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
    private var bluePrintPropertiesService: BluePrintPropertiesService,
    private val resourceResolutionService: ResourceResolutionService
) : AbstractComponentFunction() {

    private val log = LoggerFactory.getLogger(K8sConfigValueComponent::class.java)!!

    companion object {
        const val INPUT_RESOURCE_ASSIGNMENT_MAP = "resource-assignment-map"
        const val INPUT_ARTIFACT_PREFIX_NAMES = "artifact-prefix-names"
        const val INPUT_K8S_RB_CONFIG_TEMPLATE_NAME = "k8s-rb-config-template-name"
        const val INPUT_K8S_RB_CONFIG_NAME = "k8s-rb-config-name"
        const val INPUT_K8S_INSTANCE_ID = "k8s-instance-id"
        const val INPUT_K8S_CONFIG_VALUE_SOURCE = "k8s-rb-config-value-source"
        const val INPUT_K8S_CONFIG_OPERATION_TYPE = "k8s-config-operation-type"

        const val OUTPUT_STATUSES = "statuses"
        const val OUTPUT_SKIPPED = "skipped"
        const val OUTPUT_UPLOADED = "uploaded"
        const val OUTPUT_ERROR = "error"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("Triggering K8s Config Value component logic.")
        val inputParameterNames = arrayOf(
            INPUT_K8S_RB_CONFIG_TEMPLATE_NAME,
            INPUT_K8S_RB_CONFIG_NAME,
            INPUT_K8S_INSTANCE_ID,
            INPUT_K8S_CONFIG_OPERATION_TYPE,
            INPUT_K8S_CONFIG_VALUE_SOURCE,
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

            val templateName: String? = prefixInputParamsMap[INPUT_K8S_RB_CONFIG_TEMPLATE_NAME]?.returnNullIfMissing()?.asText()
            val configName: String? = prefixInputParamsMap[INPUT_K8S_RB_CONFIG_NAME]?.returnNullIfMissing()?.asText()
            val instanceId: String? = prefixInputParamsMap[INPUT_K8S_INSTANCE_ID]?.returnNullIfMissing()?.asText()
            var valueSource: String? = prefixInputParamsMap[INPUT_K8S_CONFIG_VALUE_SOURCE]?.returnNullIfMissing()?.asText()
            val operationType = prefixInputParamsMap[INPUT_K8S_CONFIG_OPERATION_TYPE]?.returnNullIfMissing()?.asText()?.toUpperCase()

            if (valueSource == null) {
                valueSource = configName
                log.info("Config name used instead of value source")
            }
            if (operationType == null || operationType == OperationType.CREATE.toString())
                createOperation(templateName, instanceId, valueSource, outputPrefixStatuses, prefix, configName)
            else if (operationType == OperationType.UPDATE.toString())
                updateOperation(templateName, instanceId, valueSource, outputPrefixStatuses, prefix, configName)
            else if (operationType == OperationType.DELETE.toString())
                deleteOperation(instanceId, configName)
            else
                throw BluePrintProcessorException("Unknown operation type: $operationType")
        }
    }

    private suspend fun createOperation(templateName: String?, instanceId: String?, valueSource: String?, outputPrefixStatuses: MutableMap<String, String>, prefix: String, configName: String?) {
        val api = K8sPluginInstanceApi(K8sConnectionPluginConfiguration(bluePrintPropertiesService))
        if (templateName == null || configName == null || instanceId == null || valueSource == null) {
            log.warn("$INPUT_K8S_RB_CONFIG_TEMPLATE_NAME or $INPUT_K8S_INSTANCE_ID or $INPUT_K8S_CONFIG_VALUE_SOURCE or $INPUT_K8S_RB_CONFIG_NAME is null - skipping create")
        } else if (templateName.isEmpty()) {
            log.warn("$INPUT_K8S_RB_CONFIG_TEMPLATE_NAME is empty - skipping create")
        } else if (configName.isEmpty()) {
            log.warn("$INPUT_K8S_RB_CONFIG_NAME is empty - skipping create")
        } else if (api.hasConfigurationValues(instanceId, configName)) {
            log.info("Configuration already exists - skipping create")
        } else {
            log.info("Uploading K8s config..")
            outputPrefixStatuses[prefix] = OUTPUT_ERROR
            val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
            val artifact: ArtifactDefinition = bluePrintContext.nodeTemplateArtifact(nodeTemplateName, valueSource)
            if (artifact.type != BluePrintConstants.MODEL_TYPE_ARTIFACT_K8S_CONFIG)
                throw BluePrintProcessorException(
                    "Unexpected config artifact type for config value source $valueSource. Expecting: $artifact.type"
                )
            val configValueRequest = K8sConfigValueRequest()
            configValueRequest.templateName = templateName
            configValueRequest.configName = configName
            configValueRequest.description = valueSource
            configValueRequest.values = parseResult(valueSource, artifact.file)
            api.createConfigurationValues(configValueRequest, instanceId)
        }
    }

    private suspend fun updateOperation(templateName: String?, instanceId: String?, valueSource: String?, outputPrefixStatuses: MutableMap<String, String>, prefix: String, configName: String?) {
        val api = K8sPluginInstanceApi(K8sConnectionPluginConfiguration(bluePrintPropertiesService))
        if (templateName == null || configName == null || instanceId == null || valueSource == null) {
            log.warn("$INPUT_K8S_RB_CONFIG_TEMPLATE_NAME or $INPUT_K8S_INSTANCE_ID or $INPUT_K8S_CONFIG_VALUE_SOURCE or $INPUT_K8S_RB_CONFIG_NAME is null - skipping update")
        } else if (templateName.isEmpty()) {
            log.warn("$INPUT_K8S_RB_CONFIG_TEMPLATE_NAME is empty - skipping update")
        } else if (configName.isEmpty()) {
            log.warn("$INPUT_K8S_RB_CONFIG_NAME is empty - skipping update")
        } else if (!api.hasConfigurationValues(instanceId, configName)) {
            log.info("Configuration does not exist - doing create instead")
            createOperation(templateName, instanceId, valueSource, outputPrefixStatuses, prefix, configName)
        } else {
            log.info("Updating K8s config..")
            outputPrefixStatuses[prefix] = OUTPUT_ERROR
            val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
            val artifact: ArtifactDefinition = bluePrintContext.nodeTemplateArtifact(nodeTemplateName, valueSource)
            if (artifact.type != BluePrintConstants.MODEL_TYPE_ARTIFACT_K8S_CONFIG)
                throw BluePrintProcessorException(
                    "Unexpected config artifact type for config value source $valueSource. Expecting: $artifact.type"
                )
            if (api.hasConfigurationValues(instanceId, configName)) {
                val configValueRequest = K8sConfigValueRequest()
                configValueRequest.templateName = templateName
                configValueRequest.configName = configName
                configValueRequest.description = valueSource
                configValueRequest.values = parseResult(valueSource, artifact.file)
                api.editConfigurationValues(configValueRequest, instanceId, configName)
            } else {
                throw BluePrintProcessorException("Error while getting configuration value")
            }
        }
    }

    private fun deleteOperation(instanceId: String?, configName: String?) {
        val api = K8sPluginInstanceApi(K8sConnectionPluginConfiguration(bluePrintPropertiesService))
        if (instanceId == null || configName == null) {
            log.warn("$INPUT_K8S_INSTANCE_ID or $INPUT_K8S_RB_CONFIG_NAME is null - skipping delete")
        } else if (api.hasConfigurationValues(instanceId, configName)) {
            log.info("Configuration does not exists - skipping delete")
        } else {
            api.deleteConfigurationValues(instanceId, configName)
        }
    }

    private suspend fun parseResult(templateValueSource: String, k8sConfigLocation: String): Any {
        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
        val bluePrintBasePath: String = bluePrintContext.rootPath
        val configeValueSourceFilePath: Path = Paths.get(
            bluePrintBasePath.plus(File.separator).plus(k8sConfigLocation)
        )

        if (!configeValueSourceFilePath.toFile().exists() || configeValueSourceFilePath.toFile().isDirectory)
            throw BluePrintProcessorException("Specified config value source $k8sConfigLocation is not a file")

        var obj: Any? = null
        val yamlReader = ObjectMapper(YAMLFactory())
        if (configeValueSourceFilePath.toFile().extension.toLowerCase() == "vtl") {
            log.info("Config building started from source $templateValueSource")
            val properties: MutableMap<String, Any> = mutableMapOf()
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT] = false
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = ""
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] = ""
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = ""
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] = 1
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_SUMMARY] = false
            val resolutionResult: Pair<String, MutableList<ResourceAssignment>> = resourceResolutionService.resolveResources(
                bluePrintRuntimeService,
                nodeTemplateName,
                templateValueSource,
                properties
            )

            val resolvedJsonContent = resolutionResult.second
                .associateBy({ it.name }, { it.property?.value })
                .asJsonNode()

            val newContent: String = templateValues(configeValueSourceFilePath.toFile(), resolvedJsonContent)
            obj = yamlReader.readValue(newContent, Any::class.java)
        } else {
            val ymlSourceFile = getYmlSourceFile(k8sConfigLocation)
            obj = yamlReader.readValue(ymlSourceFile, Any::class.java)
        }
        val jsonWriter = ObjectMapper()
        return jsonWriter.convertValue(obj)
    }

    private fun templateValues(templateFile: File, params: JsonNode): String {
        val fileContent = templateFile.bufferedReader().readText()
        return BluePrintVelocityTemplateService.generateContent(
            fileContent,
            params.toString(), true
        )
    }

    private fun getYmlSourceFile(templateValueSource: String): File {
        val bluePrintBasePath: String = bluePrintRuntimeService.bluePrintContext().rootPath
        val profileSourceFileFolderPath: Path = Paths.get(bluePrintBasePath.plus(File.separator).plus(templateValueSource))

        if (profileSourceFileFolderPath.toFile().exists() && !profileSourceFileFolderPath.toFile().isDirectory)
            return profileSourceFileFolderPath.toFile()
        else
            throw BluePrintProcessorException("Template value $profileSourceFileFolderPath is missing in CBA folder")
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
        addError(runtimeException.message!!)
    }

    private enum class OperationType {
        CREATE, UPDATE, DELETE
    }
}

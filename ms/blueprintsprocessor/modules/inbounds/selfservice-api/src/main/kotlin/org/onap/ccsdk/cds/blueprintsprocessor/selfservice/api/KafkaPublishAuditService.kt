/*
 * Copyright © 2020 Bell Canada
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BluePrintMessageLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BlueprintMessageProducerService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.common.ApplicationConstants
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.service.PropertyAssignmentService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.PropertyDefinitionUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

/**
 * Audit service used to produce execution service input and output message
 * sent into dedicated kafka topics.
 *
 * @param bluePrintMessageLibPropertyService Service used to instantiate audit service producers
 * @param blueprintsProcessorCatalogService Service used to get the base path of the current CBA executed
 *
 * @property inputInstance Request Kakfa Producer instance
 * @property outputInstance Response Kakfa Producer instance
 * @property log Audit Service logger
 */
@ConditionalOnProperty(
    name = ["blueprintsprocessor.messageproducer.self-service-api.audit.kafkaEnable"],
    havingValue = "true"
)
@Service
class KafkaPublishAuditService(
    private val bluePrintMessageLibPropertyService: BluePrintMessageLibPropertyService,
    private val blueprintsProcessorCatalogService: BluePrintCatalogService
) : PublishAuditService {

    private var inputInstance: BlueprintMessageProducerService? = null
    private var outputInstance: BlueprintMessageProducerService? = null
    private val log = LoggerFactory.getLogger(KafkaPublishAuditService::class.toString())

    companion object {

        const val INPUT_SELECTOR = "self-service-api.audit.request"
        const val OUTPUT_SELECTOR = "self-service-api.audit.response"
    }

    @PostConstruct
    private fun init() {
        log.info("Kakfa audit service is enabled")
    }

    /**
     * Publish execution input into a kafka topic.
     * The correlation UUID is used to link the input to its output.
     * Sensitive data within the request are hidden.
     * @param executionServiceInput Audited BP request
     */
    override suspend fun publishExecutionInput(executionServiceInput: ExecutionServiceInput) {
        val secureExecutionServiceInput = hideSensitiveData(executionServiceInput)
        val key = secureExecutionServiceInput.actionIdentifiers.blueprintName
        try {
            this.inputInstance = this.getInputInstance(INPUT_SELECTOR)
            this.inputInstance!!.sendMessage(key, secureExecutionServiceInput)
        } catch (e: Exception) {
            var errMsg =
                if (e.message != null) "ERROR : ${e.message}"
                else "ERROR : Failed to send execution request to Kafka."
            log.error(errMsg)
        }
    }

    /**
     * Publish execution output into a kafka topic.
     * The correlation UUID is used to link the output to its input.
     * A correlation UUID is added to link the input to its output.
     * @param correlationUUID UUID used to link the audited response to its audited request
     * @param executionServiceOutput Audited BP response
     */
    override suspend fun publishExecutionOutput(correlationUUID: String, executionServiceOutput: ExecutionServiceOutput) {
        executionServiceOutput.correlationUUID = correlationUUID
        val key = executionServiceOutput.actionIdentifiers.blueprintName
        try {
            this.outputInstance = this.getOutputInstance(OUTPUT_SELECTOR)
            this.outputInstance!!.sendMessage(key, executionServiceOutput)
        } catch (e: Exception) {
            var errMsg =
                if (e.message != null) "ERROR : $e"
                else "ERROR : Failed to send execution request to Kafka."
            log.error(errMsg)
        }
    }

    /**
     * Return the input kafka producer instance using a [selector] if not already instantiated.
     * @param selector Selector to retrieve request kafka producer configuration
     */
    private fun getInputInstance(selector: String): BlueprintMessageProducerService = inputInstance ?: createInstance(selector)

    /**
     * Return the output kafka producer instance using a [selector] if not already instantiated.
     * @param selector Selector to retrieve response kafka producer configuration
     */
    private fun getOutputInstance(selector: String): BlueprintMessageProducerService = outputInstance ?: createInstance(selector)

    /**
     * Create a kafka producer instance using a [selector].
     * @param selector Selector to retrieve kafka producer configuration
     */
    private fun createInstance(selector: String): BlueprintMessageProducerService {
        log.info("Setting up message producer($selector)...")
        return bluePrintMessageLibPropertyService.blueprintMessageProducerService(selector)
    }

    /**
     * Hide sensitive data in the [executionServiceInput].
     * Sensitive data are declared in the resource resolution mapping using
     * the property metadata "log-protect" set to true.
     * @param executionServiceInput BP Execution Request where data needs to be hidden
     */
    private suspend fun hideSensitiveData(
        executionServiceInput: ExecutionServiceInput
    ): ExecutionServiceInput {

        var clonedExecutionServiceInput = ExecutionServiceInput().apply {
            correlationUUID = executionServiceInput.correlationUUID
            commonHeader = executionServiceInput.commonHeader
            actionIdentifiers = executionServiceInput.actionIdentifiers
            payload = executionServiceInput.payload.deepCopy()
            stepData = executionServiceInput.stepData
        }

        val blueprintName = clonedExecutionServiceInput.actionIdentifiers.blueprintName
        val workflowName = clonedExecutionServiceInput.actionIdentifiers.actionName

        if (blueprintName == "default") return clonedExecutionServiceInput

        try {
            if (clonedExecutionServiceInput.payload
                .path("$workflowName-request").has("$workflowName-properties")
            ) {

                /** Retrieving sensitive input parameters */
                val requestId = clonedExecutionServiceInput.commonHeader.requestId
                val blueprintVersion = clonedExecutionServiceInput.actionIdentifiers.blueprintVersion

                val basePath = blueprintsProcessorCatalogService.getFromDatabase(blueprintName, blueprintVersion)

                val blueprintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(requestId, basePath.toString())
                val blueprintContext = blueprintRuntimeService.bluePrintContext()

                val workflowSteps = blueprintContext.workflowByName(workflowName).steps
                checkNotNull(workflowSteps) { "Failed to get step(s) for workflow($workflowName)" }
                workflowSteps.forEach { step ->
                    val nodeTemplateName = step.value.target
                    checkNotNull(nodeTemplateName) { "Failed to get node template target for workflow($workflowName), step($step)" }
                    val nodeTemplate = blueprintContext.nodeTemplateByName(nodeTemplateName)

                    /** We need to check in his Node Template Dependencies is case of a Node Template DG */
                    if (nodeTemplate.type == BluePrintConstants.NODE_TEMPLATE_TYPE_DG) {
                        val dependencyNodeTemplate =
                            nodeTemplate.properties?.get(BluePrintConstants.PROPERTY_DG_DEPENDENCY_NODE_TEMPLATE) as ArrayNode
                        dependencyNodeTemplate.forEach { dependencyNodeTemplateName ->
                            clonedExecutionServiceInput = hideSensitiveDataFromResourceResolution(
                                blueprintRuntimeService,
                                blueprintContext,
                                clonedExecutionServiceInput,
                                workflowName,
                                dependencyNodeTemplateName.asText()
                            )
                        }
                    } else {
                        clonedExecutionServiceInput = hideSensitiveDataFromResourceResolution(
                            blueprintRuntimeService,
                            blueprintContext,
                            clonedExecutionServiceInput,
                            workflowName,
                            nodeTemplateName
                        )
                    }
                }
            }
        } catch (e: Exception) {
            val errMsg = "ERROR : Couldn't hide sensitive data in the execution request."
            log.error(errMsg, e)
            clonedExecutionServiceInput.payload.replace(
                "$workflowName-request",
                "$errMsg $e".asJsonPrimitive()
            )
        }
        return clonedExecutionServiceInput
    }

    /**
     * Hide sensitive data in [executionServiceInput] if the given [nodeTemplateName] is a
     * resource resolution component.
     * @param blueprintRuntimeService Current blueprint runtime service
     * @param blueprintContext Current blueprint runtime context
     * @param executionServiceInput BP Execution Request where data needs to be hidden
     * @param workflowName Current workflow being executed
     * @param nodeTemplateName Node template to check for sensitive data
     * @return [executionServiceInput] with sensitive inputs replaced by a generic string
     */
    private suspend fun hideSensitiveDataFromResourceResolution(
        blueprintRuntimeService: BluePrintRuntimeService<MutableMap<String, JsonNode>>,
        blueprintContext: BluePrintContext,
        executionServiceInput: ExecutionServiceInput,
        workflowName: String,
        nodeTemplateName: String
    ): ExecutionServiceInput {

        val nodeTemplate = blueprintContext.nodeTemplateByName(nodeTemplateName)
        if (nodeTemplate.type == BluePrintConstants.NODE_TEMPLATE_TYPE_COMPONENT_RESOURCE_RESOLUTION) {
            val interfaceName = blueprintContext.nodeTemplateFirstInterfaceName(nodeTemplateName)
            val operationName = blueprintContext.nodeTemplateFirstInterfaceFirstOperationName(nodeTemplateName)

            val propertyAssignments: MutableMap<String, JsonNode> =
                blueprintContext.nodeTemplateInterfaceOperationInputs(nodeTemplateName, interfaceName, operationName)
                    ?: hashMapOf()

            /** Getting values define in artifact-prefix-names */
            val input = executionServiceInput.payload.get("$workflowName-request")
            blueprintRuntimeService.assignWorkflowInputs(workflowName, input)
            val artifactPrefixNamesNode = propertyAssignments[ResourceResolutionConstants.INPUT_ARTIFACT_PREFIX_NAMES]
            val propertyAssignmentService = PropertyAssignmentService(blueprintRuntimeService)
            val artifactPrefixNamesNodeValue = propertyAssignmentService.resolveAssignmentExpression(
                BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TEMPLATE,
                nodeTemplateName,
                ResourceResolutionConstants.INPUT_ARTIFACT_PREFIX_NAMES,
                artifactPrefixNamesNode!!
            )

            val artifactPrefixNames = JacksonUtils.getListFromJsonNode(artifactPrefixNamesNodeValue!!, String::class.java)

            /** Storing mapping entries with metadata log-protect set to true */
            val sensitiveParameters: List<String> = artifactPrefixNames
                .map { "$it-mapping" }
                .map { blueprintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, it) }
                .flatMap { JacksonUtils.getListFromJson(it, ResourceAssignment::class.java) }
                .filter { PropertyDefinitionUtils.hasLogProtect(it.property) }
                .map { it.name }

            /** Hiding sensitive input parameters from the request */
            var workflowProperties: ObjectNode = executionServiceInput.payload
                .path("$workflowName-request")
                .path("$workflowName-properties") as ObjectNode

            sensitiveParameters.forEach { sensitiveParameter ->
                if (workflowProperties.has(sensitiveParameter)) {
                    workflowProperties.replace(sensitiveParameter, ApplicationConstants.LOG_REDACTED.asJsonPrimitive())
                }
            }
        }
        return executionServiceInput
    }
}

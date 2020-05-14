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
import com.fasterxml.jackson.databind.node.ObjectNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BluePrintMessageLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BlueprintMessageProducerService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.common.ApplicationConstants
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
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
     */
    override suspend fun publish(executionServiceInput: ExecutionServiceInput) {
        val secureExecutionServiceInput = hideSensitiveData(executionServiceInput)
        this.inputInstance = this.getInputInstance(INPUT_SELECTOR)
        this.inputInstance!!.sendMessage(secureExecutionServiceInput)
    }

    /**
     * Publish execution output into a kafka topic.
     * The correlation UUID is used to link the output to its input.
     * A correlation UUID is added to link the input to its output.
     */
    override fun publish(correlationUUID: String, executionServiceOutput: ExecutionServiceOutput) {
        executionServiceOutput.correlationUUID = correlationUUID
        this.outputInstance = this.getOutputInstance(OUTPUT_SELECTOR)
        this.outputInstance!!.sendMessage(executionServiceOutput)
    }

    /**
     * Return the input kafka producer instance using a selector.
     */
    private fun getInputInstance(selector: String): BlueprintMessageProducerService = inputInstance ?: createInstance(selector)

    /**
     * Return the output kafka producer instance using a selector.
     */
    private fun getOutputInstance(selector: String): BlueprintMessageProducerService = outputInstance ?: createInstance(selector)

    /**
     * Create a kafka producer instance.
     */
    private fun createInstance(selector: String): BlueprintMessageProducerService {
        log.info(
                "Setting up message producer($selector)..."
        )
        return try {
            bluePrintMessageLibPropertyService
                    .blueprintMessageProducerService(selector)
        } catch (e: Exception) {
            throw BluePrintProcessorException("failed to create producer service ${e.message}")
        }
    }

    /**
     * Hide sensitive data in the request.
     * Sensitive data are declared in the resource resolution mapping using
     * the property metadata "log-protect" set to true.
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

        if (clonedExecutionServiceInput.payload
                        .path("$workflowName-request").has("$workflowName-properties")) {

            /** Retrieving sensitive input parameters */
            val requestId = clonedExecutionServiceInput.commonHeader.requestId
            val blueprintVersion = clonedExecutionServiceInput.actionIdentifiers.blueprintVersion

            val basePath = blueprintsProcessorCatalogService.getFromDatabase(blueprintName, blueprintVersion)

            val blueprintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(requestId, basePath.toString())
            val blueprintContext = blueprintRuntimeService.bluePrintContext()

            /** Looking for node templates defined as component-resource-resolution */
            val nodeTemplates = blueprintContext.nodeTemplates()
            nodeTemplates!!.forEach { nodeTemplate ->
                val nodeTemplateName = nodeTemplate.key
                val nodeTemplateType = blueprintContext.nodeTemplateByName(nodeTemplateName).type
                if (nodeTemplateType == BluePrintConstants.NODE_TEMPLATE_TYPE_COMPONENT_RESOURCE_RESOLUTION) {
                    val interfaceName = blueprintContext.nodeTemplateFirstInterfaceName(nodeTemplateName)
                    val operationName = blueprintContext.nodeTemplateFirstInterfaceFirstOperationName(nodeTemplateName)

                    val propertyAssignments: MutableMap<String, JsonNode> =
                            blueprintContext.nodeTemplateInterfaceOperationInputs(nodeTemplateName, interfaceName, operationName)
                                    ?: hashMapOf()

                    val artifactPrefixNamesNode = propertyAssignments[ResourceResolutionConstants.INPUT_ARTIFACT_PREFIX_NAMES]
                    val artifactPrefixNames = JacksonUtils.getListFromJsonNode(artifactPrefixNamesNode!!, String::class.java)

                    /** Storing mapping entries with metadata log-protect set to true */
                    val sensitiveParameters: List<String> = artifactPrefixNames
                            .map { "$it-mapping" }
                            .map { blueprintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, it) }
                            .flatMap { JacksonUtils.getListFromJson(it, ResourceAssignment::class.java) }
                            .filter { PropertyDefinitionUtils.hasLogProtect(it.property) }
                            .map { it.name }

                    /** Hiding sensitive input parameters from the request */
                    var workflowProperties: ObjectNode = clonedExecutionServiceInput.payload
                            .path("$workflowName-request")
                            .path("$workflowName-properties") as ObjectNode

                    sensitiveParameters.forEach { sensitiveParameter ->
                        if (workflowProperties.has(sensitiveParameter)) {
                            workflowProperties.replace(sensitiveParameter, ApplicationConstants.LOG_REDACTED.asJsonPrimitive())
                        }
                    }
                }
            }
        }

        return clonedExecutionServiceInput
    }
}

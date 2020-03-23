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
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.common.ApplicationConstants
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.PropertyDefinitionUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Audit service used to produce execution service input and output message
 * sent into dedicated kafka topics.
 */
@ConditionalOnProperty(
        name = ["blueprintsprocessor.messageconsumer.self-service-api.kafkaEnable"],
        havingValue = "true"
)
@Service
class KafkaPublishAuditService(
    private val bluePrintMessageLibPropertyService: BluePrintMessageLibPropertyService
) : PublishAuditService {

    private var inputInstance: BlueprintMessageProducerService? = null
    private var outputInstance: BlueprintMessageProducerService? = null

    private var correlationUUID: String? = null

    private val log = LoggerFactory.getLogger(KafkaPublishAuditService::class.toString())

    companion object {
        const val OUTPUT_SELECTOR = "self-service-api.output"
        const val INPUT_SELECTOR = "self-service-api.input"
    }

    /**
     * Publish execution input into a kafka topic.
     * The correlation UUID is used to link the input to its output.
     * Sensitive data within the request are hidden.
     */
    override suspend fun publish(executionServiceInput: ExecutionServiceInput, blueprintRuntimeService: BluePrintRuntimeService<*>?) {
        this.correlationUUID = UUID.randomUUID().toString()
        executionServiceInput.correlationUUID = this.correlationUUID
        if (blueprintRuntimeService != null) {
            hideSensitiveData(executionServiceInput, blueprintRuntimeService)
        }
        this.getInputInstance(INPUT_SELECTOR).sendMessage(executionServiceInput)
    }

    /**
     * Publish execution output into a kafka topic.
     * The correlation UUID is used to link the output to its input.
     * A correlation UUID is added to link the input to its output.
     */
    override fun publish(executionServiceOutput: ExecutionServiceOutput) {
        executionServiceOutput.correlationUUID = this.correlationUUID
        this.getOutputInstance(OUTPUT_SELECTOR).sendMessage(executionServiceOutput)
    }

    /**
     * Return the input kafka producer instance using a selector.
     */
    private fun getInputInstance(selector: String): BlueprintMessageProducerService {
        if (inputInstance == null) {
            inputInstance = createInstance(selector)
        }
        return inputInstance!!
    }

    /**
     * Return the output kafka producer instance using a selector.
     */
    private fun getOutputInstance(selector: String): BlueprintMessageProducerService {
        if (outputInstance == null) {
            outputInstance = createInstance(selector)
        }
        return outputInstance!!
    }

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
        executionServiceInput: ExecutionServiceInput,
        blueprintRuntimeService: BluePrintRuntimeService<*>
    ): ExecutionServiceInput {

        /** Retrieving sensitive input parameters */
        val blueprintContext = blueprintRuntimeService.bluePrintContext()

        val workflowName = executionServiceInput.actionIdentifiers.actionName
        val nodeTemplateName = blueprintContext.workflowFirstStepNodeTemplate(workflowName)
        val interfaceName = blueprintContext.nodeTemplateFirstInterfaceName(nodeTemplateName)
        val operationName = blueprintContext.nodeTemplateFirstInterfaceFirstOperationName(nodeTemplateName)

        val propertyAssignments: MutableMap<String, JsonNode> =
                blueprintContext.nodeTemplateInterfaceOperationInputs(nodeTemplateName, interfaceName, operationName)
                        ?: hashMapOf()
        val artifactPrefixNamesNode = propertyAssignments[ResourceResolutionConstants.INPUT_ARTIFACT_PREFIX_NAMES]
        val artifactPrefixNames = JacksonUtils.getListFromJsonNode(artifactPrefixNamesNode!!, String::class.java)
        var sensitiveParameters = mutableSetOf<String>()
        artifactPrefixNames.forEach { artifactPrefix ->
            val artifactMapping = "$artifactPrefix-mapping"
            val resourceAssignmentContent =
                    blueprintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactMapping)
            val resourceAssignments: MutableList<ResourceAssignment> =
                    JacksonUtils.getListFromJson(resourceAssignmentContent, ResourceAssignment::class.java)
                            as? MutableList<ResourceAssignment>
                            ?: throw BluePrintProcessorException("couldn't get Dictionary Definitions")
            resourceAssignments.forEach { resourceAssignment ->
                if (resourceAssignment.dictionaryName == "input-source" &&
                        PropertyDefinitionUtils.hasLogProtect(resourceAssignment.property)
                ) {
                    sensitiveParameters.add(resourceAssignment.name)
                }
            }
        }

        /** Hiding sensitive input parameters from the request */
        var clonedExecutionServiceInput = ExecutionServiceInput().apply {
            commonHeader = executionServiceInput.commonHeader
            actionIdentifiers = executionServiceInput.actionIdentifiers
            payload = executionServiceInput.payload
        }

        var workflowProperties: ObjectNode = clonedExecutionServiceInput.payload
                .path("$workflowName-request")
                .path("$workflowName-properties") as ObjectNode

        sensitiveParameters.forEach { sensitiveParameter ->
            workflowProperties.remove(sensitiveParameter)
            workflowProperties.put(sensitiveParameter, ApplicationConstants.LOG_REDACTED)
        }

        return clonedExecutionServiceInput
    }
}

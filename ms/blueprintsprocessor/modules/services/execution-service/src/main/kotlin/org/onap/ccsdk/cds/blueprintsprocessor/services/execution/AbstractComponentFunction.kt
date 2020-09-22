/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.withTimeout
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.core.cluster.executeWithLock
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.CDS_LOCK_GROUP
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotBlank
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.data.Implementation
import org.onap.ccsdk.cds.controllerblueprints.core.getAsString
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.cds.controllerblueprints.core.jsonPathParse
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.readNBLines
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintVelocityTemplateService
import org.slf4j.LoggerFactory

/**
 * AbstractComponentFunction
 * @author Brinda Santh
 */
abstract class AbstractComponentFunction : BlueprintFunctionNode<ExecutionServiceInput, ExecutionServiceOutput> {

    @Transient
    private val log = LoggerFactory.getLogger(AbstractComponentFunction::class.java)

    lateinit var executionServiceInput: ExecutionServiceInput
    var executionServiceOutput = ExecutionServiceOutput()
    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var bluePrintClusterService: BluePrintClusterService
    lateinit var implementation: Implementation
    lateinit var processId: String
    lateinit var workflowName: String
    lateinit var stepName: String
    lateinit var interfaceName: String
    lateinit var operationName: String
    lateinit var nodeTemplateName: String
    var operationInputs: MutableMap<String, JsonNode> = hashMapOf()

    override fun getName(): String {
        return stepName
    }

    override suspend fun prepareRequestNB(executionRequest: ExecutionServiceInput): ExecutionServiceInput {
        check(this::bluePrintRuntimeService.isInitialized) { "failed to prepare blueprint runtime" }
        checkNotNull(executionRequest.stepData) { "failed to get step info" }

        // Get the Step Name and Step Inputs
        this.stepName = executionRequest.stepData!!.name
        this.operationInputs = executionRequest.stepData!!.properties

        checkNotEmpty(stepName) { "failed to get step name from step data" }

        this.executionServiceInput = executionRequest

        processId = executionRequest.commonHeader.requestId
        check(processId.isNotEmpty()) { "couldn't get process id for step($stepName)" }

        workflowName = executionRequest.actionIdentifiers.actionName
        check(workflowName.isNotEmpty()) { "couldn't get action name for step($stepName)" }

        log.info("preparing request id($processId) for workflow($workflowName) step($stepName)")

        nodeTemplateName = this.operationInputs.getAsString(BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE)
        check(nodeTemplateName.isNotEmpty()) { "couldn't get NodeTemplate name for step($stepName)" }

        interfaceName = this.operationInputs.getAsString(BluePrintConstants.PROPERTY_CURRENT_INTERFACE)
        check(interfaceName.isNotEmpty()) { "couldn't get Interface name for step($stepName)" }

        operationName = this.operationInputs.getAsString(BluePrintConstants.PROPERTY_CURRENT_OPERATION)
        check(operationName.isNotEmpty()) { "couldn't get Operation name for step($stepName)" }

        /** Get the Implementation Details */
        implementation = bluePrintRuntimeService.bluePrintContext()
            .nodeTemplateOperationImplementation(nodeTemplateName, interfaceName, operationName)
            ?: Implementation()

        /** Resolve and validate lock properties */
        implementation.lock?.apply {
            val resolvedValues = bluePrintRuntimeService.resolvePropertyAssignments(
                BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TEMPLATE,
                interfaceName,
                mutableMapOf("key" to this.key, "acquireTimeout" to this.acquireTimeout)
            )
            this.key = resolvedValues["key"] ?: "".asJsonType()
            this.acquireTimeout = resolvedValues["acquireTimeout"] ?: "".asJsonType()

            checkNotBlank(this.key.textValue()) { "Failed to resolve lock key" }
            check(this.acquireTimeout.isInt && this.acquireTimeout.intValue() >= 0) {
                "Failed to resolve lock acquireTimeout - must be a positive integer"
            }
        }

        check(this::implementation.isInitialized) { "failed to prepare implementation" }

        val operationResolvedProperties = bluePrintRuntimeService
            .resolveNodeTemplateInterfaceOperationInputs(nodeTemplateName, interfaceName, operationName)

        this.operationInputs.putAll(operationResolvedProperties)

        return executionRequest
    }

    override suspend fun prepareResponseNB(): ExecutionServiceOutput {
        log.info("Preparing Response...")
        executionServiceOutput.commonHeader = executionServiceInput.commonHeader
        executionServiceOutput.actionIdentifiers = executionServiceInput.actionIdentifiers
        val status = Status()
        try {
            // Resolve the Output Expression
            val stepOutputs = bluePrintRuntimeService
                .resolveNodeTemplateInterfaceOperationOutputs(nodeTemplateName, interfaceName, operationName)

            val stepOutputData = StepData().apply {
                name = stepName
                properties = stepOutputs
            }
            executionServiceOutput.stepData = stepOutputData
            // Set the Default Step Status
            status.eventType = EventType.EVENT_COMPONENT_EXECUTED.name
        } catch (e: Exception) {
            status.message = BluePrintConstants.STATUS_FAILURE
            status.eventType = EventType.EVENT_COMPONENT_FAILURE.name
        }
        executionServiceOutput.status = status
        return this.executionServiceOutput
    }

    override suspend fun applyNB(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        try {
            prepareRequestNB(executionServiceInput)
            implementation.lock?.let {
                bluePrintClusterService.clusterLock("${it.key.textValue()}@$CDS_LOCK_GROUP")
                    .executeWithLock(it.acquireTimeout.intValue().times(1000).toLong()) {
                        applyNBWithTimeout(executionServiceInput)
                    }
            } ?: applyNBWithTimeout(executionServiceInput)
        } catch (runtimeException: RuntimeException) {
            log.error("failed in ${getName()} : ${runtimeException.message}", runtimeException)
            recoverNB(runtimeException, executionServiceInput)
        }
        return prepareResponseNB()
    }

    private suspend fun applyNBWithTimeout(executionServiceInput: ExecutionServiceInput) =
        withTimeout((implementation.timeout * 1000).toLong()) {
            log.debug(
                "DEBUG::: AbstractComponentFunction.withTimeout " +
                    "section ${implementation.timeout} seconds"
            )
            processNB(executionServiceInput)
        }

    fun getOperationInput(key: String): JsonNode {
        return operationInputs[key]
            ?: throw BluePrintProcessorException("couldn't get the operation input($key) value.")
    }

    fun getOptionalOperationInput(key: String): JsonNode? {
        return operationInputs[key]
    }

    fun setAttribute(key: String, value: JsonNode) {
        bluePrintRuntimeService.setNodeTemplateAttributeValue(nodeTemplateName, key, value)
    }

    fun addError(type: String, name: String, error: String) {
        bluePrintRuntimeService.getBluePrintError().addError(type, name, error)
    }

    fun addError(error: String) {
        bluePrintRuntimeService.getBluePrintError().addError(error)
    }

    /**
     * Get Execution Input Payload data
     */
    fun requestPayload(): JsonNode? {
        return executionServiceInput.payload
    }

    /**
     * Get Execution Input payload action property with [expression]
     * ex: requestPayloadActionProperty("data") will look for path "payload/<action-name>-request/data"
     */
    fun requestPayloadActionProperty(expression: String?): JsonNode? {
        val requestExpression = if (expression.isNullOrBlank()) {
            "$workflowName-request"
        } else {
            "$workflowName-request.$expression"
        }
        return executionServiceInput.payload.jsonPathParse(".$requestExpression")
    }

    suspend fun artifactContent(artifactName: String): String {
        return bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactName)
    }

    suspend fun relationshipProperty(relationshipName: String, propertyName: String): JsonNode {
        return bluePrintRuntimeService.resolveRelationshipTemplateProperties(relationshipName).get(propertyName)
            ?: throw BluePrintProcessorException("failed to get relationship($relationshipName) property($propertyName)")
    }

    suspend fun mashTemplateNData(artifactName: String, json: String): String {
        val content = artifactContent(artifactName)
        return BluePrintVelocityTemplateService.generateContent(content, json)
    }

    suspend fun readLinesFromArtifact(artifactName: String): List<String> {
        val artifactDefinition =
            bluePrintRuntimeService.resolveNodeTemplateArtifactDefinition(nodeTemplateName, artifactName)
        val file = normalizedFile(bluePrintRuntimeService.bluePrintContext().rootPath, artifactDefinition.file)
        return file.readNBLines()
    }
}

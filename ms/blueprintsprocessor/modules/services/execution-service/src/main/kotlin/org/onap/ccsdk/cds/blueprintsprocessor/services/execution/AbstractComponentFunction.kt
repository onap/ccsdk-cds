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
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.getAsString
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
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
        checkNotNull(bluePrintRuntimeService) { "failed to prepare blueprint runtime" }
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

        val operationResolvedProperties = bluePrintRuntimeService
                .resolveNodeTemplateInterfaceOperationInputs(nodeTemplateName, interfaceName, operationName)

        this.operationInputs.putAll(operationResolvedProperties)

        return executionRequest
    }

    override suspend fun prepareResponseNB(): ExecutionServiceOutput {
        log.info("Preparing Response...")
        executionServiceOutput.commonHeader = executionServiceInput.commonHeader
        executionServiceOutput.actionIdentifiers = executionServiceInput.actionIdentifiers
        var status = Status()
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
            processNB(executionServiceInput)
        } catch (runtimeException: RuntimeException) {
            log.error("failed in ${getName()} : ${runtimeException.message}", runtimeException)
            recoverNB(runtimeException, executionServiceInput)
        }
        return prepareResponseNB()
    }

    fun getOperationInput(key: String): JsonNode {
        return operationInputs[key]
                ?: throw BluePrintProcessorException("couldn't get the operation input($key) value.")
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

}
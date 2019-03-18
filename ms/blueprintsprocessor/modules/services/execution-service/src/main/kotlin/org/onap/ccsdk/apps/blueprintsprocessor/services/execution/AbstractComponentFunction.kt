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

package org.onap.ccsdk.apps.blueprintsprocessor.services.execution


import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.apps.controllerblueprints.common.api.EventType
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.asObjectNode
import org.onap.ccsdk.apps.controllerblueprints.core.getAsString
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
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

    override fun prepareRequest(executionRequest: ExecutionServiceInput): ExecutionServiceInput {
        checkNotNull(bluePrintRuntimeService) { "failed to prepare blueprint runtime" }

        check(stepName.isNotEmpty()) { "failed to assign step name" }

        this.executionServiceInput = executionRequest

        processId = executionRequest.commonHeader.requestId
        check(processId.isNotEmpty()) { "couldn't get process id for step($stepName)" }

        workflowName = executionRequest.actionIdentifiers.actionName
        check(workflowName.isNotEmpty()) { "couldn't get action name for step($stepName)" }

        log.info("preparing request id($processId) for workflow($workflowName) step($stepName)")

        val operationInputs = bluePrintRuntimeService.get("$stepName-step-inputs")
                ?: JsonNodeFactory.instance.objectNode()

        operationInputs.fields().forEach {
            this.operationInputs[it.key] = it.value
        }

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

    override fun prepareResponse(): ExecutionServiceOutput {
        log.info("Preparing Response...")
        executionServiceOutput.commonHeader = executionServiceInput.commonHeader
        executionServiceOutput.actionIdentifiers = executionServiceInput.actionIdentifiers

        // Resolve the Output Expression
        val stepOutputs = bluePrintRuntimeService
                .resolveNodeTemplateInterfaceOperationOutputs(nodeTemplateName, interfaceName, operationName)

        // FIXME("Not the right place to populate the response payload")
        executionServiceOutput.payload = stepOutputs.asObjectNode()

        bluePrintRuntimeService.put("$stepName-step-outputs", executionServiceOutput.payload)

        // FIXME("Not the right place to populate the status")
        // Populate Status
        val status = Status()
        status.eventType = EventType.EVENT_COMPONENT_EXECUTED.name
        status.code = 200
        status.message = BluePrintConstants.STATUS_SUCCESS
        executionServiceOutput.status = status
        return this.executionServiceOutput
    }

    override fun apply(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        try {
            prepareRequest(executionServiceInput)
            process(executionServiceInput)
        } catch (runtimeException: RuntimeException) {
            recover(runtimeException, executionServiceInput)
        }
        return prepareResponse()
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
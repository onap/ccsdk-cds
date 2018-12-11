/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
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
import com.fasterxml.jackson.databind.node.NullNode
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.getAsString
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.apps.controllerblueprints.core.putJsonElement
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.slf4j.LoggerFactory

/**
 * AbstractComponentFunction
 * @author Brinda Santh
 */
abstract class AbstractComponentFunction : BlueprintFunctionNode<ExecutionServiceInput, ExecutionServiceOutput> {
    @Transient
    private val log = LoggerFactory.getLogger(AbstractComponentFunction::class.java)

    var executionServiceInput: ExecutionServiceInput? = null
    var executionServiceOutput = ExecutionServiceOutput()
    var bluePrintRuntimeService: BluePrintRuntimeService<*>? = null
    var processId: String = ""
    var workflowName: String = ""
    var stepName: String = ""
    var interfaceName: String = ""
    var operationName: String = ""
    var nodeTemplateName: String = ""
    var operationInputs: MutableMap<String, JsonNode> = hashMapOf()


    override fun prepareRequest(executionServiceInput: ExecutionServiceInput): ExecutionServiceInput {
        checkNotNull(bluePrintRuntimeService) { "failed to prepare blueprint runtime" }

        this.executionServiceInput = executionServiceInput

        processId = executionServiceInput.commonHeader.requestId
        workflowName = executionServiceInput.actionIdentifiers.actionName

        val metadata = executionServiceInput.metadata
        stepName = metadata.getAsString(BluePrintConstants.PROPERTY_CURRENT_STEP)
        log.info("preparing request id($processId) for workflow($workflowName) step($stepName)")

        val operationInputs = metadata.get("$stepName-step-inputs") ?: JsonNodeFactory.instance.objectNode()

        operationInputs.fields().forEach {
            this.operationInputs[it.key] = it.value
        }

        nodeTemplateName = this.operationInputs.getAsString(BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE)
        interfaceName = this.operationInputs.getAsString(BluePrintConstants.PROPERTY_CURRENT_INTERFACE)
        operationName = this.operationInputs.getAsString(BluePrintConstants.PROPERTY_CURRENT_OPERATION)


        val operationResolvedProperties = bluePrintRuntimeService!!.resolveNodeTemplateInterfaceOperationInputs(nodeTemplateName, interfaceName, operationName)

        this.operationInputs.putAll(operationResolvedProperties)



        return executionServiceInput
    }

    override fun prepareResponse(): ExecutionServiceOutput {
        log.info("Preparing Response...")
        executionServiceOutput.commonHeader = executionServiceInput!!.commonHeader

        // Resolve the Output Expression
        val operationResolvedProperties = bluePrintRuntimeService!!
                .resolveNodeTemplateInterfaceOperationOutputs(nodeTemplateName, interfaceName, operationName)

        val metadata = executionServiceOutput.metadata
        metadata.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_STEP, stepName)
        metadata.putJsonElement("$stepName-step-outputs", operationResolvedProperties)

        // Populate Status
        val status = Status()
        status.eventType = "EVENT-COMPONENT-EXECUTED"
        status.code = 200
        status.message = BluePrintConstants.STATUS_SUCCESS
        executionServiceOutput.status = status
        return this.executionServiceOutput
    }

    override fun apply(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        prepareRequest(executionServiceInput)
        process(executionServiceInput)
        return prepareResponse()
    }

    fun getOperationInput(key: String): JsonNode {
        return operationInputs.get(key) ?: NullNode.instance
    }
}
/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.workflow

import kotlinx.coroutines.CompletableDeferred
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.data.EdgeLabel
import org.onap.ccsdk.cds.controllerblueprints.core.data.Graph
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintWorkflowExecutionService
import org.onap.ccsdk.cds.controllerblueprints.core.service.*
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("imperativeWorkflowExecutionService")
class ImperativeWorkflowExecutionService(
        private val imperativeBluePrintWorkflowService: BluePrintWorkFlowService<ExecutionServiceInput, ExecutionServiceOutput>)
    : BluePrintWorkflowExecutionService<ExecutionServiceInput, ExecutionServiceOutput> {

    override suspend fun executeBluePrintWorkflow(bluePrintRuntimeService: BluePrintRuntimeService<*>,
                                                  executionServiceInput: ExecutionServiceInput,
                                                  properties: MutableMap<String, Any>): ExecutionServiceOutput {

        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val workflowName = executionServiceInput.actionIdentifiers.actionName

        val graph = bluePrintContext.workflowByName(workflowName).asGraph()

        val deferredOutput = CompletableDeferred<ExecutionServiceOutput>()
        imperativeBluePrintWorkflowService.executeWorkflow(graph, bluePrintRuntimeService,
                executionServiceInput, deferredOutput)
        return deferredOutput.await()
    }
}

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ImperativeBluePrintWorkflowService(private val nodeTemplateExecutionService: NodeTemplateExecutionService)
    : AbstractBluePrintWorkFlowService<ExecutionServiceInput, ExecutionServiceOutput>() {
    val log = logger(ImperativeBluePrintWorkflowService::class)

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var executionServiceInput: ExecutionServiceInput
    lateinit var workflowName: String
    lateinit var deferredExecutionServiceOutput: CompletableDeferred<ExecutionServiceOutput>

    override suspend fun executeWorkflow(graph: Graph, bluePrintRuntimeService: BluePrintRuntimeService<*>,
                                         input: ExecutionServiceInput,
                                         output: CompletableDeferred<ExecutionServiceOutput>) {
        this.graph = graph
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.executionServiceInput = input
        this.workflowName = this.executionServiceInput.actionIdentifiers.actionName
        this.deferredExecutionServiceOutput = output
        this.workflowId = bluePrintRuntimeService.id()
        val startMessage = WorkflowExecuteMessage(input, output)
        workflowActor().send(startMessage)
    }

    override suspend fun initializeWorkflow(input: ExecutionServiceInput): EdgeLabel {
        return EdgeLabel.SUCCESS
    }

    override suspend fun prepareWorkflowOutput(exception: BluePrintProcessorException?): ExecutionServiceOutput {
        val wfStatus = if (exception != null) {
            val status = Status()
            status.message = BluePrintConstants.STATUS_FAILURE
            status.errorMessage = exception.message
            status
        } else {
            val status = Status()
            status.message = BluePrintConstants.STATUS_SUCCESS
            status
        }
        return ExecutionServiceOutput().apply {
            commonHeader = executionServiceInput.commonHeader
            actionIdentifiers = executionServiceInput.actionIdentifiers
            status = wfStatus
        }
    }

    override suspend fun prepareNodeExecutionMessage(node: Graph.Node)
            : NodeExecuteMessage<ExecutionServiceInput, ExecutionServiceOutput> {
        val nodeOutput = ExecutionServiceOutput().apply {
            commonHeader = executionServiceInput.commonHeader
            actionIdentifiers = executionServiceInput.actionIdentifiers
        }
        return NodeExecuteMessage(node, executionServiceInput, nodeOutput)
    }

    override suspend fun prepareNodeSkipMessage(node: Graph.Node)
            : NodeSkipMessage<ExecutionServiceInput, ExecutionServiceOutput> {
        val nodeOutput = ExecutionServiceOutput().apply {
            commonHeader = executionServiceInput.commonHeader
            actionIdentifiers = executionServiceInput.actionIdentifiers
        }
        return NodeSkipMessage(node, executionServiceInput, nodeOutput)
    }

    override suspend fun executeNode(node: Graph.Node, nodeInput: ExecutionServiceInput,
                                     nodeOutput: ExecutionServiceOutput): EdgeLabel {
        log.info("Executing workflow($workflowName[${this.workflowId}])'s step($${node.id})")
        val step = bluePrintRuntimeService.bluePrintContext().workflowStepByName(this.workflowName, node.id)
        checkNotEmpty(step.target) { "couldn't get step target for workflow(${this.workflowName})'s step(${node.id})" }
        val nodeTemplateName = step.target!!
        /** execute node template */
        val executionServiceOutput = nodeTemplateExecutionService
                .executeNodeTemplate(bluePrintRuntimeService, nodeTemplateName, nodeInput)

        return when (executionServiceOutput.status.message) {
            BluePrintConstants.STATUS_FAILURE -> EdgeLabel.FAILURE
            else -> EdgeLabel.SUCCESS
        }
    }

    override suspend fun skipNode(node: Graph.Node, nodeInput: ExecutionServiceInput,
                                  nodeOutput: ExecutionServiceOutput): EdgeLabel {
        return EdgeLabel.SUCCESS
    }

    override suspend fun cancelNode(node: Graph.Node, nodeInput: ExecutionServiceInput,
                                    nodeOutput: ExecutionServiceOutput): EdgeLabel {
        TODO("not implemented")
    }

    override suspend fun restartNode(node: Graph.Node, nodeInput: ExecutionServiceInput,
                                     nodeOutput: ExecutionServiceOutput): EdgeLabel {
        TODO("not implemented")
    }
}
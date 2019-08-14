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
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asGraph
import org.onap.ccsdk.cds.controllerblueprints.core.data.EdgeLabel
import org.onap.ccsdk.cds.controllerblueprints.core.data.Graph
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintWorkflowExecutionService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.*
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("imperativeWorkflowExecutionService")
class ImperativeWorkflowExecutionService(
        private val bluePrintWorkFlowService: BluePrintWorkFlowService<ExecutionServiceInput, ExecutionServiceOutput>)
    : BluePrintWorkflowExecutionService<ExecutionServiceInput, ExecutionServiceOutput> {

    override suspend fun executeBluePrintWorkflow(bluePrintRuntimeService: BluePrintRuntimeService<*>,
                                                  executionServiceInput: ExecutionServiceInput,
                                                  properties: MutableMap<String, Any>): ExecutionServiceOutput {

        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val workflowName = executionServiceInput.actionIdentifiers.actionName

        val graph = bluePrintContext.workflowByName(workflowName).asGraph()

        val deferredOutput = CompletableDeferred<ExecutionServiceOutput>()
        bluePrintWorkFlowService.executeWorkflow(graph, bluePrintRuntimeService, executionServiceInput, deferredOutput)
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
    lateinit var deferredExecutionServiceOutput: CompletableDeferred<ExecutionServiceOutput>

    override suspend fun executeWorkflow(graph: Graph, bluePrintRuntimeService: BluePrintRuntimeService<*>,
                                         input: ExecutionServiceInput,
                                         output: CompletableDeferred<ExecutionServiceOutput>) {
        this.graph = graph
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.executionServiceInput = input
        this.deferredExecutionServiceOutput = output
        this.workflowId = bluePrintRuntimeService.id()
        val startMessage = WorkflowExecuteMessage(input, output)
        workflowActor.send(startMessage)
    }

    override suspend fun initializeWorkflow(input: ExecutionServiceInput): EdgeLabel {
        return EdgeLabel.SUCCESS
    }

    override suspend fun prepareWorkflowOutput(): ExecutionServiceOutput {
        return ExecutionServiceOutput().apply {
            commonHeader = executionServiceInput.commonHeader
            actionIdentifiers = executionServiceInput.actionIdentifiers
        }
    }

    override suspend fun prepareNodeExecutionMessage(node: Graph.Node)
            : NodeExecuteMessage<ExecutionServiceInput, ExecutionServiceOutput> {
        val deferredOutput = CompletableDeferred<ExecutionServiceOutput>()
        return NodeExecuteMessage(node, executionServiceInput, deferredOutput)
    }

    override suspend fun prepareNodeSkipMessage(node: Graph.Node)
            : NodeSkipMessage<ExecutionServiceInput, ExecutionServiceOutput> {
        val deferredOutput = CompletableDeferred<ExecutionServiceOutput>()
        return NodeSkipMessage(node, executionServiceInput, deferredOutput)
    }

    override suspend fun executeNode(node: Graph.Node, nodeInput: ExecutionServiceInput,
                                     deferredNodeOutput: CompletableDeferred<ExecutionServiceOutput>,
                                     deferredNodeStatus: CompletableDeferred<EdgeLabel>) {
        try {
            val nodeTemplateName = node.id
            /** execute node template */
            val executionServiceOutput = nodeTemplateExecutionService
                    .executeNodeTemplate(bluePrintRuntimeService, nodeTemplateName, nodeInput)
            val edgeStatus = when (executionServiceOutput.status.message) {
                BluePrintConstants.STATUS_FAILURE -> EdgeLabel.FAILURE
                else -> EdgeLabel.SUCCESS
            }
            /** set deferred output and status */
            deferredNodeOutput.complete(executionServiceOutput)
            deferredNodeStatus.complete(edgeStatus)
        } catch (e: Exception) {
            log.error("failed in executeNode($node)", e)
            deferredNodeOutput.completeExceptionally(e)
            deferredNodeStatus.complete(EdgeLabel.FAILURE)
        }
    }

    override suspend fun skipNode(node: Graph.Node, nodeInput: ExecutionServiceInput,
                                  deferredNodeOutput: CompletableDeferred<ExecutionServiceOutput>,
                                  deferredNodeStatus: CompletableDeferred<EdgeLabel>) {
        val executionServiceOutput = ExecutionServiceOutput().apply {
            commonHeader = nodeInput.commonHeader
            actionIdentifiers = nodeInput.actionIdentifiers
        }
        deferredNodeOutput.complete(executionServiceOutput)
        deferredNodeStatus.complete(EdgeLabel.SUCCESS)
    }

    override suspend fun cancelNode(node: Graph.Node, nodeInput: ExecutionServiceInput,
                                    deferredNodeOutput: CompletableDeferred<ExecutionServiceOutput>,
                                    deferredNodeStatus: CompletableDeferred<EdgeLabel>) {
        TODO("not implemented")
    }

    override suspend fun restartNode(node: Graph.Node, nodeInput: ExecutionServiceInput,
                                     deferredNodeOutput: CompletableDeferred<ExecutionServiceOutput>,
                                     deferredNodeStatus: CompletableDeferred<EdgeLabel>) {
        TODO("not implemented")
    }
}
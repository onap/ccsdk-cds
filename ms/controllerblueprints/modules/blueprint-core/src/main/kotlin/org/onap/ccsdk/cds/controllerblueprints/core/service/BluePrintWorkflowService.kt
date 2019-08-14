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

package org.onap.ccsdk.cds.controllerblueprints.core.service

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.data.EdgeLabel
import org.onap.ccsdk.cds.controllerblueprints.core.data.EdgeStatus
import org.onap.ccsdk.cds.controllerblueprints.core.data.Graph
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeStatus
import kotlin.coroutines.CoroutineContext

interface BluePrintWorkFlowService<In, Out> {

    /** Executes imperative workflow graph [graph] for the bluePrintRuntimeService [bluePrintRuntimeService]
     * and workflow input [input], response will be retrieve from output [output]*/
    suspend fun executeWorkflow(graph: Graph, bluePrintRuntimeService: BluePrintRuntimeService<*>,
                                input: In, output: CompletableDeferred<Out>)

    suspend fun initializeWorkflow(input: In): EdgeLabel

    suspend fun prepareWorkflowOutput(): Out

    /** Prepare the message for the Node */
    suspend fun prepareNodeExecutionMessage(node: Graph.Node): NodeExecuteMessage<In, Out>

    suspend fun prepareNodeSkipMessage(node: Graph.Node): NodeSkipMessage<In, Out>

    suspend fun executeNode(node: Graph.Node, nodeInput: In, deferredNodeOutput: CompletableDeferred<Out>,
                            deferredNodeStatus: CompletableDeferred<EdgeLabel>)

    suspend fun skipNode(node: Graph.Node, nodeInput: In, deferredNodeOutput: CompletableDeferred<Out>,
                         deferredNodeStatus: CompletableDeferred<EdgeLabel>)

    suspend fun cancelNode(node: Graph.Node, nodeInput: In, deferredNodeOutput: CompletableDeferred<Out>,
                           deferredNodeStatus: CompletableDeferred<EdgeLabel>)

    suspend fun restartNode(node: Graph.Node, nodeInput: In, deferredNodeOutput: CompletableDeferred<Out>,
                            deferredNodeStatus: CompletableDeferred<EdgeLabel>)

}

/** Workflow Message Types */
sealed class WorkflowMessage<In, Out>

class WorkflowExecuteMessage<In, Out>(val input: In, val output: CompletableDeferred<Out>) : WorkflowMessage<In, Out>()

class WorkflowCancelMessage<In, Out>(val input: In, val output: CompletableDeferred<Out>) : WorkflowMessage<In, Out>()

class WorkflowRestartMessage<In, Out>(val input: In, val output: CompletableDeferred<Out>) : WorkflowMessage<In, Out>()

/** Node Message Types */
sealed class NodeMessage<In, Out>

class NodeReadyMessage<In, Out>(val fromEdge: Graph.Edge, val edgeAction: EdgeAction) : NodeMessage<In, Out>()

class NodeExecuteMessage<In, Out>(val node: Graph.Node, val nodeInput: In,
                                  val nodeOutput: CompletableDeferred<Out>) : NodeMessage<In, Out>()

class NodeRestartMessage<In, Out>(val node: Graph.Node, val nodeInput: In,
                                  val nodeOutput: CompletableDeferred<Out>) : NodeMessage<In, Out>()

class NodeSkipMessage<In, Out>(val node: Graph.Node, val nodeInput: In,
                               val nodeOutput: CompletableDeferred<Out>) : NodeMessage<In, Out>()

class NodeCancelMessage<In, Out>(val node: Graph.Node, val nodeInput: In,
                                 val nodeOutput: CompletableDeferred<Out>) : NodeMessage<In, Out>()

enum class EdgeAction(val id: String) {
    EXECUTE("execute"),
    SKIP("skip")
}

/** Abstract workflow service implementation */
abstract class AbstractBluePrintWorkFlowService<In, Out> : CoroutineScope, BluePrintWorkFlowService<In, Out> {

    lateinit var graph: Graph

    private val log = logger(AbstractBluePrintWorkFlowService::class)

    private val job = Job()

    lateinit var workflowId: String

    final override val coroutineContext: CoroutineContext
        get() = job + CoroutineName("Wf")

    fun cancel() {
        log.info("Received workflow($workflowId) cancel request")
        job.cancel()
        throw CancellationException("Workflow($workflowId) cancelled as requested ...")
    }

    val workflowActor = actor<WorkflowMessage<In, Out>>(coroutineContext, Channel.UNLIMITED) {

        /** Send message from workflow actor to node actor */
        fun sendNodeMessage(nodeMessage: NodeMessage<In, Out>) = launch {
            nodeActor.send(nodeMessage)
        }

        /** Process the workflow execution message */
        suspend fun executeMessageActor(workflowExecuteMessage: WorkflowExecuteMessage<In, Out>) {
            // Prepare Workflow and Populate the Initial store
            initializeWorkflow(workflowExecuteMessage.input)

            val startNode = graph.startNodes().first()
            // Prepare first node message and Send NodeExecuteMessage
            // Start node doesn't wait for any nodes, so we can pass Execute message directly
            val nodeExecuteMessage = prepareNodeExecutionMessage(startNode)
            sendNodeMessage(nodeExecuteMessage)
            log.debug("First node triggered successfully, waiting for response")

            // Wait for workflow completion or Error
            nodeActor.invokeOnClose { exception ->
                launch {
                    log.debug("End Node Completed, processing completion message")
                    val workflowOutput = prepareWorkflowOutput()
                    workflowExecuteMessage.output.complete(workflowOutput)
                    channel.close(exception)
                }
            }
        }

        /** Process each actor message received based on type */
        consumeEach { message ->
            when (message) {
                is WorkflowExecuteMessage<In, Out> -> {
                    launch {
                        executeMessageActor(message)
                    }
                }
                is WorkflowRestartMessage<In, Out> -> {
                    launch {
                        TODO("")
                    }
                }
                is WorkflowCancelMessage<In, Out> -> {
                    launch {
                        TODO("")
                    }
                }
            }
        }
    }


    private val nodeActor = actor<NodeMessage<In, Out>>(coroutineContext, Channel.UNLIMITED) {

        /** Send message to process from one state to other state */
        fun sendNodeMessage(nodeMessage: NodeMessage<In, Out>) = launch {
            channel.send(nodeMessage)
        }

        /** Process the cascade node processing, based on the previous state of the node */
        fun processNextNodes(node: Graph.Node, nodeState: EdgeLabel) {
            // Process only Next Success Node
            val stateEdges = graph.outgoingEdges(node.id, arrayListOf(nodeState))
            log.debug("Next Edges :$stateEdges")
            if (stateEdges.isNotEmpty()) {
                stateEdges.forEach { stateEdge ->
                    // Prepare next node ready message and Send NodeReadyMessage
                    val nodeReadyMessage = NodeReadyMessage<In, Out>(stateEdge, EdgeAction.EXECUTE)
                    sendNodeMessage(nodeReadyMessage)
                }
            }
        }

        suspend fun triggerToExecuteOrSkip(message: NodeReadyMessage<In, Out>) {
            val edge = message.fromEdge
            val node = edge.target
            // Check if current edge action is Skip or Execute
            when (message.edgeAction) {
                EdgeAction.SKIP -> {
                    val skipMessage = prepareNodeSkipMessage(node)
                    sendNodeMessage(skipMessage)
                }
                EdgeAction.EXECUTE -> {
                    val nodeExecuteMessage = prepareNodeExecutionMessage(node)
                    sendNodeMessage(nodeExecuteMessage)
                }
            }
        }

        suspend fun readyNodeWorker(message: NodeReadyMessage<In, Out>) {
            val edge = message.fromEdge
            val node = edge.target
            log.debug("@@@@@ Ready workflow($workflowId), node($node) from edge($edge) for action(${message.edgeAction}) @@@@@")
            // Update the current incoming edge status to executed or skipped
            when (message.edgeAction) {
                EdgeAction.SKIP -> message.fromEdge.status = EdgeStatus.SKIPPED
                EdgeAction.EXECUTE -> message.fromEdge.status = EdgeStatus.EXECUTED
            }
            val incomingEdges = graph.incomingEdges(node.id)
            if (incomingEdges.size > 1) {
                // Check all incoming edges executed or skipped
                val notCompletedEdges = incomingEdges.filter { it.status == EdgeStatus.NOT_STARTED }
                if (notCompletedEdges.isEmpty()) {
                    // Possibility of skip edge action performed at last, but other edges have execute action.
                    val executePresent = incomingEdges.filter { it.status == EdgeStatus.EXECUTED }
                    val newMessage = if (executePresent.isNotEmpty()) {
                        NodeReadyMessage(message.fromEdge, EdgeAction.EXECUTE)
                    } else {
                        message
                    }
                    triggerToExecuteOrSkip(newMessage)
                } else {
                    log.info("node(${node.id}) waiting for not completed edges($notCompletedEdges)")
                }
            } else {
                triggerToExecuteOrSkip(message)
            }
        }

        fun executeNodeWorker(message: NodeExecuteMessage<In, Out>) = launch {
            val node = message.node
            node.status = NodeStatus.EXECUTING
            val nodeState = if (node.id == BluePrintConstants.GRAPH_START_NODE_NAME
                    || node.id == BluePrintConstants.GRAPH_END_NODE_NAME) {
                EdgeLabel.SUCCESS
            } else {
                log.debug("##### Processing workflow($workflowId) node($node) #####")
                // Call the Extension function and get the next Edge state.
                val deferredNodeState = CompletableDeferred<EdgeLabel>()
                executeNode(node, message.nodeInput, message.nodeOutput, deferredNodeState)
                deferredNodeState.await()
            }
            // Update Node Completed
            node.status = NodeStatus.EXECUTED
            log.info("Execute Node($node) -> Executed State($nodeState)")

            // If End Node, Send End Message
            if (graph.isEndNode(node)) {
                // Close the current channel
                channel.close()
            } else {
                val skippingEdges = graph.outgoingEdgesNotInLabels(node.id, arrayListOf(nodeState))
                log.debug("Skipping node($node) outgoing Edges($skippingEdges)")
                // Process Skip Edges
                skippingEdges.forEach { skippingEdge ->
                    // Prepare next node ready message and Send NodeReadyMessage
                    val nodeReadyMessage = NodeReadyMessage<In, Out>(skippingEdge, EdgeAction.SKIP)
                    sendNodeMessage(nodeReadyMessage)
                }
                // Process Success Node
                processNextNodes(node, nodeState)
            }
        }

        fun skipNodeWorker(message: NodeSkipMessage<In, Out>) = launch {
            val node = message.node
            val incomingEdges = graph.incomingEdges(node.id)
            // Check All Incoming Nodes Skipped
            val nonSkippedEdges = incomingEdges.filter {
                it.status == EdgeStatus.NOT_STARTED
            }
            log.debug("Node($node) incoming edges ($incomingEdges), not skipped incoming edges ($nonSkippedEdges)")

            if (nonSkippedEdges.isEmpty()) {
                log.debug("$$$$$ Skipping workflow($workflowId) node($node) $$$$$")
                // Call the Extension Function
                val deferredNodeState = CompletableDeferred<EdgeLabel>()
                skipNode(node, message.nodeInput, message.nodeOutput, deferredNodeState)
                val nodeState = deferredNodeState.await()
                log.info("Skip Node($node) -> Executed State($nodeState)")
                // Mark the Current node as Skipped
                node.status = NodeStatus.SKIPPED
                // Look for next possible skip nodes
                graph.outgoingEdges(node.id).forEach { outgoingEdge ->
                    val nodeReadyMessage = NodeReadyMessage<In, Out>(outgoingEdge, EdgeAction.SKIP)
                    sendNodeMessage(nodeReadyMessage)
                }
            }
        }

        fun restartNodeWorker(message: NodeRestartMessage<In, Out>) = launch {
            TODO()
        }

        fun cancelNodeWorker(messageWorkflow: WorkflowCancelMessage<In, Out>) = launch {
            channel.close()
            throw CancellationException("Workflow($workflowId) actor cancelled as requested ...")
        }

        /** Process each actor message received based on type **/
        consumeEach { nodeMessage ->
            when (nodeMessage) {
                is NodeReadyMessage<In, Out> -> {
                    // Blocking call
                    readyNodeWorker(nodeMessage)
                }
                is NodeExecuteMessage<In, Out> -> {
                    launch {
                        executeNodeWorker(nodeMessage)
                    }
                }
                is NodeSkipMessage<In, Out> -> {
                    launch {
                        skipNodeWorker(nodeMessage)
                    }
                }
                is NodeRestartMessage<In, Out> -> {
                    launch {
                        restartNodeWorker(nodeMessage)
                    }
                }
            }
        }
    }

    override suspend fun executeWorkflow(graph: Graph, bluePrintRuntimeService: BluePrintRuntimeService<*>,
                                         input: In, output: CompletableDeferred<Out>) {
        log.info("Executing Graph : $graph")
        this.graph = graph
        this.workflowId = bluePrintRuntimeService.id()
        val startMessage = WorkflowExecuteMessage(input, output)
        workflowActor.send(startMessage)
    }
}
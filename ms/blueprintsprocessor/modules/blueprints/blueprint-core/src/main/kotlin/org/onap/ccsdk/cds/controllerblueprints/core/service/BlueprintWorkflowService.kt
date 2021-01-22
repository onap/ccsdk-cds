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

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.data.EdgeLabel
import org.onap.ccsdk.cds.controllerblueprints.core.data.EdgeStatus
import org.onap.ccsdk.cds.controllerblueprints.core.data.Graph
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeStatus
import org.onap.ccsdk.cds.controllerblueprints.core.incomingEdges
import org.onap.ccsdk.cds.controllerblueprints.core.isEndNode
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.outgoingEdges
import org.onap.ccsdk.cds.controllerblueprints.core.outgoingEdgesNotInLabels
import org.onap.ccsdk.cds.controllerblueprints.core.startNodes
import kotlin.coroutines.CoroutineContext

interface BlueprintWorkFlowService<In, Out> {

    /** Executes imperative workflow graph [graph] for the bluePrintRuntimeService [bluePrintRuntimeService]
     * and workflow input [input]*/
    suspend fun executeWorkflow(graph: Graph, bluePrintRuntimeService: BlueprintRuntimeService<*>, input: In): Out

    suspend fun initializeWorkflow(input: In): EdgeLabel

    suspend fun prepareWorkflowOutput(): Out

    /** Prepare the message for the Node */
    suspend fun prepareNodeExecutionMessage(node: Graph.Node): NodeExecuteMessage<In, Out>

    suspend fun prepareNodeSkipMessage(node: Graph.Node): NodeSkipMessage<In, Out>

    suspend fun executeNode(node: Graph.Node, nodeInput: In, nodeOutput: Out): EdgeLabel

    suspend fun skipNode(node: Graph.Node, nodeInput: In, nodeOutput: Out): EdgeLabel

    suspend fun cancelNode(node: Graph.Node, nodeInput: In, nodeOutput: Out): EdgeLabel

    suspend fun restartNode(node: Graph.Node, nodeInput: In, nodeOutput: Out): EdgeLabel
}

/** Workflow Message Types */
sealed class WorkflowMessage<In, Out>

class WorkflowExecuteMessage<In, Out>(val input: In, val output: CompletableDeferred<Out>) : WorkflowMessage<In, Out>()

class WorkflowCancelMessage<In, Out>(val input: In, val output: CompletableDeferred<Out>) : WorkflowMessage<In, Out>()

class WorkflowRestartMessage<In, Out>(val input: In, val output: CompletableDeferred<Out>) : WorkflowMessage<In, Out>()

/** Node Message Types */
sealed class NodeMessage<In, Out>

class NodeReadyMessage<In, Out>(val fromEdge: Graph.Edge, val edgeAction: EdgeAction) : NodeMessage<In, Out>()

class NodeExecuteMessage<In, Out>(val node: Graph.Node, val nodeInput: In, val nodeOutput: Out) : NodeMessage<In, Out>()

class NodeRestartMessage<In, Out>(val node: Graph.Node, val nodeInput: In, val nodeOutput: Out) : NodeMessage<In, Out>()

class NodeSkipMessage<In, Out>(val node: Graph.Node, val nodeInput: In, val nodeOutput: Out) : NodeMessage<In, Out>()

class NodeCancelMessage<In, Out>(val node: Graph.Node, val nodeInput: In, val nodeOutput: Out) : NodeMessage<In, Out>()

enum class EdgeAction(val id: String) {
    EXECUTE("execute"),
    SKIP("skip")
}

/** Abstract workflow service implementation */
abstract class AbstractBlueprintWorkFlowService<In, Out> : CoroutineScope, BlueprintWorkFlowService<In, Out> {

    lateinit var graph: Graph

    private val log = logger(AbstractBlueprintWorkFlowService::class)

    private val job = Job()

    lateinit var workflowId: String

    var exceptions: MutableList<Exception> = arrayListOf()

    override val coroutineContext: CoroutineContext
        get() = job + CoroutineName("Wf")

    fun cancel() {
        log.info("Received workflow($workflowId) cancel request")
        job.cancel()
        throw CancellationException("Workflow($workflowId) cancelled as requested")
    }

    suspend fun workflowActor() = actor<WorkflowMessage<In, Out>>(coroutineContext, Channel.UNLIMITED) {
        /** Process the workflow execution message */
        suspend fun executeMessageActor(workflowExecuteMessage: WorkflowExecuteMessage<In, Out>) {

            val nodeActor = nodeActor()
            // Prepare Workflow and Populate the Initial store
            initializeWorkflow(workflowExecuteMessage.input)

            val startNode = graph.startNodes().first()
            // Prepare first node message and Send NodeExecuteMessage
            // Start node doesn't wait for any nodes, so we can pass Execute message directly
            val nodeExecuteMessage = prepareNodeExecutionMessage(startNode)
            /** Send message from workflow actor to node actor */
            launch {
                nodeActor.send(nodeExecuteMessage)
            }
            // Wait for workflow completion or Error
            nodeActor.invokeOnClose { exception ->
                launch {
                    if (exception != null) exceptions.add(BlueprintProcessorException(exception))
                    log.info("workflow($workflowId) nodes completed with (${exceptions.size})exceptions")
                    val workflowOutput = prepareWorkflowOutput()
                    workflowExecuteMessage.output.complete(workflowOutput)
                    channel.close()
                }
            }
        }

        /** Process each actor message received based on type */
        consumeEach { message ->
            when (message) {
                is WorkflowExecuteMessage<In, Out> -> {
                    launch {
                        try {
                            executeMessageActor(message)
                        } catch (e: Exception) {
                            exceptions.add(e)
                        }
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

    private suspend fun nodeActor() = actor<NodeMessage<In, Out>>(coroutineContext, Channel.UNLIMITED) {

        /** Send message to process from one state to other state */
        fun sendNodeMessage(nodeMessage: NodeMessage<In, Out>) = launch {
            channel.send(nodeMessage)
        }

        /** Process the cascade node processing, based on the previous state of the node */
        fun processNextNodes(node: Graph.Node, nodeState: EdgeLabel) {
            // Process only Next Success Node
            val stateEdges = graph.outgoingEdges(node.id, arrayListOf(nodeState))
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
                    log.info("node(${node.id}) is waiting for incoming edges($notCompletedEdges)")
                }
            } else {
                triggerToExecuteOrSkip(message)
            }
        }

        suspend fun executeNodeWorker(message: NodeExecuteMessage<In, Out>) {
            val node = message.node
            node.status = NodeStatus.EXECUTING
            val nodeState = if (node.id == BlueprintConstants.GRAPH_START_NODE_NAME ||
                node.id == BlueprintConstants.GRAPH_END_NODE_NAME
            ) {
                EdgeLabel.SUCCESS
            } else {
                log.debug("##### Processing workflow($workflowId) node($node) #####")
                // Call the Extension function and get the next Edge state.
                executeNode(node, message.nodeInput, message.nodeOutput)
            }
            // Update Node Completed
            node.status = NodeStatus.EXECUTED
            log.info("Execute node(${node.id}) -> executed state($nodeState)")
            // Check if the Node status edge is there, If not close processing
            val edgePresent = graph.outgoingEdges(node.id, nodeState).isNotEmpty()

            // If End Node, Send End Message
            if (graph.isEndNode(node)) {
                // Close the current channel
                channel.close()
            } else if (!edgePresent) {
                throw BlueprintProcessorException("node(${node.id}) outgoing edge($nodeState) is missing.")
            } else {
                val skippingEdges = graph.outgoingEdgesNotInLabels(node.id, arrayListOf(nodeState))
                log.debug("Skipping node($node)'s outgoing edges($skippingEdges)")
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

        suspend fun skipNodeWorker(message: NodeSkipMessage<In, Out>) {
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
                val nodeState = skipNode(node, message.nodeInput, message.nodeOutput)
                log.info("Skip node(${node.id}) -> executed state($nodeState)")
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
            throw CancellationException("Workflow($workflowId) actor cancelled as requested.")
        }

        /** Process each actor message received based on type **/
        consumeEach { nodeMessage ->
            when (nodeMessage) {
                is NodeReadyMessage<In, Out> -> {
                    // Blocking call
                    try {
                        readyNodeWorker(nodeMessage)
                    } catch (e: Exception) {
                        exceptions.add(e)
                        channel.close()
                    }
                }
                is NodeExecuteMessage<In, Out> -> {
                    launch {
                        try {
                            executeNodeWorker(nodeMessage)
                        } catch (e: Exception) {
                            nodeMessage.node.status = NodeStatus.TERMINATED
                            exceptions.add(e)
                            channel.close()
                        }
                    }
                }
                is NodeSkipMessage<In, Out> -> {
                    launch {
                        try {
                            skipNodeWorker(nodeMessage)
                        } catch (e: Exception) {
                            nodeMessage.node.status = NodeStatus.TERMINATED
                            exceptions.add(e)
                            channel.close()
                        }
                    }
                }
                is NodeRestartMessage<In, Out> -> {
                    launch {
                        try {
                            restartNodeWorker(nodeMessage)
                        } catch (e: Exception) {
                            exceptions.add(e)
                            channel.close()
                        }
                    }
                }
            }
        }
    }
}

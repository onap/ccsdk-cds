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

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.data.EdgeLabel
import org.onap.ccsdk.cds.controllerblueprints.core.data.Graph
import org.onap.ccsdk.cds.controllerblueprints.core.toGraph
import kotlin.test.assertNotNull

class BluePrintWorkflowServiceTest {
    @Test
    fun testSimpleFlow() {
        runBlocking {
            val graph = "[START>A/SUCCESS, A>B/SUCCESS, B>C/SUCCESS, C>D/SUCCESS, D>E/SUCCESS, E>END/SUCCESS]"
                    .toGraph()
            val simpleWorkflow = TestBluePrintWorkFlowService()
            simpleWorkflow.simulatedState = prepareSimulation(arrayListOf("A", "B", "C", "D", "E"), null)
            val deferredOutput = CompletableDeferred<String>()
            val input = "123456"
            simpleWorkflow.executeWorkflow(graph, mockBluePrintRuntimeService(), input, deferredOutput)
            val response = deferredOutput.await()
            assertNotNull(response, "failed to get response")
        }
    }

    @Test
    fun testConditionalFlow() {
        runBlocking {
            val graph = "[START>A/SUCCESS, A>B/SUCCESS, A>C/FAILURE, B>D/SUCCESS, C>D/SUCCESS, D>END/SUCCESS]"
                    .toGraph()
            val simpleWorkflow = TestBluePrintWorkFlowService()
            simpleWorkflow.simulatedState = prepareSimulation(arrayListOf("A", "B", "C", "D", "E"), null)
            val deferredOutput = CompletableDeferred<String>()
            val input = "123456"
            simpleWorkflow.executeWorkflow(graph, mockBluePrintRuntimeService(), input, deferredOutput)
            val response = deferredOutput.await()
            assertNotNull(response, "failed to get response")
        }
    }

    @Test
    fun testBothConditionalFlow() {
        runBlocking {
            // Failure Flow
            val failurePatGraph = "[START>A/SUCCESS, A>B/SUCCESS, A>C/FAILURE, B>D/SUCCESS, C>D/SUCCESS, D>END/SUCCESS]"
                    .toGraph()
            val failurePathWorkflow = TestBluePrintWorkFlowService()
            failurePathWorkflow.simulatedState = prepareSimulation(arrayListOf("B", "C", "D", "E"),
                    arrayListOf("A"))
            val failurePathWorkflowDeferredOutput = CompletableDeferred<String>()
            val failurePathWorkflowInput = "123456"
            failurePathWorkflow.executeWorkflow(failurePatGraph, mockBluePrintRuntimeService(), failurePathWorkflowInput, failurePathWorkflowDeferredOutput)
            val failurePathResponse = failurePathWorkflowDeferredOutput.await()
            assertNotNull(failurePathResponse, "failed to get response")
        }
    }

    @Test
    fun testMultipleSkipFlow() {
        runBlocking {
            val graph = "[START>A/SUCCESS, A>B/SUCCESS, A>C/FAILURE, C>D/SUCCESS, D>E/SUCCESS, B>E/SUCCESS, E>END/SUCCESS]"
                    .toGraph()
            val simpleWorkflow = TestBluePrintWorkFlowService()
            simpleWorkflow.simulatedState = prepareSimulation(arrayListOf("A", "B", "C", "D", "E"), null)
            val deferredOutput = CompletableDeferred<String>()
            val input = "123456"
            simpleWorkflow.executeWorkflow(graph, mockBluePrintRuntimeService(), input, deferredOutput)
            val response = deferredOutput.await()
            assertNotNull(response, "failed to get response")
        }
    }

    @Test
    fun testParallelFlow() {
        runBlocking {
            val graph = "[START>A/SUCCESS, A>B/SUCCESS, A>C/SUCCESS, B>D/SUCCESS, C>D/SUCCESS, D>END/SUCCESS]"
                    .toGraph()
            val simpleWorkflow = TestBluePrintWorkFlowService()
            simpleWorkflow.simulatedState = prepareSimulation(arrayListOf("A", "B", "C", "D"), null)
            val deferredOutput = CompletableDeferred<String>()
            val input = "123456"
            simpleWorkflow.executeWorkflow(graph, mockBluePrintRuntimeService(), input, deferredOutput)
            val response = deferredOutput.await()
            assertNotNull(response, "failed to get response")
        }
    }

    private fun mockBluePrintRuntimeService(): BluePrintRuntimeService<*> {
        val bluePrintRuntimeService = mockk<BluePrintRuntimeService<*>>()
        every { bluePrintRuntimeService.id() } returns "123456"
        return bluePrintRuntimeService
    }

    private fun prepareSimulation(successes: List<String>?, failures: List<String>?): MutableMap<String, EdgeLabel> {
        val simulatedState: MutableMap<String, EdgeLabel> = hashMapOf()
        successes?.forEach {
            simulatedState[it] = EdgeLabel.SUCCESS
        }
        failures?.forEach {
            simulatedState[it] = EdgeLabel.FAILURE
        }
        return simulatedState
    }
}

class TestBluePrintWorkFlowService
    : AbstractBluePrintWorkFlowService<String, String>() {

    lateinit var simulatedState: MutableMap<String, EdgeLabel>

    override suspend fun initializeWorkflow(input: String): EdgeLabel {
        return EdgeLabel.SUCCESS
    }

    override suspend fun prepareNodeExecutionMessage(node: Graph.Node)
            : NodeExecuteMessage<String, String> {
        return NodeExecuteMessage(node, "$node Input", "")
    }

    override suspend fun executeNode(node: Graph.Node, nodeInput: String,
                                     nodeOutput: String): EdgeLabel {
//        val random = (1..10).random() * 1000
//        println("will reply in $random ms")
//        kotlinx.coroutines.delay(random.toLong())
        val status = simulatedState[node.id] ?: throw BluePrintException("failed to get status for the node($node)")
        return status
    }

    override suspend fun prepareNodeSkipMessage(node: Graph.Node): NodeSkipMessage<String, String> {
        val nodeOutput = ""
        val nodeSkipMessage = NodeSkipMessage(node, "$node Skip Input", nodeOutput)
        return nodeSkipMessage
    }

    override suspend fun skipNode(node: Graph.Node, nodeInput: String,
                                  nodeOutput: String): EdgeLabel {
        val status = simulatedState[node.id] ?: throw BluePrintException("failed to get status for the node($node)")
        return status
    }

    override suspend fun cancelNode(node: Graph.Node, nodeInput: String,
                                    nodeOutput: String): EdgeLabel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun restartNode(node: Graph.Node, nodeInput: String,
                                     nodeOutput: String): EdgeLabel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun prepareWorkflowOutput(exception: BluePrintProcessorException?): String {
        return "Final Response"
    }
}
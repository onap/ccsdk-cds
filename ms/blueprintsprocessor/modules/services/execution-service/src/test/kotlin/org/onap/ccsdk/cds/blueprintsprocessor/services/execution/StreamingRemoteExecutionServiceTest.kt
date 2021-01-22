/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

import com.google.protobuf.util.JsonFormat
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ACTION_MODE_SYNC
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.GRPCLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TokenAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.BlueprintGrpcLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts.MockBlueprintProcessingServer
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StreamingRemoteExecutionServiceTest {

    val log = logger(StreamingRemoteExecutionServiceTest::class)

    @get:Rule
    val grpcCleanup = GrpcCleanupRule()
    private val serverName = InProcessServerBuilder.generateName()
    private val serverBuilder = InProcessServerBuilder.forName(serverName).directExecutor()
    private val channelBuilder = InProcessChannelBuilder.forName(serverName).directExecutor()

    private val tokenAuthGrpcClientProperties = TokenAuthGrpcClientProperties().apply {
        host = "127.0.0.1"
        port = 50052
        type = GRPCLibConstants.TYPE_TOKEN_AUTH
        token = "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw=="
    }

    @Test
    @ExperimentalCoroutinesApi
    @FlowPreview
    fun testStreamingChannel() {
        grpcCleanup.register(serverBuilder.addService(MockBlueprintProcessingServer()).build().start())
        val channel = grpcCleanup.register(channelBuilder.maxInboundMessageSize(1024).build())

        runBlocking {
            val bluePrintGrpcLibPropertyService = BlueprintGrpcLibPropertyService(mockk())

            val streamingRemoteExecutionService = StreamingRemoteExecutionServiceImpl(bluePrintGrpcLibPropertyService)

            val spyStreamingRemoteExecutionService = spyk(streamingRemoteExecutionService)
            /** To test with real server, comment below line */
            coEvery() { spyStreamingRemoteExecutionService.createGrpcChannel(any()) } returns channel

            /** Test Send and Receive non interactive transaction */
            val nonInteractiveDeferred = arrayListOf<Deferred<*>>()
            repeat(2) { count ->
                val requestId = "1234-$count"
                val request = getRequest(requestId)
                val invocationId = request.commonHeader.subRequestId
                val deferred = async {
                    val response = spyStreamingRemoteExecutionService.sendNonInteractive(
                        tokenAuthGrpcClientProperties,
                        invocationId, request, 1000L
                    )
                    assertNotNull(response, "failed to get non interactive response")
                    assertEquals(
                        response.commonHeader.requestId, requestId,
                        "failed to match non interactive response id"
                    )
                    assertEquals(
                        response.status.eventType, EventType.EVENT_COMPONENT_EXECUTED,
                        "failed to match non interactive response type"
                    )
                }
                nonInteractiveDeferred.add(deferred)
            }
            nonInteractiveDeferred.awaitAll()

            /** Test Send and Receive interactive transaction */
            val responseFlowsDeferred = arrayListOf<Deferred<*>>()
            repeat(2) { count ->
                val requestId = "12345-$count"
                val request = getRequest(requestId)
                val invocationId = request.commonHeader.requestId
                val responseFlow = spyStreamingRemoteExecutionService
                    .openSubscription(tokenAuthGrpcClientProperties, invocationId)

                val deferred = async {
                    responseFlow.collect {
                        log.info("Received $count-response ($invocationId) : ${it.status.eventType}")
                        if (it.status.eventType == EventType.EVENT_COMPONENT_EXECUTED) {
                            spyStreamingRemoteExecutionService.cancelSubscription(invocationId)
                        }
                    }
                }
                responseFlowsDeferred.add(deferred)
                /** Sending Multiple messages with same requestId  and different subRequestId */
                spyStreamingRemoteExecutionService.send(invocationId, request)
            }
            responseFlowsDeferred.awaitAll()
            streamingRemoteExecutionService.closeChannel(tokenAuthGrpcClientProperties)
        }
    }

    private fun getRequest(requestId: String): ExecutionServiceInput {
        val commonHeader = CommonHeader.newBuilder()
            .setTimestamp("2012-04-23T18:25:43.511Z")
            .setOriginatorId("System")
            .setRequestId(requestId)
            .setSubRequestId("$requestId-" + UUID.randomUUID().toString()).build()

        val actionIdentifier = ActionIdentifiers.newBuilder()
            .setActionName("SampleScript")
            .setBlueprintName("sample-cba")
            .setBlueprintVersion("1.0.0")
            .setMode(ACTION_MODE_SYNC)
            .build()

        val jsonContent = """{ "key1" : "value1" }"""
        val payloadBuilder = ExecutionServiceInput.newBuilder().payloadBuilder
        JsonFormat.parser().merge(jsonContent, payloadBuilder)

        return ExecutionServiceInput.newBuilder()
            .setCommonHeader(commonHeader)
            .setActionIdentifiers(actionIdentifier)
            .setPayload(payloadBuilder.build())
            .build()
    }
}

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

package org.onap.ccsdk.cds.blueprintsprocessor.grpc.service

import com.github.marcoferrer.krotoplus.coroutines.client.clientCallBidiStreaming
import com.google.protobuf.util.JsonFormat
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ACTION_MODE_SYNC
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.GRPCLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TLSAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TLSAuthGrpcServerProperties
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertNotNull

class BluePrintGrpcServerTest {

    private val tlsAuthGrpcServerProperties = TLSAuthGrpcServerProperties().apply {
        port = 50052
        type = GRPCLibConstants.TYPE_TLS_AUTH
        certChain = "src/test/resources/tls-manual/py-executor-chain.pem"
        privateKey = "src/test/resources/tls-manual/py-executor-key.pem"
    }

    private val tlsAuthGrpcClientProperties = TLSAuthGrpcClientProperties().apply {
        host = "localhost"
        port = 50052
        type = GRPCLibConstants.TYPE_TLS_AUTH
        trustCertCollection = "src/test/resources/tls-manual/py-executor-chain.pem"
    }

    @Test
    fun testGrpcTLSContext() {
        val tlsAuthGrpcServerService = TLSAuthGrpcServerService(tlsAuthGrpcServerProperties)
        val sslContext = tlsAuthGrpcServerService.sslContext()
        assertNotNull(sslContext, "failed to create grpc server ssl context")

        val tlsAuthGrpcClientService = TLSAuthGrpcClientService(tlsAuthGrpcClientProperties)
        val clientSslContext = tlsAuthGrpcClientService.sslContext()
        assertNotNull(clientSslContext, "failed to create grpc client ssl context")
    }

    /** TLS Client Integration testing, GRPC TLS Junit testing is not supported. */
    // @Test
    fun testGrpcTLSServerIntegration() {
        runBlocking {
            val tlsAuthGrpcClientService = TLSAuthGrpcClientService(tlsAuthGrpcClientProperties)
            val grpcChannel = tlsAuthGrpcClientService.channel()
            /** Get Send and Receive Channel for bidirectional process method*/
            val (reqChannel, resChannel) = clientCallBidiStreaming(
                BluePrintProcessingServiceGrpc.getProcessMethod(),
                grpcChannel
            )
            launch {
                resChannel.consumeEach {
                    log.info("Received Response")
                    if (it.status.eventType == EventType.EVENT_COMPONENT_EXECUTED) {
                        resChannel.cancel()
                    }
                }
            }
            val request = getRequest("12345")
            reqChannel.send(request)
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

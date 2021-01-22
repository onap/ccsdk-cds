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

import io.grpc.stub.StreamObserver
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.GRPCLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TLSAuthGrpcServerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.interceptor.GrpcServerLoggingInterceptor
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BlueprintProcessingServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput

val log = logger(MockTLSBlueprintProcessingServer::class)

/** For Integration testing stat this server, Set the working path to run this method */
fun main() {
    try {
        val tlsAuthGrpcServerProperties = TLSAuthGrpcServerProperties().apply {
            port = 50052
            type = GRPCLibConstants.TYPE_TLS_AUTH
            certChain = "src/test/resources/tls-manual/py-executor-chain.pem"
            privateKey = "src/test/resources/tls-manual/py-executor-key.pem"
        }
        val server = TLSAuthGrpcServerService(tlsAuthGrpcServerProperties).serverBuilder()
            .intercept(GrpcServerLoggingInterceptor())
            .addService(MockTLSBlueprintProcessingServer())
            .build()
        server.start()
        log.info("GRPC Serve started(${server.isShutdown}) on port(${server.port})...")
        server.awaitTermination()
    } catch (e: Exception) {
        log.error("Failed to start tls grpc integration server", e)
    }
}

class MockTLSBlueprintProcessingServer : BlueprintProcessingServiceGrpc.BlueprintProcessingServiceImplBase() {

    override fun process(responseObserver: StreamObserver<ExecutionServiceOutput>): StreamObserver<ExecutionServiceInput> {

        return object : StreamObserver<ExecutionServiceInput> {
            override fun onNext(executionServiceInput: ExecutionServiceInput) {
                log.info(
                    "Received requestId(${executionServiceInput.commonHeader.requestId})  " +
                        "subRequestId(${executionServiceInput.commonHeader.subRequestId})"
                )
                responseObserver.onNext(buildResponse(executionServiceInput))
                responseObserver.onCompleted()
            }

            override fun onError(error: Throwable) {
                log.debug("Fail to process message", error)
                responseObserver.onError(
                    io.grpc.Status.INTERNAL
                        .withDescription(error.message)
                        .asException()
                )
            }

            override fun onCompleted() {
                log.info("Completed")
            }
        }
    }

    private fun buildResponse(input: ExecutionServiceInput): ExecutionServiceOutput {
        val status = Status.newBuilder().setCode(200)
            .setEventType(EventType.EVENT_COMPONENT_EXECUTED)
            .build()
        return ExecutionServiceOutput.newBuilder()
            .setCommonHeader(input.commonHeader)
            .setActionIdentifiers(input.actionIdentifiers)
            .setStatus(status)
            .build()
    }
}

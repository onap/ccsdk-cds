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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts

import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.interceptor.GrpcServerLoggingInterceptor
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status
import org.onap.ccsdk.cds.controllerblueprints.core.MDCContext
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BlueprintProcessingServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput

private val log = logger(MockBlueprintProcessingServer::class)

class MockBlueprintProcessingServer : BlueprintProcessingServiceGrpc.BlueprintProcessingServiceImplBase() {

    override fun process(responseObserver: StreamObserver<ExecutionServiceOutput>): StreamObserver<ExecutionServiceInput> {

        return object : StreamObserver<ExecutionServiceInput> {
            override fun onNext(executionServiceInput: ExecutionServiceInput) {
                log.info(
                    "Received requestId(${executionServiceInput.commonHeader.requestId})  " +
                        "subRequestId(${executionServiceInput.commonHeader.subRequestId})"
                )
                runBlocking {
                    launch(MDCContext()) {
                        responseObserver.onNext(buildNotification(executionServiceInput))
                        responseObserver.onNext(buildResponse(executionServiceInput))
                        log.info("message has sent successfully...")
                    }
                }
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

    private fun buildNotification(input: ExecutionServiceInput): ExecutionServiceOutput {
        val status = Status.newBuilder()
            .setEventType(EventType.EVENT_COMPONENT_NOTIFICATION)
            .build()
        return ExecutionServiceOutput.newBuilder()
            .setCommonHeader(input.commonHeader)
            .setActionIdentifiers(input.actionIdentifiers)
            .setStatus(status)
            .build()
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

/** For Integration testing stat this server */
fun main() {
    try {
        val server = ServerBuilder
            .forPort(50052)
            .intercept(GrpcServerLoggingInterceptor())
            .addService(MockBlueprintProcessingServer())
            .build()
        server.start()
        log.info("GRPC Serve started(${server.isShutdown}) on port(${server.port})...")
        server.awaitTermination()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

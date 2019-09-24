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
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.interceptor.GrpcServerLoggingInterceptor
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceCoroutineGrpc
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput

private val log = logger(MockBluePrintProcessingServer::class)


class MockBluePrintProcessingServer : BluePrintProcessingServiceCoroutineGrpc.BluePrintProcessingServiceImplBase() {

    override suspend fun process(requestChannel: ReceiveChannel<ExecutionServiceInput>,
                                 responseChannel: SendChannel<ExecutionServiceOutput>) {

        val requestIterator = requestChannel.iterator()

        while (requestIterator.hasNext()) {
            val request = requestIterator.next()
            log.info("Received requestId(${request.commonHeader.requestId})  " +
                    "subRequestId(${request.commonHeader.subRequestId})")
            responseChannel.send(buildNotification(request))
            responseChannel.send(buildResponse(request))
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
                .addService(MockBluePrintProcessingServer())
                .build()
        server.start()
        log.info("GRPC Serve started(${server.isShutdown}) on port(${server.port})...")
        server.awaitTermination()
    } catch (e: Exception) {
        e.printStackTrace()
    }

}
/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.utils.toJava
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
open class BluePrintProcessingGRPCHandler(private val bluePrintCoreConfiguration: BluePrintCoreConfiguration,
                                          private val executionServiceHandler: ExecutionServiceHandler)
    : BluePrintProcessingServiceGrpc.BluePrintProcessingServiceImplBase() {
    private val log = LoggerFactory.getLogger(BluePrintProcessingGRPCHandler::class.java)

    @PreAuthorize("hasRole('USER')")
    override fun process(
            responseObserver: StreamObserver<ExecutionServiceOutput>): StreamObserver<ExecutionServiceInput> {

        return object : StreamObserver<ExecutionServiceInput> {
            override fun onNext(executionServiceInput: ExecutionServiceInput) {
                try {
                    runBlocking {
                        executionServiceHandler.process(executionServiceInput.toJava(), responseObserver)
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }

            override fun onError(error: Throwable) {
                log.debug("Fail to process message", error)
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withDescription(error.message)
                        .asException())
            }

            override fun onCompleted() {
                log.info("Completed")
            }
        }
    }
}
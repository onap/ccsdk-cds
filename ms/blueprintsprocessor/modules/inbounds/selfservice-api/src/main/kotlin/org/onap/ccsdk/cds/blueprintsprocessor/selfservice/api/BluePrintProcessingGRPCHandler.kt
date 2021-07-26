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

import io.grpc.Status
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.core.utils.toJava
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.onap.ccsdk.cds.error.catalog.core.utils.errorCauseOrDefault
import org.onap.ccsdk.cds.error.catalog.core.utils.errorMessageOrDefault
import org.onap.ccsdk.cds.error.catalog.services.ErrorCatalogService
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.util.concurrent.Phaser
import javax.annotation.PreDestroy

@Service
open class BluePrintProcessingGRPCHandler(
    private val bluePrintCoreConfiguration: BluePrintCoreConfiguration,
    private val executionServiceHandler: ExecutionServiceHandler,
    private val errorCatalogService: ErrorCatalogService
) :
    BluePrintProcessingServiceGrpc.BluePrintProcessingServiceImplBase() {

    private val log = LoggerFactory.getLogger(BluePrintProcessingGRPCHandler::class.java)

    private val ph = Phaser(1)

    @PreAuthorize("hasRole('USER')")
    override fun process(
        responseObserver: StreamObserver<ExecutionServiceOutput>
    ): StreamObserver<ExecutionServiceInput> {

        return object : StreamObserver<ExecutionServiceInput> {
            override fun onNext(executionServiceInput: ExecutionServiceInput) {
                try {
                    ph.register()
                    runBlocking {
                        executionServiceHandler.process(executionServiceInput.toJava(), responseObserver)
                    }
                } catch (e: Exception) {
                    onError(e)
                } finally {
                    ph.arriveAndDeregister()
                }
            }

            override fun onError(error: Throwable) {
                log.debug("Fail to process message", error)
                if (error is BluePrintProcessorException) onErrorCatalog(error) else onError(error)
            }

            fun onError(error: Exception) {
                responseObserver.onError(
                    Status.INTERNAL
                        .withDescription(error.errorMessageOrDefault())
                        .withCause(error.errorCauseOrDefault())
                        .asException()
                )
            }

            fun onErrorCatalog(error: BluePrintProcessorException) {
                if (error.protocol == "") {
                    error.grpc(ErrorCatalogCodes.GENERIC_FAILURE)
                }
                val errorPayload = errorCatalogService.errorPayload(error)
                val grpcCode = Status.fromCodeValue(errorPayload.code)
                responseObserver.onError(
                    grpcCode
                        .withDescription(errorPayload.message)
                        .withCause(error.errorCauseOrDefault())
                        .asException()
                )
            }

            override fun onCompleted() {
                log.info("Completed")
            }
        }
    }

    @PreDestroy
    fun preDestroy() {
        val name = "BluePrintProcessingGRPCHandler"
        log.info("Starting to shutdown $name waiting for in-flight requests to finish ...")
        ph.arriveAndAwaitAdvance()
        log.info("Done waiting in $name")
    }
}

/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api

import io.grpc.stub.StreamObserver
import org.onap.ccsdk.apps.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.apps.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceInput
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceOutput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BluePrintProcessingGRPCHandler(private val bluePrintCoreConfiguration: BluePrintCoreConfiguration,
                                     private val executionServiceHandler: ExecutionServiceHandler)
    : BluePrintProcessingServiceGrpc.BluePrintProcessingServiceImplBase() {
    private val log = LoggerFactory.getLogger(BluePrintProcessingGRPCHandler::class.java)


    override fun process(responseObserver: StreamObserver<ExecutionServiceOutput>?): StreamObserver<ExecutionServiceInput> {

        return object : StreamObserver<ExecutionServiceInput> {

            override fun onNext(executionServiceInput: ExecutionServiceInput) {
                TODO("Handle Processing Response")
//                executionServiceHandler.process(executionServiceInput)
//                responseObserver.onNext(executionServiceOuput)
            }

            override fun onError(error: Throwable) {
                log.warn("Fail to process message", error)
            }

            override fun onCompleted() {
                responseObserver?.onCompleted()
            }
        }

    }


}
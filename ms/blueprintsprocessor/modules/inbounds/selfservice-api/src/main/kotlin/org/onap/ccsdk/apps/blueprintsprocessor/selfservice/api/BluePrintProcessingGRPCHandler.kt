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

import com.google.protobuf.util.JsonFormat
import io.grpc.stub.StreamObserver
import org.onap.ccsdk.apps.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.apps.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceInput
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceOutput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BluePrintProcessingGRPCHandler(private val bluePrintCoreConfiguration: BluePrintCoreConfiguration)
    : BluePrintProcessingServiceGrpc.BluePrintProcessingServiceImplBase() {
    private val log = LoggerFactory.getLogger(BluePrintProcessingGRPCHandler::class.java)

    override fun process(request: ExecutionServiceInput,
                         responseObserver: StreamObserver<ExecutionServiceOutput>) {

        val json = JsonFormat.printer().print(request)

        //log.info("Received GRPC request ${json}")
        //TODO( Handle Processing Response")
        val response = ExecutionServiceOutput.newBuilder().setCommonHeader(request.commonHeader).build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}
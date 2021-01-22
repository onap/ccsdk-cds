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

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import com.google.protobuf.util.JsonFormat
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.GRPCLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TokenAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.TokenAuthGrpcClientService
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BlueprintProcessingServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(
    classes = [SelfServiceApiTestConfiguration::class, ErrorCatalogTestConfiguration::class]
)
class BlueprintProcessingIntegrationTest {

    private val log = logger(BlueprintProcessingIntegrationTest::class)

    /** This is Integration test sample, Do not enable this test case in server build, this is for local desktop testing*/
    // @Test
    fun integrationTestGrpcManagement() {
        runBlocking {
            val tokenAuthGrpcClientProperties = TokenAuthGrpcClientProperties().apply {
                host = "127.0.0.1"
                port = 50052
                type = GRPCLibConstants.TYPE_TOKEN_AUTH
                token = "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw=="
            }
            val basicAuthGrpcClientService = TokenAuthGrpcClientService(tokenAuthGrpcClientProperties)
            val channel = basicAuthGrpcClientService.channel()

            val stub = BlueprintProcessingServiceGrpc.newStub(channel)
            repeat(1) {
                val requestObs = stub.process(object : StreamObserver<ExecutionServiceOutput> {
                    override fun onNext(executionServiceOuput: ExecutionServiceOutput) {
                        log.info("onNext Received {}", executionServiceOuput)
                    }

                    override fun onError(error: Throwable) {
                        log.error("Fail to process message", error)
                    }

                    override fun onCompleted() {
                        log.info("Done")
                    }
                })

                val commonHeader = CommonHeader.newBuilder()
                    .setTimestamp("2012-04-23T18:25:43.511Z")
                    .setOriginatorId("System")
                    .setRequestId("1234-$it")
                    .setSubRequestId("1234-56").build()

                val jsonContent = JacksonUtils.getClassPathFileContent("execution-input/sample-payload.json")
                val payloadBuilder = ExecutionServiceInput.newBuilder().payloadBuilder
                JsonFormat.parser().merge(jsonContent, payloadBuilder)

                val actionIdentifier = ActionIdentifiers.newBuilder()
                    .setActionName("SampleScript")
                    .setBlueprintName("sample-cba")
                    .setBlueprintVersion("1.0.0")
                    .build()

                val input = ExecutionServiceInput.newBuilder()
                    .setCommonHeader(commonHeader)
                    .setActionIdentifiers(actionIdentifier)
                    .setPayload(payloadBuilder.build())
                    .build()

                requestObs.onNext(input)
                requestObs.onCompleted()
            }
            delay(1000)
            channel.shutdownNow()
        }
    }
}

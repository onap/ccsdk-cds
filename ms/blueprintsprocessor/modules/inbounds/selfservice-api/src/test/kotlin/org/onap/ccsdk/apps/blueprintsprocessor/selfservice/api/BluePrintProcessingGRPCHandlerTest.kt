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
import io.grpc.testing.GrpcServerRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.apps.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.processing.api.CommonHeader
import org.onap.ccsdk.apps.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceInput
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [BluePrintProcessingGRPCHandler::class, BluePrintCoreConfiguration::class])
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BluePrintProcessingGRPCHandlerTest {

    private val log = LoggerFactory.getLogger(BluePrintProcessingGRPCHandlerTest::class.java)!!

    @get:Rule
    val grpcServerRule = GrpcServerRule().directExecutor()

    @Autowired
    lateinit var bluePrintProcessingGRPCHandler: BluePrintProcessingGRPCHandler

    @BeforeTest
    fun init() {
        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcServerRule.serviceRegistry.addService(bluePrintProcessingGRPCHandler)
    }

    @Test
    fun testSelfServiceGRPCHandler() {

        val blockingStub = BluePrintProcessingServiceGrpc.newBlockingStub(grpcServerRule.channel)

        val commonHeader = CommonHeader.newBuilder()
                .setTimestamp("2012-04-23T18:25:43.511Z")
                .setOriginatorId("System")
                .setRequestId("1234")
                .setSubRequestId("1234-56").build()

        val jsonContent = JacksonUtils.getClassPathFileContent("execution-input/sample-payload.json")
        val payloadBuilder = ExecutionServiceInput.newBuilder().payloadBuilder
        JsonFormat.parser().merge(jsonContent, payloadBuilder)

        val input = ExecutionServiceInput.newBuilder()
                .setCommonHeader(commonHeader)
                .setPayload(payloadBuilder.build())
                .build()

        val response = blockingStub.process(input)
        assertNotNull(response, "Response is null")
    }

}
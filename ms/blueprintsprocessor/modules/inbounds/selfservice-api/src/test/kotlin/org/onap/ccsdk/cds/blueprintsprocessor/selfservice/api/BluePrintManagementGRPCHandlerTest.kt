/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
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

import com.google.protobuf.ByteString
import io.grpc.testing.GrpcServerRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintManagementInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintManagementServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@DirtiesContext
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BluePrintManagementGRPCHandlerTest {

    @get:Rule
    val grpcServerRule = GrpcServerRule().directExecutor()

    @Autowired
    lateinit var bluePrintManagementGRPCHandler: BluePrintManagementGRPCHandler

    @BeforeTest
    fun init() {
        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcServerRule.serviceRegistry.addService(bluePrintManagementGRPCHandler)
    }

    @AfterTest
    fun cleanDir() {
        //TODO It's giving fluctuating results, need to look for another way to cleanup
        // works sometimes otherwise results IO Exception
        // Most probably bufferReader stream is not getting closed when cleanDir is getting invoked
        File("./target/blueprints").deleteRecursively()
    }

    @Test
    fun `test upload blueprint`() {
        val blockingStub = BluePrintManagementServiceGrpc.newBlockingStub(grpcServerRule.channel)
        val id = "123_upload"
        val req = createInputRequest(id)
        val output = blockingStub.uploadBlueprint(req)

        assertEquals(200, output.status.code)
        assertTrue(output.status.message.contains("Successfully uploaded blueprint sample:1.0.0 with id("))
        assertEquals(id, output.commonHeader.requestId)
    }

    @Test
    fun `test delete blueprint`() {
        val blockingStub = BluePrintManagementServiceGrpc.newBlockingStub(grpcServerRule.channel)
        val id = "123_delete"
        val req = createInputRequest(id)

        var output = blockingStub.uploadBlueprint(req)
        assertEquals(200, output.status.code)
        assertTrue(output.status.message.contains("Successfully uploaded blueprint sample:1.0.0 with id("))
        assertEquals(id, output.commonHeader.requestId)

        output = blockingStub.removeBlueprint(req)
        assertEquals(200, output.status.code)
    }

    private fun createInputRequest(id: String): BluePrintManagementInput {
        val file = File("./src/test/resources/test-cba.zip")
        assertTrue(file.exists(), "couldnt get file ${file.absolutePath}")

        val commonHeader = CommonHeader
                .newBuilder()
                .setTimestamp("2012-04-23T18:25:43.511Z")
                .setOriginatorId("System")
                .setRequestId(id)
                .setSubRequestId("1234-56").build()

        val fileChunk = FileChunk.newBuilder().setChunk(ByteString.copyFrom(file.inputStream().readBytes()))
                .build()

        return BluePrintManagementInput.newBuilder()
                .setCommonHeader(commonHeader)
                .setBlueprintName("sample")
                .setBlueprintVersion("1.0.0")
                .setFileChunk(fileChunk)
                .build()
    }
}

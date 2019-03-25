/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
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

import com.google.protobuf.ByteString
import io.grpc.testing.GrpcServerRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintManagementServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintRemoveInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintUploadInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
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
        deleteDir("target", "blueprints")
    }

    @AfterTest
    fun cleanDir() {
        deleteDir("target", "blueprints")
    }

    @Test
    fun `test upload blueprint`() {
        val blockingStub = BluePrintManagementServiceGrpc.newBlockingStub(grpcServerRule.channel)
        val id = "123_upload"
        val req = createUploadInputRequest(id)
        val output = blockingStub.uploadBlueprint(req)

        assertEquals(200, output.status.code)
        assertTrue(output.status.message.contains("Successfully uploaded CBA"))
        assertEquals(id, output.commonHeader.requestId)
    }

    @Test
    fun `test delete blueprint`() {
        val blockingStub = BluePrintManagementServiceGrpc.newBlockingStub(grpcServerRule.channel)
        val id = "123_delete"
        val req = createUploadInputRequest(id)

        var output = blockingStub.uploadBlueprint(req)
        assertEquals(200, output.status.code)
        assertTrue(output.status.message.contains("Successfully uploaded CBA"))
        assertEquals(id, output.commonHeader.requestId)

        val removeReq = createRemoveInputRequest(id)
        output = blockingStub.removeBlueprint(removeReq)
        assertEquals(200, output.status.code)
    }

    private fun createUploadInputRequest(id: String): BluePrintUploadInput {
        val file = normalizedFile("./src/test/resources/test-cba.zip")
        assertTrue(file.exists(), "couldnt get file ${file.absolutePath}")

        val commonHeader = CommonHeader
                .newBuilder()
                .setTimestamp("2012-04-23T18:25:43.511Z")
                .setOriginatorId("System")
                .setRequestId(id)
                .setSubRequestId("1234-56").build()

        val fileChunk = FileChunk.newBuilder().setChunk(ByteString.copyFrom(file.inputStream().readBytes()))
                .build()

        return BluePrintUploadInput.newBuilder()
                .setCommonHeader(commonHeader)
                .setFileChunk(fileChunk)
                .build()
    }

    private fun createRemoveInputRequest(id: String): BluePrintRemoveInput {
        val commonHeader = CommonHeader
                .newBuilder()
                .setTimestamp("2012-04-23T18:25:43.511Z")
                .setOriginatorId("System")
                .setRequestId(id)
                .setSubRequestId("1234-56").build()

        return BluePrintRemoveInput.newBuilder()
                .setCommonHeader(commonHeader)
                .setBlueprintName("sample")
                .setBlueprintVersion("1.0.0")
                .build()
    }
}

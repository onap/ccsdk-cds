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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api

import com.google.protobuf.ByteString
import io.grpc.testing.GrpcServerRule
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.GRPCLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TokenAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.TokenAuthGrpcClientService
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.compress
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintBootstrapInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintDownloadInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintRemoveInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintUploadInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.DownloadAction
import org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk
import org.onap.ccsdk.cds.controllerblueprints.management.api.RemoveAction
import org.onap.ccsdk.cds.controllerblueprints.management.api.UploadAction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [DesignerApiTestConfiguration::class, ErrorCatalogTestConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BlueprintManagementGRPCHandlerTest {

    @get:Rule
    val grpcServerRule = GrpcServerRule().directExecutor()

    @Autowired
    lateinit var bluePrintManagementGRPCHandler: BlueprintManagementGRPCHandler

    @BeforeTest
    fun init() {

        deleteDir("target", "blueprints")

        // Create sample CBA zip
        normalizedFile("./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")
            .compress(normalizedFile("./target/blueprints/generated-cba.zip"))

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcServerRule.serviceRegistry.addService(bluePrintManagementGRPCHandler)
    }

    @AfterTest
    fun cleanDir() {
        deleteDir("target", "blueprints")
    }

    @Test
    fun testBootstrap() {
        val blockingStub = BlueprintManagementServiceGrpc.newBlockingStub(grpcServerRule.channel)
        val id = "123_Bootstrap"
        val req = createBootstrapInputRequest(id)
        val bootstrapOutput = blockingStub.bootstrapBlueprint(req)
        assertEquals(200, bootstrapOutput.status.code)
        assertTrue(
            bootstrapOutput.status.message!!.contentEquals(BlueprintConstants.STATUS_SUCCESS),
            "failed to get success status"
        )
        assertEquals(EventType.EVENT_COMPONENT_EXECUTED, bootstrapOutput.status.eventType)
        assertEquals(id, bootstrapOutput.commonHeader.requestId)
    }

    @Test
    fun `test upload and download blueprint`() {
        val blockingStub = BlueprintManagementServiceGrpc.newBlockingStub(grpcServerRule.channel)
        val id = "123_upload"
        val req = createUploadInputRequest(id, UploadAction.PUBLISH.toString())
        val output = blockingStub.uploadBlueprint(req)

        assertEquals(200, output.status.code)
        assertTrue(
            output.status.message!!.contentEquals(BlueprintConstants.STATUS_SUCCESS),
            "failed to get success status"
        )
        assertEquals(EventType.EVENT_COMPONENT_EXECUTED, output.status.eventType)
        assertEquals(id, output.commonHeader.requestId)

        val downloadId = "123_download"
        val downloadReq = createDownloadInputRequest(downloadId, DownloadAction.SEARCH.toString())

        val downloadOutput = blockingStub.downloadBlueprint(downloadReq)
        assertEquals(200, downloadOutput.status.code)
        assertTrue(
            downloadOutput.status.message!!.contentEquals(BlueprintConstants.STATUS_SUCCESS),
            "failed to get success status"
        )
        assertEquals(EventType.EVENT_COMPONENT_EXECUTED, downloadOutput.status.eventType)
        assertNotNull(downloadOutput.fileChunk?.chunk, "failed to get cba file chunks")
        assertEquals(downloadId, downloadOutput.commonHeader.requestId)
    }

    @Test
    fun `test delete blueprint`() {
        val blockingStub = BlueprintManagementServiceGrpc.newBlockingStub(grpcServerRule.channel)
        val id = "123_delete"
        val req = createUploadInputRequest(id, UploadAction.DRAFT.toString())

        var output = blockingStub.uploadBlueprint(req)
        assertEquals(200, output.status.code)
        assertTrue(
            output.status.message!!.contentEquals(BlueprintConstants.STATUS_SUCCESS),
            "failed to get success status"
        )
        assertEquals(id, output.commonHeader.requestId)
        assertEquals(EventType.EVENT_COMPONENT_EXECUTED, output.status.eventType)

        val removeReq = createRemoveInputRequest(id)
        output = blockingStub.removeBlueprint(removeReq)
        assertEquals(200, output.status.code)
    }

    /** This is Integration test sample, Do not enable this test case in server build, this is for local desktop testing*/
    private fun integrationTestGrpcManagement() {
        runBlocking {
            val tokenAuthGrpcClientProperties = TokenAuthGrpcClientProperties().apply {
                host = "127.0.0.1"
                port = 9111
                type = GRPCLibConstants.TYPE_TOKEN_AUTH
                token = "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw=="
            }
            val basicAuthGrpcClientService = TokenAuthGrpcClientService(tokenAuthGrpcClientProperties)
            val channel = basicAuthGrpcClientService.channel()

            val blockingStub = BlueprintManagementServiceGrpc.newBlockingStub(channel)

            val bluePrintUploadInput = createUploadInputRequest("12345", UploadAction.DRAFT.toString())

            val bluePrintManagementOutput = blockingStub.uploadBlueprint(bluePrintUploadInput)
            assertNotNull(bluePrintManagementOutput, "failed to get response")
        }
    }

    private fun createBootstrapInputRequest(id: String): BlueprintBootstrapInput {
        val commonHeader = CommonHeader
            .newBuilder()
            .setTimestamp("2012-04-23T18:25:43.511Z")
            .setOriginatorId("System")
            .setRequestId(id)
            .setSubRequestId("1234-56").build()

        return BlueprintBootstrapInput.newBuilder()
            .setCommonHeader(commonHeader)
            .setLoadModelType(false)
            .setLoadResourceDictionary(false)
            .setLoadCBA(false)
            .build()
    }

    private fun createUploadInputRequest(id: String, action: String): BlueprintUploadInput {
        val file = normalizedFile("./target/blueprints/generated-cba.zip")
        assertTrue(file.exists(), "couldnt get file ${file.absolutePath}")

        val commonHeader = CommonHeader
            .newBuilder()
            .setTimestamp("2012-04-23T18:25:43.511Z")
            .setOriginatorId("System")
            .setRequestId(id)
            .setSubRequestId("1234-56").build()

        val actionIdentifier = ActionIdentifiers.newBuilder()
            .setActionName(action)
            .setBlueprintName("sample")
            .setBlueprintVersion("1.0.0")
            .build()

        val fileChunk = FileChunk.newBuilder().setChunk(ByteString.copyFrom(file.inputStream().readBytes()))
            .build()

        return BlueprintUploadInput.newBuilder()
            .setCommonHeader(commonHeader)
            .setActionIdentifiers(actionIdentifier)
            .setFileChunk(fileChunk)
            .build()
    }

    private fun createDownloadInputRequest(id: String, action: String): BlueprintDownloadInput {
        val commonHeader = CommonHeader
            .newBuilder()
            .setTimestamp("2012-04-23T18:25:43.511Z")
            .setOriginatorId("System")
            .setRequestId(id)
            .setSubRequestId("1234-56").build()

        return BlueprintDownloadInput.newBuilder()
            .setCommonHeader(commonHeader)
            .setActionIdentifiers(
                ActionIdentifiers.newBuilder()
                    .setBlueprintName("baseconfiguration")
                    .setBlueprintVersion("1.0.0")
                    .setActionName(action).build()
            )
            .build()
    }

    private fun createRemoveInputRequest(id: String): BlueprintRemoveInput {
        val commonHeader = CommonHeader
            .newBuilder()
            .setTimestamp("2012-04-23T18:25:43.511Z")
            .setOriginatorId("System")
            .setRequestId(id)
            .setSubRequestId("1234-56").build()

        return BlueprintRemoveInput.newBuilder()
            .setCommonHeader(commonHeader)
            .setActionIdentifiers(
                ActionIdentifiers.newBuilder()
                    .setBlueprintName("sample")
                    .setBlueprintVersion("1.0.0")
                    .setActionName(RemoveAction.DEFAULT.toString()).build()
            )
            .build()
    }
}

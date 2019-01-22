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

package org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api

import io.grpc.StatusException
import io.grpc.stub.StreamObserver
import org.apache.commons.io.FileUtils
import org.onap.ccsdk.apps.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api.utils.currentTimestamp
import org.onap.ccsdk.apps.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.apps.controllerblueprints.common.api.Status
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.apps.controllerblueprints.management.api.BluePrintManagementInput
import org.onap.ccsdk.apps.controllerblueprints.management.api.BluePrintManagementOutput
import org.onap.ccsdk.apps.controllerblueprints.management.api.BluePrintManagementServiceGrpc
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class BluePrintManagementGRPCHandler(private val bluePrintCoreConfiguration: BluePrintCoreConfiguration,
                                     private val bluePrintCatalogService: BluePrintCatalogService)
    : BluePrintManagementServiceGrpc.BluePrintManagementServiceImplBase() {

    private val log = LoggerFactory.getLogger(BluePrintManagementGRPCHandler::class.java)

    override fun uploadBlueprint(request: BluePrintManagementInput, responseObserver: StreamObserver<BluePrintManagementOutput>) {
        val blueprintName = request.blueprintName
        val blueprintVersion = request.blueprintVersion
        val blueprint = "blueprint $blueprintName:$blueprintVersion"

        log.info("request(${request.commonHeader.requestId}): Received upload $blueprint")

        val blueprintArchivedFilePath = "${bluePrintCoreConfiguration.archivePath}/$blueprintName/$blueprintVersion/$blueprintName.zip"
        try {
            val blueprintArchivedFile = File(blueprintArchivedFilePath)

            saveToDisk(request, blueprintArchivedFile)
            val blueprintId = bluePrintCatalogService.saveToDatabase(blueprintArchivedFile)

            File("${bluePrintCoreConfiguration.archivePath}/$blueprintName").deleteRecursively()

            responseObserver.onNext(successStatus("Successfully uploaded $blueprint with id($blueprintId)", request.commonHeader))
            responseObserver.onCompleted()
        } catch (e: Exception) {
            failStatus("request(${request.commonHeader.requestId}): Failed to upload $blueprint at path $blueprintArchivedFilePath", e)
        }
    }

    override fun removeBlueprint(request: BluePrintManagementInput, responseObserver: StreamObserver<BluePrintManagementOutput>) {
        val blueprintName = request.blueprintName
        val blueprintVersion = request.blueprintVersion
        val blueprint = "blueprint $blueprintName:$blueprintVersion"

        log.info("request(${request.commonHeader.requestId}): Received delete $blueprint")

        try {
            bluePrintCatalogService.deleteFromDatabase(blueprintName, blueprintVersion)
            responseObserver.onNext(successStatus("Successfully deleted $blueprint", request.commonHeader))
            responseObserver.onCompleted()
        } catch (e: Exception) {
            failStatus("request(${request.commonHeader.requestId}): Failed to delete $blueprint", e)
        }
    }

    private fun saveToDisk(request: BluePrintManagementInput, blueprintDir: File) {
        log.debug("request(${request.commonHeader.requestId}): Writing CBA File under :${blueprintDir.absolutePath}")
        if (blueprintDir.exists()) {
            log.debug("request(${request.commonHeader.requestId}): Re-creating blueprint directory(${blueprintDir.absolutePath})")
            FileUtils.deleteDirectory(blueprintDir.parentFile)
        }
        FileUtils.forceMkdir(blueprintDir.parentFile)
        blueprintDir.writeBytes(request.fileChunk.chunk.toByteArray()).apply {
            log.debug("request(${request.commonHeader.requestId}): CBA file(${blueprintDir.absolutePath} written successfully")
        }
    }

    private fun successStatus(message: String, header: CommonHeader): BluePrintManagementOutput =
            BluePrintManagementOutput.newBuilder()
                    .setCommonHeader(header)
                    .setStatus(Status.newBuilder()
                            .setTimestamp(currentTimestamp())
                            .setMessage(message)
                            .setCode(200)
                            .build())
                    .build()

    private fun failStatus(message: String, e: Exception): StatusException {
        log.error(message, e)
        return io.grpc.Status.INTERNAL
                .withDescription(message)
                .withCause(e)
                .asException()
    }
}

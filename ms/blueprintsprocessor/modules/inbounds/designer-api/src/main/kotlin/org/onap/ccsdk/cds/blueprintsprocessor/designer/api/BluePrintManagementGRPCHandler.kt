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
import io.grpc.StatusException
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler.BluePrintModelHandler
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.emptyTONull
import org.onap.ccsdk.cds.controllerblueprints.core.utils.currentTimestamp
import org.onap.ccsdk.cds.controllerblueprints.management.api.*
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
open class BluePrintManagementGRPCHandler(private val bluePrintModelHandler: BluePrintModelHandler)
    : BluePrintManagementServiceGrpc.BluePrintManagementServiceImplBase() {

    private val log = LoggerFactory.getLogger(BluePrintManagementGRPCHandler::class.java)

    @PreAuthorize("hasRole('USER')")
    override fun uploadBlueprint(request: BluePrintUploadInput, responseObserver:
    StreamObserver<BluePrintManagementOutput>) {

        runBlocking {
            log.info("request(${request.commonHeader.requestId})")
            try {
                /** Get the file byte array */
                val byteArray = request.fileChunk.chunk.toByteArray()
                /** Get the Upload Action */
                val uploadAction = request.actionIdentifiers?.actionName.emptyTONull()
                        ?: UploadAction.DRAFT.toString()

                when (uploadAction) {
                    UploadAction.DRAFT.toString() -> {
                        val blueprintModel = bluePrintModelHandler.upload(byteArray, false)
                        responseObserver.onNext(successStatus(request.commonHeader))
                    }
                    UploadAction.PUBLISH.toString() -> {
                        val blueprintModel = bluePrintModelHandler.upload(byteArray, true)
                        responseObserver.onNext(successStatus(request.commonHeader))
                    }
                    UploadAction.VALIDATE.toString() -> {
                        //TODO("Not Implemented")
                        responseObserver.onError(failStatus("Not Implemented",
                                BluePrintProcessorException("Not Implemented")))
                    }
                    UploadAction.ENRICH.toString() -> {
                        val enrichedByteArray = bluePrintModelHandler.enrichBlueprintFileSource(byteArray)
                        responseObserver.onNext(enrichmentStatus(request.commonHeader, enrichedByteArray))
                    }
                    else -> {
                        responseObserver.onError(failStatus("Upload action($uploadAction) not implemented",
                                BluePrintProcessorException("Upload action($uploadAction) not implemented")))
                    }
                }
                responseObserver.onCompleted()
            } catch (e: Exception) {
                responseObserver.onError(failStatus("request(${request.commonHeader.requestId}): Failed to upload CBA", e))
            }
        }
    }

    @PreAuthorize("hasRole('USER')")
    override fun removeBlueprint(request: BluePrintRemoveInput, responseObserver:
    StreamObserver<BluePrintManagementOutput>) {

        runBlocking {
            val blueprintName = request.blueprintName
            val blueprintVersion = request.blueprintVersion
            val blueprint = "blueprint $blueprintName:$blueprintVersion"

            log.info("request(${request.commonHeader.requestId}): Received delete $blueprint")
            try {
                bluePrintModelHandler.deleteBlueprintModel(blueprintName, blueprintVersion)
                responseObserver.onNext(successStatus(request.commonHeader))
                responseObserver.onCompleted()
            } catch (e: Exception) {
                responseObserver.onError(failStatus("request(${request.commonHeader.requestId}): Failed to delete $blueprint", e))
            }
        }
    }

    private fun enrichmentStatus(header: CommonHeader, byteArray: ByteArray): BluePrintManagementOutput =
            BluePrintManagementOutput.newBuilder()
                    .setCommonHeader(header)
                    .setFileChunk(FileChunk.newBuilder().setChunk(ByteString.copyFrom(byteArray)))
                    .setStatus(Status.newBuilder()
                            .setTimestamp(currentTimestamp())
                            .setMessage(BluePrintConstants.STATUS_SUCCESS)
                            .setCode(200)
                            .build())
                    .build()

    private fun successStatus(header: CommonHeader): BluePrintManagementOutput =
            BluePrintManagementOutput.newBuilder()
                    .setCommonHeader(header)
                    .setStatus(Status.newBuilder()
                            .setTimestamp(currentTimestamp())
                            .setMessage(BluePrintConstants.STATUS_SUCCESS)
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

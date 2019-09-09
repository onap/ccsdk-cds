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
import com.google.protobuf.util.JsonFormat
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler.BluePrintModelHandler
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.emptyTONull
import org.onap.ccsdk.cds.controllerblueprints.core.utils.currentTimestamp
import org.onap.ccsdk.cds.controllerblueprints.management.api.*
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

//TODO("Convert to coroutines handler")
@Service
open class BluePrintManagementGRPCHandler(private val bluePrintModelHandler: BluePrintModelHandler)
    : BluePrintManagementServiceGrpc.BluePrintManagementServiceImplBase() {

    private val log = LoggerFactory.getLogger(BluePrintManagementGRPCHandler::class.java)

    @PreAuthorize("hasRole('USER')")
    override fun uploadBlueprint(request: BluePrintUploadInput, responseObserver:
    StreamObserver<BluePrintManagementOutput>) {

        runBlocking {
            //TODO("catch if request id is missing")
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
                        responseObserver.onNext(successStatus(request.commonHeader, blueprintModel.asJsonString()))
                    }
                    UploadAction.PUBLISH.toString() -> {
                        val blueprintModel = bluePrintModelHandler.upload(byteArray, true)
                        responseObserver.onNext(successStatus(request.commonHeader, blueprintModel.asJsonString()))
                    }
                    UploadAction.VALIDATE.toString() -> {
                        //TODO("Not Implemented")
                        responseObserver.onNext(failStatus(request.commonHeader,
                                "Upload action($uploadAction) not implemented",
                                BluePrintProcessorException("Not Implemented")))
                    }
                    UploadAction.ENRICH.toString() -> {
                        val enrichedByteArray = bluePrintModelHandler.enrichBlueprintFileSource(byteArray)
                        responseObserver.onNext(outputWithFileBytes(request.commonHeader, enrichedByteArray))
                    }
                    else -> {
                        responseObserver.onNext(failStatus(request.commonHeader,
                                "Upload action($uploadAction) not implemented",
                                BluePrintProcessorException("Not implemented")))
                    }
                }
            } catch (e: Exception) {
                responseObserver.onNext(failStatus(request.commonHeader,
                        "request(${request.commonHeader.requestId}): Failed to upload CBA", e))
            } finally {
                responseObserver.onCompleted()
            }
        }
    }

    @PreAuthorize("hasRole('USER')")
    override fun downloadBlueprint(request: BluePrintDownloadInput,
                                   responseObserver: StreamObserver<BluePrintManagementOutput>) {
        runBlocking {
            val blueprintName = request.actionIdentifiers.blueprintName
            val blueprintVersion = request.actionIdentifiers.blueprintVersion
            val blueprint = "blueprint $blueprintName:$blueprintVersion"

            /** Get the Search Action */
            val searchAction = request.actionIdentifiers?.actionName.emptyTONull()
                    ?: DownloadAction.SEARCH.toString()

            log.info("request(${request.commonHeader.requestId}): Received download $blueprint")
            try {
                when (searchAction) {
                    DownloadAction.SEARCH.toString() -> {
                        val downloadByteArray = bluePrintModelHandler.download(blueprintName, blueprintVersion)
                        responseObserver.onNext(outputWithFileBytes(request.commonHeader, downloadByteArray))
                    }
                    else -> {
                        responseObserver.onNext(failStatus(request.commonHeader,
                                "Search action($searchAction) not implemented",
                                BluePrintProcessorException("Not implemented")))
                    }
                }
            } catch (e: Exception) {
                responseObserver.onNext(failStatus(request.commonHeader,
                        "request(${request.commonHeader.requestId}): Failed to delete $blueprint", e))
            } finally {
                responseObserver.onCompleted()
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
            } catch (e: Exception) {
                responseObserver.onNext(failStatus(request.commonHeader,
                        "request(${request.commonHeader.requestId}): Failed to delete $blueprint", e))
            } finally {
                responseObserver.onCompleted()
            }
        }
    }

    private fun outputWithFileBytes(header: CommonHeader, byteArray: ByteArray): BluePrintManagementOutput =
            BluePrintManagementOutput.newBuilder()
                    .setCommonHeader(header)
                    .setFileChunk(FileChunk.newBuilder().setChunk(ByteString.copyFrom(byteArray)))
                    .setStatus(Status.newBuilder()
                            .setTimestamp(currentTimestamp())
                            .setMessage(BluePrintConstants.STATUS_SUCCESS)
                            .setCode(200)
                            .build())
                    .build()

    private fun successStatus(header: CommonHeader, propertyContent: String? = null): BluePrintManagementOutput {
        // Populate Response Payload
        val propertiesBuilder = BluePrintManagementOutput.newBuilder().propertiesBuilder
        propertyContent?.let {
            JsonFormat.parser().merge(propertyContent, propertiesBuilder)
        }
        return BluePrintManagementOutput.newBuilder()
                .setCommonHeader(header)
                .setProperties(propertiesBuilder.build())
                .setStatus(Status.newBuilder()
                        .setTimestamp(currentTimestamp())
                        .setMessage(BluePrintConstants.STATUS_SUCCESS)
                        .setCode(200)
                        .build())
                .build()
    }

    private fun failStatus(header: CommonHeader, message: String, e: Exception): BluePrintManagementOutput {
        log.error(message, e)
        return BluePrintManagementOutput.newBuilder()
                .setCommonHeader(header)
                .setStatus(Status.newBuilder()
                        .setTimestamp(currentTimestamp())
                        .setMessage(BluePrintConstants.STATUS_FAILURE)
                        .setErrorMessage(message)
                        .setCode(500)
                        .build())
                .build()
//        return io.grpc.Status.INTERNAL
//                .withDescription(message)
//                .withCause(e)
//                .asException()
    }
}

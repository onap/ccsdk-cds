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
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler.BlueprintModelHandler
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.emptyTONull
import org.onap.ccsdk.cds.controllerblueprints.core.utils.currentTimestamp
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintBootstrapInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintDownloadInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutput
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintRemoveInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintUploadInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.DownloadAction
import org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk
import org.onap.ccsdk.cds.controllerblueprints.management.api.RemoveAction
import org.onap.ccsdk.cds.controllerblueprints.management.api.UploadAction
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.onap.ccsdk.cds.error.catalog.core.GrpcErrorCodes
import org.onap.ccsdk.cds.error.catalog.core.utils.errorMessageOrDefault
import org.onap.ccsdk.cds.error.catalog.services.ErrorCatalogService
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

// TODO("Convert to coroutines handler")
@Service
open class BlueprintManagementGRPCHandler(
    private val bluePrintModelHandler: BlueprintModelHandler,
    private val errorCatalogService: ErrorCatalogService
) :
    BlueprintManagementServiceGrpc.BlueprintManagementServiceImplBase() {

    private val log = LoggerFactory.getLogger(BlueprintManagementGRPCHandler::class.java)

    @PreAuthorize("hasRole('USER')")
    override fun uploadBlueprint(
        request: BlueprintUploadInput,
        responseObserver: StreamObserver<BlueprintManagementOutput>
    ) {

        runBlocking {
            // TODO("catch if request id is missing")
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
                        // TODO("Not Implemented")
                        responseObserver.onNext(
                            failStatus(
                                request.commonHeader,
                                "Upload action($uploadAction) not implemented",
                                BlueprintProcessorException("Not Implemented")
                            )
                        )
                    }
                    UploadAction.ENRICH.toString() -> {
                        val enrichedByteArray = bluePrintModelHandler.enrichBlueprintFileSource(byteArray)
                        responseObserver.onNext(outputWithFileBytes(request.commonHeader, enrichedByteArray))
                    }
                    else -> {
                        responseObserver.onNext(
                            failStatus(
                                request.commonHeader,
                                "Upload action($uploadAction) not implemented",
                                BlueprintProcessorException("Not implemented")
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                responseObserver.onNext(
                    failStatus(
                        request.commonHeader,
                        "request(${request.commonHeader.requestId}): Failed to upload CBA", e
                    )
                )
            } finally {
                responseObserver.onCompleted()
            }
        }
    }

    @PreAuthorize("hasRole('USER')")
    override fun downloadBlueprint(
        request: BlueprintDownloadInput,
        responseObserver: StreamObserver<BlueprintManagementOutput>
    ) {
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
                        responseObserver.onNext(
                            failStatus(
                                request.commonHeader,
                                "Search action($searchAction) not implemented",
                                BlueprintProcessorException("Not implemented")
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                responseObserver.onNext(
                    failStatus(
                        request.commonHeader,
                        "request(${request.commonHeader.requestId}): Failed to delete $blueprint", e
                    )
                )
            } finally {
                responseObserver.onCompleted()
            }
        }
    }

    @PreAuthorize("hasRole('USER')")
    override fun removeBlueprint(
        request: BlueprintRemoveInput,
        responseObserver:
            StreamObserver<BlueprintManagementOutput>
    ) {

        runBlocking {
            val blueprintName = request.actionIdentifiers.blueprintName
            val blueprintVersion = request.actionIdentifiers.blueprintVersion
            val blueprint = "blueprint $blueprintName:$blueprintVersion"

            log.info("request(${request.commonHeader.requestId}): Received delete $blueprint")

            /** Get the Remove Action */
            val removeAction = request.actionIdentifiers?.actionName.emptyTONull()
                ?: RemoveAction.DEFAULT.toString()

            try {
                when (removeAction) {
                    RemoveAction.DEFAULT.toString() -> {
                        bluePrintModelHandler.deleteBlueprintModel(blueprintName, blueprintVersion)
                        responseObserver.onNext(successStatus(request.commonHeader))
                    }
                    else -> {
                        responseObserver.onNext(
                            failStatus(
                                request.commonHeader,
                                "Remove action($removeAction) not implemented",
                                BlueprintProcessorException("Not implemented")
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                responseObserver.onNext(
                    failStatus(
                        request.commonHeader,
                        "request(${request.commonHeader.requestId}): Failed to delete $blueprint", e
                    )
                )
            } finally {
                responseObserver.onCompleted()
            }
        }
    }

    override fun bootstrapBlueprint(
        request: BlueprintBootstrapInput,
        responseObserver: StreamObserver<BlueprintManagementOutput>
    ) {
        runBlocking {
            try {
                log.info("request(${request.commonHeader.requestId}): Received bootstrap request")
                val bootstrapRequest = BootstrapRequest().apply {
                    loadModelType = request.loadModelType
                    loadResourceDictionary = request.loadResourceDictionary
                    loadCBA = request.loadCBA
                }
                /** Perform bootstrap of Model Types, Resource Definitions and CBA */
                bluePrintModelHandler.bootstrapBlueprint(bootstrapRequest)
                responseObserver.onNext(successStatus(request.commonHeader))
            } catch (e: Exception) {
                responseObserver.onNext(
                    failStatus(
                        request.commonHeader,
                        "request(${request.commonHeader.requestId}): Failed to bootstrap", e
                    )
                )
            } finally {
                responseObserver.onCompleted()
            }
        }
    }

    private fun outputWithFileBytes(header: CommonHeader, byteArray: ByteArray): BlueprintManagementOutput =
        BlueprintManagementOutput.newBuilder()
            .setCommonHeader(header)
            .setFileChunk(FileChunk.newBuilder().setChunk(ByteString.copyFrom(byteArray)))
            .setStatus(
                Status.newBuilder()
                    .setTimestamp(currentTimestamp())
                    .setEventType(EventType.EVENT_COMPONENT_EXECUTED)
                    .setMessage(BlueprintConstants.STATUS_SUCCESS)
                    .setCode(200)
                    .build()
            )
            .build()

    private fun successStatus(header: CommonHeader, propertyContent: String? = null): BlueprintManagementOutput {
        // Populate Response Payload
        val propertiesBuilder = BlueprintManagementOutput.newBuilder().propertiesBuilder
        propertyContent?.let {
            JsonFormat.parser().merge(propertyContent, propertiesBuilder)
        }
        return BlueprintManagementOutput.newBuilder()
            .setCommonHeader(header)
            .setProperties(propertiesBuilder.build())
            .setStatus(
                Status.newBuilder()
                    .setTimestamp(currentTimestamp())
                    .setMessage(BlueprintConstants.STATUS_SUCCESS)
                    .setEventType(EventType.EVENT_COMPONENT_EXECUTED)
                    .setCode(200)
                    .build()
            )
            .build()
    }

    private fun failStatus(header: CommonHeader, message: String, e: Exception): BlueprintManagementOutput {
        log.error(message, e)
        return if (e is BlueprintProcessorException) onErrorCatalog(header, message, e) else onError(header, message, e)
    }

    private fun onError(header: CommonHeader, message: String, error: Exception): BlueprintManagementOutput {
        val code = GrpcErrorCodes.code(ErrorCatalogCodes.GENERIC_FAILURE)
        return BlueprintManagementOutput.newBuilder()
            .setCommonHeader(header)
            .setStatus(
                Status.newBuilder()
                    .setTimestamp(currentTimestamp())
                    .setMessage(BlueprintConstants.STATUS_FAILURE)
                    .setEventType(EventType.EVENT_COMPONENT_FAILURE)
                    .setErrorMessage("Error : $message \n Details: ${error.errorMessageOrDefault()}")
                    .setCode(code)
                    .build()
            )
            .build()
    }

    private fun onErrorCatalog(header: CommonHeader, message: String, error: BlueprintProcessorException):
        BlueprintManagementOutput {
            val err = if (error.protocol == "") {
                error.grpc(ErrorCatalogCodes.GENERIC_FAILURE)
            } else {
                error.convertToGrpc()
            }
            val errorPayload = errorCatalogService.errorPayload(err.addErrorPayloadMessage(message))
            return BlueprintManagementOutput.newBuilder()
                .setCommonHeader(header)
                .setStatus(
                    Status.newBuilder()
                        .setTimestamp(currentTimestamp())
                        .setMessage(BlueprintConstants.STATUS_FAILURE)
                        .setEventType(EventType.EVENT_COMPONENT_FAILURE)
                        .setErrorMessage("Error : ${errorPayload.message}")
                        .setCode(errorPayload.code)
                        .build()
                )
                .build()
        }
}

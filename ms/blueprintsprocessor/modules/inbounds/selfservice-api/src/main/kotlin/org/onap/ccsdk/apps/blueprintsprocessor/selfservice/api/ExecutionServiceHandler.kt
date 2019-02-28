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

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onap.ccsdk.apps.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ACTION_MODE_ASYNC
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ACTION_MODE_SYNC
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api.utils.saveCBAFile
import org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api.utils.toProto
import org.onap.ccsdk.apps.blueprintsprocessor.services.workflow.BlueprintDGExecutionService
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintFileUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.slf4j.LoggerFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ExecutionServiceHandler(private val bluePrintCoreConfiguration: BluePrintCoreConfiguration,
                              private val bluePrintCatalogService: BluePrintCatalogService,
                              private val blueprintDGExecutionService: BlueprintDGExecutionService) {

    private val log = LoggerFactory.getLogger(ExecutionServiceHandler::class.toString())

    fun upload(filePart: FilePart): Mono<String> {
        try {
            val archivedPath = BluePrintFileUtils.getCbaStorageDirectory(bluePrintCoreConfiguration.archivePath)
            val cbaPath = saveCBAFile(filePart, archivedPath)
            bluePrintCatalogService.saveToDatabase(cbaPath.toFile()).let {
                return Mono.just("{\"status\": \"Successfully uploaded blueprint with id($it)\"}")
            }
        } catch (e: Exception) {
            return Mono.error<String>(BluePrintException("Error uploading the CBA file.", e))
        }
    }

    fun process(executionServiceInput: ExecutionServiceInput,
                responseObserver: StreamObserver<org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceOutput>) {
        when {
            executionServiceInput.actionIdentifiers.mode == ACTION_MODE_ASYNC -> {
                GlobalScope.launch(Dispatchers.Default) {
                    val executionServiceOutput = doProcess(executionServiceInput)
                    responseObserver.onNext(executionServiceOutput.toProto())
                    responseObserver.onCompleted()
                }
                responseObserver.onNext(response(executionServiceInput).toProto())
            }
            executionServiceInput.actionIdentifiers.mode == ACTION_MODE_SYNC -> {
                val executionServiceOutput = doProcess(executionServiceInput)
                responseObserver.onNext(executionServiceOutput.toProto())
                responseObserver.onCompleted()
            }
            else -> responseObserver.onNext(response(executionServiceInput,
                "Failed to process request, 'actionIdentifiers.mode' not specified. Valid value are: 'sync' or 'async'.",
                true).toProto());
        }
    }

    fun processSync(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        return doProcess(executionServiceInput)
    }

    private fun doProcess(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        val requestId = executionServiceInput.commonHeader.requestId
        log.info("processing request id $requestId")

        val actionIdentifiers = executionServiceInput.actionIdentifiers

        val blueprintName = actionIdentifiers.blueprintName
        val blueprintVersion = actionIdentifiers.blueprintVersion

        val basePath = bluePrintCatalogService.getFromDatabase(blueprintName, blueprintVersion)
        log.info("blueprint base path $basePath")

        val blueprintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(requestId, basePath.toString())

        return blueprintDGExecutionService.executeDirectedGraph(blueprintRuntimeService, executionServiceInput)
    }

    private fun response(executionServiceInput: ExecutionServiceInput, errorMessage: String = "",
                         failure: Boolean = false): ExecutionServiceOutput {
        val executionServiceOutput = ExecutionServiceOutput()
        executionServiceOutput.commonHeader = executionServiceInput.commonHeader
        executionServiceOutput.actionIdentifiers = executionServiceInput.actionIdentifiers
        executionServiceOutput.payload = JsonNodeFactory.instance.objectNode()

        val status = Status()
        status.errorMessage = errorMessage
        if (failure) {
            status.eventType = "EVENT-COMPONENT-FAILURE"
            status.code = 500
            status.message = BluePrintConstants.STATUS_FAILURE
        } else {
            status.eventType = "EVENT-COMPONENT-PROCESSING"
            status.code = 200
            status.message = BluePrintConstants.STATUS_PROCESSING
        }

        executionServiceOutput.status = status

        return executionServiceOutput
    }
}
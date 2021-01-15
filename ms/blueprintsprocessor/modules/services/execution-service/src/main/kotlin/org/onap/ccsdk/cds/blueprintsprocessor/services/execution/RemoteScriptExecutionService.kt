/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2020 Bell Canada.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 package org.onap.ccsdk.cds.controllerblueprints.command.api;
*
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

import com.fasterxml.jackson.databind.JsonNode
import com.google.protobuf.Struct
import com.google.protobuf.Timestamp
import com.google.protobuf.util.JsonFormat
import io.grpc.ManagedChannel
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.PrepareRemoteEnvInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteIdentifier
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteScriptUploadBlueprintInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteScriptExecutionInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteScriptExecutionOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteScriptUploadBlueprintOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StatusType
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.BluePrintGrpcClientService
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.BluePrintGrpcLibPropertyService
import org.onap.ccsdk.cds.controllerblueprints.command.api.CommandExecutorServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.command.api.ExecutionInput
import org.onap.ccsdk.cds.controllerblueprints.command.api.ExecutionOutput
import org.onap.ccsdk.cds.controllerblueprints.command.api.Identifiers
import org.onap.ccsdk.cds.controllerblueprints.command.api.Packages
import org.onap.ccsdk.cds.controllerblueprints.command.api.PrepareEnvInput
import org.onap.ccsdk.cds.controllerblueprints.command.api.UploadBlueprintInput
import org.onap.ccsdk.cds.controllerblueprints.command.api.UploadBlueprintOutput
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

interface RemoteScriptExecutionService {

    suspend fun init(selector: Any)
    suspend fun uploadBlueprint(uploadBpInput: RemoteScriptUploadBlueprintInput): RemoteScriptUploadBlueprintOutput
    suspend fun prepareEnv(prepareEnvInput: PrepareRemoteEnvInput): RemoteScriptExecutionOutput
    suspend fun executeCommand(remoteExecutionInput: RemoteScriptExecutionInput): RemoteScriptExecutionOutput
    suspend fun close()
}

@Service(ExecutionServiceConstant.SERVICE_GRPC_REMOTE_SCRIPT_EXECUTION)
@ConditionalOnProperty(
    prefix = "blueprintprocessor.remoteScriptCommand", name = arrayOf("enabled"),
    havingValue = "true", matchIfMissing = false
)
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class GrpcRemoteScriptExecutionService(private val bluePrintGrpcLibPropertyService: BluePrintGrpcLibPropertyService) :
    RemoteScriptExecutionService {

    private val log = LoggerFactory.getLogger(GrpcRemoteScriptExecutionService::class.java)!!

    private var channel: ManagedChannel? = null
    private lateinit var commandExecutorServiceGrpc: CommandExecutorServiceGrpc.CommandExecutorServiceBlockingStub

    override suspend fun init(selector: Any) {
        // Get the GRPC Client Service based on selector
        val grpcClientService: BluePrintGrpcClientService = if (selector is JsonNode) {
            bluePrintGrpcLibPropertyService.blueprintGrpcClientService(selector)
        } else {
            bluePrintGrpcLibPropertyService.blueprintGrpcClientService(selector.toString())
        }

        // Get the GRPC Channel
        channel = grpcClientService.channel()
        // Create Non Blocking Stub
        commandExecutorServiceGrpc = CommandExecutorServiceGrpc.newBlockingStub(channel)

        checkNotNull(commandExecutorServiceGrpc) {
            "failed to create command executor grpc client for selector($selector)"
        }
    }

    override suspend fun uploadBlueprint(uploadBPInput: RemoteScriptUploadBlueprintInput): RemoteScriptUploadBlueprintOutput {
        val logPart = "requestId(${uploadBPInput.requestId}) subRequestId(${uploadBPInput.subRequestId}) blueprintName(${uploadBPInput.remoteIdentifier?.blueprintName}) blueprintVersion(${uploadBPInput.remoteIdentifier?.blueprintVersion}) blueprintUUID(${uploadBPInput.remoteIdentifier?.blueprintUUID})"
        val grpcResponse = commandExecutorServiceGrpc
            .withDeadlineAfter(uploadBPInput.timeOut * 1000, TimeUnit.MILLISECONDS)
            .uploadBlueprint(uploadBPInput.asGrpcData())
        checkNotNull(grpcResponse.status) {
            "failed to get GRPC upload CBA response status for $logPart"
        }
        val uploadBlueprinOutput = grpcResponse.asJavaData()
        log.info("Received Upload CBA response status(${uploadBlueprinOutput.status}) for $logPart payload(${uploadBlueprinOutput.payload})")
        return uploadBlueprinOutput
    }

    override suspend fun prepareEnv(prepareEnvInput: PrepareRemoteEnvInput): RemoteScriptExecutionOutput {
        val grpResponse = commandExecutorServiceGrpc
            .withDeadlineAfter(prepareEnvInput.timeOut * 1000, TimeUnit.MILLISECONDS)
            .prepareEnv(prepareEnvInput.asGrpcData())

        checkNotNull(grpResponse.status) {
            "failed to get GRPC prepare env response status for requestId(${prepareEnvInput.requestId})"
        }

        val remoteScriptExecutionOutput = grpResponse.asJavaData()
        log.debug("Received prepare env response from command server for requestId(${prepareEnvInput.requestId})")

        return remoteScriptExecutionOutput
    }

    override suspend fun executeCommand(remoteExecutionInput: RemoteScriptExecutionInput): RemoteScriptExecutionOutput {
        val grpResponse =
            commandExecutorServiceGrpc
                .withDeadlineAfter(remoteExecutionInput.timeOut * 1000, TimeUnit.MILLISECONDS)
                .executeCommand(remoteExecutionInput.asGrpcData())

        checkNotNull(grpResponse.status) {
            "failed to get GRPC response status for requestId(${remoteExecutionInput.requestId})"
        }

        log.debug("Received response from command server for requestId(${remoteExecutionInput.requestId})")
        return grpResponse.asJavaData()
    }

    override suspend fun close() {
        channel?.shutdownNow()
    }

    fun RemoteScriptUploadBlueprintInput.asGrpcData(): UploadBlueprintInput {
        val correlationId = this.correlationId ?: this.requestId
        return UploadBlueprintInput.newBuilder()
            .setIdentifiers(this.remoteIdentifier!!.asGrpcData())
            .setRequestId(this.requestId)
            .setSubRequestId(this.subRequestId)
            .setOriginatorId(this.originatorId)
            .setCorrelationId(correlationId)
            .setTimestamp(Timestamp.getDefaultInstance())
            .setBinData(this.binData)
            .setArchiveType(this.archiveType)
            .setTimeOut(this.timeOut.toInt())
            .build()
    }

    fun PrepareRemoteEnvInput.asGrpcData(): PrepareEnvInput {
        val correlationId = this.correlationId ?: this.requestId

        val packageList = mutableListOf<Packages>()

        this.packages.toList().forEach {
            val pckage = Packages.newBuilder()
            JsonFormat.parser().merge(it.toString(), pckage)
            packageList.add(pckage.build())
        }

        return PrepareEnvInput.newBuilder()
            .setIdentifiers(this.remoteIdentifier!!.asGrpcData())
            .setRequestId(this.requestId)
            .setSubRequestId(this.subRequestId)
            .setOriginatorId(this.originatorId)
            .setCorrelationId(correlationId)
            .setTimeOut(this.timeOut.toInt())
            .addAllPackages(packageList)
            .setProperties(this.properties.asGrpcData())
            .build()
    }

    fun RemoteScriptExecutionInput.asGrpcData(): ExecutionInput {
        val correlationId = this.correlationId ?: this.requestId
        return ExecutionInput.newBuilder()
            .setRequestId(this.requestId)
            .setSubRequestId(this.subRequestId)
            .setOriginatorId(this.originatorId)
            .setCorrelationId(correlationId)
            .setIdentifiers(this.remoteIdentifier!!.asGrpcData())
            .setCommand(this.command)
            .setTimeOut(this.timeOut.toInt())
            .setProperties(this.properties.asGrpcData())
            .setTimestamp(Timestamp.getDefaultInstance())
            .build()
    }

    fun RemoteIdentifier.asGrpcData(): Identifiers? {
        return Identifiers.newBuilder()
            .setBlueprintName(this.blueprintName)
            .setBlueprintVersion(this.blueprintVersion)
            .setBlueprintUUID(this.blueprintUUID)
            .build()
    }

    fun Map<String, JsonNode>.asGrpcData(): Struct {
        val struct = Struct.newBuilder()
        JsonFormat.parser().merge(JacksonUtils.getJson(this), struct)
        return struct.build()
    }

    fun ExecutionOutput.asJavaData(): RemoteScriptExecutionOutput {
        return RemoteScriptExecutionOutput(
            requestId = this.requestId,
            response = this.responseList,
            status = StatusType.valueOf(this.status.name),
            payload = payload.jsonAsJsonType()
        )
    }

    fun UploadBlueprintOutput.asJavaData(): RemoteScriptUploadBlueprintOutput {
        return RemoteScriptUploadBlueprintOutput(
            requestId = this.requestId,
            subRequestId = this.subRequestId,
            status = StatusType.valueOf(this.status.name),
            payload = payload.jsonAsJsonType()
        )
    }
}

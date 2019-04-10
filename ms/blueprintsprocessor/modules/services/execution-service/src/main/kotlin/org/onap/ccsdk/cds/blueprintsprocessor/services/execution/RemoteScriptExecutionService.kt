/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.*
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.BluePrintGrpcLibPropertyService
import org.onap.ccsdk.cds.controllerblueprints.command.api.*
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service


interface RemoteScriptExecutionService {
    suspend fun init(selector: String)
    suspend fun prepareEnv(prepareEnvInput: PrepareRemoteEnvInput): RemoteScriptExecutionOutput
    suspend fun executeCommand(remoteExecutionInput: RemoteScriptExecutionInput): RemoteScriptExecutionOutput
    suspend fun close()
}

@Service(ExecutionServiceConstant.SERVICE_GRPC_REMOTE_SCRIPT_EXECUTION)
@ConditionalOnProperty(prefix = "blueprintprocessor.remoteScriptCommand", name = arrayOf("enabled"),
        havingValue = "true", matchIfMissing = false)
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class GrpcRemoteScriptExecutionService(private val bluePrintGrpcLibPropertyService: BluePrintGrpcLibPropertyService)
    : RemoteScriptExecutionService {

    private val log = LoggerFactory.getLogger(GrpcRemoteScriptExecutionService::class.java)!!

    lateinit var channel: ManagedChannel
    lateinit var commandExecutorServiceGrpc: CommandExecutorServiceGrpc.CommandExecutorServiceFutureStub

    override suspend fun init(selector: String) {
        // Get the GRPC Client Service based on selector
        val grpcClientService = bluePrintGrpcLibPropertyService.blueprintGrpcClientService(selector)
        // Get the GRPC Channel
        channel = grpcClientService.channel()
        // Create Non Blocking Stub
        commandExecutorServiceGrpc = CommandExecutorServiceGrpc.newFutureStub(channel)

        checkNotNull(commandExecutorServiceGrpc) {
            "failed to create command executor grpc client for selector($selector)"
        }
    }

    override suspend fun prepareEnv(prepareEnvInput: PrepareRemoteEnvInput)
            : RemoteScriptExecutionOutput {
        val grpResponse = commandExecutorServiceGrpc.prepareEnv(prepareEnvInput.asGrpcData()).get()

        checkNotNull(grpResponse.status) {
            "failed to get GRPC prepare env response status for requestId($prepareEnvInput.requestId)"
        }

        val remoteScriptExecutionOutput = grpResponse.asJavaData()
        log.debug("Received prepare env response from command server for requestId($prepareEnvInput.requestId)")

        return remoteScriptExecutionOutput
    }

    override suspend fun executeCommand(remoteExecutionInput: RemoteScriptExecutionInput)
            : RemoteScriptExecutionOutput {

        val grpResponse = commandExecutorServiceGrpc.executeCommand(remoteExecutionInput.asGrpcData()).get()

        checkNotNull(grpResponse.status) {
            "failed to get GRPC response status for requestId($remoteExecutionInput.requestId)"
        }

        val remoteScriptExecutionOutput = grpResponse.asJavaData()
        log.debug("Received response from command server for requestId($remoteExecutionInput.requestId)")

        return remoteScriptExecutionOutput
    }

    override suspend fun close() {
        // TODO('Verify the correct way to close the client conncetion")
        if (channel != null) {
            channel.shutdownNow()
        }
    }


    fun PrepareRemoteEnvInput.asGrpcData(): PrepareEnvInput {
        val correlationId = this.correlationId ?: this.requestId

        return PrepareEnvInput.newBuilder()
                .setRequestId(this.requestId)
                .setCorrelationId(correlationId)
                .setScriptType(ScriptType.valueOf(this.remoteScriptType.name))
                .setTimeOut(this.timeOut.toInt())
                .addAllPackages(this.packages)
                .setProperties(this.properties.asGrpcData())
                .build()
    }

    fun RemoteScriptExecutionInput.asGrpcData(): ExecutionInput {
        val correlationId = this.correlationId ?: this.requestId
        return ExecutionInput.newBuilder()
                .setRequestId(this.requestId)
                .setCorrelationId(correlationId)
                .setIdentifiers(this.remoteIdentifier.asGrpcData())
                .setScriptType(ScriptType.valueOf(this.remoteScriptType.name))
                .setCommand(this.command)
                .setTimeOut(this.timeOut.toInt())
                .setProperties(this.properties.asGrpcData())
                .setTimestamp(Timestamp.getDefaultInstance())
                .build()
    }

    fun RemoteIdentifier?.asGrpcData(): Identifiers? {
        return if (this != null) {
            Identifiers.newBuilder()
                    .setBlueprintName(this.blueprintName)
                    .setBlueprintVersion(this.blueprintVersion)
                    .build()
        } else {
            null
        }
    }

    fun Map<String, JsonNode>.asGrpcData(): Struct {
        val struct = Struct.newBuilder()
        JsonFormat.parser().merge(JacksonUtils.getJson(this), struct)
        return struct.build()
    }

    fun ExecutionOutput.asJavaData(): RemoteScriptExecutionOutput {
        return RemoteScriptExecutionOutput(
                requestId = this.requestId,
                response = this.response,
                status = StatusType.valueOf(this.status.name)
        )
    }

}
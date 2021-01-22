/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

import com.fasterxml.jackson.databind.JsonNode
import com.github.marcoferrer.krotoplus.coroutines.client.ClientBidiCallChannel
import com.github.marcoferrer.krotoplus.coroutines.client.clientCallBidiStreaming
import io.grpc.ManagedChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.GrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.BlueprintGrpcClientService
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.BlueprintGrpcLibPropertyService
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BlueprintProcessingServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

interface StreamingRemoteExecutionService<ReqT, ResT> {

    suspend fun openSubscription(selector: Any, txId: String): Flow<ResT>

    suspend fun sendNonInteractive(selector: Any, txId: String, input: ReqT, timeOutMill: Long): ResT

    suspend fun send(txId: String, input: ReqT)

    suspend fun cancelSubscription(txId: String)

    suspend fun closeChannel(selector: Any)
}

@Service
@ConditionalOnProperty(
    prefix = "blueprintsprocessor.streamingRemoteExecution", name = ["enabled"],
    havingValue = "true", matchIfMissing = false
)
class StreamingRemoteExecutionServiceImpl(private val bluePrintGrpcLibPropertyService: BlueprintGrpcLibPropertyService) :
    StreamingRemoteExecutionService<ExecutionServiceInput, ExecutionServiceOutput> {

    private val log = logger(StreamingRemoteExecutionServiceImpl::class)

    private val grpcChannels: MutableMap<String, ManagedChannel> = hashMapOf()

    private val commChannels: MutableMap<String,
        ClientBidiCallChannel<ExecutionServiceInput, ExecutionServiceOutput>> = hashMapOf()

    /**
     * Open new channel to send and receive for grpc properties [selector] for [txId],
     * Create the only one GRPC channel per host port and reuse for further communication.
     * Create request communication channel to send and receive requests and responses.
     * We can send multiple request with same txId.
     * Consume the flow for responses,
     * Client should cancel the subscription for the request Id once no longer response is needed.
     * */
    @FlowPreview
    override suspend fun openSubscription(selector: Any, txId: String): Flow<ExecutionServiceOutput> {

        if (!commChannels.containsKey(txId)) {
            /** Get GRPC Channel*/
            val grpcChannel = grpcChannel(selector)

            /** Get Send and Receive Channel for bidirectional process method*/
            val channels = clientCallBidiStreaming(BlueprintProcessingServiceGrpc.getProcessMethod(), grpcChannel)
            commChannels[txId] = channels
        }

        val commChannel = commChannels[txId]
            ?: throw BlueprintException("failed to create response subscription for transactionId($txId) channel")

        log.info("created subscription for transactionId($txId)")

        return commChannel.responseChannel.consumeAsFlow()
    }

    /**
     * Send the [input] request, by reusing same GRPC channel and Communication channel
     * for the request Id.
     */
    override suspend fun send(txId: String, input: ExecutionServiceInput) {
        val sendChannel = commChannels[txId]?.requestChannel
            ?: throw BlueprintException("failed to get transactionId($txId) send channel")
        coroutineScope {
            launch {
                sendChannel.send(input)
                log.trace("Message sent for transactionId($txId)")
            }
        }
    }

    /**
     * Simplified version of Streaming calls, Use this API only listing for actual response for [input]
     * for the GRPC [selector] with execution [timeOutMill].
     * Other state of the request will be skipped.
     * The assumption here is you can call this API with same request Id and unique subrequest Id,
     * so the correlation is sub request id to receive the response.
     */
    @ExperimentalCoroutinesApi
    override suspend fun sendNonInteractive(selector: Any, txId: String, input: ExecutionServiceInput, timeOutMill: Long):
        ExecutionServiceOutput {

            var output: ExecutionServiceOutput? = null
            val flow = openSubscription(selector, txId)

            /** Send the request */
            val sendChannel = commChannels[txId]?.requestChannel
                ?: throw BlueprintException("failed to get transactionId($txId) send channel")
            sendChannel.send(input)

            /** Receive the response with timeout */
            withTimeout(timeOutMill) {
                flow.collect {
                    log.trace("Received non-interactive transactionId($txId) response : ${it.status.eventType}")
                    if (it.status.eventType == EventType.EVENT_COMPONENT_EXECUTED) {
                        output = it
                        cancelSubscription(txId)
                    }
                }
            }
            return output!!
        }

    /** Cancel the Subscription for the [txId], This closes communication channel **/
    @ExperimentalCoroutinesApi
    override suspend fun cancelSubscription(txId: String) {
        commChannels[txId]?.let {
            if (!it.requestChannel.isClosedForSend)
                it.requestChannel.close()
            /** If receive channel has to close immediately, once the subscription has cancelled, then enable this */
            // it.responseChannel.cancel(CancellationException("subscription cancelled"))
            commChannels.remove(txId)
            log.info("closed subscription for transactionId($txId)")
        }
    }

    /** Close the GRPC channel for the host port poperties [selector]*/
    override suspend fun closeChannel(selector: Any) {
        val grpcProperties = grpcProperties(selector)
        val selectorName = "${grpcProperties.host}:${grpcProperties.port}"
        if (grpcChannels.containsKey(selectorName)) {
            grpcChannels[selectorName]!!.shutdownNow()
            grpcChannels.remove(selectorName)
            log.info("grpc channel($selectorName) shutdown completed")
        }
    }

    /** Check GRPC channel has been cached and not shutdown, If not re create channel and chache it. */
    private suspend fun grpcChannel(selector: Any): ManagedChannel {
        val grpcProperties = grpcProperties(selector)
        val selectorName = "${grpcProperties.host}:${grpcProperties.port}"
        val isGrpcChannelCached = grpcChannels.containsKey(selectorName)
        val grpcChannel = if (isGrpcChannelCached) {
            if (grpcChannels[selectorName]!!.isShutdown) {
                createGrpcChannel(grpcProperties)
            } else {
                grpcChannels[selectorName]!!
            }
        } else {
            createGrpcChannel(grpcProperties)
        }
        grpcChannels[selectorName] = grpcChannel
        return grpcChannel
    }

    suspend fun createGrpcChannel(grpcProperties: GrpcClientProperties): ManagedChannel {
        val grpcClientService: BlueprintGrpcClientService = bluePrintGrpcLibPropertyService
            .blueprintGrpcClientService(grpcProperties)
        return grpcClientService.channel()
    }

    private fun grpcProperties(selector: Any): GrpcClientProperties {
        return when (selector) {
            is String -> {
                bluePrintGrpcLibPropertyService.grpcClientProperties(selector.toString())
            }
            is JsonNode -> {
                bluePrintGrpcLibPropertyService.grpcClientProperties(selector)
            }
            is GrpcClientProperties -> {
                selector
            }
            else -> {
                throw BlueprintException("couldn't process selector($selector)")
            }
        }
    }
}

/*
 * Copyright © 2017-2019 AT&T, Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core

import com.google.common.collect.ImmutableList
import org.apache.sshd.client.SshClient
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfRpcService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfSession
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.NetconfMessageUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.RpcMessageUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.RpcStatus
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class NetconfSessionImpl(private val deviceInfo: DeviceInfo, private val rpcService: NetconfRpcService,
                         private val sshClientWrapper: NetconfSshClientWrapper) : NetconfSession {

    override fun checkAndReestablish() {
    }

    private val log = LoggerFactory.getLogger(NetconfSessionImpl::class.java)

    private val errorReplies: MutableList<String> = Collections.synchronizedList(mutableListOf())
    private val replies: MutableMap<String, CompletableFuture<String>> = ConcurrentHashMap()
    private val deviceCapabilities = mutableSetOf<String>()
    private var sessionId: String? = null

    private lateinit var streamHandler: NetconfDeviceCommunicator

    private var capabilities =
            ImmutableList.of(RpcMessageUtils.NETCONF_1_0_CAPABILITY, RpcMessageUtils.NETCONF_1_1_CAPABILITY)

    override fun connect() = try {
        log.info("$deviceInfo: Connecting to Netconf Device with timeouts C:${deviceInfo.connectTimeout}, " +
            "R:${deviceInfo.replyTimeout}, I:${deviceInfo.idleTimeout}")
        startConnection()
        setupHandler() //We should have a Netconf SSH connection opened now, setup the handler
        exchangeHelloMessage()
        log.info("$deviceInfo: Connected to Netconf Device")
    } catch (e: NetconfException) {
        log.error("$deviceInfo: Netconf Device Connection Failed. ${e.message}")
        throw NetconfException(e)
    }

    override fun disconnect() {
        if (rpcService.closeSession(false).status.equals(RpcStatus.FAILURE, true)) {
            rpcService.closeSession(true)
        }
        try {
            sshClientWrapper.close()
        } catch (ioe: IOException) {
            log.warn("$deviceInfo: Error closing session($sessionId) for host($deviceInfo)", ioe)
        }
    }

    override fun reconnect() {
        disconnect()
        connect()
    }

    override fun syncRpc(request: String, messageId: String): String {
        val formattedRequest = NetconfMessageUtils.formatRPCRequest(request, messageId, deviceCapabilities)

        if (sshClientWrapper.reestablishNetconfSshSessionIfDisconnected(deviceInfo)) {
            clearReplies()
        }

        try {
            return streamHandler.getFutureFromSendMessage(
                streamHandler.sendMessage(formattedRequest, messageId),
                deviceInfo.replyTimeout.toLong(), TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw NetconfException("$deviceInfo: Interrupted while waiting for reply for request: $formattedRequest", e)
        } catch (e: TimeoutException) {
            throw NetconfException("$deviceInfo: Timed out while waiting for reply for request $formattedRequest after ${deviceInfo.replyTimeout} sec.", e)
        } catch (e: ExecutionException) {
            log.warn("$deviceInfo: Closing session($sessionId) due to unexpected Error", e)
            try {
                sshClientWrapper.close()
            } catch (ioe: IOException) {
                log.warn("$deviceInfo: Error closing session($sessionId) for host($deviceInfo)", ioe)
            }
            clearErrorReplies()
            clearReplies()

            throw NetconfException("$deviceInfo: Closing session $sessionId for request $formattedRequest", e)
        }
        //TODO: should CancellationException be handled ?
    }

    override fun asyncRpc(request: String, messageId: String): CompletableFuture<String> {
        val formattedRequest = NetconfMessageUtils.formatRPCRequest(request, messageId, deviceCapabilities)

        if (sshClientWrapper.reestablishNetconfSshSessionIfDisconnected(deviceInfo)) {
            clearReplies()
            //TODO: Does error replies need to be cleared??
        }

        return streamHandler.sendMessage(formattedRequest, messageId)
            .handleAsync { reply, t ->
                when {
                    t != null -> throw NetconfException(messageId, t) //TODO: how to test this case?
                    else -> reply
                }
            }
    }

    override fun getDeviceInfo(): DeviceInfo {
        return deviceInfo
    }

    override fun getSessionId(): String {
        return this.sessionId!!
    }

    override fun getDeviceCapabilitiesSet(): Set<String> {
        return Collections.unmodifiableSet(deviceCapabilities)
    }

    private fun startConnection() = try {
        sshClientWrapper.setSshClient(SshClient.setUpDefaultClient())
        sshClientWrapper.startClient(deviceInfo)
    } catch (e: Exception) {
        throw NetconfException("$deviceInfo: Failed to establish SSH session", e)
    }

    private fun exchangeHelloMessage() {
        sessionId = "-1"
        val messageId = "-1"

        val serverHelloResponse = syncRpc(NetconfMessageUtils.createHelloString(capabilities), messageId)

        sessionId = NetconfMessageUtils.extractNetconfSessionIdFromHelloResponse(serverHelloResponse)
            ?: throw NetconfException("$deviceInfo: Missing sessionId in server hello message: $serverHelloResponse")
        val capabilityMatcher = NetconfMessageUtils.CAPABILITY_REGEX_PATTERN.matcher(serverHelloResponse)
        var numCapabilities = 0
        while (capabilityMatcher.find()) {
            val foundCapability = capabilityMatcher.group(1)
            deviceCapabilities.add(foundCapability)
            numCapabilities++
        }
        log.info("$deviceInfo sent $numCapabilities capabilities.")
    }

    private fun setupHandler() {
        streamHandler = NetconfDeviceCommunicator(
            sshClientWrapper.getInputStreamReader(),
                sshClientWrapper.getOutputStreamWriter(),
            deviceInfo,
            NetconfSessionListenerImpl(this), //TODO this is still a bit awkward.
            replies)
    }

    internal fun setStreamHandler(streamHandler: NetconfDeviceCommunicator) {
        this.streamHandler = streamHandler
    }

    /**
     * Add an error reply
     * Used by {@link NetconfSessionListenerImpl}
     */
    internal fun addDeviceErrorReply(errReply: String) {
        println("addDeviceErrorReply (errReply: $errReply") //TODO : get rid of this.
        errorReplies.add(errReply)
    }

    /**
     * Add a reply from the device
     * Used by {@link NetconfSessionListenerImpl}
     */
    internal fun addDeviceReply(messageId: String, replyMsg: String) {
        println("addDeviceReply (messageId: $messageId replyMsg: $replyMsg") //TODO : get rid of this.
        replies[messageId]?.complete(replyMsg)
    }

    /**
     * Internal function for accessing replies for testing.
     */
    internal fun getReplies() = replies

    /**
     * internal function for accessing errorReplies for testing.
     */
    internal fun getErrorReplies() = errorReplies
    internal fun clearErrorReplies() = errorReplies.clear()
    internal fun clearReplies() = replies.clear()


    //TODO: get rid of this
    fun sessionstatus(state: String): Boolean {
        return when (state) {
            "Close" -> sshClientWrapper.getClientChannel().isClosed
            "Open" -> sshClientWrapper.getClientChannel().isOpen
            else -> false
        }
    }
}
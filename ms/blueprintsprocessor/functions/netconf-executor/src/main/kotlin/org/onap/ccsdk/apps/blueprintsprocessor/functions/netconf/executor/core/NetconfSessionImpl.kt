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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.core

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.FactoryManager
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api.NetconfReceivedEvent
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api.NetconfRpcService
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api.NetconfSession
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api.NetconfSessionListener
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.NetconfMessageUtils
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.RpcMessageUtils
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.RpcStatus
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class NetconfSessionImpl(private val deviceInfo: DeviceInfo, private val rpcService: NetconfRpcService) :
    NetconfSession {

    private val log = LoggerFactory.getLogger(NetconfSessionImpl::class.java)

    private val errorReplies: MutableList<String> = Collections.synchronizedList(listOf())
    private val replies: MutableMap<String, CompletableFuture<String>> = ConcurrentHashMap()
    private val deviceCapabilities = setOf<String>()

    private var connectionTimeout: Long = 0
    private var replyTimeout: Int = 0
    private var idleTimeout: Int = 0
    private var sessionId: String? = null

    private lateinit var session: ClientSession
    private lateinit var client: SshClient
    private lateinit var channel: ClientChannel
    private lateinit var streamHandler: NetconfDeviceCommunicator

    private var capabilities =
        ImmutableList.of(RpcMessageUtils.NETCONF_10_CAPABILITY, RpcMessageUtils.NETCONF_11_CAPABILITY)

    override fun connect() {
        try {
            log.info("$deviceInfo: Connecting to Netconf Device with timeouts C:${deviceInfo.connectTimeout}, " +
                    "R:${deviceInfo.replyTimeout}, I:${deviceInfo.idleTimeout}")
            startConnection()
            log.info("$deviceInfo: Connected to Netconf Device")
        } catch (e: NetconfException) {
            log.error("$deviceInfo: Netconf Device Connection Failed. ${e.message}")
            throw NetconfException(e)
        }
    }

    override fun disconnect() {
        if (rpcService.closeSession(false).status.equals(
                RpcStatus.FAILURE, true)) {
            rpcService.closeSession(true)
        }

        session.close()
        // Closes the socket which should interrupt the streamHandler
        channel.close()
        client.close()
    }

    override fun reconnect() {
        disconnect()
        connect()
    }

    override fun syncRpc(request: String, messageId: String): String {
        val formattedRequest = NetconfMessageUtils.formatRPCRequest(request, messageId, deviceCapabilities)

        checkAndReestablish()

        try {
            return streamHandler.sendMessage(formattedRequest, messageId).get(replyTimeout.toLong(), TimeUnit.SECONDS)
//            replies.remove(messageId)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw NetconfException("$deviceInfo: Interrupted while waiting for reply for request: $formattedRequest", e)
        } catch (e: TimeoutException) {
            throw NetconfException("$deviceInfo: Timed out while waiting for reply for request $formattedRequest after $replyTimeout sec.",
                e)
        } catch (e: ExecutionException) {
            log.warn("$deviceInfo: Closing session($sessionId) due to unexpected Error", e)
            try {
                session.close()
                // Closes the socket which should interrupt the streamHandler
                channel.close()
                client.close()
            } catch (ioe: IOException) {
                log.warn("$deviceInfo: Error closing session($sessionId) for host($deviceInfo)", ioe)
            }

//            NetconfReceivedEvent(NetconfReceivedEvent.Type.SESSION_CLOSED, "",
//                "Closed due to unexpected error " + e.cause, "-1", deviceInfo)
            errorReplies.clear() // move to cleanUp()?
            replies.clear()

            throw NetconfException("$deviceInfo: Closing session $sessionId for request $formattedRequest", e)
        }
    }

    override fun asyncRpc(request: String, messageId: String): CompletableFuture<String> {
        val formattedRequest = NetconfMessageUtils.formatRPCRequest(request, messageId, deviceCapabilities)

        checkAndReestablish()

        return streamHandler.sendMessage(formattedRequest, messageId).handleAsync { reply, t ->
            if (t != null) {
                throw NetconfException(messageId, t)
            }
            reply
        }
    }

    override fun checkAndReestablish() {
        try {
            if (client.isClosed) {
                log.info("Trying to restart the whole SSH connection with {}", deviceInfo)
                replies.clear()
                startConnection()
            } else if (session.isClosed) {
                log.info("Trying to restart the session with {}", deviceInfo)
                replies.clear()
                startSession()
            } else if (channel.isClosed) {
                log.info("Trying to reopen the channel with {}", deviceInfo)
                replies.clear()
                openChannel()
            } else {
                return
            }
        } catch (e: IOException) {
            log.error("Can't reopen connection for device {}", e.message)
            throw NetconfException(String.format("Cannot re-open the connection with device (%s)", deviceInfo), e)
        } catch (e: IllegalStateException) {
            log.error("Can't reopen connection for device {}", e.message)
            throw NetconfException(String.format("Cannot re-open the connection with device (%s)", deviceInfo), e)
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

    private fun startConnection() {
        connectionTimeout = deviceInfo.connectTimeout
        replyTimeout = deviceInfo.replyTimeout
        idleTimeout = deviceInfo.idleTimeout
        try {
            startClient()
        } catch (e: Exception) {
            throw NetconfException("$deviceInfo: Failed to establish SSH session", e)
        }

    }

    private fun startClient() {
        client = SshClient.setUpDefaultClient()
        client.properties.putIfAbsent(FactoryManager.IDLE_TIMEOUT, TimeUnit.SECONDS.toMillis(idleTimeout.toLong()))
        client.properties.putIfAbsent(FactoryManager.NIO2_READ_TIMEOUT, TimeUnit.SECONDS.toMillis(idleTimeout + 15L))
        client.keyPairProvider = SimpleGeneratorHostKeyProvider()
        client.start()

        startSession()
    }

    private fun startSession() {
        log.info("$deviceInfo: Starting SSH session")
        val connectFuture = client.connect(deviceInfo.username, deviceInfo.ipAddress, deviceInfo.port)
            .verify(connectionTimeout, TimeUnit.SECONDS)
        session = connectFuture.session
        log.info("$deviceInfo: SSH session created")

        authSession()
    }

    private fun authSession() {
        session.addPasswordIdentity(deviceInfo.password)
        session.auth().verify(connectionTimeout, TimeUnit.SECONDS)
        val event = session.waitFor(ImmutableSet.of(ClientSession.ClientSessionEvent.WAIT_AUTH,
            ClientSession.ClientSessionEvent.CLOSED, ClientSession.ClientSessionEvent.AUTHED), 0)
        if (!event.contains(ClientSession.ClientSessionEvent.AUTHED)) {
            throw NetconfException("$deviceInfo: Failed to authenticate session.")
        }
        log.info("$deviceInfo: SSH session authenticated")

        openChannel()
    }

    private fun openChannel() {
        channel = session.createSubsystemChannel("netconf")
        val channelFuture = channel.open()
        if (channelFuture.await(connectionTimeout, TimeUnit.SECONDS) && channelFuture.isOpened) {
            log.info("$deviceInfo: SSH NETCONF subsystem channel opened")
            setupHandler()
        } else {
            throw NetconfException("$deviceInfo: Failed to open SSH subsystem channel")
        }
    }

    private fun setupHandler() {
        val sessionListener: NetconfSessionListener = NetconfSessionListenerImpl()
        streamHandler = NetconfDeviceCommunicator(channel.invertedOut, channel.invertedIn, deviceInfo,
            sessionListener, replies)

        exchangeHelloMessage()
    }

    private fun exchangeHelloMessage() {
        sessionId = "-1"
        val messageId = "-1"

        val serverHelloResponse = syncRpc(NetconfMessageUtils.createHelloString(capabilities), messageId)
        val sessionIDMatcher = NetconfMessageUtils.SESSION_ID_REGEX_PATTERN.matcher(serverHelloResponse)

        if (sessionIDMatcher.find()) {
            sessionId = sessionIDMatcher.group(1)
        } else {
            throw NetconfException("$deviceInfo: Missing sessionId in server hello message: $serverHelloResponse")
        }

        val capabilityMatcher = NetconfMessageUtils.CAPABILITY_REGEX_PATTERN.matcher(serverHelloResponse)
        while (capabilityMatcher.find()) {
            deviceCapabilities.plus(capabilityMatcher.group(1))
        }
    }

    inner class NetconfSessionListenerImpl : NetconfSessionListener {
        override fun notify(event: NetconfReceivedEvent) {
            val messageId = event.getMessageID()

            when (event.getType()) {
                NetconfReceivedEvent.Type.DEVICE_UNREGISTERED -> disconnect()
                NetconfReceivedEvent.Type.DEVICE_ERROR -> errorReplies.add(event.getMessagePayload())
                NetconfReceivedEvent.Type.DEVICE_REPLY -> replies[messageId]?.complete(event.getMessagePayload())
                NetconfReceivedEvent.Type.SESSION_CLOSED -> disconnect()
            }
        }
    }
}
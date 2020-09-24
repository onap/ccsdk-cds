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
import com.google.common.collect.ImmutableSet
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.FactoryManager
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfRpcService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfSession
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfSessionListener
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

class NetconfSessionImpl(private val deviceInfo: DeviceInfo, private val rpcService: NetconfRpcService) :
    NetconfSession {

    private val log = LoggerFactory.getLogger(NetconfSessionImpl::class.java)

    private val errorReplies: MutableList<String> = Collections.synchronizedList(mutableListOf())
    private val replies: MutableMap<String, CompletableFuture<String>> = ConcurrentHashMap()
    private val deviceCapabilities = mutableSetOf<String>()

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
            log.info(
                "$deviceInfo: Connecting to Netconf Device with timeouts C:${deviceInfo.connectTimeout}, " +
                    "R:${deviceInfo.replyTimeout}, I:${deviceInfo.idleTimeout}"
            )
            startConnection()
            log.info("$deviceInfo: Connected to Netconf Device")
        } catch (e: NetconfException) {
            log.error("$deviceInfo: Netconf Device Connection Failed. ${e.message}")
            throw NetconfException(e)
        }
    }

    override fun disconnect() {
        var retryNum = 3
        while (rpcService.closeSession(false).status
            .equals(RpcStatus.FAILURE, true) && retryNum > 0
        ) {
            log.error("disconnect: graceful disconnect failed, retrying $retryNum times...")
            retryNum--
        }
        // if we can't close the session, try to force terminate.
        if (retryNum == 0) {
            log.error("disconnect: trying to force-terminate the session.")
            rpcService.closeSession(true)
        }
        try {
            close()
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

        checkAndReestablish()

        try {
            return streamHandler.getFutureFromSendMessage(
                streamHandler.sendMessage(formattedRequest, messageId),
                replyTimeout.toLong(), TimeUnit.SECONDS
            )
        } catch (e: InterruptedException) {
            throw NetconfException("$deviceInfo: Interrupted while waiting for reply for request: $formattedRequest", e)
        } catch (e: TimeoutException) {
            throw NetconfException(
                "$deviceInfo: Timed out while waiting for reply for request $formattedRequest after $replyTimeout sec.",
                e
            )
        } catch (e: ExecutionException) {
            log.warn("$deviceInfo: Closing session($sessionId) due to unexpected Error", e)
            try {
                close()
            } catch (ioe: IOException) {
                log.warn("$deviceInfo: Error closing session($sessionId) for host($deviceInfo)", ioe)
            }
            clearErrorReplies()
            clearReplies()

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
            when {
                client.isClosed -> {
                    log.info("Trying to restart the whole SSH connection with {}", deviceInfo)
                    clearReplies()
                    startConnection()
                }
                session.isClosed -> {
                    log.info("Trying to restart the session with {}", deviceInfo)
                    clearReplies()
                    startSession()
                }
                channel.isClosed -> {
                    log.info("Trying to reopen the channel with {}", deviceInfo)
                    clearReplies()
                    openChannel()
                }
                else -> return
            }
        } catch (e: IOException) {
            log.error("Can't reopen connection for device {} error: {}", deviceInfo, e.message)
            throw NetconfException(String.format("Cannot re-open the connection with device (%s)", deviceInfo), e)
        } catch (e: IllegalStateException) {
            log.error("Can't reopen connection for device {} error: {}", deviceInfo, e.message)
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

    // Needed to unit test connect method interacting with client.start in startClient() below
    private fun setupNewSSHClient() {
        client = SshClient.setUpDefaultClient()
    }

    private fun startClient() {
        setupNewSSHClient()

        client.properties.putIfAbsent(FactoryManager.IDLE_TIMEOUT, TimeUnit.SECONDS.toMillis(idleTimeout.toLong()))
        client.properties.putIfAbsent(FactoryManager.NIO2_READ_TIMEOUT, TimeUnit.SECONDS.toMillis(idleTimeout + 15L))
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
        val event = session.waitFor(
            ImmutableSet.of(
                ClientSession.ClientSessionEvent.WAIT_AUTH,
                ClientSession.ClientSessionEvent.CLOSED, ClientSession.ClientSessionEvent.AUTHED
            ),
            0
        )
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
        val sessionListener: NetconfSessionListener = NetconfSessionListenerImpl(this)
        streamHandler = NetconfDeviceCommunicator(
            channel.invertedOut, channel.invertedIn, deviceInfo,
            sessionListener, replies
        )

        exchangeHelloMessage()
    }

    private fun exchangeHelloMessage() {
        sessionId = "-1"
        val messageId = "-1"

        val serverHelloResponse = syncRpc(NetconfMessageUtils.createHelloString(capabilities), messageId)
        val sessionIDMatcher = NetconfMessageUtils.SESSION_ID_REGEX_PATTERN.matcher(serverHelloResponse)

        if (sessionIDMatcher.find()) {
            sessionId = sessionIDMatcher.group(1)
            log.info("netconf exchangeHelloMessage sessionID: $sessionId")
        } else {
            throw NetconfException("$deviceInfo: Missing sessionId in server hello message: $serverHelloResponse")
        }

        val capabilityMatcher = NetconfMessageUtils.CAPABILITY_REGEX_PATTERN.matcher(serverHelloResponse)
        while (capabilityMatcher.find()) { // TODO: refactor to add unit test easily for device capability accumulation.
            deviceCapabilities.add(capabilityMatcher.group(1))
        }
    }

    internal fun setStreamHandler(streamHandler: NetconfDeviceCommunicator) {
        this.streamHandler = streamHandler
    }

    /**
     * Add an error reply
     * Used by {@link NetconfSessionListenerImpl}
     */
    internal fun addDeviceErrorReply(errReply: String) {
        errorReplies.add(errReply)
    }

    /**
     * Add a reply from the device
     * Used by {@link NetconfSessionListenerImpl}
     */
    internal fun addDeviceReply(messageId: String, replyMsg: String) {
        replies[messageId]?.complete(replyMsg)
    }

    /**
     * Closes the session/channel/client
     */
    @Throws(IOException::class)
    private fun close() {
        log.debug("close was called.")
        session.close()
        // Closes the socket which should interrupt the streamHandler
        channel.close()
        client.close()
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
    internal fun setClient(client: SshClient) {
        this.client = client
    }

    internal fun setSession(session: ClientSession) {
        this.session = session
    }

    internal fun setChannel(channel: ClientChannel) {
        this.channel = channel
    }
}

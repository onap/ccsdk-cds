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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.core

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import org.apache.sshd.client.ClientBuilder
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.client.simple.SimpleClient
import org.apache.sshd.common.FactoryManager
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.NetconfException
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.data.NetconfDeviceOutputEvent
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.DeviceInfo
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.NetconfSession
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.NetconfSessionDelegate
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.RpcConstants
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.RpcMessageUtils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger


class NetconfSessionImpl(private val deviceInfo: DeviceInfo ): NetconfSession  {
    val log = LoggerFactory.getLogger(NetconfSessionImpl::class.java)
    var connectTimeout: Long = 0
    var replyTimeout: Int = 0
    var idleTimeout: Int = 0
    var sessionID: String? = null
    var errorReplies: MutableList<String> = mutableListOf()
    var netconfCapabilities = ImmutableList.of("urn:ietf:params:netconf:base:1.0", "urn:ietf:params:netconf:base:1.1")

   // var replies: MutableMap<String, CompletableFuture<String>> = mutableListOf<String,CompletableFuture<String>()>()
    var replies: MutableMap<String, CompletableFuture<String>> = ConcurrentHashMap()
    val deviceCapabilities = LinkedHashSet<String>()

    lateinit var session: ClientSession
    lateinit var client: SshClient
    lateinit var channel: ClientChannel
    var streamHandler: NetconfStreamThread? = null

    val messageIdInteger = AtomicInteger(1)
    private var onosCapabilities = ImmutableList.of<String>(RpcConstants.NETCONF_10_CAPABILITY, RpcConstants.NETCONF_11_CAPABILITY)


    init {
          startConnection()
    }

    private fun startConnection() {
        connectTimeout = deviceInfo.connectTimeoutSec
        replyTimeout = deviceInfo.replyTimeout
        idleTimeout = deviceInfo.idleTimeout
        log.info("Connecting to NETCONF Device {} with timeouts C:{}, R:{}, I:{}", deviceInfo, connectTimeout,
                replyTimeout, idleTimeout)
        try {
            startClient()
        } catch (e: IOException) {
            throw NetconfException("Failed to establish SSH with device ${deviceInfo.deviceId}",e)
        } catch (e:Exception){
            throw NetconfException("Failed to establish SSH with device $deviceInfo",e)
        }

    }

    private fun startClient() {
        log.info("in the startClient")
        // client = SshClient.setUpDefaultClient().toInt()
        client = SshClient.setUpDefaultClient()

        client = ClientBuilder.builder().build() as SshClient
        log.info("client {}>>",client)
        client.getProperties().putIfAbsent(FactoryManager.IDLE_TIMEOUT, TimeUnit.SECONDS.toMillis(idleTimeout.toLong()))
        client.getProperties().putIfAbsent(FactoryManager.NIO2_READ_TIMEOUT,
                TimeUnit.SECONDS.toMillis(idleTimeout + 15L))
        client.start()
        client.setKeyPairProvider(SimpleGeneratorHostKeyProvider())
        log.info("client {}>>",client.isOpen)
        startSession()
    }

    private fun startSession() {
        log.info("in the startSession")
        val connectFuture = client.connect(deviceInfo.name, deviceInfo.ipAddress, deviceInfo.port)
                .verify(connectTimeout, TimeUnit.SECONDS)
        log.info("connectFuture {}>>"+connectFuture)
        session = connectFuture.session

        session.addPasswordIdentity(deviceInfo.pass)
        session.auth().verify(connectTimeout, TimeUnit.SECONDS)

        val event = session.waitFor(ImmutableSet.of(ClientSession.ClientSessionEvent.WAIT_AUTH,
                ClientSession.ClientSessionEvent.CLOSED, ClientSession.ClientSessionEvent.AUTHED), 0)

        if (!event.contains(ClientSession.ClientSessionEvent.AUTHED)) {
            log.debug("Session closed {} for event {}", session.isClosed(), event)
            throw NetconfException(String
                    .format("Failed to authenticate session with device (%s) check the user/pwd or key", deviceInfo))
        }
        openChannel()
    }

    private fun openChannel() {
        log.info("in the open Channel")
        channel = session.createSubsystemChannel("netconf")
        val channeuture = channel.open()

        if (channeuture!!.await(connectTimeout, TimeUnit.SECONDS) && channeuture.isOpened) {
           val netconfSessionDelegate:NetconfSessionDelegate = NetconfSessionDelegateImpl()
            streamHandler = NetconfStreamThread(channel.getInvertedOut(), channel.getInvertedIn(), deviceInfo,
                    netconfSessionDelegate, replies)
            sendHello()
        } else {
            throw NetconfException(String.format("Failed to open channel with device (%s) $deviceInfo", deviceInfo))
        }
    }

    private fun sendHello() {
        sessionID = (-1).toString()

        val serverHelloResponse = syncRpc(RpcMessageUtils.createHelloString(onosCapabilities), (-1).toString())
        val sessionIDMatcher = RpcMessageUtils.SESSION_ID_REGEX_PATTERN.matcher(serverHelloResponse)

        if (sessionIDMatcher.find()) {
            sessionID = sessionIDMatcher.group(1)
        } else {
            throw NetconfException("Missing SessionID in server hello reponse.")
        }

        val capabilityMatcher = RpcMessageUtils.CAPABILITY_REGEX_PATTERN.matcher(serverHelloResponse)
        while (capabilityMatcher.find()) {
            deviceCapabilities.add(capabilityMatcher.group(1))
        }
    }


    override fun asyncRpc( request: String, msgId: String): CompletableFuture<String> {
        //return close(false);
       var  request = RpcMessageUtils.formatRPCRequest(request, msgId, deviceCapabilities)
        /**
         * Checking Liveliness of the Session
         */
        checkAndReestablish()

        return streamHandler!!.sendMessage(request, msgId).handleAsync { reply, t ->
            if (t != null) {
                //throw NetconfTransportException(t)
                throw NetconfException(msgId)
            }
            reply
        }
    }

    override fun close(): Boolean {
        return close(false);
    }
    @Throws(NetconfException::class)
    private fun close(force: Boolean): Boolean {
        val rpc = StringBuilder()
        rpc.append("<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">")
        if (force) {
            rpc.append("<kill-session/>")
        } else {
            rpc.append("<close-session/>")
        }
        rpc.append("</rpc>")
        rpc.append(RpcConstants.END_PATTERN)
        return RpcMessageUtils.checkReply(sendRequest(rpc.toString())) || close(true)
    }



    override fun getSessionId(): String? {
          return this.sessionID
    }

    override fun getDeviceCapabilitiesSet(): Set<String> {
        return Collections.unmodifiableSet(deviceCapabilities);
    }

    fun setCapabilities(capabilities: ImmutableList<String>) {
        onosCapabilities = capabilities
    }

    override fun checkAndReestablish() {
        try {
            if (client.isClosed) {
                log.debug("Trying to restart the whole SSH connection with {}", deviceInfo.deviceId)
                replies.clear()
                startConnection()
            } else if (session.isClosed) {
                log.debug("Trying to restart the session with {}", deviceInfo.deviceId)
                replies.clear()
                startSession()
            } else if (channel.isClosed) {
                log.debug("Trying to reopen the channel with {}", deviceInfo.deviceId)
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

    override fun setCapabilities(capabilities: List<String>) {
        super.setCapabilities(capabilities)
    }

    override fun getDeviceInfo(): DeviceInfo {
        return deviceInfo
    }

    @Throws(NetconfException::class)
    private fun sendRequest(request: String): String {
        return syncRpc(request, messageIdInteger.getAndIncrement().toString())
    }

    @Throws(NetconfException::class)
    override fun syncRpc(request: String, messageId: String): String {
        var request = request
        request = RpcMessageUtils.formatRPCRequest(request, messageId, deviceCapabilities)

        /**
         * Checking Liveliness of the Session
         */
        checkAndReestablish()

        val response: String
        try {
            response = streamHandler!!.sendMessage(request, messageId).get(replyTimeout.toLong(), TimeUnit.SECONDS)
            replies.remove(messageId) // Why here???
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw NetconfException("Interrupted waiting for reply for request$request",e)
        } catch (e: TimeoutException) {
            throw NetconfException(
                    "Timed out waiting for reply for request $request after $replyTimeout sec.",e)
        } catch (e: ExecutionException) {
            log.warn("Closing session {} for {} due to unexpected Error", sessionID, deviceInfo, e)
            try {
                session.close()
                channel.close() // Closes the socket which should interrupt NetconfStreamThread
                client.close()
            } catch (ioe: IOException) {
                log.warn("Error closing session {} on {}", sessionID, deviceInfo, ioe)
            }

            NetconfDeviceOutputEvent(NetconfDeviceOutputEvent.Type.SESSION_CLOSED, null!!,
                    "Closed due to unexpected error " + e.cause, Optional.of("-1"), deviceInfo)
            errorReplies.clear() // move to cleanUp()?
            replies.clear()

            throw NetconfException(
                    "Closing session $sessionID for $deviceInfo for request $request",e)
        }

        log.debug("Response from NETCONF Device: \n {} \n", response)
        return response.trim { it <= ' ' }
    }

    inner class NetconfSessionDelegateImpl : NetconfSessionDelegate {
        override fun notify(event: NetconfDeviceOutputEvent) {
            val messageId = event.getMessageID()
            log.debug("messageID {}, waiting replies messageIDs {}", messageId, replies.keys)
            if (messageId.isNullOrBlank()) {
                errorReplies.add(event.getMessagePayload().toString())
                log.error("Device {} sent error reply {}", event.getDeviceInfo(), event.getMessagePayload())
                return
            }
            val completedReply = replies[messageId] // remove(..)?
            completedReply?.complete(event.getMessagePayload())
        }
    }
    }
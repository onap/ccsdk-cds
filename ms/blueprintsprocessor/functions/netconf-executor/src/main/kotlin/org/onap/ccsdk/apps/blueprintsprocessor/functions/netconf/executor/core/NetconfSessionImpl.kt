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
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.FactoryManager
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.data.NetconfException
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.DeviceInfo
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.NetconfSession
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class NetconfSessionImpl(val deviceInfo: DeviceInfo): NetconfSession  {
    val log = LoggerFactory.getLogger(NetconfSessionImpl::class.java)
    var connectTimeout: Long = 0
    var replyTimeout: Int = 0
    var idleTimeout: Int = 0
    var sessionID: String? = null
    var errorReplies: MutableList<String> = mutableListOf()
    var netconfCapabilities = ImmutableList.of("urn:ietf:params:netconf:base:1.0", "urn:ietf:params:netconf:base:1.1")

   // var replies: MutableMap<String, CompletableFuture<String>> = mutableListOf<String,CompletableFuture<String>()>()
    var replies: Map<String, CompletableFuture<String>> = ConcurrentHashMap()
    val deviceCapabilities = LinkedHashSet<String>()

    lateinit var session: ClientSession
    lateinit var client: SshClient
    lateinit var channel: ClientChannel
    //var streamHandler: NetconfStreamHandler? = null

    val messageIdInteger = AtomicInteger(1)

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
            throw NetconfException("Failed to establish SSH with device $deviceInfo")
        }

    }

    private fun startClient() {
        //client = SshClient.setUpDefaultClient().toInt()
        client = SshClient()
        client.getProperties().putIfAbsent(FactoryManager.IDLE_TIMEOUT, TimeUnit.SECONDS.toMillis(idleTimeout.toLong()))
        client.getProperties().putIfAbsent(FactoryManager.NIO2_READ_TIMEOUT,
                TimeUnit.SECONDS.toMillis(idleTimeout + 15L))
        client.start()
        client.setKeyPairProvider(SimpleGeneratorHostKeyProvider())
        startSession()
    }

    private fun startSession() {
        val connectFuture = client.connect(deviceInfo.name, deviceInfo.ipAddress, deviceInfo.port)
                .verify(connectTimeout, TimeUnit.SECONDS)

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
        channel = session.createSubsystemChannel("netconf")
        val channeuture = channel.open()

        if (channeuture!!.await(connectTimeout, TimeUnit.SECONDS) && channeuture.isOpened) {
           // streamHandler = NetconfStreamThread(channel.getInvertedOut(), channel.getInvertedIn(), deviceInfo,
           //         NetconfSessionDelegateImpl(), replies)
           // sendHello()
        } else {
            throw NetconfException(String.format("Failed to open channel with device (%s)", deviceInfo))
        }
    }

    private fun sendHello() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun asyncRpc(request: String, msgId: String): CompletableFuture<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSessionId(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDeviceCapabilitiesSet(): Set<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun checkAndReestablish() {
        super.checkAndReestablish()
    }

    override fun setCapabilities(capabilities: List<String>) {
        super.setCapabilities(capabilities)
    }
}
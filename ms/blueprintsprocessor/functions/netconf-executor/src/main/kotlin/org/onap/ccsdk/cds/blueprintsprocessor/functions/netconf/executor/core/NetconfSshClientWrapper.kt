/*
 *  Copyright (C) 2019 Amdocs, Bell Canada
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core

import com.google.common.collect.ImmutableSet
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.FactoryManager
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfSessionListener
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Class to wrap the SSH client, store the session and channel objects
 * Used by NetconfSessinoImpl
 */
class NetconfSshClientWrapper {

    companion object {
        const val NETCONF_SSH_SUBSYSTEM = "netconf"
    }

    private val log = LoggerFactory.getLogger(NetconfSshClientWrapper::class.java)

    private lateinit var session: ClientSession
    private lateinit var client: SshClient
    private lateinit var channel: ClientChannel


    /**
     * Get the SSH Session
     */
    fun getSession(): ClientSession {
        return session
    }

    /**
     * Get SSH client object
     */
    fun getSshClient(): SshClient {
        return client
    }

    /**
     * Get SSH Client channel
     * @returns {@link ClientChannel}
     */
    fun getClientChannel(): ClientChannel {
        return channel
    }

    /**
     * @return {@link InputStream} from the client channel's {@link #ClientChannel.invertedOut}
     */
    fun getClientChannelInputStream(): InputStream {
        return channel.invertedOut
    }

    /**
     * @return {@link OutputStream} from the client channel's {@link #ClientChannel.invertedIn}
     */
    fun getClientChannelOutputStream(): OutputStream {
        return channel.invertedIn
    }

    fun startClient(deviceInfo: DeviceInfo) {
        client = SshClient.setUpDefaultClient()
        client.properties.putIfAbsent(FactoryManager.IDLE_TIMEOUT, TimeUnit.SECONDS.toMillis(deviceInfo.idleTimeout.toLong()))
        client.properties.putIfAbsent(FactoryManager.NIO2_READ_TIMEOUT, TimeUnit.SECONDS.toMillis(deviceInfo.idleTimeout + 15L))
        client.keyPairProvider = SimpleGeneratorHostKeyProvider()
        client.start()

        startSession(deviceInfo)
    }

    private fun startSession(deviceInfo: DeviceInfo) {
        log.info("$deviceInfo: Starting SSH session")
        val connectFuture = client.connect(deviceInfo.username, deviceInfo.ipAddress, deviceInfo.port)
                .verify(deviceInfo.connectTimeout, TimeUnit.SECONDS)
        session = connectFuture.session
        log.info("$deviceInfo: SSH session created")

        authSession(deviceInfo)
    }

    private fun authSession(deviceInfo: DeviceInfo) {
        session.addPasswordIdentity(deviceInfo.password)
        session.auth().verify(deviceInfo.connectTimeout, TimeUnit.SECONDS)
        val event = session.waitFor(ImmutableSet.of(
                ClientSession.ClientSessionEvent.WAIT_AUTH,
                ClientSession.ClientSessionEvent.CLOSED,
                ClientSession.ClientSessionEvent.AUTHED), 0)

        if (!event.contains(ClientSession.ClientSessionEvent.AUTHED)) {
            throw NetconfException("$deviceInfo: Failure during Netconf SSH session authentication")
        }
        log.info("$deviceInfo: Netconf SSH session successfully authenticated")

        openChannel(deviceInfo)
    }

    private fun openChannel(deviceInfo: DeviceInfo) {
        channel = session.createSubsystemChannel(NETCONF_SSH_SUBSYSTEM)
        val channelFuture = channel.open()
        if (channelFuture.await(deviceInfo.connectTimeout, TimeUnit.SECONDS) && channelFuture.isOpened) {
            log.info("$deviceInfo: Netconf SSH subsystem channel opened")
            //we're done for now... back to NetconfSessionImpl
        } else {
            throw NetconfException("$deviceInfo: Failed to open SSH subsystem channel")
        }
    }

    /**
     * Closes the session/channel/client
     */
    @Throws(IOException::class)
    fun close() {
        session.close()
        // Closes the socket which should interrupt the streamHandler
        channel.close()
        client.close()
    }
    /**
     * Checks the state of the underlying SSH session and connection and if necessary it reestablishes
     * it.
     * @return whether part of connection was re-established. The implication on the caller is that
     * the replies map has to get cleared.
     */
    @Throws(NetconfException::class)
    fun reestablishNetconfSshSessionIfDisconnected(deviceInfo: DeviceInfo): Boolean  {
        try {
            return when {
                client.isClosed -> {
                    log.info("Netconf SSH connection to {} is down, reconnecting ", deviceInfo)
                    startClient(deviceInfo)
                    true
                }
                session.isClosed -> {
                    log.info("Trying to restart the session with {}", deviceInfo)
                    startSession(deviceInfo)
                    true
                }
                channel.isClosed -> {
                    log.info("Trying to reopen the channel with {}", deviceInfo)
                    openChannel(deviceInfo)
                    true
                }
                else -> false
            }
        } catch (e: IOException) {
            log.error("Can't reopen connection for device {}", e.message)
            throw NetconfException(String.format("Cannot re-open the connection with device (%s)", deviceInfo), e)
        } catch (e: IllegalStateException) {
            log.error("Can't reopen connection for device {}", e.message)
            throw NetconfException(String.format("Cannot re-open the connection with device (%s)", deviceInfo), e)
        }
    }

}
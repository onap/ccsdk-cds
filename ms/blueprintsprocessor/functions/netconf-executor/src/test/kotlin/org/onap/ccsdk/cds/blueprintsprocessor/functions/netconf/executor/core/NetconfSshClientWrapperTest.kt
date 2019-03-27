/*
 * Copyright © 2019 Bell Canada
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

import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ChannelSubsystem
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.future.DefaultAuthFuture
import org.apache.sshd.client.future.DefaultConnectFuture
import org.apache.sshd.client.future.DefaultOpenFuture
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.FactoryManager
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NetconfSshClientWrapperTest {
    private lateinit var mockSession: ClientSession
    private lateinit var mockClient: SshClient
    private lateinit var mockChannel: ClientChannel
    private lateinit var mockSubsystem: ChannelSubsystem
    private lateinit var sampleInputStream: InputStream
    private lateinit var sampleOutputStream: ByteArrayOutputStream

    companion object {
        private const val someString = "Some string"
        private val deviceInfo = DeviceInfo().apply {
            username = "user"
            password = "pass"
            ipAddress = "localhost"
            port = 830
        }
    }

    @Before
    fun setup() {
        mockSession = mockk()
        mockClient = mockk()
        mockChannel = mockk()
        mockSubsystem = mockk()
        sampleInputStream = ByteArrayInputStream(someString.toByteArray(StandardCharsets.UTF_8))
        sampleOutputStream = ByteArrayOutputStream()
    }

    @Test
    fun `get client channel input stream returns invertedOut`() {
        every { mockChannel.invertedOut } returns sampleInputStream
        val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
        netconfSshClientWrapper.getClientChannelInputStream()
        verify { mockChannel.invertedOut }
    }

    @Test
    fun `getInputStreamReader returns a reader of the channel`() {
        every { mockChannel.invertedOut } returns sampleInputStream
        val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
        val inputStreamReader = netconfSshClientWrapper.getInputStreamReader()
        assertEquals(someString, inputStreamReader.readText())
    }

    @Test
    fun `getOutputStreamWriter uses channel supplied writer`() {
        every { mockChannel.invertedIn } returns sampleOutputStream
        val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
        val outputStreamWriter = netconfSshClientWrapper.getOutputStreamWriter()
        outputStreamWriter.write(someString)
        outputStreamWriter.flush()
        assertEquals(someString, String(sampleOutputStream.toByteArray(), StandardCharsets.UTF_8))
    }

    @Test
    fun `startClient starts underlying client and starts SSH session`() {
        val propertiesMap = hashMapOf<String, Any>()
        every { mockClient.start() } just Runs
        every { mockClient.properties } returns propertiesMap
        val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
        val spy = spyk(netconfSshClientWrapper, recordPrivateCalls = true)
        every { spy["startSession"](deviceInfo) as Unit } just Runs
        spy.startClient(deviceInfo)
        verify { mockClient.start() }
        verify { spy["startSession"](deviceInfo) }
        assertTrue { propertiesMap.containsKey(FactoryManager.IDLE_TIMEOUT) }
        assertTrue { propertiesMap.containsKey(FactoryManager.NIO2_READ_TIMEOUT) }
    }

    @Test
    fun `startSession tries to connect to user supplied device`() {
        every { mockClient.start() } just Runs
        every { mockClient.properties } returns hashMapOf<String, Any>()
        //setup slots to capture values from the invocations
        val userSlot = CapturingSlot<String>()
        val ipSlot = CapturingSlot<String>()
        val portSlot = CapturingSlot<Int>()
        //create a future that succeeded
        val succeededFuture = DefaultConnectFuture(Any(), Any())
        succeededFuture.value = mockSession
        every { mockClient.connect(capture(userSlot), capture(ipSlot), capture(portSlot)) } returns succeededFuture
        val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
        val spy = spyk(netconfSshClientWrapper, recordPrivateCalls = true)
        every { spy["authSession"](deviceInfo) as Unit } just Runs
        //RUN
        spy.startClient(deviceInfo)
        //Verify
        verify { mockClient.connect(deviceInfo.username, deviceInfo.ipAddress, deviceInfo.port) }
        assertEquals(deviceInfo.username, userSlot.captured)
        assertEquals(deviceInfo.ipAddress, ipSlot.captured)
        assertEquals(deviceInfo.port, portSlot.captured)
        verify { spy["authSession"](deviceInfo) }
    }

    @Test
    fun `authSession throws exception if ClientSession is not AUTHED`() {
        assertFailsWith(exceptionClass = NetconfException::class) {
            //after client session connects,
            every { mockClient.start() } just Runs
            every { mockClient.properties } returns hashMapOf<String, Any>()
            val succeededAuthFuture = DefaultAuthFuture(Any(), Any())
            succeededAuthFuture.value = true //AuthFuture's value is Boolean
            val passSlot = CapturingSlot<String>()
            every { mockSession.addPasswordIdentity(capture(passSlot)) } just Runs
            every { mockSession.auth() } returns succeededAuthFuture
            val succeededSessionFuture = DefaultConnectFuture(Any(), Any())
            succeededSessionFuture.value = mockSession
            every { mockClient.connect(deviceInfo.username, deviceInfo.ipAddress, deviceInfo.port) } returns succeededSessionFuture
            every { mockSession.waitFor(any(), any()) } returns
                setOf(ClientSession.ClientSessionEvent.WAIT_AUTH, ClientSession.ClientSessionEvent.CLOSED)

            val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
            netconfSshClientWrapper.startClient(deviceInfo)
        }
    }

    private fun setupOpenChannelMocks(): Unit {
        every { mockClient.start() } just Runs
        every { mockClient.properties } returns hashMapOf<String, Any>()
        val succeededAuthFuture = DefaultAuthFuture(Any(), Any())
        succeededAuthFuture.value = true //AuthFuture's value is Boolean
        val passSlot = CapturingSlot<String>()
        every { mockSession.addPasswordIdentity(capture(passSlot)) } just Runs
        every { mockSession.auth() } returns succeededAuthFuture
        val succeededSessionFuture = DefaultConnectFuture(Any(), Any())
        succeededSessionFuture.value = mockSession
        every {mockClient.connect(deviceInfo.username, deviceInfo.ipAddress, deviceInfo.port)} returns succeededSessionFuture
        every { mockSession.waitFor(any(), any()) } returns
            setOf(ClientSession.ClientSessionEvent.WAIT_AUTH,
                ClientSession.ClientSessionEvent.CLOSED,
                ClientSession.ClientSessionEvent.AUTHED)
        //For some reason, encountered issue with mockk making "spy["openChannel"](deviceInfo) just Runs"
        //it's not different in any way from 'authSession' just runs as before, yet it didn't work!!!
        //TODO: make a test with openChannel just runs
        every { mockSession.createSubsystemChannel(any()) } returns mockSubsystem
    }


    @Test
    fun `authSession opensChannel if ClientSession is AUTHED and session can be opened`() {
        //after client session connects, make sure the client receives authentication
        setupOpenChannelMocks()
        val channelFuture = DefaultOpenFuture(Any(), Any())
        channelFuture.value = true
        channelFuture.setOpened()
        val connectFuture = DefaultConnectFuture(Any(), Any())
        connectFuture.value = mockSession
        connectFuture.session = mockSession
        every { mockSubsystem.open() } returns channelFuture
        every { mockClient.connect(deviceInfo.username, deviceInfo.ipAddress, deviceInfo.port)} returns connectFuture
        val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
        val spy = spyk(netconfSshClientWrapper, recordPrivateCalls = true)
        //Run
        netconfSshClientWrapper.startClient(deviceInfo)
        //Verify
        verify { mockSubsystem.open() }
    }

    @Test
    fun `authSession throws NetconfException if ClientSession is AUTHED but channelFuture timed out or not open`() {
        assertFailsWith(exceptionClass = NetconfException::class) {
            //after client session connects, make sure the client receives authentication
            setupOpenChannelMocks()
            val channelFuture = DefaultOpenFuture(Any(), Any())
            every { mockSubsystem.open() } returns channelFuture
            val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
            val spy = spyk(netconfSshClientWrapper, recordPrivateCalls = true)
            //Run
            netconfSshClientWrapper.startClient(deviceInfo)
            //Verify
            verify { mockSubsystem.open() }
        }
    }

    @Test
    fun `close closes session, channel, and client`() {
        every { mockSession.close() } just Runs
        every { mockChannel.close() } just Runs
        every { mockClient.close() } just Runs
        val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
        netconfSshClientWrapper.close()
        verify { mockSession.close() }
        verify { mockChannel.close() }
        verify { mockClient.close() }
    }

    @Test
    fun `close throws IOException if session doesn't close`() {
        assertFailsWith(exceptionClass = IOException::class) {
            every { mockSession.close() } throws IOException("session doesn't want to close!")
            val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
            netconfSshClientWrapper.close()
        }
    }

    @Test
    fun `close throws IOException if channel doesn't close`() {
        assertFailsWith(exceptionClass = IOException::class) {
            every { mockSession.close() } just Runs
            every { mockChannel.close() } throws IOException("channel doesn't want to close!")
            val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
            netconfSshClientWrapper.close()
            verify { mockSession.close() }
        }
    }

    @Test
    fun `close throws IOException if client doesn't close`() {
        assertFailsWith(exceptionClass = IOException::class) {
            every { mockSession.close() } just Runs
            every { mockChannel.close() } just Runs
            every { mockClient.close() } throws IOException("client doesn't want to close!")
            val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
            netconfSshClientWrapper.close()
            verify { mockSession.close() }
            verify { mockChannel.close() }
        }
    }

    @Test
    fun `reestablishNetconfSshSessionIfDisconnected restarts client if client isClosed`() {
        every { mockClient.isClosed } returns true
        every { mockSession.isClosed } returns false
        every { mockChannel.isClosed } returns false

        val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
        val spy = spyk(netconfSshClientWrapper)
        every { spy.startClient(deviceInfo) } just Runs
        assertEquals(true, spy.reestablishNetconfSshSessionIfDisconnected(deviceInfo))
        verify { spy.startClient(deviceInfo) }
    }

    @Test
    fun `reestablishNetconfSshSessionIfDisconnected restarts session if session isClosed`() {
        every { mockClient.isClosed } returns false
        every { mockSession.isClosed } returns  true
        every { mockChannel.isClosed } returns false

        val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
        val spy = spyk(netconfSshClientWrapper, recordPrivateCalls = true)
        every { spy["startSession"](deviceInfo) as Unit} just Runs
        assertEquals(true, spy.reestablishNetconfSshSessionIfDisconnected(deviceInfo))
        verify { spy["startSession"](deviceInfo) }
    }

    @Test
    fun `reestablishNetconfSshSessionIfDisconnected reopens channel if channel isClosed`() {
        every { mockClient.isClosed } returns false
        every { mockSession.isClosed } returns  false
        every { mockChannel.isClosed } returns true

        val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
        val spy = spyk(netconfSshClientWrapper, recordPrivateCalls = true)
        every { spy["openChannel"](deviceInfo) as Unit} just Runs
        assertEquals(true, spy.reestablishNetconfSshSessionIfDisconnected(deviceInfo))
        verify { spy["openChannel"](deviceInfo) }
    }

    @Test
    fun `reestablishNetconfSshSessionIfDisconnected throws NetconfException if IOException is caught`() {
        assertFailsWith(exceptionClass = NetconfException::class) {
            every { mockClient.isClosed } throws IOException("Some IOException happened!")
            val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
            assertEquals(true, netconfSshClientWrapper.reestablishNetconfSshSessionIfDisconnected(deviceInfo))
        }
    }

    @Test
    fun `reestablishNetconfSshSessionIfDisconnected throws NetconfException if IllegalStateException is caught`() {
        assertFailsWith(exceptionClass = NetconfException::class) {
            every { mockClient.isClosed } throws IllegalStateException("Ladies and gentlemen, IllegalStateException !")
            val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
            assertEquals(true, netconfSshClientWrapper.reestablishNetconfSshSessionIfDisconnected(deviceInfo))
        }
    }



    @Test
    fun `reestablishNetconfSshSessionIfDisconnected returns false if nothing is closed`() {
        every { mockClient.isClosed } returns false
        every { mockSession.isClosed } returns  false
        every { mockChannel.isClosed } returns false

        val netconfSshClientWrapper = NetconfSshClientWrapper(mockSession, mockClient, mockChannel)
        assertEquals(false, netconfSshClientWrapper.reestablishNetconfSshSessionIfDisconnected(deviceInfo))
    }
}
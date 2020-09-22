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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceResponse
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfRpcService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.NetconfMessageUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.RpcStatus
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NetconfSessionImplTest {
    companion object {
        val SUCCESSFUL_DEVICE_RESPONSE = DeviceResponse().apply {
            status = RpcStatus.SUCCESS
            errorMessage = ""
            responseMessage = ""
            requestMessage = ""
        }
        val FAILED_DEVICE_RESPONSE = DeviceResponse().apply {
            status = RpcStatus.FAILURE
            errorMessage = ""
            responseMessage = ""
            requestMessage = ""
        }
        val deviceInfo: DeviceInfo = DeviceInfo().apply {
            username = "username"
            password = "password"
            ipAddress = "localhost"
            port = 2224
            connectTimeout = 10
        }
        private const val someString = "Some string"
    }

    private lateinit var netconfSession: NetconfSessionImpl
    private lateinit var netconfCommunicator: NetconfDeviceCommunicator
    private lateinit var rpcService: NetconfRpcService
    private lateinit var mockSshClient: SshClient
    private lateinit var mockClientSession: ClientSession
    private lateinit var mockClientChannel: ClientChannel
    private lateinit var mockSubsystem: ChannelSubsystem

    private val futureMsg = "blahblahblah"
    private val request = "0"
    private val sessionId = "0"
    private val messageId = "asdfasdfadf"
    private val deviceCapabilities = setOf("capability1", "capability2")
    private val formattedRequest = NetconfMessageUtils.formatRPCRequest(request, messageId, deviceCapabilities)
    private lateinit var sampleInputStream: InputStream
    private lateinit var sampleOutputStream: ByteArrayOutputStream

    @Before
    fun setup() {
        netconfCommunicator = mockk()
        rpcService = mockk()
        netconfSession = NetconfSessionImpl(deviceInfo, rpcService)
        netconfSession.setStreamHandler(netconfCommunicator)
        mockSshClient = mockk()
        mockClientSession = mockk()
        mockClientChannel = mockk()
        mockSubsystem = mockk()
        sampleInputStream = ByteArrayInputStream(someString.toByteArray(StandardCharsets.UTF_8))
        sampleOutputStream = ByteArrayOutputStream()
    }

    @Test
    fun `connect calls appropriate methods`() {
        val session = spyk(netconfSession, recordPrivateCalls = true)
        every { session["startClient"]() as Unit } just Runs
        session.connect()
        verify { session["startClient"]() }
    }

    // look for NetconfException being thrown when cannot connect
    @Test
    fun `connect throws NetconfException on error`() {
        val errMsg = "$deviceInfo: Failed to establish SSH session"
        assertFailsWith(exceptionClass = NetconfException::class, message = errMsg) {
            val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
            every { netconfSessionSpy["startClient"]() as Unit } throws NetconfException(errMsg)
            netconfSessionSpy.connect()
        }
    }

    @Test
    fun `disconnect without force option for rpcService succeeds`() {
        // rpcService.closeSession succeeds with status not RpcStatus.FAILURE
        every { rpcService.closeSession(false) } returns SUCCESSFUL_DEVICE_RESPONSE
        every { mockClientSession.close() } just Runs
        every { mockSshClient.close() } just Runs
        every { mockClientChannel.close() } just Runs
        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        netconfSessionSpy.setSession(mockClientSession)
        netconfSessionSpy.setClient(mockSshClient)
        netconfSessionSpy.setChannel(mockClientChannel)
        // RUN
        netconfSessionSpy.disconnect()
        // make sure that rpcService.close session is not called again.
        verify(exactly = 0) { rpcService.closeSession(true) }
        verify { mockClientSession.close() }
        verify { mockSshClient.close() }
        verify { mockClientChannel.close() }
    }

    @Test
    fun `disconnect with force option for rpcService succeeds`() {
        // rpcService.closeSession succeeds with status not RpcStatus.FAILURE
        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        every { rpcService.closeSession(any()) } returns
            FAILED_DEVICE_RESPONSE andThen SUCCESSFUL_DEVICE_RESPONSE
        every { mockClientSession.close() } just Runs
        every { mockSshClient.close() } just Runs
        every { mockClientChannel.close() } just Runs
        netconfSessionSpy.setSession(mockClientSession)
        netconfSessionSpy.setClient(mockSshClient)
        netconfSessionSpy.setChannel(mockClientChannel)
        // RUN
        netconfSessionSpy.disconnect()
        // VERIFY
        verify(exactly = 2) { rpcService.closeSession(any()) }
        verify { mockClientSession.close() }
        verify { mockSshClient.close() }
        verify { mockClientChannel.close() }
    }

    @Test
    fun `disconnect wraps exception from ssh closing error`() {
        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        every { netconfSessionSpy["close"]() as Unit } throws IOException("Some IOException occurred!")
        every { rpcService.closeSession(false) } returns SUCCESSFUL_DEVICE_RESPONSE
        every { netconfSessionSpy.checkAndReestablish() } just Runs
        netconfSessionSpy.disconnect()
        verify { netconfSessionSpy["close"]() }
    }

    @Test
    fun `reconnect calls disconnect and connect`() {
        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        every { netconfSessionSpy.disconnect() } just Runs
        every { netconfSessionSpy.connect() } just Runs
        netconfSessionSpy.reconnect()
        verify { netconfSessionSpy.disconnect() }
        verify { netconfSessionSpy.connect() }
    }

    @Test
    fun `checkAndReestablish restarts connection and clears replies on sshClient disconnection`() {
        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        every { mockSshClient.isClosed } returns true
        netconfSessionSpy.setClient(mockSshClient)
        every { netconfSessionSpy["startConnection"]() as Unit } just Runs
        // Call method
        netconfSessionSpy.checkAndReestablish()
        // Verify
        verify { netconfSessionSpy.clearReplies() }
        verify { netconfSessionSpy["startConnection"]() }
    }

    @Test
    fun `checkAndReestablish restarts session and clears replies on clientSession closing`() {
        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        every { mockClientSession.isClosed } returns true
        every { mockSshClient.isClosed } returns false
        every { netconfSessionSpy["startSession"]() as Unit } just Runs
        netconfSessionSpy.setClient(mockSshClient)
        netconfSessionSpy.setSession(mockClientSession)
        // Call method
        netconfSessionSpy.checkAndReestablish()
        // Verify
        verify { netconfSessionSpy.clearReplies() }
        verify { netconfSessionSpy["startSession"]() }
    }

    @Test
    fun `checkAndReestablish reopens channel and clears replies on channel closing`() {
        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        every { mockClientSession.isClosed } returns false
        every { mockSshClient.isClosed } returns false
        every { mockClientChannel.isClosed } returns true
        every { netconfSessionSpy["openChannel"]() as Unit } just Runs
        netconfSessionSpy.setClient(mockSshClient)
        netconfSessionSpy.setSession(mockClientSession)
        netconfSessionSpy.setChannel(mockClientChannel)
        // Call method
        netconfSessionSpy.checkAndReestablish()
        // Verify
        verify { netconfSessionSpy.clearReplies() }
        verify { netconfSessionSpy["openChannel"]() }
    }

    @Test
    fun `syncRpc runs normally`() {
        val netconfSessionSpy = spyk(netconfSession)
        val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)

        // test the case where SSH connection did not need to be re-established.
        // put an existing item into the replies
        netconfSessionSpy.getReplies()["somekey"] = CompletableFuture.completedFuture("${futureMsg}2")
        every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
        every { netconfCommunicator.getFutureFromSendMessage(any(), any(), any()) } returns futureRet.get()
        every { netconfSessionSpy.checkAndReestablish() } just Runs
        // call the method
        assertEquals(futureMsg, netconfSessionSpy.syncRpc("0", "0"))
        // make sure the replies didn't change
        assertTrue {
            netconfSessionSpy.getReplies().size == 1 &&
                netconfSessionSpy.getReplies().containsKey("somekey")
        }
        verify(exactly = 0) { netconfSessionSpy.clearReplies() }
    }

    @Test
    fun `syncRpc still succeeds and replies are cleared on client disconnect`() {
        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)

        // put an item into the replies
        netconfSessionSpy.getReplies()["somekey"] = CompletableFuture.completedFuture("${futureMsg}2")

        // tests the case where SSH session needs to be re-established.
        every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
        every { netconfSessionSpy["startClient"]() as Unit } just Runs
        every { netconfCommunicator.getFutureFromSendMessage(any(), any(), any()) } returns futureRet.get()
        every { mockSshClient.isClosed } returns true
        netconfSessionSpy.setClient(mockSshClient)

        // call the method
        assertEquals(futureMsg, netconfSessionSpy.syncRpc("0", "0"))
        // make sure the replies got cleared out
        assertTrue { netconfSessionSpy.getReplies().isEmpty() }
        verify(exactly = 1) { netconfSessionSpy.clearReplies() }
    }

    // Test for handling CompletableFuture.get returns InterruptedException inside NetconfDeviceCommunicator
    @Test
    fun `syncRpc throws NetconfException if InterruptedException is caught`() {
        val expectedExceptionMsg = "$deviceInfo: Interrupted while waiting for reply for request: $formattedRequest"
        assertFailsWith(exceptionClass = NetconfException::class, message = expectedExceptionMsg) {
            val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
            val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)
            every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
            every { netconfCommunicator.getFutureFromSendMessage(any(), any(), any()) } throws InterruptedException("interrupted")
            every { netconfSessionSpy.checkAndReestablish() } just Runs
            // call the method
            netconfSessionSpy.syncRpc("0", "0")
        }
    }

    @Test
    fun `syncRpc throws NetconfException if TimeoutException is caught`() {
        val expectedExceptionMsg =
            "$deviceInfo: Timed out while waiting for reply for request $formattedRequest after ${deviceInfo.replyTimeout} sec."
        assertFailsWith(exceptionClass = NetconfException::class, message = expectedExceptionMsg) {
            val netconfSessionSpy = spyk(netconfSession)
            val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)
            every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
            every { netconfCommunicator.getFutureFromSendMessage(any(), any(), any()) } throws TimeoutException("timed out")
            every { netconfSessionSpy.checkAndReestablish() } just Runs
            // call the method
            netconfSessionSpy.syncRpc("0", "0")
        }
    }

    @Test
    fun `syncRpc throws NetconfException if ExecutionException is caught`() {
        val expectedExceptionMsg = "$deviceInfo: Closing session $sessionId for request $formattedRequest"
        assertFailsWith(exceptionClass = NetconfException::class, message = expectedExceptionMsg) {
            val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = false)
            val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)
            every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
            every { netconfCommunicator.getFutureFromSendMessage(any(), any(), any()) } throws
                ExecutionException("exec exception", Exception("nested exception"))
            every { netconfSessionSpy["close"]() as Unit } just Runs
            every { netconfSessionSpy.checkAndReestablish() } just Runs
            netconfSessionSpy.setSession(mockClientSession)
            // call the method
            netconfSessionSpy.syncRpc("0", "0")
        }
    }

    @Test
    fun `syncRpc throws NetconfException if caught ExecutionException and failed to close SSH session`() {
        val expectedExceptionMsg = "$deviceInfo: Closing session $sessionId for request $formattedRequest"
        assertFailsWith(exceptionClass = NetconfException::class, message = expectedExceptionMsg) {
            val netconfSessionSpy = spyk(netconfSession)
            val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)
            every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
            every { netconfCommunicator.getFutureFromSendMessage(any(), any(), any()) } throws
                ExecutionException("exec exception", Exception("nested exception"))
            every { netconfSessionSpy["close"]() as Unit } throws IOException("got an IO exception")
            every { netconfSessionSpy.checkAndReestablish() } just Runs
            // call the method
            netconfSessionSpy.syncRpc("0", "0")
            // make sure replies are cleared...
            verify(exactly = 1) { netconfSessionSpy.clearReplies() }
            verify(exactly = 1) { netconfSessionSpy.clearErrorReplies() }
        }
    }

    @Test
    fun `asyncRpc runs normally`() {
        val netconfSessionSpy = spyk(netconfSession)
        every { netconfSessionSpy.checkAndReestablish() } just Runs
        val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)
        every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
        // run the method
        val rpcResultFuture = netconfSessionSpy.asyncRpc("0", "0")
        every { netconfSessionSpy.checkAndReestablish() } just Runs
        // make sure the future gets resolved
        assertTrue { rpcResultFuture.get() == futureMsg }
        // make sure that clearReplies wasn't called (reestablishConnection check)
        verify(exactly = 0) { netconfSessionSpy.clearReplies() }
    }

    @Test
    fun `asyncRpc wraps exception`() {
        val netconfSessionSpy = spyk(netconfSession)
        every { netconfSessionSpy.checkAndReestablish() } just Runs
        val futureRet: CompletableFuture<String> = CompletableFuture.supplyAsync {
            throw Exception("blah")
        }
        every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
        // run the method
        val rpcResultFuture = netconfSessionSpy.asyncRpc("0", "0")
        every { netconfSessionSpy.checkAndReestablish() } just Runs
        val e = assertFailsWith(exceptionClass = ExecutionException::class, message = futureMsg) {
            rpcResultFuture.get()
        }
        val cause = e.cause
        assertTrue { cause is NetconfException }
    }

    @Test
    fun `connect starts underlying client`() {
        val propertiesMap = hashMapOf<String, Any>()
        every { mockSshClient.start() } just Runs
        every { mockSshClient.properties } returns propertiesMap
        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        every { netconfSessionSpy["setupNewSSHClient"]() as Unit } just Runs
        every { netconfSessionSpy["startSession"]() as Unit } just Runs
        netconfSessionSpy.setClient(mockSshClient)
        netconfSessionSpy.connect()
        verify { mockSshClient.start() }
        assertTrue { propertiesMap.containsKey(FactoryManager.IDLE_TIMEOUT) }
        assertTrue { propertiesMap.containsKey(FactoryManager.NIO2_READ_TIMEOUT) }
    }

    @Test
    fun `startSession tries to connect to user supplied device`() {
        every { mockSshClient.start() } just Runs
        every { mockSshClient.properties } returns hashMapOf<String, Any>()
        // setup slots to capture values from the invocations
        val userSlot = CapturingSlot<String>()
        val ipSlot = CapturingSlot<String>()
        val portSlot = CapturingSlot<Int>()
        // create a future that succeeded
        val succeededFuture = DefaultConnectFuture(Any(), Any())
        succeededFuture.value = mockClientSession
        every { mockSshClient.connect(capture(userSlot), capture(ipSlot), capture(portSlot)) } returns succeededFuture
        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        every { netconfSessionSpy["authSession"]() as Unit } just Runs
        every { netconfSessionSpy["setupNewSSHClient"]() as Unit } just Runs
        netconfSessionSpy.setClient(mockSshClient)
        // RUN
        netconfSessionSpy.connect()
        // Verify
        verify { mockSshClient.connect(deviceInfo.username, deviceInfo.ipAddress, deviceInfo.port) }
        assertEquals(deviceInfo.username, userSlot.captured)
        assertEquals(deviceInfo.ipAddress, ipSlot.captured)
        assertEquals(deviceInfo.port, portSlot.captured)
        verify { netconfSessionSpy["authSession"]() }
    }

    @Test
    fun `authSession throws exception if ClientSession is not AUTHED`() {
        assertFailsWith(exceptionClass = NetconfException::class) {
            // after client session connects,
            every { mockSshClient.start() } just Runs
            every { mockSshClient.properties } returns hashMapOf<String, Any>()
            val succeededAuthFuture = DefaultAuthFuture(Any(), Any())
            succeededAuthFuture.value = true // AuthFuture's value is Boolean
            val passSlot = CapturingSlot<String>()
            every { mockClientSession.addPasswordIdentity(capture(passSlot)) } just Runs
            every { mockClientSession.auth() } returns succeededAuthFuture
            val succeededSessionFuture = DefaultConnectFuture(Any(), Any())
            succeededSessionFuture.value = mockClientSession
            every { mockSshClient.connect(deviceInfo.username, deviceInfo.ipAddress, deviceInfo.port) } returns succeededSessionFuture
            every { mockClientSession.waitFor(any(), any()) } returns
                setOf(ClientSession.ClientSessionEvent.WAIT_AUTH, ClientSession.ClientSessionEvent.CLOSED)
            val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
            every { netconfSessionSpy["setupNewSSHClient"]() as Unit } just Runs
            netconfSessionSpy.setClient(mockSshClient)
            // RUN
            netconfSessionSpy.connect()
        }
    }

    // common mock initializer for more weird tests.
    private fun setupOpenChannelMocks() {
        every { mockSshClient.start() } just Runs
        every { mockSshClient.properties } returns hashMapOf<String, Any>()
        val succeededAuthFuture = DefaultAuthFuture(Any(), Any())
        succeededAuthFuture.value = true // AuthFuture's value is Boolean
        val passSlot = CapturingSlot<String>()
        every { mockClientSession.addPasswordIdentity(capture(passSlot)) } just Runs
        every { mockClientSession.auth() } returns succeededAuthFuture
        val succeededSessionFuture = DefaultConnectFuture(Any(), Any())
        succeededSessionFuture.value = mockClientSession
        every { mockSshClient.connect(deviceInfo.username, deviceInfo.ipAddress, deviceInfo.port) } returns succeededSessionFuture
        every { mockClientSession.waitFor(any(), any()) } returns
            setOf(
                ClientSession.ClientSessionEvent.WAIT_AUTH,
                ClientSession.ClientSessionEvent.CLOSED,
                ClientSession.ClientSessionEvent.AUTHED
            )

        every { mockClientSession.createSubsystemChannel(any()) } returns mockSubsystem
        every { mockClientChannel.invertedOut } returns sampleInputStream
        every { mockClientChannel.invertedIn } returns sampleOutputStream
    }

    @Test
    fun `authSession opensChannel if ClientSession is AUTHED and session can be opened`() {
        // after client session connects, make sure the client receives authentication
        setupOpenChannelMocks()
        val channelFuture = DefaultOpenFuture(Any(), Any())
        channelFuture.value = true
        channelFuture.setOpened()
        val connectFuture = DefaultConnectFuture(Any(), Any())
        connectFuture.value = mockClientSession
        connectFuture.session = mockClientSession
        every { mockSubsystem.open() } returns channelFuture
        every { mockSshClient.connect(deviceInfo.username, deviceInfo.ipAddress, deviceInfo.port) } returns connectFuture

        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        every { netconfSessionSpy["setupNewSSHClient"]() as Unit } just Runs
        every { netconfSessionSpy["setupHandler"]() as Unit } just Runs
        netconfSessionSpy.setClient(mockSshClient)
        // Run
        netconfSessionSpy.connect()
        // Verify
        verify { mockSubsystem.open() }
    }

    @Test
    fun `authSession throws NetconfException if ClientSession is AUTHED but channelFuture timed out or not open`() {
        assertFailsWith(exceptionClass = NetconfException::class) {
            // after client session connects, make sure the client receives authentication
            setupOpenChannelMocks()
            val channelFuture = DefaultOpenFuture(Any(), Any())
            every { mockSubsystem.open() } returns channelFuture
            val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
            every { netconfSessionSpy["setupNewSSHClient"]() as Unit } just Runs
            every { netconfSessionSpy["setupHandler"]() as Unit } just Runs
            netconfSessionSpy.setClient(mockSshClient)
            // Run
            netconfSessionSpy.connect()
            // Verify
            verify { mockSubsystem.open() }
        }
    }

    @Test
    fun `disconnect closes session, channel, and client`() {
        every { rpcService.closeSession(false) } returns SUCCESSFUL_DEVICE_RESPONSE
        every { mockClientSession.close() } just Runs
        every { mockClientChannel.close() } just Runs
        every { mockSshClient.close() } just Runs
        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        netconfSessionSpy.setChannel(mockClientChannel)
        netconfSessionSpy.setClient(mockSshClient)
        netconfSessionSpy.setSession(mockClientSession)
        // RUN
        netconfSessionSpy.disconnect()
        // VERIFY
        verify { mockClientSession.close() }
        verify { mockClientChannel.close() }
        verify { mockSshClient.close() }
    }

    @Test
    fun `disconnect wraps IOException if channel doesn't close`() { // this test is equivalent to others
        every { rpcService.closeSession(false) } returns SUCCESSFUL_DEVICE_RESPONSE
        every { mockClientSession.close() } just Runs
        every { mockClientChannel.close() } throws IOException("channel doesn't want to close!")
        val netconfSessionSpy = spyk(netconfSession, recordPrivateCalls = true)
        netconfSessionSpy.setChannel(mockClientChannel)
        netconfSessionSpy.setClient(mockSshClient)
        netconfSessionSpy.setSession(mockClientSession)
        // RUN
        netconfSessionSpy.disconnect()
        // VERIFY
        verify { mockClientSession.close() }
    }
}

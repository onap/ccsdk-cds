/*
 *  Copyright (C) 2019 Amdocs, AT&T, Bell Canada
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceResponse
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfRpcService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core.NetconfDeviceCommunicator
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core.NetconfRpcServiceImpl
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core.NetconfSessionImpl
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core.NetconfSshClientWrapper
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.NetconfMessageUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.RpcStatus
import java.io.IOException
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
            status = RpcStatus.SUCCESS
            errorMessage = ""
            responseMessage = ""
            requestMessage = ""
        }
    }

    //private var device: NetconfDeviceSimulator? = null
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var netconfSession: NetconfSessionImpl
    private lateinit var sshClientWrapper: NetconfSshClientWrapper
    private lateinit var netconfCommunicator: NetconfDeviceCommunicator
    private lateinit var rpcService: NetconfRpcService
    private val futureMsg = "blahblahblah"
    private val request = "0"
    private val sessionId = "0"
    private val messageId = "asdfasdfadf"
    private val deviceCapabilities = setOf("capability1", "capability2")
    private val formattedRequest = NetconfMessageUtils.formatRPCRequest(request, messageId, deviceCapabilities)

    @Before
    fun before() {
        deviceInfo = DeviceInfo().apply {
            username = "username"
            password = "password"
            ipAddress = "localhost"
            port = 2224
            connectTimeout = 10
        }


// deprecated, TODO: move to integration tests
//        device = NetconfDeviceSimulator(deviceInfo!!.port)
//        device!!.start()
        netconfCommunicator = mockk<NetconfDeviceCommunicator>()
        sshClientWrapper = mockk<NetconfSshClientWrapper>()
        rpcService = mockk<NetconfRpcService>()
        netconfSession = NetconfSessionImpl(deviceInfo, rpcService, sshClientWrapper)
        netconfSession.setStreamHandler(netconfCommunicator)
    }


    @After
    fun after() {
        // device!!.stop()
    }

    @Test
    //Verify that SshClientWrapper's startclient is getting called.
    fun connenectCallsAppropriateMethods() {
        val session = spyk(netconfSession, recordPrivateCalls = true)
        every { session["setupHandler"]() as Unit } just Runs
        every { session["exchangeHelloMessage"]() as Unit } just Runs
        every { sshClientWrapper.startClient(any()) } just Runs
        session.connect()
        verify { sshClientWrapper.startClient(deviceInfo) }
    }

    //look for NetconfException being thrown when cannot connect
    @Test
    fun connectThrowsNetconfExceptionOnError() {
        val errMsg = "$deviceInfo: Failed to establish SSH session"
        assertFailsWith(exceptionClass = NetconfException::class, message = errMsg) {
            val session = spyk(netconfSession, recordPrivateCalls = true)
            every { sshClientWrapper.startClient(any()) } throws
                NetconfException(errMsg)

            every { session["setupHandler"]() as Unit } just Runs
            every { session["exchangeHelloMessage"]() as Unit } just Runs
            session.connect()
        }
    }


    @Test
    fun `disconnect without force option for rpcService succeeds`() {
        //rpcService.closeSession succeeds with status not RpcStatus.FAILURE
        every { rpcService.closeSession(false) } returns SUCCESSFUL_DEVICE_RESPONSE
        every { sshClientWrapper.close() } just Runs
        val netconfSessionSpy = spyk(netconfSession)
        netconfSessionSpy.disconnect()
        //make sure that rpcService.close session is not called again.
        verify(exactly = 0) { rpcService.closeSession(true) }
        verify { sshClientWrapper.close() }
    }

    @Test
    fun `disconnect with force option for rpcService succeeds`() {
        //rpcService.closeSession succeeds with status not RpcStatus.FAILURE
        every { rpcService.closeSession(any()) } returns
            FAILED_DEVICE_RESPONSE andThen SUCCESSFUL_DEVICE_RESPONSE
        every { sshClientWrapper.close() } just Runs

        val netconfSessionSpy = spyk(netconfSession)
        netconfSessionSpy.disconnect()
        verify(exactly = 1) { rpcService.closeSession(any()) }
        verify { sshClientWrapper.close() }
    }

    @Test
    fun `disconnect wraps exception from ssh closing error`() {
        every { sshClientWrapper.close() } throws IOException("something's not right")
        every { rpcService.closeSession(false) } returns SUCCESSFUL_DEVICE_RESPONSE
        netconfSession.disconnect()
        verify { sshClientWrapper.close() }
    }

    @Test
    fun `reconnect calls disconnect and connect`() {
        val netconfSessionSpy = spyk(netconfSession)
        every { netconfSessionSpy.disconnect() } just Runs
        every { netconfSessionSpy.connect() } just Runs
        netconfSessionSpy.reconnect()
        verify { netconfSessionSpy.disconnect() }
        verify { netconfSessionSpy.connect() }
    }

    @Test
    fun `syncRpc runs normally`() {
        val netconfSessionSpy = spyk(netconfSession)
        val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)

        //test the case where SSH connection did not need to be re-established.
        every { sshClientWrapper.reestablishNetconfSshSessionIfDisconnected(any()) } returns false
        //put an existing item into the replies
        netconfSessionSpy.getReplies()["somekey"] = CompletableFuture.completedFuture("${futureMsg}2")
        every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
        every { netconfCommunicator.getFutureFromSendMessage(any(), any(), any()) } returns futureRet.get()
        //call the method
        assertEquals(futureMsg, netconfSessionSpy.syncRpc("0", "0"))
        //make sure the replies didn't change
        assertTrue {
            netconfSessionSpy.getReplies().size == 1 &&
                netconfSessionSpy.getReplies().containsKey("somekey")
        }
        verify(exactly = 0) { netconfSessionSpy.clearReplies() }
    }


    @Test
    fun `syncRpc clears replies if ssh session dropped`() {
        val netconfSessionSpy = spyk(netconfSession)
        val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)

        //put an item into the replies
        netconfSessionSpy.getReplies()["somekey"] = CompletableFuture.completedFuture("${futureMsg}2")

        //tests the case where SSH session needs to be re-established.
        every { sshClientWrapper.reestablishNetconfSshSessionIfDisconnected(any()) } returns true
        every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
        every { netconfCommunicator.getFutureFromSendMessage(any(), any(), any()) } returns futureRet.get()
        //call the method
        assertEquals(futureMsg, netconfSessionSpy.syncRpc("0", "0"))
        //make sure the replies got cleared out
        assertTrue { netconfSessionSpy.getReplies().isEmpty() }
        verify(exactly = 1) { netconfSessionSpy.clearReplies() }
    }

    //Test for handling CompletableFuture.get returns InterruptedException inside NetconfDeviceCommunicator
    @Test
    fun `syncRpc throws NetconfException if InterruptedException is caught`() {
        val expectedExceptionMsg = "$deviceInfo: Interrupted while waiting for reply for request: $formattedRequest"
        assertFailsWith(exceptionClass = NetconfException::class, message = expectedExceptionMsg) {
            val netconfSessionSpy = spyk(netconfSession)
            val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)
            every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
            every { netconfCommunicator.getFutureFromSendMessage(any(), any(), any()) } throws InterruptedException("interrupted")
            every { sshClientWrapper.reestablishNetconfSshSessionIfDisconnected(any()) } returns false
            //call the method
            netconfSessionSpy.syncRpc("0", "0")
        }
    }

    @Test
    fun `syncRpc throws NetconfException if TimeoutException is caught`() {
        val expectedExceptionMsg = "$deviceInfo: Timed out while waiting for reply for request $formattedRequest after ${deviceInfo.replyTimeout} sec."
        assertFailsWith(exceptionClass = NetconfException::class, message = expectedExceptionMsg) {
            val netconfSessionSpy = spyk(netconfSession)
            val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)
            every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
            every { netconfCommunicator.getFutureFromSendMessage(any(), any(), any()) } throws TimeoutException("timed out")
            every { sshClientWrapper.reestablishNetconfSshSessionIfDisconnected(any()) } returns false
            //call the method
            netconfSessionSpy.syncRpc("0", "0")
        }
    }

    @Test
    fun `syncRpc throws NetconfException if ExecutionException is caught`() {
        val expectedExceptionMsg = "$deviceInfo: Closing session $sessionId for request $formattedRequest"
        assertFailsWith(exceptionClass = NetconfException::class, message = expectedExceptionMsg) {
            val netconfSessionSpy = spyk(netconfSession)
            val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)
            every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
            every { netconfCommunicator.getFutureFromSendMessage(any(), any(), any()) } throws
                ExecutionException("exec exception", Exception("nested exception"))
            every { sshClientWrapper.reestablishNetconfSshSessionIfDisconnected(any()) } returns false
            every { sshClientWrapper.close() } just Runs
            //call the method
            netconfSessionSpy.syncRpc("0", "0")
            verify(exactly = 1) { netconfSessionSpy.clearReplies() }
            verify(exactly = 1) { netconfSessionSpy.clearErrorReplies() }
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
            every { sshClientWrapper.reestablishNetconfSshSessionIfDisconnected(any()) } returns false
            every { sshClientWrapper.close() } throws IOException("got an IO exception")
            //call the method
            netconfSessionSpy.syncRpc("0", "0")
            //make sure replies are cleared...
            verify(exactly = 1) { netconfSessionSpy.clearReplies() }
            verify(exactly = 1) { netconfSessionSpy.clearErrorReplies() }
        }
    }

    @Test
    fun `asyncRpc`() {
        val netconfSessionSpy = spyk(netconfSession)
        val futureRet: CompletableFuture<String> = CompletableFuture.completedFuture(futureMsg)

        every { sshClientWrapper.reestablishNetconfSshSessionIfDisconnected(any()) } returns false
        every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
        //run the method
        val rpcResultFuture = netconfSessionSpy.asyncRpc("0", "0")
        //make sure the future gets resolved
        assertTrue { rpcResultFuture.get() == futureMsg }
        //make sure that clearReplies wasn't called (reestablishConnection check)
        verify(exactly = 0) { netconfSessionSpy.clearReplies() }
    }

    @Ignore
    @Test
    //TODO: get 't' inside asyncRpc to be a Throwable
    fun `asyncRpc wraps exception`() {
        assertFailsWith(exceptionClass = NetconfException::class, message = futureMsg) {
            val netconfSessionSpy = spyk(netconfSession)
            val futureRet: CompletableFuture<String> = CompletableFuture.supplyAsync {
                throw Exception("blah")
            }
            futureRet.completeExceptionally(IOException("something is wrong"))

            every { sshClientWrapper.reestablishNetconfSshSessionIfDisconnected(any()) } returns false
            every { netconfCommunicator.sendMessage(any(), any()) } returns futureRet
            //run the method
            val rpcResultFuture = netconfSessionSpy.asyncRpc("0", "0")
        }
    }

    /**
     * Previous tests - Integration tests??
     */
    @Test
    @Ignore
    @Throws(Exception::class)
    fun testNetconfSession() {
        val netconfSession = NetconfSessionImpl(deviceInfo!!, NetconfRpcServiceImpl(DeviceInfo()),
            NetconfSshClientWrapper())

        Assert.assertNotNull(netconfSession.getSessionId())
        Assert.assertEquals("localhost:2224", netconfSession.getDeviceInfo().toString())

//        netconfSession.reestablishNetconfSshSessionIfDisconnected() doesn't belong?????

        Assert.assertNotNull(netconfSession.getSessionId())
        Assert.assertEquals("localhost:2224", netconfSession.getDeviceInfo().toString())

        Assert.assertTrue(!netconfSession.getDeviceCapabilitiesSet().isEmpty())
    }

    @Test
    @Ignore
    fun testNetconfSessionconnect() {
        val netconfSession = NetconfSessionImpl(deviceInfo!!,
            NetconfRpcServiceImpl(deviceInfo!!),
            NetconfSshClientWrapper())
        netconfSession.connect()
        Assert.assertTrue(netconfSession.sessionstatus("Open"))
    }

    @Test
    @Ignore
    fun testNetconfSessionreconnect() {
        val netconfSession = NetconfSessionImpl(deviceInfo!!,
            NetconfRpcServiceImpl(deviceInfo!!),
            NetconfSshClientWrapper())
        netconfSession.connect()
        netconfSession.reconnect()
        Assert.assertTrue(netconfSession.sessionstatus("Open"))

    }

    @Test
    @Ignore
    fun testNetconfSessiondisconnect() {
        val netconfSession = NetconfSessionImpl(deviceInfo!!,
            NetconfRpcServiceImpl(deviceInfo!!),
            NetconfSshClientWrapper())
        netconfSession.connect()
        netconfSession.disconnect()
        Assert.assertTrue(netconfSession.sessionstatus("Close"))
    }

    @Test
    @Ignore
    fun testNetconfSessionconnecgetDeviceInfo() {
        val netconfSession = NetconfSessionImpl(deviceInfo!!,
            NetconfRpcServiceImpl(deviceInfo!!),
            NetconfSshClientWrapper())
        netconfSession.connect()
        Assert.assertNotNull(netconfSession.getDeviceInfo())
        Assert.assertFalse(!netconfSession.getDeviceCapabilitiesSet().isEmpty())
    }


}

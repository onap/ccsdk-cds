/*
 * Copyright © 2019 Bell Canada
 * Modifications Copyright (c) 2019 IBM
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

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceResponse
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.RpcStatus
import java.io.IOException
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NetconfRpcServiceImplTest {

    private lateinit var mockNetconfSession: NetconfSessionImpl

    companion object {

        private const val someString = "someString"
        private const val replyStr = "this is a reply"
        private val failedDeviceResponse = DeviceResponse(
            status = RpcStatus.FAILURE,
            requestMessage = "request message", responseMessage = replyStr
        ) // responseMessage will be null in this POJO
        private val successfulDeviceResponse = DeviceResponse(
            status = RpcStatus.SUCCESS,
            requestMessage = "request message", responseMessage = replyStr
        ) // responseMessage will be null in this POJO

        // but will be set later from mockSession
        private const val msgId = "100"
        private const val timeout = 5
        private val deviceInfo: DeviceInfo = DeviceInfo().apply {
            username = "username"
            password = "password"
            ipAddress = "localhost"
            port = 2224
            connectTimeout = 5
        }
    }

    @Before
    fun setup() {
        mockNetconfSession = mockk()
    }

    @Test
    fun `invokeRpc completes normally`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val invokeRpcrResult = spy.invokeRpc(someString)
        assertEquals(successfulDeviceResponse, invokeRpcrResult)
    }

    @Test
    fun `invokeRpc on error sets DeviceResponse status to FAILURE`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } throws IOException("Some IO exception...")
        val invokeRpcrResult = spy.invokeRpc(someString)
        assertEquals(failedDeviceResponse.status, invokeRpcrResult.status)
        assertTrue { invokeRpcrResult.errorMessage!!.contains("failed in 'invokeRpc' command") }
    }

    @Test
    fun `get completes normally`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val getRpcrResult = spy.get(someString)
        assertEquals(successfulDeviceResponse, getRpcrResult)
    }

    @Test
    fun `get on error sets DeviceResponse status to FAILURE`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } throws IOException("Some IO exception...")
        val getRpcResult = spy.get(someString)
        assertEquals(failedDeviceResponse.status, getRpcResult.status)
        assertTrue { getRpcResult.errorMessage!!.contains("failed in 'get' command") }
    }

    @Test
    fun `getConfig completes normally`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val getConfigRpcResult = spy.getConfig(someString)
        assertEquals(successfulDeviceResponse, getConfigRpcResult)
    }

    @Test
    fun `getConfig on error sets DeviceResponse status to FAILURE`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } throws IOException("Some IO exception...")
        val getConfigRpcResult = spy.getConfig(someString)
        assertEquals(failedDeviceResponse.status, getConfigRpcResult.status)
        assertTrue { getConfigRpcResult.errorMessage!!.contains("failed in 'get-config' command") }
    }

    @Test
    fun `deleteConfig completes normally`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val rpcResult = spy.deleteConfig(someString)
        assertEquals(successfulDeviceResponse, rpcResult)
    }

    @Test
    fun `deleteConfig on error sets DeviceResponse status to FAILURE`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } throws IOException("Some IO exception...")
        val rpcResult = spy.deleteConfig(someString)
        assertEquals(failedDeviceResponse.status, rpcResult.status)
        assertTrue { rpcResult.errorMessage!!.contains("failed in 'delete-config' command") }
    }

    @Test
    fun `lock completes normally`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val rpcResult = spy.lock(someString)
        assertEquals(successfulDeviceResponse, rpcResult)
    }

    @Test
    fun `lock on error sets DeviceResponse status to FAILURE`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } throws IOException("Some IO exception...")
        val rpcResult = spy.lock(someString)
        assertEquals(failedDeviceResponse.status, rpcResult.status)
        assertTrue { rpcResult.errorMessage!!.contains("failed in 'lock' command") }
    }

    @Test
    fun `unLock completes normally`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val rpcResult = spy.unLock(someString)
        assertEquals(successfulDeviceResponse, rpcResult)
    }

    @Test
    fun `unLock on error sets DeviceResponse status to FAILURE`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } throws IOException("Some IO exception...")
        val rpcResult = spy.unLock(someString)
        assertEquals(failedDeviceResponse.status, rpcResult.status)
        assertTrue { rpcResult.errorMessage!!.contains("failed in 'unLock' command") }
    }

    @Test
    fun `commit completes normally on confirmed flag and only persist but not persistId specified`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val rpcResult = spy.commit(true, timeout, persist = "blah", persistId = "")
        assertEquals(successfulDeviceResponse, rpcResult)
    }

    @Test
    fun `commit completes normally on no confirm flag and only persistId but not persist specified`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val rpcResult = spy.commit(false, timeout, persistId = "blah")
        assertEquals(successfulDeviceResponse, rpcResult)
    }

    @Test
    fun `commit fails on confirm flag with persistId specified`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns failedDeviceResponse
        val rpcResult = spy.commit(true, timeout, persistId = "blah")
        assertTrue { rpcResult.errorMessage!!.contains("failed in 'commit' command") }
    }

    @Test
    fun `commit fails on confirm flag with persist and persistId specified`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns failedDeviceResponse
        val rpcResult = spy.commit(true, timeout, persist = "blah", persistId = "blah")
        assertTrue { rpcResult.errorMessage!!.contains("failed in 'commit' command") }
    }

    @Test
    fun `commit fails on no confirm flag with persist and persistId specified`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns failedDeviceResponse
        val rpcResult = spy.commit(false, timeout, persist = "blah", persistId = "blah")
        assertTrue { rpcResult.errorMessage!!.contains("failed in 'commit' command") }
    }

    @Test
    fun `cancelCommit completes normally`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val rpcResult = spy.cancelCommit(someString)
        assertEquals(successfulDeviceResponse, rpcResult)
    }

    @Test
    fun `cancelCommit on error sets DeviceResponse status to FAILURE`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } throws IOException("Some IO exception...")
        val rpcResult = spy.cancelCommit(someString)
        assertEquals(failedDeviceResponse.status, rpcResult.status)
        assertTrue { rpcResult.errorMessage!!.contains("failed in 'cancelCommit' command") }
    }

    @Test
    fun `discardConfig completes normally`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val rpcResult = spy.discardConfig()
        assertEquals(successfulDeviceResponse, rpcResult)
    }

    @Test
    fun `discardConfig on error sets DeviceResponse status to FAILURE`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } throws IOException("Some IO exception...")
        val rpcResult = spy.discardConfig()
        assertEquals(failedDeviceResponse.status, rpcResult.status)
        assertTrue { rpcResult.errorMessage!!.contains("failed in 'discard-config' command") }
    }

    @Test
    fun `editConfig completes normally`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val rpcResult = spy.editConfig("blah1", "blah2", "blah3")
        assertEquals(successfulDeviceResponse, rpcResult)
    }

    @Test
    fun `editConfig on error sets DeviceResponse status to FAILURE`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } throws IOException("Some IO exception...")
        val rpcResult = spy.editConfig("blah1", "blah2", "blah3")
        assertEquals(failedDeviceResponse.status, rpcResult.status)
        assertTrue { rpcResult.errorMessage!!.contains("failed in 'editConfig' command") }
    }

    @Test
    fun `validate completes normally`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val rpcResult = spy.validate("blah1")
        assertEquals(successfulDeviceResponse, rpcResult)
    }

    @Test
    fun `validate on error sets DeviceResponse status to FAILURE`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } throws IOException("Some IO exception...")
        val rpcResult = spy.validate("blah1")
        assertEquals(failedDeviceResponse.status, rpcResult.status)
        assertTrue { rpcResult.errorMessage!!.contains("failed in 'validate' command") }
    }

    @Test
    fun `closeSession completes normally without force`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val rpcResult = spy.closeSession(false)
        assertEquals(successfulDeviceResponse, rpcResult)
    }

    @Test
    fun `closeSession completes normally with force`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val rpcResult = spy.closeSession(true)
        assertEquals(successfulDeviceResponse, rpcResult)
    }

    @Test
    fun `closeSession on error sets DeviceResponse status to FAILURE`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } throws IOException("Some IO exception...")
        val rpcResult = spy.closeSession(true)
        assertEquals(failedDeviceResponse.status, rpcResult.status)
        assertTrue { rpcResult.errorMessage!!.contains("failed in 'closeSession' command") }
    }

    @Test
    fun `asyncRpc completes normally`() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(mockNetconfSession)
        val spy = spyk(netconfRpcService)
        every { spy.asyncRpc(any(), any()) } returns successfulDeviceResponse
        val rpcResult = spy.asyncRpc("blah1", "blah2")
        assertEquals(successfulDeviceResponse, rpcResult)
    }

    @Test
    fun `asyncRpc on error throws NetconfException`() {
        assertFailsWith(exceptionClass = NetconfException::class) {
            val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
            netconfRpcService.setNetconfSession(mockNetconfSession)
            val spy = spyk(netconfRpcService)
            val erroneousFuture = CompletableFuture<String>()
            erroneousFuture.complete("something something rpc-error>")
            every { mockNetconfSession.asyncRpc(any(), any()) } returns erroneousFuture
            val rpcResult = spy.asyncRpc("blah1", "blah2")
            assertEquals(failedDeviceResponse.status, rpcResult.status)
            assertTrue { rpcResult.errorMessage!!.contains("failed in 'closeSession' command") }
        }
    }
}

/*
 *  Copyright (C) 2019 Bell Canada
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

import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Ignore

import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceResponse
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.RpcStatus
import kotlin.test.assertEquals

class NetconfRpcServiceImplTest {
    private lateinit var netconfSession: NetconfSessionImpl
    companion object {
        const val someString = "someString"
        val deviceInfo: DeviceInfo = DeviceInfo().apply {
            username = "username"
            password = "password"
            ipAddress = "localhost"
            port = 2224
            connectTimeout = 5
        }
    }

    @Before
    fun setup() {
        netconfSession = mockk()
    }

    @Ignore
    @Test
    fun invokeRpc() {
        val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
        netconfRpcService.setNetconfSession(netconfSession)
        val expectedDeviceResponse = DeviceResponse(status = RpcStatus.SUCCESS,
            requestMessage = "request message",
            responseMessage = "response message")
        val msgId = "100"
        every { netconfRpcService.asyncRpc(expectedDeviceResponse.requestMessage!! , msgId) } returns
            DeviceResponse()
        val invokeRpcrResult = netconfRpcService.invokeRpc(someString)
        assertEquals(expectedDeviceResponse, invokeRpcrResult)
    }

    @Test
    fun getConfig() {
    }

    @Test
    fun deleteConfig() {
    }

    @Test
    fun lock() {
    }

    @Test
    fun unLock() {
    }

    @Test
    fun commit() {
    }

    @Test
    fun cancelCommit() {
    }

    @Test
    fun discardConfig() {
    }

    @Test
    fun editConfig() {
    }

    @Test
    fun validate() {
    }

    @Test
    fun closeSession() {
    }

    @Test
    fun asyncRpc() {
    }
}
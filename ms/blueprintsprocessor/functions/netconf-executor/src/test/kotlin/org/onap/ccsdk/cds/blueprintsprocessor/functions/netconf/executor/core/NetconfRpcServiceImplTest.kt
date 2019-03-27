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

import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.mocks.NetconfDeviceSimulator

class NetconfRpcServiceImplTest {

    private var device: NetconfDeviceSimulator? = null
    private lateinit var deviceInfo: DeviceInfo

    @Before
    fun before() {
        deviceInfo = DeviceInfo().apply {
            username = "username"
            password = "password"
            ipAddress = "localhost"
            port = 2224
            connectTimeout = 10
        }

        device = NetconfDeviceSimulator(deviceInfo!!.port)
        device!!.start()
    }

    @After
    fun after() {
        device!!.stop()
    }

    @Test
    fun setNetconfSession() {
    }

    companion object {
        const val FAILURE = "failure"
        const val FILTER = "filter"
        const val TARGET = "target"
    }

    @Test
    fun getConfig() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo)
        val netconfSession = NetconfSessionImpl(deviceInfo,
                NetconfRpcServiceImpl(deviceInfo),
                NetconfSshClientWrapper())
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        assertEquals(FAILURE,
            netconfRpcServiceImpl.getConfig(FILTER, TARGET).status)
    }


    @Test
    fun deleteConfig() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo)
        val netconfSession = NetconfSessionImpl(deviceInfo,
                netconfRpcServiceImpl, NetconfSshClientWrapper())
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        assertEquals(FAILURE, netconfRpcServiceImpl.deleteConfig(TARGET).status)
    }

    @Test
    fun lock() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo)
        val netconfSession = NetconfSessionImpl(deviceInfo,
                netconfRpcServiceImpl, NetconfSshClientWrapper())
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        assertEquals(FAILURE, netconfRpcServiceImpl.lock(TARGET).status)
    }

    @Test
    fun unLock() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo)
        val netconfSession = NetconfSessionImpl(deviceInfo,
                netconfRpcServiceImpl, NetconfSshClientWrapper())
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        assertEquals(FAILURE, netconfRpcServiceImpl.unLock(TARGET).status)
    }

    @Test
    fun commit() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo)
        val netconfSession = NetconfSessionImpl(deviceInfo,
                netconfRpcServiceImpl, NetconfSshClientWrapper())
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        assertEquals(FAILURE,
            netconfRpcServiceImpl.commit(true, 60, "persist", "1").status)
    }

    @Test
    fun cancelCommit() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo)
        val netconfSession = NetconfSessionImpl(deviceInfo,
                netconfRpcServiceImpl, NetconfSshClientWrapper())
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()

        assertNotNull(netconfRpcServiceImpl.cancelCommit("1"))
    }

    @Test
    fun discardConfig() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo)
        val netconfSession = NetconfSessionImpl(deviceInfo,
                netconfRpcServiceImpl, NetconfSshClientWrapper())
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        assertEquals(FAILURE, netconfRpcServiceImpl.discardConfig().status)
    }

    @Test
    fun editConfig() {
    }

    @Test
    fun validate() {
        val netconfRpcServiceImpl = NetconfRpcServiceImpl(deviceInfo)
        val netconfSession = NetconfSessionImpl(deviceInfo,
                netconfRpcServiceImpl, NetconfSshClientWrapper())
        netconfRpcServiceImpl.setNetconfSession(netconfSession)
        netconfSession.connect()
        assertEquals(FAILURE, netconfRpcServiceImpl.validate(TARGET).status)
    }
}
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
package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.core.NetconfSessionImpl
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.DeviceInfo
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.NetconfDeviceSimulator


class NetconfSessionImplTest {

    private var device: NetconfDeviceSimulator? = null
    private var deviceInfo: DeviceInfo? = null

    @Before
    fun before() {
        deviceInfo =DeviceInfo("name", "password", "localhost", 2224, "10")

        device = NetconfDeviceSimulator(deviceInfo!!.port)
        device!!.start()
    }

    @After
    fun after() {
        device!!.stop()
    }

    @Throws(Exception::class)
    fun testNetconfSession() {
        val netconfSession = NetconfSessionImpl(deviceInfo!!)

        Assert.assertNotNull(netconfSession.getSessionId())
        Assert.assertEquals("localhost:2224", netconfSession.getDeviceInfo().toString())

        netconfSession.checkAndReestablish()

        Assert.assertNotNull(netconfSession.getSessionId())
        Assert.assertEquals("localhost:2224", netconfSession.getDeviceInfo().toString())

        Assert.assertTrue(!netconfSession.getDeviceCapabilitiesSet().isEmpty())
    }

}

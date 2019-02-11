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

import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.NetconfException
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.DeviceInfo
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.NetconfSession
import java.util.*

object NetconfSessionFactory {

    private fun NetconfSessionFactory() {}

    val netConfSessionManagerMap = HashMap<String, NetconfSession>()

    fun registerNetConfSessionManager(type: String, netconfSession: NetconfSession) {
        netConfSessionManagerMap[type] = netconfSession
    }

    /**
     * Creates a new NETCONF session for the specified device.
     *
     * @param type type of the session.
     * @param netconfDeviceInfo information of the device to create the session for.
     * @return Instance of NetconfSession.
     * @throws NetconfException when problems arise establishing the connection.
     */
    @Throws(NetconfException::class)
    fun instance(type: String, netconfDeviceInfo: DeviceInfo): NetconfSession {
        return if (netConfSessionManagerMap.containsKey(type)) {
            netConfSessionManagerMap[type]!!
        } else {
            return NetconfSessionImpl(netconfDeviceInfo)
        }
    }
}
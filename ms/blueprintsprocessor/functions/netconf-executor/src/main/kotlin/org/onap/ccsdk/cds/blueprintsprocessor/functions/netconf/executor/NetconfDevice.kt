/*
 * Copyright (C) 2019 Bell Canada.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor

import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfSession
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core.NetconfRpcServiceImpl
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core.NetconfSessionImpl

data class NetconfDevice(val deviceInfo: DeviceInfo) {

    val netconfRpcService = NetconfRpcServiceImpl(deviceInfo)
    val netconfSession: NetconfSession

    init {
        netconfSession = NetconfSessionImpl(deviceInfo, netconfRpcService)
        netconfRpcService.setNetconfSession(netconfSession)
    }
}

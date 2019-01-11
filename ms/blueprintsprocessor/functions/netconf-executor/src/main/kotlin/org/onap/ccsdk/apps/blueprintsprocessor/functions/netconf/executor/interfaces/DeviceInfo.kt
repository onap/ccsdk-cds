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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces


data class DeviceInfo (
        var name: String? = null,
        var pass: String? = null,
        var ipAddress: String? = null,
        var port: Int = 0,
        var key: String? = null,
   // private var sshClientLib: NetconfSshClientLib = NetconfSshClientLib,

        var connectTimeoutSec: Long = 30,
        var replyTimeout: Int = 60,
        var idleTimeout: Int = 45,
        var deviceId: String? = null
){
    /**
     * Information for contacting the controller.
     *
     * @param name the connection type
     * @param pass the pass for the device
     * @param ipAddress the ip address
     * @param port the tcp port
     */
    fun DeviceInfo(name: String, pass: String, ipAddress: String, port: Int, connectTimeoutSec: Long){
        //checkArgument(name != "", "Empty device username")
       // checkArgument(port > 0, "Negative port")
        //checkNotNull(ipAddress, "Null ip address")
        this.name = name
        this.pass = pass
        this.ipAddress = ipAddress
        this.port = port
        //this.sshClientLib = Optional.ofNullable(NetconfSshClientLib)
        this.connectTimeoutSec = connectTimeoutSec
        this. deviceId = "$ipAddress:$port"
    }
}
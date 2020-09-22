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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

class DeviceInfo {

    @get:JsonProperty("login-account")
    var username: String? = null

    @get:JsonProperty("login-key")
    var password: String? = null

    @get:JsonProperty("target-ip-address")
    var ipAddress: String? = null

    @get:JsonProperty("port-number")
    var port: Int = 0

    @get:JsonProperty("connection-time-out")
    var connectTimeout: Long = 30

    @get:JsonIgnore
    var source: String? = null

    @get:JsonProperty("reply-time-out")
    var replyTimeout: Int = 30

    @get:JsonIgnore
    var idleTimeout: Int = 99999

    override fun toString(): String {
        return "$ipAddress:$port"
    }

    // TODO: should this be a data class instead? Is anything using the JSON serdes?
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

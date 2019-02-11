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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.data

import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.DeviceInfo
import java.io.IOException
import java.util.*




class NetconfExecutionRequest {
    lateinit var requestId: String
    val action: String? = null
    val source: String? = null
    val loginKey: String? = null
    val loginAccount: String? = null
    val targetIP: String? = null
    val port: Int = 0
    val connectionTimeoutSec: Int = 0
    val implementationScript: String? = null
    val context: MutableMap<String, Any> = mutableMapOf()
}

class DeviceResponse {
    lateinit var deviceInfo: DeviceInfo
    lateinit var status: String
    var errorMessage: String? = null
    var responseMessage: String? = null
    var requestMessage: String? = null
    var subDeviceResponse: MutableMap<Any, Any>? = null

    fun addSubDeviceResponse(key: String, subDeviceResponse: DeviceResponse) {
        if (this.subDeviceResponse == null) {
            this.subDeviceResponse = hashMapOf()
        }
        this.subDeviceResponse!![key] = subDeviceResponse
    }
}

class NetconfExecutionResponse {
    val status: String? = null
    val errorMessage: String? = null
    val responseData: Any = Any()
}


class NetconfDeviceOutputEvent {

        private var type: NetconfDeviceOutputEvent.Type
        private var messagePayload: String? = null
        private var messageID: String? = null
        private var deviceInfo: DeviceInfo? = null
        private var subject: Any? = null
        private var time: Long = 0

        /**
         * Type of device related events.
         */
        enum class Type {
            DEVICE_REPLY,
            DEVICE_NOTIFICATION,
            DEVICE_UNREGISTERED,
            DEVICE_ERROR,
            SESSION_CLOSED
        }

        /**
         * Creates an event of a given type and for the specified subject and the current time.
         *
         * @param type event type
         * @param subject event subject
         * @param payload message from the device
         * @param msgID id of the message related to the event
         * @param netconfDeviceInfo device of event
         */
        constructor(type: Type, subject: String, payload: String, msgID: Optional<String>, netconfDeviceInfo: DeviceInfo) {
            this.type = type
            this.subject = subject
            this.messagePayload = payload
            this.deviceInfo = netconfDeviceInfo
            this.messageID = msgID.get()
        }

        /**
         * Creates an event of a given type and for the specified subject and time.
         *
         * @param type event type
         * @param subject event subject
         * @param payload message from the device
         * @param msgID id of the message related to the event
         * @param netconfDeviceInfo device of event
         * @param time occurrence time
         */
        constructor(type: Type, subject: Any, payload: String, msgID: String, netconfDeviceInfo: DeviceInfo, time: Long) {
            this.type = type
            this.subject = subject
            this.time = time
            this.messagePayload = payload
            this.deviceInfo = netconfDeviceInfo
            this.messageID = msgID
        }

    /**
     * return the message payload of the reply form the device.
     *
     * @return reply
     */
    fun getMessagePayload(): String? {
        return messagePayload
    }

    /**
     * Event-related device information.
     *
     * @return information about the device
     */
    fun getDeviceInfo(): DeviceInfo? {
        return deviceInfo
    }

    /**
     * Reply messageId.
     *
     * @return messageId
     */
    fun getMessageID(): String? {
        return messageID
    }

}
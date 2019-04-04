/*
 * Copyright © 2019 Bell Canada
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

import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.RpcStatus

class DeviceResponse {
    var status: String? = null
    var errorMessage: String? = null
    var responseMessage: String? = null
    var requestMessage: String? = null
    private var subDeviceResponse: MutableMap<Any, Any>? = null

    fun addSubDeviceResponse(key: String, subDeviceResponse: DeviceResponse) {
        if (this.subDeviceResponse == null) {
            this.subDeviceResponse = hashMapOf()
        }
        this.subDeviceResponse!![key] = subDeviceResponse
    }

    fun isSuccess(): Boolean {
        return this.status == RpcStatus.SUCCESS && this.errorMessage.isNullOrEmpty()
    }
}

/**
 * Creates an event of a given type and for the specified subject and the current time.
 *
 * @param type event type
 * @param messagePayload message from the device
 * @param messageId id of the message related to the event
 * @param deviceInfo device of event
 */
/**
 * Creates an event of a given type and for the specified subject and the current time.
 *
 * @param type event type
 * @param payload message from the device
 * @param messageId id of the message related to the event
 * @param deviceInfo device of event
 */
class NetconfReceivedEvent
(private var type: Type, private var payload: String = "", private var messageId: String = "",
 private var deviceInfo: DeviceInfo) {

    enum class Type {
        DEVICE_REPLY,
        DEVICE_UNREGISTERED,
        DEVICE_ERROR,
        SESSION_CLOSED
    }

    fun getType(): Type {
        return type
    }

    fun getMessagePayload(): String {
        return payload
    }

    fun getMessageID(): String {
        return messageId
    }
}
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

import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.data.DeviceResponse

interface NetconfRpcClientService {

    fun disconnect()


    fun reconnect()

    /**
     * @param messageId message id of the request.
     * @param configTarget config target ( running or candidate)
     * @param messageTimeout message timeout of the request.
     * @return Device response
     */
    fun lock(messageId: String, configTarget: String, messageTimeout: Int): DeviceResponse

    /**
     * @param messageId message id of the request.
     * @param messageContent filter content.
     * @param configTarget config target ( running or candidate)
     * @param messageTimeout message timeout of the request.
     * @return Device response
     */
    fun getConfig(messageId: String, messageContent: String, configTarget: String, messageTimeout: Int): DeviceResponse

    /**
     * @param messageId message id of the request.
     * @param configTarget config target ( running or candidate)
     * @param messageTimeout message timeout of the request.
     * @return Device response
     */
    fun deleteConfig(messageId: String, configTarget: String, messageTimeout: Int): DeviceResponse

    /**
     * @param messageId message id of the request.
     * @param messageContent edit config content.
     * @param reConnect reconnect session
     * @param wait waiting time to perform operation ( 0 indicates no wait )
     * @param lock lock the device before performing edit.
     * @param configTarget config target ( running or candidate)
     * @param editDefaultOperation edit default operation (merge | replace | create | delete | remove or
     * delete)
     * @param clearCandidate commit after edit config
     * @param commit clear candiate store before edit
     * @param discardChanges Rollback on failure
     * @param validate validate the config before commit
     * @param unlock unlock device after edit
     * @param preRestartWait
     * @param postRestartWait
     * @param messageTimeout message timeout of the request.
     * @return Device response
     */
    fun editConfig(messageId: String, messageContent: String, reConnect: Boolean, wait: Int, lock: Boolean,
                   configTarget: String, editDefaultOperation: String, clearCandidate: Boolean, validate: Boolean, commit: Boolean,
                   discardChanges: Boolean, unlock: Boolean, preRestartWait: Int, postRestartWait: Int, messageTimeout: Int): DeviceResponse

    /**
     * @param messageId message id of the request.
     * @param configTarget config target ( running or candidate)
     * @param messageTimeout message timeout of the request.
     * @return Device response
     */
    fun validate(messageId: String, configTarget: String, messageTimeout: Int): DeviceResponse

    /**
     * @param messageId message id of the request.
     * @param message optional commit message
     * @param discardChanges Rollback on failure
     * @param messageTimeout message timeout of the request.
     * @return Device response
     */
    fun commit(messageId: String, message: String, discardChanges: Boolean, messageTimeout: Int): DeviceResponse

    /**
     * @param messageId message id of the request.
     * @param configTarget config target ( running or candidate)
     * @param messageTimeout message timeout of the request.
     * @return Device response
     */
    fun unLock(messageId: String, configTarget: String, messageTimeout: Int): DeviceResponse

    /**
     * @param messageId message id of the request.
     * @param messageTimeout message timeout of the request.
     * @return Device response
     */
    fun discardConfig(messageId: String, messageTimeout: Int): DeviceResponse

    /**
     * @param messageId message id of the request.
     * @param force force close
     * @param messageTimeout message timeout of the request.
     * @return Device response
     */
    fun close(messageId: String, force: Boolean, messageTimeout: Int): DeviceResponse

    /**
     * Executes an RPC request to the netconf server.
     *
     * @param request the XML containing the RPC request for the server.
     * @param messageId message id of the request.
     * @param messageTimeout message timeout of the request.
     * @return Device response
     */
    fun asyncRpc(request: String, messageId: String, messageTimeout: Int): DeviceResponse
}
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
package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api

import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.ModifyAction
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.NetconfDatastore

interface NetconfRpcService {

    /**
     * Lock
     *
     * @param configTarget running or candidate, default candidate
     * @return Device response
     */
    fun lock(configTarget: String = NetconfDatastore.CANDIDATE.datastore): DeviceResponse

    /**
     * Get-config
     *
     * @param filter filter content, default empty
     * @param configTarget running or candidate, default running
     * @return Device response
     */
    fun getConfig(filter: String = "", configTarget: String = NetconfDatastore.RUNNING.datastore): DeviceResponse

    /**
     * Delete config
     *
     * @param configTarget running or candidate, default candidate
     * @return Device response
     */
    fun deleteConfig(configTarget: String = NetconfDatastore.CANDIDATE.datastore): DeviceResponse

    /**
     * Edit-config
     *
     * @param messageContent edit config content.
     * @param configTarget running or candidate, default candidate
     * @param editDefaultOperation, default set to none. Valid values: merge, replace, create, delete, none
     * @return Device response
     */
    fun editConfig(messageContent: String, configTarget: String = NetconfDatastore.CANDIDATE.datastore,
                   editDefaultOperation: String = ModifyAction.NONE.action): DeviceResponse

    /**
     * Validate
     *
     * @param configTarget running or candidate, default candidate
     * @return Device response
     */
    fun validate(configTarget: String = NetconfDatastore.CANDIDATE.datastore): DeviceResponse

    /**
     * Commit
     *
     * @return Device response
     */
    fun commit(): DeviceResponse

    /**
     * Unlock
     *
     * @param configTarget running or candidate, default candidate
     * @return Device response
     */
    fun unLock(configTarget: String = NetconfDatastore.CANDIDATE.datastore): DeviceResponse

    /**
     * Discard config
     *
     * @return Device response
     */
    fun discardConfig(): DeviceResponse

    /**
     * Close session
     *
     * @param force force closeSession
     * @return Device response
     */
    fun closeSession(force: Boolean): DeviceResponse

    /**
     * Executes an RPC request to the netconf server.
     *
     * @param request the XML containing the RPC request for the server.
     * @return Device response
     */
    fun asyncRpc(request: String, messageId: String): DeviceResponse
}
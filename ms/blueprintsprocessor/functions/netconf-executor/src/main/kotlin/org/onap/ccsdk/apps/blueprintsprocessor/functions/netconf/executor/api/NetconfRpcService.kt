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
     * Invoke custom RPC as provided as input.
     *
     * Some use cases might required one to directly invoke a device
     * specific RPC. The RPC must be correctly formatted.
     *
     * Ex: in order to rollback last submitted configuration
     * for JUNOS devices, such RPC can be use:
     * <code>
     *  &lt;rpc>
     *      &lt;load-configuration rollback="1"/>
     *  &lt;/rpc>
     * </code>
     *
     * @param rpc the rpc content.
     */
    fun invokeRpc(rpc: String): DeviceResponse

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
     * @param confirmed Perform a confirmed <commit> operation. If flag set to true,
     * then it is expected to have a follow-up <commit> operation to confirm the request
     * @param confirmTimeout Timeout period for confirmed commit, in seconds.
     * @param persist Make the confirmed commit survive a session termination, and
     * set a token on the ongoing confirmed commit.
     * @param persistId Used to issue a follow-up confirmed commit or a confirming
     * commit from any session, with the token from the previous <commit> operation.
     * If unspecified, the confirm timeout defaults to 600 seconds.
     * @return Device response
     */
    fun commit(confirmed: Boolean = false, confirmTimeout: Int = 60, persist: String = "",
               persistId: String = ""): DeviceResponse

    /**
     * Cancels an ongoing confirmed commit.  If the <persist-id> parameter is not given,
     * the <cancel-commit> operation MUST be issued on the same session that issued
     * the confirmed commit.
     *
     * @param persistId Cancels a persistent confirmed commit.  The value MUST be equal
     * to the value given in the <persist> parameter to the <commit> operation.
     * If the value does not match, the operation fails with an "invalid-value" error.
     */
    fun cancelCommit(persistId: String = ""): DeviceResponse

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
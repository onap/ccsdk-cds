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

import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

interface NetconfSession {

    /**
     * Executes an asynchronous RPC request to the server and obtains a future for it's response.
     *
     * @param request the XML containing the RPC request for the server.
     * @param msgId message id of the request.
     * @return Server response or ERROR
     * @throws NetconfException when there is a problem in the communication process on the underlying
     * connection
     * @throws NetconfTransportException on secure transport-layer error
     */
    fun asyncRpc(request: String, msgId: String): CompletableFuture<String>

    /**
     * Closes the Netconf session with the device. the first time it tries gracefully, then kills it
     * forcefully
     *
     * @return true if closed
     * @throws NetconfException when there is a problem in the communication process on the underlying
     * connection
     */
    fun close(): Boolean

    /**
     * Gets the session ID of the Netconf session.
     *
     * @return Session ID as a string.
     */
    fun getSessionId(): String

    /**
     * Gets the capabilities of the remote Netconf device associated to this session.
     *
     * @return Network capabilities as strings in a Set.
     */
    fun getDeviceCapabilitiesSet(): Set<String>

    /**
     * Checks the state of the underlying SSH session and connection and if necessary it reestablishes
     * it. Should be implemented, providing a default here for retro compatibility.
     *
     * @throws NetconfException when there is a problem in reestablishing the connection or the session
     * to the device.
     */
    fun checkAndReestablish() {
        val log = LoggerFactory.getLogger(NetconfSession::class.java)
        log.error("Not implemented/exposed by the underlying ({}) implementation", "NetconfSession")
    }

    /**
     * Sets the ONOS side capabilities.
     *
     * @param capabilities list of capabilities has.
     */
    fun setCapabilities(capabilities: List<String>) {
        // default implementation should be removed in the future
        // no-op
    }

    /**
     * Get the device information for initialised session.
     *
     * @return DeviceInfo as device information
     */
    //fun getDeviceInfo(): DeviceInfo
}
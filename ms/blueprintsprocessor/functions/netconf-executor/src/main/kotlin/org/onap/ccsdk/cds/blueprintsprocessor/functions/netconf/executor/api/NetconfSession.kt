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

import java.util.concurrent.CompletableFuture

interface NetconfSession {

    /**
     * Establish netconf session
     */
    fun connect()


    /**
     * Disconnect netconf session
     */
    fun disconnect()

    /**
     * Reconnect netconf session
     */
    fun reconnect()

    /**
     * Executes an synchronous RPC request.
     *
     * @param request the XML request
     * @param messageId message id of the request.
     * @return Response
     */
    @Throws(NetconfException::class)
    fun syncRpc(request: String, messageId: String): String

    /**
     * Executes an asynchronous RPC request.
     *
     * @param request the XML request
     * @param messageId message id of the request.
     * @return Response
     */
    @Throws(NetconfException::class)
    fun asyncRpc(request: String, messageId: String): CompletableFuture<String>

    /**
     * Checks the state of the underlying SSH session and connection and if necessary it reestablishes
     * it.
     */
    @Throws(NetconfException::class)
    fun checkAndReestablish()

    /**
     * Get the device information for initialised session.
     *
     * @return DeviceInfo as device information
     */
    fun getDeviceInfo(): DeviceInfo

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
}
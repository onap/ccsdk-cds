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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.RpcStatus

class NetconfMessageTest {

    @Test
    fun testSuccessfulDeviceResponse() {
        val dr: DeviceResponse = genSuccessfulEmptyDeviceResponse()
        assertTrue(dr.isSuccess())

        val dr2: DeviceResponse = genSuccessfulEmptyDeviceResponse()
        dr2.errorMessage = "some error msg"
        assertFalse(dr2.isSuccess())
    }

    @Test
    fun testUnsuccessfulDeviceResponse() {
        val dr: DeviceResponse = genUnsuccessfulEmptyDeviceResponse()
        assertFalse(dr.isSuccess())

        // case 2: Success, but with error message
        val dr2: DeviceResponse = genUnsuccessfulEmptyDeviceResponse()
        dr2.errorMessage = "Some error message."
        assertFalse(dr2.isSuccess())
    }

    // helper function to generate a device response
    private fun genSuccessfulEmptyDeviceResponse(): DeviceResponse {
        return DeviceResponse().apply {
            status = RpcStatus.SUCCESS
            errorMessage = ""
            responseMessage = ""
            requestMessage = ""
        }
    }

    private fun genUnsuccessfulEmptyDeviceResponse(): DeviceResponse {
        return DeviceResponse().apply {
            status = RpcStatus.FAILURE
            errorMessage = ""
            responseMessage = ""
            requestMessage = ""
        }
    }
}

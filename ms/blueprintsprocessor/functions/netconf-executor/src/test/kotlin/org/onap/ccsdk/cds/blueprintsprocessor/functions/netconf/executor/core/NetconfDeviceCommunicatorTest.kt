/*
 *  Copyright (C) 2019 Amdocs, Bell Canada
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core

import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class NetconfDeviceCommunicatorTest {
//TODO
//    @Test
//    fun testInvalidSymbols() {
//        val txtInputStream: InputStream = generateInputStreamFromString("nonsense text")
//        val outputStream: OutputStream = ByteArrayOutputStream()
//        val replies: MutableMap<String, CompletableFuture<String>> = ConcurrentHashMap()
//        val netconfDevCommunicator: NetconfDeviceCommunicator = NetconfDeviceCommunicator(
//                txtInputStream, outputStream,
//                genDeviceInfo(),
//                NetconfSessionListenerImpl(NetconfSessionImpl)
//                replies)
//
//    }

    private fun genDeviceInfo(): DeviceInfo {
        return DeviceInfo().apply {
            username = "user"
            password = "pass"
            ipAddress = "localhost"
            port = 4567
        }
    }

    //String to InputStream
    private fun generateInputStreamFromString(str: String): InputStream {
        return ByteArrayInputStream(str.toByteArray(Charsets.UTF_8))
    }
}
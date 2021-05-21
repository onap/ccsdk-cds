/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2021 Bell Canada
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verifySequence
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfSession
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core.NetconfRpcServiceImpl
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.storedContentFromResolvedArtifactNB

class ConfigDeployTest {

    private lateinit var unitUnderTest: ConfigDeploy
    private lateinit var netconfSession: NetconfSession
    private lateinit var netconfRpcService: NetconfRpcServiceImpl

    private val payload = """
            <configuration xmlns:junos="http://xml.juniper.net/junos/17.4R1/junos">
            <system xmlns="http://yang.juniper.net/junos-qfx/conf/system">
                <host-name operation="create">Test-Script</host-name>
            </system>
            </configuration>
    """.trimIndent()

    @Before
    fun setup() {
        // This will stub the extension functions
        mockkStatic("org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionExtensionsKt")
        mockkStatic("org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.NetconfExecutorExtensionsKt")

        unitUnderTest = ConfigDeploy()

        // Mock return values
        coEvery {
            unitUnderTest.storedContentFromResolvedArtifactNB("my-resolution-key", "create")
        }.returns(payload)

        mockk<NetconfDevice>().let {
            netconfSession = mockk(relaxed = true)
            netconfRpcService = mockk(relaxed = true)
            every { it.netconfSession }.returns(netconfSession)
            every { it.netconfRpcService }.returns(netconfRpcService)
            every { unitUnderTest.netconfDevice("netconf-connection") }.returns(it)
        }
    }

    @Test
    fun `should retrieve stored payload then connect and send to device`() {
        runBlocking { unitUnderTest.processNB(ExecutionServiceInput()) }

        coVerify {
            unitUnderTest.storedContentFromResolvedArtifactNB("my-resolution-key", "create")
        }
        verifySequence {
            netconfSession.connect()
            netconfRpcService.lock()
            netconfRpcService.editConfig(payload)
            netconfRpcService.commit()
            netconfRpcService.unLock()
            netconfSession.disconnect()
        }
    }
}

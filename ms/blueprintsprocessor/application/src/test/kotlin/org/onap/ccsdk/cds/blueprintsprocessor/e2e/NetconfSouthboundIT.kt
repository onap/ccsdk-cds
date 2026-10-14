/*
 * Copyright © 2026 Deutsche Telekom AG.
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

package org.onap.ccsdk.cds.blueprintsprocessor.e2e

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class NetconfSouthboundIT : BaseE2eIT() {

    @Before
    fun publishBlueprint() {
        publishOnce()
    }

    @Test
    fun `config deploy pushes the resolved configuration to the device in one candidate transaction`() {
        val resolutionKey = resolveConfig(vlanId = "301", mgmtIp = "10.0.0.31")

        val result = process(configDeployRequest(resolutionKey))

        assertEquals(200, result.httpStatus, result.body.toString())
        assertEquals(listOf("lock", "edit-config", "commit", "unlock", "close-session"), netconf.operations())
        val editConfig = netconf.rpcs.single { it.operation == "edit-config" }.xml
        assertThat(editConfig, containsString("<candidate/>"))
        assertThat(editConfig, containsString("<vlan-id>301</vlan-id>"))
        assertThat(editConfig, containsString("<mgmt-ip>10.0.0.31</mgmt-ip>"))
    }

    @Test
    fun `a rejected edit-config fails the workflow and nothing is committed`() {
        val resolutionKey = resolveConfig(vlanId = "302", mgmtIp = "10.0.0.32")
        netconf.failOperation("edit-config")

        val result = process(configDeployRequest(resolutionKey))

        assertEquals(500, result.httpStatus, result.body.toString())
        assertEquals(500, result.statusCode)
        assertThat(result.errorMessage, containsString("edit-config failed"))
        assertEquals(listOf("lock", "edit-config", "close-session"), netconf.operations())
    }

    private fun resolveConfig(vlanId: String, mgmtIp: String): String {
        val hostname = uniqueId("netconf")
        val resolutionKey = uniqueId("key")
        seedDevice(hostname, mgmtIp)
        wireMock.stubFor(get(urlEqualTo("/devices/$hostname")).willReturn(okJson("""{"vlan-id": "$vlanId"}""")))
        val assignment = process(resourceAssignmentRequest(hostname, resolutionKey = resolutionKey))
        assertEquals(200, assignment.httpStatus, assignment.body.toString())
        return resolutionKey
    }
}

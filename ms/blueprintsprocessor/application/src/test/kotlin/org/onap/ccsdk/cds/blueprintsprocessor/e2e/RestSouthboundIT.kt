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

import com.github.tomakehurst.wiremock.client.BasicCredentials
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.emptyString
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class RestSouthboundIT : BaseE2eIT() {

    @Before
    fun publishBlueprint() {
        publishOnce()
    }

    @Test
    fun `resource assignment resolves the vlan-id from the inventory over HTTP`() {
        val hostname = uniqueId("rest-ok")
        seedDevice(hostname, "10.0.0.11")
        wireMock.stubFor(get(urlEqualTo("/devices/$hostname")).willReturn(okJson("""{"vlan-id": "101"}""")))

        val result = process(resourceAssignmentRequest(hostname))

        assertEquals(200, result.httpStatus, result.body.toString())
        assertEquals(200, result.statusCode)
        assertThat(result.body.toString(), containsString("<vlan-id>101</vlan-id>"))
        wireMock.verify(
            1,
            getRequestedFor(urlEqualTo("/devices/$hostname"))
                .withBasicAuth(BasicCredentials(REST_USERNAME, REST_PASSWORD))
                .withHeader("Accept", equalTo("application/json"))
        )
    }

    @Test
    fun `an inventory error fails the workflow because the vlan-id is required`() {
        val hostname = uniqueId("rest-fail")
        seedDevice(hostname, "10.0.0.12")
        wireMock.stubFor(get(urlEqualTo("/devices/$hostname")).willReturn(aResponse().withStatus(500)))

        val result = process(resourceAssignmentRequest(hostname))

        assertEquals(500, result.httpStatus, result.body.toString())
        assertEquals(500, result.statusCode)
        assertThat(result.errorMessage, not(emptyString()))
        wireMock.verify(getRequestedFor(urlEqualTo("/devices/$hostname")))
    }
}

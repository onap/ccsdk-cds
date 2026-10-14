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

class KafkaInboundIT : BaseE2eIT() {

    @Before
    fun publishBlueprint() {
        publishOnce()
    }

    @Test
    fun `resource assignment requested over Kafka is answered on the response topic and stored`() {
        val hostname = uniqueId("kafka")
        val requestId = uniqueId("req")
        val resolutionKey = uniqueId("key")
        seedDevice(hostname, "10.0.0.51")
        wireMock.stubFor(get(urlEqualTo("/devices/$hostname")).willReturn(okJson("""{"vlan-id": "501"}""")))

        sendOverKafka(resourceAssignmentRequest(hostname, requestId = requestId, resolutionKey = resolutionKey))
        val response = awaitKafkaResponse(requestId)

        assertEquals(200, response.at("/status/code").asInt(), response.toString())
        assertThat(response.toString(), containsString("<vlan-id>501</vlan-id>"))
        val storedVlanId = jdbc.queryForObject(
            "SELECT value FROM RESOURCE_RESOLUTION WHERE resolution_key = ? AND name = 'vlan-id'",
            String::class.java, resolutionKey
        )
        assertEquals("501", storedVlanId)
    }
}

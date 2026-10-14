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
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import kotlin.test.assertEquals

class GrpcInboundIT : BaseE2eIT() {

    @Before
    fun publishBlueprint() {
        publishOnce()
    }

    @Test
    fun `resource assignment over gRPC resolves from the inventory and the processor DB`() {
        val hostname = uniqueId("grpc")
        val requestId = uniqueId("req")
        seedDevice(hostname, "10.0.0.41")
        wireMock.stubFor(get(urlEqualTo("/devices/$hostname")).willReturn(okJson("""{"vlan-id": "401"}""")))

        val output = processOverGrpc(resourceAssignmentRequest(hostname, requestId = requestId))

        assertEquals(EventType.EVENT_COMPONENT_EXECUTED, output.status.eventType, output.status.errorMessage)
        assertEquals(200, output.status.code)
        assertEquals(requestId, output.commonHeader.requestId)
        val renderedConfig = output.payload
            .getFieldsOrThrow("resource-assignment-response").structValue
            .getFieldsOrThrow("config").structValue
            .getFieldsOrThrow("config").stringValue
        assertThat(renderedConfig, containsString("<vlan-id>401</vlan-id>"))
        assertThat(renderedConfig, containsString("<mgmt-ip>10.0.0.41</mgmt-ip>"))
        wireMock.verify(1, getRequestedFor(urlEqualTo("/devices/$hostname")))
    }
}

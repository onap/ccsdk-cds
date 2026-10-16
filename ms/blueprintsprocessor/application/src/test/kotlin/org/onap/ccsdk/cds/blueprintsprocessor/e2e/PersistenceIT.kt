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
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PersistenceIT : BaseE2eIT() {

    @Test
    fun `publishing stores the blueprint and its archive in the database`() {
        val version = "2.0.0"

        publish(version)

        val modelIds = jdbc.queryForList(
            "SELECT blueprint_model_id FROM BLUEPRINT_MODEL WHERE artifact_name = ? AND artifact_version = ?",
            String::class.java, BLUEPRINT_NAME, version
        )
        assertEquals(1, modelIds.size)
        val archiveSize = jdbc.queryForObject(
            "SELECT LENGTH(content) FROM BLUEPRINT_MODEL_CONTENT WHERE blueprint_model_id = ?",
            Long::class.java, modelIds.single()
        )
        assertTrue(archiveSize!! > 0)
    }

    @Test
    fun `resource assignment stores every resolved value and the rendered template`() {
        publishOnce()
        val hostname = uniqueId("persist")
        val resolutionKey = uniqueId("key")
        seedDevice(hostname, "10.0.0.21")
        wireMock.stubFor(get(urlEqualTo("/devices/$hostname")).willReturn(okJson("""{"vlan-id": "201"}""")))

        val result = process(resourceAssignmentRequest(hostname, resolutionKey = resolutionKey))
        assertEquals(200, result.httpStatus, result.body.toString())

        val resolved = jdbc.queryForList(
            "SELECT name, value, status FROM RESOURCE_RESOLUTION WHERE resolution_key = ? AND blueprint_name = ?",
            resolutionKey, BLUEPRINT_NAME
        ).associate { it["name"] as String to (it["value"] as String to it["status"] as String) }
        assertEquals(
            mapOf(
                "hostname" to (hostname to "success"),
                "vlan-id" to ("201" to "success"),
                "mgmt-ip" to ("10.0.0.21" to "success")
            ),
            resolved
        )

        val template = jdbc.queryForObject(
            "SELECT result FROM TEMPLATE_RESOLUTION WHERE resolution_key = ? AND artifact_name = 'config'",
            String::class.java, resolutionKey
        )
        assertThat(template, containsString("<mgmt-ip>10.0.0.21</mgmt-ip>"))

        val stored = http.get()
            .uri {
                it.path("/api/v1/resources")
                    .queryParam("bpName", BLUEPRINT_NAME)
                    .queryParam("bpVersion", DEFAULT_VERSION)
                    .queryParam("artifactName", "config")
                    .queryParam("resolutionKey", resolutionKey)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!
        val storedValues = JacksonUtils.jsonNode(stored).associate { it["name"].asText() to it["value"].asText() }
        assertEquals("201", storedValues["vlan-id"])
        assertEquals("10.0.0.21", storedValues["mgmt-ip"])
    }

    @Test
    fun `deleting a resolution key removes its resolved values and template`() {
        publishOnce()
        val hostname = uniqueId("delete")
        val resolutionKey = uniqueId("key")
        seedDevice(hostname, "10.0.0.22")
        wireMock.stubFor(get(urlEqualTo("/devices/$hostname")).willReturn(okJson("""{"vlan-id": "202"}""")))
        val result = process(resourceAssignmentRequest(hostname, resolutionKey = resolutionKey))
        assertEquals(200, result.httpStatus, result.body.toString())

        assertEquals(3, delete("/api/v1/resources", resolutionKey))
        assertEquals(1, delete("/api/v1/template", resolutionKey))

        assertEquals(
            0,
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM RESOURCE_RESOLUTION WHERE resolution_key = ?", Int::class.java, resolutionKey
            )
        )
        assertEquals(
            0,
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM TEMPLATE_RESOLUTION WHERE resolution_key = ?", Int::class.java, resolutionKey
            )
        )
    }

    private fun delete(path: String, resolutionKey: String): Int {
        val response = http.delete()
            .uri {
                it.path(path)
                    .queryParam("bpName", BLUEPRINT_NAME)
                    .queryParam("bpVersion", DEFAULT_VERSION)
                    .queryParam("artifactName", "config")
                    .queryParam("resolutionKey", resolutionKey)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult().responseBody!!
        return JacksonUtils.jsonNode(response)["deletedEntries"].asInt()
    }
}

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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api

import org.hamcrest.Matchers.containsString
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
    classes = [DesignerApiTestConfiguration::class, ErrorCatalogTestConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class ResourceDictionaryControllerWebTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    private val authorization =
        "Basic " + Base64.getEncoder().encodeToString("ccsdkapps:ccsdkapps".toByteArray(UTF_8))

    private fun resourceDefinition(name: String): ResourceDefinition {
        val resourceDefinition = JacksonUtils.readValueFromFile(
            "./../../../../../components/model-catalog/resource-dictionary/starter-dictionary/sample-db-source.json",
            ResourceDefinition::class.java
        )!!
        resourceDefinition.name = name
        return resourceDefinition
    }

    @Test
    fun saveAllResourceDefinitionWithInvalidEntryReturnsBadRequest() {
        val invalid = resourceDefinition("web-test-no-updated-by")
        invalid.updatedBy = ""

        webTestClient.post().uri("/api/v1/dictionary/definition-bulk")
            .header("Authorization", authorization)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(JacksonUtils.getJson(listOf(resourceDefinition("web-test-valid-entry"), invalid)))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.code").isEqualTo(400)
            .jsonPath("$.message").value(containsString("web-test-no-updated-by"))

        webTestClient.get().uri("/api/v1/dictionary/web-test-valid-entry")
            .header("Authorization", authorization)
            .exchange()
            .expectStatus().isNotFound
    }
}

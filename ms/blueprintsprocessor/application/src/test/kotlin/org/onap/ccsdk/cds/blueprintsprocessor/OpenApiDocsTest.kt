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

package org.onap.ccsdk.cds.blueprintsprocessor

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assume.assumeNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.TestSecuritySettings
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.WorkingFoldersInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "springdoc.writer-with-default-pretty-printer=true",
        "springdoc.writer-with-order-by-keys=true"
    ]
)
@ContextConfiguration(
    initializers = [
        WorkingFoldersInitializer::class,
        TestSecuritySettings.ServerContextInitializer::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@AutoConfigureWebTestClient(timeout = "PT30S")
class OpenApiDocsTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var buildProperties: BuildProperties

    @LocalServerPort
    var serverPort: Int = 0

    @Test
    fun `api-docs describe the REST endpoints`() {
        val spec = ObjectMapper().readTree(get("/v3/api-docs"))

        assertTrue(spec["openapi"].asText().startsWith("3."))
        assertEquals(buildProperties.version, spec["info"]["version"].asText())
        val paths = spec["paths"]
        assertTrue(paths["/api/v1/execution-service/process"].has("post"))
        assertTrue(paths["/api/v1/blueprint-model"].has("get"))
        assertTrue(paths["/api/v1/blueprint-model"].has("post"))
        assertEquals(
            "ExecutionServiceController_process_POST.org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api",
            paths["/api/v1/execution-service/process"]["post"]["operationId"].asText()
        )
    }

    @Test
    fun `swagger ui is served`() {
        webTestClient.get().uri("/webjars/swagger-ui/index.html")
            .header(HttpHeaders.AUTHORIZATION, TestSecuritySettings.clientAuthToken())
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `api-docs require authentication`() {
        webTestClient.get().uri("/v3/api-docs")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `write the api reference`() {
        val outputDirectory = System.getProperty("openapi.outputDirectory")
        assumeNotNull(outputDirectory)

        mapOf("json" to "/v3/api-docs", "yaml" to "/v3/api-docs.yaml").forEach { (extension, uri) ->
            val spec = get(uri).replace("http://localhost:$serverPort", "http://localhost:8080")
            Files.writeString(Paths.get(outputDirectory, "cds-bp-processor-api-swagger.$extension"), spec)
        }
    }

    private fun get(uri: String): String =
        webTestClient.get().uri(uri)
            .header(HttpHeaders.AUTHORIZATION, TestSecuritySettings.clientAuthToken())
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()
            .responseBody!!
}

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

import com.fasterxml.jackson.databind.JsonNode
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.junit.After
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.TestSecuritySettings
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.WorkingFoldersInitializer
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.utility.DockerImageName
import java.io.ByteArrayOutputStream
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.streams.toList

/**
 * Boots the complete blueprints processor against real infrastructure: a MariaDB container as both the
 * primary database and the `processor-db` resource source, and a WireMock server as the `rest` resource
 * source. The infrastructure is started once per JVM and every subclass gets the same configuration, so
 * all ITs share one cached Spring context.
 */
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
    initializers = [
        WorkingFoldersInitializer::class,
        TestSecuritySettings.ServerContextInitializer::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
abstract class BaseE2eIT {

    @LocalServerPort
    private var httpPort: Int = 0

    protected val http: WebTestClient by lazy {
        WebTestClient.bindToServer()
            .baseUrl("http://localhost:$httpPort")
            .defaultHeader(HttpHeaders.AUTHORIZATION, TestSecuritySettings.clientAuthToken())
            .responseTimeout(Duration.ofMinutes(2))
            .build()
    }

    @After
    fun resetSouthbound() {
        wireMock.resetAll()
    }

    protected fun publish(version: String) {
        val body = MultipartBodyBuilder().apply {
            part(
                "file",
                object : ByteArrayResource(cba(version)) {
                    override fun getFilename() = "$BLUEPRINT_NAME.zip"
                }
            )
        }.build()
        http.post().uri("/api/v1/blueprint-model/publish")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
            .expectStatus().isOk
    }

    protected fun publishOnce(version: String = DEFAULT_VERSION) {
        synchronized(published) {
            if (published.add(version)) {
                publish(version)
            }
        }
    }

    protected fun process(request: JsonNode): ProcessResult {
        val result = http.post().uri("/api/v1/execution-service/process")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request.toString())
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        return ProcessResult(result.status.value(), JacksonUtils.jsonNode(result.responseBody!!))
    }

    protected fun resourceAssignmentRequest(
        hostname: String,
        requestId: String = uniqueId("req"),
        resolutionKey: String = uniqueId("key"),
        version: String = DEFAULT_VERSION
    ): JsonNode = executionRequest(
        "resource-assignment", requestId, version,
        mapOf(
            "resolution-key" to resolutionKey,
            "resource-assignment-properties" to mapOf("hostname" to hostname)
        )
    )

    protected fun executionRequest(
        actionName: String,
        requestId: String,
        version: String,
        requestPayload: Map<String, Any>
    ): JsonNode = JacksonUtils.objectMapper.valueToTree(
        mapOf(
            "commonHeader" to mapOf(
                "originatorId" to "e2e-it",
                "requestId" to requestId,
                "subRequestId" to "$requestId-sub"
            ),
            "actionIdentifiers" to mapOf(
                "blueprintName" to BLUEPRINT_NAME,
                "blueprintVersion" to version,
                "actionName" to actionName,
                "mode" to "sync"
            ),
            "payload" to mapOf("$actionName-request" to requestPayload)
        )
    )

    protected fun seedDevice(hostname: String, mgmtIp: String) {
        jdbc.update("INSERT INTO E2E_DEVICE (hostname, mgmt_ip) VALUES (?, ?)", hostname, mgmtIp)
    }

    data class ProcessResult(val httpStatus: Int, val body: JsonNode) {

        val statusCode: Int get() = body.at("/status/code").asInt()
        val errorMessage: String get() = body.at("/status/errorMessage").asText()
    }

    companion object {

        const val BLUEPRINT_NAME = "e2e-it"
        const val DEFAULT_VERSION = "1.0.0"
        const val REST_USERNAME = "e2e-inventory"
        const val REST_PASSWORD = "e2e-inventory-secret"

        private val mariaDb = MariaDBContainer<Nothing>(DockerImageName.parse("mariadb:11.7.2")).apply {
            withDatabaseName("sdnctl")
            withUsername("sdnctl")
            withPassword("sdnctl")
            start()
        }

        val wireMock = WireMockServer(wireMockConfig().dynamicPort()).apply { start() }

        val grpcPort: Int = ServerSocket(0).use { it.localPort }

        val jdbc = JdbcTemplate(DriverManagerDataSource(mariaDb.jdbcUrl, mariaDb.username, mariaDb.password)).apply {
            execute("CREATE TABLE E2E_DEVICE (hostname VARCHAR(64) PRIMARY KEY, mgmt_ip VARCHAR(64) NOT NULL)")
        }

        private val published = mutableSetOf<String>()

        @JvmStatic
        @DynamicPropertySource
        fun infrastructureProperties(registry: DynamicPropertyRegistry) {
            registry.add("blueprintsprocessor.db.url") { mariaDb.jdbcUrl }
            registry.add("blueprintsprocessor.db.username") { mariaDb.username }
            registry.add("blueprintsprocessor.db.password") { mariaDb.password }
            registry.add("blueprintsprocessor.db.driverClassName") { "org.mariadb.jdbc.Driver" }
            registry.add("blueprintsprocessor.db.hibernateDialect") { "org.hibernate.dialect.MariaDBDialect" }

            registry.add("blueprintsprocessor.db.processor-db.type") { "maria-db" }
            registry.add("blueprintsprocessor.db.processor-db.url") { mariaDb.jdbcUrl }
            registry.add("blueprintsprocessor.db.processor-db.username") { mariaDb.username }
            registry.add("blueprintsprocessor.db.processor-db.password") { mariaDb.password }

            registry.add("blueprintsprocessor.restclient.rest.type") { "basic-auth" }
            registry.add("blueprintsprocessor.restclient.rest.url") { wireMock.baseUrl() }
            registry.add("blueprintsprocessor.restclient.rest.username") { REST_USERNAME }
            registry.add("blueprintsprocessor.restclient.rest.password") { REST_PASSWORD }

            registry.add("blueprintsprocessor.grpcPort") { grpcPort }
        }

        fun uniqueId(prefix: String) = "$prefix-${UUID.randomUUID()}"

        /** Zips the CBA from the test resources, stamped with [version] so tests can publish it side by side. */
        fun cba(version: String): ByteArray {
            val root = Paths.get(BaseE2eIT::class.java.getResource("/$BLUEPRINT_NAME")!!.toURI())
            val files = Files.walk(root).use { paths -> paths.filter { Files.isRegularFile(it) }.sorted().toList() }
            return ByteArrayOutputStream().use { bytes ->
                ZipOutputStream(bytes).use { zip ->
                    files.forEach { file ->
                        val name = root.relativize(file).toString().replace('\\', '/')
                        val content = String(Files.readAllBytes(file))
                            .replace("Template-Version: $DEFAULT_VERSION", "Template-Version: $version")
                            .replace("\"template_version\" : \"$DEFAULT_VERSION\"", "\"template_version\" : \"$version\"")
                        zip.putNextEntry(ZipEntry(name))
                        zip.write(content.toByteArray())
                        zip.closeEntry()
                    }
                }
                bytes.toByteArray()
            }
        }
    }
}

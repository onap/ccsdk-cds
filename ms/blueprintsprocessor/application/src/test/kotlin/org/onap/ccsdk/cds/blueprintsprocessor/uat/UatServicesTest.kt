/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.cds.blueprintsprocessor.uat

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.VerificationException
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.request
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalToIgnoringCase
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor.COLOR_WIREMOCK
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor.markerOf
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.MarkedSlf4jNotifier
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.ServiceDefinition
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.TestSecuritySettings
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.UatDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.UAT_SPECIFICATION_FILE
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintArchiveUtils.Companion.compressToBytes
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.support.TestPropertySourceUtils.INLINED_PROPERTIES_PROPERTY_SOURCE_NAME
import org.yaml.snakeyaml.Yaml
import java.nio.file.Paths
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

fun String.prefixIfNot(prefix: String) =
    if (this.startsWith(prefix)) this else "$prefix$this"

@ActiveProfiles("uat")
@Suppress("MemberVisibilityCanBePrivate")
class UatServicesTest : BaseUatTest() {

    companion object {

        private const val BLUEPRINT_NAME = "pnf_config"
        private val BLUEPRINT_BASE_DIR = Paths.get(UAT_BLUEPRINTS_BASE_DIR, BLUEPRINT_NAME)
        private val UAT_PATH = BLUEPRINT_BASE_DIR.resolve(UAT_SPECIFICATION_FILE)
        private val wireMockMarker = markerOf(COLOR_WIREMOCK)
    }

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var environment: ConfigurableEnvironment

    private val ephemeralProperties = mutableSetOf<String>()
    private val startedMockServers = mutableListOf<WireMockServer>()

    private fun setProperties(properties: Map<String, String>) {
        inlinedPropertySource().putAll(properties)
        ephemeralProperties += properties.keys
    }

    @AfterTest
    fun resetProperties() {
        val source = inlinedPropertySource()
        ephemeralProperties.forEach { key -> source.remove(key) }
        ephemeralProperties.clear()
    }

    @AfterTest
    fun stopMockServers() {
        startedMockServers.forEach { mockServer ->
            try {
                mockServer.checkForUnmatchedRequests()
            } finally {
                mockServer.stop()
            }
        }
        startedMockServers.clear()
    }

    private fun inlinedPropertySource(): MutableMap<String, Any> =
        (environment.propertySources[INLINED_PROPERTIES_PROPERTY_SOURCE_NAME] as MapPropertySource).source

    @LocalServerPort
    var localServerPort: Int = 0

    // use lazy evaluation to postpone until localServerPort is injected by Spring
    val baseUrl: String by lazy {
        "http://127.0.0.1:$localServerPort"
    }

    lateinit var httpClient: CloseableHttpClient

    @BeforeTest
    fun setupHttpClient() {
        val defaultHeaders = listOf(
            BasicHeader(
                org.apache.http.HttpHeaders.AUTHORIZATION,
                TestSecuritySettings.clientAuthToken()
            )
        )
        httpClient = HttpClientBuilder.create()
            .setDefaultHeaders(defaultHeaders)
            .build()
    }

    @Test
    fun `verify service validates candidate UAT`() {
        // GIVEN
        val cbaBytes = compressToBytes(BLUEPRINT_BASE_DIR)
        val multipartEntity = MultipartEntityBuilder.create()
            .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            .addBinaryBody("cba", cbaBytes, ContentType.DEFAULT_BINARY, "cba.zip")
            .build()
        val request = HttpPost("$baseUrl/api/v1/uat/verify").apply {
            entity = multipartEntity
        }

        // WHEN
        httpClient.execute(request) { response ->

            // THEN
            val statusLine = response.statusLine
            assertThat(statusLine.statusCode, CoreMatchers.equalTo(HttpStatus.SC_OK))
        }
    }

    @Test
    fun `spy service generates complete UAT from bare UAT`() {
        // GIVEN
        val uatSpec = UAT_PATH.toFile().readText()
        val fullUat = UatDefinition.load(mapper, uatSpec)
        val expectedJson = mapper.writeValueAsString(fullUat)

        val bareUatBytes = fullUat.toBare().dump(mapper).toByteArray()

        fullUat.externalServices.forEach { service ->
            val mockServer = createMockServer(service)
            mockServer.start()
            startedMockServers += mockServer
            setPropertiesForMockServer(service, mockServer)
        }

        val cbaBytes = compressToBytes(BLUEPRINT_BASE_DIR)
        val multipartEntity = MultipartEntityBuilder.create()
            .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            .addBinaryBody("cba", cbaBytes, ContentType.DEFAULT_BINARY, "cba.zip")
            .addBinaryBody("uat", bareUatBytes, ContentType.DEFAULT_BINARY, "uat.yaml")
            .build()
        val request = HttpPost("$baseUrl/api/v1/uat/spy").apply {
            entity = multipartEntity
        }

        // WHEN
        httpClient.execute(request) { response ->

            // THEN
            val statusLine = response.statusLine
            assertThat(statusLine.statusCode, CoreMatchers.equalTo(HttpStatus.SC_OK))
            val entity = response.entity
            assertNotNull(entity)
            val contentType = ContentType.get(entity)
            assertThat(contentType.mimeType, equalToIgnoringCase("text/vnd.yaml"))
            val yamlResponse = entity.content.bufferedReader().readText()
            val jsonResponse = yamlToJson(yamlResponse)
            JSONAssert.assertEquals(expectedJson, jsonResponse, JSONCompareMode.LENIENT)
        }
    }

    private fun createMockServer(service: ServiceDefinition): WireMockServer {
        val mockServer = WireMockServer(
            wireMockConfig()
                .dynamicPort()
                .notifier(MarkedSlf4jNotifier(wireMockMarker))
        )
        service.expectations.forEach { expectation ->

            val request = expectation.request
            // WebTestClient always use absolute path, prefixing with "/" if necessary
            val urlPattern = urlEqualTo(request.path.prefixIfNot("/"))
            val mappingBuilder: MappingBuilder = request(request.method, urlPattern)
            request.headers.forEach { (key, value) ->
                mappingBuilder.withHeader(key, equalTo(value))
            }
            if (request.body != null) {
                mappingBuilder.withRequestBody(equalToJson(mapper.writeValueAsString(request.body), true, true))
            }

            for (response in expectation.responses) {
                val responseDefinitionBuilder: ResponseDefinitionBuilder = aResponse()
                    .withStatus(response.status)
                if (response.body != null) {
                    responseDefinitionBuilder.withBody(mapper.writeValueAsBytes(response.body))
                        .withHeaders(
                            HttpHeaders(
                                response.headers.entries.map { e -> HttpHeader(e.key, e.value) }
                            )
                        )
                }

                // TODO: MockServer verification for multiple responses should be done using Wiremock scenarios
                mappingBuilder.willReturn(responseDefinitionBuilder)
            }

            mockServer.stubFor(mappingBuilder)
        }
        return mockServer
    }

    private fun setPropertiesForMockServer(service: ServiceDefinition, mockServer: WireMockServer) {
        val selector = service.selector
        val httpPort = mockServer.port()
        val properties = mapOf(
            "blueprintsprocessor.restclient.$selector.type" to "basic-auth",
            "blueprintsprocessor.restclient.$selector.url" to "http://localhost:$httpPort/",
            // TODO credentials should be validated
            "blueprintsprocessor.restclient.$selector.username" to "admin",
            "blueprintsprocessor.restclient.$selector.password" to "Kp8bJ4SXszM0WXlhak3eHlcse2gAw84vaoGGmJvUy2U"
        )
        setProperties(properties)
    }

    /**
     * Borrowed from com.github.tomakehurst.wiremock.junit.WireMockRule.checkForUnmatchedRequests
     */
    private fun WireMockServer.checkForUnmatchedRequests() {
        val unmatchedRequests = findAllUnmatchedRequests()
        if (unmatchedRequests.isNotEmpty()) {
            val nearMisses = findNearMissesForAllUnmatchedRequests()
            if (nearMisses.isEmpty()) {
                throw VerificationException.forUnmatchedRequests(unmatchedRequests)
            } else {
                throw VerificationException.forUnmatchedNearMisses(nearMisses)
            }
        }
    }

    private fun yamlToJson(yaml: String): String {
        val map: Map<String, Any> = Yaml().load(yaml)
        return mapper.writeValueAsString(map)
    }
}

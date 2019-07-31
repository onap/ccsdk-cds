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
package org.onap.ccsdk.cds.blueprintsprocessor

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.atLeast
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.ClassRule
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService.WebClientResponse
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.rules.SpringClassRule
import org.springframework.test.context.junit4.rules.SpringMethodRule
import org.springframework.test.web.reactive.server.WebTestClient
import org.yaml.snakeyaml.Yaml
import reactor.core.publisher.Mono
import java.io.File
import java.io.FileReader
import java.nio.file.Paths
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(Parameterized::class)
// Set blueprintsprocessor.httpPort=0 to trigger a random port selection
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient(timeout = "PT10S")
@ContextConfiguration(initializers = [
    WorkingFoldersInitializer::class,
    TestSecuritySettings.ServerContextInitializer::class
])
@TestPropertySource(locations = ["classpath:application-test.properties"])
@Suppress("UNCHECKED_CAST")
class BlueprintsAcceptanceTests(private val blueprintName: String, private val filename: String) {

    companion object {
        const val BLUEPRINTS_BASE_DIR = "src/test/resources/blueprints"

        @ClassRule
        @JvmField
        val springClassRule = SpringClassRule()

        val log: Logger = LoggerFactory.getLogger(BlueprintsAcceptanceTests::class.java)

        @Parameterized.Parameters(name = "{index} {0}")
        @JvmStatic
        fun filenames(): List<Array<String>> {
            return File(BLUEPRINTS_BASE_DIR)
                    .listFiles { file -> file.isFile && file.extension == "yaml" }
                    ?.map { file -> arrayOf(file.nameWithoutExtension, file.canonicalPath) }
                    ?: emptyList()
        }
    }

    @Rule
    @JvmField
    val springMethodRule = SpringMethodRule()

    @MockBean(name = RestLibConstants.SERVICE_BLUEPRINT_REST_LIB_PROPERTY)
    lateinit var restClientFactory: BluePrintRestLibPropertyService

    @Autowired
    // Bean is created programmatically by {@link WorkingFoldersInitializer#initialize(String)}
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    lateinit var tempFolder: ExtendedTemporaryFolder

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var mapper: ObjectMapper

    @BeforeTest
    fun cleanupTemporaryFolder() {
        tempFolder.deleteAllFiles()
    }

    @BeforeTest
    fun resetMocks() {
        reset(restClientFactory)
    }

    @Test
    fun testBlueprint() {
        val yaml: Map<String, *> = loadYaml()

        uploadBlueprint(blueprintName)

        // Configure mocked external services
        val services = yaml["external-services"] as List<Map<String, *>>? ?: emptyList()
        val expectationPerClient = services.map { service ->
            val selector = service["selector"] as String
            val expectations = (service["expectations"] as List<Map<String, *>>).map {
                parseExpectation(it)
            }
            val mockClient = createRestClientMock(selector, expectations)
            mockClient to expectations
        }.toMap()

        // Run processes
        for (process in (yaml["processes"] as List<Map<String, *>>)) {
            val processName = process["name"]
            log.info("Executing process '$processName'")
            val request = mapper.writeValueAsString(process["request"])
            val expectedResponse = mapper.writeValueAsString(process["expectedResponse"])
            processBlueprint(request, expectedResponse)
        }

        // Validate request payloads
        for ((mockClient, expectations) in expectationPerClient) {
            expectations.forEach { expectation ->
                verify(mockClient, atLeastOnce()).exchangeResource(
                        eq(expectation.method),
                        eq(expectation.path),
                        argThat { assertJsonEqual(expectation.expectedRequestBody, this) },
                        expectation.requestHeadersMatcher())
            }
            // Don't mind the invocations to the overloaded exchangeResource(String, String, String)
            verify(mockClient, atLeast(0)).exchangeResource(any(), any(), any())
            verifyNoMoreInteractions(mockClient)
        }
    }

    private fun createRestClientMock(selector: String, restExpectations: List<RestExpectation>): BlueprintWebClientService {
        val restClient = mock<BlueprintWebClientService>(verboseLogging = true)

        // Delegates to overloaded exchangeResource(String, String, String, Map<String, String>)
        whenever(restClient.exchangeResource(any(), any(), any()))
                .thenAnswer { invocation ->
                    val method = invocation.arguments[0] as String
                    val path = invocation.arguments[1] as String
                    val request = invocation.arguments[2] as String
                    restClient.exchangeResource(method, path, request, emptyMap())
                }
        for (expectation in restExpectations) {
            whenever(restClient.exchangeResource(
                    eq(expectation.method),
                    eq(expectation.path),
                    any(),
                    any()))
                    .thenReturn(WebClientResponse(expectation.statusCode, expectation.responseBody))
        }

        whenever(restClientFactory.blueprintWebClientService(selector))
                .thenReturn(restClient)
        return restClient
    }

    private fun uploadBlueprint(blueprintName: String) {
        val body = toMultiValueMap("file", getBlueprintAsResource(blueprintName))
        webTestClient
                .post()
                .uri("/api/v1/execution-service/upload")
                .header("Authorization", TestSecuritySettings.clientAuthToken())
                .syncBody(body)
                .exchange()
                .expectStatus().isOk
    }

    private fun processBlueprint(request: String, expectedResponse: String) {
        webTestClient
                .post()
                .uri("/api/v1/execution-service/process")
                .header("Authorization", TestSecuritySettings.clientAuthToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(request), String::class.java)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(expectedResponse)
    }

    private fun getBlueprintAsResource(blueprintName: String): Resource {
        val baseDir = Paths.get(BLUEPRINTS_BASE_DIR, blueprintName)
        val zipBytes = zipFolder(baseDir)
        return object : ByteArrayResource(zipBytes) {
            // Filename has to be returned in order to be able to post
            override fun getFilename() = "$blueprintName.zip"
        }
    }

    private fun loadYaml(): Map<String, Any> {
        return FileReader(filename).use { reader ->
            Yaml().load(reader)
        }
    }

    private fun assertJsonEqual(expected: Any, actual: String): Boolean {
        if (actual != expected) {
            // assertEquals throws an exception whenever match fails
            JSONAssert.assertEquals(mapper.writeValueAsString(expected), actual, JSONCompareMode.LENIENT)
        }
        return true
    }

    private fun parseExpectation(expectation: Map<String, *>): RestExpectation {
        val request = expectation["request"] as Map<String, Any>
        val method = request["method"] as String
        val path = joinPath(request.getValue("path"))
        val contentType = request["content-type"] as String?
        val requestBody = request.getOrDefault("body", "")

        val response = expectation["response"] as Map<String, Any>? ?: emptyMap()
        val status = response["status"] as Int? ?: 200
        val responseBody = when (val body = response["body"] ?: "") {
            is String -> body
            else -> mapper.writeValueAsString(body)
        }

        return RestExpectation(method, path, contentType, requestBody, status, responseBody)
    }

    /**
     * Join a multilevel lists of strings.
     * Example: joinPath(listOf("a", listOf("b", "c"), "d")) will result in "a/b/c/d".
     */
    private fun joinPath(any: Any): String {
        fun recursiveJoin(any: Any, sb: StringBuilder): StringBuilder {
            when (any) {
                is List<*> -> any.filterNotNull().forEach { recursiveJoin(it, sb) }
                is String -> {
                    if (sb.isNotEmpty()) {
                        sb.append('/')
                    }
                    sb.append(any)
                }
                else -> throw IllegalArgumentException("Unsupported type: ${any.javaClass}")
            }
            return sb
        }

        return recursiveJoin(any, StringBuilder()).toString()
    }

    data class RestExpectation(val method: String, val path: String, val contentType: String?,
                               val expectedRequestBody: Any,
                               val statusCode: Int, val responseBody: String) {

        fun requestHeadersMatcher(): Map<String, String> {
            return if (contentType != null) eq(mapOf("Content-Type" to contentType)) else any()
        }
    }
}
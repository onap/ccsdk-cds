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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.MissingNode
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.atLeast
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
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
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintArchiveUtils.Companion.compressToBytes
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
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import kotlin.test.BeforeTest
import kotlin.test.Test

// Only one runner can be configured with jUnit 4. We had to replace the SpringRunner by equivalent jUnit rules.
// See more on https://docs.spring.io/autorepo/docs/spring-framework/current/spring-framework-reference/testing.html#testcontext-junit4-rules
@RunWith(Parameterized::class)
// Set blueprintsprocessor.httpPort=0 to trigger a random port selection
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient(timeout = "PT10S")
@ContextConfiguration(initializers = [
    WorkingFoldersInitializer::class,
    TestSecuritySettings.ServerContextInitializer::class
])
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BlueprintsAcceptanceTest(private val blueprintName: String, private val filename: String) {

    companion object {
        const val UAT_BLUEPRINTS_BASE_DIR = "../../../components/model-catalog/blueprint-model/uat-blueprints"
        const val EMBEDDED_UAT_FILE = "Tests/uat.yaml"

        @ClassRule
        @JvmField
        val springClassRule = SpringClassRule()

        val log: Logger = LoggerFactory.getLogger(BlueprintsAcceptanceTest::class.java)

        /**
         * Generates the parameters to create a test instance for every blueprint found under UAT_BLUEPRINTS_BASE_DIR
         * that contains the proper UAT definition file.
         */
        @Parameterized.Parameters(name = "{index} {0}")
        @JvmStatic
        fun testParameters(): List<Array<String>> {
            return File(UAT_BLUEPRINTS_BASE_DIR)
                    .listFiles { file -> file.isDirectory && File(file, EMBEDDED_UAT_FILE).isFile }
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

    @Test
    fun testBlueprint() {
        val uat = UatDefinition.load(mapper, Paths.get(filename, EMBEDDED_UAT_FILE))

        uploadBlueprint(blueprintName)

        // Configure mocked external services
        val expectationPerClient = uat.externalServices.associateBy(
                { service -> createRestClientMock(service.selector, service.expectations) },
                { service -> service.expectations }
        )

        // Run processes
        for (process in uat.processes) {
            log.info("Executing process '${process.name}'")
            processBlueprint(process.request, process.expectedResponse,
                    JsonNormalizer.getNormalizer(mapper, process.responseNormalizerSpec))
        }

        // Validate request payloads to external services
        for ((mockClient, expectations) in expectationPerClient) {
            expectations.forEach { expectation ->
                verify(mockClient, atLeastOnce()).exchangeResource(
                        eq(expectation.request.method),
                        eq(expectation.request.path),
                        argThat { assertJsonEqual(expectation.request.body, this) },
                        expectation.request.requestHeadersMatcher())
            }
            // Don't mind the invocations to the overloaded exchangeResource(String, String, String)
            verify(mockClient, atLeast(0)).exchangeResource(any(), any(), any())
            verifyNoMoreInteractions(mockClient)
        }
    }

    private fun createRestClientMock(selector: String, restExpectations: List<ExpectationDefinition>)
            : BlueprintWebClientService {
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
                    eq(expectation.request.method),
                    eq(expectation.request.path),
                    any(),
                    any()))
                    .thenReturn(WebClientResponse(expectation.response.status, expectation.response.body.toString()))
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

    private fun processBlueprint(request: JsonNode, expectedResponse: JsonNode,
                                 responseNormalizer: (String) -> String) {
        webTestClient
                .post()
                .uri("/api/v1/execution-service/process")
                .header("Authorization", TestSecuritySettings.clientAuthToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(request.toString()), String::class.java)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .consumeWith { response ->
                    assertJsonEqual(expectedResponse, responseNormalizer(getBodyAsString(response)))
                }
    }

    private fun getBlueprintAsResource(blueprintName: String): Resource {
        val baseDir = Paths.get(UAT_BLUEPRINTS_BASE_DIR, blueprintName)
        val zipBytes = compressToBytes(baseDir)
        return object : ByteArrayResource(zipBytes) {
            // Filename has to be returned in order to be able to post
            override fun getFilename() = "$blueprintName.zip"
        }
    }

    private fun assertJsonEqual(expected: JsonNode, actual: String): Boolean {
        if ((actual == "") && (expected is MissingNode)) {
            return true
        }
        JSONAssert.assertEquals(expected.toString(), actual, JSONCompareMode.LENIENT)
        // assertEquals throws an exception whenever match fails
        return true
    }

    private fun getBodyAsString(result: EntityExchangeResult<ByteArray>): String {
        val body = result.responseBody
        if ((body == null) || body.isEmpty()) {
            return ""
        }
        val charset = result.responseHeaders.contentType?.charset ?: StandardCharsets.UTF_8
        return String(body, charset)
    }
}
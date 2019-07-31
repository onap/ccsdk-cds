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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService.*
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
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.nio.file.Paths
import kotlin.test.BeforeTest

/**
 * - Create a folder named after your blueprint under 'src/test/resources/blueprints'
 *
 * - Avoid using multiple mocking frameworks. Prefer using Mockito mocks since they integrate better with Spring Boot.
 *   To avoid some well-known issues when using Mockito with Kotlin, the mockito-kotlin library can be handful.
 *
 * - Always use real components to play with a most realistic environment possible. Using a uniform Spring Context also
 *   reduces test feedback time. The only exception to this rule is the BluePrintRestLibPropertyService component
 *   that needs to be mocked to provided mocked REST clients for external services.
 */
@RunWith(SpringRunner::class)
// Set blueprintsprocessor.httpPort=0 to trigger a random port selection
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient(timeout = "PT10S")
@ContextConfiguration(initializers = [
    WorkingFoldersInitializer::class,
    TestSecuritySettings.ServerContextInitializer::class
])
@TestPropertySource(locations = ["classpath:application-test.properties"])
abstract class AbstractBlueprintTest {

    @MockBean(name = RestLibConstants.SERVICE_BLUEPRINT_REST_LIB_PROPERTY)
    lateinit var restClientFactory: BluePrintRestLibPropertyService

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    // Bean is created programmatically by {@link WorkingFoldersInitializer#initialize(String)}
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    lateinit var tempFolder: ExtendedTemporaryFolder

    companion object {
        val log: Logger = LoggerFactory.getLogger(AbstractBlueprintTest::class.java)
    }

    // ====================================
    // JUnit Lifecycle Methods
    // ====================================

    @BeforeTest
    fun cleanupTemporaryFolder() {
        tempFolder.deleteAllFiles()
    }

    @BeforeTest
    fun resetMocks() {
        reset(restClientFactory)
    }

    // ====================================
    // Common Methods
    // ====================================

    fun createRestClientMock(selector: String, vararg restExpectations: RestExpectation): BlueprintWebClientService {
        val restClient = mock<BlueprintWebClientService>()

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
                    .thenAnswer { invocation ->
                        val response = WebClientResponse(expectation.statusCode, expectation.responseBody)
                        log.info("Rest request($selector):\n${invocation.arguments.asList()}\nRest response: $response")
                        response
                    }
        }

        whenever(restClientFactory.blueprintWebClientService(selector))
                .thenReturn(restClient)
        return restClient
    }

    fun uploadBlueprint(blueprintName: String) {
        val body = toMultiValueMap("file", getBlueprintAsResource(blueprintName))
        webTestClient
                .post()
                .uri("/api/v1/execution-service/upload")
                .header("Authorization", TestSecuritySettings.clientAuthToken())
                .syncBody(body)
                .exchange()
                .expectStatus().isOk
    }

    fun processBlueprint(request: String, expectedResponse: String) {
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
        val baseDir = Paths.get("src/test/resources/blueprints", blueprintName)
        val zipBytes = zipFolder(baseDir)
        return object : ByteArrayResource(zipBytes) {
            // Filename has to be returned in order to be able to post
            override fun getFilename() = "$blueprintName.zip"
        }
    }

    data class RestExpectation(val method: String, val path: String, val statusCode: Int, val responseBody: String = "")
}

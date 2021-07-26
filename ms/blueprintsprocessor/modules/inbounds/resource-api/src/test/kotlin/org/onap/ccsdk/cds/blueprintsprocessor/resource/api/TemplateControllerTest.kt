/*
 * Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.resource.api

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import kotlin.test.AfterTest

@RunWith(SpringRunner::class)
@WebFluxTest
@ContextConfiguration(
    classes = [
        TestDatabaseConfiguration::class, BluePrintCoreConfiguration::class,
        BluePrintCatalogService::class, ErrorCatalogTestConfiguration::class
    ]
)
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@TestPropertySource(locations = ["classpath:application-test.properties"])
class TemplateControllerTest {

    private val log = LoggerFactory.getLogger(TemplateControllerTest::class.toString())

    @Autowired
    lateinit var webTestClient: WebTestClient

    var resolutionKey = "7cafa9f3-bbc8-49ec-8f25-fcaa6ac3ff08"
    val blueprintName = "baseconfiguration"
    val blueprintVersion = "1.0.0"
    val templatePrefix = "activate"
    val payloadDummyTemplateData = "PAYLOAD DATA"

    var requestArguments = "bpName=$blueprintName&bpVersion=$blueprintVersion" +
        "&artifactName=$templatePrefix&resolutionKey=$resolutionKey"

    @AfterTest
    fun cleanDir() {
        deleteDir("target", "blueprints")
    }

    @Test
    fun `ping return Success`() {
        runBlocking {
            webTestClient.get().uri("/api/v1/template/health-check")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .equals("Success")
        }
    }

    @Test
    fun `store same value and tries to retrieve - duplicate entry execption`() {
        runBlocking {

            resolutionKey = "1"

            post(resolutionKey)
            post(resolutionKey)

            webTestClient
                .get()
                .uri("/api/v1/template?$requestArguments")
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody().equals(payloadDummyTemplateData)
        }
    }

    @Test
    fun `get returns requested JSON content-type`() {
        runBlocking {
            resolutionKey = "2"
            post(resolutionKey)
            get("json", resolutionKey)
        }
    }

    @Test
    fun `get returns requested XML content-type`() {
        runBlocking {
            resolutionKey = "3"
            post(resolutionKey)
            get("xml", resolutionKey)
        }
    }

    @Test
    fun `get returns 400 error if missing arg`() {
        runBlocking {
            val arguments = "bpBADName=$blueprintName" +
                "&bpBADVersion=$blueprintVersion" +
                "&artifactName=$templatePrefix" +
                "&resolutionKey=$resolutionKey"

            webTestClient.get().uri("/api/v1/template?$arguments")
                .exchange()
                .expectStatus().isNotFound
        }
    }

    @Test
    fun `get returns 404 if entry not found`() {
        runBlocking {

            webTestClient
                .get()
                .uri(
                    "/api/v1/template?bpName=$blueprintName&bpVersion=$blueprintVersion" +
                        "&artifactName=$templatePrefix&resolutionKey=notFound"
                )
                .exchange()
                .expectStatus().isNotFound
        }
    }

    private fun post(resKey: String) {
        webTestClient
            .post()
            .uri("/api/v1/template/$blueprintName/$blueprintVersion/$templatePrefix/$resKey")
            .body(BodyInserters.fromObject(payloadDummyTemplateData))
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody()
            .consumeWith {
                log.info("Stored result under UUID ${it.responseBody}")
            }
    }

    private fun get(expectedType: String, resKey: String) {
        var requestArguments = "bpName=$blueprintName&bpVersion=$blueprintVersion" +
            "&artifactName=$templatePrefix&resolutionKey=$resKey"

        if (expectedType.isNotEmpty()) {
            requestArguments = "$requestArguments&format=$expectedType"
            webTestClient
                .get()
                .uri("/api/v1/template?$requestArguments")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectHeader().contentType(MediaType.valueOf("application/$expectedType"))
                .expectBody().equals(payloadDummyTemplateData)
        } else {
            webTestClient
                .get()
                .uri("/api/v1/template?$requestArguments")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectHeader().contentType(MediaType.TEXT_PLAIN)
                .expectBody().equals(payloadDummyTemplateData)
        }
    }
}

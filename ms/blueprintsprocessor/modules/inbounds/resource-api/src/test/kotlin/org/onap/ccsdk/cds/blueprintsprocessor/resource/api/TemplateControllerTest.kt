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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.io.File
import java.nio.file.Paths
import kotlin.test.AfterTest
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@WebFluxTest
@ContextConfiguration(classes = [BluePrintCoreConfiguration::class,
    BluePrintCatalogService::class, SecurityProperties::class])
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

    @AfterTest
    fun cleanDir() {
        deleteDir("target", "blueprints")
    }

    @Test
    fun `ping return Success`() {
        runBlocking {
            webTestClient.get().uri("/api/v1/template/ping")
                .exchange()
                .expectStatus().isOk
                .expectBody().equals("Success")
        }
    }

    @Test
    fun `store-retrieve result by path or UUID`() {
        runBlocking {
            createRetrieve()
        }
    }

    @Test
    fun `get returns requested JSON content-type`() {
        runBlocking {
            createRetrieve("json")
        }
    }

    @Test
    fun `get returns requested XML content-type`() {
        runBlocking {
            createRetrieve("xml")
        }
    }

    private fun createRetrieve(expectedType: String? = null) {
        var requestArguments = "bpName=$blueprintName&bpVersion=$blueprintVersion" +
                "&artifactName=$templatePrefix&resolutionKey=$resolutionKey"

        // Store new result for blueprint/artifact/resolutionkey
        webTestClient
            .post()
            .uri("/api/v1/template?$requestArguments")
            .body(BodyInserters.fromObject(payloadDummyTemplateData))
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody()

        // Retrieve same payload
        if (expectedType != null) {
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

    /*
     * Error cases
     */
    @Test
    fun `get returns 400 error if missing arg`() {
        runBlocking {
            val arguments = "bpBADName=$blueprintName" +
                    "&bpBADVersion=$blueprintVersion" +
                    "&artifactName=$templatePrefix" +
                    "&resolutionKey=$resolutionKey"

            webTestClient.get().uri("/api/v1/template?$arguments")
                .exchange()
                .expectStatus().isBadRequest
        }
    }

    @Test
    fun `get returns 404 if entry not found`() {
        runBlocking {

            webTestClient
                .get()
                .uri("/api/v1/template?bpName=$blueprintName&bpVersion=$blueprintVersion" +
                        "&artifactName=$templatePrefix&resolutionKey=$resolutionKey")
                .exchange()
                .expectStatus().isNotFound
        }
    }
}
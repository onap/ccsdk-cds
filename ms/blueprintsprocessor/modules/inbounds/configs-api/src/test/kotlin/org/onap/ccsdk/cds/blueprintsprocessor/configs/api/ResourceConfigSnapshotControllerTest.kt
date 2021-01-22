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

package org.onap.ccsdk.cds.blueprintsprocessor.configs.api

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintCoreConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintCatalogService
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

@RunWith(SpringRunner::class)
@WebFluxTest
@ContextConfiguration(
    classes = [BlueprintCoreConfiguration::class, BlueprintCatalogService::class, ErrorCatalogTestConfiguration::class]
)
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@TestPropertySource(locations = ["classpath:application-test.properties"])
class ResourceConfigSnapshotControllerTest {

    private val log = LoggerFactory.getLogger(ResourceConfigSnapshotControllerTest::class.toString())

    @Autowired
    lateinit var webTestClient: WebTestClient

    val resourceId = "fcaa6ac3ff08"
    val resourceType = "PNF"
    val snapshotData = "PAYLOAD DATA"

    var requestArguments = "resourceId=$resourceId&resourceType=$resourceType"

    @Test
    fun `ping return Success`() {
        runBlocking {
            webTestClient.get().uri("/api/v1/configs/health-check")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .equals("Success")
        }
    }

    @Test
    fun `update configuration is allowed and updates timestamp`() {
        runBlocking {

            webTestClient
                .post()
                .uri("/api/v1/configs/$resourceType/$resourceId/running")
                .body(BodyInserters.fromObject(snapshotData))
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.createdDate")
                .value<String> { println(it) }

            webTestClient
                .post()
                .uri("/api/v1/configs/$resourceType/$resourceId/running")
                .body(BodyInserters.fromObject(snapshotData))
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.createdDate")
                .value<String> { println(it) }
        }
    }

    @Test
    fun `get returns requested JSON content-type`() {
        runBlocking {
            post(resourceType, "22", "RUNNING")
            get("json", resourceType, "22", "RUNNING")
        }
    }

    @Test
    fun `get returns requested XML content-type`() {
        runBlocking {
            post(resourceType, "3", "CANDIDATE")
            get("xml", resourceType, "3", "CANDIDATE")
        }
    }

    @Test
    fun `get returns 400 error if missing arg`() {
        runBlocking {
            val arguments = "artifactName=WRONGARG1&resolutionKey=WRONGARG1"

            webTestClient.get().uri("/api/v1/configs?$arguments")
                .exchange()
                .expectStatus().is4xxClientError
        }
    }

    @Test
    fun `get returns 400 error if wrong Status arg`() {
        runBlocking {
            val arguments = "resourceId=MISSING&resourceType=PNF&status=TOTALLY_WRONG"

            webTestClient.get().uri("/api/v1/configs?$arguments")
                .exchange()
                .expectStatus().isBadRequest
        }
    }

    @Test
    fun `get returns 200 if entry not found`() {
        runBlocking {

            webTestClient
                .get()
                .uri("/api/v1/configs?resourceId=MISSING&resourceType=PNF")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
        }
    }

    @Test
    fun `getAllByID returns 200 if entries found`() {
        runBlocking {
            post(resourceType, "3", "RUNNING")
            post(resourceType, "2", "RUNNING")
            post(resourceType, resourceId, "RUNNING")

            webTestClient
                .get()
                .uri("/api/v1/configs/allByID?resourceId=$resourceId")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.length()")
                .isEqualTo(1)
        }
    }

    @Test
    fun `getAllByID with CANDIDATE status returns 200 if entries found`() {
        runBlocking {
            post(resourceType, "3", "RUNNING")
            post(resourceType, "2", "RUNNING")
            post(resourceType, resourceId, "CANDIDATE")

            webTestClient
                .get()
                .uri("/api/v1/configs/allByID?resourceId=$resourceId&status=CANDIDATE")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.length()")
                .isEqualTo(1)
        }
    }

    @Test
    fun `getAllByID returns 400 error if missing parameter`() {
        runBlocking {

            webTestClient
                .get()
                .uri("/api/v1/configs/allByID")
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
        }
    }

    @Test
    fun `getAllByID returns 400 error if wrong status parameter`() {
        runBlocking {

            webTestClient
                .get()
                .uri("/api/v1/configs/allByID?resourceId=$resourceId&status=NOTGOOD")
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
        }
    }

    @Test
    fun `getAllByType returns 200 if entries found`() {
        runBlocking {
            post(resourceType, "3", "RUNNING")
            post(resourceType + "DIFF", "2", "RUNNING")
            post(resourceType, "1", "RUNNING")

            webTestClient
                .get()
                .uri("/api/v1/configs/allByType?resourceType=$resourceType")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.length()")
                .isEqualTo(3)
        }
    }

    @Test
    fun `getAllByType returns 400 error if missing parameter`() {
        runBlocking {

            webTestClient
                .get()
                .uri("/api/v1/configs/allByType")
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
        }
    }

    @Test
    fun `getAllByType returns 400 error if wrong status parameter`() {
        runBlocking {

            webTestClient
                .get()
                .uri("/api/v1/configs/allByType?resourceType=$resourceType&status=NOTGOOD")
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
        }
    }

    private fun post(resourceType: String, resourceId: String, status: String) {
        webTestClient
            .post()
            .uri("/api/v1/configs/$resourceType/$resourceId/$status")
            .body(BodyInserters.fromObject(snapshotData))
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody()
    }

    private fun get(expectedType: String, resourceType: String, resourceId: String, status: String) {
        var requestArguments = "resourceId=$resourceId&resourceType=$resourceType&status=$status"

        if (expectedType.isNotEmpty()) {
            requestArguments = "$requestArguments&format=$expectedType"
            webTestClient
                .get()
                .uri("/api/v1/configs?$requestArguments")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectHeader().contentType(MediaType.valueOf("application/$expectedType"))
                .expectBody().equals(snapshotData)
        } else {
            webTestClient
                .get()
                .uri("/api/v1/configs?$requestArguments")
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectHeader().contentType(MediaType.TEXT_PLAIN)
                .expectBody().equals(snapshotData)
        }
    }
}

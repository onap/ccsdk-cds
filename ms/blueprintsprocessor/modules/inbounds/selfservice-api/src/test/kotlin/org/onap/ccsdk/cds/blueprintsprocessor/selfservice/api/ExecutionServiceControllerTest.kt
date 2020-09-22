/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
 * Modifications Copyright © 2019 IBM.
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
package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.core.compress
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.io.File
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@WebFluxTest
@ContextConfiguration(
    classes = [
        ExecutionServiceHandler::class, BluePrintCoreConfiguration::class,
        BluePrintCatalogService::class, SelfServiceApiTestConfiguration::class, ErrorCatalogTestConfiguration::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class ExecutionServiceControllerTest {

    @Autowired
    lateinit var blueprintsProcessorCatalogService: BluePrintCatalogService

    @Autowired
    lateinit var webTestClient: WebTestClient

    @BeforeTest
    fun init() {
        deleteDir("target", "blueprints")

        // Create sample CBA zip
        normalizedFile("./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")
            .compress(normalizedFile("./target/blueprints/generated-cba.zip"))
    }

    @AfterTest
    fun cleanDir() {
        deleteDir("target", "blueprints")
    }

    @Test
    fun `test rest process`() {
        runBlocking {
            blueprintsProcessorCatalogService.saveToDatabase(UUID.randomUUID().toString(), loadTestCbaFile())

            val executionServiceInput = JacksonUtils
                .readValueFromClassPathFile(
                    "execution-input/default-input.json",
                    ExecutionServiceInput::class.java
                )!!

            webTestClient
                .post()
                .uri("/api/v1/execution-service/process")
                .body(BodyInserters.fromObject(executionServiceInput))
                .exchange()
                .expectStatus().isOk
        }
    }

    @Test
    fun `rest resource process should return status code 500 in case of server-side exception`() {
        runBlocking {
            blueprintsProcessorCatalogService.saveToDatabase(UUID.randomUUID().toString(), loadTestCbaFile())

            val executionServiceInput = JacksonUtils
                .readValueFromClassPathFile(
                    "execution-input/faulty-input.json",
                    ExecutionServiceInput::class.java
                )!!

            webTestClient
                .post()
                .uri("/api/v1/execution-service/process")
                .body(BodyInserters.fromObject(executionServiceInput))
                .exchange()
                .expectStatus().is5xxServerError
        }
    }

    private fun loadTestCbaFile(): File {
        val testCbaFile = normalizedFile("./target/blueprints/generated-cba.zip")
        assertTrue(testCbaFile.exists(), "couldn't get file ${testCbaFile.absolutePath}")
        return testCbaFile
    }
}

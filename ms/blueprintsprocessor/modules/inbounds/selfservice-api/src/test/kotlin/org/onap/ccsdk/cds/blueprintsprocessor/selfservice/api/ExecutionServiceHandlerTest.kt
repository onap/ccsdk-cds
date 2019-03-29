/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
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

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@WebFluxTest
@ContextConfiguration(classes = [ExecutionServiceHandler::class, BluePrintCoreConfiguration::class, BluePrintCatalogService::class, SecurityProperties::class])
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@TestPropertySource(locations = ["classpath:application-test.properties"])
class ExecutionServiceHandlerTest {

    @Autowired
    lateinit var blueprintCatalog: BluePrintCatalogService
    @Autowired
    lateinit var webTestClient: WebTestClient

    @BeforeTest
    fun init() {
        deleteDir("target", "blueprints")
    }

    @AfterTest
    fun cleanDir() {
        deleteDir("target", "blueprints")
    }


    @Test
    fun `test rest upload blueprint`() {
        runBlocking {
            val file = Paths.get("./src/test/resources/test-cba.zip").toFile()
            assertTrue(file.exists(), "couldn't get file ${file.absolutePath}")

            val body = MultipartBodyBuilder().apply {
                part("file", object : ByteArrayResource(Files.readAllBytes(Paths.get("./src/test/resources/test-cba.zip"))) {
                    override fun getFilename(): String {
                        return "test-cba.zip"
                    }
                })
            }.build()

            webTestClient
                    .post()
                    .uri("/api/v1/execution-service/upload")
                    .body(BodyInserters.fromMultipartData(body))
                    .exchange()
                    .expectStatus().isOk
                    .returnResult<String>()
                    .responseBody
                    .awaitSingle()
        }

    }

    @Test
    fun `test rest process`() {
        runBlocking {
            val file = Paths.get("./src/test/resources/test-cba.zip").toFile()
            assertTrue(file.exists(), "couldnt get file ${file.absolutePath}")
            blueprintCatalog.saveToDatabase(UUID.randomUUID().toString(), file)

            val executionServiceInput = JacksonUtils
                    .readValueFromClassPathFile("execution-input/default-input.json",
                            ExecutionServiceInput::class.java)!!

            webTestClient
                    .post()
                    .uri("/api/v1/execution-service/process")
                    .body(BodyInserters.fromObject(executionServiceInput))
                    .exchange()
                    .expectStatus().isOk
        }
    }
}
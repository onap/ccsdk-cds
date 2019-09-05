/*
 * Copyright © 2019 Bell Canada
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

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.Test

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@ContextConfiguration(classes = [ExecutionServiceControllerTest::class, SecurityProperties::class])
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@TestPropertySource(locations = ["classpath:application-test.properties"])
@DirtiesContext
@WebFluxTest
class ExecutionServiceControllerTest {

    private val log = LoggerFactory.getLogger(ExecutionServiceControllerTest::class.java)!!

    @Autowired
    lateinit var webTestClient: WebTestClient

    var event: ExecutionServiceInput? = null

    @Before
    fun setup() {
        deleteDir("target", "blueprints")
    }

    @After
    fun clean() {
        deleteDir("target", "blueprints")
    }

    @Test
    fun uploadBluePrint() {
        runBlocking {
            val body = MultipartBodyBuilder().apply {
                part("file", object : ByteArrayResource(Files.readAllBytes(loadCbaArchive().toPath())) {
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

    private fun loadCbaArchive(): File {
        return Paths.get("./src/test/resources/cba-for-kafka-integration_enriched.zip").toFile()
    }
}



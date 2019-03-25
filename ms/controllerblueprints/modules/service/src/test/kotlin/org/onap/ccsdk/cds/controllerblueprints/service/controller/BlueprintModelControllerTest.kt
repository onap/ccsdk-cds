/*
 * Copyright © 2019 Bell Canada Intellectual Property.
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

package org.onap.ccsdk.cds.controllerblueprints.service.controller

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.onap.ccsdk.cds.controllerblueprints.TestApplication
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.config.BluePrintPathConfiguration
import org.onap.ccsdk.cds.controllerblueprints.service.ControllerBluePrintCoreConfiguration
import org.onap.ccsdk.cds.controllerblueprints.service.domain.BlueprintModelSearch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.util.Base64Utils
import org.springframework.web.reactive.function.BodyInserters
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * BlueprintModelControllerTest Purpose: Integration test at API level
 *
 * @author Vinal Patel
 * @version 1.0
 */

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = [TestApplication::class, ControllerBluePrintCoreConfiguration::class])
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.controllerblueprints"])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@EnableAutoConfiguration
class BlueprintModelControllerTest {

    private val log = LoggerFactory.getLogger(BlueprintModelControllerTest::class.java)!!

    companion object {
        private var bp: BlueprintModelSearch? = null
    }

    @Autowired
    lateinit var webTestClient: WebTestClient

    private var bluePrintLoadConfiguration: BluePrintPathConfiguration? = null

    private val blueprintDir = "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
    private var zipBlueprintFileName: String? = null

    private var testZipFile: File? = null


    @Before
    fun setUp() {
        assertNotNull(webTestClient, " Failed to create WebTestClient")

        bluePrintLoadConfiguration = BluePrintPathConfiguration().apply {
            blueprintArchivePath = "./target/blueprints/archive"
            blueprintWorkingPath = "./target/blueprints/work"
            blueprintDeployPath = "./target/blueprints/deploy"
        }
        zipBlueprintFileName = normalizedPathName(bluePrintLoadConfiguration!!.blueprintArchivePath, "test.zip")

        val archiveDir = normalizedFile(bluePrintLoadConfiguration!!.blueprintArchivePath).reCreateDirs()
        assertTrue(archiveDir.exists(), "failed to create archiveDir(${archiveDir.absolutePath}")

        val blueprintFile = Paths.get(blueprintDir).toFile().normalize()
        testZipFile = blueprintFile.compress(zipBlueprintFileName!!)
        assertNotNull(testZipFile, "test zip is null")
        assertTrue(testZipFile!!.exists(), "Failed to create blueprint test zip(${testZipFile!!.absolutePath}")
    }

    @After
    fun tearDown() {
        deleteDir(bluePrintLoadConfiguration!!.blueprintArchivePath)
        deleteDir(bluePrintLoadConfiguration!!.blueprintWorkingPath)
    }

    @Test
    fun test01_saveBluePrint() {
        bp = runBlocking {
            val body = MultipartBodyBuilder().apply {
                part("file", object : ByteArrayResource(testZipFile!!.readBytes()) {
                    override fun getFilename(): String {
                        return "test.zip"
                    }
                })
            }.build()

            val saveBP = webTestClient
                    .post()
                    .uri("/api/v1/blueprint-model")
                    .body(BodyInserters.fromMultipartData(body))
                    .exchange()
                    .expectStatus().isOk
                    .returnResult<BlueprintModelSearch>()
                    .responseBody
                    .awaitSingle()

            assertNotNull(saveBP, "failed to get response")
            assertEquals("baseconfiguration", saveBP.artifactName, "mismatch artifact name")
            assertEquals("1.0.0", saveBP.artifactVersion, "mismatch artifact version")
            assertEquals("N", saveBP.published, "mismatch publish")
            saveBP
        }
    }

    @Test
    @Throws(JSONException::class)
    fun test02_getBluePrintByNameAndVersion() {
        webTestClient(HttpMethod.GET, null,
                "/api/v1/blueprint-model/by-name/${bp!!.artifactName}/version/${bp!!.artifactVersion}",
                HttpStatus.OK, false)
    }


    @Test
    @Throws(JSONException::class)
    fun test03_getBlueprintModel() {
        webTestClient(HttpMethod.GET, null,
                "/api/v1/blueprint-model/${bp!!.id}",
                HttpStatus.OK, false)
    }

    @Test
    @Throws(JSONException::class)
    fun test04_getAllBlueprintModel() {
        webTestClient(HttpMethod.GET, null, "/api/v1/blueprint-model", HttpStatus.OK, false)
    }

    @Test
    @Throws(JSONException::class)
    fun test05_downloadBluePrint() {
        webTestClient(HttpMethod.GET, null,
                "/api/v1/blueprint-model/download/${bp!!.id}",
                HttpStatus.OK, false)
    }

    @Test
    fun test06_enrichBlueprintModel() {
    }

    @Test
    fun test07_publishBlueprintModel() {
    }

    @Test
    @Throws(JSONException::class)
    fun test08_searchBlueprintModels() {
        webTestClient(HttpMethod.GET, null,
                "/api/v1/blueprint-model/search/${bp!!.artifactName}",
                HttpStatus.OK, false)
    }

    @Test
    @Throws(JSONException::class)
    fun test09_downloadBlueprintByNameAndVersion() {
        webTestClient(HttpMethod.GET, null,
                "/api/v1/blueprint-model/download/by-name/${bp!!.artifactName}/version/${bp!!.artifactVersion}",
                HttpStatus.OK, false)
    }

    @Test
    fun test10_deleteBluePrint() {
        webTestClient.delete().uri("/api/v1/blueprint-model/${bp!!.id}")
                .header("Authorization", "Basic " + Base64Utils
                        .encodeToString(("ccsdkapps" + ":" + "ccsdkapps").toByteArray(UTF_8)))
                .exchange()
                .expectStatus().is2xxSuccessful
    }

    @Throws(JSONException::class)
    private fun webTestClient(requestMethod: HttpMethod, body: BodyInserters.MultipartInserter?, uri: String,
                              expectedResponceStatus: HttpStatus, setParam: Boolean) {

        log.info("Requesting($uri): Method(${requestMethod.name})")

        webTestClient.method(requestMethod).uri(uri)
                .header("Authorization", "Basic " + Base64Utils
                        .encodeToString(("ccsdkapps" + ":" + "ccsdkapps").toByteArray(UTF_8)))
                .body(body)
                .exchange()
                .expectStatus().isEqualTo(expectedResponceStatus)
                .expectBody()
                .returnResult().responseBody!!

    }

}
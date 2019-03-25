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

import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.onap.ccsdk.cds.controllerblueprints.TestApplication
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintArchiveUtils
import org.onap.ccsdk.cds.controllerblueprints.service.domain.BlueprintModelSearch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.Base64Utils
import org.springframework.web.reactive.function.BodyInserters
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Paths

/**
 * BlueprintModelControllerTest Purpose: Integration test at API level
 *
 * @author Vinal Patel
 * @version 1.0
 */

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = [TestApplication::class])
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.controllerblueprints"])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@EnableAutoConfiguration
class BlueprintModelControllerTest {

    companion object {

        private var id: String? = null
        private var name: String? = null
        private var version: String? = null
        private var tag: String? = null
        private var result: String? = null
    }

    @Value("\${controllerblueprints.loadBluePrintPaths}")
    private val loadBluePrintPaths: String? = null

    @Autowired
    private val webTestClient: WebTestClient? = null

    @Value("\${controllerblueprints.loadBlueprintsExamplesPath}")
    private val blueprintArchivePath: String? = null

    private val filename = "test.zip"
    private var blueprintFile: File? = null
    private var zipBlueprintFile: File? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        blueprintFile = File(loadBluePrintPaths+"/baseconfiguration")
        if (blueprintFile!!.isDirectory) {
            zipBlueprintFile = File(Paths.get(blueprintArchivePath).resolve(filename).toString())
            BluePrintArchiveUtils.compress(blueprintFile!!, zipBlueprintFile!!, true)
        }
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        zipBlueprintFile!!.delete()
    }

    @Test
    @Throws(IOException::class, JSONException::class)
    fun test1_saveBluePrint() {
        webTestClient(HttpMethod.POST,
                BodyInserters.fromMultipartData("file", object : ByteArrayResource(Files.readAllBytes(zipBlueprintFile!!.toPath())) {
                    override fun getFilename(): String? {
                        return "test.zip"
                    }
                }),
                "/api/v1/blueprint-model",
                HttpStatus.OK, true)
    }

    @Test
    @Throws(JSONException::class)
    fun test2_getBluePrintByNameAndVersion() {
        webTestClient(HttpMethod.GET, null, "/api/v1/blueprint-model/by-name/$name/version/$version", HttpStatus.OK, false)
    }


    @Test
    @Throws(JSONException::class)
    fun test3_getBlueprintModel() {
        webTestClient(HttpMethod.GET, null, "/api/v1/blueprint-model/$id", HttpStatus.OK, false)
    }

    @Test
    @Throws(JSONException::class)
    fun test4_getAllBlueprintModel() {
        webTestClient(HttpMethod.GET, null, "/api/v1/blueprint-model", HttpStatus.OK, false)
    }

    @Test
    @Throws(JSONException::class)
    fun test5_downloadBluePrint() {
        webTestClient(HttpMethod.GET, null, "/api/v1/blueprint-model/download/$id", HttpStatus.OK, false)
    }

    @Test
    fun test6_publishBlueprintModel() {
    }

    @Test
    @Throws(JSONException::class)
    fun test7_searchBlueprintModels() {
        webTestClient(HttpMethod.GET, null, "/api/v1/blueprint-model/search/$name", HttpStatus.OK, false)
    }

    @Test
    @Throws(JSONException::class)
    fun test8_downloadBlueprintByNameAndVersion() {
        webTestClient(HttpMethod.GET, null, "/api/v1/blueprint-model/download/by-name/$name/version/$version", HttpStatus.OK, false)
    }

    @Test
    fun test9_deleteBluePrint() {
        //TODO: Use webTestClient function
        //webTestClient(HttpMethod.DELETE, null, "/api/v1/blueprint-model/" + id, HttpStatus.OK, false);
        webTestClient!!.delete().uri("/api/v1/blueprint-model/$id")
                .header("Authorization", "Basic " + Base64Utils
                        .encodeToString(("ccsdkapps" + ":" + "ccsdkapps").toByteArray(UTF_8)))
                .exchange()
                .expectStatus().is2xxSuccessful
    }

    @Throws(JSONException::class)
    private fun webTestClient(requestMethod: HttpMethod, body: BodyInserters.MultipartInserter?, uri: String, expectedResponceStatus: HttpStatus, setParam: Boolean) {

        result = String(webTestClient!!.method(requestMethod).uri(uri)
                .header("Authorization", "Basic " + Base64Utils
                        .encodeToString(("ccsdkapps" + ":" + "ccsdkapps").toByteArray(UTF_8)))
                .body(body)
                .exchange()
                .expectStatus().isEqualTo(expectedResponceStatus)
                .expectBody()
                .returnResult().responseBody!!)

        if (setParam) {
            val jsonResponse = JSONObject(result)
            val blueprintModelSearchJSON = jsonResponse.getJSONObject("blueprintModel")
            val gson = Gson()
            val blueprintModelSearch = gson.fromJson(blueprintModelSearchJSON.toString(), BlueprintModelSearch::class.java)
            id = blueprintModelSearch.id
            name = blueprintModelSearch.artifactName
            version = blueprintModelSearch.artifactVersion
            tag = blueprintModelSearch.tags
        }
    }

}
/*
 * Copyright © 2026 Deutsche Telekom AG.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ModelType
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
    classes = [DesignerApiTestConfiguration::class, ErrorCatalogTestConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class ModelTypeControllerWebTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    private val modelName = "web-test-datatype"

    private val authorization =
        "Basic " + Base64.getEncoder().encodeToString("ccsdkapps:ccsdkapps".toByteArray(UTF_8))

    @Test
    fun saveAndDeleteModelTypeOverHttp() {
        val modelType = ModelType().apply {
            definitionType = BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE
            derivedFrom = BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT
            description = "Definition for Sample Datatype"
            definition = JacksonUtils.jsonNode(
                JacksonUtils.getClassPathFileContent("model_type/data_type/datatype-property.json")
            )
            modelName = this@ModelTypeControllerWebTest.modelName
            version = "1.0.0"
            tags = "$modelName,${BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE}"
            updatedBy = "xxxxxx@xxx.com"
        }

        webTestClient.post().uri("/api/v1/model-type")
            .header("Authorization", authorization)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(JacksonUtils.getJson(modelType))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.modelName").isEqualTo(modelName)

        webTestClient.delete().uri("/api/v1/model-type/$modelName")
            .header("Authorization", authorization)
            .exchange()
            .expectStatus().isOk
    }
}

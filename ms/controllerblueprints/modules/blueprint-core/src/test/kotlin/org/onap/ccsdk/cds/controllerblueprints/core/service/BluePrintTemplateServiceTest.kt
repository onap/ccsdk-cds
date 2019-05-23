/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Modifications Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.controllerblueprints.core.service

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
class BluePrintTemplateServiceTest {

    lateinit var blueprintRuntime: BluePrintRuntimeService<*>

    @BeforeTest
    fun setup() {
        val blueprintBasePath: String = ("./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")
        blueprintRuntime = BluePrintMetadataUtils.getBluePrintRuntime("1234", blueprintBasePath)
    }

    @Test
    fun testVelocityGeneratedContent() {
        runBlocking {
            val template = JacksonUtils.getClassPathFileContent("templates/base-config-velocity-template.vtl")
            val json = JacksonUtils.getClassPathFileContent("templates/base-config-data-velocity.json")

            val content = BluePrintVelocityTemplateService.generateContent(template, json)
            assertNotNull(content, "failed to generate content for velocity template")
        }

    }

    @Test
    fun testJinjaGeneratedContent() {
        runBlocking {
            val template = JacksonUtils.getClassPathFileContent("templates/base-config-jinja-template.jinja")
            val json = JacksonUtils.getClassPathFileContent("templates/base-config-data-jinja.json")

            var element: MutableMap<String, JsonNode> = mutableMapOf()
            val jsonNode = JacksonUtils.getJsonNode(arrayListOf(hashMapOf("name" to "Element1", "location" to "Region0"), hashMapOf("name" to "Element2", "location" to "Region1")))
            element["additional_array"] = jsonNode

            val content = BluePrintJinjaTemplateService.generateContent(template, json, false, element)
            assertNotNull(content, "failed to generate content for velocity template")
        }

    }

    @Test
    fun testVelocityGeneratedContentFromFiles() {
        runBlocking {
            val bluePrintTemplateService = BluePrintTemplateService()
            val templateFile = "templates/base-config-velocity-template.vtl"
            val jsonFile = "templates/base-config-data-velocity.json"

            val content = bluePrintTemplateService.generateContentFromFiles(
                    templateFile, BluePrintConstants.ARTIFACT_VELOCITY_TYPE_NAME, jsonFile, false, mutableMapOf())
            assertNotNull(content, "failed to generate content for velocity template")
        }

    }

    @Test
    fun testJinjaGeneratedContentFromFiles() {
        runBlocking {
            var element: MutableMap<String, JsonNode> = mutableMapOf()
            val jsonNode = JacksonUtils.getJsonNode(arrayListOf(hashMapOf("name" to "Element1", "location" to "Region0"), hashMapOf("name" to "Element2", "location" to "Region1")))
            element["additional_array"] = jsonNode

            val bluePrintTemplateService = BluePrintTemplateService()

            val templateFile = "templates/base-config-jinja-template.jinja"
            val jsonFile = "templates/base-config-data-jinja.json"

            val content = bluePrintTemplateService.generateContentFromFiles(
                    templateFile, BluePrintConstants.ARTIFACT_JINJA_TYPE_NAME,
                    jsonFile, false, element)
            assertNotNull(content, "failed to generate content for velocity template")
        }
    }
}


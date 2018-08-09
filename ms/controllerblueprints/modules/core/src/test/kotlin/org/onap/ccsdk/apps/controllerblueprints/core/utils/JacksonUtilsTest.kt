/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints.core.utils

import com.fasterxml.jackson.databind.JsonNode
import org.junit.Test
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * JacksonUtilsTest
 * @author Brinda Santh
 * ${DATA}
 */
class JacksonUtilsTest {

    private val logger: Logger = LoggerFactory.getLogger(this::class.toString())

    val basePath = "load/blueprints"

    @Test
    fun testReadValues() {
        val content = ResourceResolverUtils.getFileContent("baseconfiguration/Definitions/activation-blueprint.json", basePath)
        val serviceTemplate = JacksonUtils.readValue(content, ServiceTemplate::class.java)
        assertNotNull(serviceTemplate, "Failed to simple transform Service Template")
        assertEquals(true, serviceTemplate is ServiceTemplate, "failed to get Service Template instance")

        val jsonContent = JacksonUtils.getJson(serviceTemplate!!, true)
        assertNotNull(jsonContent, "Failed to get json content")
    }

    @Test
    fun testJsonNodeFromClassPathFile() {
        val filePath = "data/default-context.json"
        val jsonNode = JacksonUtils.jsonNodeFromClassPathFile(filePath)
        assertNotNull(jsonNode, "Failed to get json node from file")
        assertEquals(true, jsonNode is JsonNode, "failed to get JSON node instance")
    }

    @Test
    fun testJsonNodeFromFile() {
        val filePath =  basePath + "/baseconfiguration/Definitions/activation-blueprint.json"
        val jsonNode = JacksonUtils.jsonNodeFromFile(filePath)
        assertNotNull(jsonNode, "Failed to get json node from file")
        assertEquals(true, jsonNode is JsonNode, "failed to get JSON node instance")
    }

    @Test
    fun testGetListFromJson() {
        val content = "[\"good\",\"boy\" ]"
        val nodeType = JacksonUtils.getListFromJson(content, String::class.java)
        assertNotNull(nodeType, "Failed to get String array from content")
    }
}
/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core.utils

import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.slf4j.LoggerFactory
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * JacksonUtilsTest
 * @author Brinda Santh
 * ${DATA}
 */
class JacksonUtilsTest {

    private val log = LoggerFactory.getLogger(this::class.toString())

    @Test
    fun testJsonNodeFromClassPathFile() {
        val filePath = "data/default-context.json"
        JacksonUtils.jsonNodeFromClassPathFile(filePath)
    }

    @Test
    fun testJsonNodeFromFile() {
        val filePath = "src/test/resources/data/default-context.json"
        JacksonUtils.jsonNodeFromFile(filePath)
    }

    @Test
    fun testGetListFromJson() {
        val content = "[\"good\",\"boy\" ]"
        val nodeType = JacksonUtils.getListFromJson(content, String::class.java)
        assertNotNull(nodeType, "Failed to get String array from content")
    }

    @Test
    fun testJsonValue() {
        val filePath = "data/alltype-data.json"
        val rootJson = JacksonUtils.jsonNodeFromClassPathFile(filePath)
        assertNotNull(rootJson, "Failed to get all type data json node")
        val intValue = rootJson.get("intValue")
        assertTrue(JacksonUtils.checkJsonNodeValueOfType(BlueprintConstants.DATA_TYPE_INTEGER, intValue), "Failed to get as int value")
        val floatValue = rootJson.get("floatValue")
        assertTrue(JacksonUtils.checkJsonNodeValueOfType(BlueprintConstants.DATA_TYPE_FLOAT, floatValue), "Failed to get as float value")
        val stringValue = rootJson.get("stringValue")
        assertTrue(JacksonUtils.checkJsonNodeValueOfType(BlueprintConstants.DATA_TYPE_STRING, stringValue), "Failed to get as string value")
        val booleanValue = rootJson.get("booleanValue")
        assertTrue(JacksonUtils.checkJsonNodeValueOfType(BlueprintConstants.DATA_TYPE_BOOLEAN, booleanValue), "Failed to get as boolean value")
        val arrayStringValue = rootJson.get("arrayStringValue")
        assertTrue(JacksonUtils.checkJsonNodeValueOfType(BlueprintConstants.DATA_TYPE_LIST, arrayStringValue), "Failed to get as List value")
        // FIX needed for ("complex type JSON & MAP")
        // val mapValue = rootJson.get("mapValue")
        // assertTrue(JacksonUtils.checkJsonNodeValueOfType(BlueprintConstants.DATA_TYPE_MAP, mapValue), "Failed to get as Map value")

        assertTrue(!JacksonUtils.checkJsonNodeValueOfType(BlueprintConstants.DATA_TYPE_LIST, stringValue), "Negative type failed")
    }
}

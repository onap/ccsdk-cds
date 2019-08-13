/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright © 2019 IBM, Bell Canada
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */


package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import io.mockk.every

import io.mockk.mockk
import io.mockk.spyk
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils.Companion.parseResponseNode
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import kotlin.test.assertEquals

data class IpAddresses(val name: String, val ip: String)

class ResourceAssignmentUtilsTest {
    private lateinit var resourceAssignmentRuntimeService: ResourceAssignmentRuntimeService

    @Before
    fun setup() {

        val bluePrintContext = BluePrintMetadataUtils.getBluePrintContext(
                "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")

        resourceAssignmentRuntimeService = spyk(ResourceAssignmentRuntimeService("1234", bluePrintContext))

        val bluePrintRuntimeService = mockk<BluePrintRuntimeService<*>>()

        val propertiesDefinition1 = PropertyDefinition().apply {
            type = "string"
            id = "name"
        }

        val propertiesDefinition2 = PropertyDefinition().apply {
            type = "string"
            id = "address"
        }

        val myDataType = DataType().apply {
            id = "myDataType"
            properties = mutableMapOf("property1" to propertiesDefinition1, "property2" to propertiesDefinition2)
        }

        every { resourceAssignmentRuntimeService.bluePrintContext().dataTypeByName("myDataType") } returns myDataType

        every { resourceAssignmentRuntimeService.setNodeTemplateAttributeValue(any(), any(), any()) } returns Unit
    }

    @Test
    fun `generateResourceDataForAssignments - positive test`() {
        //given a valid resource assignment
        val validResourceAssignment = createResourceAssignmentForTest("valid_value")

        //and a list containing that resource assignment
        val resourceAssignmentList = listOf<ResourceAssignment>(validResourceAssignment)

        //when the values of the resources are evaluated
        val outcome = ResourceAssignmentUtils.generateResourceDataForAssignments(resourceAssignmentList)

        //then the assignment should produce a valid result
        val expected = "{\n" + "  \"pnf-id\" : \"valid_value\"\n" + "}"
        assertEquals(expected, outcome, "unexpected outcome generated")

    }

    @Test
    fun `generateResourceDataForAssignments - resource without value is not resolved as null`() {
        //given a valid resource assignment
        val resourceAssignmentWithNullValue = createResourceAssignmentForTest(null)

        //and a list containing that resource assignment
        val resourceAssignmentList = listOf<ResourceAssignment>(resourceAssignmentWithNullValue)

        //when the values of the resources are evaluated
        val outcome = ResourceAssignmentUtils.generateResourceDataForAssignments(resourceAssignmentList)

        //then the assignment should produce a valid result
        val expected = "{\n" + "  \"pnf-id\" : \"\${pnf-id}\"\n" + "}"
        assertEquals(expected, outcome, "unexpected outcome generated")

    }

    private fun createResourceAssignmentForTest(resourceValue: String?): ResourceAssignment {
        val valueForTest = if (resourceValue == null) null else TextNode(resourceValue)
        val resourceAssignmentForTest = ResourceAssignment().apply {
            name = "pnf-id"
            dictionaryName = "pnf-id"
            dictionarySource = "input"
            property = PropertyDefinition().apply {
                type = "string"
                value = valueForTest
            }
        }
        return resourceAssignmentForTest
    }

    @Test
    fun parseResponseNodeTest(){
        // Input values for primitive type
        val value = arrayListOf("1.2.3.1")
        val keyValue = mutableMapOf<String, String>()
        keyValue["value"]= "1.2.3.1"

        // Input values for collection type
        val arrayOfString = arrayListOf("1.2.3.1", "1.2.3.2", "1.2.3.3")
        val arrayOfKeyValue = arrayListOf(IpAddresses("value", "1.2.3.1"), IpAddresses("value", "1.2.3.2"), IpAddresses("value", "1.2.3.3"))
        val mapOfString = mutableMapOf<String, String>()
        mapOfString["value1"] = "1.2.3.1"
        mapOfString["value2"] = "1.2.3.2"
        mapOfString["value3"] = "1.2.3.3"
        val mapOfkeyValue = mutableMapOf<String, IpAddresses>()
        mapOfkeyValue["data1"] = IpAddresses("value", "1.2.3.1")
        mapOfkeyValue["data2"] = IpAddresses("value", "1.2.3.2")
        mapOfkeyValue["data3"] = IpAddresses("value", "1.2.3.3")

        var outcome: JsonNode

        // Test primitives types
        val expectedPrimitiveType = TextNode("1.2.3.1")
        outcome = prepareResponsenodeForTest("sample-value", "string", "", value)
        assertEquals(expectedPrimitiveType, outcome, "unexpected outcome returned for primitive type of simple String")
        outcome = prepareResponsenodeForTest("sample-key-value", "string", "", keyValue)
        assertEquals(expectedPrimitiveType, outcome, "unexpected outcome returned for primitive type of key-value String")

        // Test collection types
        val expectedCollectionString = mutableMapOf<String, JsonNode>()
        /*expectedCollectionString["value1"]=
        TextNode("1.2.3.1")
        outcome = prepareResponsenodeForTest("sample-value", "string", "", value)
        assertEquals(expectedPrimitiveType, outcome, "unexpected outcome returned for primitive type of simple String")
        outcome = prepareResponsenodeForTest("sample-key-value", "string", "", keyValue)
        assertEquals(expectedPrimitiveType, outcome, "unexpected outcome returned for primitive type of key-value String")*/

    }

    private fun prepareResponsenodeForTest(dictionary_source: String, sourceType: String, entrySchema: String,
                                           response: Any): JsonNode {

        val resourceAssignment = when (sourceType) {
            "list", "map" -> {
                prepareRADataDictionnaryCollection(dictionary_source, sourceType, entrySchema)
            }
            "string" -> {
                prepareRADataDictionnaryOfPrimaryType(dictionary_source)
            }
            else -> {
                prepareRADataDictionnaryCollection(dictionary_source, sourceType, entrySchema)
            }
        }

        val responseNode = checkNotNull(JacksonUtils.getJsonNode(response)) {
            "Failed to get database query result into Json node."
        }

        val outputKeyMapping = prepareOutputKeyMapping(dictionary_source)

        return parseResponseNode(responseNode, resourceAssignment, resourceAssignmentRuntimeService, outputKeyMapping)
    }

    private fun prepareRADataDictionnaryCollection(dictionary_source: String, sourceType: String, entrySchema: String): ResourceAssignment {
        return ResourceAssignment().apply {
            name = "ipAddress-list"
            dictionaryName = "sample-licenses"
            dictionarySource = "$dictionary_source"
            property = PropertyDefinition().apply {
                type = "$sourceType"
                entrySchema.apply {
                    type = "$entrySchema"
                }
            }
        }
    }

    private fun prepareRADataDictionnaryOfPrimaryType(dictionary_source: String): ResourceAssignment {
        return ResourceAssignment().apply {
            name = "ipAddress"
            dictionaryName = "sample-ip"
            dictionarySource = "$dictionary_source"
            property = PropertyDefinition().apply {
                type = "string"
            }
        }
    }

    private fun prepareOutputKeyMapping(dictionary_source: String): MutableMap<String, String> {
        val outputMapping = mutableMapOf<String, String>()

        when (dictionary_source) {
            "listOfString", "mapOfString" -> {
                outputMapping["value"] = "value"
            }
            "listOfMyDataType", "mapOfMyDataType" -> {
                outputMapping["name"] = "name"
                outputMapping["address"] = "address"
            }
            "sample-key-value", "sample-value" -> {
                outputMapping["sample-ip"] = "value"
            }
            else -> {
                outputMapping["value"] = "value"
            }
        }
        return outputMapping
    }
}
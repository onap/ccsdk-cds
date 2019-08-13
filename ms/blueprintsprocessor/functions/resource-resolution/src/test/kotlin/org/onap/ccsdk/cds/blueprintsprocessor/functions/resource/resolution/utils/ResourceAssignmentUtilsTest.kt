/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (c) 2019 IBM, Bell Canada.
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
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import io.mockk.every
import io.mockk.spyk
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.EntrySchema
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import kotlin.test.assertEquals

data class IpAddress(val port: String, val ip: String)
data class Host(val name: String, val ipAddress: IpAddress)
data class ExpectedResponseIp(val ip: String)
data class ExpectedResponsePort(val port: String)

class ResourceAssignmentUtilsTest {
    private lateinit var resourceAssignmentRuntimeService: ResourceAssignmentRuntimeService

    @Before
    fun setup() {

        val bluePrintContext = BluePrintMetadataUtils.getBluePrintContext(
                "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")

        resourceAssignmentRuntimeService = spyk(ResourceAssignmentRuntimeService("1234", bluePrintContext))

        val propertiesDefinition1 = PropertyDefinition().apply {
            type = "string"
            id = "port"
        }

        val propertiesDefinition2 = PropertyDefinition().apply {
            type = "string"
            id = "ip"
        }

        val propertiesDefinition3 = PropertyDefinition().apply {
            type = "string"
            id = "name"
        }

        val propertiesDefinition4 = PropertyDefinition().apply {
            type = "ip-address"
            id = "ipAddress"
        }

        var mapOfPropertiesIpAddress = mutableMapOf<String, PropertyDefinition>()
        mapOfPropertiesIpAddress["port"] = propertiesDefinition1
        mapOfPropertiesIpAddress["ip"] = propertiesDefinition2

        var mapOfPropertiesHost = mutableMapOf<String, PropertyDefinition>()
        mapOfPropertiesHost["name"] = propertiesDefinition3
        mapOfPropertiesHost["ipAddress"] = propertiesDefinition4

        val myDataTypeIpaddress = DataType().apply {
            id = "ip-address"
            properties = mapOfPropertiesIpAddress
        }

        val myDataTypeHost = DataType().apply {
            id = "host"
            properties = mapOfPropertiesHost
        }

        every { resourceAssignmentRuntimeService.bluePrintContext().dataTypeByName("ip-address") } returns myDataTypeIpaddress

        every { resourceAssignmentRuntimeService.bluePrintContext().dataTypeByName("host") } returns myDataTypeHost

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
    fun parseResponseNodeTestForPrimitivesTypes(){
        // Input values for primitive type
        val keyValue = mutableMapOf<String, String>()
        keyValue["value"]= "1.2.3.1"
        val expectedPrimitiveType = TextNode("1.2.3.1")

        var outcome = prepareResponseNodeForTest("sample-value", "string",
                "", "1.2.3.1".asJsonPrimitive())
        assertEquals(expectedPrimitiveType, outcome, "Unexpected outcome returned for primitive type of simple String")
        outcome = prepareResponseNodeForTest("sample-key-value", "string", "", keyValue)
        assertEquals(expectedPrimitiveType, outcome, "Unexpected outcome returned for primitive type of key-value String")
    }

    @Test
    fun parseResponseNodeTestForCollectionsOfString(){
        // Input values for collection type
        val mapOfString = mutableMapOf<String, String>()
        mapOfString["value1"] = "1.2.3.1"
        mapOfString["port"] = "8888"
        mapOfString["value2"] = "1.2.3.2"
        val arrayOfKeyValue = arrayListOf(ExpectedResponseIp("1.2.3.1"),
                ExpectedResponsePort( "8888"), ExpectedResponseIp("1.2.3.2"))

        val mutableMapKeyValue = mutableMapOf<String, String>()
        mutableMapKeyValue["value1"] = "1.2.3.1"
        mutableMapKeyValue["port"] = "8888"

        //List
        val expectedListOfString = arrayOfKeyValue.asJsonType()
        var outcome = prepareResponseNodeForTest("listOfString", "list",
                "string", mapOfString.asJsonType())
        assertEquals(expectedListOfString, outcome, "unexpected outcome returned for list of String")

        //Map
        val expectedMapOfString = mutableMapOf<String, JsonNode>()
        expectedMapOfString["ip"] = "1.2.3.1".asJsonPrimitive()
        expectedMapOfString["port"] = "8888".asJsonPrimitive()

        val arrayNode = JacksonUtils.objectMapper.createArrayNode()
        expectedMapOfString.map {
            val arrayChildNode = JacksonUtils.objectMapper.createObjectNode()
            arrayChildNode.set(it.key, it.value)
            arrayNode.add(arrayChildNode)
        }
        val arrayChildNode1 = JacksonUtils.objectMapper.createObjectNode()
        arrayChildNode1.set("ip", NullNode.getInstance())
        arrayNode.add(arrayChildNode1)
        outcome = prepareResponseNodeForTest("mapOfString", "map", "string",
                mutableMapKeyValue.asJsonType())
        assertEquals(arrayNode, outcome, "unexpected outcome returned for map of String")
    }

    @Test
    fun parseResponseNodeTestForCollectionsOfJsonNode(){
        // Input values for collection type
        val mapOfString = mutableMapOf<String, JsonNode>()
        mapOfString["value1"] = "1.2.3.1".asJsonPrimitive()
        mapOfString["port"] = "8888".asJsonPrimitive()
        mapOfString["value2"] = "1.2.3.2".asJsonPrimitive()
        val arrayOfKeyValue = arrayListOf(ExpectedResponseIp("1.2.3.1"),
                ExpectedResponsePort( "8888"), ExpectedResponseIp("1.2.3.2"))

        val mutableMapKeyValue = mutableMapOf<String, JsonNode>()
        mutableMapKeyValue["value1"] = "1.2.3.1".asJsonPrimitive()
        mutableMapKeyValue["port"] = "8888".asJsonPrimitive()

        //List
        val expectedListOfString = arrayOfKeyValue.asJsonType()
        var outcome = prepareResponseNodeForTest("listOfString", "list",
                "string", mapOfString.asJsonType())
        assertEquals(expectedListOfString, outcome, "unexpected outcome returned for list of String")

        //Map
        val expectedMapOfString = mutableMapOf<String, JsonNode>()
        expectedMapOfString["ip"] = "1.2.3.1".asJsonPrimitive()
        expectedMapOfString["port"] = "8888".asJsonPrimitive()
        val arrayNode = JacksonUtils.objectMapper.createArrayNode()
        expectedMapOfString.map {
            val arrayChildNode = JacksonUtils.objectMapper.createObjectNode()
            arrayChildNode.set(it.key, it.value)
            arrayNode.add(arrayChildNode)
        }
        val arrayChildNode1 = JacksonUtils.objectMapper.createObjectNode()
        arrayChildNode1.set("ip", NullNode.getInstance())
        arrayNode.add(arrayChildNode1)
        outcome = prepareResponseNodeForTest("mapOfString", "map",
                "string", mutableMapKeyValue.asJsonType())
        assertEquals(arrayNode, outcome, "unexpected outcome returned for map of String")
    }

    @Test
    fun parseResponseNodeTestForCollectionsOfComplexType(){
        // Input values for collection type
        val mapOfComplexType = mutableMapOf<String, JsonNode>()
        mapOfComplexType["value1"] = IpAddress("1111", "1.2.3.1").asJsonType()
        mapOfComplexType["value2"] = IpAddress("2222", "1.2.3.2").asJsonType()
        mapOfComplexType["value3"] = IpAddress("3333", "1.2.3.3").asJsonType()

        //List
        val arrayNode = JacksonUtils.objectMapper.createArrayNode()
        mapOfComplexType.map {
            val arrayChildNode = JacksonUtils.objectMapper.createObjectNode()
            arrayChildNode.set("ipAddress", it.value)
            arrayNode.add(arrayChildNode)
        }
        var outcome = prepareResponseNodeForTest("listOfMyDataType", "list",
                "ip-address", mapOfComplexType.asJsonType())
        assertEquals(arrayNode, outcome, "unexpected outcome returned for list of String")
    }

    @Test
    fun `parseResponseNodeTestForComplexType find one output key mapping`(){
        // Input values for complex type
        val objectNode = JacksonUtils.objectMapper.createObjectNode()

        // Input values for collection type
        val mapOfComplexType = mutableMapOf<String, JsonNode>()
        mapOfComplexType["value"] = Host("my-ipAddress", IpAddress("1111", "1.2.3.1")).asJsonType()
        mapOfComplexType["port"] = "8888".asJsonType()
        mapOfComplexType["something"] = "1.2.3.2".asJsonType()

        val expectedComplexType = objectNode.set("ipAddress", Host("my-ipAddress", IpAddress("1111", "1.2.3.1")).asJsonType())
        val outcome = prepareResponseNodeForTest("complexTypeOneKeys", "host",
                "", mapOfComplexType.asJsonType())
        assertEquals(expectedComplexType, outcome, "Unexpected outcome returned for complex type")
    }

    @Test
    fun `parseResponseNodeTestForComplexType find all output key mapping`(){
        // Input values for complex type
        val objectNode = JacksonUtils.objectMapper.createObjectNode()

        // Input values for collection type
        val mapOfComplexType = mutableMapOf<String, JsonNode>()
        mapOfComplexType["name"] = "my-ipAddress".asJsonType()
        mapOfComplexType["ipAddress"] = IpAddress("1111", "1.2.3.1").asJsonType()

        val expectedComplexType = Host("my-ipAddress", IpAddress("1111", "1.2.3.1")).asJsonType()
        val outcome = prepareResponseNodeForTest("complexTypeAllKeys", "host",
                "", mapOfComplexType.asJsonType())
        assertEquals(expectedComplexType, outcome, "Unexpected outcome returned for complex type")
    }

    private fun prepareResponseNodeForTest(dictionary_source: String, sourceType: String, entrySchema: String,
                                           response: Any): JsonNode {

        val resourceAssignment = when (sourceType) {
            "list", "map" -> {
                prepareRADataDictionaryCollection(dictionary_source, sourceType, entrySchema)
            }
            "string" -> {
                prepareRADataDictionaryOfPrimaryType(dictionary_source)
            }
            else -> {
                prepareRADataDictionaryComplexType(dictionary_source, sourceType, entrySchema)
            }
        }

        val responseNode = checkNotNull(JacksonUtils.getJsonNode(response)) {
            "Failed to get database query result into Json node."
        }

        val outputKeyMapping = prepareOutputKeyMapping(dictionary_source)

        return ResourceAssignmentUtils.parseResponseNode(responseNode, resourceAssignment, resourceAssignmentRuntimeService, outputKeyMapping)
    }

    private fun prepareRADataDictionaryOfPrimaryType(dictionary_source: String): ResourceAssignment {
        return ResourceAssignment().apply {
            name = "ipAddress"
            dictionaryName = "sample-ip"
            dictionarySource = "$dictionary_source"
            property = PropertyDefinition().apply {
                type = "string"
            }
        }
    }

    private fun prepareRADataDictionaryCollection(dictionary_source: String, sourceType: String, schema: String): ResourceAssignment {
        return ResourceAssignment().apply {
            name = "ipAddress-list"
            dictionaryName = "sample-licenses"
            dictionarySource = "$dictionary_source"
            property = PropertyDefinition().apply {
                type = "$sourceType"
                entrySchema = EntrySchema().apply {
                    type = "$schema"
                }
            }
        }
    }

    private fun prepareRADataDictionaryComplexType(dictionary_source: String, sourceType: String, schema: String): ResourceAssignment {
        return ResourceAssignment().apply {
            name = "ipAddress-complexType"
            dictionaryName = "sample-licenses"
            dictionarySource = "$dictionary_source"
            property = PropertyDefinition().apply {
                type = "$sourceType"
            }
        }
    }

    private fun prepareOutputKeyMapping(dictionary_source: String): MutableMap<String, String> {
        val outputMapping = mutableMapOf<String, String>()

        when (dictionary_source) {
            "listOfString", "mapOfString" -> {
                //List of string
                outputMapping["value1"] = "ip"
                outputMapping["port"] = "port"
                outputMapping["value2"] = "ip"
            }
            "listOfMyDataType", "mapOfMyDataType" -> {
                //List or map of complex Type
                outputMapping["value1"] = "ipAddress"
                outputMapping["value2"] = "ipAddress"
                outputMapping["value3"] = "ipAddress"
            }
            "sample-key-value", "sample-value" -> {
                //Primary Type
                if (dictionary_source=="sample-key-value")
                    outputMapping["sample-ip"] = "value"
            }
            else -> {
                //Complex Type
                if (dictionary_source == "complexTypeOneKeys")
                    outputMapping["value"] = "ipAddress"
                else {
                    outputMapping["name"] = "name"
                    outputMapping["ipAddress"] = "ipAddress"
                }

            }
        }
        return outputMapping
    }
}
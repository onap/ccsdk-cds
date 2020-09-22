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
import com.fasterxml.jackson.databind.node.TextNode
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.METADATA_TRANSFORM_TEMPLATE
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.EntrySchema
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import kotlin.test.assertEquals

data class IpAddress(val port: String, val ip: String)
data class Host(val name: String, val ipAddress: IpAddress)
data class ExpectedResponseIp(val ip: String)
data class ExpectedResponseIpAddress(val ipAddress: IpAddress)

class ResourceAssignmentUtilsTest {

    private lateinit var resourceAssignmentRuntimeService: ResourceAssignmentRuntimeService
    private lateinit var resourceAssignment: ResourceAssignment

    private lateinit var inputMapToTestPrimitiveTypeWithValue: JsonNode
    private lateinit var inputMapToTestPrimitiveTypeWithKeyValue: JsonNode
    private lateinit var inputMapToTestCollectionOfPrimitiveType: JsonNode
    private lateinit var inputMapToTestCollectionOfComplexTypeWithOneOutputKeyMapping: JsonNode
    private lateinit var inputMapToTestCollectionOfComplexTypeWithAllOutputKeyMapping: JsonNode
    private lateinit var inputMapToTestComplexTypeWithOneOutputKeyMapping: JsonNode
    private lateinit var inputMapToTestComplexTypeWithAllOutputKeyMapping: JsonNode
    private lateinit var expectedValueToTestPrimitiveType: JsonNode
    private lateinit var expectedValueToTesCollectionOfPrimitiveType: JsonNode
    private lateinit var expectedValueToTestCollectionOfComplexTypeWithOneOutputKeyMapping: JsonNode
    private lateinit var expectedValueToTestComplexTypeWithOneOutputKeyMapping: JsonNode
    private lateinit var expectedValueToTestComplexTypeWithAllOutputKeyMapping: JsonNode
    private lateinit var expectedValueToTestCollectionOfComplexTypeWithAllOutputKeyMapping: JsonNode

    @Before
    fun setup() {

        val bluePrintContext = runBlocking {
            BluePrintMetadataUtils.getBluePrintContext(
                "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
            )
        }

        resourceAssignmentRuntimeService = spyk(ResourceAssignmentRuntimeService("1234", bluePrintContext))

        // Init input map and expected values for tests
        initInputMapAndExpectedValuesForPrimitiveType()
        initInputMapAndExpectedValuesForCollection()
        initInputMapAndExpectedValuesForComplexType()

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

        val mapOfPropertiesIpAddress = mutableMapOf<String, PropertyDefinition>()
        mapOfPropertiesIpAddress["port"] = propertiesDefinition1
        mapOfPropertiesIpAddress["ip"] = propertiesDefinition2

        val mapOfPropertiesHost = mutableMapOf<String, PropertyDefinition>()
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

        every {
            resourceAssignmentRuntimeService.bluePrintContext().dataTypeByName("ip-address")
        } returns myDataTypeIpaddress

        every { resourceAssignmentRuntimeService.bluePrintContext().dataTypeByName("host") } returns myDataTypeHost

        every { resourceAssignmentRuntimeService.setNodeTemplateAttributeValue(any(), any(), any()) } returns Unit
    }

    @Test
    fun `generateResourceDataForAssignments - positive test`() {
        // given a valid resource assignment
        val validResourceAssignment1 = createResourceAssignmentForTest("valid_value", "pnf-id")
        val validResourceAssignment2 = createResourceAssignmentForTest("also_valid", "a1")

        // and a list containing that resource assignment
        val resourceAssignmentList = listOf<ResourceAssignment>(validResourceAssignment1, validResourceAssignment2)

        // when the values of the resources are evaluated
        val outcome = ResourceAssignmentUtils.generateResourceDataForAssignments(resourceAssignmentList)

        // then the assignment should produce a valid result
        val expected = """
            {
              "a1" : "also_valid",
              "pnf-id" : "valid_value"
            }
        """.trimIndent()
        assertEquals(expected, outcome.trimIndent(), "unexpected outcome generated")
    }

    @Test
    fun `generateResourceDataForAssignments - resource without value is not resolved as null`() {
        // given a valid resource assignment
        val resourceAssignmentWithNullValue = createResourceAssignmentForTest(null)

        // and a list containing that resource assignment
        val resourceAssignmentList = listOf<ResourceAssignment>(resourceAssignmentWithNullValue)

        // when the values of the resources are evaluated
        val outcome = ResourceAssignmentUtils.generateResourceDataForAssignments(resourceAssignmentList)

        // then the assignment should produce a valid result
        val expected = "{\n" + "  \"pnf-id\" : \"\${pnf-id}\"\n" + "}"
        assertEquals(expected, outcome.replace("\r\n", "\n"), "unexpected outcome generated")
    }

    @Test
    fun generateResolutionSummaryDataTest() {
        val resourceAssignment = createResourceAssignmentForTest(null)
        val resourceDefinition = ResourceDefinition()
        val nodeTemplate = NodeTemplate().apply {
            properties = mutableMapOf("resolved-payload" to JacksonUtils.jsonNode("{\"mock\": true}"))
        }
        resourceDefinition.sources = mutableMapOf("input" to nodeTemplate)
        resourceDefinition.property = PropertyDefinition().apply {
            this.description = "pnf-id"
            this.metadata = mutableMapOf("aai-path" to "//path/in/aai")
        }

        val result = ResourceAssignmentUtils.generateResolutionSummaryData(
            listOf(resourceAssignment), mapOf("pnf-id" to resourceDefinition)
        )

        assertEquals(
            """
            {
                "resolution-summary":[
                    {
                        "name":"pnf-id",
                        "value":"",
                        "required":false,
                        "type":"string",
                        "key-identifiers":[],
                        "dictionary-description":"pnf-id",
                        "dictionary-metadata":[
                            {"name":"aai-path","value":"//path/in/aai"}
                        ],
                        "dictionary-name":"pnf-id",
                        "dictionary-source":"input",
                        "request-payload":{"mock":true},
                        "status":"",
                        "message":""
                    }
                ]
            }
        """.replace("\n|\\s".toRegex(), ""),
            result
        )
    }

    private fun createResourceAssignmentForTest(resourceValue: String?, resourceName: String = "pnf-id"): ResourceAssignment {
        val valueForTest = if (resourceValue == null) null else TextNode(resourceValue)
        val resourceAssignmentForTest = ResourceAssignment().apply {
            name = resourceName
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
    fun parseResponseNodeTestForPrimitivesTypes() {
        var outcome = prepareResponseNodeForTest(
            "sample-value", "string", "",
            inputMapToTestPrimitiveTypeWithValue
        )
        assertEquals(
            expectedValueToTestPrimitiveType,
            outcome,
            "Unexpected outcome returned for primitive type of simple String"
        )
        assertEquals(0, resourceAssignment.keyIdentifiers.size)

        outcome = prepareResponseNodeForTest(
            "sample-key-value", "string", "",
            inputMapToTestPrimitiveTypeWithKeyValue
        )
        assertEquals(
            expectedValueToTestPrimitiveType,
            outcome,
            "Unexpected outcome returned for primitive type of key-value String"
        )
        assertEquals(
            expectedValueToTestPrimitiveType,
            resourceAssignment.keyIdentifiers[0].value
        )
    }

    @Test
    fun parseResponseNodeTestForCollectionsOfString() {
        var outcome = prepareResponseNodeForTest(
            "listOfString", "list",
            "string", inputMapToTestCollectionOfPrimitiveType
        )
        assertEquals(
            expectedValueToTesCollectionOfPrimitiveType,
            outcome,
            "unexpected outcome returned for list of String"
        )

        val expectedKeyIdentifierValue = JacksonUtils.getJsonNode(outcome.map { it["ip"] })
        assertEquals(
            expectedKeyIdentifierValue,
            resourceAssignment.keyIdentifiers[0].value
        )

        // FIXME("Map is not collection type, It is known complex type")
        // outcome = prepareResponseNodeForTest(
        //     "mapOfString", "map", "string",
        //     inputMapToTestCollectionOfPrimitiveType
        // )
        // assertEquals(
        //     expectedValueToTesCollectionOfPrimitiveType,
        //     outcome,
        //     "unexpected outcome returned for map of String"
        // )
    }

    @Test
    fun parseResponseNodeTestForCollectionsOfComplexType() {
        var outcome = prepareResponseNodeForTest(
            "listOfMyDataTypeWithOneOutputKeyMapping", "list",
            "ip-address", inputMapToTestCollectionOfComplexTypeWithOneOutputKeyMapping
        )
        assertEquals(
            expectedValueToTestCollectionOfComplexTypeWithOneOutputKeyMapping,
            outcome,
            "unexpected outcome returned for list of String"
        )

        outcome = prepareResponseNodeForTest(
            "listOfMyDataTypeWithAllOutputKeyMapping", "list",
            "ip-address", inputMapToTestCollectionOfComplexTypeWithAllOutputKeyMapping
        )
        assertEquals(
            expectedValueToTestCollectionOfComplexTypeWithAllOutputKeyMapping,
            outcome,
            "unexpected outcome returned for list of String"
        )
    }

    @Test
    fun `parseResponseNodeTestForComplexType find one output key mapping`() {
        val outcome = prepareResponseNodeForTest(
            "complexTypeOneKeys", "host",
            "", inputMapToTestComplexTypeWithOneOutputKeyMapping
        )
        assertEquals(
            expectedValueToTestComplexTypeWithOneOutputKeyMapping,
            outcome,
            "Unexpected outcome returned for complex type"
        )
        assertEquals(
            expectedValueToTestComplexTypeWithOneOutputKeyMapping["host"],
            resourceAssignment.keyIdentifiers[0].value
        )
    }

    @Test
    fun `parseResponseNodeTestForComplexType find all output key mapping`() {
        val outcome = prepareResponseNodeForTest(
            "complexTypeAllKeys", "host",
            "", inputMapToTestComplexTypeWithAllOutputKeyMapping
        )
        assertEquals(
            expectedValueToTestComplexTypeWithAllOutputKeyMapping,
            outcome,
            "Unexpected outcome returned for complex type"
        )
        assertEquals(2, resourceAssignment.keyIdentifiers.size)
        assertEquals(
            expectedValueToTestComplexTypeWithAllOutputKeyMapping["name"],
            resourceAssignment.keyIdentifiers[0].value
        )

        assertEquals(
            expectedValueToTestComplexTypeWithAllOutputKeyMapping["ipAddress"],
            resourceAssignment.keyIdentifiers[1].value
        )
    }

    @Test
    fun `transform resolved value with inline template`() {
        resourceAssignmentRuntimeService.putResolutionStore("vnf_name", "abc-vnf".asJsonType())
        resourceAssignment = ResourceAssignment()
        resourceAssignment.name = "int_pktgen_private_net_id"
        resourceAssignment.property = PropertyDefinition()
        resourceAssignment.property!!.type = "string"
        val value = "".asJsonType()

        // Enable transform template
        resourceAssignment.property!!.metadata =
            mutableMapOf(METADATA_TRANSFORM_TEMPLATE to "\${vnf_name}_private2")

        ResourceAssignmentUtils
            .setResourceDataValue(resourceAssignment, resourceAssignmentRuntimeService, value)

        assertEquals(
            "abc-vnf_private2",
            resourceAssignment.property!!.value!!.asText()
        )
    }

    private fun initInputMapAndExpectedValuesForPrimitiveType() {
        inputMapToTestPrimitiveTypeWithValue = "1.2.3.1".asJsonType()
        val keyValue = mutableMapOf<String, String>()
        keyValue["value"] = "1.2.3.1"
        inputMapToTestPrimitiveTypeWithKeyValue = keyValue.asJsonType()
        expectedValueToTestPrimitiveType = TextNode("1.2.3.1")
    }

    private fun initInputMapAndExpectedValuesForCollection() {
        val listOfIps = arrayListOf("1.2.3.1", "1.2.3.2", "1.2.3.3")
        val arrayNodeForList1 = JacksonUtils.objectMapper.createArrayNode()
        listOfIps.forEach {
            val arrayChildNode = JacksonUtils.objectMapper.createObjectNode()
            arrayChildNode.set<JsonNode>("value", it.asJsonPrimitive())
            arrayNodeForList1.add(arrayChildNode)
        }
        inputMapToTestCollectionOfPrimitiveType = arrayNodeForList1

        expectedValueToTesCollectionOfPrimitiveType = arrayListOf(
            ExpectedResponseIp("1.2.3.1"),
            ExpectedResponseIp("1.2.3.2"), ExpectedResponseIp("1.2.3.3")
        ).asJsonType()

        val listOfIpAddresses = arrayListOf(
            IpAddress("1111", "1.2.3.1").asJsonType(),
            IpAddress("2222", "1.2.3.2").asJsonType(), IpAddress("3333", "1.2.3.3").asJsonType()
        )
        val arrayNodeForList2 = JacksonUtils.objectMapper.createArrayNode()
        listOfIpAddresses.forEach {
            val arrayChildNode = JacksonUtils.objectMapper.createObjectNode()
            arrayChildNode.set<JsonNode>("value", it.asJsonType())
            arrayNodeForList2.add(arrayChildNode)
        }
        inputMapToTestCollectionOfComplexTypeWithOneOutputKeyMapping = arrayNodeForList2

        val arrayNodeForList3 = JacksonUtils.objectMapper.createArrayNode()
        var childNode = JacksonUtils.objectMapper.createObjectNode()
        childNode.set<JsonNode>("port", "1111".asJsonPrimitive())
        childNode.set<JsonNode>("ip", "1.2.3.1".asJsonPrimitive())
        arrayNodeForList3.add(childNode)
        childNode = JacksonUtils.objectMapper.createObjectNode()
        childNode.set<JsonNode>("port", "2222".asJsonPrimitive())
        childNode.set<JsonNode>("ip", "1.2.3.2".asJsonPrimitive())
        arrayNodeForList3.add(childNode)
        childNode = JacksonUtils.objectMapper.createObjectNode()
        childNode.set<JsonNode>("port", "3333".asJsonPrimitive())
        childNode.set<JsonNode>("ip", "1.2.3.3".asJsonPrimitive())
        arrayNodeForList3.add(childNode)
        inputMapToTestCollectionOfComplexTypeWithAllOutputKeyMapping = arrayNodeForList3

        expectedValueToTestCollectionOfComplexTypeWithOneOutputKeyMapping = arrayListOf(
            ExpectedResponseIpAddress(IpAddress("1111", "1.2.3.1")),
            ExpectedResponseIpAddress(IpAddress("2222", "1.2.3.2")),
            ExpectedResponseIpAddress(
                IpAddress("3333", "1.2.3.3")
            )
        ).asJsonType()
        expectedValueToTestCollectionOfComplexTypeWithAllOutputKeyMapping = arrayListOf(
            IpAddress("1111", "1.2.3.1"),
            IpAddress("2222", "1.2.3.2"),
            IpAddress("3333", "1.2.3.3")
        ).asJsonType()
    }

    private fun initInputMapAndExpectedValuesForComplexType() {
        val mapOfComplexType = mutableMapOf<String, JsonNode>()
        mapOfComplexType["value"] = Host("my-ipAddress", IpAddress("1111", "1.2.3.1")).asJsonType()
        mapOfComplexType["port"] = "8888".asJsonType()
        mapOfComplexType["something"] = "1.2.3.2".asJsonType()
        inputMapToTestComplexTypeWithOneOutputKeyMapping = mapOfComplexType.asJsonType()

        val objectNode = JacksonUtils.objectMapper.createObjectNode()
        expectedValueToTestComplexTypeWithOneOutputKeyMapping =
            objectNode.set("host", Host("my-ipAddress", IpAddress("1111", "1.2.3.1")).asJsonType())

        val childNode1 = JacksonUtils.objectMapper.createObjectNode()
        childNode1.set<JsonNode>("name", "my-ipAddress".asJsonPrimitive())
        childNode1.set<JsonNode>("ipAddress", IpAddress("1111", "1.2.3.1").asJsonType())
        childNode1.set<JsonNode>("port", "8888".asJsonType())
        childNode1.set<JsonNode>("something", IpAddress("2222", "1.2.3.1").asJsonType())
        inputMapToTestComplexTypeWithAllOutputKeyMapping = childNode1

        val childNode2 = JacksonUtils.objectMapper.createObjectNode()
        childNode2.set<JsonNode>("name", "my-ipAddress".asJsonPrimitive())
        childNode2.set<JsonNode>("ipAddress", IpAddress("1111", "1.2.3.1").asJsonType())
        expectedValueToTestComplexTypeWithAllOutputKeyMapping = childNode2
    }

    private fun prepareResponseNodeForTest(
        dictionary_source: String,
        sourceType: String,
        entrySchema: String,
        response: Any
    ): JsonNode {

        resourceAssignment = when (sourceType) {
            "list" -> {
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

        return ResourceAssignmentUtils.parseResponseNode(
            responseNode,
            resourceAssignment,
            resourceAssignmentRuntimeService,
            outputKeyMapping
        )
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

    private fun prepareRADataDictionaryCollection(
        dictionary_source: String,
        sourceType: String,
        schema: String
    ): ResourceAssignment {
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

    private fun prepareRADataDictionaryComplexType(
        dictionary_source: String,
        sourceType: String,
        schema: String
    ): ResourceAssignment {
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
            "sample-key-value", "sample-value" -> {
                // Primary Type
                if (dictionary_source == "sample-key-value")
                    outputMapping["sample-ip"] = "value"
            }
            "listOfString", "mapOfString" -> {
                // List of string
                outputMapping["ip"] = "value"
            }
            "listOfMyDataTypeWithOneOutputKeyMapping", "listOfMyDataTypeWithAllOutputKeyMapping" -> {
                // List or map of complex Type
                if (dictionary_source == "listOfMyDataTypeWithOneOutputKeyMapping")
                    outputMapping["ipAddress"] = "value"
                else {
                    outputMapping["port"] = "port"
                    outputMapping["ip"] = "ip"
                }
            }
            else -> {
                // Complex Type
                if (dictionary_source == "complexTypeOneKeys")
                    outputMapping["host"] = "value"
                else {
                    outputMapping["name"] = "name"
                    outputMapping["ipAddress"] = "ipAddress"
                }
            }
        }
        return outputMapping
    }
}

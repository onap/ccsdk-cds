/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018-2019 IBM.
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
import com.fasterxml.jackson.databind.node.NullNode
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.TestConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintRuntimeUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 *
 *
 * @author Brinda Santh
 */
class BluePrintRuntimeServiceTest {

    private val log = LoggerFactory.getLogger(this::class.toString())

    @Test
    fun `test Resolve NodeTemplate Properties`() {
        log.info("************************ testResolveNodeTemplateProperties **********************")

        val bluePrintRuntimeService = getBluePrintRuntimeService()

        val inputDataPath = "src/test/resources/data/default-context.json"

        val inputNode: JsonNode = JacksonUtils.jsonNodeFromFile(inputDataPath)
        bluePrintRuntimeService.assignInputs(inputNode)

        val propContext: MutableMap<String, JsonNode> = bluePrintRuntimeService
            .resolveNodeTemplateProperties("activate-process")

        assertNotNull(propContext, "Failed to populate interface property values")
    }

    @Test
    fun `test Resolve Relationship Properties`() {
        log.info("************************ testResolveRelationshipTemplateProperties **********************")

        val bluePrintRuntimeService = getBluePrintRuntimeService()

        val inputDataPath = "src/test/resources/data/default-context.json"

        val inputNode: JsonNode = JacksonUtils.jsonNodeFromFile(inputDataPath)
        bluePrintRuntimeService.assignInputs(inputNode)

        val propContext: MutableMap<String, JsonNode> = bluePrintRuntimeService
            .resolveRelationshipTemplateProperties("cli-device-properties")

        assertNotNull(propContext, "Failed to populate relationship property values")
        assertEquals(
            "localhost".asJsonPrimitive(),
            propContext["connection-config"]!!.get("host"),
            "failed to resolve expression"
        )
    }

    @Test
    fun `test resolve NodeTemplate Capability Properties`() {
        log.info("************************ testResolveNodeTemplateRequirementProperties **********************")
        val bluePrintRuntimeService = getBluePrintRuntimeService()

        val executionContext = bluePrintRuntimeService.getExecutionContext()

        BluePrintRuntimeUtils.assignInputsFromClassPathFile(
            bluePrintRuntimeService.bluePrintContext(),
            "data/default-context.json", executionContext
        )

        val assignmentParams = "{\n" +
            "            \"ipAddress\": \"127.0.0.1\",\n" +
            "            \"hostName\": \"vnf-host\"\n" +
            "          }"

        bluePrintRuntimeService.setNodeTemplateAttributeValue(
            "resource-assignment", "assignment-params",
            JacksonUtils.jsonNode(assignmentParams)
        )

        val capProperties = bluePrintRuntimeService.resolveNodeTemplateCapabilityProperties(
            "sample-netconf-device",
            "netconf"
        )
        assertNotNull(capProperties, "Failed to populate capability property values")
        assertEquals(
            capProperties["target-ip-address"],
            "127.0.0.1".asJsonPrimitive(),
            "Failed to populate parameter target-ip-address"
        )
        assertEquals(
            capProperties["port-number"],
            JacksonUtils.jsonNodeFromObject(830),
            "Failed to populate parameter port-number"
        )
    }

    @Test
    fun `test Resolve NodeTemplate Interface Operation Inputs`() {
        log.info("************************ testResolveNodeTemplateInterfaceOperationInputs **********************")

        val bluePrintRuntimeService = getBluePrintRuntimeService()

        val executionContext = bluePrintRuntimeService.getExecutionContext()

        BluePrintRuntimeUtils.assignInputsFromClassPathFile(
            bluePrintRuntimeService.bluePrintContext(),
            "data/default-context.json", executionContext
        )

        val inContext: MutableMap<String, JsonNode> = bluePrintRuntimeService
            .resolveNodeTemplateInterfaceOperationInputs(
                "resource-assignment",
                "ResourceResolutionComponent", "process"
            )

        assertNotNull(inContext, "Failed to populate interface input property values")
        assertEquals(
            inContext["action-name"],
            JacksonUtils.jsonNodeFromObject("sample-action"),
            "Failed to populate parameter action-name"
        )
        assertEquals(
            inContext["request-id"],
            JacksonUtils.jsonNodeFromObject("12345"),
            "Failed to populate parameter action-name"
        )
    }

    @Test
    fun `test Resolve NodeTemplate Interface Operation Outputs`() {
        log.info("************************ testResolveNodeTemplateInterfaceOperationOutputs **********************")

        val bluePrintRuntimeService = getBluePrintRuntimeService()

        bluePrintRuntimeService.setNodeTemplateAttributeValue(
            "resource-assignment",
            "assignment-params",
            NullNode.getInstance()
        )

        bluePrintRuntimeService.resolveNodeTemplateInterfaceOperationOutputs(
            "resource-assignment",
            "ResourceResolutionComponent", "process"
        )

        val outputStatus = bluePrintRuntimeService.getNodeTemplateOperationOutputValue(
            "resource-assignment",
            "ResourceResolutionComponent", "process", "status"
        )
        assertEquals("success".asJsonPrimitive(), outputStatus, "Failed to get operation property status")

        val outputParams = bluePrintRuntimeService.getNodeTemplateOperationOutputValue(
            "resource-assignment",
            "ResourceResolutionComponent", "process", "resource-assignment-params"
        )
        assertEquals(
            NullNode.getInstance(),
            outputParams,
            "Failed to get operation property resource-assignment-params"
        )
    }

    @Test
    fun `test NodeTemplate Context Property`() {
        log.info("************************ testNodeTemplateContextProperty **********************")
        val bluePrintRuntimeService = getBluePrintRuntimeService()

        bluePrintRuntimeService.setNodeTemplateAttributeValue(
            "resource-assignment-ra-component", "context1",
            JacksonUtils.jsonNodeFromObject("context1-value")
        )
        bluePrintRuntimeService.setNodeTemplateAttributeValue(
            "resource-assignment-ra-component", "context2",
            JacksonUtils.jsonNodeFromObject("context2-value")
        )

        val keys = listOf("context1", "context2")

        val jsonValueNode =
            bluePrintRuntimeService.getJsonForNodeTemplateAttributeProperties("resource-assignment-ra-component", keys)
        assertNotNull(jsonValueNode, "Failed to get Json for Node Template Context Properties")
        log.info("JSON Prepared Value Context {}", jsonValueNode)
    }

    @Test
    fun `test Resolve DSL Properties`() {
        log.info("************************ resolveDSLExpression **********************")

        val bluePrintRuntimeService = getBluePrintRuntimeService()

        bluePrintRuntimeService.setInputValue("rest-user-name", "sample-username".asJsonPrimitive())

        val resolvedJsonNode: JsonNode = bluePrintRuntimeService.resolveDSLExpression("dynamic-rest-source")
        assertNotNull(resolvedJsonNode, "Failed to populate dsl property values")
    }

    @Test
    fun `test Resolve Workflow Outputs`() {
        log.info("************************ resolvePropertyAssignments **********************")
        val bluePrintRuntimeService = getBluePrintRuntimeService()

        val assignmentParams = "{\"ipAddress\": \"127.0.0.1\", \"hostName\": \"vnf-host\"}"

        bluePrintRuntimeService.setNodeTemplateAttributeValue(
            "resource-assignment", "assignment-params",
            JacksonUtils.jsonNode(assignmentParams)
        )

        val resolvedJsonNode = bluePrintRuntimeService.resolveWorkflowOutputs("resource-assignment")
        assertNotNull(resolvedJsonNode, "Failed to populate workflow output property values")
    }

    @Test
    fun `test resolvePropertyDefinitions using sub attributes`() {
        val bluePrintRuntimeService = getBluePrintRuntimeService()

        bluePrintRuntimeService.setNodeTemplateAttributeValue(
            "resource-assignment", "assignment-map",
            JacksonUtils.jsonNode(
                """
                    {
                      "a-prefix":{
                        "an-object":{
                          "a-key":123
                        }
                      }
                    }
                """.trimIndent()
            )
        )

        val propertyDefinitions = mutableMapOf<String, PropertyDefinition>(
            "resolution" to PropertyDefinition().apply {
                this.type = "json"
                this.value = JacksonUtils.jsonNode(
                    """
                        {
                          "get_attribute":[
                            "resource-assignment",
                            "",
                            "assignment-map",
                            "a-prefix",
                            "an-object",
                            "a-key"
                            ]
                        }
                    """.trimIndent()
                )
            }
        )

        val result = bluePrintRuntimeService.resolvePropertyDefinitions("workflow", "WORKFLOW", propertyDefinitions)

        assertEquals("123", result["resolution"]!!.asText())
    }

    private fun getBluePrintRuntimeService(): BluePrintRuntimeService<MutableMap<String, JsonNode>> {
        val blueprintBasePath = normalizedPathName(TestConstants.PATH_TEST_BLUEPRINTS_BASECONFIG)
        val blueprintRuntime = BluePrintMetadataUtils.bluePrintRuntime("1234", blueprintBasePath)
        val checkProcessId = blueprintRuntime.get(BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID)
        val checkBasePath = blueprintRuntime.get(BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH)

        assertEquals(
            "1234".asJsonPrimitive(),
            checkProcessId, "Failed to get process id after runtime creation"
        )
        assertEquals(
            blueprintBasePath.asJsonPrimitive(),
            checkBasePath, "Failed to get base path after runtime creation"
        )

        return blueprintRuntime
    }
}

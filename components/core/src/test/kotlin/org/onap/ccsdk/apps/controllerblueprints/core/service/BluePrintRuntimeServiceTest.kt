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

package org.onap.ccsdk.apps.controllerblueprints.core.service

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import org.junit.Test
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintRuntimeUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils.jsonNodeFromFile
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils.jsonNodeFromObject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 *
 *
 * @author Brinda Santh
 */
class BluePrintRuntimeServiceTest {
    private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())

    @Test
    fun testResolveNodeTemplateProperties() {
        log.info("************************ testResolveNodeTemplateProperties **********************")

        val bluePrintRuntimeService = getBluePrintRuntimeService()

        val inputDataPath = "src/test/resources/data/default-context.json"

        val inputNode: JsonNode = jsonNodeFromFile(inputDataPath)
        bluePrintRuntimeService.assignInputs(inputNode)

        val propContext: MutableMap<String, JsonNode> = bluePrintRuntimeService.resolveNodeTemplateProperties("activate-process")

        assertNotNull(propContext, "Failed to populate interface property values")
        assertEquals(propContext["process-name"], jsonNodeFromObject("sample-action"), "Failed to populate parameter process-name")
        assertEquals(propContext["version"], jsonNodeFromObject("sample-action"), "Failed to populate parameter version")
    }

    @Test
    fun testResolveNodeTemplateInterfaceOperationInputs() {
        log.info("************************ testResolveNodeTemplateInterfaceOperationInputs **********************")

        val bluePrintRuntimeService = getBluePrintRuntimeService()

        val executionContext = bluePrintRuntimeService.getExecutionContext()

        BluePrintRuntimeUtils.assignInputsFromClassPathFile(bluePrintRuntimeService.bluePrintContext(),
                "data/default-context.json", executionContext)

        val inContext: MutableMap<String, JsonNode> = bluePrintRuntimeService.resolveNodeTemplateInterfaceOperationInputs("resource-assignment",
                "ResourceAssignmentComponent", "process")

        assertNotNull(inContext, "Failed to populate interface input property values")
        assertEquals(inContext["action-name"], jsonNodeFromObject("sample-action"), "Failed to populate parameter action-name")
        assertEquals(inContext["request-id"], jsonNodeFromObject("12345"), "Failed to populate parameter action-name")
        assertEquals(inContext["template-content"], jsonNodeFromObject("This is Sample Velocity Template"), "Failed to populate parameter action-name")
    }

    @Test
    fun testResolveNodeTemplateInterfaceOperationOutputs() {
        log.info("************************ testResolveNodeTemplateInterfaceOperationOutputs **********************")

        val bluePrintRuntimeService = getBluePrintRuntimeService()

        bluePrintRuntimeService.setNodeTemplateAttributeValue("resource-assignment", "assignment-params", NullNode.getInstance())

        bluePrintRuntimeService.resolveNodeTemplateInterfaceOperationOutputs("resource-assignment",
                "ResourceAssignmentComponent", "process")

        val outputStatus = bluePrintRuntimeService.getNodeTemplateOperationOutputValue("resource-assignment",
                "ResourceAssignmentComponent", "process", "status")
        assertEquals("success".asJsonPrimitive(), outputStatus, "Failed to get operation property status")

        val outputParams = bluePrintRuntimeService.getNodeTemplateOperationOutputValue("resource-assignment",
                "ResourceAssignmentComponent", "process", "resource-assignment-params")
        assertEquals(NullNode.getInstance(), outputParams, "Failed to get operation property resource-assignment-params")

    }

    @Test
    fun testNodeTemplateContextProperty() {
        log.info("************************ testNodeTemplateContextProperty **********************")
        val bluePrintRuntimeService = getBluePrintRuntimeService()

        bluePrintRuntimeService.setNodeTemplateAttributeValue("resource-assignment-ra-component", "context1",
                jsonNodeFromObject("context1-value"))
        bluePrintRuntimeService.setNodeTemplateAttributeValue("resource-assignment-ra-component", "context2",
                jsonNodeFromObject("context2-value"))

        val keys = listOf("context1", "context2")

        val jsonValueNode = bluePrintRuntimeService.getJsonForNodeTemplateAttributeProperties("resource-assignment-ra-component", keys)
        assertNotNull(jsonValueNode, "Failed to get Json for Node Template Context Properties")
        log.info("JSON Prepared Value Context {}", jsonValueNode)

    }

    private fun getBluePrintRuntimeService(): BluePrintRuntimeService<MutableMap<String, JsonNode>> {
        val blueprintBasePath: String = ("./../model-catalog/blueprint-model/starter-blueprint/baseconfiguration")
        val blueprintRuntime = BluePrintMetadataUtils.getBluePrintRuntime("1234", blueprintBasePath)
        val checkBasePath = blueprintRuntime.get(BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH)

        assertEquals(blueprintBasePath.asJsonPrimitive(), checkBasePath, "Failed to get base path after runtime creation")

        return blueprintRuntime
    }

}
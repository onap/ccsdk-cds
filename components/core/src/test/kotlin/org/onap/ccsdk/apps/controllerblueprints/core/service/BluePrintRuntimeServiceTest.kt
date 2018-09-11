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

import com.fasterxml.jackson.databind.JsonNode
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.factory.BluePrintParserFactory
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintRuntimeUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils.jsonNodeFromFile
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils.jsonNodeFromObject
import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 *
 *
 * @author Brinda Santh
 */
class BluePrintRuntimeServiceTest {
    private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())
    val basepath = "load/blueprints"


    @Before
    fun setUp(): Unit {

    }

    @Test
    fun testResolveNodeTemplateProperties() {
        log.info("************************ testResolveNodeTemplateProperties **********************")

        val bluePrintRuntimeService = getBluePrintRuntimeService()

        val inputDataPath = "src/test/resources/data/default-context.json"

        val inputNode: JsonNode = jsonNodeFromFile(inputDataPath)
        bluePrintRuntimeService.assignInputs(inputNode)

        val propContext: MutableMap<String, Any?> = bluePrintRuntimeService.resolveNodeTemplateProperties("resource-assignment-action")
        log.info("Context {}", bluePrintRuntimeService.context)

        assertNotNull(propContext, "Failed to populate interface property values")
        assertEquals(propContext.get("mode"), jsonNodeFromObject("sync"), "Failed to populate parameter process-name")
        assertEquals(propContext.get("version"), jsonNodeFromObject("1.0.0"), "Failed to populate parameter version")
    }

    @Test
    fun testResolveNodeTemplateInterfaceOperationInputs() {
        log.info("************************ testResolveNodeTemplateInterfaceOperationInputs **********************")
        val bluePrintContext: BluePrintContext = BluePrintParserFactory.instance(BluePrintConstants.TYPE_DEFAULT)!!
                .readBlueprintFile("baseconfiguration/Definitions/activation-blueprint.json", basepath)
        assertNotNull(bluePrintContext, "Failed to populate Blueprint context")

        val context: MutableMap<String, Any> = hashMapOf()
        context[BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH] = basepath.plus("/simple-baseconfig")

        val inputDataPath = "src/test/resources/data/default-context.json"
        BluePrintRuntimeUtils.assignInputsFromFile(bluePrintContext, inputDataPath, context)


        val bluePrintRuntimeService = BluePrintRuntimeService(bluePrintContext, context)

        log.info("Prepared Context {}", context)

        val inContext: MutableMap<String, Any?> = bluePrintRuntimeService.resolveNodeTemplateInterfaceOperationInputs("resource-assignment-ra-component",
                "org-onap-ccsdk-config-assignment-service-ConfigAssignmentNode", "process")

        log.info("In Context {}", inContext)

        assertNotNull(inContext, "Failed to populate interface input property values")
        assertEquals(inContext.get("action-name"), jsonNodeFromObject("sample-action"), "Failed to populate parameter action-name")
        assertEquals(inContext.get("request-id"), jsonNodeFromObject("12345"), "Failed to populate parameter action-name")
    }

    @Test
    fun testResolveNodeTemplateInterfaceOperationOutputs() {
        log.info("************************ testResolveNodeTemplateInterfaceOperationOutputs **********************")

        val bluePrintRuntimeService = getBluePrintRuntimeService()

        val successValue: JsonNode = jsonNodeFromObject("Success")
        val paramValue: JsonNode = jsonNodeFromObject("param-content")

        bluePrintRuntimeService.setNodeTemplateAttributeValue("resource-assignment-ra-component", "params", paramValue)

        bluePrintRuntimeService.resolveNodeTemplateInterfaceOperationOutputs("resource-assignment-ra-component",
                "org-onap-ccsdk-config-assignment-service-ConfigAssignmentNode", "process")

        val resourceAssignmentParamsNode = bluePrintRuntimeService.getNodeTemplateOperationOutputValue("resource-assignment-ra-component",
                "org-onap-ccsdk-config-assignment-service-ConfigAssignmentNode", "process", "resource-assignment-params")

        val statusNode = bluePrintRuntimeService.getNodeTemplateOperationOutputValue("resource-assignment-ra-component",
                "org-onap-ccsdk-config-assignment-service-ConfigAssignmentNode", "process", "status")

        assertEquals(paramValue, resourceAssignmentParamsNode, "Failed to get operation property resource-assignment-params")

        assertEquals(successValue, statusNode, "Failed to get operation property status")


    }

    @Test
    fun testNodeTemplateContextProperty() {
        log.info("************************ testNodeTemplateContextProperty **********************")
        val bluePrintRuntimeService = getBluePrintRuntimeService()

        bluePrintRuntimeService.setNodeTemplateAttributeValue("resource-assignment-ra-component", "context1",
                jsonNodeFromObject("context1-value"))
        bluePrintRuntimeService.setNodeTemplateAttributeValue("resource-assignment-ra-component", "context2",
                jsonNodeFromObject("context2-value"))

        log.info("Context {}", bluePrintRuntimeService.context)

        val keys = listOf("context1", "context2")

        val jsonValueNode = bluePrintRuntimeService.getJsonForNodeTemplateAttributeProperties("resource-assignment-ra-component", keys)
        assertNotNull(jsonValueNode, "Failed to get Json for Node Template Context Properties")
        log.info("JSON Prepared Value Context {}", jsonValueNode)

    }

    private fun getBluePrintRuntimeService(): BluePrintRuntimeService {
        val bluePrintContext: BluePrintContext = BluePrintParserFactory.instance(BluePrintConstants.TYPE_DEFAULT)!!
                .readBlueprintFile("baseconfiguration/Definitions/activation-blueprint.json", basepath)
        assertNotNull(bluePrintContext, "Failed to populate Blueprint context")

        val context: MutableMap<String, Any> = hashMapOf()
        context[BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH] = basepath.plus("/simple-baseconfig")

        return BluePrintRuntimeService(bluePrintContext, context)
    }

}
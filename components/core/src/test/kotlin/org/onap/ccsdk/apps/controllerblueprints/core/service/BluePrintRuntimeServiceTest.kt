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

package org.onap.ccsdk.apps.controllerblueprints.core.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
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
        val bluePrintContext: BluePrintContext = BluePrintParserFactory.instance(BluePrintConstants.TYPE_DEFAULT)!!
                .readBlueprintFile("baseconfiguration/Definitions/activation-blueprint.json", basepath)

        val context: MutableMap<String, Any> = hashMapOf()
        context[BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH] = basepath.plus("/simple-baseconfig")
        val bluePrintRuntimeService = BluePrintRuntimeService(bluePrintContext, context)

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
                "org-onap-sdnc-config-assignment-service-ConfigAssignmentNode", "process")

        log.info("In Context {}", inContext)

        assertNotNull(inContext, "Failed to populate interface input property values")
        assertEquals(inContext.get("action-name"), jsonNodeFromObject("sample-action"), "Failed to populate parameter action-name")
        assertEquals(inContext.get("request-id"), jsonNodeFromObject("12345"), "Failed to populate parameter action-name")
    }

    @Test
    fun testResolveNodeTemplateInterfaceOperationOutputs() {
        log.info("************************ testResolveNodeTemplateInterfaceOperationOutputs **********************")
        val bluePrintContext: BluePrintContext = BluePrintParserFactory.instance(BluePrintConstants.TYPE_DEFAULT)!!
                .readBlueprintFile("baseconfiguration/Definitions/activation-blueprint.json", basepath)
        assertNotNull(bluePrintContext, "Failed to populate Blueprint context")

        val context: MutableMap<String, Any> = hashMapOf()
        context[BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH] = basepath.plus("/simple-baseconfig")

        val bluePrintRuntimeService = BluePrintRuntimeService(bluePrintContext, context)

        val componentContext: MutableMap<String, Any?> = hashMapOf()
        val successValue: JsonNode = jsonNodeFromObject("Success")
        componentContext["resource-assignment-ra-component.org-onap-sdnc-config-assignment-service-ConfigAssignmentNode.process.status"] = successValue
        componentContext["resource-assignment-ra-component.org-onap-sdnc-config-assignment-service-ConfigAssignmentNode.process.resource-assignment-params"] = null

        bluePrintRuntimeService.resolveNodeTemplateInterfaceOperationOutputs("resource-assignment-ra-component",
                "org-onap-sdnc-config-assignment-service-ConfigAssignmentNode", "process", componentContext)

        assertEquals(NullNode.instance,
                context.get("node_templates/resource-assignment-ra-component/interfaces/org-onap-sdnc-config-assignment-service-ConfigAssignmentNode/operations/process/properties/resource-assignment-params"),
                "Failed to get operation property resource-assignment-params")

        assertEquals(successValue,
                context.get("node_templates/resource-assignment-ra-component/interfaces/org-onap-sdnc-config-assignment-service-ConfigAssignmentNode/operations/process/properties/status"),
                "Failed to get operation property status")


    }
}
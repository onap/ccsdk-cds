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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 *
 *
 * @author Brinda Santh
 */
class BluePrintRuntimeServiceTest {
    private val logger: Logger = LoggerFactory.getLogger(this::class.toString())
    val basepath = "load/blueprints"


    @Before
    fun setUp(): Unit {

    }

    @Test
    fun testResolveNodeTemplateProperties() {
        logger.info("************************ testResolveNodeTemplateProperties **********************")
        val bluePrintContext: BluePrintContext = BluePrintParserFactory.instance(BluePrintConstants.TYPE_DEFAULT)!!
                .readBlueprintFile("baseconfiguration/Definitions/activation-blueprint.json", basepath)

        val context: MutableMap<String, Any> = hashMapOf()
        context[BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH] = basepath.plus("/simple-baseconfig")
        val bluePrintRuntimeService = BluePrintRuntimeService(bluePrintContext, context)

        val inputDataPath =  "src/test/resources/data/default-context.json"

        val inputNode: JsonNode = jsonNodeFromFile(inputDataPath)
        bluePrintRuntimeService.assignInputs(inputNode)

        val propContext: MutableMap<String, Any?> = bluePrintRuntimeService.resolveNodeTemplateProperties("activate-process")
        logger.info("Context {}" ,bluePrintRuntimeService.context)

        assertNotNull(propContext, "Failed to populate interface property values")
        assertEquals(propContext.get("process-name"), jsonNodeFromObject("sample-action"), "Failed to populate parameter process-name")
        assertEquals(propContext.get("version"), jsonNodeFromObject("sample-action"), "Failed to populate parameter version")
    }

    @Test
    fun testResolveNodeTemplateInterfaceOperationInputs() {
        logger.info("************************ testResolveNodeTemplateInterfaceOperationInputs **********************")
        val bluePrintContext: BluePrintContext = BluePrintParserFactory.instance(BluePrintConstants.TYPE_DEFAULT)!!
                .readBlueprintFile("baseconfiguration/Definitions/activation-blueprint.json", basepath)
        assertNotNull(bluePrintContext, "Failed to populate Blueprint context")

        val context: MutableMap<String, Any> = hashMapOf()
        context[BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH] = basepath.plus("/simple-baseconfig")

        val inputDataPath =  "src/test/resources/data/default-context.json"
        BluePrintRuntimeUtils.assignInputsFromFile(bluePrintContext, inputDataPath, context)


        val bluePrintRuntimeService = BluePrintRuntimeService(bluePrintContext, context)

        logger.info("Prepared Context {}" ,context)

        val inContext: MutableMap<String, Any?> = bluePrintRuntimeService.resolveNodeTemplateInterfaceOperationInputs("resource-assignment",
                "DefaultComponentNode", "process")

        logger.trace("In Context {}" ,inContext)

        assertNotNull(inContext, "Failed to populate interface input property values")
        assertEquals(inContext.get("action-name"), jsonNodeFromObject("sample-action"), "Failed to populate parameter action-name")
        assertEquals(inContext.get("request-id"), jsonNodeFromObject("12345"), "Failed to populate parameter action-name")
        assertEquals(inContext.get("template-content"), jsonNodeFromObject("This is Sample Velocity Template"), "Failed to populate parameter action-name")

    }

    @Test
    fun testResolveNodeTemplateInterfaceOperationOutputs() {
        logger.info("************************ testResolveNodeTemplateInterfaceOperationOutputs **********************")
        val bluePrintContext: BluePrintContext = BluePrintParserFactory.instance(BluePrintConstants.TYPE_DEFAULT)!!
                .readBlueprintFile("baseconfiguration/Definitions/activation-blueprint.json", basepath)
        assertNotNull(bluePrintContext, "Failed to populate Blueprint context")

        val context: MutableMap<String, Any> = hashMapOf()
        context[BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH] =  basepath.plus("/simple-baseconfig")

        val bluePrintRuntimeService = BluePrintRuntimeService(bluePrintContext, context)

        val componentContext: MutableMap<String, Any?> = hashMapOf()
        val successValue : JsonNode= jsonNodeFromObject("Success")
        componentContext["resource-assignment.DefaultComponentNode.process.status"] = successValue
        componentContext["resource-assignment.DefaultComponentNode.process.resource-assignment-params"] = null

        bluePrintRuntimeService.resolveNodeTemplateInterfaceOperationOutputs("resource-assignment",
                "DefaultComponentNode", "process", componentContext)

        assertEquals(NullNode.instance,
                context.get("node_templates/resource-assignment/interfaces/DefaultComponentNode/operations/process/properties/resource-assignment-params"),
                "Failed to get operation property resource-assignment-params")

        assertEquals(successValue,
                context.get("node_templates/resource-assignment/interfaces/DefaultComponentNode/operations/process/properties/status"),
                "Failed to get operation property status")


    }
}
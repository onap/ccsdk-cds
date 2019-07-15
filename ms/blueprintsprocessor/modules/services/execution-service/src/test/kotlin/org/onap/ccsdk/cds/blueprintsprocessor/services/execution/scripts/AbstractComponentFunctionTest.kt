/*-
 * ============LICENSE_START=======================================================
 * ONAP - CDS
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts;

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.componentScriptExecutor
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BluePrintScriptsServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit test cases for abstract component function.
 */
@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [ComponentFunctionScriptingService::class,
    BluePrintScriptsServiceImpl::class, PythonExecutorProperty::class,
    BlueprintJythonService::class])
class AbstractComponentFunctionTest {

    lateinit var blueprintContext: BluePrintContext

    @Autowired
    lateinit var compSvc: ComponentFunctionScriptingService

    @BeforeTest
    fun init() {
        blueprintContext = mockk<BluePrintContext>()
        every { blueprintContext.rootPath } returns normalizedPathName("target")
    }

    /**
     * Tests the abstract component functionality.
     */
    @Test
    fun testAbstractComponent() {
        runBlocking {
            val bluePrintRuntime = mockk<DefaultBluePrintRuntimeService>("1234")
            val samp = SampleComponent()
            val comp = samp as AbstractComponentFunction

            comp.bluePrintRuntimeService = bluePrintRuntime
            comp.stepName = "sample-step"
            assertNotNull(comp, "failed to get kotlin instance")

            val input = getMockedInput(bluePrintRuntime)

            val output = comp.applyNB(input)

            assertEquals(output.actionIdentifiers.actionName, "activate")
            assertEquals(output.commonHeader.requestId, "1234")
            assertEquals(output.stepData!!.name, "activate-restconf")
            assertEquals(output.status.message, "success")
        }
    }

    /**
     * Tests the abstract script component functionality.
     */
    @Test
    fun testAbstractScriptComponent() {
        runBlocking {
            val bluePrintRuntime = mockk<DefaultBluePrintRuntimeService>("1234")
            val samp = SampleRestconfComponent(compSvc)
            val comp = samp as AbstractComponentFunction

            comp.bluePrintRuntimeService = bluePrintRuntime
            comp.stepName = "sample-step"
            assertNotNull(comp, "failed to get kotlin instance")

            val input = getMockedInput(bluePrintRuntime)
            val inp = getMockedContext()

            val output = comp.applyNB(input)

            assertEquals(output.actionIdentifiers.actionName, "activate")
            assertEquals(output.commonHeader.requestId, "1234")
            assertEquals(output.stepData!!.name, "activate-restconf")
            assertEquals(output.status.message, "success")
        }
    }

    /**
     * Mocked input for abstract function test.
     */
    private fun getMockedContext() {
        val operationOutputs = hashMapOf<String, JsonNode>()
        every {
            blueprintContext.name()
        } returns "SampleTest"
        every {
            blueprintContext.version()
        } returns "SampleScriptComponent"
    }

    /**
     * Mocked input for abstract function test.
     */
    private fun getMockedInput(bluePrintRuntime: DefaultBluePrintRuntimeService):
            ExecutionServiceInput {

        val mapper = ObjectMapper()
        val rootNode = mapper.createObjectNode()
        rootNode.put("ip-address", "0.0.0.0")
        rootNode.put("type", "rest")

        val operationInputs = hashMapOf<String, JsonNode>()
        operationInputs[BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE] =
                "activate-restconf".asJsonPrimitive()
        operationInputs[BluePrintConstants.PROPERTY_CURRENT_INTERFACE] =
                "interfaceName".asJsonPrimitive()
        operationInputs[BluePrintConstants.PROPERTY_CURRENT_OPERATION] =
                "operationName".asJsonPrimitive()
        operationInputs["dynamic-properties"] = rootNode


        val stepInputData = StepData().apply {
            name = "activate-restconf"
            properties = operationInputs
        }
        val executionServiceInput = ExecutionServiceInput().apply {
            commonHeader = CommonHeader().apply {
                requestId = "1234"
            }
            actionIdentifiers = ActionIdentifiers().apply {
                actionName = "activate"
            }
            payload = JacksonUtils.jsonNode("{}") as ObjectNode
        }
        executionServiceInput.stepData = stepInputData

        every {
            bluePrintRuntime.resolveNodeTemplateInterfaceOperationInputs(
                    "activate-restconf", "interfaceName", "operationName")
        } returns operationInputs

        val operationOutputs = hashMapOf<String, JsonNode>()
        every {
            bluePrintRuntime.resolveNodeTemplateInterfaceOperationOutputs(
                    "activate-restconf", "interfaceName", "operationName")
        } returns operationOutputs
        every { bluePrintRuntime.bluePrintContext() } returns blueprintContext

        return executionServiceInput
    }

    @Test
    fun testComponentScriptExecutorNodeType() {
        val componentScriptExecutor = BluePrintTypes.componentScriptExecutor()
        assertNotNull(componentScriptExecutor.interfaces, "failed to get interface operations")
    }
}


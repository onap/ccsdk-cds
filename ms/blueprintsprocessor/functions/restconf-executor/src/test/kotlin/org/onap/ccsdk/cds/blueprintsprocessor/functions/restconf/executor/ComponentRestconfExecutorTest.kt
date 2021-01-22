/*
 *  Copyright © 2018 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentScriptExecutor
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.Implementation
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.context.ApplicationContext
import kotlin.test.assertNotNull

class ComponentRestconfExecutorTest {

    @Test
    fun `test Restconf Component Instance`() {
        runBlocking {

            val applicationContext = mockk<ApplicationContext>()
            every { applicationContext.getBean(any()) } returns mockk()
            val componentFunctionScriptingService = ComponentFunctionScriptingService(applicationContext, mockk())
            val componentScriptExecutor = ComponentScriptExecutor(componentFunctionScriptingService)

            assertNotNull(componentScriptExecutor, "failed to get ComponentRestconfExecutor instance")
            val executionServiceInput = ExecutionServiceInput().apply {
                commonHeader = CommonHeader().apply {
                    requestId = "1234"
                }
                actionIdentifiers = ActionIdentifiers().apply {
                    actionName = "activate"
                }
                payload = JacksonUtils.jsonNode("{}") as ObjectNode
            }

            val blueprintContext = mockk<BlueprintContext>()
            every {
                blueprintContext.nodeTemplateOperationImplementation(
                    any(), any(), any()
                )
            } returns Implementation()

            val bluePrintRuntime = mockk<DefaultBlueprintRuntimeService>("1234")
            every { bluePrintRuntime.bluePrintContext() } returns blueprintContext

            componentScriptExecutor.bluePrintRuntimeService = bluePrintRuntime
            componentScriptExecutor.stepName = "sample-step"

            val operationInputs = hashMapOf<String, JsonNode>()
            operationInputs[BlueprintConstants.PROPERTY_CURRENT_NODE_TEMPLATE] = "activate-restconf".asJsonPrimitive()
            operationInputs[BlueprintConstants.PROPERTY_CURRENT_INTERFACE] = "interfaceName".asJsonPrimitive()
            operationInputs[BlueprintConstants.PROPERTY_CURRENT_OPERATION] = "operationName".asJsonPrimitive()
            operationInputs[ComponentScriptExecutor.INPUT_SCRIPT_TYPE] =
                BlueprintConstants.SCRIPT_INTERNAL.asJsonPrimitive()
            operationInputs[ComponentScriptExecutor.INPUT_SCRIPT_CLASS_REFERENCE] =
                "internal.scripts.TestRestconfConfigure".asJsonPrimitive()

            val stepInputData = StepData().apply {
                name = "activate-restconf"
                properties = operationInputs
            }
            executionServiceInput.stepData = stepInputData

            every {
                bluePrintRuntime.resolveNodeTemplateInterfaceOperationInputs(
                    "activate-restconf",
                    "interfaceName", "operationName"
                )
            } returns operationInputs

            val operationOutputs = hashMapOf<String, JsonNode>()
            every {
                bluePrintRuntime.resolveNodeTemplateInterfaceOperationOutputs(
                    "activate-restconf",
                    "interfaceName", "operationName"
                )
            } returns operationOutputs

            componentScriptExecutor.applyNB(executionServiceInput)
        }
    }
}

/*
 * Copyright © 2020 Huawei Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.restful.executor

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
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.Implementation
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.context.ApplicationContext

class ComponentRestfulExecutorTest {

    @Test
    fun testComponentRestfulExecutor() {
        runBlocking {

            val applicationContext = mockk<ApplicationContext>()
            every { applicationContext.getBean(any()) } returns mockk()

            val componentFunctionScriptingService = ComponentFunctionScriptingService(applicationContext, mockk())

            val componentRestfulExecutor = ComponentRestfulExecutor(componentFunctionScriptingService)

            val executionServiceInput = ExecutionServiceInput().apply {
                commonHeader = CommonHeader().apply {
                    requestId = "1234"
                }
                actionIdentifiers = ActionIdentifiers().apply {
                    actionName = "config-deploy"
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

            componentRestfulExecutor.bluePrintRuntimeService = bluePrintRuntime
            componentRestfulExecutor.stepName = "sample-step"

            val operationInputs = hashMapOf<String, JsonNode>()
            operationInputs[BlueprintConstants.PROPERTY_CURRENT_NODE_TEMPLATE] = "config-deploy-process".asJsonPrimitive()
            operationInputs[BlueprintConstants.PROPERTY_CURRENT_INTERFACE] = "interfaceName".asJsonPrimitive()
            operationInputs[BlueprintConstants.PROPERTY_CURRENT_OPERATION] = "operationName".asJsonPrimitive()
            operationInputs["script-type"] = BlueprintConstants.SCRIPT_INTERNAL.asJsonPrimitive()
            operationInputs["script-class-reference"] = "internal.scripts.TestRestfulConfigure".asJsonPrimitive()

            val stepInputData = StepData().apply {
                name = "call-config-deploy-process"
                properties = operationInputs
            }
            executionServiceInput.stepData = stepInputData

            every {
                bluePrintRuntime.resolveNodeTemplateInterfaceOperationInputs(
                    "config-deploy-process",
                    "interfaceName", "operationName"
                )
            } returns operationInputs

            val operationOutputs = hashMapOf<String, JsonNode>()
            every {
                bluePrintRuntime.resolveNodeTemplateInterfaceOperationOutputs(
                    "config-deploy-process",
                    "interfaceName", "operationName"
                )
            } returns operationOutputs

            componentRestfulExecutor.applyNB(executionServiceInput)
        }
    }
}

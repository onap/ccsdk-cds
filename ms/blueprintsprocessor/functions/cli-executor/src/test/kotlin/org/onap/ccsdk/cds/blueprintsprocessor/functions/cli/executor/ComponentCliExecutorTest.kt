/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.cli.executor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentScriptExecutor
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ExecutionServiceConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.BlueprintSshLibConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.Implementation
import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BlueprintScriptsServiceImpl
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        CliExecutorConfiguration::class,
        ExecutionServiceConfiguration::class,
        BlueprintSshLibConfiguration::class, BlueprintScriptsServiceImpl::class,
        BlueprintPropertyConfiguration::class, BlueprintPropertiesService::class, BlueprintDependencyService::class
    ]
)
@DirtiesContext
@TestPropertySource(properties = [], locations = ["classpath:application-test.properties"])
class ComponentCliExecutorTest {

    @Autowired
    lateinit var componentScriptExecutor: ComponentScriptExecutor

    @Test
    fun `test CLI Component Instance`() {
        runBlocking {
            assertNotNull(componentScriptExecutor, "failed to get ComponentCliExecutor instance")
            val executionServiceInput = ExecutionServiceInput().apply {
                commonHeader = CommonHeader().apply {
                    requestId = "1234"
                }
                actionIdentifiers = ActionIdentifiers().apply {
                    actionName = "activate"
                }
                payload = JacksonUtils.jsonNode("{}") as ObjectNode
            }
            val bluePrintRuntime = mockk<DefaultBlueprintRuntimeService>("1234")
            componentScriptExecutor.bluePrintRuntimeService = bluePrintRuntime
            componentScriptExecutor.stepName = "sample-step"

            val operationInputs = hashMapOf<String, JsonNode>()
            operationInputs[BlueprintConstants.PROPERTY_CURRENT_NODE_TEMPLATE] = "activate-cli".asJsonPrimitive()
            operationInputs[BlueprintConstants.PROPERTY_CURRENT_INTERFACE] = "interfaceName".asJsonPrimitive()
            operationInputs[BlueprintConstants.PROPERTY_CURRENT_OPERATION] = "operationName".asJsonPrimitive()
            operationInputs[ComponentScriptExecutor.INPUT_SCRIPT_TYPE] = BlueprintConstants.SCRIPT_INTERNAL.asJsonPrimitive()
            operationInputs[ComponentScriptExecutor.INPUT_SCRIPT_CLASS_REFERENCE] =
                "internal.scripts.TestCliScriptFunction".asJsonPrimitive()

            val stepInputData = StepData().apply {
                name = "activate-cli"
                properties = operationInputs
            }
            executionServiceInput.stepData = stepInputData

            val blueprintContext = mockk<BlueprintContext>()
            every {
                blueprintContext.nodeTemplateOperationImplementation(
                    any(), any(), any()
                )
            } returns Implementation()

            every { bluePrintRuntime.bluePrintContext() } returns blueprintContext
            every {
                bluePrintRuntime.resolveNodeTemplateInterfaceOperationInputs(
                    "activate-cli",
                    "interfaceName", "operationName"
                )
            } returns operationInputs

            val operationOutputs = hashMapOf<String, JsonNode>()
            every {
                bluePrintRuntime.resolveNodeTemplateInterfaceOperationOutputs(
                    "activate-cli",
                    "interfaceName", "operationName"
                )
            } returns operationOutputs

            componentScriptExecutor.applyNB(executionServiceInput)
        }
    }
}

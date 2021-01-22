/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ACTION_MODE_SYNC
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.core.utils.createExecutionServiceOutputProto
import org.onap.ccsdk.cds.blueprintsprocessor.core.utils.createStatus
import org.onap.ccsdk.cds.blueprintsprocessor.core.utils.toProto
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.Implementation
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput
import kotlin.test.assertNotNull

class ComponentRemoteScriptExecutorTest {

    @Test
    fun testNodeComponentRemoteScriptExecutorType() {
        val nodeType = BlueprintTypes.nodeTypeComponentRemoteScriptExecutor()
        assertNotNull(nodeType, "failed to generate nodeType Component Remote Script Executor")
    }

    @Test
    fun testNodeTemplateComponentRemoteScriptExecutor() {

        val serviceTemplate = serviceTemplate("remote-script-dsl", "1.0.0", "xx@xx.com", "remote-script-ds") {
            topologyTemplate {
                nodeTemplateComponentRemoteScriptExecutor(
                    "remote-sample",
                    "This is sample node template"
                ) {
                    definedOperation(" Sample Operation") {
                        implementation(180, "SELF")
                        inputs {
                            selector("remote-script-executor")
                            blueprintName("sample")
                            blueprintVersion("1.0.0")
                            blueprintAction("sample-action")
                            timeout(120)
                            requestData("""{"key" :"value"}""")
                        }
                        outputs {
                            status("success")
                        }
                    }
                }
            }
            nodeTypeComponentRemoteScriptExecutor()
        }
        // println(serviceTemplate.asJsonString(true))
        assertNotNull(serviceTemplate, "failed to service template")
        assertNotNull(serviceTemplate.nodeTypes, "failed to service template node Types")
        assertNotNull(
            serviceTemplate.nodeTypes!!["component-remote-script-executor"],
            "failed to service template nodeType(component-remote-script-executor)"
        )
        assertNotNull(
            serviceTemplate.topologyTemplate?.nodeTemplates?.get("remote-sample"),
            "failed to nodeTemplate(remote-sample)"
        )
    }

    @Test
    fun testComponentRemoteScriptExecutor() {
        runBlocking {
            /** Mock blueprint context */
            val blueprintContext = mockk<BlueprintContext>()
            every { blueprintContext.rootPath } returns normalizedPathName("target")
            every {
                blueprintContext.nodeTemplateOperationImplementation(
                    "remote-execute", "ComponentRemoteScriptExecutor", "process"
                )
            } returns Implementation()

            val bluePrintRuntime = mockk<DefaultBlueprintRuntimeService>("1234")
            every { bluePrintRuntime.bluePrintContext() } returns blueprintContext

            val mockExecutionServiceInput = mockExecutionServiceInput(bluePrintRuntime)

            val mockStreamingRemoteExecutionService = mockk<StreamingRemoteExecutionService<
                    org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput,
                    ExecutionServiceOutput>>()

            coEvery {
                mockStreamingRemoteExecutionService.sendNonInteractive(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns mockExecutionServiceOutput(mockExecutionServiceInput)

            val componentRemoteScriptExecutor = ComponentRemoteScriptExecutor(mockStreamingRemoteExecutionService)
            componentRemoteScriptExecutor.bluePrintRuntimeService = bluePrintRuntime
            componentRemoteScriptExecutor.implementation = Implementation()
            val componentRemoteScriptExecutorOutput = componentRemoteScriptExecutor.applyNB(mockExecutionServiceInput)
            assertNotNull(componentRemoteScriptExecutorOutput, "failed to get executor output")
        }
    }

    private fun mockExecutionServiceInput(bluePrintRuntime: DefaultBlueprintRuntimeService): ExecutionServiceInput {

        val mapper = ObjectMapper()
        val requestNode = mapper.createObjectNode()
        requestNode.put("ip-address", "0.0.0.0")
        requestNode.put("type", "grpc")

        val operationInputs = hashMapOf<String, JsonNode>()
        operationInputs[BlueprintConstants.PROPERTY_CURRENT_NODE_TEMPLATE] = "remote-execute".asJsonPrimitive()
        operationInputs[BlueprintConstants.PROPERTY_CURRENT_INTERFACE] =
            "ComponentRemoteScriptExecutor".asJsonPrimitive()
        operationInputs[BlueprintConstants.PROPERTY_CURRENT_OPERATION] = "process".asJsonPrimitive()

        operationInputs[ComponentRemoteScriptExecutor.INPUT_SELECTOR] = "remote-script-executor".asJsonPrimitive()
        operationInputs[ComponentRemoteScriptExecutor.INPUT_BLUEPRINT_NAME] = "sample-blueprint".asJsonPrimitive()
        operationInputs[ComponentRemoteScriptExecutor.INPUT_BLUEPRINT_VERSION] = "1.0.0".asJsonPrimitive()
        operationInputs[ComponentRemoteScriptExecutor.INPUT_BLUEPRINT_ACTION] = "remote-activate".asJsonPrimitive()
        operationInputs[ComponentRemoteScriptExecutor.INPUT_TIMEOUT] = 120.asJsonPrimitive()
        operationInputs[ComponentRemoteScriptExecutor.INPUT_REQUEST_DATA] = requestNode

        val stepInputData = StepData().apply {
            name = "remote-execute"
            properties = operationInputs
        }
        val executionServiceInput = ExecutionServiceInput().apply {
            commonHeader = CommonHeader().apply {
                requestId = "1234"
                subRequestId = "1234-123"
                originatorId = "test-client"
            }
            actionIdentifiers = ActionIdentifiers().apply {
                blueprintName = "sample-blueprint"
                blueprintVersion = "1.0.0"
                actionName = "remote-activate"
                mode = ACTION_MODE_SYNC
            }
            payload = """{}""".jsonAsJsonType() as ObjectNode
        }
        executionServiceInput.stepData = stepInputData

        every {
            bluePrintRuntime.resolveNodeTemplateInterfaceOperationInputs(
                "remote-execute", "ComponentRemoteScriptExecutor", "process"
            )
        } returns operationInputs

        /** Mock Set Attributes */
        every {
            bluePrintRuntime.setNodeTemplateAttributeValue(
                "remote-execute", any(), any()
            )
        } returns Unit

        val operationOutputs = hashMapOf<String, JsonNode>()
        every {
            bluePrintRuntime.resolveNodeTemplateInterfaceOperationOutputs(
                "remote-execute", "ComponentRemoteScriptExecutor", "process"
            )
        } returns operationOutputs

        return executionServiceInput
    }

    private fun mockExecutionServiceOutput(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        val actionName = executionServiceInput.actionIdentifiers.actionName
        val responsePayload = """
            {
            "$actionName-response" :{
            "key" : "value"
            }
            }
        """.trimIndent()
        return createExecutionServiceOutputProto(
            executionServiceInput.commonHeader.toProto(),
            executionServiceInput.actionIdentifiers.toProto(),
            createStatus(BlueprintConstants.STATUS_SUCCESS, 200),
            responsePayload
        )
    }
}

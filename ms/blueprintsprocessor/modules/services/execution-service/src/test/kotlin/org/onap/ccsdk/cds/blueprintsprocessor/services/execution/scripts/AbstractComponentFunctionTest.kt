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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.CDS_LOCK_GROUP
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterLock
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.nodeTypeComponentScriptExecutor
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.Implementation
import org.onap.ccsdk.cds.controllerblueprints.core.data.LockAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BluePrintScriptsServiceImpl
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import java.lang.RuntimeException
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit test cases for abstract component function.
 */
@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        ComponentFunctionScriptingService::class,
        BluePrintScriptsServiceImpl::class, DeprecatedBlueprintJythonService::class
    ]
)
class AbstractComponentFunctionTest {

    lateinit var bluePrintRuntimeService: DefaultBluePrintRuntimeService
    lateinit var blueprintContext: BluePrintContext
    lateinit var blueprintClusterService: BluePrintClusterService

    @Autowired
    lateinit var compSvc: ComponentFunctionScriptingService

    @BeforeTest
    fun init() {
        bluePrintRuntimeService = mockk()
        blueprintContext = mockk()
        blueprintClusterService = mockk()
        every { bluePrintRuntimeService.bluePrintContext() } returns blueprintContext

        every { blueprintContext.rootPath } returns normalizedPathName("target")
        every {
            blueprintContext.nodeTemplateOperationImplementation(
                any(), any(), any()
            )
        } returns Implementation()
    }

    @Test
    fun testAbstractComponent() {
        runBlocking {
            val samp = SampleComponent()
            val comp = samp as AbstractComponentFunction

            comp.bluePrintRuntimeService = bluePrintRuntimeService
            comp.stepName = "sample-step"
            assertNotNull(comp, "failed to get kotlin instance")

            val input = getMockedInput(bluePrintRuntimeService)

            val output = comp.applyNB(input)

            assertEquals(output.actionIdentifiers.actionName, "activate")
            assertEquals(output.commonHeader.requestId, "1234")
            assertEquals(output.stepData!!.name, "activate-restconf")
            assertEquals(output.status.message, "success")
        }
    }

    @Test
    fun testComponentFunctionPayload() {
        val sampleComponent = SampleComponent()
        sampleComponent.workflowName = "sample-action"
        sampleComponent.executionServiceInput = JacksonUtils.readValueFromClassPathFile(
            "payload/requests/sample-execution-request.json", ExecutionServiceInput::class.java
        )!!
        val payload = sampleComponent.requestPayload()
        assertNotNull(payload, "failed to get payload")
        val data = sampleComponent.requestPayloadActionProperty("data")?.first()
        assertNotNull(data, "failed to get payload request action data")
    }

    @Test
    fun testAbstractScriptComponent() {
        runBlocking {
            val samp = SampleRestconfComponent(compSvc)
            val comp = samp as AbstractComponentFunction

            comp.bluePrintRuntimeService = bluePrintRuntimeService
            comp.stepName = "sample-step"
            assertNotNull(comp, "failed to get kotlin instance")

            val input = getMockedInput(bluePrintRuntimeService)

            val output = comp.applyNB(input)

            assertEquals(output.actionIdentifiers.actionName, "activate")
            assertEquals(output.commonHeader.requestId, "1234")
            assertEquals(output.stepData!!.name, "activate-restconf")
            assertEquals(output.status.message, "success")
        }
    }

    @Test
    fun testComponentScriptExecutorNodeType() {
        val componentScriptExecutor = BluePrintTypes.nodeTypeComponentScriptExecutor()
        assertNotNull(componentScriptExecutor.interfaces, "failed to get interface operations")
    }

    @Test
    fun `prepareRequestNB should resolve lock properties`() {
        val implementation = Implementation().apply {
            this.lock = LockAssignment().apply {
                this.key = """ {"get_input": "lock-key"} """.asJsonPrimitive()
            }
        }
        every {
            blueprintContext.nodeTemplateOperationImplementation(any(), any(), any())
        } returns implementation

        every {
            bluePrintRuntimeService.resolvePropertyAssignments(any(), any(), any())
        } returns mutableMapOf(
            "key" to "abc-123-def-456".asJsonType(),
            "acquireTimeout" to implementation.lock!!.acquireTimeout
        )

        val component: AbstractComponentFunction = SampleComponent()
        component.bluePrintRuntimeService = bluePrintRuntimeService
        component.bluePrintClusterService = blueprintClusterService

        runBlocking {
            component.prepareRequestNB(getMockedInput(bluePrintRuntimeService))
        }

        val resolvedLock = component.implementation.lock!!

        assertEquals("abc-123-def-456", resolvedLock.key.textValue())
        // default value
        assertEquals(180, resolvedLock.acquireTimeout.intValue())
    }

    @Test(expected = Exception::class)
    fun `prepareRequestNB should throw exception if it fails to resolve lock key`() {
        every {
            blueprintContext.nodeTemplateOperationImplementation(any(), any(), any())
        } returns Implementation().apply { this.lock = LockAssignment() }

        every {
            bluePrintRuntimeService.resolvePropertyAssignments(any(), any(), any())
        } returns mutableMapOf(
            "key" to "".asJsonType(),
            "acquireTimeout" to Integer(360).asJsonType()
        )

        val component: AbstractComponentFunction = SampleComponent()
        component.bluePrintRuntimeService = bluePrintRuntimeService
        component.bluePrintClusterService = blueprintClusterService

        runBlocking {
            component.prepareRequestNB(getMockedInput(bluePrintRuntimeService))
        }
    }

    @Test
    fun `applyNB should catch exceptions and call recoverNB`() {
        val exception = RuntimeException("Intentional test exception")
        every {
            bluePrintRuntimeService.resolvePropertyAssignments(any(), any(), any())
        } throws exception
        every {
            blueprintContext.nodeTemplateOperationImplementation(any(), any(), any())
        } returns Implementation().apply {
            this.lock = LockAssignment().apply { this.key = "testing-lock".asJsonType() }
        }

        val component: AbstractComponentFunction = spyk(SampleComponent())
        component.bluePrintRuntimeService = bluePrintRuntimeService
        component.bluePrintClusterService = blueprintClusterService
        val input = getMockedInput(bluePrintRuntimeService)

        runBlocking { component.applyNB(input) }
        verify { runBlocking { component.recoverNB(exception, input) } }
    }

    @Test
    fun `applyNB - when lock is present use ClusterLock`() {

        val lockName = "testing-lock"

        every {
            blueprintContext.nodeTemplateOperationImplementation(any(), any(), any())
        } returns Implementation().apply {
            this.lock = LockAssignment().apply { this.key = lockName.asJsonType() }
        }

        every {
            bluePrintRuntimeService.resolvePropertyAssignments(any(), any(), any())
        } returns mutableMapOf(
            "key" to lockName.asJsonType(),
            "acquireTimeout" to Integer(180).asJsonType()
        )

        val clusterLock: ClusterLock = mockk()

        every { clusterLock.name() } returns lockName
        every { runBlocking { clusterLock.tryLock(any()) } } returns true
        every { runBlocking { clusterLock.unLock() } } returns Unit

        every {
            runBlocking { blueprintClusterService.clusterLock(any()) }
        } returns clusterLock

        val component: AbstractComponentFunction = SampleComponent()
        component.bluePrintRuntimeService = bluePrintRuntimeService
        component.bluePrintClusterService = blueprintClusterService

        runBlocking {
            component.applyNB(getMockedInput(bluePrintRuntimeService))
        }

        verify {
            runBlocking { blueprintClusterService.clusterLock("$lockName@$CDS_LOCK_GROUP") }
        }
        verify { runBlocking { clusterLock.unLock() } }
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
                    "activate-restconf", "interfaceName", "operationName"
                )
            } returns operationInputs

            val operationOutputs = hashMapOf<String, JsonNode>()
            every {
                bluePrintRuntime.resolveNodeTemplateInterfaceOperationOutputs(
                    "activate-restconf", "interfaceName", "operationName"
                )
            } returns operationOutputs
            every { bluePrintRuntime.bluePrintContext() } returns blueprintContext

            return executionServiceInput
        }
}

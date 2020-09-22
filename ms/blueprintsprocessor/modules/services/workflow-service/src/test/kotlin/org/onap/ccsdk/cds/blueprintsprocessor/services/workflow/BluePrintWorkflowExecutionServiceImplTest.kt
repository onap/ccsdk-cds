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

package org.onap.ccsdk.cds.blueprintsprocessor.services.workflow

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.services.workflow.mock.MockComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintError
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.Step
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintWorkflowExecutionService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import java.lang.RuntimeException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [WorkflowServiceConfiguration::class])
class BluePrintWorkflowExecutionServiceImplTest {

    @Autowired
    lateinit var bluePrintWorkflowExecutionService: BluePrintWorkflowExecutionService<ExecutionServiceInput, ExecutionServiceOutput>

    @MockBean
    lateinit var bluePrintClusterService: BluePrintClusterService

    @Before
    fun init() {
        mockkObject(BluePrintDependencyService)
        every { BluePrintDependencyService.applicationContext.getBean(any()) } returns MockComponentFunction()
    }

    @After
    fun afterTests() {
        unmockkAll()
    }

    @Test
    fun testBluePrintWorkflowExecutionService() {
        runBlocking {
            val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(
                "1234",
                "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
            )

            val executionServiceInput = JacksonUtils.readValueFromClassPathFile(
                "execution-input/resource-assignment-input.json",
                ExecutionServiceInput::class.java
            )!!

            val executionServiceOutput = bluePrintWorkflowExecutionService
                .executeBluePrintWorkflow(bluePrintRuntimeService, executionServiceInput, hashMapOf())

            assertNotNull(executionServiceOutput, "failed to get response")
            assertEquals(
                BluePrintConstants.STATUS_SUCCESS, executionServiceOutput.status.message,
                "failed to get successful response"
            )
        }
    }

    @Test
    fun testImperativeBluePrintWorkflowExecutionService() {
        runBlocking {
            val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(
                "1234",
                "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
            )

            val executionServiceInput = JacksonUtils.readValueFromClassPathFile(
                "execution-input/imperative-test-input.json",
                ExecutionServiceInput::class.java
            )!!

            val executionServiceOutput = bluePrintWorkflowExecutionService
                .executeBluePrintWorkflow(bluePrintRuntimeService, executionServiceInput, hashMapOf())

            assertNotNull(executionServiceOutput, "failed to get response")
            assertEquals(
                BluePrintConstants.STATUS_SUCCESS, executionServiceOutput.status.message,
                "failed to get successful response"
            )
        }
    }

    @Test
    fun `Blueprint fails on missing workflowName-parameters with a useful message`() {
        assertFailsWith(exceptionClass = BluePrintProcessorException::class) {
            runBlocking {
                val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(
                    "1234",
                    "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
                )
                // service input will have a mislabeled input params, we are expecting to get an error when that happens with a useful error message
                val executionServiceInput =
                    JacksonUtils.readValueFromClassPathFile(
                        "execution-input/resource-assignment-input-missing-resource_assignment_request.json",
                        ExecutionServiceInput::class.java
                    )!!

                val executionServiceOutput = bluePrintWorkflowExecutionService
                    .executeBluePrintWorkflow(bluePrintRuntimeService, executionServiceInput, hashMapOf())
            }
        }
    }

    @Test
    fun `Should handle errors from resolve workflow output`() {
        val imperativeWorkflowExecutionService: ImperativeWorkflowExecutionService = mockk()
        val bluePrintWorkflowExecutionServiceImpl = BluePrintWorkflowExecutionServiceImpl(
            mockk(), mockk(), imperativeWorkflowExecutionService
        )
        val bluePrintRuntimeService: BluePrintRuntimeService<MutableMap<String, JsonNode>> = mockk()
        val bluePrintContext: BluePrintContext = mockk()
        val executionServiceInput = ExecutionServiceInput().apply {
            this.actionIdentifiers = ActionIdentifiers().apply { this.actionName = "config-assign" }
            this.commonHeader = CommonHeader()
            this.payload = """{"config-assign-request": {}}""".asJsonType() as ObjectNode
        }
        val workflow = Workflow().apply {
            this.steps = mutableMapOf("one" to Step(), "two" to Step())
        }
        val blueprintError = BluePrintError()

        every { bluePrintRuntimeService.bluePrintContext() } returns bluePrintContext
        every { bluePrintRuntimeService.assignWorkflowInputs(any(), any()) } returns Unit
        every { bluePrintContext.workflowByName(any()) } returns workflow
        every {
            bluePrintRuntimeService.resolveWorkflowOutputs(any())
        } throws RuntimeException("failed to resolve property...")
        every {
            runBlocking {
                imperativeWorkflowExecutionService.executeBluePrintWorkflow(any(), any(), any())
            }
        } returns ExecutionServiceOutput()
        every { bluePrintRuntimeService.getBluePrintError() } returns blueprintError

        runBlocking {
            val output = bluePrintWorkflowExecutionServiceImpl.executeBluePrintWorkflow(
                bluePrintRuntimeService, executionServiceInput, mutableMapOf()
            )
            assertEquals("failed to resolve property...", blueprintError.errors[0])
            assertEquals("""{"config-assign-response":{}}""".asJsonType(), output.payload)
        }
    }
}

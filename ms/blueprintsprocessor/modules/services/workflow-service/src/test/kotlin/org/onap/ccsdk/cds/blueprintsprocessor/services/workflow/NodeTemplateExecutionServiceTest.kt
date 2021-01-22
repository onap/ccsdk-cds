/*
 * Copyright © 2019 IBM, Bell Canada.
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
package org.onap.ccsdk.cds.blueprintsprocessor.services.workflow

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BlueprintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.services.workflow.mock.MockComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [WorkflowServiceConfiguration::class])

class NodeTemplateExecutionServiceTest {

    @MockBean
    lateinit var bluePrintClusterService: BlueprintClusterService

    @Before
    fun init() {
        mockkObject(BlueprintDependencyService)
        every { BlueprintDependencyService.applicationContext.getBean(any()) } returns MockComponentFunction()
    }

    @After
    fun afterTests() {
        unmockkAll()
    }

    @Test
    fun testExecuteNodeTemplate() {
        runBlocking {
            val bluePrintRuntimeService = BlueprintMetadataUtils.getBlueprintRuntime(
                "1234",
                "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
            )

            val executionServiceInput = JacksonUtils.readValueFromClassPathFile(
                "execution-input/resource-assignment-input.json",
                ExecutionServiceInput::class.java
            )!!

            // Assign Workflow inputs Mock
            val input = executionServiceInput.payload.get("resource-assignment-request")
            bluePrintRuntimeService.assignWorkflowInputs("resource-assignment", input)

            val nodeTemplate = "resource-assignment"
            val nodeTemplateExecutionService = NodeTemplateExecutionService(bluePrintClusterService)
            val executionServiceOutput = nodeTemplateExecutionService
                .executeNodeTemplate(bluePrintRuntimeService, nodeTemplate, executionServiceInput)

            assertNotNull(executionServiceOutput, "failed to get response")
            assertEquals(
                BlueprintConstants.STATUS_SUCCESS, executionServiceOutput.status.message,
                "failed to get successful response"
            )
        }
    }
}

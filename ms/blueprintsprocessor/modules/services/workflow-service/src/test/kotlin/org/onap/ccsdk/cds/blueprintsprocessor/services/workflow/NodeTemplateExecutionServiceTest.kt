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

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [WorkflowServiceConfiguration::class])

class NodeTemplateExecutionServiceTest {
    @Autowired
    lateinit var nodeTemplateExecutionService: NodeTemplateExecutionService

    @Test
    fun testExecuteNodeTemplate() {
        runBlocking {
            val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime("1234",
                    "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")

            val executionServiceInput = JacksonUtils.readValueFromClassPathFile("execution-input/resource-assignment-input.json",
                    ExecutionServiceInput::class.java)!!

            // Assign Workflow inputs Mock
            val input = executionServiceInput.payload.get("resource-assignment-request")
            bluePrintRuntimeService.assignWorkflowInputs("resource-assignment", input)

            val nodeTemplate = "resource-assignment"

            val executionServiceOutput = nodeTemplateExecutionService
                    .executeNodeTemplate(bluePrintRuntimeService, nodeTemplate, executionServiceInput)

            assertNotNull(executionServiceOutput, "failed to get response")
            assertEquals(BluePrintConstants.STATUS_SUCCESS, executionServiceOutput.status.message,
                    "failed to get successful response")
        }
    }
}
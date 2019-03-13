/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.apps.blueprintsprocessor.services.workflow

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.services.workflow.executor.ComponentExecuteNodeExecutor
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [WorkflowServiceConfiguration::class, ComponentExecuteNodeExecutor::class])
class DGWorkflowExecutionServiceTest {

    private val log = LoggerFactory.getLogger(BlueprintServiceLogicTest::class.java)

    @Autowired
    lateinit var dgWorkflowExecutionService: DGWorkflowExecutionService


    @Test
    fun testExecuteDirectedGraph() {

        val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime("1234",
                "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")

        val executionServiceInput = JacksonUtils.readValueFromClassPathFile("execution-input/resource-assignment-input.json", ExecutionServiceInput::class.java)!!

        runBlocking {
            dgWorkflowExecutionService.executeBluePrintWorkflow(bluePrintRuntimeService, executionServiceInput, mutableMapOf())
        }

    }


}
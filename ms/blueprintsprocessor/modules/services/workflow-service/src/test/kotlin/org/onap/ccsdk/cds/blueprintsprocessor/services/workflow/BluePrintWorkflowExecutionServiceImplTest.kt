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

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintWorkflowExecutionService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [WorkflowServiceConfiguration::class])
class BluePrintWorkflowExecutionServiceImplTest {

    @Autowired
    lateinit var bluePrintWorkflowExecutionService: BluePrintWorkflowExecutionService<ExecutionServiceInput, ExecutionServiceOutput>

    @Test
    fun testBluePrintWorkflowExecutionService() {

        val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime("1234",
                "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")

        val executionServiceInput = JacksonUtils.readValueFromClassPathFile("execution-input/resource-assignment-input.json",
                ExecutionServiceInput::class.java)!!

        runBlocking {
            bluePrintWorkflowExecutionService.executeBluePrintWorkflow(bluePrintRuntimeService, executionServiceInput,
                    hashMapOf())
        }
    }

}

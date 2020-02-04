/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Huawei.
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
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restful.executor.mock.MockInstanceConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor.scripts.PythonExecutorConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor.scripts.PythonExecutorProperty
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.putJsonElement
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [PythonExecutorConfiguration::class, PythonExecutorProperty::class,
    ComponentRestfulExecutor::class, MockInstanceConfiguration::class])
@TestPropertySource(properties =
["blueprints.processor.functions.restful.executor.modulePaths=./../../../../components/scripts/python/ccsdk_blueprints",
    "blueprints.processor.functions.restful.executor.executionPath=./../../../../components/scripts/python/ccsdk_blueprints"])
class ComponentRestfulExecutorTest {

    @Autowired
    lateinit var componentRestfulExecutor: ComponentRestfulExecutor

    @Test
    fun testComponentRestfulExecutor() {
        runBlocking {

            val executionServiceInput = JacksonUtils.readValueFromClassPathFile("payload/requests/sample-activate-request.json",
                    ExecutionServiceInput::class.java)!!

            val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime("1234",
                    "./../../../../components/model-catalog/blueprint-model/test-blueprint/activateRestful")

            val stepMetaData: MutableMap<String, JsonNode> = hashMapOf()
            stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE, "activate-restful")
            stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_INTERFACE, "ComponentRestfulExecutor")
            stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_OPERATION, "process")
            componentRestfulExecutor.bluePrintRuntimeService = bluePrintRuntimeService
            val stepInputData = StepData().apply {
                name = "activate-restful"
                properties = stepMetaData
            }
            executionServiceInput.stepData = stepInputData
            componentRestfulExecutor.applyNB(executionServiceInput)
        }
    }
}

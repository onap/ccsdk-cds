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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor.mock.MockInstanceConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor.scripts.PythonExecutorConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor.scripts.PythonExecutorProperty
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.putJsonElement
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        PythonExecutorConfiguration::class, PythonExecutorProperty::class,
        ComponentJythonExecutor::class, MockInstanceConfiguration::class
    ]
)
@TestPropertySource(
    properties =
        [
            "blueprints.processor.functions.python.executor.modulePaths=./../../../../components/scripts/python/ccsdk_blueprints",
            "blueprints.processor.functions.python.executor.executionPath=./../../../../components/scripts/python/ccsdk_blueprints"
        ]
)
class ComponentJythonExecutorTest {

    @Autowired
    lateinit var componentJythonExecutor: ComponentJythonExecutor

    @Test
    fun testPythonComponentInjection() {
        runBlocking {

            val executionServiceInput = JacksonUtils.readValueFromClassPathFile(
                "payload/requests/sample-activate-request.json",
                ExecutionServiceInput::class.java
            )!!

            val bluePrintRuntimeService = BlueprintMetadataUtils.getBlueprintRuntime(
                "1234",
                "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
            )

            val stepMetaData: MutableMap<String, JsonNode> = hashMapOf()
            stepMetaData.putJsonElement(BlueprintConstants.PROPERTY_CURRENT_NODE_TEMPLATE, "activate-jython")
            stepMetaData.putJsonElement(BlueprintConstants.PROPERTY_CURRENT_INTERFACE, "ComponentJythonExecutor")
            stepMetaData.putJsonElement(BlueprintConstants.PROPERTY_CURRENT_OPERATION, "process")
            componentJythonExecutor.bluePrintRuntimeService = bluePrintRuntimeService
            val stepInputData = StepData().apply {
                name = "activate-jython"
                properties = stepMetaData
            }
            executionServiceInput.stepData = stepInputData
            componentJythonExecutor.applyNB(executionServiceInput)
        }
    }
}

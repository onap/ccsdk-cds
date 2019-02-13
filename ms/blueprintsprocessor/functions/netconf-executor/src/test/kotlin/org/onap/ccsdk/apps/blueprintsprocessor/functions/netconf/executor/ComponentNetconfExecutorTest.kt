/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Modifications Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor

import com.fasterxml.jackson.databind.JsonNode
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.BlueprintJythonService
import org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.PythonExecutorProperty
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.apps.controllerblueprints.core.putJsonElement
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [BlueprintJythonService::class, PythonExecutorProperty::class, ComponentNetconfExecutor::class, JsonParserService::class])
@TestPropertySource(properties =
["blueprints.processor.functions.python.executor.modulePaths=./../../../../components/scripts/python/ccsdk_netconf,./../../../../components/scripts/python/ccsdk_blueprints",
    "blueprints.processor.functions.python.executor.executionPath=./../../../../components/scripts/python/ccsdk_netconf"])
class ComponentNetconfExecutorTest {

    @Autowired
    lateinit var componentNetconfExecutor: ComponentNetconfExecutor


    @Test
    fun testComponentNetconfExecutor() {

        val executionServiceInput = JacksonUtils.readValueFromClassPathFile("requests/sample-activate-request.json",
            ExecutionServiceInput::class.java)!!

        val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime("1234",
            "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")

        val executionContext = bluePrintRuntimeService.getExecutionContext()


        componentNetconfExecutor.bluePrintRuntimeService = bluePrintRuntimeService


        val stepMetaData: MutableMap<String, JsonNode> = hashMapOf()
        stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE, "activate-netconf")
        stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_INTERFACE, "ComponentNetconfExecutor")
        stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_OPERATION, "process")
        // Set Step Inputs in Blueprint Runtime Service
        bluePrintRuntimeService.put("activate-netconf-step-inputs", stepMetaData.asJsonNode())

        componentNetconfExecutor.bluePrintRuntimeService = bluePrintRuntimeService
        componentNetconfExecutor.stepName = "activate-netconf"
        componentNetconfExecutor.apply(executionServiceInput)

    }
}


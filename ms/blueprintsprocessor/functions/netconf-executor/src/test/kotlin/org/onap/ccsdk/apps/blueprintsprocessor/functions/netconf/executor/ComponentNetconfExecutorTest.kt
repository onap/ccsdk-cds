/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.PythonExecutorProperty
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.putJsonElement
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [NetconfExecutorConfiguration::class, PythonExecutorProperty::class])
@TestPropertySource(properties =
["blueprints.processor.functions.python.executor.modulePaths=./../../../../components/scripts/python/ccsdk_blueprints",
    "blueprints.processor.functions.python.executor.executionPath=./../../../../components/scripts/python/ccsdk_blueprints"])
class ComponentNetconfExecutorTest {

    @Autowired
    lateinit var componentNetconfExecutor: ComponentNetconfExecutor

    @Test
    fun testComponentNetconfExecutor() {

        val executionServiceInput = ExecutionServiceInput()
        val commonHeader = CommonHeader()
        commonHeader.requestId = "1234"
        executionServiceInput.commonHeader = commonHeader

        val actionIdentifiers = ActionIdentifiers()
        actionIdentifiers.blueprintName = "baseconfiguration"
        actionIdentifiers.blueprintVersion = "1.0.0"
        actionIdentifiers.actionName = "activate"
        executionServiceInput.actionIdentifiers = actionIdentifiers


        val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(commonHeader.requestId,
                "./../../../../components/model-catalog/blueprint-model/starter-blueprint/baseconfiguration")

        componentNetconfExecutor.bluePrintRuntimeService = bluePrintRuntimeService


        val metaData: MutableMap<String, JsonNode> = hashMapOf()
        metaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_STEP, "activate-jython")

        val stepMetaData: MutableMap<String, JsonNode> = hashMapOf()
        stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE, "activate-jython")
        stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_INTERFACE, "JythonExecutorComponent")
        stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_OPERATION, "process")
        metaData.putJsonElement("activate-jython-step-inputs", stepMetaData)
        executionServiceInput.metadata = metaData

        componentNetconfExecutor.apply(executionServiceInput)

    }
}


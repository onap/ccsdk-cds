/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Modifications Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor

import com.fasterxml.jackson.databind.JsonNode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts.BlueprintJythonService
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.putJsonElement
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.context.ApplicationContext

class ComponentNetconfExecutorTest {

    @Test
    fun testComponentNetconfExecutor() {

        runBlocking {

            val applicationContext = mockk<ApplicationContext>()
            every { applicationContext.getBean(any()) } returns mockk()

            val blueprintJythonService = mockk<BlueprintJythonService>()
            val mockAbstractScriptComponentFunction = spyk<AbstractScriptComponentFunction>()
            coEvery { mockAbstractScriptComponentFunction.executeScript(any()) } returns mockk()

            coEvery { blueprintJythonService.jythonComponentInstance(any(), any()) } returns mockAbstractScriptComponentFunction

            val componentFunctionScriptingService = ComponentFunctionScriptingService(
                applicationContext,
                blueprintJythonService
            )

            val componentNetconfExecutor = ComponentNetconfExecutor(componentFunctionScriptingService)

            val executionServiceInput = JacksonUtils.readValueFromClassPathFile(
                "requests/sample-activate-request.json",
                ExecutionServiceInput::class.java
            )!!

            val bluePrintRuntimeService = BlueprintMetadataUtils.getBlueprintRuntime(
                "1234",
                "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
            )

            val assignmentParams = """{
                "ipAddress" : "127.0.0.1",
                "hostName" : "vnf-host"
                }                
            """.trimIndent()

            val json = """{
                "hostname" : "127.0.0.1"
                }                
            """.trimIndent()

            bluePrintRuntimeService.assignInputs(json.asJsonType())
            bluePrintRuntimeService.setNodeTemplateAttributeValue(
                "resource-assignment", "assignment-params",
                JacksonUtils.jsonNode(assignmentParams)
            )

            componentNetconfExecutor.bluePrintRuntimeService = bluePrintRuntimeService

            // TODO("Set Attribute properties")
            val stepMetaData: MutableMap<String, JsonNode> = hashMapOf()
            stepMetaData.putJsonElement(BlueprintConstants.PROPERTY_CURRENT_NODE_TEMPLATE, "activate-netconf")
            stepMetaData.putJsonElement(BlueprintConstants.PROPERTY_CURRENT_INTERFACE, "ComponentNetconfExecutor")
            stepMetaData.putJsonElement(BlueprintConstants.PROPERTY_CURRENT_OPERATION, "process")
            // Set Step Inputs in Blueprint Runtime Service
            componentNetconfExecutor.bluePrintRuntimeService = bluePrintRuntimeService
            val stepInputData = StepData().apply {
                name = "activate-netconf"
                properties = stepMetaData
            }
            executionServiceInput.stepData = stepInputData
            componentNetconfExecutor.applyNB(executionServiceInput)
        }
    }
}

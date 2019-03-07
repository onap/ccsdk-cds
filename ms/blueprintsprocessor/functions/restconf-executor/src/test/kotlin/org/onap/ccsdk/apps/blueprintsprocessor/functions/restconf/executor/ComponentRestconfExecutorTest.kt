/*
 *  Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.restconf.executor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.apps.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.core.service.DefaultBluePrintRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@ComponentScan(basePackages = ["org.onap.ccsdk.apps.blueprintsprocessor", "org.onap.ccsdk.apps.controllerblueprints"])
@DirtiesContext
@TestPropertySource(properties =
["server.port=9111",
    "blueprintsprocessor.restconfEnabled=true",
    "blueprintsprocessor.restclient.odlPrimary.type=basic-auth",
    "blueprintsprocessor.restclient.odlPrimary.url=http://127.0.0.1:9111",
    "blueprintsprocessor.restclient.odlPrimary.userId=sampleuser",
    "blueprintsprocessor.restclient.odlPrimary.token=sampletoken"],
    locations = ["classpath:application-test.properties"])
class ComponentRestconfExecutorTest {

    @Autowired
    lateinit var componentRestconfExecutor: ComponentRestconfExecutor

    @Test
    fun `test Restconf Component Instance`() {
        assertNotNull(componentRestconfExecutor, "failed to get ComponentRestconfExecutor instance")
        val executionServiceInput = ExecutionServiceInput().apply {
            commonHeader = CommonHeader().apply {
                requestId = "1234"
            }
            actionIdentifiers = ActionIdentifiers().apply {
                actionName = "activate"
            }
            payload = JacksonUtils.jsonNode("{}") as ObjectNode
        }
        val bluePrintRuntime = mockk<DefaultBluePrintRuntimeService>("1234")
        componentRestconfExecutor.bluePrintRuntimeService = bluePrintRuntime
        componentRestconfExecutor.stepName = "sample-step"

        val operationInputs = hashMapOf<String, JsonNode>()
        operationInputs[BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE] = "activate-restconf".asJsonPrimitive()
        operationInputs[BluePrintConstants.PROPERTY_CURRENT_INTERFACE] = "interfaceName".asJsonPrimitive()
        operationInputs[BluePrintConstants.PROPERTY_CURRENT_OPERATION] = "operationName".asJsonPrimitive()
        operationInputs[ComponentRestconfExecutor.SCRIPT_TYPE] = BluePrintConstants.SCRIPT_INTERNAL.asJsonPrimitive()
        operationInputs[ComponentRestconfExecutor.SCRIPT_CLASS_REFERENCE] =
            "InternalSimpleRestconf_cba\$TestRestconfConfigure".asJsonPrimitive()
        operationInputs[ComponentRestconfExecutor.INSTANCE_DEPENDENCIES] = JacksonUtils.jsonNode("[]") as ArrayNode

        val blueprintContext = mockk<BluePrintContext>()
        every { bluePrintRuntime.bluePrintContext() } returns blueprintContext
        every { bluePrintRuntime.get("sample-step-step-inputs") } returns operationInputs.asJsonNode()
        every {
            bluePrintRuntime.resolveNodeTemplateInterfaceOperationInputs("activate-restconf",
                "interfaceName", "operationName")
        } returns operationInputs

        val operationOutputs = hashMapOf<String, JsonNode>()
        every {
            bluePrintRuntime.resolveNodeTemplateInterfaceOperationOutputs("activate-restconf",
                "interfaceName", "operationName")
        } returns operationOutputs
        every { bluePrintRuntime.put("sample-step-step-outputs", any()) } returns Unit

        componentRestconfExecutor.apply(executionServiceInput)
    }
}
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

package org.onap.ccsdk.apps.blueprintsprocessor.services.workflow

import com.fasterxml.jackson.databind.JsonNode
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.services.workflow.executor.ComponentExecuteNodeExecutor
import org.onap.ccsdk.apps.blueprintsprocessor.services.workflow.utils.SvcGraphUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.putJsonElement
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [WorkflowServiceConfiguration::class, ComponentExecuteNodeExecutor::class])
class BlueprintServiceLogicTest {

    private val log = LoggerFactory.getLogger(BlueprintServiceLogicTest::class.java)

    val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime("1234",
            "./../../../../../components/model-catalog/blueprint-model/starter-blueprint/baseconfiguration")

    @Autowired
    lateinit var blueprintSvcLogicService: BlueprintSvcLogicService

    @Test
    fun testExecuteGraphWithSingleComponent() {

        val graph = SvcGraphUtils.getSvcGraphFromClassPathFile("service-logic/one-component.xml")
        val svcLogicContext = BlueprintSvcLogicContext()
        svcLogicContext.setBluePrintRuntimeService(bluePrintRuntimeService)
        svcLogicContext.setRequest(getDefaultExecutionServiceInput())
        blueprintSvcLogicService.execute(graph, svcLogicContext)

    }

    @Test
    fun testExecuteGraphWithMultipleComponents() {
        val graph = SvcGraphUtils.getSvcGraphFromClassPathFile("service-logic/two-component.xml")
        val svcLogicContext = BlueprintSvcLogicContext()
        svcLogicContext.setBluePrintRuntimeService(bluePrintRuntimeService)
        svcLogicContext.setRequest(getDefaultExecutionServiceInput())
        blueprintSvcLogicService.execute(graph, svcLogicContext)

    }

    private fun getDefaultExecutionServiceInput(): ExecutionServiceInput {
        val executionServiceInput = ExecutionServiceInput()
        val commonHeader = CommonHeader()
        commonHeader.requestId = "1234"
        executionServiceInput.commonHeader = commonHeader

        val actionIdentifiers = ActionIdentifiers()
        actionIdentifiers.blueprintName = "baseconfiguration"
        actionIdentifiers.blueprintVersion = "1.0.0"
        actionIdentifiers.actionName = "activate"
        executionServiceInput.actionIdentifiers = actionIdentifiers

        val metaData: MutableMap<String, JsonNode> = hashMapOf()
        metaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_STEP,"resource-assignment-py")
        metaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE, "resource-assignment-py")
        metaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_INTERFACE, "ResourceAssignmentComponent")
        metaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_OPERATION, "process")
        executionServiceInput.metadata = metaData

        return executionServiceInput
    }
}
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

package org.onap.ccsdk.cds.controllerblueprints.validation

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintError
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.Step
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.validation.extension.ResourceDefinitionValidator
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BlueprintDesignTimeValidatorServiceTest {

    private val blueprintBasePath = "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
    private val bluePrintRuntime = BlueprintMetadataUtils.bluePrintRuntime("1234", blueprintBasePath)
    private val mockBlueprintTypeValidatorService = MockBlueprintTypeValidatorService()
    private val resourceDefinitionValidator = mockk<ResourceDefinitionValidator>()
    private val defaultBlueprintValidatorService =
        BlueprintDesignTimeValidatorService(mockBlueprintTypeValidatorService, resourceDefinitionValidator)
    private val workflowValidator = BlueprintWorkflowValidatorImpl(mockBlueprintTypeValidatorService)

    @Test
    fun testValidateOfType() {
        runBlocking {
            every { resourceDefinitionValidator.validate(bluePrintRuntime, any(), any()) } returns Unit

            val valid = defaultBlueprintValidatorService.validateBlueprints(bluePrintRuntime)
            assertTrue(valid, "failed in blueprint Validation")
        }
    }

    @Test
    fun testValidateWorkflowFailToFoundNodeTemplate() {
        val workflowName = "resource-assignment"

        val step = Step()
        step.target = "TestCaseFailNoNodeTemplate"
        val workflow = Workflow()
        workflow.steps = mutableMapOf("test" to step)
        workflowValidator.validate(bluePrintRuntime, workflowName, workflow)

        assertEquals(1, bluePrintRuntime.getBlueprintError().errors.size)
        assertEquals(
            "Failed to validate Workflow(resource-assignment)'s step(test)'s definition : resource-assignment/steps/test : could't get node template for the name(TestCaseFailNoNodeTemplate)",
            bluePrintRuntime.getBlueprintError().errors[0]
        )
    }

    @Test
    fun testValidateWorkflowFailNodeTemplateNotDgGeneric() {
        val workflowName = "resource-assignment"
        val nodeTemplateName = "resource-assignment-process"

        val nodeTemplate = mockk<NodeTemplate>()
        every { nodeTemplate.type } returns "TestNodeType"

        val nodeType = mockk<NodeType>()
        every { nodeType.derivedFrom } returns "tosca.nodes.TEST"

        val blueprintContext = mockk<BlueprintContext>()
        every { blueprintContext.nodeTemplateByName(nodeTemplateName) } returns nodeTemplate
        every { blueprintContext.nodeTemplateNodeType(nodeTemplateName) } returns nodeType

        val bluePrintRuntime = mockk<DefaultBlueprintRuntimeService>("1234")

        every { bluePrintRuntime.getBlueprintError() } returns BlueprintError()
        every { bluePrintRuntime.bluePrintContext() } returns blueprintContext

        val step = Step()
        step.target = nodeTemplateName
        val workflow = Workflow()
        workflow.steps = mutableMapOf("test" to step)
        workflowValidator.validate(bluePrintRuntime, workflowName, workflow)

        assertEquals(1, bluePrintRuntime.getBlueprintError().errors.size)
        assertEquals(
            "Failed to validate Workflow(resource-assignment)'s step(test)'s definition : " +
                "resource-assignment/steps/test : NodeType(TestNodeType) derived from is 'tosca.nodes.TEST', " +
                "Expected 'tosca.nodes.Workflow' or 'tosca.nodes.Component'",
            bluePrintRuntime.getBlueprintError().errors[0]
        )
    }

    @Test
    fun testValidateWorkflowSuccess() {
        val workflowName = "resource-assignment"
        workflowValidator.validate(
            bluePrintRuntime,
            workflowName,
            bluePrintRuntime.bluePrintContext().workflowByName(workflowName)
        )
    }
}

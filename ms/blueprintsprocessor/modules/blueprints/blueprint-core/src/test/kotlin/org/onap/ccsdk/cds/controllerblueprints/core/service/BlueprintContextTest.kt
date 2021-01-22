/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.TestConstants
import org.onap.ccsdk.cds.controllerblueprints.core.data.Activity
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.CapabilityAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.PolicyType
import org.onap.ccsdk.cds.controllerblueprints.core.data.RequirementAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.Step
import org.onap.ccsdk.cds.controllerblueprints.core.data.TopologyTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 *
 *
 * @author Brinda Santh
 */
class BlueprintContextTest {

    private val log = LoggerFactory.getLogger(this::class.toString())

    val blueprintBasePath = TestConstants.PATH_TEST_BLUEPRINTS_BASECONFIG

    @Test
    fun testBlueprintContextCreation() {
        runBlocking {
            val bluePrintContext = BlueprintMetadataUtils.getBlueprintContext(blueprintBasePath)
            assertNotNull(bluePrintContext, "Failed to populate Blueprint context")
        }
    }

    @Test
    fun testChainedProperty() {
        runBlocking {
            val bluePrintContext = BlueprintMetadataUtils.getBlueprintContext(blueprintBasePath)
            val nodeType = bluePrintContext.nodeTypeChained("component-resource-resolution")
            assertNotNull(nodeType, "Failed to get chained node type")
            log.trace("Properties {}", JacksonUtils.getJson(nodeType, true))
        }
    }

    @Test
    fun testImports() {
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.imports = mutableListOf()
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertTrue(bluePrintContext.imports()!!.isEmpty())

        serviceTemplate.imports = null
        assertNull(bluePrintContext.imports())
    }

    @Test
    fun testDataTypes() {
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.dataTypes = mutableMapOf()
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertTrue(bluePrintContext.dataTypes()!!.isEmpty())

        serviceTemplate.dataTypes = null
        assertNull(bluePrintContext.dataTypes())
    }

    @Test
    fun testInputs() {
        val topologyTemplate = TopologyTemplate()
        topologyTemplate.inputs = mutableMapOf()
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.topologyTemplate = topologyTemplate
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertTrue(bluePrintContext.inputs()!!.isEmpty())

        topologyTemplate.inputs = null

        assertNull(bluePrintContext.inputs())
    }

    @Test
    fun testBlueprintJson() {
        val serviceTemplate = ServiceTemplate()
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals("{\"tosca_definitions_version\":\"controller_blueprint_1_0_0\"}", bluePrintContext.blueprintJson())
    }

    @Test(expected = BlueprintException::class)
    fun testName() {
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.metadata = mutableMapOf(BlueprintConstants.METADATA_TEMPLATE_NAME to "hello")
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals("hello", bluePrintContext.name())

        serviceTemplate.metadata = mutableMapOf()
        val bluePrintContext2 = BlueprintContext(serviceTemplate)
        bluePrintContext2.name()
    }

    @Test(expected = BlueprintException::class)
    fun testVersion() {
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.metadata = mutableMapOf(BlueprintConstants.METADATA_TEMPLATE_VERSION to "hello")
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals("hello", bluePrintContext.version())

        serviceTemplate.metadata = mutableMapOf()
        val bluePrintContext2 = BlueprintContext(serviceTemplate)
        bluePrintContext2.version()
    }

    @Test(expected = BlueprintException::class)
    fun testAuthor() {
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.metadata = mutableMapOf(BlueprintConstants.METADATA_TEMPLATE_AUTHOR to "hello")
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals("hello", bluePrintContext.author())

        serviceTemplate.metadata = mutableMapOf()
        val bluePrintContext2 = BlueprintContext(serviceTemplate)
        bluePrintContext2.author()
    }

    @Test
    fun testWorkflows() {
        val topologyTemplate = TopologyTemplate()
        topologyTemplate.workflows = mutableMapOf()
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.topologyTemplate = topologyTemplate
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertTrue(bluePrintContext.workflows()!!.isEmpty())

        topologyTemplate.workflows = null
        assertNull(bluePrintContext.workflows())
    }

    @Test(expected = BlueprintException::class)
    fun testWorkFlowsByName() {
        val topologyTemplate = TopologyTemplate()
        topologyTemplate.workflows = mutableMapOf("workflow" to Workflow())
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.topologyTemplate = topologyTemplate
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertNotNull(bluePrintContext.workflowByName("workflow"))

        bluePrintContext.workflowByName("")
    }

    @Test
    fun testWorkflowInput() {
        val topologyTemplate = TopologyTemplate()
        val workflow = Workflow()
        workflow.inputs = mutableMapOf()
        topologyTemplate.workflows = mutableMapOf("workflow" to workflow)
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.topologyTemplate = topologyTemplate
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertTrue(bluePrintContext.workflowInputs("workflow")!!.isEmpty())

        workflow.inputs = null

        assertNull(bluePrintContext.workflowInputs("workflow"))
    }

    @Test(expected = BlueprintException::class)
    fun testWorkflowStepByName() {
        val topologyTemplate = TopologyTemplate()
        val workflow = Workflow()
        workflow.steps = mutableMapOf("step" to Step())
        topologyTemplate.workflows = mutableMapOf("workflow" to workflow)
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.topologyTemplate = topologyTemplate
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertNotNull(bluePrintContext.workflowStepByName("workflow", "step"))

        bluePrintContext.workflowStepByName("workflow", "")
    }

    @Test(expected = BlueprintException::class)
    fun testWorkflowStepNodeTemplate() {
        val topologyTemplate = TopologyTemplate()
        val workflow = Workflow()
        val step = Step()
        step.target = "hello"
        workflow.steps = mutableMapOf("step" to step)
        topologyTemplate.workflows = mutableMapOf("workflow" to workflow)
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.topologyTemplate = topologyTemplate
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals("hello", bluePrintContext.workflowStepNodeTemplate("workflow", "step"))

        bluePrintContext.workflowStepNodeTemplate("workflow", "")
    }

    @Test(expected = BlueprintException::class)
    fun testWorkflowFirstStepNodeTemplate() {
        val topologyTemplate = TopologyTemplate()
        val workflow = Workflow()
        val step = Step()
        step.target = "hello"
        workflow.steps = mutableMapOf("step" to step, "step2" to Step())
        topologyTemplate.workflows = mutableMapOf("workflow" to workflow)
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.topologyTemplate = topologyTemplate
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals("hello", bluePrintContext.workflowFirstStepNodeTemplate("workflow"))

        workflow.steps = null
        bluePrintContext.workflowFirstStepNodeTemplate("workflow")
    }

    @Test(expected = BlueprintException::class)
    fun testWorkflowStepFirstCallOperation() {
        val topologyTemplate = TopologyTemplate()
        val workflow = Workflow()
        val step = Step()
        val activity = Activity()
        activity.callOperation = "hello"
        step.activities = arrayListOf(activity)
        workflow.steps = mutableMapOf("step" to step)
        topologyTemplate.workflows = mutableMapOf("workflow" to workflow)
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.topologyTemplate = topologyTemplate
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals("hello", bluePrintContext.workflowStepFirstCallOperation("workflow", "step"))

        bluePrintContext.workflowStepFirstCallOperation("workflow", "")
    }

    @Test
    fun testDatatypeByName() {
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.dataTypes = mutableMapOf("data" to DataType())
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertNotNull(bluePrintContext.dataTypeByName("data"))
        assertNull(bluePrintContext.dataTypeByName(""))
    }

    @Test
    fun testArtifactTypes() {
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.artifactTypes = mutableMapOf()
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertTrue(bluePrintContext.artifactTypes()!!.isEmpty())

        serviceTemplate.artifactTypes = null
        assertNull(bluePrintContext.artifactTypes())
    }

    @Test
    fun testPolicyTypes() {
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.policyTypes = mutableMapOf()
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertTrue(bluePrintContext.policyTypes()!!.isEmpty())

        serviceTemplate.policyTypes = null
        assertNull(bluePrintContext.policyTypes())
    }

    @Test(expected = BlueprintException::class)
    fun testPolicyTypeByName() {
        val serviceTemplate = ServiceTemplate()
        serviceTemplate.policyTypes = mutableMapOf("policy" to PolicyType())
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertNotNull(bluePrintContext.policyTypeByName("policy"))

        bluePrintContext.policyTypeByName("")
    }

    @Test
    fun testPolicyTypesDerivedFrom() {
        val serviceTemplate = ServiceTemplate()
        val policyType = PolicyType()
        policyType.derivedFrom = "hi"
        val policyType2 = PolicyType()
        policyType2.derivedFrom = "hello"
        serviceTemplate.policyTypes = mutableMapOf("policy" to policyType, "policy2" to policyType2)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals(1, bluePrintContext.policyTypesDerivedFrom("hi")!!.size)

        serviceTemplate.policyTypes = null
        assertNull(bluePrintContext.policyTypesDerivedFrom("hi"))
    }

    @Test
    fun testPolicyTypesTarget() {
        val serviceTemplate = ServiceTemplate()
        val policyType = PolicyType()
        policyType.targets = mutableListOf("hi")
        val policyType2 = PolicyType()
        policyType2.targets = mutableListOf()
        serviceTemplate.policyTypes = mutableMapOf("policy" to policyType, "policy2" to policyType2)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals(1, bluePrintContext.policyTypesTarget("hi")!!.size)

        serviceTemplate.policyTypes = null
        assertNull(bluePrintContext.policyTypesTarget("hi"))
    }

    @Test
    fun testPolicyTypesTargetNDerivedFrom() {
        val serviceTemplate = ServiceTemplate()
        val policyType = PolicyType()
        policyType.targets = mutableListOf("hi")
        policyType.derivedFrom = "hi"
        val policyType2 = PolicyType()
        policyType2.targets = mutableListOf()
        policyType2.derivedFrom = "hi"
        serviceTemplate.policyTypes = mutableMapOf("policy" to policyType, "policy2" to policyType2)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals(1, bluePrintContext.policyTypesTargetNDerivedFrom("hi", "hi")!!.size)

        serviceTemplate.policyTypes = null
        assertNull(bluePrintContext.policyTypesTargetNDerivedFrom("hi", "hi"))
    }

    @Test
    fun testNodeTypeDerivedFrom() {
        val serviceTemplate = ServiceTemplate()
        val nodeType = NodeType()
        nodeType.derivedFrom = "hi"
        val nodeType2 = NodeType()
        nodeType2.derivedFrom = "hiii"
        serviceTemplate.nodeTypes = mutableMapOf("node" to nodeType, "node2" to nodeType2)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals(1, bluePrintContext.nodeTypeDerivedFrom("hi")!!.size)

        serviceTemplate.nodeTypes = null
        assertNull(bluePrintContext.nodeTypeDerivedFrom("hi"))
    }

    @Test(expected = BlueprintException::class)
    fun testInterfaceNameForNodeType() {
        val serviceTemplate = ServiceTemplate()
        val nodeType = NodeType()
        nodeType.interfaces = mutableMapOf("hello" to InterfaceDefinition(), "hi" to InterfaceDefinition())
        serviceTemplate.nodeTypes = mutableMapOf("node" to nodeType)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals("hello", bluePrintContext.interfaceNameForNodeType("node"))

        bluePrintContext.interfaceNameForNodeType("")
    }

    @Test
    fun testNodeTemplateForNodeType() {
        val serviceTemplate = ServiceTemplate()
        val nodeTemplate = NodeTemplate()
        nodeTemplate.type = "hello"
        val nodeTemplate2 = NodeTemplate()
        nodeTemplate2.type = "hi"
        serviceTemplate.topologyTemplate = TopologyTemplate()
        serviceTemplate.topologyTemplate!!.nodeTemplates =
            mutableMapOf("node" to nodeTemplate, "node2" to nodeTemplate2)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals(1, bluePrintContext.nodeTemplateForNodeType("hello")!!.size)

        serviceTemplate.topologyTemplate!!.nodeTemplates = null
        assertNull(bluePrintContext.nodeTemplateForNodeType("hello"))
    }

    @Test
    fun testNodeTemplateProperty() {
        val serviceTemplate = ServiceTemplate()
        val nodeTemplate = NodeTemplate()
        nodeTemplate.properties = mutableMapOf("prop" to ObjectMapper().createObjectNode())
        serviceTemplate.topologyTemplate = TopologyTemplate()
        serviceTemplate.topologyTemplate!!.nodeTemplates = mutableMapOf("node" to nodeTemplate)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertNotNull(bluePrintContext.nodeTemplateProperty("node", "prop"))

        assertNull(bluePrintContext.nodeTemplateProperty("node", ""))

        nodeTemplate.properties = null
        assertNull(bluePrintContext.nodeTemplateProperty("node", "prop"))
    }

    @Test
    fun testNodeTemplateArtifacts() {
        val serviceTemplate = ServiceTemplate()
        val nodeTemplate = NodeTemplate()
        nodeTemplate.artifacts = mutableMapOf()
        serviceTemplate.topologyTemplate = TopologyTemplate()
        serviceTemplate.topologyTemplate!!.nodeTemplates = mutableMapOf("node" to nodeTemplate)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertTrue(bluePrintContext.nodeTemplateArtifacts("node")!!.isEmpty())
    }

    @Test(expected = BlueprintException::class)
    fun testNodeTemplateArtifact() {
        val serviceTemplate = ServiceTemplate()
        val nodeTemplate = NodeTemplate()
        nodeTemplate.artifacts = mutableMapOf("art" to ArtifactDefinition())
        serviceTemplate.topologyTemplate = TopologyTemplate()
        serviceTemplate.topologyTemplate!!.nodeTemplates = mutableMapOf("node" to nodeTemplate)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertNotNull(bluePrintContext.nodeTemplateArtifact("node", "art"))

        bluePrintContext.nodeTemplateArtifact("node", "")
    }

    @Test(expected = BlueprintException::class)
    fun testNodeTemplateArtifactForArtifactType() {
        val serviceTemplate = ServiceTemplate()
        val nodeTemplate = NodeTemplate()
        val artifactDefinition = ArtifactDefinition()
        artifactDefinition.type = "type"
        val artifactDefinition2 = ArtifactDefinition()
        artifactDefinition2.type = "No type"
        nodeTemplate.artifacts = mutableMapOf("art" to artifactDefinition, "art2" to artifactDefinition2)
        serviceTemplate.topologyTemplate = TopologyTemplate()
        serviceTemplate.topologyTemplate!!.nodeTemplates = mutableMapOf("node" to nodeTemplate)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertNotNull(bluePrintContext.nodeTemplateArtifactForArtifactType("node", "type"))

        bluePrintContext.nodeTemplateArtifactForArtifactType("", "")
    }

    @Test(expected = BlueprintException::class)
    fun testNodeTemplateFirstInterface() {
        val serviceTemplate = ServiceTemplate()
        val nodeTemplate = NodeTemplate()
        nodeTemplate.interfaces = mutableMapOf("interface" to InterfaceAssignment(), "interf" to InterfaceAssignment())
        serviceTemplate.topologyTemplate = TopologyTemplate()
        serviceTemplate.topologyTemplate!!.nodeTemplates = mutableMapOf("node" to nodeTemplate)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertNotNull(bluePrintContext.nodeTemplateFirstInterface("node"))

        nodeTemplate.interfaces = null
        bluePrintContext.nodeTemplateFirstInterface("node")
    }

    @Test(expected = BlueprintException::class)
    fun testNodeTemplateFirstInterfaceName() {
        val serviceTemplate = ServiceTemplate()
        val nodeTemplate = NodeTemplate()
        nodeTemplate.interfaces = mutableMapOf("interface" to InterfaceAssignment(), "interf" to InterfaceAssignment())
        serviceTemplate.topologyTemplate = TopologyTemplate()
        serviceTemplate.topologyTemplate!!.nodeTemplates = mutableMapOf("node" to nodeTemplate)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals("interface", bluePrintContext.nodeTemplateFirstInterfaceName("node"))

        nodeTemplate.interfaces = null
        bluePrintContext.nodeTemplateFirstInterfaceName("node")
    }

    @Test(expected = BlueprintException::class)
    fun testNodeTemplateFirstInterfaceFirstOperationName() {
        val serviceTemplate = ServiceTemplate()
        val nodeTemplate = NodeTemplate()
        val interfaceAssignment = InterfaceAssignment()
        interfaceAssignment.operations = mutableMapOf("op" to OperationAssignment(), "op2" to OperationAssignment())
        nodeTemplate.interfaces = mutableMapOf("intf" to interfaceAssignment)
        serviceTemplate.topologyTemplate = TopologyTemplate()
        serviceTemplate.topologyTemplate!!.nodeTemplates = mutableMapOf("node" to nodeTemplate)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertEquals("op", bluePrintContext.nodeTemplateFirstInterfaceFirstOperationName("node"))

        interfaceAssignment.operations = null
        bluePrintContext.nodeTemplateFirstInterfaceFirstOperationName("node")
    }

    @Test(expected = BlueprintException::class)
    fun testNodeTemplateCapability() {
        val serviceTemplate = ServiceTemplate()
        val nodeTemplate = NodeTemplate()
        nodeTemplate.capabilities = mutableMapOf("cap" to CapabilityAssignment())
        serviceTemplate.topologyTemplate = TopologyTemplate()
        serviceTemplate.topologyTemplate!!.nodeTemplates = mutableMapOf("node" to nodeTemplate)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertNotNull(bluePrintContext.nodeTemplateCapability("node", "cap"))

        bluePrintContext.nodeTemplateCapability("node", "")
    }

    @Test(expected = BlueprintException::class)
    fun testNodeTemplateRequirement() {
        val serviceTemplate = ServiceTemplate()
        val nodeTemplate = NodeTemplate()
        nodeTemplate.requirements = mutableMapOf("req" to RequirementAssignment())
        serviceTemplate.topologyTemplate = TopologyTemplate()
        serviceTemplate.topologyTemplate!!.nodeTemplates = mutableMapOf("node" to nodeTemplate)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertNotNull(bluePrintContext.nodeTemplateRequirement("node", "req"))

        bluePrintContext.nodeTemplateRequirement("node", "")
    }

    @Test(expected = BlueprintException::class)
    fun testNodeTemplateRequirementNode() {
        val serviceTemplate = ServiceTemplate()
        val nodeTemplate = NodeTemplate()
        val requirementAssignment = RequirementAssignment()
        requirementAssignment.node = "node"
        nodeTemplate.requirements = mutableMapOf("req" to requirementAssignment)
        serviceTemplate.topologyTemplate = TopologyTemplate()
        serviceTemplate.topologyTemplate!!.nodeTemplates = mutableMapOf("node" to nodeTemplate)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertNotNull(bluePrintContext.nodeTemplateRequirementNode("node", "req"))

        bluePrintContext.nodeTemplateRequirementNode("node", "")
    }

    @Test
    fun testNodeTemplateCapabilityProperty() {
        val serviceTemplate = ServiceTemplate()
        val nodeTemplate = NodeTemplate()
        val capabilityAssignment = CapabilityAssignment()
        capabilityAssignment.properties = mutableMapOf("prop" to ObjectMapper().createObjectNode())
        nodeTemplate.capabilities = mutableMapOf("cap" to capabilityAssignment)
        serviceTemplate.topologyTemplate = TopologyTemplate()
        serviceTemplate.topologyTemplate!!.nodeTemplates = mutableMapOf("node" to nodeTemplate)
        val bluePrintContext = BlueprintContext(serviceTemplate)

        assertNotNull(bluePrintContext.nodeTemplateCapabilityProperty("node", "cap", "prop"))

        capabilityAssignment.properties = null

        assertNull(bluePrintContext.nodeTemplateCapabilityProperty("node", "cap", "prop"))
    }
}

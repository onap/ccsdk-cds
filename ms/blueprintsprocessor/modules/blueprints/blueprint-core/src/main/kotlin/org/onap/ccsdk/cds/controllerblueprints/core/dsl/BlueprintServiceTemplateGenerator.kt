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

package org.onap.ccsdk.cds.controllerblueprints.core.dsl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.bpClone
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.AttributeDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.TopologyTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow

/**
 * Generate Service Template for the simplified DSL.
 * @author Brinda Santh
 */
class BlueprintServiceTemplateGenerator(private val dslBlueprint: DSLBlueprint) {

    private var serviceTemplate = ServiceTemplate()

    private val nodeTypes: MutableMap<String, NodeType> = hashMapOf()
    private val artifactTypes: MutableMap<String, ArtifactType> = hashMapOf()
    private val dataTypes: MutableMap<String, DataType> = hashMapOf()

    fun serviceTemplate(): ServiceTemplate {
        serviceTemplate.metadata = dslBlueprint.metadata
        serviceTemplate.dslDefinitions = dslBlueprint.properties

        dataTypes.putAll(dslBlueprint.dataTypes)
        artifactTypes.putAll(dslBlueprint.artifactTypes)

        serviceTemplate.dataTypes = dataTypes
        serviceTemplate.artifactTypes = artifactTypes
        serviceTemplate.nodeTypes = nodeTypes

        serviceTemplate.topologyTemplate = populateTopologyTemplate()

        return serviceTemplate
    }

    private fun populateTopologyTemplate(): TopologyTemplate {
        val topologyTemplate = TopologyTemplate()
        topologyTemplate.nodeTemplates = populateNodeTemplates()
        topologyTemplate.workflows = populateWorkflow()
        return topologyTemplate
    }

    private fun populateNodeTemplates(): MutableMap<String, NodeTemplate> {

        val nodeTemplates: MutableMap<String, NodeTemplate> = hashMapOf()

        // For New or Dynamic Components
        val components = dslBlueprint.components
        components.forEach { (dslCompName, dslComp) ->
            val nodeTemplate = NodeTemplate()
            nodeTemplate.type = dslComp.type
            nodeTemplate.properties = propertyAssignments(dslComp.properties)
            nodeTemplate.artifacts = dslComp.artifacts
            nodeTemplate.interfaces = populateInterfaceAssignments(dslComp)
            nodeTemplates[dslCompName] = nodeTemplate

            /** Populate Type **/
            nodeTypes[dslComp.type] = populateNodeType(dslComp)
        }

        // For Registry Components
        val registryComponents = dslBlueprint.registryComponents
        registryComponents.forEach { (dslCompName, dslComp) ->
            val nodeTemplate = NodeTemplate()
            nodeTemplate.type = dslComp.type
            nodeTemplate.properties = dslComp.properties
            nodeTemplate.artifacts = dslComp.artifacts
            nodeTemplate.interfaces = populateInterfaceAssignments(dslComp)
            nodeTemplates[dslCompName] = nodeTemplate
        }
        return nodeTemplates
    }

    private fun populateWorkflow(): MutableMap<String, Workflow>? {
        var workflows: MutableMap<String, Workflow>? = null
        if (dslBlueprint.workflows.isNotEmpty()) {
            workflows = hashMapOf()

            dslBlueprint.workflows.forEach { (dslWorkflowName, dslWorkflow) ->
                val workflow = Workflow()
                workflow.description = dslWorkflow.description
                workflow.steps = dslWorkflow.steps
                workflow.inputs = dslWorkflow.inputs
                workflow.outputs = dslWorkflow.outputs
                workflows[dslWorkflowName] = workflow
            }
        }
        return workflows
    }

    private fun populateNodeType(dslComponent: DSLComponent): NodeType {
        val nodeType = NodeType()
        nodeType.derivedFrom = BlueprintConstants.MODEL_TYPE_NODES_ROOT
        nodeType.version = dslComponent.version
        nodeType.description = dslComponent.description
        nodeType.interfaces = populateInterfaceDefinitions(dslComponent, nodeType)
        return nodeType
    }

    private fun populateInterfaceDefinitions(dslComponent: DSLComponent, nodeType: NodeType): MutableMap<String, InterfaceDefinition> {

        // Populate Node Type Attribute
        nodeType.attributes = attributeDefinitions(dslComponent.attributes)

        val operationDefinition = OperationDefinition()
        operationDefinition.inputs = propertyDefinitions(dslComponent.inputs)
        operationDefinition.outputs = propertyDefinitions(dslComponent.outputs)

        val operations: MutableMap<String, OperationDefinition> = hashMapOf()
        operations[BlueprintConstants.DEFAULT_STEP_OPERATION] = operationDefinition

        val interfaceDefinition = InterfaceDefinition()
        interfaceDefinition.operations = operations

        val interfaces: MutableMap<String, InterfaceDefinition> = hashMapOf()
        interfaces[BlueprintConstants.DEFAULT_STEP_INTERFACE] = interfaceDefinition
        return interfaces
    }

    private fun populateInterfaceAssignments(dslComponent: DSLRegistryComponent): MutableMap<String, InterfaceAssignment> {
        val operationAssignment = OperationAssignment()
        operationAssignment.implementation = dslComponent.implementation
        operationAssignment.inputs = dslComponent.inputs
        operationAssignment.outputs = dslComponent.outputs

        val operations: MutableMap<String, OperationAssignment> = hashMapOf()
        operations[BlueprintConstants.DEFAULT_STEP_OPERATION] = operationAssignment

        val interfaceAssignment = InterfaceAssignment()
        interfaceAssignment.operations = operations

        val interfaces: MutableMap<String, InterfaceAssignment> = hashMapOf()
        interfaces[dslComponent.interfaceName] = interfaceAssignment
        return interfaces
    }

    private fun populateInterfaceAssignments(dslComponent: DSLComponent): MutableMap<String, InterfaceAssignment> {
        val operationAssignment = OperationAssignment()
        operationAssignment.implementation = dslComponent.implementation
        operationAssignment.inputs = propertyAssignments(dslComponent.inputs)
        operationAssignment.outputs = propertyAssignments(dslComponent.outputs)

        val operations: MutableMap<String, OperationAssignment> = hashMapOf()
        operations[BlueprintConstants.DEFAULT_STEP_OPERATION] = operationAssignment

        val interfaceAssignment = InterfaceAssignment()
        interfaceAssignment.operations = operations

        val interfaces: MutableMap<String, InterfaceAssignment> = hashMapOf()
        interfaces[BlueprintConstants.DEFAULT_STEP_INTERFACE] = interfaceAssignment
        return interfaces
    }

    private fun propertyDefinitions(propertyDefinitions: Map<String, PropertyDefinition>?): MutableMap<String, PropertyDefinition>? {
        val definitions: MutableMap<String, PropertyDefinition>? = propertyDefinitions?.bpClone()?.toMutableMap()

        definitions?.forEach { (_, prop) ->
            prop.value = null
        }
        return definitions
    }

    private fun attributeDefinitions(attributeDefinitions: Map<String, AttributeDefinition>?): MutableMap<String, AttributeDefinition>? {
        val definitions: MutableMap<String, AttributeDefinition>? = attributeDefinitions?.bpClone()?.toMutableMap()

        definitions?.forEach { (_, prop) ->
            prop.value = null
        }
        return definitions
    }

    private fun propertyAssignments(propertyDefinitions: Map<String, PropertyDefinition>?): MutableMap<String, JsonNode>? {
        var assignments: MutableMap<String, JsonNode>? = null
        if (propertyDefinitions != null) {
            assignments = hashMapOf()
            propertyDefinitions.forEach { (propertyName, property) ->
                assignments[propertyName] = property.value ?: NullNode.instance
            }
        }
        return assignments
    }
}

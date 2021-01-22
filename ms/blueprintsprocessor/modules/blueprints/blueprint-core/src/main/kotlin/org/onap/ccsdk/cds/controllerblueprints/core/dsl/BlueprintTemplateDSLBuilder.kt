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
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.CapabilityAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.Implementation
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.RequirementAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.TopologyTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.jvm.reflect

open class TopologyTemplateBuilder {

    private var topologyTemplate = TopologyTemplate()
    var nodeTemplates: MutableMap<String, NodeTemplate>? = null
    var relationshipTemplates: MutableMap<String, RelationshipTemplate>? = null
    private var workflows: MutableMap<String, Workflow>? = null

    fun nodeTemplate(id: String, type: String, description: String, block: NodeTemplateBuilder.() -> Unit) {
        if (nodeTemplates == null) nodeTemplates = hashMapOf()
        nodeTemplates!![id] = NodeTemplateBuilder(id, type, description).apply(block).build()
    }

    fun nodeTemplate(nodeTemplate: NodeTemplate) {
        if (nodeTemplates == null) nodeTemplates = hashMapOf()
        nodeTemplates!![nodeTemplate.id!!] = nodeTemplate
    }

    fun relationshipTemplate(
        id: String,
        type: String,
        description: String,
        block: RelationshipTemplateBuilder.() -> Unit
    ) {
        if (relationshipTemplates == null) relationshipTemplates = hashMapOf()
        relationshipTemplates!![id] = RelationshipTemplateBuilder(id, type, description).apply(block).build()
    }

    fun relationshipTemplate(relationshipTemplate: RelationshipTemplate) {
        if (relationshipTemplates == null) relationshipTemplates = hashMapOf()
        relationshipTemplates!![relationshipTemplate.id!!] = relationshipTemplate
    }

    fun nodeTemplateOperation(
        nodeTemplateName: String,
        type: String,
        interfaceName: String,
        description: String,
        operationBlock: OperationAssignmentBuilder<PropertiesAssignmentBuilder,
            PropertiesAssignmentBuilder>.() -> Unit
    ) {
        if (nodeTemplates == null) nodeTemplates = hashMapOf()

        val nodeTemplateBuilder = NodeTemplateBuilder(nodeTemplateName, type, description)
        nodeTemplateBuilder.operation(interfaceName, "$description operation", operationBlock)
        nodeTemplates!![nodeTemplateName] = nodeTemplateBuilder.build()
    }

    fun workflow(id: String, description: String, block: WorkflowBuilder.() -> Unit) {
        if (workflows == null) workflows = hashMapOf()
        workflows!![id] = WorkflowBuilder(id, description).apply(block).build()
    }

    fun workflow(workflow: Workflow) {
        if (workflows == null) workflows = hashMapOf()
        workflows!![workflow.id!!] = workflow
    }

    // TODO("populate inputs, outputs")
    fun workflowNodeTemplate(
        actionName: String,
        nodeTemplateType: String,
        description: String,
        block: NodeTemplateBuilder.() -> Unit
    ) {
        if (nodeTemplates == null) nodeTemplates = hashMapOf()

        if (workflows == null) workflows = hashMapOf()

        val workflowBuilder = WorkflowBuilder(actionName, description)
        workflowBuilder.nodeTemplateStep(actionName, description)
        // Workflow name is NodeTemplate name
        workflows!![actionName] = workflowBuilder.build()

        nodeTemplates!![actionName] =
            NodeTemplateBuilder(actionName, nodeTemplateType, description).apply(block).build()
    }

    fun build(): TopologyTemplate {
        topologyTemplate.nodeTemplates = nodeTemplates
        topologyTemplate.relationshipTemplates = relationshipTemplates
        topologyTemplate.workflows = workflows
        return topologyTemplate
    }
}

open class NodeTemplateBuilder(
    private val id: String,
    private val type: String,
    private val description: String? = ""
) {

    var nodeTemplate: NodeTemplate = NodeTemplate()
    var properties: MutableMap<String, JsonNode>? = null
    var interfaces: MutableMap<String, InterfaceAssignment>? = null
    var artifacts: MutableMap<String, ArtifactDefinition>? = null
    var capabilities: MutableMap<String, CapabilityAssignment>? = null
    var requirements: MutableMap<String, RequirementAssignment>? = null

    fun properties(properties: Map<String, JsonNode>) {
        if (this.properties == null) this.properties = hashMapOf()
        this.properties!!.putAll(properties)
    }

    fun properties(block: PropertiesAssignmentBuilder.() -> Unit) {
        if (properties == null) properties = hashMapOf()
        properties = PropertiesAssignmentBuilder().apply(block).build()
    }

    open fun <Prop : PropertiesAssignmentBuilder> typedProperties(block: Prop.() -> Unit) {
        if (properties == null) properties = hashMapOf()
        val instance: Prop = (block.reflect()?.parameters?.get(0)?.type?.classifier as KClass<Prop>).createInstance()
        properties = instance.apply(block).build()
    }

    open fun <In : PropertiesAssignmentBuilder, Out : PropertiesAssignmentBuilder> typedOperation(
        interfaceName: String,
        description: String = "",
        block: OperationAssignmentBuilder<In, Out>.() -> Unit
    ) {
        if (interfaces == null) interfaces = hashMapOf()

        val interfaceAssignment = InterfaceAssignment()
        val defaultOperationName = BlueprintConstants.DEFAULT_STEP_OPERATION
        interfaceAssignment.operations = hashMapOf()
        interfaceAssignment.operations!![defaultOperationName] =
            OperationAssignmentBuilder<In, Out>(defaultOperationName, description).apply(block).build()
        interfaces!![interfaceName] = interfaceAssignment
    }

    fun operation(
        interfaceName: String,
        description: String,
        block: OperationAssignmentBuilder<PropertiesAssignmentBuilder, PropertiesAssignmentBuilder>.() -> Unit
    ) {
        typedOperation<PropertiesAssignmentBuilder, PropertiesAssignmentBuilder>(interfaceName, description, block)
    }

    fun artifact(id: String, type: String, file: String) {
        if (artifacts == null) artifacts = hashMapOf()
        artifacts!![id] = ArtifactDefinitionBuilder(id, type, file).build()
    }

    fun artifact(id: String, type: String, file: String, block: ArtifactDefinitionBuilder.() -> Unit) {
        if (artifacts == null) artifacts = hashMapOf()
        artifacts!![id] = ArtifactDefinitionBuilder(id, type, file).apply(block).build()
    }

    fun artifacts(artifacts: MutableMap<String, ArtifactDefinition>?) {
        this.artifacts = artifacts
    }

    fun capability(id: String, block: CapabilityAssignmentBuilder.() -> Unit) {
        if (capabilities == null) capabilities = hashMapOf()
        capabilities!![id] = CapabilityAssignmentBuilder(id).apply(block).build()
    }

    fun capabilities(capabilities: MutableMap<String, CapabilityAssignment>) {
        if (this.capabilities == null) this.capabilities = hashMapOf()
        this.capabilities!!.putAll(capabilities)
    }

    fun requirement(id: String, capability: String, node: String, relationship: String) {
        if (requirements == null) requirements = hashMapOf()
        requirements!![id] = RequirementAssignmentBuilder(id, capability, node, relationship).build()
    }

    fun requirements(requirements: MutableMap<String, RequirementAssignment>) {
        if (this.requirements == null) this.requirements = hashMapOf()
        this.requirements!!.putAll(requirements)
    }

    open fun build(): NodeTemplate {
        nodeTemplate.id = id
        nodeTemplate.type = type
        nodeTemplate.description = description
        nodeTemplate.properties = properties
        nodeTemplate.interfaces = interfaces
        nodeTemplate.artifacts = artifacts
        nodeTemplate.capabilities = capabilities
        nodeTemplate.requirements = requirements
        return nodeTemplate
    }
}

open class RelationshipTemplateBuilder(
    private val id: String,
    private val type: String,
    private val description: String? = ""
) {

    var relationshipTemplate: RelationshipTemplate = RelationshipTemplate()
    var properties: MutableMap<String, JsonNode>? = null

    fun properties(properties: Map<String, JsonNode>) {
        if (this.properties == null) this.properties = hashMapOf()
        this.properties!!.putAll(properties)
    }

    fun properties(block: PropertiesAssignmentBuilder.() -> Unit) {
        if (properties == null) properties = hashMapOf()
        properties = PropertiesAssignmentBuilder().apply(block).build()
    }

    fun property(id: String, value: Any) {
        if (properties == null) properties = hashMapOf()
        properties!![id] = value.asJsonType()
    }

    fun property(id: String, value: JsonNode) {
        if (properties == null) properties = hashMapOf()
        properties!![id] = value
    }

    fun copy(copy: String) {
        relationshipTemplate.copy = copy
    }

    open fun <Prop : PropertiesAssignmentBuilder> typedProperties(block: Prop.() -> Unit) {
        if (properties == null) properties = hashMapOf()
        val instance: Prop = (block.reflect()?.parameters?.get(0)?.type?.classifier as KClass<Prop>).createInstance()
        properties = instance.apply(block).build()
    }

    open fun build(): RelationshipTemplate {
        relationshipTemplate.id = id
        relationshipTemplate.type = type
        relationshipTemplate.description = description
        relationshipTemplate.properties = properties
        return relationshipTemplate
    }
}

class ArtifactDefinitionBuilder(private val id: String, private val type: String, private val file: String) {

    private var artifactDefinition: ArtifactDefinition = ArtifactDefinition()
    private var properties: MutableMap<String, JsonNode>? = null

    fun repository(repository: String) {
        artifactDefinition.repository = repository
    }

    fun deployPath(deployPath: String) {
        artifactDefinition.deployPath = deployPath
    }

    fun properties(block: PropertiesAssignmentBuilder.() -> Unit) {
        if (properties == null)
            properties = hashMapOf()
        properties = PropertiesAssignmentBuilder().apply(block).build()
    }

    fun build(): ArtifactDefinition {
        artifactDefinition.id = id
        artifactDefinition.type = type
        artifactDefinition.file = file
        artifactDefinition.properties = properties
        return artifactDefinition
    }
}

open class CapabilityAssignmentBuilder(private val id: String) {

    var capabilityAssignment: CapabilityAssignment = CapabilityAssignment()
    var attributes: MutableMap<String, JsonNode>? = null
    var properties: MutableMap<String, JsonNode>? = null

    fun attributes(block: AttributesAssignmentBuilder.() -> Unit) {
        if (attributes == null) attributes = hashMapOf()
        attributes = AttributesAssignmentBuilder().apply(block).build()
    }

    fun properties(block: PropertiesAssignmentBuilder.() -> Unit) {
        if (properties == null) properties = hashMapOf()
        properties = PropertiesAssignmentBuilder().apply(block).build()
    }

    fun property(id: String, value: Any) {
        if (properties == null) properties = hashMapOf()
        properties!![id] = value.asJsonType()
    }

    fun property(id: String, value: JsonNode) {
        if (properties == null) properties = hashMapOf()
        properties!![id] = value
    }

    open fun build(): CapabilityAssignment {
        capabilityAssignment.properties = properties
        capabilityAssignment.attributes = attributes
        return capabilityAssignment
    }
}

open class RequirementAssignmentBuilder(
    private val id: String,
    private val capability: String,
    private val node: String,
    private val relationship: String
) {

    private var requirementAssignment: RequirementAssignment = RequirementAssignment()

    fun build(): RequirementAssignment {
        requirementAssignment.id = id
        requirementAssignment.capability = capability
        requirementAssignment.node = node
        requirementAssignment.relationship = relationship
        return requirementAssignment
    }
}

class InterfaceAssignmentBuilder(private val id: String) {

    private var interfaceAssignment: InterfaceAssignment = InterfaceAssignment()
    private var operations: MutableMap<String, OperationAssignment>? = null

    fun operation(
        id: String,
        description: String? = "",
        block: OperationAssignmentBuilder<PropertiesAssignmentBuilder, PropertiesAssignmentBuilder>.() -> Unit
    ) {
        if (operations == null)
            operations = hashMapOf()
        operations!![id] = OperationAssignmentBuilder<PropertiesAssignmentBuilder, PropertiesAssignmentBuilder>(
            id, description
        ).apply(block).build()
    }

    fun build(): InterfaceAssignment {
        interfaceAssignment.id = id
        interfaceAssignment.operations = operations
        return interfaceAssignment
    }
}

class OperationAssignmentBuilder<In : PropertiesAssignmentBuilder, Out : PropertiesAssignmentBuilder>(
    private val id: String,
    private val description: String? = ""
) {

    private var operationAssignment: OperationAssignment = OperationAssignment()

    fun implementation(implementation: Implementation?) {
        operationAssignment.implementation = implementation
    }

    fun implementation(timeout: Int, operationHost: String? = BlueprintConstants.PROPERTY_SELF) {
        operationAssignment.implementation = Implementation().apply {
            this.operationHost = operationHost!!
            this.timeout = timeout
        }
    }

    fun implementation(
        timeout: Int,
        operationHost: String? = BlueprintConstants.PROPERTY_SELF,
        block: ImplementationBuilder.() -> Unit
    ) {
        operationAssignment.implementation = ImplementationBuilder(timeout, operationHost!!).apply(block).build()
    }

    fun inputs(inputs: MutableMap<String, JsonNode>?) {
        operationAssignment.inputs = inputs
    }

    fun inputs(block: In.() -> Unit) {
        val instance: In = (block.reflect()?.parameters?.get(0)?.type?.classifier as KClass<In>).createInstance()
        operationAssignment.inputs = instance.apply(block).build()
    }

    fun outputs(outputs: MutableMap<String, JsonNode>?) {
        operationAssignment.outputs = outputs
    }

    fun outputs(block: Out.() -> Unit) {
        val instance: Out = (block.reflect()?.parameters?.get(0)?.type?.classifier as KClass<Out>).createInstance()
        operationAssignment.outputs = instance.apply(block).build()
    }

    fun build(): OperationAssignment {
        operationAssignment.id = id
        operationAssignment.description = description
        return operationAssignment
    }
}

class ImplementationBuilder(private val timeout: Int, private val operationHost: String) {

    private val implementation = Implementation()

    fun primary(primary: String) {
        implementation.primary = primary
    }

    fun dependencies(vararg dependencies: String) {
        if (implementation.dependencies == null)
            implementation.dependencies = arrayListOf()
        dependencies.forEach {
            implementation.dependencies!!.add(it)
        }
    }

    fun build(): Implementation {
        implementation.timeout = timeout
        implementation.operationHost = operationHost
        return implementation
    }
}

open class PropertiesAssignmentBuilder {

    var properties: MutableMap<String, JsonNode> = hashMapOf()

    fun property(id: String, value: Any) {
        property(id, value.asJsonType())
    }

    fun property(id: String, value: JsonNode) {
        properties[id] = value
    }

    fun property(kProperty: KMutableProperty1<*, *>, value: JsonNode) {
        properties[kProperty.name] = value
    }

    open fun build(): MutableMap<String, JsonNode> {
        return properties
    }
}

open class AttributesAssignmentBuilder {

    var attributes: MutableMap<String, JsonNode> = hashMapOf()

    fun attribute(id: String, value: String) {
        attribute(id, value.asJsonType())
    }

    fun attribute(id: String, value: JsonNode) {
        attributes[id] = value
    }

    fun build(): MutableMap<String, JsonNode> {
        return attributes
    }
}

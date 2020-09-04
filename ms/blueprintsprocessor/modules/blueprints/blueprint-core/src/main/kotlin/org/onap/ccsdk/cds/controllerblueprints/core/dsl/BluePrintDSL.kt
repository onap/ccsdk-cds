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
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType

// CDS DSLs
fun blueprint(
    name: String,
    version: String,
    author: String,
    tags: String,
    block: DSLBluePrintBuilder.() -> Unit
): DSLBluePrint {
    return DSLBluePrintBuilder(name, version, author, tags).apply(block).build()
}

// TOSCA DSLs
fun serviceTemplate(
    name: String,
    version: String,
    author: String,
    tags: String,
    block: ServiceTemplateBuilder.() -> Unit
): ServiceTemplate {
    return ServiceTemplateBuilder(name, version, author, tags).apply(block).build()
}

fun workflow(id: String, description: String, block: WorkflowBuilder.() -> Unit): Workflow {
    return WorkflowBuilder(id, description).apply(block).build()
}

fun nodeTemplate(
    id: String,
    type: String,
    description: String,
    block: NodeTemplateBuilder.() -> Unit
): NodeTemplate {
    return NodeTemplateBuilder(id, type, description).apply(block).build()
}

fun nodeType(
    id: String,
    version: String,
    derivedFrom: String,
    description: String,
    block: NodeTypeBuilder.() -> Unit
): NodeType {
    return NodeTypeBuilder(id, version, derivedFrom, description).apply(block).build()
}

fun dataType(
    id: String,
    version: String,
    derivedFrom: String,
    description: String,
    block: DataTypeBuilder.() -> Unit
): DataType {
    return DataTypeBuilder(id, version, derivedFrom, description).apply(block).build()
}

fun artifactType(
    id: String,
    version: String,
    derivedFrom: String,
    description: String,
    block: ArtifactTypeBuilder.() -> Unit
): ArtifactType {
    return ArtifactTypeBuilder(id, version, derivedFrom, description).apply(block).build()
}

fun relationshipType(
    id: String,
    version: String,
    derivedFrom: String,
    description: String,
    block: RelationshipTypeBuilder.() -> Unit
): RelationshipType {
    return RelationshipTypeBuilder(id, version, derivedFrom, description).apply(block).build()
}

// DSL Function
fun dslExpression(key: String): JsonNode {
    return ("*$key").asJsonPrimitive()
}
// Input Function

fun getInput(inputKey: String, jsonPath: String? = null): JsonNode {
    return """{"get_input": "$inputKey"}""".jsonAsJsonType()
}

fun getAttribute(attributeId: String, jsonPath: String? = null): JsonNode {
    return getNodeTemplateAttribute("SELF", attributeId, jsonPath)
}

fun getNodeTemplateAttribute(nodeTemplateId: String, attributeId: String): JsonNode {
    return getNodeTemplateAttribute(nodeTemplateId, attributeId, null)
}

fun getNodeTemplateAttribute(nodeTemplateId: String, attributeId: String, jsonPath: String?): JsonNode {
    return if (jsonPath.isNullOrEmpty() || jsonPath.isNullOrBlank()) {
        """{"get_attribute": ["$nodeTemplateId", "$attributeId"]}""".jsonAsJsonType()
    } else {
        """{"get_attribute": ["$nodeTemplateId", "$attributeId", "$jsonPath"]}""".jsonAsJsonType()
    }
}

// Property Function

fun getProperty(propertyId: String, jsonPath: String? = null): JsonNode {
    return getNodeTemplateProperty("SELF", propertyId, jsonPath)
}

fun getNodeTemplateProperty(nodeTemplateName: String, propertyId: String): JsonNode {
    return getNodeTemplateProperty(nodeTemplateName, propertyId, null)
}

fun getNodeTemplateProperty(nodeTemplateName: String, propertyId: String, jsonPath: String?): JsonNode {
    return if (jsonPath.isNullOrEmpty() || jsonPath.isNullOrBlank()) {
        """{"get_property": ["$nodeTemplateName", "$propertyId"]}""".jsonAsJsonType()
    } else {
        """{"get_property": ["$nodeTemplateName", "$propertyId", "$jsonPath"]}""".jsonAsJsonType()
    }
}

// Artifact Function

fun getArtifact(artifactId: String): JsonNode {
    return getNodeTemplateArtifact("SELF", artifactId)
}

fun getNodeTemplateArtifact(nodeTemplateName: String, artifactId: String): JsonNode {
    return """{"get_artifact": ["$nodeTemplateName", "$artifactId"]}""".jsonAsJsonType()
}

// Operation Function

fun getNodeTemplateOperationOutput(
    nodeTemplateName: String,
    interfaceName: String,
    propertyId: String,
    jsonPath: String? = null
): JsonNode {
    return """{"get_operation_output": ["$nodeTemplateName", "$interfaceName", "process","$propertyId","$jsonPath" ]}""".trimMargin()
        .jsonAsJsonType()
}

/** Blueprint Type Extensions */
fun ServiceTemplateBuilder.nodeTypeComponent() {
    val nodeType = BluePrintTypes.nodeTypeComponent()
    if (this.nodeTypes == null) this.nodeTypes = hashMapOf()
    this.nodeTypes!![nodeType.id!!] = nodeType
}

fun BluePrintTypes.nodeTypeComponent(): NodeType {
    return nodeType(
        id = BluePrintConstants.MODEL_TYPE_NODE_COMPONENT,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_NODES_ROOT,
        description = "This is default Component Node"
    ) {
    }
}

@Deprecated("CDS won't support, use implerative workflow definitions.")
fun BluePrintTypes.nodeTypeWorkflow(): NodeType {
    return nodeType(
        id = BluePrintConstants.MODEL_TYPE_NODE_WORKFLOW,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_NODES_ROOT,
        description = "This is default Workflow Node"
    ) {
    }
}

fun ServiceTemplateBuilder.nodeTypeVnf() {
    val nodeType = BluePrintTypes.nodeTypeVnf()
    if (this.nodeTypes == null) this.nodeTypes = hashMapOf()
    this.nodeTypes!![nodeType.id!!] = nodeType
}

fun BluePrintTypes.nodeTypeVnf(): NodeType {
    return nodeType(
        id = BluePrintConstants.MODEL_TYPE_NODE_VNF,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_NODES_ROOT,
        description = "This is default VNF Node"
    ) {
    }
}

fun ServiceTemplateBuilder.nodeTypeResourceSource() {
    val nodeType = BluePrintTypes.nodeTypeResourceSource()
    if (this.nodeTypes == null) this.nodeTypes = hashMapOf()
    this.nodeTypes!![nodeType.id!!] = nodeType
}

fun BluePrintTypes.nodeTypeResourceSource(): NodeType {
    return nodeType(
        id = BluePrintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_NODES_ROOT,
        description = "This is default Resource Source Node"
    ) {
    }
}

/** Artifacts */
fun ServiceTemplateBuilder.artifactTypeTemplateVelocity() {
    val artifactType = BluePrintTypes.artifactTypeTemplateVelocity()
    if (this.artifactTypes == null) this.artifactTypes = hashMapOf()
    this.artifactTypes!![artifactType.id!!] = artifactType
}

fun BluePrintTypes.artifactTypeTemplateVelocity(): ArtifactType {
    return artifactType(
        id = BluePrintConstants.MODEL_TYPE_ARTIFACT_TEMPLATE_VELOCITY,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
        description = "Velocity Artifact"
    ) {
        fileExt("vtl")
    }
}

fun ServiceTemplateBuilder.artifactTypeTempleJinja() {
    val artifactType = BluePrintTypes.artifactTypeTempleJinja()
    if (this.artifactTypes == null) this.artifactTypes = hashMapOf()
    this.artifactTypes!![artifactType.id!!] = artifactType
}

fun BluePrintTypes.artifactTypeTempleJinja(): ArtifactType {
    return artifactType(
        id = BluePrintConstants.MODEL_TYPE_ARTIFACT_TEMPLATE_JINJA,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
        description = "Jinja Artifact"
    ) {
        fileExt("jinja")
    }
}

fun ServiceTemplateBuilder.artifactTypeMappingResource() {
    val artifactType = BluePrintTypes.artifactTypeMappingResource()
    if (this.artifactTypes == null) this.artifactTypes = hashMapOf()
    this.artifactTypes!![artifactType.id!!] = artifactType
}

fun BluePrintTypes.artifactTypeMappingResource(): ArtifactType {
    return artifactType(
        id = BluePrintConstants.MODEL_TYPE_ARTIFACT_MAPPING_RESOURCE,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
        description = "Mapping Resource Artifact"
    ) {
        fileExt("json")
    }
}

@Deprecated("CDS won't support", replaceWith = ReplaceWith("artifactTypeScriptKotlin"))
fun BluePrintTypes.artifactTypeScriptJython(): ArtifactType {
    return artifactType(
        id = BluePrintConstants.MODEL_TYPE_ARTIFACT_SCRIPT_JYTHON,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
        description = "Jython Script Artifact"
    ) {
        fileExt("py")
    }
}

fun ServiceTemplateBuilder.artifactTypeScriptKotlin() {
    val artifactType = BluePrintTypes.artifactTypeScriptKotlin()
    if (this.artifactTypes == null) this.artifactTypes = hashMapOf()
    this.artifactTypes!![artifactType.id!!] = artifactType
}

fun BluePrintTypes.artifactTypeScriptKotlin(): ArtifactType {
    return artifactType(
        id = BluePrintConstants.MODEL_TYPE_ARTIFACT_SCRIPT_KOTLIN,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
        description = "Kotlin Script Artifact"
    ) {
        fileExt("kts")
    }
}

fun ServiceTemplateBuilder.artifactTypeK8sProfileFolder() {
    val artifactType = BluePrintTypes.artifactTypeK8sProfileFolder()
    if (this.artifactTypes == null) this.artifactTypes = hashMapOf()
    this.artifactTypes!![artifactType.id!!] = artifactType
}

fun BluePrintTypes.artifactTypeK8sProfileFolder(): ArtifactType {
    return artifactType(
            id = BluePrintConstants.MODEL_TYPE_ARTIFACT_K8S_PROFILE,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
            description = "K8s Profile Folder Artifact"
    ) {
    }
}

@Deprecated("CDS won't support, use implerative workflow definitions.")
fun BluePrintTypes.artifactTypeDirectedGraph(): ArtifactType {
    return artifactType(
        id = BluePrintConstants.MODEL_TYPE_ARTIFACT_DIRECTED_GRAPH,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
        description = "Directed Graph Artifact"
    ) {
        fileExt("xml", "json")
    }
}

fun ServiceTemplateBuilder.artifactTypeComponentJar() {
    val artifactType = BluePrintTypes.artifactTypeComponentJar()
    if (this.artifactTypes == null) this.artifactTypes = hashMapOf()
    this.artifactTypes!![artifactType.id!!] = artifactType
}

fun BluePrintTypes.artifactTypeComponentJar(): ArtifactType {
    return artifactType(
        id = BluePrintConstants.MODEL_TYPE_ARTIFACT_COMPONENT_JAR,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
        description = "Component Artifact"
    ) {
        fileExt("jar")
    }
}

/** Relationship Types */

fun ServiceTemplateBuilder.relationshipTypeConnectsTo() {
    val relationshipType = BluePrintTypes.relationshipTypeConnectsTo()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BluePrintTypes.relationshipTypeConnectsTo(): RelationshipType {
    return relationshipType(
        id = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_ROOT,
        description = "Relationship connects to"
    ) {
        validTargetTypes(arrayListOf(BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT))
    }
}

fun ServiceTemplateBuilder.relationshipTypeDependsOn() {
    val relationshipType = BluePrintTypes.relationshipTypeDependsOn()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BluePrintTypes.relationshipTypeDependsOn(): RelationshipType {
    return relationshipType(
        id = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_DEPENDS_ON,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_ROOT,
        description = "Relationship depends on"
    ) {
    }
}

fun ServiceTemplateBuilder.relationshipTypeHostedOn() {
    val relationshipType = BluePrintTypes.relationshipTypeHostedOn()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BluePrintTypes.relationshipTypeHostedOn(): RelationshipType {
    return relationshipType(
        id = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_HOSTED_ON,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_ROOT,
        description = "Relationship hosted on"
    ) {
    }
}

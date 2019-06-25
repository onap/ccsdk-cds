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
import org.onap.ccsdk.cds.controllerblueprints.core.data.*
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType

// CDS DSLs
fun blueprint(name: String, version: String, author: String, tags: String,
              block: DSLBluePrintBuilder.() -> Unit): DSLBluePrint {
    return DSLBluePrintBuilder(name, version, author, tags).apply(block).build()
}

// TOSCA DSLs
fun serviceTemplate(name: String, version: String, author: String, tags: String,
                    block: ServiceTemplateBuilder.() -> Unit): ServiceTemplate {
    return ServiceTemplateBuilder(name, version, author, tags).apply(block).build()
}

fun workflow(id: String, description: String, block: WorkflowBuilder.() -> Unit): Workflow {
    return WorkflowBuilder(id, description).apply(block).build()
}

fun nodeTemplate(id: String, type: String, description: String,
                 block: NodeTemplateBuilder.() -> Unit): NodeTemplate {
    return NodeTemplateBuilder(id, type, description).apply(block).build()
}

fun nodeType(id: String, version: String, derivedFrom: String, description: String,
             block: NodeTypeBuilder.() -> Unit): NodeType {
    return NodeTypeBuilder(id, version, derivedFrom, description).apply(block).build()
}

fun dataType(id: String, version: String, derivedFrom: String, description: String,
             block: DataTypeBuilder.() -> Unit): DataType {
    return DataTypeBuilder(id, version, derivedFrom, description).apply(block).build()
}

fun artifactType(id: String, version: String, derivedFrom: String, description: String,
                 block: ArtifactTypeBuilder.() -> Unit): ArtifactType {
    return ArtifactTypeBuilder(id, version, derivedFrom, description).apply(block).build()
}

fun relationshipType(id: String, version: String, derivedFrom: String, description: String,
                     block: RelationshipTypeBuilder.() -> Unit): RelationshipType {
    return RelationshipTypeBuilder(id, version, derivedFrom, description).apply(block).build()
}

// Input Function

fun getInput(inputKey: String): JsonNode {
    return """{"get_input": "$inputKey"}""".jsonAsJsonType()
}

fun getAttribute(attributeId: String): JsonNode {
    return """{"get_attribute": ["SELF", "$attributeId"]}""".jsonAsJsonType()
}

fun getAttribute(attributeId: String, jsonPath: String): JsonNode {
    return """{"get_attribute": ["SELF", "$attributeId", "$jsonPath"]}""".jsonAsJsonType()
}

fun getNodeTemplateAttribute(nodeTemplateId: String, attributeId: String): JsonNode {
    return """{"get_attribute": ["$nodeTemplateId", "$attributeId"]}""".jsonAsJsonType()
}

fun getNodeTemplateAttribute(nodeTemplateId: String, attributeId: String, jsonPath: String): JsonNode {
    return """{"get_attribute": ["$nodeTemplateId", "$attributeId", "$jsonPath]}""".jsonAsJsonType()
}

// Property Function

fun getProperty(propertyId: String): JsonNode {
    return """{"get_property": ["SELF", "$propertyId"]}""".jsonAsJsonType()
}

fun getProperty(propertyId: String, jsonPath: String): JsonNode {
    return """{"get_property": ["SELF", "$propertyId", "$jsonPath"]}""".jsonAsJsonType()
}

fun getNodeTemplateProperty(nodeTemplateName: String, propertyId: String): JsonNode {
    return """{"get_property": ["$nodeTemplateName", "$propertyId"]}""".jsonAsJsonType()
}

fun getNodeTemplateProperty(nodeTemplateName: String, propertyId: String, jsonPath: String): JsonNode {
    return """{"get_property": ["$nodeTemplateName", "$propertyId", "$jsonPath]}""".jsonAsJsonType()
}

// Artifact Function

fun getArtifact(artifactId: String): JsonNode {
    return """{"get_artifact": ["SELF", "$artifactId"]}""".jsonAsJsonType()
}

fun getNodeTemplateArtifact(nodeTemplateName: String, artifactId: String): JsonNode {
    return """{"get_artifact": ["$nodeTemplateName", "$artifactId"]}""".jsonAsJsonType()
}


/** Blueprint Type Extensions */

fun BluePrintTypes.nodeTypeComponent(): NodeType {
    return nodeType(id = BluePrintConstants.MODEL_TYPE_NODE_COMPONENT,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_NODES_ROOT,
            description = "This is default Component Node") {
    }
}

fun BluePrintTypes.nodeTypeWorkflow(): NodeType {
    return nodeType(id = BluePrintConstants.MODEL_TYPE_NODE_WORKFLOW,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_NODES_ROOT,
            description = "This is default Workflow Node") {
    }
}

fun BluePrintTypes.nodeTypeVnf(): NodeType {
    return nodeType(id = BluePrintConstants.MODEL_TYPE_NODE_VNF,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_NODES_ROOT,
            description = "This is default VNF Node") {
    }
}

fun BluePrintTypes.nodeTypeResourceSource(): NodeType {
    return nodeType(id = BluePrintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_NODES_ROOT,
            description = "This is default Resource Source Node") {
    }
}

/** Artifacts */

fun BluePrintTypes.artifactTypeTemplateVelocity(): ArtifactType {
    return artifactType(id = BluePrintConstants.MODEL_TYPE_ARTIFACT_TEMPLATE_VELOCITY,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
            description = "Velocity Artifact") {
        fileExt("vtl")
    }
}

fun BluePrintTypes.artifactTypeTempleJinja(): ArtifactType {
    return artifactType(id = BluePrintConstants.MODEL_TYPE_ARTIFACT_TEMPLATE_JINJA,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
            description = "Jinja Artifact") {
        fileExt("jinja")
    }
}

fun BluePrintTypes.artifactTypeMappingResource(): ArtifactType {
    return artifactType(id = BluePrintConstants.MODEL_TYPE_ARTIFACT_MAPPING_RESOURCE,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
            description = "Mapping Resource Artifact") {
        fileExt("json")
    }
}

fun BluePrintTypes.artifactTypeScriptJython(): ArtifactType {
    return artifactType(id = BluePrintConstants.MODEL_TYPE_ARTIFACT_SCRIPT_JYTHON,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
            description = "Jython Script Artifact") {
        fileExt("py")
    }
}

fun BluePrintTypes.artifactTypeScriptKotlin(): ArtifactType {
    return artifactType(id = BluePrintConstants.MODEL_TYPE_ARTIFACT_SCRIPT_KOTLIN,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
            description = "Kotlin Script Artifact") {
        fileExt("kts")
    }
}

fun BluePrintTypes.artifactTypeDirectedGraph(): ArtifactType {
    return artifactType(id = BluePrintConstants.MODEL_TYPE_ARTIFACT_DIRECTED_GRAPH,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
            description = "Directed Graph Artifact") {
        fileExt("xml", "json")
    }
}

fun BluePrintTypes.artifactTypeComponentJar(): ArtifactType {
    return artifactType(id = BluePrintConstants.MODEL_TYPE_ARTIFACT_COMPONENT_JAR,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION,
            description = "Component Artifact") {
        fileExt("jar")
    }
}

/** Relationship Types */

fun BluePrintTypes.relationshipTypeConnectsTo(): RelationshipType {
    return relationshipType(id = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_ROOT,
            description = "Relationship connects to") {
    }
}

fun BluePrintTypes.relationshipTypeDependsOn(): RelationshipType {
    return relationshipType(id = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_DEPENDS_ON,
            version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_ROOT,
            description = "Relationship depends on") {
    }
}

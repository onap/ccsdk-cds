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
import org.onap.ccsdk.cds.controllerblueprints.core.asBlueprintsDataTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.asPropertyDefinitionMap
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.ImportDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.PolicyType
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.TopologyTemplate
import kotlin.reflect.KClass

class ServiceTemplateBuilder(
    private val name: String,
    private val version: String,
    private val author: String,
    private val tags: String
) {

    private var serviceTemplate = ServiceTemplate()
    private var topologyTemplate: TopologyTemplate? = null
    private var metadata: MutableMap<String, String> = hashMapOf()
    private var dslDefinitions: MutableMap<String, JsonNode>? = null
    private var imports: MutableList<ImportDefinition> = mutableListOf()
    var nodeTypes: MutableMap<String, NodeType>? = null
    var artifactTypes: MutableMap<String, ArtifactType>? = null
    var dataTypes: MutableMap<String, DataType>? = null
    var relationshipTypes: MutableMap<String, RelationshipType>? = null
    var policyTypes: MutableMap<String, PolicyType>? = null

    private fun initMetaData() {
        metadata[BlueprintConstants.METADATA_TEMPLATE_NAME] = name
        metadata[BlueprintConstants.METADATA_TEMPLATE_VERSION] = version
        metadata[BlueprintConstants.METADATA_TEMPLATE_AUTHOR] = author
        metadata[BlueprintConstants.METADATA_TEMPLATE_TAGS] = tags
    }

    fun metadata(id: String, value: String) {
        metadata[id] = value
    }

    fun import(file: String) {
        val importDefinition = ImportDefinition().apply {
            this.file = file
        }
        imports.add(importDefinition)
    }

    fun dsl(id: String, kclass: KClass<*>) {
        dsl(id, kclass.asPropertyDefinitionMap().asJsonNode())
    }

    fun dataType(dataType: KClass<*>) {
        dataType(dataType.asBlueprintsDataTypes())
    }

    fun dsl(id: String, content: Any) {
        dsl(id, content.asJsonType())
    }

    fun dsl(id: String, json: JsonNode) {
        if (dslDefinitions == null) dslDefinitions = hashMapOf()
        dslDefinitions!![id] = json
    }

    fun dataTypes(dataTypes: MutableMap<String, DataType>) {
        if (this.dataTypes == null) this.dataTypes = hashMapOf()

        this.dataTypes!!.putAll(dataTypes)
    }

    fun artifactTypes(artifactTypes: MutableMap<String, ArtifactType>) {
        if (this.artifactTypes == null) this.artifactTypes = hashMapOf()
        this.artifactTypes!!.putAll(artifactTypes)
    }

    fun relationshipTypes(relationshipTypes: MutableMap<String, RelationshipType>) {
        if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
        this.relationshipTypes!!.putAll(relationshipTypes)
    }

    fun policyTypes(policyTypes: MutableMap<String, PolicyType>) {
        if (this.policyTypes == null) this.policyTypes = hashMapOf()
        this.policyTypes!!.putAll(policyTypes)
    }

    fun nodeType(nodeTypes: MutableMap<String, NodeType>) {
        if (this.nodeTypes == null) this.nodeTypes = hashMapOf()
        this.nodeTypes!!.putAll(nodeTypes)
    }

    fun dataType(dataType: DataType) {
        if (dataTypes == null) dataTypes = hashMapOf()
        dataTypes!![dataType.id!!] = dataType
    }

    fun artifactType(artifactType: ArtifactType) {
        if (artifactTypes == null) artifactTypes = hashMapOf()
        artifactTypes!![artifactType.id!!] = artifactType
    }

    fun relationshipType(relationshipType: RelationshipType) {
        if (relationshipTypes == null) relationshipTypes = hashMapOf()
        relationshipTypes!![relationshipType.id!!] = relationshipType
    }

    fun relationshipTypes(relationshipTypes: List<RelationshipType>) {
        if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
        relationshipTypes.forEach { relationshipType ->
            this.relationshipTypes!![relationshipType.id!!] = relationshipType
        }
    }

    fun policyType(policyType: PolicyType) {
        if (policyTypes == null) policyTypes = hashMapOf()
        policyTypes!![policyType.id!!] = policyType
    }

    fun nodeType(nodeType: NodeType) {
        if (nodeTypes == null) nodeTypes = hashMapOf()
        nodeTypes!![nodeType.id!!] = nodeType
    }

    fun nodeTypes(nodeTypes: List<NodeType>) {
        if (this.nodeTypes == null) this.nodeTypes = hashMapOf()
        nodeTypes.forEach { nodeType ->
            this.nodeTypes!![nodeType.id!!] = nodeType
        }
    }

    fun dataType(
        id: String,
        version: String,
        derivedFrom: String,
        description: String,
        block: DataTypeBuilder.() -> Unit
    ) {
        if (dataTypes == null) dataTypes = hashMapOf()
        dataTypes!![id] = DataTypeBuilder(id, version, derivedFrom, description).apply(block).build()
    }

    fun artifactType(
        id: String,
        version: String,
        derivedFrom: String,
        description: String,
        block: ArtifactTypeBuilder.() -> Unit
    ) {
        if (artifactTypes == null) artifactTypes = hashMapOf()
        artifactTypes!![id] = ArtifactTypeBuilder(id, version, derivedFrom, description).apply(block).build()
    }

    fun relationshipType(
        id: String,
        version: String,
        derivedFrom: String,
        description: String,
        block: RelationshipTypeBuilder.() -> Unit
    ) {
        if (relationshipTypes == null) relationshipTypes = hashMapOf()
        relationshipTypes!![id] = RelationshipTypeBuilder(id, version, derivedFrom, description).apply(block).build()
    }

    fun policyType(
        id: String,
        version: String,
        derivedFrom: String,
        description: String,
        block: PolicyTypeBuilder.() -> Unit
    ) {
        if (policyTypes == null) policyTypes = hashMapOf()
        policyTypes!![id] = PolicyTypeBuilder(id, version, derivedFrom, description).apply(block).build()
    }

    fun nodeType(
        id: String,
        version: String,
        derivedFrom: String,
        description: String,
        block: NodeTypeBuilder.() -> Unit
    ) {
        if (nodeTypes == null) nodeTypes = hashMapOf()
        nodeTypes!![id] = NodeTypeBuilder(id, version, derivedFrom, description).apply(block).build()
    }

    fun topologyTemplate(block: TopologyTemplateBuilder.() -> Unit) {
        topologyTemplate = TopologyTemplateBuilder().apply(block).build()
    }

    fun build(): ServiceTemplate {
        initMetaData()
        serviceTemplate.metadata = metadata
        serviceTemplate.imports = imports
        serviceTemplate.dslDefinitions = dslDefinitions
        serviceTemplate.nodeTypes = nodeTypes
        serviceTemplate.artifactTypes = artifactTypes
        serviceTemplate.dataTypes = dataTypes
        serviceTemplate.relationshipTypes = relationshipTypes
        serviceTemplate.policyTypes = policyTypes
        serviceTemplate.topologyTemplate = topologyTemplate
        return serviceTemplate
    }
}

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
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.*

class ServiceTemplateBuilder(private val name: String,
                             private val version: String,
                             private val author: String,
                             private val tags: String) {
    private var serviceTemplate = ServiceTemplate()
    private lateinit var topologyTemplate: TopologyTemplate
    private var metadata: MutableMap<String, String> = hashMapOf()
    private var dslDefinitions: MutableMap<String, JsonNode>? = null
    private var imports: MutableList<ImportDefinition>? = null
    private var nodeTypes: MutableMap<String, NodeType>? = null
    private var artifactTypes: MutableMap<String, ArtifactType>? = null
    private var dataTypes: MutableMap<String, DataType>? = null
    private var relationshipTypes: MutableMap<String, RelationshipType>? = null

    private fun initMetaData() {
        metadata[BluePrintConstants.METADATA_TEMPLATE_NAME] = name
        metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION] = version
        metadata[BluePrintConstants.METADATA_TEMPLATE_AUTHOR] = author
        metadata[BluePrintConstants.METADATA_TEMPLATE_TAGS] = tags
    }

    fun metadata(id: String, value: String) {
        metadata[id] = value
    }

    fun dsl(id: String, json: String) {
       dsl(id, json.asJsonType())
    }

    fun dsl(id: String, json: JsonNode) {
        if (dslDefinitions == null)
            dslDefinitions = hashMapOf()
        dslDefinitions!![id] = json.asJsonType()
    }

    // TODO("Imports")

    fun dataType(id: String, version: String, description: String, block: DataTypeBuilder.() -> Unit) {
        if (dataTypes == null)
            dataTypes = hashMapOf()
        dataTypes!![id] = DataTypeBuilder(id, version, description).apply(block).build()
    }

    fun artifactType(id: String, version: String, description: String, block: ArtifactTypeBuilder.() -> Unit) {
        if (artifactTypes == null)
            artifactTypes = hashMapOf()
        artifactTypes!![id] = ArtifactTypeBuilder(id, version, description).apply(block).build()
    }

    fun relationshipType(id: String, version: String, description: String, block: RelationshipTypeBuilder.() -> Unit) {
        if (relationshipTypes == null)
            relationshipTypes = hashMapOf()
        relationshipTypes!![id] = RelationshipTypeBuilder(id, version, description).apply(block).build()
    }

    fun nodeType(id: String, version: String, description: String, block: NodeTypeBuilder.() -> Unit) {
        if (nodeTypes == null)
            nodeTypes = hashMapOf()
        nodeTypes!![id] = NodeTypeBuilder(id, version, description).apply(block).build()
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
        serviceTemplate.topologyTemplate = topologyTemplate
        return serviceTemplate
    }
}

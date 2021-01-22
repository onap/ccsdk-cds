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

package org.onap.ccsdk.cds.controllerblueprints.core.service

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.AttributeDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.CapabilityDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.RequirementDefinition

/**
 *
 *
 * @author Brinda Santh
 */
internal class BlueprintChainedService {

    var bpc: BlueprintContext

    constructor(bpc: BlueprintContext) {
        this.bpc = bpc
    }

    fun nodeTypeChained(nodeTypeName: String): NodeType {

        val nodeType: NodeType = bpc.nodeTypeByName(nodeTypeName)
        val attributes = hashMapOf<String, AttributeDefinition>()
        val properties = hashMapOf<String, PropertyDefinition>()
        val requirements = hashMapOf<String, RequirementDefinition>()
        val capabilities = hashMapOf<String, CapabilityDefinition>()
        val interfaces = hashMapOf<String, InterfaceDefinition>()
        val artifacts = hashMapOf<String, ArtifactDefinition>()

        recNodeTypesChained(nodeTypeName).forEach { nodeType ->

            val subAttributes = bpc.nodeTypeByName(nodeType.id!!).attributes
            if (subAttributes != null) {
                attributes.putAll(subAttributes)
            }

            val subProperties = bpc.nodeTypeByName(nodeType.id!!).properties
            if (subProperties != null) {
                properties.putAll(subProperties)
            }

            val subRequirements = bpc.nodeTypeByName(nodeType.id!!).requirements
            if (subRequirements != null) {
                requirements.putAll(subRequirements)
            }
            val subCapabilities = bpc.nodeTypeByName(nodeType.id!!).capabilities
            if (subCapabilities != null) {
                capabilities.putAll(subCapabilities)
            }
            val subInterfaces = bpc.nodeTypeByName(nodeType.id!!).interfaces
            if (subInterfaces != null) {
                interfaces.putAll(subInterfaces)
            }

            val subArtifacts = bpc.nodeTypeByName(nodeType.id!!).artifacts
            if (subArtifacts != null) {
                artifacts.putAll(subArtifacts)
            }
        }
        nodeType.attributes = attributes
        nodeType.properties = properties
        nodeType.requirements = requirements
        nodeType.capabilities = capabilities
        nodeType.interfaces = interfaces
        nodeType.artifacts = artifacts
        return nodeType
    }

    fun nodeTypeChainedProperties(nodeTypeName: String): MutableMap<String, PropertyDefinition>? {
        val nodeType = bpc.nodeTypeByName(nodeTypeName)
        val properties = hashMapOf<String, PropertyDefinition>()

        recNodeTypesChained(nodeTypeName).forEach { nodeType ->
            val subProperties = bpc.nodeTypeByName(nodeType.id!!).properties
            if (subProperties != null) {
                properties.putAll(subProperties)
            }
        }
        return properties
    }

    private fun recNodeTypesChained(nodeTypeName: String, nodeTypes: MutableList<NodeType>? = arrayListOf()): MutableList<NodeType> {
        val nodeType: NodeType = bpc.nodeTypeByName(nodeTypeName)
        nodeType.id = nodeTypeName
        val derivedFrom: String = nodeType.derivedFrom
        if (!BlueprintTypes.rootNodeTypes().contains(derivedFrom)) {
            recNodeTypesChained(derivedFrom, nodeTypes)
        }
        nodeTypes!!.add(nodeType)
        return nodeTypes
    }

    private fun recDataTypesChained(dataTypeName: String, dataTypes: MutableList<DataType>? = arrayListOf()): MutableList<DataType> {
        val dataType: DataType = bpc.dataTypeByName(dataTypeName)!!
        dataType.id = dataTypeName
        val derivedFrom: String = dataType.derivedFrom
        if (!BlueprintTypes.rootDataTypes().contains(derivedFrom)) {
            recDataTypesChained(derivedFrom, dataTypes)
        }
        dataTypes!!.add(dataType)
        return dataTypes
    }
}

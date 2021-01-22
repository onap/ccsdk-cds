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
import com.fasterxml.jackson.databind.node.ArrayNode
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.asListOfString
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.AttributeDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.CapabilityDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.ConstraintClause
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.EntityType
import org.onap.ccsdk.cds.controllerblueprints.core.data.EntrySchema
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.PolicyType
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.data.RequirementDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType

open class EntityTypeBuilder(
    private val id: String,
    private val version: String,
    private val derivedFrom: String,
    private val description: String? = ""
) {

    var metadata: MutableMap<String, String>? = null
    var properties: MutableMap<String, PropertyDefinition>? = null
    var attributes: MutableMap<String, AttributeDefinition>? = null

    fun metadata(key: String, value: String) {
        if (metadata == null)
            metadata = hashMapOf()
        metadata!![key] = value
    }

    fun attribute(id: String, type: String, required: Boolean, description: String? = "") {
        if (attributes == null)
            attributes = hashMapOf()
        val attribute = AttributeDefinitionBuilder(id, type, required, description).build()
        attributes!![id] = attribute
    }

    fun attribute(
        id: String,
        type: String,
        required: Boolean,
        description: String? = "",
        block: AttributeDefinitionBuilder.() -> Unit
    ) {
        if (attributes == null)
            attributes = hashMapOf()
        val attribute = AttributeDefinitionBuilder(id, type, required, description).apply(block).build()
        attributes!![id] = attribute
    }

    fun property(id: String, type: String, required: Boolean, description: String? = "") {
        if (properties == null)
            properties = hashMapOf()
        val property = PropertyDefinitionBuilder(id, type, required, description).build()
        properties!![id] = property
    }

    fun property(
        id: String,
        type: String,
        required: Boolean,
        description: String? = "",
        block: PropertyDefinitionBuilder.() -> Unit
    ) {
        if (properties == null)
            properties = hashMapOf()
        val property = PropertyDefinitionBuilder(id, type, required, description).apply(block).build()
        properties!![id] = property
    }

    fun buildEntityType(entity: EntityType) {
        entity.id = id
        entity.description = description
        entity.version = version
        entity.derivedFrom = derivedFrom
        entity.metadata = metadata
        entity.properties = properties
        entity.attributes = attributes
    }
}

class NodeTypeBuilder(
    id: String,
    version: String,
    derivedFrom: String,
    description: String?
) : EntityTypeBuilder(id, version, derivedFrom, description) {

    private var nodeType = NodeType()
    private var capabilities: MutableMap<String, CapabilityDefinition>? = null
    private var requirements: MutableMap<String, RequirementDefinition>? = null
    private var interfaces: MutableMap<String, InterfaceDefinition>? = null
    private var artifacts: MutableMap<String, ArtifactDefinition>? = null

    fun capability(id: String, type: String, description: String, block: CapabilityDefinitionBuilder.() -> Unit) {
        if (capabilities == null)
            capabilities = hashMapOf()
        capabilities!![id] = CapabilityDefinitionBuilder(id, type, description).apply(block).build()
    }

    fun requirement(id: String, capability: String, node: String, relationship: String, description: String) {
        if (requirements == null)
            requirements = hashMapOf()
        requirements!![id] = RequirementDefinitionBuilder(id, capability, node, relationship, description).build()
    }

    fun requirement(
        id: String,
        capability: String,
        node: String,
        relationship: String,
        description: String,
        block: RequirementDefinitionBuilder.() -> Unit
    ) {
        if (requirements == null)
            requirements = hashMapOf()
        requirements!![id] = RequirementDefinitionBuilder(id, capability, node, relationship, description)
            .apply(block).build()
    }

    fun artifact(id: String, type: String, file: String) {
        if (artifacts == null)
            artifacts = hashMapOf()
        artifacts!![id] = ArtifactDefinitionBuilder(id, type, file).build()
    }

    private fun nodeInterface(id: String, block: InterfaceDefinitionBuilder.() -> Unit) {
        if (interfaces == null)
            interfaces = hashMapOf()
        interfaces!![id] = InterfaceDefinitionBuilder(id).apply(block).build()
    }

    fun operation(interfaceName: String, description: String?, block: OperationDefinitionBuilder.() -> Unit) {
        if (interfaces == null)
            interfaces = hashMapOf()

        val interfaceDefinition = InterfaceDefinition()
        val defaultOperationName = "process"
        interfaceDefinition.operations = hashMapOf()
        interfaceDefinition.operations!![defaultOperationName] =
            OperationDefinitionBuilder(defaultOperationName, description).apply(block).build()
        interfaces!![interfaceName] = interfaceDefinition
    }

    fun build(): NodeType {
        buildEntityType(nodeType)
        nodeType.capabilities = capabilities
        nodeType.requirements = requirements
        nodeType.interfaces = interfaces
        nodeType.artifacts = artifacts
        return nodeType
    }
}

class ArtifactTypeBuilder(
    id: String,
    version: String,
    derivedFrom: String,
    description: String?
) : EntityTypeBuilder(id, version, derivedFrom, description) {

    private var artifactType = ArtifactType()
    private var fileExt: MutableList<String>? = null

    fun fileExt(vararg fileExts: String) {
        if (fileExt == null)
            fileExt = arrayListOf()
        fileExts.forEach {
            fileExt!!.add(it)
        }
    }

    fun build(): ArtifactType {
        buildEntityType(artifactType)
        artifactType.fileExt = fileExt
        return artifactType
    }
}

class PolicyTypeBuilder(
    id: String,
    version: String,
    derivedFrom: String,
    description: String?
) : EntityTypeBuilder(id, version, derivedFrom, description) {

    private var policyType = PolicyType()

    fun targets(targetsStr: String) {
        val arrayNode = targetsStr.jsonAsJsonType() as ArrayNode
        targets(arrayNode.asListOfString())
    }

    fun targets(target: List<String>) {
        policyType.targets = target.toMutableList()
    }

    fun build(): PolicyType {
        buildEntityType(policyType)
        return policyType
    }
}

class RelationshipTypeBuilder(
    private val id: String,
    private val version: String,
    derivedFrom: String,
    private val description: String?
) :
    EntityTypeBuilder(id, version, derivedFrom, description) {

    private var relationshipType = RelationshipType()

    fun validTargetTypes(validTargetTypesStr: String) {
        val arrayNode = validTargetTypesStr.jsonAsJsonType() as ArrayNode
        validTargetTypes(arrayNode.asListOfString())
    }

    fun validTargetTypes(validTargetTypes: List<String>) {
        relationshipType.validTargetTypes = validTargetTypes.toMutableList()
    }

    fun build(): RelationshipType {
        buildEntityType(relationshipType)
        relationshipType.id = id
        relationshipType.version = version
        relationshipType.description = description
        return relationshipType
    }
}

class DataTypeBuilder(
    id: String,
    version: String,
    derivedFrom: String,
    description: String?
) : EntityTypeBuilder(id, version, derivedFrom, description) {

    private var dataType = DataType()

    fun constrain(block: ConstraintClauseBuilder.() -> Unit) {
        if (dataType.constraints == null) {
            dataType.constraints = mutableListOf()
        }
        val constraintClause = ConstraintClauseBuilder().apply(block).build()
        dataType.constraints!!.add(constraintClause)
    }

    fun build(): DataType {
        buildEntityType(dataType)
        return dataType
    }
}

class CapabilityDefinitionBuilder(private val id: String, private val type: String, private val description: String? = "") {

    private var capabilityDefinition = CapabilityDefinition()
    private val properties: MutableMap<String, PropertyDefinition> = hashMapOf()

    fun property(id: String, type: String? = BlueprintConstants.DATA_TYPE_STRING, required: Boolean? = false, description: String? = "") {
        val property = PropertyDefinitionBuilder(id, type, required, description).build()
        properties[id] = property
    }

    fun build(): CapabilityDefinition {
        capabilityDefinition.id = id
        capabilityDefinition.description = description
        capabilityDefinition.type = type
        capabilityDefinition.properties = properties
        return capabilityDefinition
    }
}

class RequirementDefinitionBuilder(
    private val id: String,
    private val capability: String,
    private val node: String,
    private val relationship: String,
    private val description: String? = ""
) {

    private var requirementDefinition = RequirementDefinition()

    fun build(): RequirementDefinition {
        requirementDefinition.id = id
        requirementDefinition.description = description
        requirementDefinition.capability = capability
        requirementDefinition.node = node
        requirementDefinition.relationship = relationship
        return requirementDefinition
    }
}

class InterfaceDefinitionBuilder(private val id: String) {

    private var interfaceDefinition: InterfaceDefinition = InterfaceDefinition()
    private var operations: MutableMap<String, OperationDefinition>? = null

    fun operation(id: String, description: String? = "", block: OperationDefinitionBuilder.() -> Unit) {
        if (operations == null)
            operations = hashMapOf()
        operations!![id] = OperationDefinitionBuilder(id, description).apply(block).build()
    }

    fun build(): InterfaceDefinition {
        interfaceDefinition.id = id
        interfaceDefinition.operations = operations
        return interfaceDefinition
    }
}

class OperationDefinitionBuilder(
    private val id: String,
    private val description: String? = ""
) {

    private var operationDefinition: OperationDefinition = OperationDefinition()

    fun inputs(block: PropertiesDefinitionBuilder.() -> Unit) {
        operationDefinition.inputs = PropertiesDefinitionBuilder().apply(block).build()
    }

    fun outputs(block: PropertiesDefinitionBuilder.() -> Unit) {
        operationDefinition.outputs = PropertiesDefinitionBuilder().apply(block).build()
    }

    fun build(): OperationDefinition {
        operationDefinition.id = id
        operationDefinition.description = description
        return operationDefinition
    }
}

class AttributeDefinitionBuilder(
    private val id: String,
    private val type: String? = BlueprintConstants.DATA_TYPE_STRING,
    private val required: Boolean? = false,
    private val description: String? = ""
) {

    private var attributeDefinition: AttributeDefinition = AttributeDefinition()

    fun entrySchema(entrySchemaType: String) {
        attributeDefinition.entrySchema = EntrySchemaBuilder(entrySchemaType).build()
    }

    fun entrySchema(entrySchemaType: String, block: EntrySchemaBuilder.() -> Unit) {
        attributeDefinition.entrySchema = EntrySchemaBuilder(entrySchemaType).apply(block).build()
    }

    fun constrain(block: ConstraintClauseBuilder.() -> Unit) {
        if (attributeDefinition.constraints == null) {
            attributeDefinition.constraints = mutableListOf()
        }
        val constraintClause = ConstraintClauseBuilder().apply(block).build()
        attributeDefinition.constraints!!.add(constraintClause)
    }

    fun defaultValue(defaultValue: Any) {
        defaultValue(defaultValue.asJsonType())
    }

    fun defaultValue(defaultValue: JsonNode) {
        attributeDefinition.defaultValue = defaultValue
    }

    fun build(): AttributeDefinition {
        attributeDefinition.id = id
        attributeDefinition.type = type!!
        attributeDefinition.required = required
        attributeDefinition.description = description
        return attributeDefinition
    }
}

class PropertiesDefinitionBuilder {

    private val properties: MutableMap<String, PropertyDefinition> = hashMapOf()

    fun property(id: String, property: PropertyDefinition) {
        properties[id] = property
    }

    fun property(id: String, type: String?, required: Boolean?, description: String? = "") {
        val property = PropertyDefinitionBuilder(id, type, required, description).build()
        properties[id] = property
    }

    fun property(
        id: String,
        type: String?,
        required: Boolean?,
        description: String? = "",
        block: PropertyDefinitionBuilder.() -> Unit
    ) {
        val property = PropertyDefinitionBuilder(id, type, required, description).apply(block).build()
        properties[id] = property
    }

    fun build(): MutableMap<String, PropertyDefinition> {
        return properties
    }
}

class PropertyDefinitionBuilder(
    private val id: String,
    private val type: String? = BlueprintConstants.DATA_TYPE_STRING,
    private val required: Boolean? = false,
    private val description: String? = ""
) {

    private var propertyDefinition: PropertyDefinition = PropertyDefinition()

    fun entrySchema(entrySchemaType: String) {
        propertyDefinition.entrySchema = EntrySchemaBuilder(entrySchemaType).build()
    }

    fun entrySchema(entrySchemaType: String, block: EntrySchemaBuilder.() -> Unit) {
        propertyDefinition.entrySchema = EntrySchemaBuilder(entrySchemaType).apply(block).build()
    }

    fun constrain(block: ConstraintClauseBuilder.() -> Unit) {
        if (propertyDefinition.constraints == null) {
            propertyDefinition.constraints = mutableListOf()
        }
        val constraintClause = ConstraintClauseBuilder().apply(block).build()
        propertyDefinition.constraints!!.add(constraintClause)
    }

    fun defaultValue(defaultValue: Any) {
        defaultValue(defaultValue.asJsonType())
    }

    fun defaultValue(defaultValue: JsonNode) {
        propertyDefinition.defaultValue = defaultValue
    }

    fun metadata(name: String, value: String) {
        if (propertyDefinition.metadata == null) {
            propertyDefinition.metadata = hashMapOf()
        }
        propertyDefinition.metadata!![name] = value
    }

    fun value(value: Any) {
        value(value.asJsonType())
    }

    fun value(value: JsonNode) {
        propertyDefinition.value = value
    }

    fun build(): PropertyDefinition {
        propertyDefinition.id = id
        propertyDefinition.type = type!!
        propertyDefinition.required = required
        propertyDefinition.description = description
        return propertyDefinition
    }
}

class ConstraintsClauseBuilder {

    val constraints: MutableList<ConstraintClause> = mutableListOf()

    fun constrain(block: ConstraintClauseBuilder.() -> Unit) {
        val constraintClause = ConstraintClauseBuilder().apply(block).build()
        constraints.add(constraintClause)
    }

    fun build(): MutableList<ConstraintClause> {
        return constraints
    }
}

class ConstraintClauseBuilder {

    private val constraintClause = ConstraintClause()

    fun equal(equal: Any) = equal(equal.asJsonType())

    fun equal(equal: JsonNode) {
        constraintClause.equal = equal
    }

    fun greaterOrEqual(greaterOrEqual: Any) {
        constraintClause.greaterOrEqual = greaterOrEqual.asJsonPrimitive()
    }

    fun greaterThan(greaterThan: Any) {
        constraintClause.greaterThan = greaterThan.asJsonPrimitive()
    }

    fun lessOrEqual(lessOrEqual: Any) {
        constraintClause.lessOrEqual = lessOrEqual.asJsonPrimitive()
    }

    fun lessThan(lessThan: Any) {
        constraintClause.lessThan = lessThan.asJsonPrimitive()
    }

    fun inRange(inRangeStr: String) = inRange(inRangeStr.jsonAsJsonType() as ArrayNode)

    fun inRange(inRangeNode: ArrayNode) {
        constraintClause.inRange = inRangeNode.toMutableList()
    }

    fun validValues(validValuesStr: String) = validValues(validValuesStr.jsonAsJsonType() as ArrayNode)

    fun validValues(validValuesNode: ArrayNode) = validValues(validValuesNode.toMutableList())

    fun validValues(validValues: List<JsonNode>) {
        constraintClause.validValues = validValues.toMutableList()
    }

    fun length(length: Any) {
        constraintClause.length = length.asJsonPrimitive()
    }

    fun minLength(minLength: Any) {
        constraintClause.minLength = minLength.asJsonPrimitive()
    }

    fun maxLength(maxLength: Any) {
        constraintClause.maxLength = maxLength.asJsonPrimitive()
    }

    fun pattern(pattern: String) {
        constraintClause.pattern = pattern
    }

    fun schema(schema: String) {
        constraintClause.schema = schema
    }

    fun build(): ConstraintClause {
        return constraintClause
    }
}

class EntrySchemaBuilder(private val type: String) {

    private var entrySchema: EntrySchema = EntrySchema()

    fun constrain(block: ConstraintClauseBuilder.() -> Unit) {
        if (entrySchema.constraints == null) {
            entrySchema.constraints = mutableListOf()
        }
        val constraintClause = ConstraintClauseBuilder().apply(block).build()
        entrySchema.constraints!!.add(constraintClause)
    }

    fun build(): EntrySchema {
        entrySchema.type = type
        return entrySchema
    }
}

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

/**
 * @author Brinda Santh
 */
class DSLBluePrintBuilder(private val name: String,
                          private val version: String,
                          private val author: String,
                          private val tags: String) {

    private var dslBluePrint = DSLBluePrint()
    private var metadata: MutableMap<String, String> = hashMapOf()
    var properties: MutableMap<String, PropertyDefinition>? = null
    var data: MutableMap<String, DataType> = hashMapOf()
    var artifacts: MutableMap<String, ArtifactDefinition> = hashMapOf()
    var components: MutableMap<String, DSLComponent> = hashMapOf()
    var workflows: MutableMap<String, DSLWorkflow> = hashMapOf()

    private fun initMetaData() {
        metadata[BluePrintConstants.METADATA_TEMPLATE_NAME] = name
        metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION] = version
        metadata[BluePrintConstants.METADATA_TEMPLATE_AUTHOR] = author
        metadata[BluePrintConstants.METADATA_TEMPLATE_TAGS] = tags
    }

    fun metadata(id: String, value: String) {
        metadata[id] = value
    }

    fun dataType(id: String, version: String, derivedFrom: String, description: String,
                 block: DataTypeBuilder.() -> Unit) {
        data[id] = DataTypeBuilder(id, version, derivedFrom, description).apply(block).build()
    }


    fun component(id: String, type: String, version: String, description: String, block: DSLComponentBuilder.() -> Unit) {
        components[id] = DSLComponentBuilder(id, type, version, description).apply(block).build()
    }

    fun workflow(id: String, description: String, block: DSLWorkflowBuilder.() -> Unit) {
        workflows[id] = DSLWorkflowBuilder(id, description).apply(block).build()
    }

    fun build(): DSLBluePrint {
        initMetaData()
        dslBluePrint.metadata = metadata

        dslBluePrint.data = data
        dslBluePrint.components = components
        dslBluePrint.workflows = workflows
        return dslBluePrint
    }
}

class DSLComponentBuilder(private val id: String, private val type: String,
                          private val version: String, private val description: String) {
    private val dslComponent = DSLComponent()
    var properties: MutableMap<String, PropertyDefinition>? = null
    var attributes: MutableMap<String, AttributeDefinition>? = null

    // For already registered components
    private var assignProperties: MutableMap<String, JsonNode>? = null

    var artifacts: MutableMap<String, ArtifactDefinition>? = null
    var implementation: Implementation? = null
    var inputs: MutableMap<String, PropertyDefinition>? = null
    var outputs: MutableMap<String, PropertyDefinition>? = null

    // For already registered components
    private var assignInputs: MutableMap<String, JsonNode>? = null
    private var assignOutputs: MutableMap<String, JsonNode>? = null

    fun attribute(id: String, type: String, required: Boolean, expression: Any, description: String? = "") {
        if (attributes == null)
            attributes = hashMapOf()
        val attribute = DSLAttributeDefinitionBuilder(id, type, required, expression.asJsonType(), description).build()
        attributes!![id] = attribute
    }

    fun attribute(id: String, type: String, required: Boolean, expression: Any, description: String? = "",
                  block: DSLAttributeDefinitionBuilder.() -> Unit) {
        if (attributes == null)
            attributes = hashMapOf()
        val attribute = DSLAttributeDefinitionBuilder(id, type, required, expression.asJsonType(), description)
                .apply(block).build()
        attributes!![id] = attribute
    }

    fun property(id: String, type: String, required: Boolean, expression: Any, description: String? = "") {
        if (properties == null)
            properties = hashMapOf()
        val property = DSLPropertyDefinitionBuilder(id, type, required, expression.asJsonType(), description).build()
        properties!![id] = property
    }

    fun property(id: String, type: String, required: Boolean, expression: Any, description: String? = "",
                 block: DSLPropertyDefinitionBuilder.() -> Unit) {
        if (properties == null)
            properties = hashMapOf()
        val property = DSLPropertyDefinitionBuilder(id, type, required, expression.asJsonType(), description)
                .apply(block).build()
        properties!![id] = property
    }

    fun assignProperty(id: String, expression: Any) {
        if (assignProperties == null)
            assignProperties = hashMapOf()
        assignProperties!![id] = expression.asJsonType()
    }

    fun implementation(timeout: Int, operationHost: String? = BluePrintConstants.PROPERTY_SELF) {
        implementation = Implementation().apply {
            this.operationHost = operationHost!!
            this.timeout = timeout
        }
    }

    fun artifacts(id: String, type: String, file: String) {
        if (artifacts == null)
            artifacts = hashMapOf()
        artifacts!![id] = ArtifactDefinitionBuilder(id, type, file).build()
    }

    fun artifacts(id: String, type: String, file: String, block: ArtifactDefinitionBuilder.() -> Unit) {
        if (artifacts == null)
            artifacts = hashMapOf()
        artifacts!![id] = ArtifactDefinitionBuilder(id, type, file).apply(block).build()
    }


    fun input(id: String, type: String, required: Boolean, expression: Any, description: String? = "") {
        if (inputs == null)
            inputs = hashMapOf()
        val property = DSLPropertyDefinitionBuilder(id, type, required, expression.asJsonType(), description)
        inputs!![id] = property.build()
    }

    fun input(id: String, type: String, required: Boolean, expression: Any, description: String? = "",
              block: DSLPropertyDefinitionBuilder.() -> Unit) {
        if (inputs == null)
            inputs = hashMapOf()
        val property = DSLPropertyDefinitionBuilder(id, type, required, expression.asJsonType(), description)
                .apply(block).build()
        inputs!![id] = property
    }

    fun assignInput(id: String, expression: Any) {
        if (assignInputs == null)
            assignInputs = hashMapOf()
        assignInputs!![id] = expression.asJsonType()
    }

    fun output(id: String, type: String, required: Boolean, expression: Any, description: String? = "") {
        if (outputs == null)
            outputs = hashMapOf()
        val property = DSLPropertyDefinitionBuilder(id, type, required, expression.asJsonType(), description)
        outputs!![id] = property.build()
    }

    fun output(id: String, type: String, required: Boolean, expression: Any, description: String? = "",
               block: DSLPropertyDefinitionBuilder.() -> Unit) {
        if (outputs == null)
            outputs = hashMapOf()
        val property = DSLPropertyDefinitionBuilder(id, type, required, expression.asJsonType(), description)
                .apply(block).build()
        outputs!![id] = property
    }

    fun assignOutput(id: String, expression: Any) {
        if (assignOutputs == null)
            assignOutputs = hashMapOf()
        assignOutputs!![id] = expression.asJsonType()
    }

    fun build(): DSLComponent {
        dslComponent.id = id
        dslComponent.type = type
        dslComponent.version = version
        dslComponent.description = description
        dslComponent.attributes = attributes
        dslComponent.properties = properties
        dslComponent.assignProperties = assignProperties
        dslComponent.implementation = implementation
        dslComponent.artifacts = artifacts
        dslComponent.inputs = inputs
        dslComponent.outputs = outputs
        dslComponent.assignInputs = assignInputs
        dslComponent.assignOutputs = assignOutputs
        dslComponent.outputs = outputs

        return dslComponent
    }
}

class DSLWorkflowBuilder(private val actionName: String, private val description: String) {
    private val dslWorkflow = DSLWorkflow()
    private var steps: MutableMap<String, Step>? = null
    private var inputs: MutableMap<String, PropertyDefinition>? = null
    private var outputs: MutableMap<String, PropertyDefinition>? = null

    fun input(id: String, type: String, required: Boolean, description: String? = "") {
        if (inputs == null)
            inputs = hashMapOf()
        val property = PropertyDefinitionBuilder(id, type, required, description)
        inputs!![id] = property.build()
    }

    fun input(id: String, type: String, required: Boolean, description: String, defaultValue: Any?,
              block: PropertyDefinitionBuilder.() -> Unit) {
        if (inputs == null)
            inputs = hashMapOf()
        val property = PropertyDefinitionBuilder(id, type, required, description).apply(block).build()
        if (defaultValue != null)
            property.defaultValue = defaultValue.asJsonType()
        inputs!![id] = property
    }

    fun output(id: String, type: String, required: Boolean, expression: Any, description: String? = "") {
        if (outputs == null)
            outputs = hashMapOf()
        val property = DSLPropertyDefinitionBuilder(id, type, required, expression.asJsonType(), description)
        outputs!![id] = property.build()
    }

    fun output(id: String, type: String, required: Boolean, expression: Any, description: String? = "",
               block: DSLPropertyDefinitionBuilder.() -> Unit) {
        if (outputs == null)
            outputs = hashMapOf()
        val property = DSLPropertyDefinitionBuilder(id, type, required, expression.asJsonType(), description)
                .apply(block).build()
        outputs!![id] = property
    }

    fun step(id: String, target: String, description: String) {
        if (steps == null)
            steps = hashMapOf()
        steps!![id] = StepBuilder(id, target, description).build()
    }

    fun step(id: String, target: String, description: String, block: StepBuilder.() -> Unit) {
        if (steps == null)
            steps = hashMapOf()
        steps!![id] = StepBuilder(id, target, description).apply(block).build()
    }

    fun build(): DSLWorkflow {
        dslWorkflow.actionName = actionName
        dslWorkflow.description = description
        dslWorkflow.inputs = inputs
        dslWorkflow.outputs = outputs
        dslWorkflow.steps = steps!!
        return dslWorkflow
    }
}

class DSLAttributeDefinitionBuilder(private val id: String,
                                    private val type: String? = BluePrintConstants.DATA_TYPE_STRING,
                                    private val required: Boolean? = false,
                                    private val expression: JsonNode,
                                    private val description: String? = "") {

    private var attributeDefinition = AttributeDefinition()

    fun entrySchema(entrySchemaType: String) {
        attributeDefinition.entrySchema = EntrySchemaBuilder(entrySchemaType).build()
    }

    fun entrySchema(entrySchemaType: String, block: EntrySchemaBuilder.() -> Unit) {
        attributeDefinition.entrySchema = EntrySchemaBuilder(entrySchemaType).apply(block).build()
    }
    // TODO("Constrains")

    fun defaultValue(defaultValue: JsonNode) {
        attributeDefinition.defaultValue = defaultValue
    }

    fun build(): AttributeDefinition {
        attributeDefinition.id = id
        attributeDefinition.type = type!!
        attributeDefinition.required = required
        attributeDefinition.value = expression
        attributeDefinition.description = description
        return attributeDefinition
    }
}

class DSLPropertyDefinitionBuilder(private val id: String,
                                   private val type: String? = BluePrintConstants.DATA_TYPE_STRING,
                                   private val required: Boolean? = false,
                                   private val expression: JsonNode,
                                   private val description: String? = "") {

    private var propertyDefinition: PropertyDefinition = PropertyDefinition()

    fun entrySchema(entrySchemaType: String) {
        propertyDefinition.entrySchema = EntrySchemaBuilder(entrySchemaType).build()
    }

    fun entrySchema(entrySchemaType: String, block: EntrySchemaBuilder.() -> Unit) {
        propertyDefinition.entrySchema = EntrySchemaBuilder(entrySchemaType).apply(block).build()
    }
    // TODO("Constrains")

    fun defaultValue(defaultValue: JsonNode) {
        propertyDefinition.defaultValue = defaultValue
    }

    fun build(): PropertyDefinition {
        propertyDefinition.id = id
        propertyDefinition.type = type!!
        propertyDefinition.required = required
        propertyDefinition.value = expression
        propertyDefinition.description = description
        return propertyDefinition
    }
}
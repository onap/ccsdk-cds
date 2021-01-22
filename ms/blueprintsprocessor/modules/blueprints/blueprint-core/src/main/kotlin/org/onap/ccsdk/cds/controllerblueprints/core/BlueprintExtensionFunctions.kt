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

package org.onap.ccsdk.cds.controllerblueprints.core

import org.onap.ccsdk.cds.controllerblueprints.core.annotations.ArtifactExpression
import org.onap.ccsdk.cds.controllerblueprints.core.annotations.AttributeExpression
import org.onap.ccsdk.cds.controllerblueprints.core.annotations.BlueprintsConstrain
import org.onap.ccsdk.cds.controllerblueprints.core.annotations.BlueprintsDataType
import org.onap.ccsdk.cds.controllerblueprints.core.annotations.BlueprintsProperty
import org.onap.ccsdk.cds.controllerblueprints.core.annotations.DSLExpression
import org.onap.ccsdk.cds.controllerblueprints.core.annotations.InputExpression
import org.onap.ccsdk.cds.controllerblueprints.core.annotations.OperationOutputExpression
import org.onap.ccsdk.cds.controllerblueprints.core.annotations.PropertyDefaultValue
import org.onap.ccsdk.cds.controllerblueprints.core.annotations.PropertyExpression
import org.onap.ccsdk.cds.controllerblueprints.core.data.ConstraintClause
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.EntrySchema
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.dslExpression
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.getInput
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.getNodeTemplateArtifact
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.getNodeTemplateAttribute
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.getNodeTemplateOperationOutput
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.getNodeTemplateProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties

fun <T : KClass<*>> T.asBlueprintsDataTypes(): DataType {
    val annotation = this.annotations.filter { it is BlueprintsDataType }.single() as BlueprintsDataType
    checkNotNull(annotation) { "BlueprintsDataType annotation definition not found" }
    val dataType = DataType().apply {
        id = annotation.name
        version = annotation.version
        derivedFrom = annotation.derivedFrom
        description = annotation.description
    }
    dataType.properties = this.asPropertyDefinitionMap()
    return dataType
}

fun <T : KClass<*>> T.asPropertyDefinitionMap(): MutableMap<String, PropertyDefinition> {
    val properties: MutableMap<String, PropertyDefinition> = hashMapOf()
    this.declaredMemberProperties.forEach { member ->
        properties[member.name] = member.asPropertyDefinition()
    }
    return properties
}

fun <T> KProperty1<T, *>.asPropertyDefinition(): PropertyDefinition {
    val property = PropertyDefinition()
    property.id = this.name
    val getter = this.getter
    property.required = !this.returnType.isMarkedNullable
    property.type = this.returnType.asBlueprintsDataType(this.name)
    if (this.returnType.arguments.isNotEmpty()) {
        property.entrySchema = this.returnType.entitySchema()
    }
    this.annotations.forEach { fieldAnnotation ->
        // println("Field : ${this.name} : Annotation : $fieldAnnotation")
        when (fieldAnnotation) {
            is BlueprintsProperty ->
                property.description = fieldAnnotation.description
            is PropertyDefaultValue ->
                property.value = fieldAnnotation.value.asJsonType(property.type)
            is BlueprintsConstrain -> {
                if (property.constraints == null) property.constraints = arrayListOf()
                property.constraints!!.add(fieldAnnotation.asBlueprintConstraintClause())
            }
            is InputExpression -> {
                property.value = getInput(fieldAnnotation.propertyName)
            }
            is PropertyExpression -> {
                property.value = getNodeTemplateProperty(
                    fieldAnnotation.modelableEntityName,
                    fieldAnnotation.propertyName, fieldAnnotation.subPropertyName
                )
            }
            is AttributeExpression -> {
                property.value = getNodeTemplateAttribute(
                    fieldAnnotation.modelableEntityName,
                    fieldAnnotation.attributeName, fieldAnnotation.subAttributeName
                )
            }
            is ArtifactExpression -> {
                property.value = getNodeTemplateArtifact(
                    fieldAnnotation.modelableEntityName,
                    fieldAnnotation.artifactName
                )
            }
            is OperationOutputExpression -> {
                property.value = getNodeTemplateOperationOutput(
                    fieldAnnotation.modelableEntityName,
                    fieldAnnotation.interfaceName, fieldAnnotation.propertyName, fieldAnnotation.subPropertyName
                )
            }
            is DSLExpression -> {
                property.value = dslExpression(fieldAnnotation.propertyName)
            }
        }
    }
    return property
}

internal fun BlueprintsConstrain.asBlueprintConstraintClause(): ConstraintClause {
    TODO()
}

internal fun <T : KType> T.entitySchema(): EntrySchema {
    val entrySchema = EntrySchema()
    if (this.arguments.size == 1) {
        entrySchema.type = this.arguments[0].type!!.asBlueprintsDataType("")
    } else if (this.arguments.size == 2) {
        entrySchema.type = this.arguments[1].type!!.asBlueprintsDataType("")
    }
    return entrySchema
}

internal fun <T : KType> T.asBlueprintsDataType(propertyName: String): String {
    val simpleName = (this.classifier as? KClass<*>)?.java?.simpleName
        ?: throw BlueprintException("filed to get simple name.")
    return when (simpleName) {
        "String", "Date" -> BlueprintConstants.DATA_TYPE_STRING
        "int" -> BlueprintConstants.DATA_TYPE_INTEGER
        "Boolean" -> BlueprintConstants.DATA_TYPE_BOOLEAN
        "Float" -> BlueprintConstants.DATA_TYPE_FLOAT
        "Double" -> BlueprintConstants.DATA_TYPE_DOUBLE
        "List" -> BlueprintConstants.DATA_TYPE_LIST
        "Map" -> BlueprintConstants.DATA_TYPE_MAP
        "Object", "JsonNode", "ObjectNode", "ArrayNode" -> BlueprintConstants.DATA_TYPE_JSON
        else -> simpleName
    }
}

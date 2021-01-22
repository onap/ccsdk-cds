/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.cds.controllerblueprints.validation.utils

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.format
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintExpressionService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils

open class PropertyAssignmentValidationUtils(private val bluePrintContext: BlueprintContext) {

    // Property Definition holds both Definitons and Expression in same construct
    open fun validatePropertyDefinitionNAssignments(propertyDefinitions: MutableMap<String, PropertyDefinition>) {
        propertyDefinitions.forEach { propertyName, propertyDefinition ->
            validatePropertyDefinitionNAssignment(propertyName, propertyDefinition)
        }
    }

    // Property Definition holds both Definitons and Expression in same construct
    open fun validatePropertyDefinitionNAssignment(propertyName: String, propertyDefinition: PropertyDefinition) {
        // Check and Validate if Expression Node
        checkNotNull(propertyDefinition.value) {
            throw BlueprintException("couldn't get 'value' property from PropertyDefinition($propertyName)")
        }
        val propertyAssignment = propertyDefinition.value!!
        val expressionData = BlueprintExpressionService.getExpressionData(propertyAssignment)
        if (!expressionData.isExpression) {
            checkPropertyValue(propertyName, propertyDefinition, propertyAssignment)
        }
    }

    open fun validatePropertyAssignments(
        nodeTypeProperties: MutableMap<String, PropertyDefinition>,
        properties: MutableMap<String, JsonNode>
    ) {
        properties.forEach { propertyName, propertyAssignment ->
            val propertyDefinition: PropertyDefinition = nodeTypeProperties[propertyName]
                ?: throw BlueprintException("validatePropertyAssignments failed to get definition for the property ($propertyName)")

            validatePropertyAssignment(propertyName, propertyDefinition, propertyAssignment)
        }
    }

    open fun validatePropertyAssignment(
        propertyName: String,
        propertyDefinition: PropertyDefinition,
        propertyAssignment: JsonNode
    ) {
        // Check and Validate if Expression Node
        val expressionData = BlueprintExpressionService.getExpressionData(propertyAssignment)
        if (!expressionData.isExpression) {
            checkPropertyValue(propertyName, propertyDefinition, propertyAssignment)
        }
    }

    open fun checkPropertyValue(propertyName: String, propertyDefinition: PropertyDefinition, propertyAssignment: JsonNode) {
        val propertyType = propertyDefinition.type
        val isValid: Boolean

        if (BlueprintTypes.validPrimitiveTypes().contains(propertyType)) {
            isValid = JacksonUtils.checkJsonNodeValueOfPrimitiveType(propertyType, propertyAssignment)
        } else if (BlueprintTypes.validComplexTypes().contains(propertyType)) {
            isValid = true
        } else if (BlueprintTypes.validCollectionTypes().contains(propertyType)) {

            val entrySchemaType = propertyDefinition.entrySchema?.type
                ?: throw BlueprintException(format("Failed to get EntrySchema type for the collection property ({})", propertyName))

            if (!BlueprintTypes.validPropertyTypes().contains(entrySchemaType)) {
                checkPropertyDataType(entrySchemaType, propertyName)
            }
            isValid = JacksonUtils.checkJsonNodeValueOfCollectionType(propertyType, propertyAssignment)
        } else {
            checkPropertyDataType(propertyType, propertyName)
            isValid = true
        }

        check(isValid) {
            throw BlueprintException("property($propertyName) defined of type($propertyType) is not compatible with the value ($propertyAssignment)")
        }
    }

    open fun checkPropertyDataType(dataTypeName: String, propertyName: String) {

        val dataType = bluePrintContext.serviceTemplate.dataTypes?.get(dataTypeName)
            ?: throw BlueprintException("DataType ($dataTypeName) for the property ($propertyName) not found")

        checkValidDataTypeDerivedFrom(propertyName, dataType.derivedFrom)
    }

    open fun checkValidDataTypeDerivedFrom(dataTypeName: String, derivedFrom: String) {
        check(BlueprintTypes.validDataTypeDerivedFroms.contains(derivedFrom)) {
            throw BlueprintException("Failed to get DataType($dataTypeName)'s  derivedFrom($derivedFrom) definition ")
        }
    }
}

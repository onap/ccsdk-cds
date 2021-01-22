/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.validation

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.AttributeDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.format
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintAttributeDefinitionValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("default-attribute-definition-validator")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BlueprintAttributeDefinitionValidatorImpl(private val bluePrintTypeValidatorService: BlueprintTypeValidatorService) :
    BlueprintAttributeDefinitionValidator {

    private val log = LoggerFactory.getLogger(BlueprintServiceTemplateValidatorImpl::class.toString())

    lateinit var bluePrintRuntimeService: BlueprintRuntimeService<*>

    override fun validate(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        attributeDefinition: AttributeDefinition
    ) {

        log.trace("Validating AttributeDefinition($name)")
        this.bluePrintRuntimeService = bluePrintRuntimeService
        val dataType: String = attributeDefinition.type

        when {
            BlueprintTypes.validPrimitiveTypes().contains(dataType) -> {
                // Do Nothing
            }
            BlueprintTypes.validComplexTypes().contains(dataType) -> {
                // Do Nothing
            }
            BlueprintTypes.validCollectionTypes().contains(dataType) -> {
                val entrySchemaType: String = attributeDefinition.entrySchema?.type
                    ?: throw BlueprintException("Entry schema for DataType ($dataType) for the property ($name) not found")
                checkPrimitiveOrComplex(entrySchemaType, name)
            }
            else -> checkPropertyDataType(dataType, name)
        }
    }

    private fun checkPrimitiveOrComplex(dataType: String, propertyName: String): Boolean {
        if (BlueprintTypes.validPrimitiveTypes().contains(dataType) || checkDataType(dataType)) {
            return true
        } else {
            throw BlueprintException("DataType($dataType) for the attribute($propertyName) is not valid")
        }
    }

    private fun checkPropertyDataType(dataTypeName: String, propertyName: String) {

        val dataType = bluePrintRuntimeService.bluePrintContext().serviceTemplate.dataTypes?.get(dataTypeName)
            ?: throw BlueprintException(format("DataType ({}) for the property ({}) not found", dataTypeName, propertyName))

        checkValidDataTypeDerivedFrom(propertyName, dataType.derivedFrom)
    }

    private fun checkDataType(key: String): Boolean {
        return bluePrintRuntimeService.bluePrintContext().serviceTemplate.dataTypes?.containsKey(key) ?: false
    }

    open fun checkValidDataTypeDerivedFrom(dataTypeName: String, derivedFrom: String) {
        check(BlueprintTypes.validDataTypeDerivedFroms.contains(derivedFrom)) {
            throw BlueprintException("Failed to get DataType($dataTypeName)'s  derivedFrom($derivedFrom) definition ")
        }
    }
}

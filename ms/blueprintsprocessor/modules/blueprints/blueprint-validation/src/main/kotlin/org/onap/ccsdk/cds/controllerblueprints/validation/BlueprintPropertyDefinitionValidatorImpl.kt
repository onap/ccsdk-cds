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
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.format
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintPropertyDefinitionValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("default-property-definition-validator")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BlueprintPropertyDefinitionValidatorImpl(private val bluePrintTypeValidatorService: BlueprintTypeValidatorService) :
    BlueprintPropertyDefinitionValidator {

    private val log = LoggerFactory.getLogger(BlueprintServiceTemplateValidatorImpl::class.toString())

    lateinit var bluePrintRuntimeService: BlueprintRuntimeService<*>

    override fun validate(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, propertyDefinition: PropertyDefinition) {
        this.bluePrintRuntimeService = bluePrintRuntimeService

        log.trace("Validating PropertyDefinition($name)")

        val dataType: String = propertyDefinition.type

        when {
            BlueprintTypes.validPrimitiveTypes().contains(dataType) -> {
                // Do Nothing
            }
            BlueprintTypes.validComplexTypes().contains(dataType) -> {
                // Do Nothing
            }
            BlueprintTypes.validCollectionTypes().contains(dataType) -> {
                val entrySchemaType: String = propertyDefinition.entrySchema?.type
                    ?: throw BlueprintException(format("Entry schema for DataType ({}) for the property ({}) not found", dataType, name))
                checkPrimitiveOrComplex(entrySchemaType, name)
            }
            else -> checkPropertyDataType(dataType, name)
        }
    }

    private fun checkPrimitiveOrComplex(dataType: String, propertyName: String): Boolean {
        if (BlueprintTypes.validPrimitiveTypes().contains(dataType) || checkDataType(dataType)) {
            return true
        } else {
            throw BlueprintException(format("DataType({}) for the property({}) is not valid", dataType, propertyName))
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
            throw BlueprintException(format("Failed to get DataType({})'s  derivedFrom({}) definition ", dataTypeName, derivedFrom))
        }
    }
}

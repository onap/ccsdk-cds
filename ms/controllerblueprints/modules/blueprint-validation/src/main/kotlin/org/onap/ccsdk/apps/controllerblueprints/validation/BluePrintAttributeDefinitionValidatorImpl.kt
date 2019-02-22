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

package org.onap.ccsdk.apps.controllerblueprints.validation

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.apps.controllerblueprints.core.data.AttributeDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.format
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintAttributeDefinitionValidator
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("default-attribute-definition-validator")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintAttributeDefinitionValidatorImpl(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService) : BluePrintAttributeDefinitionValidator {


    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintServiceTemplateValidatorImpl::class.toString())

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>


    override fun validate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String,
                          attributeDefinition: AttributeDefinition) {

        log.trace("Validating AttributeDefinition($name)")
        this.bluePrintRuntimeService = bluePrintRuntimeService
        val dataType: String = attributeDefinition.type

        when {
            BluePrintTypes.validPrimitiveTypes().contains(dataType) -> {
                // Do Nothing
            }
            BluePrintTypes.validCollectionTypes().contains(dataType) -> {
                val entrySchemaType: String = attributeDefinition.entrySchema?.type
                        ?: throw BluePrintException("Entry schema for DataType ($dataType) for the property ($name) not found")
                checkPrimitiveOrComplex(entrySchemaType, name)
            }
            else -> checkPropertyDataType(dataType, name)
        }
    }

    private fun checkPrimitiveOrComplex(dataType: String, propertyName: String): Boolean {
        if (BluePrintTypes.validPrimitiveTypes().contains(dataType) || checkDataType(dataType)) {
            return true
        } else {
            throw BluePrintException("DataType($dataType) for the attribute($propertyName) is not valid")
        }
    }

    private fun checkPropertyDataType(dataTypeName: String, propertyName: String) {

        val dataType = bluePrintRuntimeService.bluePrintContext().serviceTemplate.dataTypes?.get(dataTypeName)
                ?: throw BluePrintException(format("DataType ({}) for the property ({}) not found", dataTypeName, propertyName))

        checkValidDataTypeDerivedFrom(propertyName, dataType.derivedFrom)
    }

    private fun checkDataType(key: String): Boolean {
        return bluePrintRuntimeService.bluePrintContext().serviceTemplate.dataTypes?.containsKey(key) ?: false
    }

    open fun checkValidDataTypeDerivedFrom(dataTypeName: String, derivedFrom: String) {
        check(BluePrintTypes.validDataTypeDerivedFroms.contains(derivedFrom)) {
            throw BluePrintException("Failed to get DataType($dataTypeName)'s  derivedFrom($derivedFrom) definition ")
        }
    }
}
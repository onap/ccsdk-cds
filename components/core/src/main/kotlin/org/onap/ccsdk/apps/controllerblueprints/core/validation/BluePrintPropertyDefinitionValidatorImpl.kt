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

package org.onap.ccsdk.apps.controllerblueprints.core.validation

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintError
import org.onap.ccsdk.apps.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.format
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintPropertyDefinitionValidator
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext

open class BluePrintPropertyDefinitionValidatorImpl(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService) : BluePrintPropertyDefinitionValidator {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintServiceTemplateValidatorImpl::class.toString())

    var bluePrintContext: BluePrintContext? = null
    var error: BluePrintError? = null

    override fun validate(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, propertyDefinition: PropertyDefinition) {
        this.bluePrintContext = bluePrintContext
        this.error = error

        log.trace("Validating PropertyDefinition($name)")

        val dataType: String = propertyDefinition.type

        when {
            BluePrintTypes.validPrimitiveTypes().contains(dataType) -> {
                // Do Nothing
            }
            BluePrintTypes.validCollectionTypes().contains(dataType) -> {
                val entrySchemaType: String = propertyDefinition.entrySchema?.type
                        ?: throw BluePrintException(format("Entry schema for DataType ({}) for the property ({}) not found", dataType, name))
                checkPrimitiveOrComplex(entrySchemaType, name)
            }
            else -> checkPropertyDataType(dataType, name)
        }
    }


    private fun checkPrimitiveOrComplex(dataType: String, propertyName: String): Boolean {
        if (BluePrintTypes.validPrimitiveTypes().contains(dataType) || checkDataType(dataType)) {
            return true
        } else {
            throw BluePrintException(format("DataType({}) for the property({}) is not valid", dataType, propertyName))
        }
    }

    private fun checkPropertyDataType(dataTypeName: String, propertyName: String) {

        val dataType = bluePrintContext!!.serviceTemplate.dataTypes?.get(dataTypeName)
                ?: throw BluePrintException(format("DataType ({}) for the property ({}) not found", dataTypeName, propertyName))

        checkValidDataTypeDerivedFrom(propertyName, dataType.derivedFrom)
    }

    private fun checkDataType(key: String): Boolean {
        return bluePrintContext!!.serviceTemplate.dataTypes?.containsKey(key) ?: false
    }

    open fun checkValidDataTypeDerivedFrom(dataTypeName: String, derivedFrom: String) {
        check(BluePrintTypes.validDataTypeDerivedFroms.contains(derivedFrom)) {
            throw BluePrintException(format("Failed to get DataType({})'s  derivedFrom({}) definition ", dataTypeName, derivedFrom))
        }
    }
}
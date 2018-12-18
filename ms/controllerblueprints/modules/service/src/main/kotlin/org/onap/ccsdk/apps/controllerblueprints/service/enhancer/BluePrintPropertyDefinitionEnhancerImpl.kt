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

package org.onap.ccsdk.apps.controllerblueprints.service.enhancer

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.apps.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintPropertyDefinitionEnhancer
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.service.utils.BluePrintEnhancerUtils
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintPropertyDefinitionEnhancerImpl(private val bluePrintRepoService: BluePrintRepoService,
                                                   private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService)
    : BluePrintPropertyDefinitionEnhancer {

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var bluePrintContext: BluePrintContext

    override fun enhance(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, propertyDefinition: PropertyDefinition) {
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val propertyType = propertyDefinition.type
        if (BluePrintTypes.validPrimitiveTypes().contains(propertyType)) {

        } else if (BluePrintTypes.validCollectionTypes().contains(propertyType)) {
            val entrySchema = propertyDefinition.entrySchema
                    ?: throw BluePrintException("Entry Schema is missing for collection property($name)")

            if (!BluePrintTypes.validPrimitiveTypes().contains(entrySchema.type)) {
                BluePrintEnhancerUtils.populateDataTypes(bluePrintContext, bluePrintRepoService, entrySchema.type)
            }
        } else {
            BluePrintEnhancerUtils.populateDataTypes(bluePrintContext, bluePrintRepoService, propertyType)
        }
    }

}
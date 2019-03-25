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

package org.onap.ccsdk.cds.controllerblueprints.service.enhancer

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.AttributeDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintAttributeDefinitionEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.service.utils.BluePrintEnhancerUtils

class BluePrintAttributeDefinitionEnhancerImpl(private val bluePrintRepoService: BluePrintRepoService,
                                               private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService)
    : BluePrintAttributeDefinitionEnhancer {

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var bluePrintContext: BluePrintContext

    override fun enhance(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, attributeDefinition: AttributeDefinition) {
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val propertyType = attributeDefinition.type
        if (BluePrintTypes.validPrimitiveTypes().contains(propertyType)) {

        } else if (BluePrintTypes.validCollectionTypes().contains(propertyType)) {
            val entrySchema = attributeDefinition.entrySchema
                    ?: throw BluePrintException("Entry Schema is missing for collection property($name)")

            if (!BluePrintTypes.validPrimitiveTypes().contains(entrySchema.type)) {
                BluePrintEnhancerUtils.populateDataTypes(bluePrintContext, bluePrintRepoService, entrySchema.type)
            }
        } else {
            BluePrintEnhancerUtils.populateDataTypes(bluePrintContext, bluePrintRepoService, propertyType)
        }
    }

}
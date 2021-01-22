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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.enhancer

import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils.BlueprintEnhancerUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.AttributeDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintAttributeDefinitionEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintRepoService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService

class BlueprintAttributeDefinitionEnhancerImpl(
    private val bluePrintRepoService: BlueprintRepoService,
    private val bluePrintTypeEnhancerService: BlueprintTypeEnhancerService
) :
    BlueprintAttributeDefinitionEnhancer {

    lateinit var bluePrintRuntimeService: BlueprintRuntimeService<*>
    lateinit var bluePrintContext: BlueprintContext

    override fun enhance(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, attributeDefinition: AttributeDefinition) {
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val propertyType = attributeDefinition.type
        if (BlueprintTypes.validPrimitiveTypes().contains(propertyType)) {
        } else if (BlueprintTypes.validCollectionTypes().contains(propertyType)) {
            val entrySchema = attributeDefinition.entrySchema
                ?: throw BlueprintException("Entry Schema is missing for collection property($name)")

            if (!BlueprintTypes.validPrimitiveTypes().contains(entrySchema.type)) {
                BlueprintEnhancerUtils.populateDataTypes(bluePrintContext, bluePrintRepoService, entrySchema.type)
            }
        } else {
            BlueprintEnhancerUtils.populateDataTypes(bluePrintContext, bluePrintRepoService, propertyType)
        }
    }
}

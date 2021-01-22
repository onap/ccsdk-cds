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

package org.onap.ccsdk.cds.controllerblueprints.validation.extension

import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintValidator
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

interface ResourceDefinitionValidator : BlueprintValidator<ResourceDefinition>

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ResourceDefinitionValidatorImpl(private val bluePrintTypeValidatorService: BlueprintTypeValidatorService) : ResourceDefinitionValidator {

    private val log = LoggerFactory.getLogger(ResourceDefinitionValidatorImpl::class.java)

    override fun validate(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        resourceDefinition: ResourceDefinition
    ) {
        log.trace("validating resource definition($name)")
        resourceDefinition.sources.forEach { name, nodeTemplate ->
            bluePrintTypeValidatorService.validateNodeTemplate(bluePrintRuntimeService, name, nodeTemplate)
        }
    }
}

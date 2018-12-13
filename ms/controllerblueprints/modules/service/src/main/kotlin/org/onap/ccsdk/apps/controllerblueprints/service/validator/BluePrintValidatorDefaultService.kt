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

package org.onap.ccsdk.apps.controllerblueprints.service.validator

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.apps.controllerblueprints.core.validation.*
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.service.ResourceAssignmentValidationServiceImpl
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.service.ResourceDefinitionValidationServiceImpl
import org.springframework.stereotype.Service
import java.util.*

@Service
class BluePrintTypeValidatorDefaultService(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService)
    : BluePrintValidatorService {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintValidatorServiceImpl::class.toString())

    override fun validateBluePrints(basePath: String): Boolean {

        log.info("validating blueprint($basePath)")
        val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(UUID.randomUUID().toString(), basePath)
        return validateBluePrints(bluePrintRuntimeService)
    }

    override fun validateBluePrints(bluePrintRuntimeService: BluePrintRuntimeService<*>): Boolean {

        bluePrintTypeValidatorService.validateServiceTemplate(bluePrintRuntimeService, "service_template",
                bluePrintRuntimeService.bluePrintContext().serviceTemplate)

        if (bluePrintRuntimeService.getBluePrintError().errors.size > 0) {
            throw BluePrintException("failed in blueprint validation : ${bluePrintRuntimeService.getBluePrintError().errors.joinToString("\n")}")
        }
        return true
    }
}

// Core Validator Services

@Service
class DefaultBluePrintServiceTemplateValidator(bluePrintTypeValidatorService: BluePrintTypeValidatorService)
    : BluePrintServiceTemplateValidatorImpl(bluePrintTypeValidatorService)

@Service
class DefaultBluePrintDataTypeValidator(bluePrintTypeValidatorService: BluePrintTypeValidatorService)
    : BluePrintDataTypeValidatorImpl(bluePrintTypeValidatorService)

@Service
class DefaultBluePrintArtifactTypeValidator(bluePrintTypeValidatorService: BluePrintTypeValidatorService)
    : BluePrintArtifactTypeValidatorImpl(bluePrintTypeValidatorService)

@Service
class DefaultBluePrintNodeTypeValidator(bluePrintTypeValidatorService: BluePrintTypeValidatorService)
    : BluePrintNodeTypeValidatorImpl(bluePrintTypeValidatorService)

@Service
class DefaultBluePrintTopologyTemplateValidator(bluePrintTypeValidatorService: BluePrintTypeValidatorService)
    : BluePrintTopologyTemplateValidatorImpl(bluePrintTypeValidatorService)

@Service
class DefaulBluePrintNodeTemplateValidator(bluePrintTypeValidatorService: BluePrintTypeValidatorService)
    : BluePrintNodeTemplateValidatorImpl(bluePrintTypeValidatorService)

@Service
class DefaultBluePrintWorkflowValidator(bluePrintTypeValidatorService: BluePrintTypeValidatorService)
    : BluePrintWorkflowValidatorImpl(bluePrintTypeValidatorService)

@Service
class DefaulBluePrintPropertyDefinitionValidator(bluePrintTypeValidatorService: BluePrintTypeValidatorService)
    : BluePrintPropertyDefinitionValidatorImpl(bluePrintTypeValidatorService)

@Service
class DefaultBluePrintAttributeDefinitionValidator(bluePrintTypeValidatorService: BluePrintTypeValidatorService)
    : BluePrintAttributeDefinitionValidatorImpl(bluePrintTypeValidatorService)

// Resource Dictionary Validation Services

@Service
class DefaultResourceAssignmentValidationService : ResourceAssignmentValidationServiceImpl()

@Service
class DefalutResourceDefinitionValidationService(bluePrintRepoService: BluePrintRepoService)
    : ResourceDefinitionValidationServiceImpl(bluePrintRepoService)
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

import org.slf4j.LoggerFactory
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDictionaryConstants
import org.onap.ccsdk.cds.controllerblueprints.validation.extension.ResourceDefinitionValidator
import org.springframework.stereotype.Service
import java.io.File
import java.util.*


@Service
open class BluePrintDesignTimeValidatorService(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService,
                                               private val resourceDefinitionValidator: ResourceDefinitionValidator)
    : BluePrintValidatorService {

    private val log= LoggerFactory.getLogger(BluePrintDesignTimeValidatorService::class.toString())

    override fun validateBluePrints(basePath: String): Boolean {

        val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(UUID.randomUUID().toString(), basePath)
        return validateBluePrints(bluePrintRuntimeService)
    }

    override fun validateBluePrints(bluePrintRuntimeService: BluePrintRuntimeService<*>): Boolean {

        bluePrintTypeValidatorService.validateServiceTemplate(bluePrintRuntimeService, "service_template",
                bluePrintRuntimeService.bluePrintContext().serviceTemplate)

        // Validate Resource Definitions
        validateResourceDefinitions(bluePrintRuntimeService)

        if (bluePrintRuntimeService.getBluePrintError().errors.size > 0) {
            throw BluePrintException("failed in blueprint validation : ${bluePrintRuntimeService.getBluePrintError().errors.joinToString("\n")}")
        }
        return true
    }

    private fun validateResourceDefinitions(bluePrintRuntimeService: BluePrintRuntimeService<*>) {
        // Validate Resource Dictionary
        val blueprintBasePath = bluePrintRuntimeService.bluePrintContext().rootPath

        val resourceDefinitionsPath = blueprintBasePath.plus(File.separator)
                .plus(BluePrintConstants.TOSCA_DEFINITIONS_DIR).plus(File.separator)
                .plus("${ResourceDictionaryConstants.PATH_RESOURCE_DEFINITION_TYPE}.json")

        val resourceDefinitionFile = File(resourceDefinitionsPath)

        if (resourceDefinitionFile.exists()) {
            val resourceDefinitionMap = JacksonUtils.getMapFromFile(resourceDefinitionFile, ResourceDefinition::class.java)

            resourceDefinitionMap?.forEach { resourceDefinitionName, resourceDefinition ->
                resourceDefinitionValidator.validate(bluePrintRuntimeService, resourceDefinitionName, resourceDefinition)
            }
        }
    }
}

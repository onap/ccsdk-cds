/*
 *  Copyright © 2018 IBM.
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

import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintArtifactDefinitionValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.service.ResourceAssignmentValidationServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service("artifact-mapping-resource-artifact-definition-validator")
open class ArtifactMappingResourceValidator(private val bluePrintTypeValidatorService: BlueprintTypeValidatorService) :
    BlueprintArtifactDefinitionValidator {

    private val log = LoggerFactory.getLogger(ArtifactMappingResourceValidator::class.toString())

    override fun validate(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        artifactDefinition: ArtifactDefinition
    ) {

        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
        val file: String = artifactDefinition.file
        val completePath = bluePrintContext.rootPath.plus(File.separator).plus(file)
        log.trace("Validation artifact-mapping-resource($completePath)")
        val resourceAssignment = JacksonUtils.getListFromFile(completePath, ResourceAssignment::class.java)
        val resourceAssignmentValidationService = ResourceAssignmentValidationServiceImpl()
        resourceAssignmentValidationService.validate(resourceAssignment)
    }
}

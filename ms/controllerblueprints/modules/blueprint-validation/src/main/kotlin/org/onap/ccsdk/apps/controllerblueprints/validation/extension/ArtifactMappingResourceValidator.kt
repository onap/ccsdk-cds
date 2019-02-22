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

package org.onap.ccsdk.apps.controllerblueprints.validation.extension

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintArtifactDefinitionValidator
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.service.ResourceAssignmentValidationServiceImpl
import org.springframework.stereotype.Service
import java.io.File

@Service("artifact-mapping-resource-artifact-definition-validator")
open class ArtifactMappingResourceValidator(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService)
    : BluePrintArtifactDefinitionValidator {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(ArtifactMappingResourceValidator::class.toString())

    override fun validate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String,
                          artifactDefinition: ArtifactDefinition) {

        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
        val file: String = artifactDefinition.file
        val completePath = bluePrintContext.rootPath.plus(File.separator).plus(file)
        log.info("Validation artifact-mapping-resource($completePath)")
        val resourceAssignment = JacksonUtils.getListFromFile(completePath, ResourceAssignment::class.java)
        val resourceAssignmentValidationService = ResourceAssignmentValidationServiceImpl()
        resourceAssignmentValidationService.validate(resourceAssignment)
    }
}
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

package org.onap.ccsdk.cds.controllerblueprints.validation

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintArtifactDefinitionValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.io.File

@Service("default-artifact-definition-validator")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintArtifactDefinitionValidatorImpl(
        private val bluePrintTypeValidatorService: BluePrintTypeValidatorService) : BluePrintArtifactDefinitionValidator {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintArtifactDefinitionValidatorImpl::class.toString())

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var bluePrintContext: BluePrintContext
    var paths: MutableList<String> = arrayListOf()

    override fun validate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String,
                          artifactDefinition: ArtifactDefinition) {

        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        paths.add(name)
        val type: String = artifactDefinition.type
        log.trace("Validation ArtifactDefinition of type {$type}")
        // Check Artifact Type
        checkValidArtifactType(name, type)

        val file: String = artifactDefinition.file

        val completePath = bluePrintContext.rootPath.plus(File.separator).plus(file)

        check(File(completePath).exists()) {
            throw BluePrintException("couldn't find file ($completePath)")
        }

        // Perform Extension Validation
        validateExtension("$type-artifact-definition-validator", name, artifactDefinition)

        paths.removeAt(paths.lastIndex)
    }

    open fun checkValidArtifactType(artifactDefinitionName: String, artifactTypeName: String) {

        val artifactType = bluePrintContext.serviceTemplate.artifactTypes?.get(artifactTypeName)
                ?: throw BluePrintException("failed to get artifactType($artifactTypeName) for ArtifactDefinition($artifactDefinitionName)")

        checkValidArtifactTypeDerivedFrom(artifactTypeName, artifactType.derivedFrom)
    }

    @Throws(BluePrintException::class)
    open fun checkValidArtifactTypeDerivedFrom(artifactTypeName: String, derivedFrom: String) {
        check(BluePrintTypes.validArtifactTypeDerivedFroms.contains(derivedFrom)) {
            throw BluePrintException("failed to get artifactType($artifactTypeName)'s derivedFrom($derivedFrom) definition")
        }
    }

    private fun validateExtension(referencePrefix: String, name: String, artifactDefinition: ArtifactDefinition) {

        val customValidators = bluePrintTypeValidatorService
                .bluePrintValidators(referencePrefix, BluePrintArtifactDefinitionValidator::class.java)

        customValidators?.let {
            it.forEach { validator ->
                validator.validate(bluePrintRuntimeService, name, artifactDefinition)
            }

        }
    }
}
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

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintArtifactDefinitionValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.io.File

@Service("default-artifact-definition-validator")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BlueprintArtifactDefinitionValidatorImpl(
    private val bluePrintTypeValidatorService: BlueprintTypeValidatorService
) : BlueprintArtifactDefinitionValidator {

    private val log = LoggerFactory.getLogger(BlueprintArtifactDefinitionValidatorImpl::class.toString())

    lateinit var bluePrintRuntimeService: BlueprintRuntimeService<*>
    lateinit var bluePrintContext: BlueprintContext
    var paths: MutableList<String> = arrayListOf()

    override fun validate(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        artifactDefinition: ArtifactDefinition
    ) {

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
            throw BlueprintException("couldn't find file ($completePath)")
        }

        // Perform Extension Validation
        validateExtension("$type-artifact-definition-validator", name, artifactDefinition)

        paths.removeAt(paths.lastIndex)
    }

    open fun checkValidArtifactType(artifactDefinitionName: String, artifactTypeName: String) {

        val artifactType = bluePrintContext.serviceTemplate.artifactTypes?.get(artifactTypeName)
            ?: throw BlueprintException("failed to get artifactType($artifactTypeName) for ArtifactDefinition($artifactDefinitionName)")

        checkValidArtifactTypeDerivedFrom(artifactTypeName, artifactType.derivedFrom)
    }

    @Throws(BlueprintException::class)
    open fun checkValidArtifactTypeDerivedFrom(artifactTypeName: String, derivedFrom: String) {
        check(BlueprintTypes.validArtifactTypeDerivedFroms.contains(derivedFrom)) {
            throw BlueprintException("failed to get artifactType($artifactTypeName)'s derivedFrom($derivedFrom) definition")
        }
    }

    private fun validateExtension(referencePrefix: String, name: String, artifactDefinition: ArtifactDefinition) {

        val customValidators = bluePrintTypeValidatorService
            .bluePrintValidators(referencePrefix, BlueprintArtifactDefinitionValidator::class.java)

        customValidators?.let {
            it.forEach { validator ->
                validator.validate(bluePrintRuntimeService, name, artifactDefinition)
            }
        }
    }
}

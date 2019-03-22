/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.apps.controllerblueprints.service.load

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.common.ApplicationConstants
import org.onap.ccsdk.apps.controllerblueprints.core.config.BluePrintLoadConfiguration
import org.onap.ccsdk.apps.controllerblueprints.core.data.ErrorCode
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.apps.controllerblueprints.db.resources.BlueprintCatalogServiceImpl
import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModel
import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModelContent
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ControllerBlueprintModelRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Similar implementation in [org.onap.ccsdk.apps.blueprintsprocessor.db.BlueprintProcessorCatalogServiceImpl]
 */
@Service
class ControllerBlueprintCatalogServiceImpl(bluePrintValidatorService: BluePrintValidatorService,
                                            private val bluePrintLoadConfiguration: BluePrintLoadConfiguration,
                                            private val blueprintModelRepository: ControllerBlueprintModelRepository)
    : BlueprintCatalogServiceImpl(bluePrintValidatorService) {


    private val log = LoggerFactory.getLogger(ControllerBlueprintCatalogServiceImpl::class.toString())

    init {
        log.info("BlueprintProcessorCatalogServiceImpl initialized")
    }

    override fun delete(name: String, version: String) = blueprintModelRepository.deleteByArtifactNameAndArtifactVersion(name, version)

    override fun get(name: String, version: String, extract: Boolean): Path? {
        val path = if (extract) {
            Paths.get("${bluePrintLoadConfiguration.blueprintDeployPath}/$name/$version")
        } else {
            Paths.get("${bluePrintLoadConfiguration.blueprintArchivePath}/$name/$version.zip")
        }
        blueprintModelRepository.findByArtifactNameAndArtifactVersion(name, version)?.also {
            it.blueprintModelContent.run {
                path.toFile().writeBytes(this!!.content!!).let {
                    return path
                }
            }
        }
        return null
    }

    override fun save(metadata: MutableMap<String, String>, archiveFile: File) {

        val artifactName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]
        val artifactVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]


        blueprintModelRepository.findByArtifactNameAndArtifactVersion(artifactName!!, artifactVersion!!)?.let {
            log.info("Overwriting blueprint model :$artifactName::$artifactVersion")
            blueprintModelRepository.deleteByArtifactNameAndArtifactVersion(artifactName, artifactVersion)
        }

        val blueprintModel = BlueprintModel()
        blueprintModel.id = metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID]
        blueprintModel.artifactType = ApplicationConstants.ASDC_ARTIFACT_TYPE_SDNC_MODEL
        blueprintModel.published = metadata[BluePrintConstants.PROPERTY_BLUEPRINT_VALID]
                ?: BluePrintConstants.FLAG_N
        blueprintModel.artifactName = artifactName
        blueprintModel.artifactVersion = artifactVersion
        blueprintModel.updatedBy = metadata[BluePrintConstants.METADATA_TEMPLATE_AUTHOR]
        blueprintModel.tags = metadata[BluePrintConstants.METADATA_TEMPLATE_TAGS]
        blueprintModel.artifactDescription = "Controller Blueprint for $artifactName:$artifactVersion"

        val blueprintModelContent = BlueprintModelContent()
        blueprintModelContent.id = metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID]
        blueprintModelContent.contentType = "CBA_ZIP"
        blueprintModelContent.name = "$artifactName:$artifactVersion"
        blueprintModelContent.description = "$artifactName:$artifactVersion CBA Zip Content"
        blueprintModelContent.content = Files.readAllBytes(archiveFile.toPath())
        blueprintModelContent.blueprintModel = blueprintModel
        // Set the Blueprint Model Content into blueprintModel
        blueprintModel.blueprintModelContent = blueprintModelContent

        try {
            blueprintModelRepository.saveAndFlush(blueprintModel)
        } catch (ex: DataIntegrityViolationException) {
            throw BluePrintException(ErrorCode.CONFLICT_ADDING_RESOURCE.value, "The blueprint entry " +
                    "is already exist in database: ${ex.message}", ex)
        }
    }
}
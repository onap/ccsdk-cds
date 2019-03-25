/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.db

import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintProcessorModel
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintProcessorModelContent
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.repository.BlueprintProcessorModelRepository
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.common.ApplicationConstants
import org.onap.ccsdk.cds.controllerblueprints.core.config.BluePrintPathConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.data.ErrorCode
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintArchiveUtils
import org.onap.ccsdk.cds.controllerblueprints.db.resources.BlueprintCatalogServiceImpl
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * Similar/Duplicate implementation in [org.onap.ccsdk.cds.controllerblueprints.service.load.ControllerBlueprintCatalogServiceImpl]
 */
@Service
class BlueprintProcessorCatalogServiceImpl(bluePrintRuntimeValidatorService: BluePrintValidatorService,
                                           private val bluePrintPathConfiguration: BluePrintPathConfiguration,
                                           private val blueprintModelRepository: BlueprintProcessorModelRepository)
    : BlueprintCatalogServiceImpl(bluePrintPathConfiguration, bluePrintRuntimeValidatorService) {

    private val log = LoggerFactory.getLogger(BlueprintProcessorCatalogServiceImpl::class.toString())

    init {

        log.info("BlueprintProcessorCatalogServiceImpl initialized")
    }

    override suspend fun delete(name: String, version: String) {
        // Cleaning Deployed Blueprint
        deleteDir(bluePrintPathConfiguration.blueprintDeployPath, name, version)
        // Cleaning Data Base
        blueprintModelRepository
                .deleteByArtifactNameAndArtifactVersion(name, version)
    }


    override suspend fun get(name: String, version: String, extract: Boolean): Path? {

        val getId = UUID.randomUUID().toString()
        var path = "${bluePrintPathConfiguration.blueprintArchivePath}/$getId/cba.zip"

        // TODO("Check first location for the file", If not get from database")

        blueprintModelRepository.findByArtifactNameAndArtifactVersion(name, version)?.also {
            it.blueprintModelContent.run {
                val file = normalizedFile(path)
                file.parentFile.reCreateDirs()

                file.writeBytes(this!!.content!!).let {
                    if (extract) {
                        path = "${bluePrintPathConfiguration.blueprintDeployPath}/$name/$version"
                        BluePrintArchiveUtils.deCompress(file, path)
                    }
                    return normalizedPath(path)
                }
            }
        }
        return null
    }

    override suspend fun save(metadata: MutableMap<String, String>, archiveFile: File) {
        val artifactName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]
        val artifactVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]

        check(archiveFile.isFile && !archiveFile.isDirectory) {
            throw BluePrintException("Not a valid Archive file(${archiveFile.absolutePath})")
        }

        blueprintModelRepository.findByArtifactNameAndArtifactVersion(artifactName!!, artifactVersion!!)?.let {
            log.info("Overwriting blueprint model :$artifactName::$artifactVersion")
            blueprintModelRepository.deleteByArtifactNameAndArtifactVersion(artifactName, artifactVersion)
        }

        val blueprintModel = BlueprintProcessorModel()
        blueprintModel.id = metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID]
        blueprintModel.artifactType = ApplicationConstants.ASDC_ARTIFACT_TYPE_SDNC_MODEL
        blueprintModel.artifactName = artifactName
        blueprintModel.artifactVersion = artifactVersion
        blueprintModel.updatedBy = metadata[BluePrintConstants.METADATA_TEMPLATE_AUTHOR]
        blueprintModel.tags = metadata[BluePrintConstants.METADATA_TEMPLATE_TAGS]
        blueprintModel.artifactDescription = "Controller Blueprint for $artifactName:$artifactVersion"

        val blueprintModelContent = BlueprintProcessorModelContent()
        blueprintModelContent.id = metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID]
        blueprintModelContent.contentType = "CBA_ZIP"
        blueprintModelContent.name = "$artifactName:$artifactVersion"
        blueprintModelContent.description = "$artifactName:$artifactVersion CBA Zip Content"
        blueprintModelContent.content = Files.readAllBytes(archiveFile.toPath())
        blueprintModelContent.blueprintModel = blueprintModel

        blueprintModel.blueprintModelContent = blueprintModelContent

        try {
            blueprintModelRepository.saveAndFlush(blueprintModel)
        } catch (ex: DataIntegrityViolationException) {
            throw BluePrintException(ErrorCode.CONFLICT_ADDING_RESOURCE.value, "The blueprint entry " +
                    "is already exist in database: ${ex.message}", ex)
        }
    }
}
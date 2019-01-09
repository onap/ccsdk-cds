/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.apps.blueprintsprocessor.db

import org.onap.ccsdk.apps.blueprintsprocessor.db.primary.domain.BlueprintProcessorModel
import org.onap.ccsdk.apps.blueprintsprocessor.db.primary.domain.BlueprintProcessorModelContent
import org.onap.ccsdk.apps.blueprintsprocessor.db.primary.repository.BlueprintProcessorModelContentRepository
import org.onap.ccsdk.apps.blueprintsprocessor.db.primary.repository.BlueprintProcessorModelRepository
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.common.ApplicationConstants
import org.onap.ccsdk.apps.controllerblueprints.core.config.BluePrintLoadConfiguration
import org.onap.ccsdk.apps.controllerblueprints.core.data.ErrorCode
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintArchiveUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.apps.controllerblueprints.db.resources.BlueprintCatalogServiceImpl
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files

/**
Similar/Duplicate implementation in [org.onap.ccsdk.apps.controllerblueprints.service.load.ControllerBlueprintCatalogServiceImpl]
 */
@Service
class BlueprintProcessorCatalogServiceImpl(bluePrintLoadConfiguration: BluePrintLoadConfiguration,
                                           private val bluePrintValidatorService: BluePrintValidatorService,
                                           private val blueprintModelRepository: BlueprintProcessorModelRepository)
    : BlueprintCatalogServiceImpl(bluePrintLoadConfiguration) {

    override fun saveToDataBase(extractedDirectory: File, id: String, archiveFile: File, checkValidity: Boolean?) {
        var valid = false
        val firstItem = BluePrintArchiveUtils.getFirstItemInDirectory(extractedDirectory)
        val blueprintBaseDirectory = extractedDirectory.absolutePath + "/" + firstItem
        // Validate Blueprint
        val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(id, blueprintBaseDirectory)

        // Check Validity of blueprint
        if (checkValidity!!) {
            valid = bluePrintValidatorService.validateBluePrints(bluePrintRuntimeService)
        }

        if ((valid && checkValidity!!) || (!valid && !checkValidity!!)) {
            val metaData = bluePrintRuntimeService.bluePrintContext().metadata!!
            // FIXME("Check Duplicate for Artifact Name and Artifact Version")
            val blueprintModel = BlueprintProcessorModel()
            blueprintModel.id = id
            blueprintModel.artifactType = ApplicationConstants.ASDC_ARTIFACT_TYPE_SDNC_MODEL
            blueprintModel.published = ApplicationConstants.ACTIVE_N
            blueprintModel.artifactName = metaData[BluePrintConstants.METADATA_TEMPLATE_NAME]
            blueprintModel.artifactVersion = metaData[BluePrintConstants.METADATA_TEMPLATE_VERSION]
            blueprintModel.updatedBy = metaData[BluePrintConstants.METADATA_TEMPLATE_AUTHOR]
            blueprintModel.tags = metaData[BluePrintConstants.METADATA_TEMPLATE_TAGS]
            blueprintModel.artifactDescription = "Controller Blueprint for ${blueprintModel.artifactName}:${blueprintModel.artifactVersion}"

            val blueprintModelContent = BlueprintProcessorModelContent()
            blueprintModelContent.id = id // For quick access both id's are same.always have one to one mapping.
            blueprintModelContent.contentType = "CBA_ZIP"
            blueprintModelContent.name = "${blueprintModel.artifactName}:${blueprintModel.artifactVersion}"
            blueprintModelContent.description = "(${blueprintModel.artifactName}:${blueprintModel.artifactVersion} CBA Zip Content"
            blueprintModelContent.content = Files.readAllBytes(archiveFile.toPath())

            // Set the Blueprint Model into blueprintModelContent
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
}
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

package org.onap.ccsdk.apps.controllerblueprints.service.load

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintArchiveUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintFileUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.apps.controllerblueprints.service.common.ApplicationConstants
import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModel
import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModelContent
import org.onap.ccsdk.apps.controllerblueprints.service.repository.BlueprintModelContentRepository
import org.onap.ccsdk.apps.controllerblueprints.service.repository.BlueprintModelRepository
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files

@Service
class BluePrintCatalogServiceImpl(private val bluePrintLoadConfiguration: BluePrintLoadConfiguration,
                                  private val bluePrintValidatorService: BluePrintValidatorService,
                                  private val blueprintModelContentRepository: BlueprintModelContentRepository,
                                  private val blueprintModelRepository: BlueprintModelRepository) : BluePrintCatalogService {

    override fun uploadToDataBase(file: String, validate: Boolean): String {
        // The file name provided here is unique as we transform to UUID before storing
        val blueprintFile = File(file)
        val fileName = blueprintFile.name
        val id = BluePrintFileUtils.stripFileExtension(fileName)
        // If the file is directory
        if (blueprintFile.isDirectory) {

            val zipFile = File("${bluePrintLoadConfiguration.blueprintArchivePath}/$fileName")
            // zip the directory
            BluePrintArchiveUtils.compress(blueprintFile, zipFile, true)

            // Upload to the Data Base
            saveToDataBase(blueprintFile, id, zipFile)

            // After Upload to Database delete the zip file
            zipFile.delete()

        } else {
            // If the file is ZIP
            // unzip the CBA file to validate before store in database
            val targetDir = "${bluePrintLoadConfiguration.blueprintDeployPath}/$id/"
            val extractedDirectory = BluePrintArchiveUtils.deCompress(blueprintFile, targetDir)

            // Upload to the Data Base
            saveToDataBase(extractedDirectory, id, blueprintFile)

            // After Upload to Database delete the zip file
            blueprintFile.delete()
            extractedDirectory.delete()
        }

        return id
    }

    override fun downloadFromDataBase(name: String, version: String, path: String): String {
        // If path ends with zip, then compress otherwise download as extracted folder

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun downloadFromDataBase(uuid: String, path: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prepareBluePrint(name: String, version: String): String {
        val preparedPath = "${bluePrintLoadConfiguration.blueprintDeployPath}/$name/$version"
        downloadFromDataBase(name, version, preparedPath)
        return preparedPath
    }

    private fun saveToDataBase(extractedDirectory: File, id: String, archiveFile: File, checkValidity: Boolean? = false) {
        // Upload to the Data Base
        //val id = "save-$uuid"
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
            val blueprintModel = BlueprintModel()
            blueprintModel.id = id
            blueprintModel.artifactType =  ApplicationConstants.ASDC_ARTIFACT_TYPE_SDNC_MODEL
            blueprintModel.published = ApplicationConstants.ACTIVE_N
            blueprintModel.artifactName = metaData[BluePrintConstants.METADATA_TEMPLATE_NAME]
            blueprintModel.artifactVersion = metaData[BluePrintConstants.METADATA_TEMPLATE_VERSION]
            blueprintModel.updatedBy = metaData[BluePrintConstants.METADATA_TEMPLATE_AUTHOR]
            blueprintModel.tags = metaData[BluePrintConstants.METADATA_TEMPLATE_TAGS]
            blueprintModel.artifactDescription = "Controller Blueprint for ${blueprintModel.artifactName}:${blueprintModel.artifactVersion}"

            val blueprintModelContent = BlueprintModelContent()
            blueprintModelContent.id = id // For quick access both id's are same.always have one to one mapping.
            blueprintModelContent.contentType = "CBA_ZIP"
            blueprintModelContent.name = "${blueprintModel.artifactName}:${blueprintModel.artifactVersion}"
            blueprintModelContent.description = "(${blueprintModel.artifactName}:${blueprintModel.artifactVersion} CBA Zip Content"
            blueprintModelContent.content = Files.readAllBytes(archiveFile.toPath())

            // Set the Blueprint Model into blueprintModelContent
            blueprintModelContent.blueprintModel = blueprintModel

            // Set the Blueprint Model Content into blueprintModel
            blueprintModel.blueprintModelContent = blueprintModelContent

            blueprintModelRepository.saveAndFlush(blueprintModel)
        }
    }
}
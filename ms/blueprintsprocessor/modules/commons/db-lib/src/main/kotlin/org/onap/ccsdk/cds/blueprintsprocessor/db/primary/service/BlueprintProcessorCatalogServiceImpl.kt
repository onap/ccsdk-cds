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

package org.onap.ccsdk.cds.blueprintsprocessor.db.primary.service

import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModel
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModelContent
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.repository.BlueprintModelRepository
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.common.ApplicationConstants
import org.onap.ccsdk.cds.controllerblueprints.core.config.BluePrintLoadConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.data.ErrorCode
import org.onap.ccsdk.cds.controllerblueprints.core.deCompress
import org.onap.ccsdk.cds.controllerblueprints.core.deleteNBDir
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.reCreateNBDirs
import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BluePrintCompileCache
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintFileUtils
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Path
import java.util.UUID

// TODO("Duplicate : Merge BlueprintProcessorCatalogServiceImpl and ControllerBlueprintCatalogServiceImpl")
/**
 * Similar/Duplicate implementation in [org.onap.ccsdk.cds.controllerblueprints.service.load.ControllerBlueprintCatalogServiceImpl]
 */
@Service("blueprintsProcessorCatalogService")
class BlueprintProcessorCatalogServiceImpl(
    bluePrintRuntimeValidatorService: BluePrintValidatorService,
    private val bluePrintLoadConfiguration: BluePrintLoadConfiguration,
    private val blueprintModelRepository: BlueprintModelRepository
) :
    BlueprintCatalogServiceImpl(bluePrintLoadConfiguration, bluePrintRuntimeValidatorService) {

    private val log = LoggerFactory.getLogger(BlueprintProcessorCatalogServiceImpl::class.toString())

    override suspend fun delete(name: String, version: String) {
        // Clean blueprint script cache
        val cacheKey = BluePrintFileUtils
            .compileCacheKey(normalizedPathName(bluePrintLoadConfiguration.blueprintDeployPath, name, version))
        BluePrintCompileCache.cleanClassLoader(cacheKey)
        log.info("removed cba file name($name), version($version) from cache")
        // Cleaning Deployed Blueprint
        deleteNBDir(bluePrintLoadConfiguration.blueprintDeployPath, name, version)
        log.info("removed cba file name($name), version($version) from deploy location")
        // Cleaning Data Base
        blueprintModelRepository
            .deleteByArtifactNameAndArtifactVersion(name, version)
        log.info("removed cba file name($name), version($version) from database")
    }

    override suspend fun get(name: String, version: String, extract: Boolean): Path? {

        val deployFile = normalizedFile(bluePrintLoadConfiguration.blueprintDeployPath, name, version)
        val cbaFile = normalizedFile(
            bluePrintLoadConfiguration.blueprintArchivePath,
            UUID.randomUUID().toString(), "cba.zip"
        )

        if (extract && deployFile.exists()) {
            log.info("cba file name($name), version($version) already present(${deployFile.absolutePath})")
        } else {
            deployFile.reCreateNBDirs()
            cbaFile.parentFile.reCreateNBDirs()

            try {
                log.info("getting cba file name($name), version($version) from db")
                blueprintModelRepository.findByArtifactNameAndArtifactVersion(name, version)?.also {
                    it.blueprintModelContent.run {

                        cbaFile.writeBytes(this!!.content!!)
                        cbaFile.deCompress(deployFile)
                        log.info("cba file name($name), version($version) saved in (${deployFile.absolutePath})")
                    }
                }

                check(deployFile.exists() && deployFile.list().isNotEmpty()) {
                    throw BluePrintProcessorException("file check failed")
                }
            } catch (e: Exception) {
                deleteNBDir(deployFile.absolutePath)
                throw BluePrintProcessorException(
                    "failed to get  get cba file name($name), version($version) from db" +
                        " : ${e.message}"
                )
            } finally {
                deleteNBDir(cbaFile.parentFile.absolutePath)
            }
        }

        return if (extract) {
            deployFile.toPath()
        } else {
            cbaFile.toPath()
        }
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
            val deployFile =
                normalizedPathName(bluePrintLoadConfiguration.blueprintDeployPath, artifactName, artifactVersion)

            val cacheKey = BluePrintFileUtils.compileCacheKey(deployFile)
            BluePrintCompileCache.cleanClassLoader(cacheKey)

            deleteNBDir(deployFile).let {
                if (it) log.info("Deleted deployed blueprint model :$artifactName::$artifactVersion")
                else log.info("Fail to delete deployed blueprint model :$artifactName::$artifactVersion")
            }
        }

        val blueprintModel = BlueprintModel()
        blueprintModel.id = metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID]
        blueprintModel.artifactType = ApplicationConstants.ASDC_ARTIFACT_TYPE_SDNC_MODEL
        blueprintModel.published = metadata[BluePrintConstants.PROPERTY_BLUEPRINT_VALID]
            ?: BluePrintConstants.FLAG_N
        blueprintModel.artifactName = artifactName
        blueprintModel.artifactVersion = artifactVersion
        blueprintModel.updatedBy = metadata[BluePrintConstants.METADATA_TEMPLATE_AUTHOR]!!
        blueprintModel.tags = metadata[BluePrintConstants.METADATA_TEMPLATE_TAGS]!!
        val description =
            if (null != metadata[BluePrintConstants.METADATA_TEMPLATE_DESCRIPTION]) metadata[BluePrintConstants.METADATA_TEMPLATE_DESCRIPTION] else ""
        blueprintModel.artifactDescription = description

        val blueprintModelContent = BlueprintModelContent()
        blueprintModelContent.id = metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID]
        blueprintModelContent.contentType = "CBA_ZIP"
        blueprintModelContent.name = "$artifactName:$artifactVersion"
        blueprintModelContent.description = "$artifactName:$artifactVersion CBA Zip Content"
        blueprintModelContent.content = archiveFile.readBytes()
        blueprintModelContent.blueprintModel = blueprintModel

        blueprintModel.blueprintModelContent = blueprintModelContent

        try {
            blueprintModelRepository.saveAndFlush(blueprintModel)
        } catch (ex: DataIntegrityViolationException) {
            throw BluePrintException(
                ErrorCode.CONFLICT_ADDING_RESOURCE.value,
                "The blueprint entry " +
                    "is already exist in database: ${ex.message}",
                ex
            )
        }
    }
}

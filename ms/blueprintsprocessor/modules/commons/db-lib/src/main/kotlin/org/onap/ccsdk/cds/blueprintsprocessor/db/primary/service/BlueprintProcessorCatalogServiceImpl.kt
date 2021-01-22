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

import org.onap.ccsdk.cds.blueprintsprocessor.core.cluster.BlueprintClusterTopic
import org.onap.ccsdk.cds.blueprintsprocessor.core.cluster.optionalClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModel
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModelContent
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.repository.BlueprintModelRepository
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.common.ApplicationConstants
import org.onap.ccsdk.cds.controllerblueprints.core.config.BlueprintLoadConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.data.ErrorCode
import org.onap.ccsdk.cds.controllerblueprints.core.deCompress
import org.onap.ccsdk.cds.controllerblueprints.core.deleteNBDir
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.reCreateNBDirs
import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BlueprintCompileCache
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintFileUtils
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
    bluePrintRuntimeValidatorService: BlueprintValidatorService,
    private val bluePrintLoadConfiguration: BlueprintLoadConfiguration,
    private val blueprintModelRepository: BlueprintModelRepository
) :
    BlueprintCatalogServiceImpl(bluePrintLoadConfiguration, bluePrintRuntimeValidatorService) {

    private val log = LoggerFactory.getLogger(BlueprintProcessorCatalogServiceImpl::class.toString())

    override suspend fun delete(name: String, version: String) {
        // Clean blueprint script cache
        val cacheKey = BlueprintFileUtils
            .compileCacheKey(normalizedPathName(bluePrintLoadConfiguration.blueprintDeployPath, name, version))
        cleanClassLoader(cacheKey)
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
                    throw BlueprintProcessorException("file check failed")
                }
            } catch (e: Exception) {
                deleteNBDir(deployFile.absolutePath)
                throw BlueprintProcessorException(
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
        val artifactName = metadata[BlueprintConstants.METADATA_TEMPLATE_NAME]
        val artifactVersion = metadata[BlueprintConstants.METADATA_TEMPLATE_VERSION]

        check(archiveFile.isFile && !archiveFile.isDirectory) {
            throw BlueprintException("Not a valid Archive file(${archiveFile.absolutePath})")
        }

        blueprintModelRepository.findByArtifactNameAndArtifactVersion(artifactName!!, artifactVersion!!)?.let {
            log.info("Overwriting blueprint model :$artifactName::$artifactVersion")
            blueprintModelRepository.deleteByArtifactNameAndArtifactVersion(artifactName, artifactVersion)
            val deployFile =
                normalizedPathName(bluePrintLoadConfiguration.blueprintDeployPath, artifactName, artifactVersion)

            val cacheKey = BlueprintFileUtils.compileCacheKey(deployFile)
            cleanClassLoader(cacheKey)

            deleteNBDir(deployFile).let {
                if (it) log.info("Deleted deployed blueprint model :$artifactName::$artifactVersion")
                else log.info("Fail to delete deployed blueprint model :$artifactName::$artifactVersion")
            }
        }

        val blueprintModel = BlueprintModel()
        blueprintModel.id = metadata[BlueprintConstants.PROPERTY_BLUEPRINT_PROCESS_ID]
        blueprintModel.artifactType = ApplicationConstants.ASDC_ARTIFACT_TYPE_SDNC_MODEL
        blueprintModel.published = metadata[BlueprintConstants.PROPERTY_BLUEPRINT_VALID]
            ?: BlueprintConstants.FLAG_N
        blueprintModel.artifactName = artifactName
        blueprintModel.artifactVersion = artifactVersion
        blueprintModel.updatedBy = metadata[BlueprintConstants.METADATA_TEMPLATE_AUTHOR]!!
        blueprintModel.tags = metadata[BlueprintConstants.METADATA_TEMPLATE_TAGS]!!
        val description =
            if (null != metadata[BlueprintConstants.METADATA_TEMPLATE_DESCRIPTION]) metadata[BlueprintConstants.METADATA_TEMPLATE_DESCRIPTION] else ""
        blueprintModel.artifactDescription = description

        val blueprintModelContent = BlueprintModelContent()
        blueprintModelContent.id = metadata[BlueprintConstants.PROPERTY_BLUEPRINT_PROCESS_ID]
        blueprintModelContent.contentType = "CBA_ZIP"
        blueprintModelContent.name = "$artifactName:$artifactVersion"
        blueprintModelContent.description = "$artifactName:$artifactVersion CBA Zip Content"
        blueprintModelContent.content = archiveFile.readBytes()
        blueprintModelContent.blueprintModel = blueprintModel

        blueprintModel.blueprintModelContent = blueprintModelContent

        try {
            blueprintModelRepository.saveAndFlush(blueprintModel)
        } catch (ex: DataIntegrityViolationException) {
            throw BlueprintException(
                ErrorCode.CONFLICT_ADDING_RESOURCE.value,
                "The blueprint entry " +
                    "is already exist in database: ${ex.message}",
                ex
            )
        }
    }

    private suspend fun cleanClassLoader(cacheKey: String) {
        val clusterService = BlueprintDependencyService.optionalClusterService()
        if (null == clusterService)
            BlueprintCompileCache.cleanClassLoader(cacheKey)
        else {
            log.info("Sending ClusterMessage: Clean Classloader Cache")
            clusterService.sendMessage(BlueprintClusterTopic.BLUEPRINT_CLEAN_COMPILER_CACHE, cacheKey)
        }
    }
}

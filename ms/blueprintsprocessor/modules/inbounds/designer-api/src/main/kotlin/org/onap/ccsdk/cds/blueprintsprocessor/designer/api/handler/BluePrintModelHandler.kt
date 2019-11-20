/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
 * Modifications Copyright © 2019 IBM.
 * Modifications Copyright © 2019 Orange.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler

import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModel
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModelSearch
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.repository.BlueprintModelContentRepository
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.repository.BlueprintModelRepository
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.repository.BlueprintModelSearchRepository
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.BootstrapRequest
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.load.BluePrintDatabaseLoadService
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils.BluePrintEnhancerUtils
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.config.BluePrintLoadConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.data.ErrorCode
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BluePrintCompileCache
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintFileUtils
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.util.*


/**
 * BlueprintModelHandler Purpose: Handler service to handle the request from BlurPrintModelRest
 *
 * @author Brinda Santh
 * @version 1.0
 */

@Service
open class BluePrintModelHandler(private val bluePrintDatabaseLoadService: BluePrintDatabaseLoadService,
                                 private val blueprintsProcessorCatalogService: BluePrintCatalogService,
                                 private val bluePrintLoadConfiguration: BluePrintLoadConfiguration,
                                 private val blueprintModelSearchRepository: BlueprintModelSearchRepository,
                                 private val blueprintModelRepository: BlueprintModelRepository,
                                 private val blueprintModelContentRepository: BlueprintModelContentRepository,
                                 private val bluePrintEnhancerService: BluePrintEnhancerService) {

    private val log = logger(BluePrintModelHandler::class)


    open suspend fun bootstrapBlueprint(bootstrapRequest: BootstrapRequest) {
        log.info("Bootstrap request with type load(${bootstrapRequest.loadModelType}), " +
                "resource dictionary load(${bootstrapRequest.loadResourceDictionary}) and " +
                "cba load(${bootstrapRequest.loadCBA})")
        if (bootstrapRequest.loadModelType) {
            bluePrintDatabaseLoadService.initModelTypes()
        }
        if (bootstrapRequest.loadResourceDictionary) {
            bluePrintDatabaseLoadService.initResourceDictionary()
        }
        if (bootstrapRequest.loadCBA) {
            bluePrintDatabaseLoadService.initBluePrintCatalog()
        }
    }

    /**
     * This is a getAllBlueprintModel method to retrieve all the BlueprintModel in Database
     *
     * @return List<BlueprintModelSearch> list of the controller blueprint archives
    </BlueprintModelSearch> */
    open fun allBlueprintModel(): List<BlueprintModelSearch> {
        return blueprintModelSearchRepository.findAll()
    }

    /**
     * This is a getAllBlueprintModel method to retrieve all the BlueprintModel in Database
     *
     * @return List<BlueprintModelSearch> list of the controller blueprint archives
    </BlueprintModelSearch> */
    open fun allBlueprintModel(pageRequest: Pageable): Page<BlueprintModelSearch> {
        return blueprintModelSearchRepository.findAll(pageRequest)
    }

    /**
     * This is a saveBlueprintModel method
     *
     * @param filePart filePart
     * @return Mono<BlueprintModelSearch>
     * @throws BluePrintException BluePrintException
    </BlueprintModelSearch> */
    @Throws(BluePrintException::class)
    open suspend fun saveBlueprintModel(filePart: FilePart): BlueprintModelSearch {
        try {
            return upload(filePart, false)
        } catch (e: IOException) {
            throw BluePrintException(ErrorCode.IO_FILE_INTERRUPT.value,
                    "Error in Save CBA: ${e.message}", e)
        }
    }


    /**
     * This is a searchBlueprintModels method
     *
     * @param tags tags
     * @return List<BlueprintModelSearch>
    </BlueprintModelSearch> */
    open fun searchBlueprintModels(tags: String): List<BlueprintModelSearch> {
        return blueprintModelSearchRepository.findByTagsContainingIgnoreCase(tags)
    }

    /**
     * This is a getBlueprintModelSearchByNameAndVersion method
     *
     * @param name name
     * @param version version
     * @return BlueprintModelSearch
     * @throws BluePrintException BluePrintException
     */
    @Throws(BluePrintException::class)
    open fun getBlueprintModelSearchByNameAndVersion(name: String, version: String): BlueprintModelSearch {
        return blueprintModelSearchRepository.findByArtifactNameAndArtifactVersion(name, version)
                ?: throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value,
                        String.format(BLUEPRINT_MODEL_NAME_VERSION_FAILURE_MSG, name, version))

    }

    /**
     * This is a downloadBlueprintModelFileByNameAndVersion method to download a Blueprint by Name and Version
     *
     * @param name name
     * @param version version
     * @return ResponseEntity<Resource>
     * @throws BluePrintException BluePrintException
    </Resource> */
    @Throws(BluePrintException::class)
    open fun downloadBlueprintModelFileByNameAndVersion(name: String,
                                                        version: String): ResponseEntity<Resource> {
        try {
            val archiveByteArray = download(name, version)
            val fileName = "${name}_$version.zip"
            return prepareResourceEntity(fileName, archiveByteArray)
        } catch (e: BluePrintException) {
            throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value,
                    String.format("Error while " + "downloading the CBA file: %s", e.message), e)
        }
    }

    /**
     * This is a downloadBlueprintModelFile method to find the target file to download and return a file resource
     *
     * @return ResponseEntity<Resource>
     * @throws BluePrintException BluePrintException
    </Resource> */
    @Throws(BluePrintException::class)
    open fun downloadBlueprintModelFile(id: String): ResponseEntity<Resource> {
        val blueprintModel: BlueprintModel
        try {
            blueprintModel = getBlueprintModel(id)
        } catch (e: BluePrintException) {
            throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value, String.format("Error while " + "downloading the CBA file: %s", e.message), e)
        }

        val fileName = "${blueprintModel.artifactName}_${blueprintModel.artifactVersion}.zip"
        val file = blueprintModel.blueprintModelContent?.content
                ?: throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value,
                        String.format("Error while downloading the CBA file: couldn't get model content"))
        return prepareResourceEntity(fileName, file)
    }

    /**
     * @return ResponseEntity<Resource>
    </Resource> */
    private fun prepareResourceEntity(fileName: String, file: ByteArray): ResponseEntity<Resource> {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/plain"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .body(ByteArrayResource(file))
    }

    /**
     * This is a getBlueprintModel method
     *
     * @param id id
     * @return BlueprintModel
     * @throws BluePrintException BluePrintException
     */
    @Throws(BluePrintException::class)
    open fun getBlueprintModel(id: String): BlueprintModel {
        val blueprintModel: BlueprintModel
        val dbBlueprintModel = blueprintModelRepository.findById(id)
        if (dbBlueprintModel.isPresent) {
            blueprintModel = dbBlueprintModel.get()
        } else {
            val msg = String.format(BLUEPRINT_MODEL_ID_FAILURE_MSG, id)
            throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value, msg)
        }
        return blueprintModel
    }

    /**
     * This is a getBlueprintModelByNameAndVersion method
     *
     * @param name name
     * @param version version
     * @return BlueprintModel
     * @throws BluePrintException BluePrintException
     */
    @Throws(BluePrintException::class)
    open fun getBlueprintModelByNameAndVersion(name: String, version: String): BlueprintModel {
        val blueprintModel = blueprintModelRepository
                .findByArtifactNameAndArtifactVersion(name, version)
        if (blueprintModel != null) {
            return blueprintModel
        } else {
            val msg = String.format(BLUEPRINT_MODEL_NAME_VERSION_FAILURE_MSG, name, version)
            throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value, msg)
        }
    }

    /**
     * This is a getBlueprintModelSearch method
     *
     * @param id id
     * @return BlueprintModelSearch
     * @throws BluePrintException BluePrintException
     */
    @Throws(BluePrintException::class)
    open fun getBlueprintModelSearch(id: String): BlueprintModelSearch {
        return blueprintModelSearchRepository.findById(id)
                ?: throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value,
                        String.format(BLUEPRINT_MODEL_ID_FAILURE_MSG, id))
    }

    /**
     * This is a searchBluePrintModelsByKeyWord method to retrieve specific  BlueprintModel in Database
     * where keyword equals updatedBy or tags or artifcat name or artifcat version or artifact type
     * @author Shaaban Ebrahim
     * @param keyWord
     *
     * @return List<BlueprintModelSearch> list of the controller blueprint
    </BlueprintModelSearch> */
    open fun searchBluePrintModelsByKeyWord(keyWord: String): List<BlueprintModelSearch> {
        return blueprintModelSearchRepository.findByUpdatedByOrTagsOrOrArtifactNameOrOrArtifactVersionOrArtifactType(
                keyWord, keyWord, keyWord, keyWord, keyWord)
    }


    /**
     * This is a searchBluePrintModelsByKeyWordPagebale method to retrieve specific  BlueprintModel in Database
     * where keyword equals updatedBy or tags or artifcat name or artifcat version or artifact type and pageable
     * @author Shaaban Ebrahim
     * @param keyWord
     *
     * @return List<BlueprintModelSearch> list of the controller blueprint
    </BlueprintModelSearch> */
    open fun searchBluePrintModelsByKeyWordPaged(keyWord: String, pageRequest: PageRequest): Page<BlueprintModelSearch> {
        return blueprintModelSearchRepository.findByUpdatedByOrTagsOrOrArtifactNameOrOrArtifactVersionOrArtifactType(keyWord, keyWord, keyWord, keyWord, keyWord, pageRequest)
    }

    /**
     * This is a deleteBlueprintModel method
     *
     * @param id id
     * @throws BluePrintException BluePrintException
     */
    @Transactional
    @Throws(BluePrintException::class)
    open fun deleteBlueprintModel(id: String) {
        val dbBlueprintModel = blueprintModelRepository.findById(id)
        if (dbBlueprintModel != null && dbBlueprintModel.isPresent) {
            blueprintModelContentRepository.deleteByBlueprintModel(dbBlueprintModel.get())
            blueprintModelRepository.delete(dbBlueprintModel.get())
        } else {
            val msg = String.format(BLUEPRINT_MODEL_ID_FAILURE_MSG, id)
            throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value, msg)
        }
    }

    open suspend fun deleteBlueprintModel(name: String, version: String) {
        blueprintsProcessorCatalogService.deleteFromDatabase(name, version)
    }

    /**
     * This is a CBA enrichBlueprint method
     * Save the Zip File in archive location and extract the cba content.
     * Populate the Enhancement Location
     * Enhance the CBA content
     * Compress the Enhanced Content
     * Return back the the compressed content back to the caller.
     *
     * @param filePart filePart
     * @return ResponseEntity<Resource>
     * @throws BluePrintException BluePrintException
     */
    @Throws(BluePrintException::class)
    open suspend fun enrichBlueprint(filePart: FilePart): ResponseEntity<Resource> {
        try {
            val enhancedByteArray = enrichBlueprintFileSource(filePart)
            return BluePrintEnhancerUtils.prepareResourceEntity("enhanced-cba.zip", enhancedByteArray)
        } catch (e: IOException) {
            throw BluePrintException(ErrorCode.IO_FILE_INTERRUPT.value,
                    "Error in Enriching CBA: ${e.message}", e)
        }
    }

    /**
     * This is a publishBlueprintModel method to change the status published to YES
     *
     * @param filePart filePart
     * @return BlueprintModelSearch
     * @throws BluePrintException BluePrintException
     */
    @Throws(BluePrintException::class)
    open suspend fun publishBlueprint(filePart: FilePart): BlueprintModelSearch {
        try {
            return upload(filePart, true)
        } catch (e: Exception) {
            throw BluePrintException(ErrorCode.IO_FILE_INTERRUPT.value,
                    "Error in Publishing CBA: ${e.message}", e)
        }
    }

    /** Common CBA Save and Publish function for RestController and GRPC Handler, the [fileSource] may be
     * byteArray or File Part type.*/
    open suspend fun upload(fileSource: Any, validate: Boolean): BlueprintModelSearch {
        val saveId = UUID.randomUUID().toString()
        val blueprintArchive = normalizedPathName(bluePrintLoadConfiguration.blueprintArchivePath, saveId)
        val blueprintWorking = normalizedPathName(bluePrintLoadConfiguration.blueprintWorkingPath, saveId)
        try {
            val compressedFile = normalizedFile(blueprintArchive, "cba.zip")
            when (fileSource) {
                is FilePart -> BluePrintEnhancerUtils.filePartAsFile(fileSource, compressedFile)
                is ByteArray -> BluePrintEnhancerUtils.byteArrayAsFile(fileSource, compressedFile)
            }
            // Save the Copied file to Database
            val blueprintId = blueprintsProcessorCatalogService.saveToDatabase(saveId, compressedFile, validate)

            return blueprintModelSearchRepository.findById(blueprintId)
                    ?: throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value,
                            String.format(BLUEPRINT_MODEL_ID_FAILURE_MSG, blueprintId))

        } catch (e: IOException) {
            throw BluePrintException(ErrorCode.IO_FILE_INTERRUPT.value,
                    "Error in Upload CBA: ${e.message}", e)
        } finally {
            // Clean blueprint script cache
            val cacheKey = BluePrintFileUtils
                    .compileCacheKey(normalizedPathName(bluePrintLoadConfiguration.blueprintWorkingPath, saveId))
            BluePrintCompileCache.cleanClassLoader(cacheKey)
            deleteNBDir(blueprintArchive)
            deleteNBDir(blueprintWorking)
        }
    }

    /** Common CBA download function for RestController and GRPC Handler, the [fileSource] may be
     * byteArray or File Part type.*/
    open fun download(name: String, version: String): ByteArray {
        try {
            val blueprintModel = getBlueprintModelByNameAndVersion(name, version)
            return blueprintModel.blueprintModelContent?.content
                    ?: throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value,
                            String.format("Error while downloading the CBA file: couldn't get model content"))
        } catch (e: BluePrintException) {
            throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value,
                    String.format("Error while " + "downloading the CBA file: %s", e.message), e)
        }
    }

    /** Common CBA Enrich function for RestController and GRPC Handler, the [fileSource] may be
     * byteArray or File Part type.*/
    open suspend fun enrichBlueprintFileSource(fileSource: Any): ByteArray {
        val enhanceId = UUID.randomUUID().toString()
        val blueprintArchive = normalizedPathName(bluePrintLoadConfiguration.blueprintArchivePath, enhanceId)
        val blueprintWorkingDir = normalizedPathName(bluePrintLoadConfiguration.blueprintWorkingPath, enhanceId)
        try {
            when (fileSource) {
                is FilePart -> BluePrintEnhancerUtils
                        .copyFilePartToEnhanceDir(fileSource, blueprintArchive, blueprintWorkingDir)
                is ByteArray -> BluePrintEnhancerUtils
                        .copyByteArrayToEnhanceDir(fileSource, blueprintArchive, blueprintWorkingDir)
            }            // Enhance the Blue Prints
            bluePrintEnhancerService.enhance(blueprintWorkingDir)

            return BluePrintEnhancerUtils.compressEnhanceDirAndReturnByteArray(blueprintWorkingDir, blueprintArchive)

        } catch (e: IOException) {
            throw BluePrintException(ErrorCode.IO_FILE_INTERRUPT.value,
                    "Error in Enriching CBA: ${e.message}", e)
        } finally {
            BluePrintEnhancerUtils.cleanEnhancer(blueprintArchive, blueprintWorkingDir)
        }
    }

    companion object {

        private const val BLUEPRINT_MODEL_ID_FAILURE_MSG = "failed to get blueprint model id(%s) from repo"
        private const val BLUEPRINT_MODEL_NAME_VERSION_FAILURE_MSG = "failed to get blueprint model by name(%s)" + " and version(%s) from repo"
    }
}

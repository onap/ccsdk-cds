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

package org.onap.ccsdk.apps.controllerblueprints.service.handler

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.config.BluePrintLoadConfiguration
import org.onap.ccsdk.apps.controllerblueprints.core.data.ErrorCode
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintFileUtils
import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModel
import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModelSearch
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ControllerBlueprintModelContentRepository
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ControllerBlueprintModelRepository
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ControllerBlueprintModelSearchRepository
import org.onap.ccsdk.apps.controllerblueprints.service.utils.BluePrintEnhancerUtils
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.io.File
import java.io.IOException
import java.util.*

/**
 * BlueprintModelHandler Purpose: Handler service to handle the request from BlurPrintModelRest
 *
 * @author Brinda Santh
 * @version 1.0
 */

@Service
open class BluePrintModelHandler(private val bluePrintCatalogService: BluePrintCatalogService,
                                 private val bluePrintLoadConfiguration: BluePrintLoadConfiguration,
                                 private val blueprintModelSearchRepository: ControllerBlueprintModelSearchRepository,
                                 private val blueprintModelRepository: ControllerBlueprintModelRepository,
                                 private val blueprintModelContentRepository: ControllerBlueprintModelContentRepository,
                                 private val bluePrintEnhancerService: BluePrintEnhancerService) {

    /**
     * This is a getAllBlueprintModel method to retrieve all the BlueprintModel in Database
     *
     * @return List<BlueprintModelSearch> list of the controller blueprint archives
    </BlueprintModelSearch> */
    open fun allBlueprintModel(): List<BlueprintModelSearch> {
        return blueprintModelSearchRepository.findAll()
    }

    /**
     * This is a saveBlueprintModel method
     *
     * @param filePart filePart
     * @return Mono<BlueprintModelSearch>
     * @throws BluePrintException BluePrintException
    </BlueprintModelSearch> */
    @Throws(BluePrintException::class)
    open fun saveBlueprintModel(filePart: FilePart): Mono<BlueprintModelSearch> {
        try {
            val cbaLocation = BluePrintFileUtils.getCbaStorageDirectory(bluePrintLoadConfiguration.blueprintArchivePath)
            return BluePrintEnhancerUtils.saveCBAFile(filePart, cbaLocation).map { fileName ->
                var blueprintId: String? = null
                try {
                    blueprintId = bluePrintCatalogService.saveToDatabase(cbaLocation.resolve(fileName).toFile(), false)
                } catch (e: BluePrintException) {
                    // FIXME handle expection
                }
                blueprintModelSearchRepository.findById(blueprintId!!).get()
            }
        } catch (e: IOException) {
            throw BluePrintException(ErrorCode.IO_FILE_INTERRUPT.value,
                    String.format("I/O Error while uploading the CBA file: %s", e.message), e)
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
        val blueprintModelSearch: BlueprintModelSearch
        val dbBlueprintModel = blueprintModelSearchRepository
                .findByArtifactNameAndArtifactVersion(name, version)
        if (dbBlueprintModel.isPresent) {
            blueprintModelSearch = dbBlueprintModel.get()
        } else {
            throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value,
                    String.format(BLUEPRINT_MODEL_NAME_VERSION_FAILURE_MSG, name, version))
        }
        return blueprintModelSearch
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
        val blueprintModel: BlueprintModel
        try {
            blueprintModel = getBlueprintModelByNameAndVersion(name, version)
        } catch (e: BluePrintException) {
            throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value, String.format("Error while " + "downloading the CBA file: %s", e.message), e)
        }

        val fileName = blueprintModel.id + ".zip"
        val file = blueprintModel.blueprintModelContent.content
        return prepareResourceEntity(fileName, file)
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

        val fileName = blueprintModel.id + ".zip"
        val file = blueprintModel.blueprintModelContent.content
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
        val blueprintModelSearch: BlueprintModelSearch
        val dbBlueprintModel = blueprintModelSearchRepository.findById(id)
        if (dbBlueprintModel.isPresent) {
            blueprintModelSearch = dbBlueprintModel.get()
        } else {
            val msg = String.format(BLUEPRINT_MODEL_ID_FAILURE_MSG, id)
            throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value, msg)
        }

        return blueprintModelSearch
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
        if (dbBlueprintModel.isPresent) {
            blueprintModelContentRepository.deleteByBlueprintModel(dbBlueprintModel.get())
            blueprintModelRepository.delete(dbBlueprintModel.get())
        } else {
            val msg = String.format(BLUEPRINT_MODEL_ID_FAILURE_MSG, id)
            throw BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.value, msg)
        }
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
        val enhanceId = UUID.randomUUID().toString()
        val blueprintArchive = normalizedPathName(bluePrintLoadConfiguration.blueprintArchivePath, enhanceId)
        val blueprintEnrichmentDir = normalizedPathName(bluePrintLoadConfiguration.blueprintEnrichmentPath, enhanceId)
        try {
            BluePrintEnhancerUtils.decompressFilePart(filePart, blueprintArchive, blueprintEnrichmentDir)

            // Enhance the Blue Prints
            bluePrintEnhancerService.enhance(blueprintEnrichmentDir)

            return BluePrintEnhancerUtils.compressToFilePart(blueprintEnrichmentDir, blueprintArchive)

        } catch (e: IOException) {
            throw BluePrintException(ErrorCode.IO_FILE_INTERRUPT.value,
                    "Error in Enriching CBA: ${e.message}", e)
        } finally {
            BluePrintEnhancerUtils.cleanEnhancer(blueprintArchive, blueprintEnrichmentDir)
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
        val publishId = UUID.randomUUID().toString()
        val blueprintArchive = bluePrintLoadConfiguration.blueprintArchivePath.plus(File.separator).plus(publishId)
        val blueprintEnrichmentDir = bluePrintLoadConfiguration.blueprintEnrichmentPath.plus(File.separator).plus(publishId)
        try {
            val compressedFilePart = BluePrintEnhancerUtils
                    .extractCompressFilePart(filePart, blueprintArchive, blueprintEnrichmentDir)

            val blueprintId = bluePrintCatalogService.saveToDatabase(compressedFilePart, true)

            return blueprintModelSearchRepository.findById(blueprintId).get()

        } catch (e: Exception) {
            throw BluePrintException(ErrorCode.IO_FILE_INTERRUPT.value,
                    "Error in Publishing CBA: ${e.message}", e)
        } finally {
            BluePrintEnhancerUtils.cleanEnhancer(blueprintArchive, blueprintEnrichmentDir)
        }
    }

    companion object {

        private const val BLUEPRINT_MODEL_ID_FAILURE_MSG = "failed to get blueprint model id(%s) from repo"
        private const val BLUEPRINT_MODEL_NAME_VERSION_FAILURE_MSG = "failed to get blueprint model by name(%s)" + " and version(%s) from repo"
    }
}

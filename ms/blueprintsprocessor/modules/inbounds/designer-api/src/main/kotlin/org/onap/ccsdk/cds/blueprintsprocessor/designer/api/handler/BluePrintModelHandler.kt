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
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.DesignerApiDomains
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.WorkFlowData
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.WorkFlowSpecRequest
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.WorkFlowSpecResponse
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.WorkFlowsResponse
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.load.BluePrintDatabaseLoadService
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils.BluePrintEnhancerUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.config.BluePrintLoadConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.deleteNBDir
import org.onap.ccsdk.cds.controllerblueprints.core.httpProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BluePrintCompileCache
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.updateErrorMessage
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintFileUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.onap.ccsdk.cds.error.catalog.core.utils.errorCauseOrDefault
import org.onap.ccsdk.cds.error.catalog.core.utils.errorMessageOrDefault
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
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * BlueprintModelHandler Purpose: Handler service to handle the request from BlurPrintModelRest
 *
 * @author Brinda Santh
 * @version 1.0
 */

@Service
open class BluePrintModelHandler(
    private val bluePrintDatabaseLoadService: BluePrintDatabaseLoadService,
    private val blueprintsProcessorCatalogService: BluePrintCatalogService,
    private val bluePrintLoadConfiguration: BluePrintLoadConfiguration,
    private val blueprintModelSearchRepository: BlueprintModelSearchRepository,
    private val blueprintModelRepository: BlueprintModelRepository,
    private val blueprintModelContentRepository: BlueprintModelContentRepository,
    private val bluePrintEnhancerService: BluePrintEnhancerService
) {

    private val log = logger(BluePrintModelHandler::class)

    open suspend fun bootstrapBlueprint(bootstrapRequest: BootstrapRequest) {
        log.info(
            "Bootstrap request with type load(${bootstrapRequest.loadModelType}), " +
                "resource dictionary load(${bootstrapRequest.loadResourceDictionary}) and " +
                "cba load(${bootstrapRequest.loadCBA})"
        )
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

    @Throws(BluePrintException::class)
    open suspend fun prepareWorkFlowSpec(req: WorkFlowSpecRequest):
        WorkFlowSpecResponse {
            val basePath = blueprintsProcessorCatalogService.getFromDatabase(
                req
                    .blueprintName,
                req.version
            )
            log.info("blueprint base path $basePath")

            val blueprintContext = BluePrintMetadataUtils.getBluePrintContext(basePath.toString())
            val workFlow = blueprintContext.workflowByName(req.workflowName)

            val wfRes = WorkFlowSpecResponse()
            wfRes.blueprintName = req.blueprintName
            wfRes.version = req.version

            val workFlowData = WorkFlowData()
            workFlowData.workFlowName = req.workflowName
            workFlowData.inputs = workFlow.inputs
            workFlowData.outputs = workFlow.outputs
            wfRes.workFlowData = workFlowData

            if (workFlow.inputs != null) {
                for ((k, v) in workFlow.inputs!!) {
                    addPropertyInfo(k, v, blueprintContext, wfRes)
                }
            }

            if (workFlow.outputs != null) {
                for ((k, v) in workFlow.outputs!!) {
                    addPropertyInfo(k, v, blueprintContext, wfRes)
                }
            }

            return wfRes
        }

    private fun addPropertyInfo(propName: String, prop: PropertyDefinition, ctx: BluePrintContext, res: WorkFlowSpecResponse) {
        updatePropertyInfo(propName, prop, ctx, res)
        addDataType(prop.type, ctx, res)
        if (prop.entrySchema != null && prop.entrySchema!!.type != null) {
            addDataType(prop.entrySchema!!.type, ctx, res)
        }
    }

    private fun updatePropertyInfo(name: String, prop: PropertyDefinition, ctx: BluePrintContext, res: WorkFlowSpecResponse) {
        if (prop.inputparam == null || prop.inputparam == false) {
            var workflow = ctx.workflowByName(res.workFlowData.workFlowName)
            for ((k, v) in workflow.steps!!) {
                var arts = ctx.nodeTemplateArtifacts(v.target!!)
                if (arts != null) {
                    for ((k, v) in arts.entries!!) {
                        if (v.type == "artifact-mapping-resource") {
                            val file: String = v.file
                            val completePath = ctx.rootPath.plus(File.separator).plus(file)
                            val resourceAssignment = JacksonUtils.getListFromFile(completePath, ResourceAssignment::class.java)
                            for (res in resourceAssignment) {
                                if (res.name == name && res.inputParameter) {
                                    prop.inputparam = true
                                    return
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addDataType(name: String, ctx: BluePrintContext, res: WorkFlowSpecResponse) {
        var data = ctx.dataTypeByName(name)
        if (data != null) {
            res.dataTypes?.put(name, data)
            addParentDataType(data, ctx, res)
        }
    }

    private fun addParentDataType(data: DataType, ctx: BluePrintContext, res: WorkFlowSpecResponse) {
        if (data.properties != null) {
            for ((k, v) in data.properties!!) {
                addPropertyInfo(k, v, ctx, res)
            }
        }
    }

    @Throws(BluePrintException::class)
    open suspend fun getWorkflowNames(name: String, version: String): WorkFlowsResponse {
        val basePath = blueprintsProcessorCatalogService.getFromDatabase(
            name, version
        )
        log.info("blueprint base path $basePath")

        var res = WorkFlowsResponse()
        res.blueprintName = name
        res.version = version

        val blueprintContext = BluePrintMetadataUtils.getBluePrintContext(
            basePath.toString()
        )
        if (blueprintContext.workflows() != null) {
            res.workflows = blueprintContext.workflows()!!.keys
        }
        return res
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
            throw httpProcessorException(
                ErrorCatalogCodes.IO_FILE_INTERRUPT, DesignerApiDomains.DESIGNER_API,
                "Error in Save CBA: ${e.message}", e.errorCauseOrDefault()
            )
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
    open fun getBlueprintModelSearchByNameAndVersion(name: String, version: String): BlueprintModelSearch? {
        return blueprintModelSearchRepository.findByArtifactNameAndArtifactVersion(name, version)
        /*?: throw BluePrintException(
            ErrorCode.RESOURCE_NOT_FOUND.value,
            String.format(BLUEPRINT_MODEL_NAME_VERSION_FAILURE_MSG, name, version)
        )*/
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
    open fun downloadBlueprintModelFileByNameAndVersion(
        name: String,
        version: String
    ): ResponseEntity<Resource> {
        try {
            val archiveByteArray = download(name, version)
            val fileName = "${name}_$version.zip"
            return prepareResourceEntity(fileName, archiveByteArray)
        } catch (e: BluePrintProcessorException) {
            e.http(ErrorCatalogCodes.RESOURCE_NOT_FOUND)
            val errorMsg = "Error while downloading the CBA file by Blueprint Name ($name) and Version ($version)."
            throw e.updateErrorMessage(
                DesignerApiDomains.DESIGNER_API, errorMsg,
                "Wrong resource definition or resolution failed."
            )
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
            throw httpProcessorException(
                ErrorCatalogCodes.RESOURCE_NOT_FOUND, DesignerApiDomains.DESIGNER_API,
                "Error while downloading the CBA file: couldn't get blueprint modelby ID ($id)",
                e.errorCauseOrDefault()
            )
        }

        val fileName = "${blueprintModel.artifactName}_${blueprintModel.artifactVersion}.zip"
        val file = blueprintModel.blueprintModelContent?.content
            ?: throw httpProcessorException(
                ErrorCatalogCodes.RESOURCE_NOT_FOUND, DesignerApiDomains.DESIGNER_API,
                "Error while downloading the CBA file: couldn't get model content"
            )
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
            throw httpProcessorException(ErrorCatalogCodes.RESOURCE_NOT_FOUND, DesignerApiDomains.DESIGNER_API, msg)
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
            throw httpProcessorException(ErrorCatalogCodes.RESOURCE_NOT_FOUND, DesignerApiDomains.DESIGNER_API, msg)
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
            ?: throw httpProcessorException(
                ErrorCatalogCodes.RESOURCE_NOT_FOUND, DesignerApiDomains.DESIGNER_API,
                String.format(BLUEPRINT_MODEL_ID_FAILURE_MSG, id)
            )
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
            keyWord, keyWord, keyWord, keyWord, keyWord
        )
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
        return blueprintModelSearchRepository.findByUpdatedByContainingIgnoreCaseOrTagsContainingIgnoreCaseOrArtifactNameContainingIgnoreCaseOrArtifactVersionContainingIgnoreCaseOrArtifactTypeContainingIgnoreCase(
            keyWord,
            keyWord,
            keyWord,
            keyWord,
            keyWord,
            pageRequest
        )
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
            throw httpProcessorException(ErrorCatalogCodes.RESOURCE_NOT_FOUND, DesignerApiDomains.DESIGNER_API, msg)
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
        } catch (e: BluePrintProcessorException) {
            e.http(ErrorCatalogCodes.IO_FILE_INTERRUPT)
            val errorMsg = "Error while enhancing the CBA package."
            throw e.updateErrorMessage(
                DesignerApiDomains.DESIGNER_API, errorMsg,
                "Wrong CBA file provided, please verify and enrich Again."
            )
        } catch (e: Exception) {
            throw httpProcessorException(
                ErrorCatalogCodes.IO_FILE_INTERRUPT, DesignerApiDomains.DESIGNER_API,
                "EnrichBlueprint: ${e.message}", e.errorCauseOrDefault()
            )
        }
    }

    /**
     * This is a publishBlueprintModel method to change the status published to YES
     * NOTE: this method is meant for enriched blueprints only.
     *
     * @param filePart filePart
     * @return BlueprintModelSearch
     * @throws BluePrintException BluePrintException
     */
    @Throws(BluePrintException::class)
    open suspend fun publishBlueprint(filePart: FilePart): BlueprintModelSearch {
        try {
            return upload(filePart, true)
        } catch (e: BluePrintProcessorException) {
            e.http(ErrorCatalogCodes.IO_FILE_INTERRUPT)
            val errorMsg = "Error in Publishing CBA."
            throw e.updateErrorMessage(
                DesignerApiDomains.DESIGNER_API, errorMsg,
                "Wrong CBA provided, please verify and enrich your CBA."
            )
        } catch (e: Exception) {
            throw httpProcessorException(
                ErrorCatalogCodes.IO_FILE_INTERRUPT, DesignerApiDomains.DESIGNER_API,
                "Error in Publishing CBA: ${e.message}", e.errorCauseOrDefault()
            )
        }
    }

    /**
     * Enrich and publish the blueprint.
     * NOTE: this method is meant for the unenriched vs publishBlueprint(filePart)
     *       which is used for enriched blueprints.
     *
     * @param filePart filePart
     * @return BlueprintModelSearch
     * @throws BluePrintException BluePrintException
     */
    @Throws(BluePrintException::class)
    open suspend fun enrichAndPublishBlueprint(filePart: FilePart): BlueprintModelSearch {
        try {
            val enhancedByteArray = enrichBlueprintFileSource(filePart)
            return upload(enhancedByteArray, true)
        } catch (e: BluePrintProcessorException) {
            e.http(ErrorCatalogCodes.IO_FILE_INTERRUPT)
            val errorMsg = "Error while enhancing and uploading the CBA package."
            throw e.updateErrorMessage(
                DesignerApiDomains.DESIGNER_API, errorMsg,
                "Wrong CBA file provided, please verify the source CBA."
            )
        } catch (e: Exception) {
            throw httpProcessorException(
                ErrorCatalogCodes.IO_FILE_INTERRUPT, DesignerApiDomains.DESIGNER_API,
                "EnrichBlueprint: ${e.message}", e.errorCauseOrDefault()
            )
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
                ?: throw httpProcessorException(
                    ErrorCatalogCodes.RESOURCE_NOT_FOUND, DesignerApiDomains.DESIGNER_API,
                    String.format(BLUEPRINT_MODEL_ID_FAILURE_MSG, blueprintId)
                )
        } catch (e: BluePrintException) {
            e.http(ErrorCatalogCodes.IO_FILE_INTERRUPT)
            val errorMsg = "Error in Upload CBA."
            throw e.updateErrorMessage(
                DesignerApiDomains.DESIGNER_API, errorMsg,
                "Wrong enriched CBA."
            )
        } catch (e: IOException) {
            throw httpProcessorException(
                ErrorCatalogCodes.IO_FILE_INTERRUPT, DesignerApiDomains.DESIGNER_API,
                "Error in Upload CBA: ${e.errorMessageOrDefault()}", e.errorCauseOrDefault()
            )
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
                ?: throw httpProcessorException(
                    ErrorCatalogCodes.RESOURCE_NOT_FOUND, DesignerApiDomains.DESIGNER_API,
                    "Error while downloading the CBA file: couldn't get model content"
                )
        } catch (e: BluePrintException) {
            e.http(ErrorCatalogCodes.RESOURCE_NOT_FOUND)
            val errorMsg = "Fail to get Blueprint Model content."
            throw e.updateErrorMessage(
                DesignerApiDomains.DESIGNER_API, errorMsg,
                "Wrong name and version was provide."
            )
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
                is FilePart ->
                    BluePrintEnhancerUtils
                        .copyFilePartToEnhanceDir(fileSource, blueprintArchive, blueprintWorkingDir)
                is ByteArray ->
                    BluePrintEnhancerUtils
                        .copyByteArrayToEnhanceDir(fileSource, blueprintArchive, blueprintWorkingDir)
            } // Enhance the Blue Prints
            bluePrintEnhancerService.enhance(blueprintWorkingDir)

            return BluePrintEnhancerUtils.compressEnhanceDirAndReturnByteArray(blueprintWorkingDir, blueprintArchive)
        } catch (e: BluePrintException) {
            e.http(ErrorCatalogCodes.IO_FILE_INTERRUPT)
            val errorMsg = "Fail Enriching the CBA."
            throw e.updateErrorMessage(DesignerApiDomains.DESIGNER_API, errorMsg)
        } catch (e: IOException) {
            throw httpProcessorException(
                ErrorCatalogCodes.IO_FILE_INTERRUPT, DesignerApiDomains.DESIGNER_API,
                "Error while Enriching the CBA file.", e.errorCauseOrDefault()
            )
        } finally {
            BluePrintEnhancerUtils.cleanEnhancer(blueprintArchive, blueprintWorkingDir)
        }
    }

    companion object {

        private const val BLUEPRINT_MODEL_ID_FAILURE_MSG = "failed to get blueprint model id(%s) from repo"
        private const val BLUEPRINT_MODEL_NAME_VERSION_FAILURE_MSG = "failed to get blueprint model by name(%s)" + " and version(%s) from repo"
    }
}

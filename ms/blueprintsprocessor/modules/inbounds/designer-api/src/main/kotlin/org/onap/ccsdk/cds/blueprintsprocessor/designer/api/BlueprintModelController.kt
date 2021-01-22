/*
 * Copyright © 2019 Bell Canada Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.Api
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.jetbrains.annotations.NotNull
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModelSearch
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler.BlueprintModelHandler
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils.BlueprintSortByOption
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.mdcWebCoroutineScope
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * BlueprintModelController Purpose: Handle controllerBlueprint API request
 *
 * @author Vinal Patel
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/blueprint-model")
@Api(
    value = "Blueprint Model Catalog",
    description = "Manages all blueprint models which are available in CDS"
)
open class BlueprintModelController(private val bluePrintModelHandler: BlueprintModelHandler) {

    @PostMapping(
        path = arrayOf("/bootstrap"), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE),
        consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE)
    )
    @ApiOperation(
        value = "Bootstrap CDS",
        notes = "Loads all Model Types, Resource Dictionaries and Blueprint Models which are included in CDS by default. " +
            "Before starting to work with CDS, bootstrap should be called to load all the basic models that each orginization might support. " +
            "Parameter values can be set as `false`  to skip loading e.g. the Resource Dictionaries but this is not recommended."
    )
    @ApiResponses(
        ApiResponse(code = 200, message = "OK"),
        ApiResponse(code = 500, message = "Internal Server Error")
    )
    @ResponseBody
    @Throws(BlueprintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun bootstrap(
        @ApiParam(required = true, value = "Specifies which elements to load")
        @RequestBody bootstrapRequest: BootstrapRequest
    ): Unit = mdcWebCoroutineScope {
        bluePrintModelHandler.bootstrapBlueprint(bootstrapRequest)
    }

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ApiOperation(
        value = "Save a Blueprint Model",
        notes = "Saves a blueprint model by the given CBA zip file input. " +
            "There is no validation of the attached CBA happening when this API is called.",
        response = BlueprintModelSearch::class
    )
    @ApiResponses(
        ApiResponse(code = 200, message = "OK"),
        ApiResponse(code = 500, message = "Internal Server Error")
    )
    @ResponseBody
    @Throws(BlueprintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun saveBlueprint(
        @ApiParam(name = "file", value = "CBA file to be uploaded (example: cba.zip)", required = true)
        @RequestPart("file") filePart: FilePart
    ): BlueprintModelSearch = mdcWebCoroutineScope {
        bluePrintModelHandler.saveBlueprintModel(filePart)
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "List all Blueprint Models",
        notes = "Lists all meta-data of blueprint models which are saved in CDS."
    )
    @ApiResponses(
        ApiResponse(code = 200, message = "OK"),
        ApiResponse(code = 500, message = "Internal Server Error")
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun allBlueprintModel(): List<BlueprintModelSearch> {
        return this.bluePrintModelHandler.allBlueprintModel()
    }

    @GetMapping("/paged", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Get Blueprints ordered",
        notes = "Lists all blueprint models which are saved in CDS in an ordered mode.",
        nickname = "BlueprintModelController_allBlueprintModelPaged_GET.org.onap.ccsdk.cds.blueprintsprocessor.designer.api"
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun allBlueprintModel(
        @ApiParam(value = "Maximum number of returned blueprint models") @RequestParam(defaultValue = "20") limit: Int,
        @ApiParam(value = "Offset") @RequestParam(defaultValue = "0") offset: Int,
        @ApiParam(value = "Order of returned blueprint models") @RequestParam(defaultValue = "DATE") sort: BlueprintSortByOption,
        @ApiParam(value = "Ascend or descend ordering") @RequestParam(defaultValue = "ASC") sortType: String
    ): Page<BlueprintModelSearch> {
        val pageRequest = PageRequest.of(
            offset, limit,
            Sort.Direction.fromString(sortType), sort.columnName
        )
        return this.bluePrintModelHandler.allBlueprintModel(pageRequest)
    }

    @GetMapping("meta-data/{keyword}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Search for Blueprints by a Keyword",
        notes = "Lists all blueprint models by a matching keyword in any of the meta-data of the blueprint models. " +
            "Blueprint models are just returned if a whole keyword is matching, not just parts of it. Not case-sensitive. " +
            "Used by CDS UI.",
        responseContainer = "List",
        response = BlueprintModelSearch::class
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    suspend fun allBlueprintModelMetaData(
        @NotNull
        @ApiParam(value = "Keyword to search for in blueprint model meta-data", required = true, example = "pnf_netconf")
        @PathVariable(value = "keyword") keyWord: String
    ): List<BlueprintModelSearch> =
        mdcWebCoroutineScope {
            bluePrintModelHandler.searchBlueprintModelsByKeyWord(keyWord)
        }

    @GetMapping("/paged/meta-data/{keyword}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Search for Blueprints by a Keyword in an ordered mode",
        notes = "Lists all blueprint models by a matching keyword in any of the meta-data of the blueprint models in an ordered mode. " +
            "Blueprint models are just returned if a whole keyword is matching, not just parts of it. Not case-sensitive. " +
            "Used by CDS UI."
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun allBlueprintModelMetaDataPaged(
        @ApiParam(value = "Keyword to search for in blueprint model meta-data", required = true, example = "pnf_netconf")
        @NotNull @PathVariable(value = "keyword") keyWord: String,
        @ApiParam(value = "Maximum number of returned blueprint models") @RequestParam(defaultValue = "20") limit: Int,
        @ApiParam(value = "Offset") @RequestParam(defaultValue = "0") offset: Int,
        @ApiParam(value = "Order of returned blueprint models") @RequestParam(defaultValue = "DATE") sort: BlueprintSortByOption,
        @ApiParam(value = "Ascend or descend ordering") @RequestParam(defaultValue = "ASC") sortType: String
    ): Page<BlueprintModelSearch> {
        val pageRequest = PageRequest.of(
            offset, limit,
            Sort.Direction.fromString(sortType), sort.columnName
        )
        return this.bluePrintModelHandler.searchBlueprintModelsByKeyWordPaged(keyWord, pageRequest)
    }

    @DeleteMapping("/{id}")
    @ApiOperation(
        value = "Delete a Blueprint Model by ID",
        notes = "Delete a blueprint model by its ID. ID is the internally created ID of blueprint, not the name of blueprint."
    )
    @ApiResponses(
        ApiResponse(code = 200, message = "OK"),
        ApiResponse(code = 404, message = "RESOURCE_NOT_FOUND")
    )
    @Throws(BlueprintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun deleteBlueprint(
        @ApiParam(value = "ID of the blueprint model to delete", required = true, example = "67ec1f96-ab55-4b81-aff9-23ee0ed1d7a4")
        @PathVariable(value = "id") id: String
    ) = mdcWebCoroutineScope {
        bluePrintModelHandler.deleteBlueprintModel(id)
    }

    @GetMapping("/by-name/{name}/version/{version}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Get a Blueprint Model by Name and Version",
        notes = "Get Meta-Data of a Blueprint Model by its name and version.",
        response = BlueprintModelSearch::class
    )
    @ApiResponses(
        ApiResponse(code = 200, message = "OK"),
        ApiResponse(code = 404, message = "Not Found")
    )
    @ResponseBody
    @Throws(BlueprintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun getBlueprintByNameAndVersion(
        @ApiParam(value = "Name of the blueprint model", required = true, example = "pnf_netconf") @PathVariable(value = "name") name: String,
        @ApiParam(value = "Version of the blueprint model", required = true, example = "1.0.0") @PathVariable(value = "version") version: String
    ): ResponseEntity<BlueprintModelSearch> = mdcWebCoroutineScope {
        val bluePrintModel: BlueprintModelSearch? =
            bluePrintModelHandler.getBlueprintModelSearchByNameAndVersion(name, version)
        if (bluePrintModel != null)
            ResponseEntity(bluePrintModel, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/download/by-name/{name}/version/{version}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Download a Blueprint Model",
        notes = "Gets the CBA of a blueprint model by its name and version. Response can be saved to a file to download the CBA."
    )
    @ApiResponses(
        ApiResponse(code = 200, message = "OK"),
        ApiResponse(code = 404, message = "Not Found")
    )
    @ResponseBody
    @Throws(BlueprintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun downloadBlueprintByNameAndVersion(
        @ApiParam(value = "Name of the blueprint model", required = true, example = "pnf_netconf") @PathVariable(value = "name") name: String,
        @ApiParam(value = "Version of the blueprint model", required = true, example = "1.0.0") @PathVariable(value = "version") version: String
    ): ResponseEntity<Resource> = mdcWebCoroutineScope {
        bluePrintModelHandler.downloadBlueprintModelFileByNameAndVersion(name, version)
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Get a Blueprint Model by ID",
        notes = "Get meta-data of a blueprint model by its internally created ID.",
        response = BlueprintModelSearch::class
    )
    @ApiResponses(
        ApiResponse(code = 200, message = "OK"),
        ApiResponse(code = 404, message = "Not Found")
    )
    @ResponseBody
    @Throws(BlueprintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun getBlueprintModel(
        @ApiParam(value = "ID of the blueprint model to search for", required = true, example = "67ec1f96-ab55-4b81-aff9-23ee0ed1d7a4")
        @PathVariable(value = "id") id: String
    ): BlueprintModelSearch = mdcWebCoroutineScope {
        bluePrintModelHandler.getBlueprintModelSearch(id)
    }

    @GetMapping("/download/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Download a Blueprint Model by ID",
        notes = "Gets the CBA of a blueprint model by its ID. Response can be saved to a file to download the CBA."
    )
    @ApiResponses(
        ApiResponse(code = 200, message = "OK"),
        ApiResponse(code = 404, message = "Not Found")
    )
    @ResponseBody
    @Throws(BlueprintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun downloadBlueprint(
        @ApiParam(value = "ID of the blueprint model to download", required = true, example = "67ec1f96-ab55-4b81-aff9-23ee0ed1d7a4")
        @PathVariable(value = "id") id: String
    ): ResponseEntity<Resource> =
        mdcWebCoroutineScope {
            bluePrintModelHandler.downloadBlueprintModelFile(id)
        }

    @PostMapping(
        "/enrich", produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [
            MediaType
                .MULTIPART_FORM_DATA_VALUE
        ]
    )
    @ApiOperation(
        value = "Enrich a Blueprint Model",
        notes = "Enriches the attached CBA and returns the enriched CBA zip file in the response. " +
            "The enrichment process will complete the package by providing all the definition of types used."
    )
    @ResponseBody
    @Throws(BlueprintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun enrichBlueprint(
        @ApiParam(name = "file", value = "CBA zip file to be uploaded (example: cba_unenriched.zip)", required = true)
        @RequestPart("file") file: FilePart
    ): ResponseEntity<Resource> = mdcWebCoroutineScope {
        bluePrintModelHandler.enrichBlueprint(file)
    }

    @PostMapping(
        "/enrichandpublish", produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [
            MediaType
                .MULTIPART_FORM_DATA_VALUE
        ]
    )
    @ApiOperation(
        value = "Enrich and publish a Blueprint Model",
        notes = "Enriches the attached CBA, validates it and saves it in CDS if validation was successful.",
        response = BlueprintModelSearch::class
    )
    @ApiResponses(
        ApiResponse(code = 200, message = "OK"),
        ApiResponse(code = 503, message = "Service Unavailable")
    )
    @ResponseBody
    @Throws(BlueprintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun enrichAndPubishlueprint(
        @ApiParam(name = "file", value = "Unenriched CBA zip file to be uploaded (example: cba_unenriched.zip)", required = true)
        @RequestPart("file") file: FilePart
    ): BlueprintModelSearch = mdcWebCoroutineScope {
        bluePrintModelHandler.enrichAndPublishBlueprint(file)
    }

    @PostMapping("/publish", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ApiOperation(
        value = "Publish a Blueprint Model",
        notes = "Validates the attached CBA file and saves it in CDS if validation was successful. CBA needs to be already enriched.",
        response = BlueprintModelSearch::class
    )
    @ResponseBody
    @Throws(BlueprintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun publishBlueprint(
        @ApiParam(name = "file", value = "Enriched CBA zip file to be uploaded (example: cba_enriched.zip)", required = true)
        @RequestPart("file") file: FilePart
    ): BlueprintModelSearch = mdcWebCoroutineScope {
        bluePrintModelHandler.publishBlueprint(file)
    }

    @GetMapping("/search/{tags}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Search for a Blueprint by Tag",
        notes = "Searches for all blueprint models which contain the specified input parameter in their tags. " +
            "Blueprint models which contain just parts of the searched word in their tags are also returned.",
        responseContainer = "List",
        response = BlueprintModelSearch::class
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    suspend fun searchBlueprintModels(
        @ApiParam(value = "Tag to search for", example = "test", required = true)
        @PathVariable(value = "tags") tags: String
    ): List<BlueprintModelSearch> =
        mdcWebCoroutineScope {
            bluePrintModelHandler.searchBlueprintModels(tags)
        }

    @DeleteMapping("/name/{name}/version/{version}")
    @ApiOperation(
        value = "Delete a Blueprint Model by Name",
        notes = "Deletes a blueprint model identified by its name and version from CDS.",
        // to avoid duplicate operation IDs
        nickname = "BlueprintModelController_deleteBlueprintByName_DELETE.org.onap.ccsdk.cds.blueprintsprocessor.designer.api",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('USER')")
    suspend fun deleteBlueprint(
        @ApiParam(value = "Name of the blueprint model", required = true, example = "pnf_netconf")
        @PathVariable(value = "name") name: String,
        @ApiParam(value = "Version of the blueprint model", required = true, example = "1.0.0")
        @PathVariable(value = "version") version: String
    ) = mdcWebCoroutineScope {
        bluePrintModelHandler.deleteBlueprintModel(name, version)
    }

    @PostMapping(
        path = arrayOf("/workflow-spec"),
        produces = arrayOf(
            MediaType
                .APPLICATION_JSON_VALUE
        ),
        consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE)
    )
    @ApiOperation(
        value = "Get Workflow Specification",
        notes = "Get the workflow of a blueprint identified by Blueprint and workflow name. " +
            "Inputs, outputs and data types of workflow is returned."
    )
    @ResponseBody
    @Throws(BlueprintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun workflowSpec(
        @ApiParam(required = true, value = "Blueprint and workflow identification")
        @RequestBody workFlowSpecReq: WorkFlowSpecRequest
    ):
        ResponseEntity<String> = mdcWebCoroutineScope {
            var json = bluePrintModelHandler.prepareWorkFlowSpec(workFlowSpecReq)
                .asJsonString()
            ResponseEntity(json, HttpStatus.OK)
        }

    @GetMapping(
        path = arrayOf(
            "/workflows/blueprint-name/{name}/version/{version}"
        ),
        produces = arrayOf(MediaType.APPLICATION_JSON_VALUE)
    )
    @ApiOperation(
        value = "Get Workflows of a Blueprint",
        notes = "Get all available workflows of a Blueprint identified by its name and version."
    )
    @ResponseBody
    @Throws(BlueprintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun getWorkflowList(
        @ApiParam(value = "Name of the blueprint model", example = "pnf_netconf", required = true)
        @PathVariable(value = "name") name: String,
        @ApiParam(value = "Version of the blueprint model", example = "1.0.0", required = true)
        @PathVariable(value = "version") version: String
    ): ResponseEntity<String> = mdcWebCoroutineScope {
        var json = bluePrintModelHandler.getWorkflowNames(name, version)
            .asJsonString()
        ResponseEntity(json, HttpStatus.OK)
    }
}

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

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.parameters.RequestBody as RequestBodyDoc
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.jetbrains.annotations.NotNull
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModelSearch
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler.BluePrintModelHandler
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils.BlueprintSortByOption
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.mdcWebCoroutineScope
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
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
@Tag(
    name = "Blueprint Model Catalog",
    description = "Manages all blueprint models which are available in CDS"
)
open class BlueprintModelController(private val bluePrintModelHandler: BluePrintModelHandler) {

    @PostMapping(
        path = arrayOf("/bootstrap"), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE),
        consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE)
    )
    @Operation(
        summary = "Bootstrap CDS",
        description = "Loads all Model Types, Resource Dictionaries and Blueprint Models which are included in CDS by default. " +
            "Before starting to work with CDS, bootstrap should be called to load all the basic models that each orginization might support. " +
            "Parameter values can be set as `false`  to skip loading e.g. the Resource Dictionaries but this is not recommended."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun bootstrap(
        @RequestBodyDoc(required = true, description = "Specifies which elements to load")
        @RequestBody bootstrapRequest: BootstrapRequest
    ): Unit = mdcWebCoroutineScope {
        bluePrintModelHandler.bootstrapBlueprint(bootstrapRequest)
    }

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Save a Blueprint Model",
        description = "Saves a blueprint model by the given CBA zip file input. " +
            "There is no validation of the attached CBA happening when this API is called."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun saveBlueprint(
        @Parameter(name = "file", description = "CBA file to be uploaded (example: cba.zip)", required = true)
        @RequestPart("file") filePart: FilePart
    ): BlueprintModelSearch = mdcWebCoroutineScope {
        bluePrintModelHandler.saveBlueprintModel(filePart)
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "List all Blueprint Models",
        description = "Lists all meta-data of blueprint models which are saved in CDS."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "500", description = "Internal Server Error")
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun allBlueprintModel(): List<BlueprintModelSearch> {
        return this.bluePrintModelHandler.allBlueprintModel()
    }

    @GetMapping("/paged", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get Blueprints ordered",
        description = "Lists all blueprint models which are saved in CDS in an ordered mode.",
        operationId = "BlueprintModelController_allBlueprintModelPaged_GET.org.onap.ccsdk.cds.blueprintsprocessor.designer.api"
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun allBlueprintModel(
        @Parameter(description = "Maximum number of returned blueprint models") @RequestParam(defaultValue = "20") limit: Int,
        @Parameter(description = "Offset") @RequestParam(defaultValue = "0") offset: Int,
        @Parameter(description = "Order of returned blueprint models") @RequestParam(defaultValue = "DATE") sort: BlueprintSortByOption,
        @Parameter(description = "Ascend or descend ordering") @RequestParam(defaultValue = "ASC") sortType: String,
        @Parameter(description = "Filter by published status (true or false)") @RequestParam(required = false) published: Boolean?
    ): Page<BlueprintModelSearch> {
        val pageRequest = PageRequest.of(
            offset, limit,
            Sort.Direction.fromString(sortType), sort.columnName
        )
        return if (published != null) {
            this.bluePrintModelHandler.allBlueprintModelByPublished(published, pageRequest)
        } else {
            this.bluePrintModelHandler.allBlueprintModel(pageRequest)
        }
    }

    @GetMapping("meta-data/{keyword}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Search for Blueprints by a Keyword",
        description = "Lists all blueprint models by a matching keyword in any of the meta-data of the blueprint models. " +
            "Blueprint models are just returned if a whole keyword is matching, not just parts of it. Not case-sensitive. " +
            "Used by CDS UI."
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    suspend fun allBlueprintModelMetaData(
        @NotNull
        @Parameter(description = "Keyword to search for in blueprint model meta-data", required = true, example = "pnf_netconf")
        @PathVariable(value = "keyword") keyWord: String
    ): List<BlueprintModelSearch> =
        mdcWebCoroutineScope {
            bluePrintModelHandler.searchBluePrintModelsByKeyWord(keyWord)
        }

    @GetMapping("/paged/meta-data/{keyword}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Search for Blueprints by a Keyword in an ordered mode",
        description = "Lists all blueprint models by a matching keyword in any of the meta-data of the blueprint models in an ordered mode. " +
            "Blueprint models are just returned if a whole keyword is matching, not just parts of it. Not case-sensitive. " +
            "Used by CDS UI."
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun allBlueprintModelMetaDataPaged(
        @Parameter(description = "Keyword to search for in blueprint model meta-data", required = true, example = "pnf_netconf")
        @NotNull @PathVariable(value = "keyword") keyWord: String,
        @Parameter(description = "Maximum number of returned blueprint models") @RequestParam(defaultValue = "20") limit: Int,
        @Parameter(description = "Offset") @RequestParam(defaultValue = "0") offset: Int,
        @Parameter(description = "Order of returned blueprint models") @RequestParam(defaultValue = "DATE") sort: BlueprintSortByOption,
        @Parameter(description = "Ascend or descend ordering") @RequestParam(defaultValue = "ASC") sortType: String
    ): Page<BlueprintModelSearch> {
        val pageRequest = PageRequest.of(
            offset, limit,
            Sort.Direction.fromString(sortType), sort.columnName
        )
        return this.bluePrintModelHandler.searchBluePrintModelsByKeyWordPaged(keyWord, pageRequest)
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a Blueprint Model by ID",
        description = "Delete a blueprint model by its ID. ID is the internally created ID of blueprint, not the name of blueprint."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND")
    )
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun deleteBlueprint(
        @Parameter(description = "ID of the blueprint model to delete", required = true, example = "67ec1f96-ab55-4b81-aff9-23ee0ed1d7a4")
        @PathVariable(value = "id") id: String
    ) = mdcWebCoroutineScope {
        bluePrintModelHandler.deleteBlueprintModel(id)
    }

    @GetMapping("/by-name/{name}/version/{version}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get a Blueprint Model by Name and Version",
        description = "Get Meta-Data of a Blueprint Model by its name and version."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Not Found")
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun getBlueprintByNameAndVersion(
        @Parameter(description = "Name of the blueprint model", required = true, example = "pnf_netconf") @PathVariable(value = "name") name: String,
        @Parameter(description = "Version of the blueprint model", required = true, example = "1.0.0") @PathVariable(value = "version") version: String
    ): ResponseEntity<BlueprintModelSearch> = mdcWebCoroutineScope {
        val bluePrintModel: BlueprintModelSearch? =
            bluePrintModelHandler.getBlueprintModelSearchByNameAndVersion(name, version)
        if (bluePrintModel != null)
            ResponseEntity(bluePrintModel, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @GetMapping("/download/by-name/{name}/version/{version}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Download a Blueprint Model",
        description = "Gets the CBA of a blueprint model by its name and version. Response can be saved to a file to download the CBA."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Not Found")
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun downloadBlueprintByNameAndVersion(
        @Parameter(description = "Name of the blueprint model", required = true, example = "pnf_netconf") @PathVariable(value = "name") name: String,
        @Parameter(description = "Version of the blueprint model", required = true, example = "1.0.0") @PathVariable(value = "version") version: String
    ): ResponseEntity<Resource> = mdcWebCoroutineScope {
        bluePrintModelHandler.downloadBlueprintModelFileByNameAndVersion(name, version)
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get a Blueprint Model by ID",
        description = "Get meta-data of a blueprint model by its internally created ID."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Not Found")
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun getBlueprintModel(
        @Parameter(description = "ID of the blueprint model to search for", required = true, example = "67ec1f96-ab55-4b81-aff9-23ee0ed1d7a4")
        @PathVariable(value = "id") id: String
    ): BlueprintModelSearch = mdcWebCoroutineScope {
        bluePrintModelHandler.getBlueprintModelSearch(id)
    }

    @GetMapping("/download/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Download a Blueprint Model by ID",
        description = "Gets the CBA of a blueprint model by its ID. Response can be saved to a file to download the CBA."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Not Found")
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun downloadBluePrint(
        @Parameter(description = "ID of the blueprint model to download", required = true, example = "67ec1f96-ab55-4b81-aff9-23ee0ed1d7a4")
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
    @Operation(
        summary = "Enrich a Blueprint Model",
        description = "Enriches the attached CBA and returns the enriched CBA zip file in the response. " +
            "The enrichment process will complete the package by providing all the definition of types used."
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun enrichBlueprint(
        @Parameter(name = "file", description = "CBA zip file to be uploaded (example: cba_unenriched.zip)", required = true)
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
    @Operation(
        summary = "Enrich and publish a Blueprint Model",
        description = "Enriches the attached CBA, validates it and saves it in CDS if validation was successful."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "503", description = "Service Unavailable")
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun enrichAndPubishlueprint(
        @Parameter(name = "file", description = "Unenriched CBA zip file to be uploaded (example: cba_unenriched.zip)", required = true)
        @RequestPart("file") file: FilePart
    ): BlueprintModelSearch = mdcWebCoroutineScope {
        bluePrintModelHandler.enrichAndPublishBlueprint(file)
    }

    @PostMapping("/publish", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Publish a Blueprint Model",
        description = "Validates the attached CBA file and saves it in CDS if validation was successful. CBA needs to be already enriched."
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun publishBlueprint(
        @Parameter(name = "file", description = "Enriched CBA zip file to be uploaded (example: cba_enriched.zip)", required = true)
        @RequestPart("file") file: FilePart
    ): BlueprintModelSearch = mdcWebCoroutineScope {
        bluePrintModelHandler.publishBlueprint(file)
    }

    @GetMapping("/search/{tags}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Search for a Blueprint by Tag",
        description = "Searches for all blueprint models which contain the specified input parameter in their tags. " +
            "Blueprint models which contain just parts of the searched word in their tags are also returned."
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    suspend fun searchBlueprintModels(
        @Parameter(description = "Tag to search for", example = "test", required = true)
        @PathVariable(value = "tags") tags: String
    ): List<BlueprintModelSearch> =
        mdcWebCoroutineScope {
            bluePrintModelHandler.searchBlueprintModels(tags)
        }

    @DeleteMapping("/name/{name}/version/{version}")
    @Operation(
        summary = "Delete a Blueprint Model by Name",
        description = "Deletes a blueprint model identified by its name and version from CDS.",
        // to avoid duplicate operation IDs
        operationId = "BlueprintModelController_deleteBlueprintByName_DELETE.org.onap.ccsdk.cds.blueprintsprocessor.designer.api"
    )
    @PreAuthorize("hasRole('USER')")
    suspend fun deleteBlueprint(
        @Parameter(description = "Name of the blueprint model", required = true, example = "pnf_netconf")
        @PathVariable(value = "name") name: String,
        @Parameter(description = "Version of the blueprint model", required = true, example = "1.0.0")
        @PathVariable(value = "version") version: String
    ) = mdcWebCoroutineScope {
        bluePrintModelHandler.deleteBlueprintModel(name, version)
    }

    @PostMapping(
        path = arrayOf("/workflow-spec"),
        produces = arrayOf(MediaType.APPLICATION_JSON_VALUE),
        consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE)
    )
    @Operation(
        summary = "Get Workflow Specification",
        description = "Get the workflow of a blueprint identified by Blueprint and workflow name. " +
            "Inputs, outputs and data types of workflow is returned."
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun workflowSpec(
        @RequestBodyDoc(required = true, description = "Blueprint and workflow identification")
        @RequestBody workFlowSpecReq: WorkFlowSpecRequest
    ):
        ResponseEntity<String> = mdcWebCoroutineScope {
            var json = bluePrintModelHandler.prepareWorkFlowSpec(workFlowSpecReq)
                .asJsonString()
            ResponseEntity(json, HttpStatus.OK)
        }

    @GetMapping(
        path = arrayOf("/workflows/blueprint-name/{name}/version/{version}"),
        produces = arrayOf(MediaType.APPLICATION_JSON_VALUE)
    )
    @Operation(
        summary = "Get Workflows of a Blueprint",
        description = "Get all available workflows of a Blueprint identified by its name and version."
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    suspend fun getWorkflowList(
        @Parameter(description = "Name of the blueprint model", example = "pnf_netconf", required = true)
        @PathVariable(value = "name") name: String,
        @Parameter(description = "Version of the blueprint model", example = "1.0.0", required = true)
        @PathVariable(value = "version") version: String
    ): ResponseEntity<String> = mdcWebCoroutineScope {
        var json = bluePrintModelHandler.getWorkflowNames(name, version)
            .asJsonString()
        ResponseEntity(json, HttpStatus.OK)
    }
}

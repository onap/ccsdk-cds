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
import io.swagger.annotations.ApiParam
import org.jetbrains.annotations.NotNull
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModelSearch
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler.BluePrintModelHandler
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils.BlueprintSortByOption
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.monoMdc
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
import reactor.core.publisher.Mono

/**
 * BlueprintModelController Purpose: Handle controllerBlueprint API request
 *
 * @author Vinal Patel
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/blueprint-model")
open class BlueprintModelController(private val bluePrintModelHandler: BluePrintModelHandler) {

    @PostMapping(
        path = arrayOf("/bootstrap"), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE),
        consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE)
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    fun bootstrap(@RequestBody bootstrapRequest: BootstrapRequest): Mono<Unit> = monoMdc {
        bluePrintModelHandler.bootstrapBlueprint(bootstrapRequest)
    }

    @PostMapping("", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    fun saveBlueprint(@RequestPart("file") filePart: FilePart): Mono<BlueprintModelSearch> = monoMdc {
        bluePrintModelHandler.saveBlueprintModel(filePart)
    }

    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun allBlueprintModel(): List<BlueprintModelSearch> {
        return this.bluePrintModelHandler.allBlueprintModel()
    }

    @GetMapping("/paged", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun allBlueprintModel(
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "DATE") sort: BlueprintSortByOption
    ): Page<BlueprintModelSearch> {
        val pageRequest = PageRequest.of(offset, limit, Sort.Direction.ASC, sort.columnName)
        return this.bluePrintModelHandler.allBlueprintModel(pageRequest)
    }

    @GetMapping("meta-data/{keyword}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun allBlueprintModelMetaData(@NotNull @PathVariable(value = "keyword") keyWord: String): List<BlueprintModelSearch> {
        return this.bluePrintModelHandler.searchBluePrintModelsByKeyWord(keyWord)
    }

    @GetMapping("/paged/meta-data/{keyword}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun allBlueprintModelMetaDataPaged(
        @NotNull @PathVariable(value = "keyword") keyWord: String,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "DATE") sort: BlueprintSortByOption
    ): Page<BlueprintModelSearch> {
        val pageRequest = PageRequest.of(offset, limit, Sort.Direction.ASC, sort.columnName)
        return this.bluePrintModelHandler.searchBluePrintModelsByKeyWordPaged(keyWord, pageRequest)
    }

    @DeleteMapping("/{id}")
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    fun deleteBlueprint(@PathVariable(value = "id") id: String) {
        this.bluePrintModelHandler.deleteBlueprintModel(id)
    }

    @GetMapping("/by-name/{name}/version/{version}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    fun getBlueprintByNameAndVersion(
        @PathVariable(value = "name") name: String,
        @PathVariable(value = "version") version: String
    ):
            Mono<BlueprintModelSearch> = monoMdc {
        bluePrintModelHandler.getBlueprintModelSearchByNameAndVersion(name, version)
    }

    @GetMapping("/download/by-name/{name}/version/{version}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    fun downloadBlueprintByNameAndVersion(
        @PathVariable(value = "name") name: String,
        @PathVariable(value = "version") version: String
    ):
            Mono<ResponseEntity<Resource>> = monoMdc {
        bluePrintModelHandler.downloadBlueprintModelFileByNameAndVersion(name, version)
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    fun getBlueprintModel(@PathVariable(value = "id") id: String): BlueprintModelSearch {
        return this.bluePrintModelHandler.getBlueprintModelSearch(id)
    }

    @GetMapping("/download/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    fun downloadBluePrint(@PathVariable(value = "id") id: String): Mono<ResponseEntity<Resource>> = monoMdc {
        bluePrintModelHandler.downloadBlueprintModelFile(id)
    }

    @PostMapping(
        "/enrich", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType
            .MULTIPART_FORM_DATA_VALUE]
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    fun enrichBlueprint(@RequestPart("file") file: FilePart): Mono<ResponseEntity<Resource>> = monoMdc {
        bluePrintModelHandler.enrichBlueprint(file)
    }

    @PostMapping("/publish", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    @PreAuthorize("hasRole('USER')")
    fun publishBlueprint(@RequestPart("file") file: FilePart): Mono<BlueprintModelSearch> = monoMdc {
        bluePrintModelHandler.publishBlueprint(file)
    }

    @GetMapping("/search/{tags}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun searchBlueprintModels(@PathVariable(value = "tags") tags: String): List<BlueprintModelSearch> {
        return this.bluePrintModelHandler.searchBlueprintModels(tags)
    }

    @DeleteMapping("/name/{name}/version/{version}")
    @ApiOperation(
        value = "Delete a CBA",
        notes = "Delete the CBA package identified by its name and version.",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('USER')")
    fun deleteBlueprint(
        @ApiParam(value = "Name of the CBA.", required = true)
        @PathVariable(value = "name") name: String,
        @ApiParam(value = "Version of the CBA.", required = true)
        @PathVariable(value = "version") version: String
    ) = monoMdc {
        bluePrintModelHandler.deleteBlueprintModel(name, version)
    }
}

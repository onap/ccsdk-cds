/*
 * Copyright © 2019 Bell Canada Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints.service.controller

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModelSearch
import org.onap.ccsdk.apps.controllerblueprints.service.handler.BluePrintModelHandler
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
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

    @PostMapping("", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    fun saveBlueprint(@RequestPart("file") file: FilePart): Mono<BlueprintModelSearch> {
        return bluePrintModelHandler.saveBlueprintModel(file)
    }

    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun allBlueprintModel(): List<BlueprintModelSearch> {
        return this.bluePrintModelHandler.allBlueprintModel()
    }

    @DeleteMapping("/{id}")
    @Throws(BluePrintException::class)
    fun deleteBlueprint(@PathVariable(value = "id") id: String) {
        this.bluePrintModelHandler.deleteBlueprintModel(id)
    }

    @GetMapping("/by-name/{name}/version/{version}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    fun getBlueprintByNameAndVersion(@PathVariable(value = "name") name: String,
                                     @PathVariable(value = "version") version: String): BlueprintModelSearch {
        return this.bluePrintModelHandler.getBlueprintModelSearchByNameAndVersion(name, version)
    }

    @GetMapping("/download/by-name/{name}/version/{version}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    fun downloadBlueprintByNameAndVersion(@PathVariable(value = "name") name: String,
                                          @PathVariable(value = "version") version: String): ResponseEntity<Resource> {
        return this.bluePrintModelHandler.downloadBlueprintModelFileByNameAndVersion(name, version)
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    fun getBlueprintModel(@PathVariable(value = "id") id: String): BlueprintModelSearch {
        return this.bluePrintModelHandler.getBlueprintModelSearch(id)
    }

    @GetMapping("/download/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    fun downloadBluePrint(@PathVariable(value = "id") id: String): ResponseEntity<Resource> {
        return this.bluePrintModelHandler.downloadBlueprintModelFile(id)
    }

    @PutMapping("/publish/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    fun publishBlueprintModel(@PathVariable(value = "id") id: String): BlueprintModelSearch {
        return this.bluePrintModelHandler.publishBlueprintModel(id)
    }

    @GetMapping("/search/{tags}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchBlueprintModels(@PathVariable(value = "tags") tags: String): List<BlueprintModelSearch> {
        return this.bluePrintModelHandler.searchBlueprintModels(tags)
    }
}

/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.parameters.RequestBody as RequestBodyDoc
import io.swagger.v3.oas.annotations.tags.Tag
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ResourceDictionary
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler.ResourceDictionaryHandler
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils.DictionarySortByOption
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.mdcWebCoroutineScope
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceSourceMapping
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/v1/dictionary"])
@Tag(
    name = "Resource Dictionary",
    description = "Interaction with stored dictionaries"
)
open class ResourceDictionaryController(private val resourceDictionaryHandler: ResourceDictionaryHandler) {

    @GetMapping(path = ["/{name}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Retrieve a resource dictionary",
        description = "Retrieve a resource dictionary by name provided."
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    suspend fun getResourceDictionaryByName(
        @Parameter(description = "Name of the resource", required = true, example = "hostname")
        @PathVariable(value = "name") name: String
    ): ResourceDictionary =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.getResourceDictionaryByName(name)
        }

    @GetMapping("/paged", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get Blueprints Dictionary ordered",
        description = "Lists all blueprint Dictionary which are saved in CDS in an ordered mode.",
        operationId = "BlueprintModelController_allBlueprintDictionaryPaged_GET.org.onap.ccsdk.cds.blueprintsprocessor.designer.api"
    )
    @ResponseBody
    suspend fun allBlueprintModel(
        @Parameter(description = "Maximum number of returned blueprint dictionary") @RequestParam(defaultValue = "20") limit: Int,
        @Parameter(description = "Offset") @RequestParam(defaultValue = "0") offset: Int,
        @Parameter(description = "Order of returned blueprint dictionary") @RequestParam(defaultValue = "DATE") sort: DictionarySortByOption,
        @Parameter(description = "Ascend or descend ordering") @RequestParam(defaultValue = "ASC") sortType: String
    ): Page<ResourceDictionary> {
        val pageRequest = PageRequest.of(
            offset, limit,
            Sort.Direction.fromString(sortType), sort.columnName
        )
        return resourceDictionaryHandler.getAllDictionary(pageRequest)
    }

    @PostMapping(
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Save a resource dictionary",
        description = "Save a resource dictionary by dictionary provided."
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    suspend fun saveResourceDictionary(
        @RequestBodyDoc(description = "Resource dictionary to store", required = true)
        @RequestBody resourceDictionary: ResourceDictionary
    ): ResourceDictionary =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.saveResourceDictionary(resourceDictionary)
        }

    @PostMapping(
        path = ["/definition"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Save a resource dictionary",
        description = "Save a resource dictionary by provided resource definition json.",
        operationId = "ResourceDictionaryController_saveResourceDictionary_1_POST.org.onap.ccsdk.cds.blueprintsprocessor.designer.api"
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    suspend fun saveResourceDefinition(
        @RequestBodyDoc(description = "Resource definition to generate Resource Dictionary", required = true)
        @RequestBody resourceDefinition: ResourceDefinition
    ): ResourceDictionary =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.saveResourceDefinition(resourceDefinition)
        }

    @PostMapping(
        path = ["/definition-bulk"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Save multiple resource dictionaries",
        description = "Save multiple resource dictionaries by provided resource definition json array.",
        operationId = "ResourceDictionaryController_saveAllResourceDictionary_1_POST.org.onap.ccsdk.cds.blueprintsprocessor.designer.api"
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    suspend fun saveAllResourceDictionary(
        @RequestBodyDoc(description = "Resource definition json array to generate Resource Dictionaries", required = true)
        @RequestBody resourceDefinition: List<ResourceDefinition>
    ): MutableList<ResourceDictionary> =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.saveAllResourceDefinition(resourceDefinition)
        }

    @DeleteMapping(path = ["/{name}"])
    @Operation(
        summary = "Remove a resource dictionary",
        description = "Remove a resource dictionary by name provided."
    )
    suspend fun deleteResourceDictionaryByName(
        @Parameter(description = "Name of the resource", required = true)
        @PathVariable(value = "name") name: String
    ) = mdcWebCoroutineScope {
        resourceDictionaryHandler.deleteResourceDictionary(name)
    }

    @PostMapping(
        path = ["/by-names"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Search for a resource dictionary",
        description = "Search for a resource dictionary by names provided."
    )
    @ResponseBody
    suspend fun searchResourceDictionaryByNames(
        @RequestBodyDoc(description = "List of names", required = true)
        @RequestBody names: List<String>
    ): List<ResourceDictionary> =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.searchResourceDictionaryByNames(names)
        }

    @GetMapping(path = ["/search/{tags}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Search for a resource dictionary",
        description = "Search for a resource dictionary by tags provided."
    )
    @ResponseBody
    suspend fun searchResourceDictionaryByTags(
        @Parameter(description = "Tags list", required = true, example = "status")
        @PathVariable(value = "tags") tags: String
    ): List<ResourceDictionary> =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.searchResourceDictionaryByTags(tags)
        }

    @GetMapping(path = ["/source-mapping"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Search for a source mapping",
        description = "Search for a source mapping."
    )
    @ResponseBody
    suspend fun getResourceSourceMapping(): ResourceSourceMapping = mdcWebCoroutineScope {
        resourceDictionaryHandler.getResourceSourceMapping()
    }

    @GetMapping(path = ["/resource_dictionary_group"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Retrieve all resource dictionary groups",
        description = "Retrieve all resource dictionary groups."
    )
    @ResponseBody
    suspend fun getResourceDictionaryDistinct(): List<String> = mdcWebCoroutineScope {
        resourceDictionaryHandler.getResourceDictionaryDistinct()
    }
}

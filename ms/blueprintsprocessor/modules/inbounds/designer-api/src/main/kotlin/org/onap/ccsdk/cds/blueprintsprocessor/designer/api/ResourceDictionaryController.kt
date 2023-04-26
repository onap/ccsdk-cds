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

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
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
@Api(
    value = "Resource Dictionary",
    description = "Interaction with stored dictionaries"
)
open class ResourceDictionaryController(private val resourceDictionaryHandler: ResourceDictionaryHandler) {

    @GetMapping(path = ["/{name}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Retrieve a resource dictionary",
        notes = "Retrieve a resource dictionary by name provided.",
        response = ResourceDictionary::class
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    suspend fun getResourceDictionaryByName(
        @ApiParam(value = "Name of the resource", required = true, example = "\"hostname\"")
        @PathVariable(value = "name") name: String
    ): ResourceDictionary =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.getResourceDictionaryByName(name)
        }

    @GetMapping("/paged", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Get Blueprints Dictionary ordered",
        notes = "Lists all blueprint Dictionary which are saved in CDS in an ordered mode.",
        nickname = "BlueprintModelController_allBlueprintDictionaryPaged_GET.org.onap.ccsdk.cds.blueprintsprocessor.designer.api"
    )
    @ResponseBody
    suspend fun allBlueprintModel(
        @ApiParam(value = "Maximum number of returned blueprint dictionary") @RequestParam(defaultValue = "20") limit: Int,
        @ApiParam(value = "Offset") @RequestParam(defaultValue = "0") offset: Int,
        @ApiParam(value = "Order of returned blueprint dictionary") @RequestParam(defaultValue = "DATE") sort: DictionarySortByOption,
        @ApiParam(value = "Ascend or descend ordering") @RequestParam(defaultValue = "ASC") sortType: String
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
    @ApiOperation(
        value = "Save a resource dictionary",
        notes = "Save a resource dictionary by dictionary provided.",
        response = ResourceDictionary::class
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    suspend fun saveResourceDictionary(
        @ApiParam(value = "Resource dictionary to store", required = true)
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
    @ApiOperation(
        value = "Save a resource dictionary",
        notes = "Save a resource dictionary by provided resource definition json.",
        nickname = "ResourceDictionaryController_saveResourceDictionary_1_POST.org.onap.ccsdk.cds.blueprintsprocessor.designer.api",
        response = ResourceDictionary::class
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    suspend fun saveResourceDefinition(
        @ApiParam(value = "Resource definition to generate Resource Dictionary", required = true)
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
    @ApiOperation(
        value = "Save multiple resource dictionaries",
        notes = "Save multiple resource dictionaries by provided resource definition json array.",
        nickname = "ResourceDictionaryController_saveAllResourceDictionary_1_POST.org.onap.ccsdk.cds.blueprintsprocessor.designer.api",
        response = ResourceDictionary::class
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    suspend fun saveAllResourceDictionary(
        @ApiParam(value = "Resource definition json array to generate Resource Dictionaries", required = true)
        @RequestBody resourceDefinition: List<ResourceDefinition>
    ): MutableList<ResourceDictionary> =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.saveAllResourceDefinition(resourceDefinition)
        }

    @DeleteMapping(path = ["/{name}"])
    @ApiOperation(
        value = "Remove a resource dictionary",
        notes = "Remove a resource dictionary by name provided."
    )
    suspend fun deleteResourceDictionaryByName(
        @ApiParam(value = "Name of the resource", required = true)
        @PathVariable(value = "name") name: String
    ) = mdcWebCoroutineScope {
        resourceDictionaryHandler.deleteResourceDictionary(name)
    }

    @PostMapping(
        path = ["/by-names"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(
        value = "Search for a resource dictionary",
        notes = "Search for a resource dictionary by names provided.",
        responseContainer = "List",
        response = ResourceDictionary::class
    )
    @ResponseBody
    suspend fun searchResourceDictionaryByNames(
        @ApiParam(value = "List of names", required = true)
        @RequestBody names: List<String>
    ): List<ResourceDictionary> =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.searchResourceDictionaryByNames(names)
        }

    @GetMapping(path = ["/search/{tags}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Search for a resource dictionary",
        notes = "Search for a resource dictionary by tags provided.",
        responseContainer = "List",
        response = ResourceDictionary::class
    )
    @ResponseBody
    suspend fun searchResourceDictionaryByTags(
        @ApiParam(value = "Tags list", required = true, example = "\"status\"")
        @PathVariable(value = "tags") tags: String
    ): List<ResourceDictionary> =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.searchResourceDictionaryByTags(tags)
        }

    @GetMapping(path = ["/source-mapping"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Search for a source mapping",
        notes = "Search for a source mapping.",
        response = ResourceSourceMapping::class
    )
    @ResponseBody
    suspend fun getResourceSourceMapping(): ResourceSourceMapping = mdcWebCoroutineScope {
        resourceDictionaryHandler.getResourceSourceMapping()
    }

    @GetMapping(path = ["/resource_dictionary_group"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Retrieve all resource dictionary groups",
        notes = "Retrieve all resource dictionary groups.",
        responseContainer = "List",
        response = String::class
    )
    @ResponseBody
    suspend fun getResourceDictionaryDistinct(): List<String> = mdcWebCoroutineScope {
        resourceDictionaryHandler.getResourceDictionaryDistinct()
    }
}

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
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.mdcWebCoroutineScope
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceSourceMapping
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/v1/dictionary"])
@Api(
    value = "Resource dictionary",
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
    @Throws(BlueprintException::class)
    suspend fun getResourceDictionaryByName(
        @ApiParam(value = "Name of the resource", required = true, example = "\"hostname\"")
        @PathVariable(value = "name") name: String
    ): ResourceDictionary =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.getResourceDictionaryByName(name)
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
    @Throws(BlueprintException::class)
    suspend fun saveResourceDictionary(
        @ApiParam(value = "Resource dictionary to store", required = true)
        @RequestBody dataDictionary: ResourceDictionary
    ): ResourceDictionary =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.saveResourceDictionary(dataDictionary)
        }

    @PostMapping(
        path = ["/definition"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(
        value = "Save a resource dictionary",
        notes = "Save a resource dictionary by resource definition provided.",
        nickname = "ResourceDictionaryController_saveResourceDictionary_1_POST.org.onap.ccsdk.cds.blueprintsprocessor.designer.api",
        response = ResourceDefinition::class
    )
    @ResponseBody
    @Throws(BlueprintException::class)
    suspend fun saveResourceDictionary(
        @ApiParam(value = "Resource definition to generate", required = true)
        @RequestBody resourceDefinition: ResourceDefinition
    ): ResourceDefinition =
        mdcWebCoroutineScope {
            resourceDictionaryHandler.saveResourceDefinition(resourceDefinition)
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

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

package org.onap.ccsdk.apps.controllerblueprints.service.controller

import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceSourceMapping
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ResourceDictionary
import org.onap.ccsdk.apps.controllerblueprints.service.handler.ResourceDictionaryHandler
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/v1/dictionary"])
open class ResourceDictionaryController(private val resourceDictionaryHandler: ResourceDictionaryHandler) {

    @GetMapping(path = ["/{name}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    fun getResourceDictionaryByName(@PathVariable(value = "name") name: String): ResourceDictionary = runBlocking {
        resourceDictionaryHandler.getResourceDictionaryByName(name)
    }

    @PostMapping(path = [""], produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(BluePrintException::class)
    fun saveResourceDictionary(@RequestBody dataDictionary: ResourceDictionary): ResourceDictionary = runBlocking {
        resourceDictionaryHandler.saveResourceDictionary(dataDictionary)
    }

    @DeleteMapping(path = ["/{name}"])
    fun deleteResourceDictionaryByName(@PathVariable(value = "name") name: String) = runBlocking {
        resourceDictionaryHandler.deleteResourceDictionary(name)
    }

    @PostMapping(path = ["/by-names"], produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchResourceDictionaryByNames(@RequestBody names: List<String>): List<ResourceDictionary> = runBlocking {
        resourceDictionaryHandler.searchResourceDictionaryByNames(names)
    }

    @GetMapping(path = ["/search/{tags}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun searchResourceDictionaryByTags(@PathVariable(value = "tags") tags: String): List<ResourceDictionary> = runBlocking {
        resourceDictionaryHandler.searchResourceDictionaryByTags(tags)

    }

    @GetMapping(path = ["/source-mapping"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getResourceSourceMapping(): ResourceSourceMapping = runBlocking {
        resourceDictionaryHandler.getResourceSourceMapping()
    }

}

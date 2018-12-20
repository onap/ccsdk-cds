/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType
import org.onap.ccsdk.apps.controllerblueprints.service.handler.ModelTypeHandler
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = arrayOf("/api/v1/model-type"))
open class ModelTypeController(private val modelTypeHandler: ModelTypeHandler) {

    @GetMapping(path = arrayOf("/{name}"), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun getModelTypeByName(@PathVariable(value = "name") name: String): ModelType? {
        return modelTypeHandler.getModelTypeByName(name)
    }

    @GetMapping(path = arrayOf("/search/{tags}"), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun searchModelTypes(@PathVariable(value = "tags") tags: String): List<ModelType> {
        return modelTypeHandler.searchModelTypes(tags)
    }

    @GetMapping(path = arrayOf("/by-definition/{definitionType}"), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    @ResponseBody
    fun getModelTypeByDefinitionType(@PathVariable(value = "definitionType") definitionType: String): List<ModelType> {
        return modelTypeHandler.getModelTypeByDefinitionType(definitionType)
    }

    @PostMapping(path = arrayOf(""), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    @ResponseBody
    @Throws(BluePrintException::class)
    fun saveModelType(@RequestBody modelType: ModelType): ModelType {
        return modelTypeHandler.saveModel(modelType)
    }

    @DeleteMapping(path = arrayOf("/{name}"))
    fun deleteModelTypeByName(@PathVariable(value = "name") name: String) {
        modelTypeHandler.deleteByModelName(name)
    }
}
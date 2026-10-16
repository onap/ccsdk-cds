/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ModelType
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler.ModelTypeHandler
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.mdcWebCoroutineScope
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
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
@RequestMapping(value = ["/api/v1/model-type"])
@Tag(
    name = "Model Type Catalog",
    description = "Manages data types in CDS"
)
open class ModelTypeController(private val modelTypeHandler: ModelTypeHandler) {

    @GetMapping(path = ["/{name}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Retrieve a model type",
        description = "Retrieve a model type by name provided."
    )
    suspend fun getModelTypeByName(@PathVariable(value = "name") name: String): ModelType? = mdcWebCoroutineScope {
        modelTypeHandler.getModelTypeByName(name)
    }

    @GetMapping(path = ["/search/{tags}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Retrieve a list of model types",
        description = "Retrieve a list of model types by tags provided."
    )
    suspend fun searchModelTypes(@PathVariable(value = "tags") tags: String): List<ModelType> = mdcWebCoroutineScope {
        modelTypeHandler.searchModelTypes(tags)
    }

    @GetMapping(path = ["/by-definition/{definitionType}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Retrieve a list of model types",
        description = "Retrieve a list of model types by definition type provided."
    )
    @ResponseBody
    suspend fun getModelTypeByDefinitionType(@PathVariable(value = "definitionType") definitionType: String): List<ModelType> =
        mdcWebCoroutineScope {
            modelTypeHandler.getModelTypeByDefinitionType(definitionType)
        }

    @PostMapping(
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Save a model type",
        description = "Save a model type by model type definition provided."
    )
    @ResponseBody
    @Throws(BluePrintException::class)
    suspend fun saveModelType(@RequestBody modelType: ModelType): ModelType = mdcWebCoroutineScope {
        modelTypeHandler.saveModel(modelType)
    }

    @DeleteMapping(path = ["/{name}"])
    @Operation(
        summary = "Remove a model type",
        description = "Remove a model type by name provided."
    )
    suspend fun deleteModelTypeByName(@PathVariable(value = "name") name: String) = mdcWebCoroutineScope {
        modelTypeHandler.deleteByModelName(name)
    }
}

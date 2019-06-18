/*
 * Copyright © 2018-2019 Bell Canada Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.resolutionresults.api

import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionResult
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionResultService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.springframework.stereotype.Service
import java.util.*

/**
 * Process Resolution Results API request to store and retrieve resource resolution results using database acess layer
 * ResourceResolutionResultService and corresponding entities
 *
 * @author Serge Simard
 * @version 1.0
 */
@Service
class ResolutionResultsServiceHandler(private val bluePrintCatalogService: BluePrintCatalogService,
                                      private var resolutionResultService: ResourceResolutionResultService) {

    suspend fun loadStoredResultById(resolutionResultId: String): String {

        return resolutionResultService.readByKey(resolutionResultId)
    }

    suspend fun loadStoredResult(blueprintName : String, blueprintVersion : String, artifactTemplate: String,
                                     resolutionKey: String): String {

        val basePath = bluePrintCatalogService.getFromDatabase(blueprintName, blueprintVersion)
        val blueprintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(UUID.randomUUID().toString(),
                basePath.toString())

        return resolutionResultService.read(blueprintRuntimeService, artifactTemplate, resolutionKey)
    }

    suspend fun saveNewStoredResult(blueprintName : String, blueprintVersion : String, artifactTemplate: String,
                                    resolutionKey: String, result: String): ResourceResolutionResult {

        val basePath = bluePrintCatalogService.getFromDatabase(blueprintName, blueprintVersion)
        val blueprintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(UUID.randomUUID().toString(),
                basePath.toString())

        val properties = mapOf(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_KEY to resolutionKey)

        val resultStored = resolutionResultService.write(properties, result, blueprintRuntimeService, artifactTemplate)

        return resultStored
    }

    suspend fun removeStoredResultById(resolutionResultId: String): Unit {

        return resolutionResultService.deleteByKey(resolutionResultId)
    }
}
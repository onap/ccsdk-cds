/*
 * Copyright (C) 2019 Bell Canada.
 * Modifications Copyright © 2019 IBM.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.util.*

@Service
class ResourceResolutionResultService(private val resourceResolutionRepository: ResourceResolutionRepository) {

    suspend fun read(bluePrintRuntimeService: BluePrintRuntimeService<*>, artifactPrefix: String,
                     resolutionKey: String): String = withContext(Dispatchers.IO) {

        val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

        val blueprintVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]
        val blueprintName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]

        resourceResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
                resolutionKey,
                blueprintName,
                blueprintVersion,
                artifactPrefix).result!!
    }

    suspend fun write(properties: Map<String, Any>, result: String, bluePrintRuntimeService: BluePrintRuntimeService<*>,
                      artifactPrefix: String) = withContext(Dispatchers.IO) {

        val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

        val resourceResolutionResult = ResourceResolutionResult()
        resourceResolutionResult.id = UUID.randomUUID().toString()
        resourceResolutionResult.artifactName = artifactPrefix
        resourceResolutionResult.blueprintVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]
        resourceResolutionResult.blueprintName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]
        resourceResolutionResult.resolutionKey =
                properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_KEY].toString()
        resourceResolutionResult.result = result

        try {
            resourceResolutionRepository.saveAndFlush(resourceResolutionResult)
        } catch (ex: DataIntegrityViolationException) {
            throw BluePrintException("Failed to store resource resolution result.", ex)
        }
    }

    suspend fun readByKey(resolutionResultId: String): String = withContext(Dispatchers.IO) {

        resourceResolutionRepository.getOne(resolutionResultId).result!!
    }

    suspend fun deleteByKey(resolutionResultId: String): Unit = withContext(Dispatchers.IO) {

        val row = resourceResolutionRepository.getOne(resolutionResultId)
        resourceResolutionRepository.delete(row)
        resourceResolutionRepository.flush()
    }
}
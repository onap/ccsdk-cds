/*
 * Copyright (C) 2019 Bell Canada.
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
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.util.*

@Service
class ResourceResolutionDBService(private val resourceResolutionRepository: ResourceResolutionRepository) {

    suspend fun read(blueprintName: String,
                     blueprintVersion: String,
                     artifactPrefix: String,
                     resolutionKey: String,
                     name: String): String = withContext(Dispatchers.IO) {

        resourceResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndName(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix,
            name).value!!
    }

    suspend fun write(properties: Map<String, Any>,
                      bluePrintRuntimeService: BluePrintRuntimeService<*>,
                      artifactPrefix: String,
                      resourceAssignment: ResourceAssignment): ResourceResolution = withContext(Dispatchers.IO) {

        val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

        val resourceResolution = ResourceResolution()
        resourceResolution.id = UUID.randomUUID().toString()
        resourceResolution.artifactName = artifactPrefix
        resourceResolution.blueprintVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]
        resourceResolution.blueprintName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]
        resourceResolution.resolutionKey =
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_KEY].toString()
        resourceResolution.value = resourceAssignment.property?.value?.toString() ?: ""
        resourceResolution.name = resourceAssignment.name
        resourceResolution.dictionaryName = resourceAssignment.dictionaryName
        resourceResolution.dictionaryVersion = resourceAssignment.version
        resourceResolution.dictionarySource = resourceAssignment.dictionarySource
        resourceResolution.status = resourceAssignment.status

        try {
            resourceResolutionRepository.saveAndFlush(resourceResolution)
        } catch (ex: DataIntegrityViolationException) {
            throw BluePrintException("Failed to store resource api result.", ex)
        }
    }
}
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
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.util.*

@Service
class ResourceResolutionDBService(private val resourceResolutionRepository: ResourceResolutionRepository) {

    private val log = LoggerFactory.getLogger(ResourceResolutionDBService::class.toString())

    suspend fun readValue(blueprintName: String,
                          blueprintVersion: String,
                          artifactPrefix: String,
                          resolutionKey: String,
                          name: String): ResourceResolution = withContext(Dispatchers.IO) {

        resourceResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndName(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix,
            name)
    }

    suspend fun readWithResolutionKey(blueprintName: String,
                                      blueprintVersion: String,
                                      artifactPrefix: String,
                                      resolutionKey: String): List<ResourceResolution> = withContext(Dispatchers.IO) {

        resourceResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix)
    }

    suspend fun readWithResourceIdAndResourceType(blueprintName: String,
                                                  blueprintVersion: String,
                                                  resourceId: String,
                                                  resourceType: String): List<ResourceResolution> = withContext(Dispatchers.IO) {

        resourceResolutionRepository.findByBlueprintNameAndBlueprintVersionAndResourceIdAndResourceType(
            blueprintName,
            blueprintVersion,
            resourceId,
            resourceType)
    }

    suspend fun write(properties: Map<String, Any>,
                      bluePrintRuntimeService: BluePrintRuntimeService<*>,
                      artifactPrefix: String,
                      resourceAssignment: ResourceAssignment): ResourceResolution = withContext(Dispatchers.IO) {

        val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

        val blueprintVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]!!
        val blueprintName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]!!
        val resolutionKey =
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_KEY].toString()
        val resourceType =
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE].toString()
        val resourceId =
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID].toString()

        write(blueprintName,
            blueprintVersion,
            resolutionKey,
            resourceId,
            resourceType,
            artifactPrefix,
            resourceAssignment)
    }

    suspend fun write(blueprintName: String,
                      blueprintVersion: String,
                      resolutionKey: String,
                      resourceId: String,
                      resourceType: String,
                      artifactPrefix: String,
                      resourceAssignment: ResourceAssignment): ResourceResolution = withContext(Dispatchers.IO) {

        val resourceResolution = ResourceResolution()
        resourceResolution.id = UUID.randomUUID().toString()
        resourceResolution.artifactName = artifactPrefix
        resourceResolution.blueprintVersion = blueprintVersion
        resourceResolution.blueprintName = blueprintName
        resourceResolution.resolutionKey = resolutionKey
        resourceResolution.resourceType = resourceType
        resourceResolution.resourceId = resourceId
        resourceResolution.value = JacksonUtils.getValue(resourceAssignment.property?.value!!).toString()
        resourceResolution.name = resourceAssignment.name
        resourceResolution.dictionaryName = resourceAssignment.dictionaryName
        resourceResolution.dictionaryVersion = resourceAssignment.version
        resourceResolution.dictionarySource = resourceAssignment.dictionarySource
        resourceResolution.status = resourceAssignment.status

        try {
            resourceResolutionRepository.saveAndFlush(resourceResolution)
        } catch (ex: Exception) {
            throw BluePrintException("Failed to store resource resolution result.", ex)
        }
    }
}
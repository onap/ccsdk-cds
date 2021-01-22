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
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ResourceResolutionDBService(private val resourceResolutionRepository: ResourceResolutionRepository) {

    private val log = LoggerFactory.getLogger(ResourceResolutionDBService::class.toString())

    suspend fun findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyAndOccurrence(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        key: String,
        occurrence: Int,
        artifactPrefix: String
    ): List<ResourceResolution> {
        return try {
            val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

            val blueprintVersion = metadata[BlueprintConstants.METADATA_TEMPLATE_VERSION]!!
            val blueprintName = metadata[BlueprintConstants.METADATA_TEMPLATE_NAME]!!

            resourceResolutionRepository.findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyAndOccurrence(
                blueprintName,
                blueprintVersion,
                artifactPrefix,
                key,
                occurrence
            )
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    suspend fun findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResourceIdAndResourceTypeAndOccurrence(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        resourceId: String,
        resourceType: String,
        occurrence: Int,
        artifactPrefix: String
    ): List<ResourceResolution> {
        return try {

            val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

            val blueprintVersion = metadata[BlueprintConstants.METADATA_TEMPLATE_VERSION]!!
            val blueprintName = metadata[BlueprintConstants.METADATA_TEMPLATE_NAME]!!

            resourceResolutionRepository.findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResourceIdAndResourceTypeAndOccurrence(
                blueprintName,
                blueprintVersion,
                artifactPrefix,
                resourceId,
                resourceType,
                occurrence
            )
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    suspend fun readValue(
        blueprintName: String,
        blueprintVersion: String,
        artifactPrefix: String,
        resolutionKey: String,
        name: String
    ): ResourceResolution = withContext(Dispatchers.IO) {

        resourceResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndName(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix,
            name
        )
    }

    suspend fun readWithResolutionKey(
        blueprintName: String,
        blueprintVersion: String,
        artifactPrefix: String,
        resolutionKey: String
    ): List<ResourceResolution> = withContext(Dispatchers.IO) {

        resourceResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix
        )
    }

    suspend fun readWithResourceIdAndResourceType(
        blueprintName: String,
        blueprintVersion: String,
        resourceId: String,
        resourceType: String
    ): List<ResourceResolution> =
        withContext(Dispatchers.IO) {

            resourceResolutionRepository.findByBlueprintNameAndBlueprintVersionAndResourceIdAndResourceType(
                blueprintName,
                blueprintVersion,
                resourceId,
                resourceType
            )
        }

    suspend fun write(
        properties: Map<String, Any>,
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        artifactPrefix: String,
        resourceAssignment: ResourceAssignment
    ): ResourceResolution = withContext(Dispatchers.IO) {

        val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

        val blueprintVersion = metadata[BlueprintConstants.METADATA_TEMPLATE_VERSION]!!
        val blueprintName = metadata[BlueprintConstants.METADATA_TEMPLATE_NAME]!!

        val resolutionKey = properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] as String
        val resourceId = properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] as String
        val resourceType = properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] as String
        val occurrence = properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] as Int

        write(
            blueprintName,
            blueprintVersion,
            resolutionKey,
            resourceId,
            resourceType,
            artifactPrefix,
            resourceAssignment,
            occurrence
        )
    }

    suspend fun write(
        blueprintName: String,
        blueprintVersion: String,
        resolutionKey: String,
        resourceId: String,
        resourceType: String,
        artifactPrefix: String,
        resourceAssignment: ResourceAssignment,
        occurrence: Int = 0
    ): ResourceResolution = withContext(Dispatchers.IO) {

        val resourceResolution = ResourceResolution()
        resourceResolution.id = UUID.randomUUID().toString()
        resourceResolution.artifactName = artifactPrefix
        resourceResolution.occurrence = occurrence
        resourceResolution.blueprintVersion = blueprintVersion
        resourceResolution.blueprintName = blueprintName
        resourceResolution.resolutionKey = resolutionKey
        resourceResolution.resourceType = resourceType
        resourceResolution.resourceId = resourceId
        resourceResolution.value = resourceAssignment.property?.value?.let {
            if (BlueprintConstants.STATUS_SUCCESS == resourceAssignment.status)
                JacksonUtils.getValue(it).toString()
            else ""
        } ?: ""
        resourceResolution.name = resourceAssignment.name
        resourceResolution.dictionaryName = resourceAssignment.dictionaryName
        resourceResolution.dictionaryVersion = resourceAssignment.version
        resourceResolution.dictionarySource = resourceAssignment.dictionarySource
        resourceResolution.status = resourceAssignment.status ?: BlueprintConstants.STATUS_FAILURE

        try {
            resourceResolutionRepository.saveAndFlush(resourceResolution)
        } catch (ex: Exception) {
            throw BlueprintException("Failed to store resource resolution result.", ex)
        }
    }

    /**
     * This is a deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKey method to delete resources
     * associated to a specific resolution-key
     *
     * @param blueprintName name of the CBA
     * @param blueprintVersion version of the CBA
     * @param artifactName name of the artifact
     * @param resolutionKey value of the resolution-key
     */
    suspend fun deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKey(
        blueprintName: String,
        blueprintVersion: String,
        artifactName: String,
        resolutionKey: String
    ) {
        resourceResolutionRepository.deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKey(
            blueprintName,
            blueprintVersion,
            artifactName,
            resolutionKey
        )
    }
}

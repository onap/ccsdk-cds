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
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.util.UUID

@Service
class ResourceResolutionDBService(private val resourceResolutionRepository: ResourceResolutionRepository) {

    private val log = LoggerFactory.getLogger(ResourceResolutionDBService::class.toString())

    suspend fun findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyAndOccurrence(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        key: String,
        occurrence: Int,
        artifactPrefix: String
    ): List<ResourceResolution> {
        return try {
            val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

            val blueprintVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]!!
            val blueprintName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]!!

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
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        resourceId: String,
        resourceType: String,
        occurrence: Int,
        artifactPrefix: String
    ): List<ResourceResolution> {
        return try {

            val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

            val blueprintVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]!!
            val blueprintName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]!!

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
    ): ResourceResolution? = withContext(Dispatchers.IO) {

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

    /**
     * This returns the resolutions of first N 'occurrences'.
     *
     * @param blueprintName
     * @param blueprintVersion
     * @param artifactPrefix
     * @param resolutionKey
     * @param firstN
     */
    suspend fun findFirstNOccurrences(
        blueprintName: String,
        blueprintVersion: String,
        artifactPrefix: String,
        resolutionKey: String,
        firstN: Int
    ): Map<Int, List<ResourceResolution>> = withContext(Dispatchers.IO) {

        resourceResolutionRepository.findFirstNOccurrences(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix,
            firstN
        ).groupBy(ResourceResolution::occurrence).toSortedMap(reverseOrder())
    }

    /**
     * This returns the resolutions of last N 'occurrences'.
     *
     * @param blueprintName
     * @param blueprintVersion
     * @param artifactPrefix
     * @param resolutionKey
     * @param lastN
     */
    suspend fun findLastNOccurrences(
        blueprintName: String,
        blueprintVersion: String,
        artifactPrefix: String,
        resolutionKey: String,
        lastN: Int
    ): Map<Int, List<ResourceResolution>> = withContext(Dispatchers.IO) {

        resourceResolutionRepository.findLastNOccurrences(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix,
            lastN
        ).groupBy(ResourceResolution::occurrence).toSortedMap(reverseOrder())
    }

    /**
     * This returns the resolutions with 'occurrence' value between begin and end.
     *
     * @param blueprintName
     * @param blueprintVersion
     * @param artifactPrefix
     * @param resolutionKey
     * @param begin
     * @param end
     */
    suspend fun findOccurrencesWithinRange(
        blueprintName: String,
        blueprintVersion: String,
        artifactPrefix: String,
        resolutionKey: String,
        begin: Int,
        end: Int
    ): Map<Int, List<ResourceResolution>> = withContext(Dispatchers.IO) {

        resourceResolutionRepository.findOccurrencesWithinRange(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix,
            begin,
            end
        ).groupBy(ResourceResolution::occurrence).toSortedMap(reverseOrder())
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
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        artifactPrefix: String,
        resourceAssignment: ResourceAssignment
    ): ResourceResolution = withContext(Dispatchers.IO) {

        val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

        val blueprintVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]!!
        val blueprintName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]!!

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
            if (BluePrintConstants.STATUS_SUCCESS == resourceAssignment.status)
                JacksonUtils.getValue(it).toString()
            else ""
        } ?: ""
        resourceResolution.name = resourceAssignment.name
        resourceResolution.dictionaryName = resourceAssignment.dictionaryName
        resourceResolution.dictionaryVersion = resourceAssignment.version
        resourceResolution.dictionarySource = resourceAssignment.dictionarySource
        resourceResolution.status = resourceAssignment.status ?: BluePrintConstants.STATUS_FAILURE

        try {
            resourceResolutionRepository.saveAndFlush(resourceResolution)
        } catch (ex: Exception) {
            throw BluePrintException("Failed to store resource resolution result.", ex)
        }
    }

    /**
     * This method to deletes resources associated to a specific resolution-key
     *
     * @param blueprintName name of the CBA
     * @param blueprintVersion version of the CBA
     * @param artifactName name of the artifact
     * @param resolutionKey value of the resolution-key
     * @param lastNOccurrences number of occurrences to delete starting from the last,
     * all occurrences will be deleted when null
     *
     * @return number of deleted rows
     */
    fun deleteResources(
        blueprintName: String,
        blueprintVersion: String,
        artifactName: String,
        resolutionKey: String,
        lastNOccurrences: Int?
    ): Int = lastNOccurrences?.let {
        if (lastNOccurrences < 0) {
            throw IllegalArgumentException("last N occurrences must be a positive integer")
        }
        resourceResolutionRepository.deleteLastNOccurrences(
            blueprintName,
            blueprintVersion,
            artifactName,
            resolutionKey,
            it
        )
    } ?: resourceResolutionRepository.deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKey(
        blueprintName,
        blueprintVersion,
        artifactName,
        resolutionKey
    )

    /**
     * This method to deletes resources associated to a specific resourceType and resourceId
     *
     * @param blueprintName name of the CBA
     * @param blueprintVersion version of the CBA
     * @param artifactName name of the artifact
     * @param resourceType value of the resourceType
     * @param resourceId value of the resourceId
     * @param lastNOccurrences number of occurrences to delete starting from the last,
     * all occurrences will be deleted when null
     *
     * @return number of deleted rows
     */
    fun deleteResources(
        blueprintName: String,
        blueprintVersion: String,
        artifactName: String,
        resourceType: String,
        resourceId: String,
        lastNOccurrences: Int?
    ): Int = lastNOccurrences?.let {
        if (lastNOccurrences < 0) {
            throw IllegalArgumentException("last N occurrences must be a positive integer")
        }
        resourceResolutionRepository.deleteLastNOccurrences(
            blueprintName,
            blueprintVersion,
            artifactName,
            resourceType,
            resourceId,
            it
        )
    } ?: resourceResolutionRepository.deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResourceTypeAndResourceId(
        blueprintName,
        blueprintVersion,
        artifactName,
        resourceType,
        resourceId
    )

    suspend fun deleteResourceResolutionList(listResourceResolution: List<ResourceResolution>) = withContext(Dispatchers.IO) {
        try {
            resourceResolutionRepository.deleteInBatch(listResourceResolution)
        } catch (ex: Exception) {
            throw BluePrintException("Failed to batch delete resource resolution", ex)
        }
    }

    /**
     * This method returns the (highest occurrence + 1) of resource resolutions if present in DB, returns 1 otherwise.
     * The 'occurrence' is used to persist new resource resolution in the DB.
     *
     * @param resolutionKey
     * @param blueprintName
     * @param blueprintVersion
     * @param artifactPrefix
     */
    suspend fun findNextOccurrenceByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
        resolutionKey: String,
        blueprintName: String,
        blueprintVersion: String,
        artifactPrefix: String
    ) = withContext(Dispatchers.IO) {
        val maxOccurrence = resourceResolutionRepository.findMaxOccurrenceByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix
        )
        maxOccurrence?.inc() ?: 1
    }

    /**
     * This method returns the (highest occurrence + 1) of resource resolutions if present in DB, returns 1 otherwise.
     * The 'occurrence' is used to persist new resource resolution in the DB.
     *
     * @param blueprintName
     * @param blueprintVersion
     * @param resourceId
     * @param resourceType
     */
    suspend fun findNextOccurrenceByBlueprintNameAndBlueprintVersionAndResourceIdAndResourceType(
        blueprintName: String,
        blueprintVersion: String,
        resourceId: String,
        resourceType: String
    ) = withContext(Dispatchers.IO) {
        val maxOccurrence = resourceResolutionRepository.findMaxOccurrenceByBlueprintNameAndBlueprintVersionAndResourceIdAndResourceType(
            blueprintName,
            blueprintVersion,
            resourceId,
            resourceType
        )
        maxOccurrence?.inc() ?: 1
    }
}

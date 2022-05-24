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
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TemplateResolutionService(private val templateResolutionRepository: TemplateResolutionRepository) {

    private val log = LoggerFactory.getLogger(TemplateResolutionService::class.toString())

    suspend fun findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        artifactPrefix: String,
        resolutionKey: String
    ): String =
        withContext(Dispatchers.IO) {

            val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

            val blueprintVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]!!
            val blueprintName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]!!

            findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
                blueprintName,
                blueprintVersion,
                artifactPrefix,
                resolutionKey
            )
        }

    suspend fun findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
        blueprintName: String,
        blueprintVersion: String,
        artifactPrefix: String,
        resolutionKey: String,
        occurrence: Int = 1
    ): String =
        withContext(Dispatchers.IO) {

            templateResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                resolutionKey,
                blueprintName,
                blueprintVersion,
                artifactPrefix,
                occurrence
            )?.result ?: throw EmptyResultDataAccessException(1)
        }

    suspend fun findResolutionKeysByBlueprintNameAndBlueprintVersionAndArtifactName(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        artifactPrefix: String,
        occurrence: Int = 1
    ): List<String> =
        withContext(Dispatchers.IO) {

            val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

            val blueprintVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]!!
            val blueprintName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]!!

            templateResolutionRepository.findResolutionKeysByBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                blueprintName,
                blueprintVersion,
                artifactPrefix,
                occurrence
            ) ?: throw EmptyResultDataAccessException(1)
        }

    suspend fun findArtifactNamesAndResolutionKeysByBlueprintNameAndBlueprintVersion(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        occurrence: Int = 1
    ): Map<String, List<String>> =
        withContext(Dispatchers.IO) {

            val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!

            val blueprintVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]!!
            val blueprintName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]!!

            val resultList = templateResolutionRepository.findArtifactNamesAndResolutionKeysByBlueprintNameAndBlueprintVersionAndOccurrence(
                blueprintName,
                blueprintVersion,
                occurrence
            ) ?: throw EmptyResultDataAccessException(1)

            var resultMap: MutableMap<String, MutableList<String>> = mutableMapOf()

            resultList.forEach { it ->
                if (!resultMap.containsKey(it.getArtifactName())) {
                    resultMap[it.getArtifactName()] = mutableListOf(it.getResolutionKey())
                } else {
                    resultMap[it.getArtifactName()]!!.add(it.getResolutionKey())
                }
            }
            resultMap
                .mapValues { it.value.toList() }
                .toMap()
        }

    suspend fun findByResoureIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactName(
        blueprintName: String,
        blueprintVersion: String,
        artifactPrefix: String,
        resourceId: String,
        resourceType: String,
        occurrence: Int = 1
    ): String =
        withContext(Dispatchers.IO) {

            templateResolutionRepository.findByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                resourceId,
                resourceType,
                blueprintName,
                blueprintVersion,
                artifactPrefix,
                occurrence
            )?.result!!
        }

    suspend fun write(
        properties: Map<String, Any>,
        result: String,
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        artifactPrefix: String
    ): TemplateResolution = withContext(Dispatchers.IO) {

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
            artifactPrefix,
            result,
            occurrence,
            resolutionKey,
            resourceId,
            resourceType
        )
    }

    suspend fun write(
        blueprintName: String,
        blueprintVersion: String,
        artifactPrefix: String,
        template: String,
        occurrence: Int = 1,
        resolutionKey: String = "",
        resourceId: String = "",
        resourceType: String = ""
    ): TemplateResolution =
        withContext(Dispatchers.IO) {

            val resourceResolutionResult = TemplateResolution()
            resourceResolutionResult.id = UUID.randomUUID().toString()
            resourceResolutionResult.artifactName = artifactPrefix
            resourceResolutionResult.blueprintVersion = blueprintVersion
            resourceResolutionResult.blueprintName = blueprintName
            resourceResolutionResult.resolutionKey = resolutionKey
            resourceResolutionResult.resourceId = resourceId
            resourceResolutionResult.resourceType = resourceType
            resourceResolutionResult.result = template
            resourceResolutionResult.occurrence = occurrence

            // Overwrite template resolution-key of resourceId/resourceType already existant
            if (resolutionKey.isNotEmpty()) {
                templateResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                    resolutionKey, blueprintName, blueprintVersion, artifactPrefix, occurrence
                )?.let {
                    log.info(
                        "Overwriting template resolution for blueprintName=($blueprintVersion), blueprintVersion=($blueprintName), " +
                            "artifactName=($artifactPrefix) and resolutionKey=($resolutionKey)"
                    )
                    templateResolutionRepository.deleteByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                        resolutionKey,
                        blueprintName,
                        blueprintVersion,
                        artifactPrefix,
                        occurrence
                    )
                }
            } else if (resourceId.isNotEmpty() && resourceType.isNotEmpty()) {
                templateResolutionRepository.findByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                    resourceId, resourceType, blueprintName, blueprintVersion, artifactPrefix, occurrence
                )?.let {
                    log.info(
                        "Overwriting template resolution for blueprintName=($blueprintVersion), blueprintVersion=($blueprintName), " +
                            "artifactName=($artifactPrefix), resourceId=($resourceId) and resourceType=($resourceType)"
                    )
                    templateResolutionRepository.deleteByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                        resourceId,
                        resourceType,
                        blueprintName,
                        blueprintVersion,
                        artifactPrefix,
                        occurrence
                    )
                }
            }
            try {
                log.info(
                    "Writing out template_resolution result: bpName: $blueprintName bpVer $blueprintVersion resKey:$resolutionKey" +
                        " (resourceId: $resourceId resourceType: $resourceType) occurrence:$occurrence"
                )
                templateResolutionRepository.saveAndFlush(resourceResolutionResult)
            } catch (ex: DataIntegrityViolationException) {
                log.error(
                    "Error writing out template_resolution result: bpName: $blueprintName bpVer $blueprintVersion resKey:$resolutionKey" +
                        " (resourceId: $resourceId resourceType: $resourceType) occurrence:$occurrence error: {}",
                    ex.message
                )
                throw BluePrintException("Failed to store resource api result.", ex)
            }
        }
    /**
     * This returns the templates of first N 'occurrences'.
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
    ): Map<Int, List<TemplateResolution>> = withContext(Dispatchers.IO) {

        templateResolutionRepository.findFirstNOccurrences(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix,
            firstN
        ).groupBy(TemplateResolution::occurrence).toSortedMap(reverseOrder())
    }

    /**
     * This returns the templates of last N 'occurrences'.
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
    ): Map<Int, List<TemplateResolution>> = withContext(Dispatchers.IO) {

        templateResolutionRepository.findLastNOccurrences(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix,
            lastN
        ).groupBy(TemplateResolution::occurrence).toSortedMap(reverseOrder())
    }

    /**
     * This returns the templates with 'occurrence' value between begin and end.
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
    ): Map<Int, List<TemplateResolution>> = withContext(Dispatchers.IO) {

        templateResolutionRepository.findOccurrencesWithinRange(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix,
            begin,
            end
        ).groupBy(TemplateResolution::occurrence).toSortedMap(reverseOrder())
    }

    suspend fun readWithResolutionKey(
        blueprintName: String,
        blueprintVersion: String,
        artifactPrefix: String,
        resolutionKey: String
    ): List<TemplateResolution> = withContext(Dispatchers.IO) {

        templateResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
            resolutionKey,
            blueprintName,
            blueprintVersion,
            artifactPrefix
        )
    }
}

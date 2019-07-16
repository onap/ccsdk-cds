/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018-2019 IBM, Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolution
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionDBService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.TemplateResolutionService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.utils.BulkResourceSequencingUtils
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

interface ResourceResolutionService {

    fun registeredResourceSources(): List<String>

    suspend fun resolveFromDatabase(bluePrintRuntimeService: BluePrintRuntimeService<*>, artifactTemplate: String,
                                    resolutionKey: String): String

    suspend fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                                 artifactNames: List<String>, properties: Map<String, Any>): MutableMap<String, JsonNode>

    suspend fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                                 artifactPrefix: String, properties: Map<String, Any>): String

    suspend fun resolveResourceAssignments(blueprintRuntimeService: BluePrintRuntimeService<*>,
                                           resourceDefinitions: MutableMap<String, ResourceDefinition>,
                                           resourceAssignments: MutableList<ResourceAssignment>,
                                           artifactPrefix: String,
                                           properties: Map<String, Any>)
}

@Service(ResourceResolutionConstants.SERVICE_RESOURCE_RESOLUTION)
open class ResourceResolutionServiceImpl(private var applicationContext: ApplicationContext,
                                         private var templateResolutionDBService: TemplateResolutionService,
                                         private var blueprintTemplateService: BluePrintTemplateService,
                                         private var resourceResolutionDBService: ResourceResolutionDBService) :
    ResourceResolutionService {

    private val log = LoggerFactory.getLogger(ResourceResolutionService::class.java)

    override fun registeredResourceSources(): List<String> {
        return applicationContext.getBeanNamesForType(ResourceAssignmentProcessor::class.java)
            .filter { it.startsWith(ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR) }
            .map { it.substringAfter(ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR) }
    }

    override suspend fun resolveFromDatabase(bluePrintRuntimeService: BluePrintRuntimeService<*>,
                                             artifactTemplate: String,
                                             resolutionKey: String): String {
        return templateResolutionDBService.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
            bluePrintRuntimeService,
            artifactTemplate,
            resolutionKey)
    }

    override suspend fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                                          artifactNames: List<String>,
                                          properties: Map<String, Any>): MutableMap<String, JsonNode> {


        val resourceAssignmentRuntimeService =
            ResourceAssignmentUtils.transformToRARuntimeService(bluePrintRuntimeService, artifactNames.toString())

        val resolvedParams: MutableMap<String, JsonNode> = hashMapOf()
        artifactNames.forEach { artifactName ->
            val resolvedContent = resolveResources(resourceAssignmentRuntimeService, nodeTemplateName,
                artifactName, properties)

/*            val resolvedResult: JsonNode
            resolvedResult = if (JacksonUtils.isAValidJsonNode(resolvedContent))
                JacksonUtils.readValue(resolvedContent)
            else
                resolvedContent.asJsonPrimitive()*/

            resolvedParams[artifactName] = resolvedContent.asJsonType()

        }
        return resolvedParams
    }


    override suspend fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                                          artifactPrefix: String, properties: Map<String, Any>): String {

        // Velocity Artifact Definition Name
        val artifactTemplate = "$artifactPrefix-template"
        // Resource Assignment Artifact Definition Name
        val artifactMapping = "$artifactPrefix-mapping"

        val resolvedContent: String
        log.info("Resolving resource for template artifact($artifactTemplate) with resource assignment artifact($artifactMapping)")

        val resourceAssignmentContent =
            bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactMapping)

        val resourceAssignments: MutableList<ResourceAssignment> =
            JacksonUtils.getListFromJson(resourceAssignmentContent, ResourceAssignment::class.java)
                    as? MutableList<ResourceAssignment>
                ?: throw BluePrintProcessorException("couldn't get Dictionary Definitions")

        if (isToStore(properties)) {
            val existingResourceResolution = isNewResolution(bluePrintRuntimeService, properties, artifactPrefix)
            if (existingResourceResolution.isNotEmpty()) {
                updateResourceAssignmentWithExisting(existingResourceResolution, resourceAssignments)
            }
        }

        // Get the Resource Dictionary Name
        val resourceDefinitions: MutableMap<String, ResourceDefinition> = ResourceAssignmentUtils
            .resourceDefinitions(bluePrintRuntimeService.bluePrintContext().rootPath)

        // Resolve resources
        resolveResourceAssignments(bluePrintRuntimeService,
            resourceDefinitions,
            resourceAssignments,
            artifactPrefix,
            properties)

        val resolvedParamJsonContent =
            ResourceAssignmentUtils.generateResourceDataForAssignments(resourceAssignments.toList())

        resolvedContent = blueprintTemplateService.generateContent(bluePrintRuntimeService, nodeTemplateName,
            artifactTemplate, resolvedParamJsonContent)

        if (isToStore(properties)) {
            templateResolutionDBService.write(properties, resolvedContent, bluePrintRuntimeService, artifactPrefix)
            log.info("Template resolution saved into database successfully : ($properties)")
        }

        return resolvedContent
    }

    /**
     * Iterate the Batch, get the Resource Assignment, dictionary Name, Look for the Resource definition for the
     * name, then get the type of the Resource Definition, Get the instance for the Resource Type and process the
     * request.
     */
    override suspend fun resolveResourceAssignments(blueprintRuntimeService: BluePrintRuntimeService<*>,
                                                    resourceDefinitions: MutableMap<String, ResourceDefinition>,
                                                    resourceAssignments: MutableList<ResourceAssignment>,
                                                    artifactPrefix: String,
                                                    properties: Map<String, Any>) {

        val bulkSequenced = BulkResourceSequencingUtils.process(resourceAssignments)
        val resourceAssignmentRuntimeService = blueprintRuntimeService as ResourceAssignmentRuntimeService

        coroutineScope {
            bulkSequenced.forEach { batchResourceAssignments ->
                // Execute Non Dependent Assignments in parallel ( ie asynchronously )
                val deferred = batchResourceAssignments
                    .filter { it.name != "*" && it.name != "start" }
                    .filter { it.status != BluePrintConstants.STATUS_SUCCESS }
                    .map { resourceAssignment ->
                        async {
                            val dictionaryName = resourceAssignment.dictionaryName
                            val dictionarySource = resourceAssignment.dictionarySource

                            val processorName = processorName(dictionaryName!!, dictionarySource!!, resourceDefinitions)

                            val resourceAssignmentProcessor =
                                applicationContext.getBean(processorName) as? ResourceAssignmentProcessor
                                    ?: throw BluePrintProcessorException("failed to get resource processor ($processorName) " +
                                            "for resource assignment(${resourceAssignment.name})")
                            try {
                                // Set BluePrint Runtime Service
                                resourceAssignmentProcessor.raRuntimeService = resourceAssignmentRuntimeService
                                // Set Resource Dictionaries
                                resourceAssignmentProcessor.resourceDictionaries = resourceDefinitions
                                // Invoke Apply Method
                                resourceAssignmentProcessor.applyNB(resourceAssignment)

                                if (isToStore(properties)) {
                                    resourceResolutionDBService.write(properties,
                                        blueprintRuntimeService,
                                        artifactPrefix,
                                        resourceAssignment)
                                    log.info("Resource resolution saved into database successfully : ($resourceAssignment)")
                                }

                                // Set errors from RA
                                blueprintRuntimeService.setBluePrintError(resourceAssignmentRuntimeService.getBluePrintError())
                            } catch (e: RuntimeException) {
                                log.error("Fail in processing ${resourceAssignment.name}", e)
                                throw BluePrintProcessorException(e)
                            }
                        }
                    }
                log.debug("Resolving (${deferred.size})resources parallel.")
                deferred.awaitAll()
            }
        }

    }

    /**
     * If the Source instance is "input", then it is not mandatory to have source Resource Definition, So it can
     *  derive the default input processor.
     */
    private fun processorName(dictionaryName: String, dictionarySource: String,
                              resourceDefinitions: MutableMap<String, ResourceDefinition>): String {
        val processorName: String = when (dictionarySource) {
            "input" -> {
                "${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-input"
            }
            "default" -> {
                "${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-default"
            }
            else -> {
                val resourceDefinition = resourceDefinitions[dictionaryName]
                    ?: throw BluePrintProcessorException("couldn't get resource dictionary definition for $dictionaryName")

                val resourceSource = resourceDefinition.sources[dictionarySource]
                    ?: throw BluePrintProcessorException("couldn't get resource definition $dictionaryName source($dictionarySource)")

                ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR.plus(resourceSource.type)
            }
        }
        checkNotEmpty(processorName) {
            "couldn't get processor name for resource dictionary definition($dictionaryName) source($dictionarySource)"
        }

        return processorName

    }

    // Check whether to store or not the resolution of resource and template
    private fun isToStore(properties: Map<String, Any>): Boolean {
        return properties.containsKey(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT)
                && properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT] as Boolean
    }

    // Check whether resolution already exist in the database for the specified resolution-key or resourceId/resourceType
    private suspend fun isNewResolution(bluePrintRuntimeService: BluePrintRuntimeService<*>,
                                        properties: Map<String, Any>,
                                        artifactPrefix: String): List<ResourceResolution> {
        val occurrence = properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] as Int
        val resolutionKey = properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] as String
        val resourceId = properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] as String
        val resourceType = properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] as String

        if (resolutionKey.isNotEmpty()) {
            val existingResourceAssignments =
                resourceResolutionDBService.findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyAndOccurrence(
                    bluePrintRuntimeService,
                    resolutionKey,
                    occurrence,
                    artifactPrefix)
            if (existingResourceAssignments.isNotEmpty()) {
                log.info("Resolution with resolutionKey=($resolutionKey) already exist - will resolve all resources not already resolved.",
                    resolutionKey)
            }
            return existingResourceAssignments
        } else if (resourceId.isNotEmpty() && resourceType.isNotEmpty()) {
            val existingResourceAssignments =
                resourceResolutionDBService.findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResourceIdAndResourceTypeAndOccurrence(
                    bluePrintRuntimeService,
                    resourceId,
                    resourceType,

                    occurrence,
                    artifactPrefix)
            if (existingResourceAssignments.isNotEmpty()) {
                log.info("Resolution with resourceId=($resourceId) and resourceType=($resourceType) already exist - will resolve " +
                        "all resources not already resolved.")
            }
            return existingResourceAssignments
        }
        return emptyList()
    }

    // Update the resource assignment list with the status of the resource that have already been resolved
    private fun updateResourceAssignmentWithExisting(resourceResolutionList: List<ResourceResolution>,
                                                     resourceAssignmentList: MutableList<ResourceAssignment>) {
        resourceResolutionList.forEach { resourceResolution ->
            if (resourceResolution.status == BluePrintConstants.STATUS_SUCCESS) {
                resourceAssignmentList.forEach {
                    if (compareOne(resourceResolution, it)) {
                        log.info("Resource ({}) already resolve: value=({})", it.name, resourceResolution.value)
                        it.property!!.value = resourceResolution.value!!.asJsonPrimitive()
                        it.status = resourceResolution.status
                    }
                }
            }
        }
    }

    // Comparision between what we have in the database vs what we have to assign.
    private fun compareOne(resourceResolution: ResourceResolution, resourceAssignment: ResourceAssignment): Boolean {
        return (resourceResolution.name == resourceAssignment.name
                && resourceResolution.dictionaryName == resourceAssignment.dictionaryName
                && resourceResolution.dictionarySource == resourceAssignment.dictionarySource
                && resourceResolution.dictionaryVersion == resourceAssignment.version)
    }

}

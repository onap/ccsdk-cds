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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionDBService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionResultService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotEmpty
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
                                 artifactNames: List<String>, properties: Map<String, Any>): MutableMap<String, String>

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
                                         private var resolutionResultService: ResourceResolutionResultService,
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
        return resolutionResultService.read(bluePrintRuntimeService, artifactTemplate, resolutionKey)
    }

    override suspend fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                                          artifactNames: List<String>,
                                          properties: Map<String, Any>): MutableMap<String, String> {

        val resolvedParams: MutableMap<String, String> = hashMapOf()
        artifactNames.forEach { artifactName ->
            val resolvedContent = resolveResources(bluePrintRuntimeService, nodeTemplateName,
                artifactName, properties)
            resolvedParams[artifactName] = resolvedContent
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

        if (properties.containsKey(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT)
            && properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT] as Boolean) {
            resolutionResultService.write(properties, resolvedContent, bluePrintRuntimeService, artifactPrefix)
            log.info("template resolution saved into database successfully : ($properties)")
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
        val resourceAssignmentRuntimeService =
            ResourceAssignmentUtils.transformToRARuntimeService(blueprintRuntimeService, artifactPrefix)

        coroutineScope {
            bulkSequenced.forEach { batchResourceAssignments ->
                // Execute Non Dependent Assignments in parallel ( ie asynchronously )
                val deferred = batchResourceAssignments.filter { it.name != "*" && it.name != "start" }
                    .map { resourceAssignment ->
                        async {
                            val dictionaryName = resourceAssignment.dictionaryName
                            val dictionarySource = resourceAssignment.dictionarySource
                            /**
                             * Get the Processor name
                             */
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

                                if (properties.containsKey(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT)
                                    && properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT] as Boolean) {
                                    resourceResolutionDBService.write(properties,
                                        blueprintRuntimeService,
                                        artifactPrefix,
                                        resourceAssignment)
                                    log.info("resolution saved into database successfully : ($resourceAssignment)")
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
}

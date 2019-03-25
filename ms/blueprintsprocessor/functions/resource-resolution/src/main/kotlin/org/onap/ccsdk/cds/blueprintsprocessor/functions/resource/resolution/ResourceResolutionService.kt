/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018 IBM.
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

import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionResultService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotEmptyOrThrow
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.utils.BulkResourceSequencingUtils
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.io.File

interface ResourceResolutionService {

    fun registeredResourceSources(): List<String>

    fun resolveFromDatabase(bluePrintRuntimeService: BluePrintRuntimeService<*>, artifactTemplate: String,
                            resolutionKey: String): String

    fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                         artifactNames: List<String>, properties: Map<String, Any>): MutableMap<String, String>

    fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                         artifactPrefix: String, properties: Map<String, Any>): String

    fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                         artifactMapping: String, artifactTemplate: String?): String

    fun resolveResourceAssignments(blueprintRuntimeService: BluePrintRuntimeService<*>,
                                   resourceDictionaries: MutableMap<String, ResourceDefinition>,
                                   resourceAssignments: MutableList<ResourceAssignment>,
                                   identifierName: String)
}

@Service(ResourceResolutionConstants.SERVICE_RESOURCE_RESOLUTION)
open class ResourceResolutionServiceImpl(private var applicationContext: ApplicationContext,
                                         private var resolutionResultService: ResourceResolutionResultService) :
        ResourceResolutionService {

    private val log = LoggerFactory.getLogger(ResourceResolutionService::class.java)

    override fun registeredResourceSources(): List<String> {
        return applicationContext.getBeanNamesForType(ResourceAssignmentProcessor::class.java)
                .filter { it.startsWith(ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR) }
                .map { it.substringAfter(ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR) }
    }

    override fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                                  artifactNames: List<String>,
                                  properties: Map<String, Any>): MutableMap<String, String> {

        val resolvedParams: MutableMap<String, String> = hashMapOf()
        artifactNames.forEach { artifactName ->
            val resolvedContent = resolveResources(bluePrintRuntimeService, nodeTemplateName, artifactName, properties)
            resolvedParams[artifactName] = resolvedContent
        }
        return resolvedParams
    }

    override fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                                  artifactPrefix: String, properties: Map<String, Any>): String {

        // Velocity Artifact Definition Name
        val artifactTemplate = "$artifactPrefix-template"
        // Resource Assignment Artifact Definition Name
        val artifactMapping = "$artifactPrefix-mapping"

        val result = resolveResources(bluePrintRuntimeService, nodeTemplateName, artifactMapping, artifactTemplate)

        if (properties.containsKey(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT)
                && properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT] as Boolean) {
            resolutionResultService.write(properties, result, bluePrintRuntimeService, artifactPrefix)
        }

        return result
    }

    override fun resolveFromDatabase(bluePrintRuntimeService: BluePrintRuntimeService<*>, artifactTemplate: String,
                                     resolutionKey: String): String {
        return resolutionResultService.read(bluePrintRuntimeService, artifactTemplate, resolutionKey)
    }

    override fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                                  artifactMapping: String, artifactTemplate: String?): String {

        var resolvedContent = ""
        log.info("Resolving resource for template artifact($artifactTemplate) with resource assignment artifact($artifactMapping)")

        val identifierName = artifactTemplate ?: "no-template"

        val resourceAssignmentContent =
                bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactMapping)

        val resourceAssignments: MutableList<ResourceAssignment> =
                JacksonUtils.getListFromJson(resourceAssignmentContent, ResourceAssignment::class.java)
                        as? MutableList<ResourceAssignment>
                        ?: throw BluePrintProcessorException("couldn't get Dictionary Definitions")

        // Get the Resource Dictionary Name
        val dictionaryFile = bluePrintRuntimeService.bluePrintContext().rootPath.plus(File.separator)
                .plus(BluePrintConstants.TOSCA_DEFINITIONS_DIR).plus(File.separator)
                .plus(ResourceResolutionConstants.FILE_NAME_RESOURCE_DEFINITION_TYPES)

        val resourceDictionaries: MutableMap<String, ResourceDefinition> =
                JacksonUtils.getMapFromFile(dictionaryFile, ResourceDefinition::class.java)
                        ?: throw BluePrintProcessorException("couldn't get Dictionary Definitions")

        // Resolve resources
        resolveResourceAssignments(bluePrintRuntimeService, resourceDictionaries, resourceAssignments, identifierName)

        val resolvedParamJsonContent =
                ResourceAssignmentUtils.generateResourceDataForAssignments(resourceAssignments.toList())

        // Check Template is there
        if (artifactTemplate != null) {
            val templateContent =
                    bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactTemplate)
            resolvedContent = BluePrintTemplateService.generateContent(templateContent, resolvedParamJsonContent)
        } else {
            resolvedContent = resolvedParamJsonContent
        }

        return resolvedContent
    }

    /**
     * Iterate the Batch, get the Resource Assignment, dictionary Name, Look for the Resource definition for the
     * name, then get the type of the Resource Definition, Get the instance for the Resource Type and process the
     * request.
     */
    override fun resolveResourceAssignments(blueprintRuntimeService: BluePrintRuntimeService<*>,
                                            resourceDictionaries: MutableMap<String, ResourceDefinition>,
                                            resourceAssignments: MutableList<ResourceAssignment>,
                                            identifierName: String) {

        val bulkSequenced = BulkResourceSequencingUtils.process(resourceAssignments)
        val resourceAssignmentRuntimeService =
                ResourceAssignmentUtils.transformToRARuntimeService(blueprintRuntimeService, identifierName)

        bulkSequenced.map { batchResourceAssignments ->
            batchResourceAssignments.filter { it.name != "*" && it.name != "start" }
                    .forEach { resourceAssignment ->
                        val dictionaryName = resourceAssignment.dictionaryName
                        val dictionarySource = resourceAssignment.dictionarySource
                        /**
                         * Get the Processor name
                         */
                        val processorName = processorName(dictionaryName!!, dictionarySource!!, resourceDictionaries)

                        val resourceAssignmentProcessor =
                                applicationContext.getBean(processorName) as? ResourceAssignmentProcessor
                                        ?: throw BluePrintProcessorException("failed to get resource processor for name($processorName) " +
                                                "for resource assignment(${resourceAssignment.name})")
                        try {
                            // Set BluePrint Runtime Service
                            resourceAssignmentProcessor.raRuntimeService = resourceAssignmentRuntimeService
                            // Set Resource Dictionaries
                            resourceAssignmentProcessor.resourceDictionaries = resourceDictionaries
                            // Invoke Apply Method
                            resourceAssignmentProcessor.apply(resourceAssignment)

                            // Set errors from RA
                            blueprintRuntimeService.setBluePrintError(resourceAssignmentRuntimeService.getBluePrintError())
                        } catch (e: RuntimeException) {
                            throw BluePrintProcessorException(e)
                        }
                    }
        }
    }


    /**
     * If the Source instance is "input", then it is not mandatory to have source Resource Definition, So it can
     *  derive the default input processor.
     */
    private fun processorName(dictionaryName: String, dictionarySource: String,
                              resourceDictionaries: MutableMap<String, ResourceDefinition>): String {
        var processorName: String? = null
        when (dictionarySource) {
            "input" -> {
                processorName = "${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-input"
            }
            "default" -> {
                processorName = "${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-default"
            }
            else -> {
                val resourceDefinition = resourceDictionaries[dictionaryName]
                        ?: throw BluePrintProcessorException("couldn't get resource dictionary definition for $dictionaryName")

                val resourceSource = resourceDefinition.sources[dictionarySource]
                        ?: throw BluePrintProcessorException("couldn't get resource definition $dictionaryName source($dictionarySource)")

                processorName = ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR
                        .plus(resourceSource.type)
            }
        }
        checkNotEmptyOrThrow(processorName,
                "couldn't get processor name for resource dictionary definition($dictionaryName) source" +
                        "($dictionarySource)")

        return processorName

    }
}

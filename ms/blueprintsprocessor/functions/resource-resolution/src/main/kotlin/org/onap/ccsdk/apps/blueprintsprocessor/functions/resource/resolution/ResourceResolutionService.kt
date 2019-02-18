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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution

import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintTemplateService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.utils.BulkResourceSequencingUtils
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.io.File

interface ResourceResolutionService {

    fun registeredResourceSources(): List<String>

    fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                         artifactNames: List<String>): MutableMap<String, String>

    fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                         artifactPrefix: String): String

    fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                         artifactMapping: String, artifactTemplate: String?): String

    fun resolveResourceAssignments(blueprintRuntimeService: BluePrintRuntimeService<*>,
                                   resourceDictionaries: MutableMap<String, ResourceDefinition>,
                                   resourceAssignments: MutableList<ResourceAssignment>,
                                   identifierName: String)
}

@Service(ResourceResolutionConstants.SERVICE_RESOURCE_RESOLUTION)
open class ResourceResolutionServiceImpl(private var applicationContext: ApplicationContext) :
        ResourceResolutionService {

    private val log = LoggerFactory.getLogger(ResourceResolutionService::class.java)

    override fun registeredResourceSources(): List<String> {
        return applicationContext.getBeanNamesForType(ResourceAssignmentProcessor::class.java)
                .filter { it.startsWith(ResourceResolutionConstants.PREFIX_RESOURCE_ASSIGNMENT_PROCESSOR) }
                .map { it.substringAfter(ResourceResolutionConstants.PREFIX_RESOURCE_ASSIGNMENT_PROCESSOR) }
    }

    override fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                                  artifactNames: List<String>): MutableMap<String, String> {

        val resolvedParams: MutableMap<String, String> = hashMapOf()
        artifactNames.forEach { artifactName ->
            val resolvedContent = resolveResources(bluePrintRuntimeService, nodeTemplateName, artifactName)
            resolvedParams[artifactName] = resolvedContent
        }
        return resolvedParams
    }

    override fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                                  artifactPrefix: String): String {

        // Velocity Artifact Definition Name
        val artifactTemplate = "$artifactPrefix-template"
        // Resource Assignment Artifact Definition Name
        val artifactMapping = "$artifactPrefix-mapping"

        return resolveResources(bluePrintRuntimeService, nodeTemplateName, artifactMapping, artifactTemplate)
    }


    override fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                                  artifactMapping: String, artifactTemplate: String?): String {

        var resolvedContent = ""
        log.info("Resolving resource for template artifact($artifactTemplate) with resource assignment artifact($artifactMapping)")

        val identifierName = artifactTemplate ?: "no-template"

        val resourceAssignmentContent = bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactMapping)

        val resourceAssignments: MutableList<ResourceAssignment> = JacksonUtils.getListFromJson(resourceAssignmentContent, ResourceAssignment::class.java)
                as? MutableList<ResourceAssignment>
                ?: throw BluePrintProcessorException("couldn't get Dictionary Definitions")

        // Get the Resource Dictionary Name
        val dictionaryFile = bluePrintRuntimeService.bluePrintContext().rootPath.plus(File.separator)
                .plus(BluePrintConstants.TOSCA_DEFINITIONS_DIR).plus(File.separator)
                .plus(ResourceResolutionConstants.FILE_NAME_RESOURCE_DEFINITION_TYPES)

        val resourceDictionaries: MutableMap<String, ResourceDefinition> = JacksonUtils.getMapFromFile(dictionaryFile, ResourceDefinition::class.java)
                ?: throw BluePrintProcessorException("couldn't get Dictionary Definitions")

        // Resolve resources
        resolveResourceAssignments(bluePrintRuntimeService, resourceDictionaries, resourceAssignments, identifierName)

        val resolvedParamJsonContent = ResourceAssignmentUtils.generateResourceDataForAssignments(resourceAssignments.toList())

        // Check Template is there
        if (artifactTemplate != null) {
            val templateContent = bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactTemplate)
            resolvedContent = BluePrintTemplateService.generateContent(templateContent, resolvedParamJsonContent)
        } else {
            resolvedContent = resolvedParamJsonContent
        }
        return resolvedContent
    }

    override fun resolveResourceAssignments(blueprintRuntimeService: BluePrintRuntimeService<*>,
                                            resourceDictionaries: MutableMap<String, ResourceDefinition>,
                                            resourceAssignments: MutableList<ResourceAssignment>,
                                            identifierName: String) {

        val bulkSequenced = BulkResourceSequencingUtils.process(resourceAssignments)
        val resourceAssignmentRuntimeService = ResourceAssignmentUtils.transformToRARuntimeService(blueprintRuntimeService, identifierName)

        bulkSequenced.map { batchResourceAssignments ->
            batchResourceAssignments.filter { it.name != "*" && it.name != "start" }
                    .map { resourceAssignment ->
                        val dictionarySource = resourceAssignment.dictionarySource
                        val processorInstanceName = ResourceResolutionConstants.PREFIX_RESOURCE_ASSIGNMENT_PROCESSOR.plus(dictionarySource)

                        val resourceAssignmentProcessor = applicationContext.getBean(processorInstanceName) as? ResourceAssignmentProcessor
                                ?: throw BluePrintProcessorException("failed to get resource processor for instance name($processorInstanceName) " +
                                        "for resource assignment(${resourceAssignment.name})")
                        try {
                            // Set BluePrint Runtime Service
                            resourceAssignmentProcessor.raRuntimeService = resourceAssignmentRuntimeService
                            // Set Resource Dictionaries
                            resourceAssignmentProcessor.resourceDictionaries = resourceDictionaries
                            // Invoke Apply Method
                            resourceAssignmentProcessor.apply(resourceAssignment)
                        } catch (e: RuntimeException) {
                            resourceAssignmentProcessor.recover(e, resourceAssignment)
                            throw BluePrintProcessorException(e)
                        }
                    }
        }
    }

}

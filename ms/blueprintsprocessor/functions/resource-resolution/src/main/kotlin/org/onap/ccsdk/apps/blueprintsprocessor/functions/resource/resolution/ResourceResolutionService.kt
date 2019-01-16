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
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.utils.BulkResourceSequencingUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.io.File

/**
 * ResourceResolutionService
 * @author Brinda Santh
 * 8/14/2018
 */

@Service
class ResourceResolutionService {

    private val log = LoggerFactory.getLogger(ResourceResolutionService::class.java)

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    fun registeredResourceSources(): List<String> {
        return applicationContext.getBeanNamesForType(ResourceAssignmentProcessor::class.java)
                .filter { it.startsWith(ResourceResolutionConstants.PREFIX_RESOURCE_ASSIGNMENT_PROCESSOR) }
                .map { it.substringAfter(ResourceResolutionConstants.PREFIX_RESOURCE_ASSIGNMENT_PROCESSOR) }
    }


    fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                         artifactNames: List<String>): MutableMap<String, String> {

        val resolvedParams: MutableMap<String, String> = hashMapOf()
        artifactNames.forEach { artifactName ->
            val resolvedContent = resolveResources(bluePrintRuntimeService, nodeTemplateName, artifactName)
            resolvedParams[artifactName] = resolvedContent
        }
        return resolvedParams
    }


    fun resolveResources(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String, artifactName: String): String {

        var resolvedContent = ""
        // Velocity Artifact Definition Name
        val templateArtifactName = "$artifactName-template"
        // Resource Assignment Artifact Definition Name
        val mappingArtifactName = "$artifactName-mapping"

        log.info("Resolving resource for template artifact($templateArtifactName) with resource assignment artifact($mappingArtifactName)")

        val resourceAssignmentContent = bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, mappingArtifactName)

        val resourceAssignments: MutableList<ResourceAssignment> = JacksonUtils.getListFromJson(resourceAssignmentContent, ResourceAssignment::class.java)
                as? MutableList<ResourceAssignment>
                ?: throw BluePrintProcessorException("couldn't get Dictionary Definitions")

        // Get the Resource Dictionary Name
        val dictionaryFile = bluePrintRuntimeService.bluePrintContext().rootPath.plus(File.separator)
                .plus(BluePrintConstants.TOSCA_DEFINITIONS_DIR).plus(File.separator)
                .plus(ResourceResolutionConstants.FILE_NAME_RESOURCE_DEFINITION_TYPES)

        val resourceDictionaries: MutableMap<String, ResourceDefinition> = JacksonUtils.getMapFromFile(dictionaryFile, ResourceDefinition::class.java)
                ?: throw BluePrintProcessorException("couldn't get Dictionary Definitions")

        executeProcessors(bluePrintRuntimeService, resourceDictionaries, resourceAssignments, templateArtifactName)

        // Check Template is there
        val templateContent = bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, mappingArtifactName)

        // TODO ("Generate Param JSON from Resource Assignment")
        val resolvedParamJsonContent = "{}"

        if (templateContent.isNotEmpty()) {
            // TODO ( "Mash Data and Content")
            resolvedContent = "Mashed Content"

        } else {
            resolvedContent = resolvedParamJsonContent
        }
        return resolvedContent
    }


    private fun executeProcessors(blueprintRuntimeService: BluePrintRuntimeService<*>,
                                  resourceDictionaries: MutableMap<String, ResourceDefinition>,
                                  resourceAssignments: MutableList<ResourceAssignment>,
                                  templateArtifactName: String) {

        val bulkSequenced = BulkResourceSequencingUtils.process(resourceAssignments)
        val resourceAssignmentRuntimeService = ResourceAssignmentUtils.transformToRARuntimeService(blueprintRuntimeService, templateArtifactName)

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

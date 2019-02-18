/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.apps.controllerblueprints.service.enhancer

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.utils.ResourceDictionaryUtils
import org.onap.ccsdk.apps.controllerblueprints.service.ResourceDefinitionRepoService
import org.springframework.stereotype.Service

interface ResourceDefinitionEnhancerService {

    @Throws(BluePrintException::class)
    fun enhance(bluePrintRuntimeService: BluePrintRuntimeService<*>)
}

@Service
class ResourceDefinitionEnhancerServiceImpl(private val resourceDefinitionRepoService: ResourceDefinitionRepoService) :
        ResourceDefinitionEnhancerService {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(ResourceDefinitionEnhancerService::class.toString())

    companion object {
        const val ARTIFACT_TYPE_MAPPING_SOURCE: String = "artifact-mapping-resource"
        const val PROPERTY_DEPENDENCY_NODE_TEMPLATES = "dependency-node-templates"
    }

    // Enhance the Resource Definition
    // 1. Get the Resource Mapping files from all NodeTemplates.
    // 2. Get all the Unique Resource assignments from all mapping files
    // 3. Collect the Resource Definition for Resource Assignment names from database.
    // 4. Create the Resource Definition under blueprint base path.
    override fun enhance(bluePrintRuntimeService: BluePrintRuntimeService<*>) {

        val blueprintContext = bluePrintRuntimeService.bluePrintContext()

        val mappingFiles = getAllResourceMappingFiles(blueprintContext)
        log.info("resources assignment files ($mappingFiles)")
        if (mappingFiles != null) {
            getResourceDefinition(blueprintContext, mappingFiles)
        }
    }

    // Get all the Mapping files from all node templates.
    private fun getAllResourceMappingFiles(blueprintContext: BluePrintContext): List<String>? {

        return blueprintContext.nodeTemplates?.mapNotNull { nodeTemplateMap ->

            // Return only Mapping Artifact File Names
            nodeTemplateMap.value.artifacts?.filter { artifactDefinitionMap ->
                artifactDefinitionMap.value.type == ARTIFACT_TYPE_MAPPING_SOURCE
            }?.mapNotNull { artifactDefinitionMap ->
                artifactDefinitionMap.value.file
            }

        }?.flatten()?.distinct()
    }

    // Convert file content to ResourceAssignments asynchronously
    private fun getResourceDefinition(blueprintContext: BluePrintContext, files: List<String>) {
        runBlocking {
            val blueprintBasePath = blueprintContext.rootPath
            val deferredResourceAssignments = mutableListOf<Deferred<List<ResourceAssignment>>>()
            for (file in files) {
                log.info("processing file ($file)")
                deferredResourceAssignments += async {
                    ResourceDictionaryUtils.getResourceAssignmentFromFile("$blueprintBasePath/$file")
                }
            }

            val resourceAssignments = mutableListOf<ResourceAssignment>()
            for (deferredResourceAssignment in deferredResourceAssignments) {
                resourceAssignments.addAll(deferredResourceAssignment.await())
            }

            val distinctResourceAssignments = resourceAssignments.distinctBy { it.name }
            generateResourceDictionaryFile(blueprintBasePath, distinctResourceAssignments)
            //log.info("distinct Resource assignment ($distinctResourceAssignments)")
        }
    }

    // Read the Resource Definitions from the Database and write to type file.
    private fun generateResourceDictionaryFile(blueprintBasePath: String, resourceAssignments: List<ResourceAssignment>) {
        val resourceKeys = resourceAssignments.mapNotNull { it.dictionaryName }.distinct().sorted()
        log.info("distinct resource keys ($resourceKeys)")

        //TODO("Optimise DB single Query to multiple Query")
        // Collect the Resource Definition from database and convert to map to save in file
        val resourceDefinitionMap = resourceKeys.map { resourceKey ->
            getResourceDefinition(resourceKey)
        }.map { it.name to it }.toMap()

        // Recreate the Resource Definition File
        ResourceDictionaryUtils.writeResourceDefinitionTypes(blueprintBasePath, resourceDefinitionMap)
        log.info("resource definition file created successfully")
    }

    // Get the Resource Definition from Database
    private fun getResourceDefinition(name: String): ResourceDefinition {
        return resourceDefinitionRepoService.getResourceDefinition(name)
    }
}
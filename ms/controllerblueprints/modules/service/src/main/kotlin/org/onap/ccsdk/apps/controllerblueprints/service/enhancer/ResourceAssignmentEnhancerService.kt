/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints.service.enhancer

import com.att.eelf.configuration.EELFLogger
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.apps.controllerblueprints.core.format
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDictionaryConstants
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.factory.ResourceSourceMappingFactory
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.service.ResourceAssignmentValidationDefaultService
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.service.ResourceDefinitionRepoService
import org.springframework.stereotype.Service

/**
 * ResourceAssignmentEnhancerService.
 *
 * @author Brinda Santh
 */
interface ResourceAssignmentEnhancerService {

    @Throws(BluePrintException::class)
    fun enhanceBluePrint(bluePrintEnhancerService: BluePrintEnhancerService,
                         resourceAssignments: List<ResourceAssignment>)

    @Throws(BluePrintException::class)
    fun enhanceBluePrint(resourceAssignments: List<ResourceAssignment>): ServiceTemplate
}

/**
 * ResourceAssignmentEnhancerDefaultService.
 *
 * @author Brinda Santh
 */
@Service
open class ResourceAssignmentEnhancerDefaultService(private val resourceDefinitionRepoService: ResourceDefinitionRepoService)
    : ResourceAssignmentEnhancerService {
    private val log: EELFLogger = EELFManager.getInstance().getLogger(ResourceAssignmentValidationDefaultService::class.java)

    /**
     * Get the defined source instance from the ResourceAssignment,
     * then get the NodeType of the Sources assigned
     */
    override fun enhanceBluePrint(bluePrintEnhancerService: BluePrintEnhancerService,
                                  resourceAssignments: List<ResourceAssignment>) {

        val uniqueSourceNodeTypeNames = hashSetOf<String>()

        // Iterate the Resource Assignment and
        resourceAssignments.map { resourceAssignment ->
            val dictionaryName = resourceAssignment.dictionaryName!!
            val dictionarySource = resourceAssignment.dictionarySource!!
            log.debug("Enriching Assignment name({}), dictionary name({}), source({})", resourceAssignment.name,
                    dictionaryName, dictionarySource)
            val sourceNodeTypeName = ResourceSourceMappingFactory.getRegisterSourceMapping(dictionarySource)

            // Add Unique Node Types
            uniqueSourceNodeTypeNames.add(sourceNodeTypeName)

            // TODO("Candidate for Optimisation")
            if (checkResourceDefinitionNeeded(resourceAssignment)) {

                bluePrintEnhancerService.enrichPropertyDefinition(resourceAssignment.name, resourceAssignment.property!!);

                // Get the Resource Definition from Repo
                val resourceDefinition: ResourceDefinition = getResourceDefinition(dictionaryName)

                val sourceNodeTemplate = resourceDefinition.sources.get(dictionarySource)
                        ?: throw BluePrintException(format("failed to get assigned dictionarySource({}) from resourceDefinition({})", dictionarySource, dictionaryName))

                // Enrich as NodeTemplate
                bluePrintEnhancerService.enrichNodeTemplate(dictionarySource, sourceNodeTemplate)
            }
        }
        // Enrich the ResourceSource NodeTypes
        uniqueSourceNodeTypeNames.map { nodeTypeName ->
            resourceDefinitionRepoService.getNodeType(nodeTypeName).subscribe { nodeType ->
                bluePrintEnhancerService.enrichNodeType(nodeTypeName, nodeType)
            }
        }

    }

    override fun enhanceBluePrint(resourceAssignments: List<ResourceAssignment>): ServiceTemplate {
        val bluePrintEnhancerService = BluePrintEnhancerDefaultService(resourceDefinitionRepoService)
        bluePrintEnhancerService.serviceTemplate = ServiceTemplate()
        bluePrintEnhancerService.initialCleanUp()
        enhanceBluePrint(bluePrintEnhancerService, resourceAssignments)
        return bluePrintEnhancerService.serviceTemplate
    }

    private fun checkResourceDefinitionNeeded(resourceAssignment: ResourceAssignment): Boolean {
        return !((resourceAssignment.dictionarySource.equals(ResourceDictionaryConstants.SOURCE_INPUT)
                || resourceAssignment.dictionarySource.equals(ResourceDictionaryConstants.SOURCE_DEFAULT))
                && BluePrintTypes.validPrimitiveOrCollectionPrimitive(resourceAssignment.property!!))
    }

    private fun getResourceDefinition(name: String): ResourceDefinition {
        return resourceDefinitionRepoService.getResourceDefinition(name).block()
                ?: throw BluePrintException(format("failed to get dictionary definition({})", name))
    }
}
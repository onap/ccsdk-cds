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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.enhancer

import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.service.ResourceDefinitionRepoService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDictionaryConstants
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.factory.ResourceSourceMappingFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

/**
 * ResourceAssignmentEnhancerService.
 *
 * @author Brinda Santh
 */
interface ResourceAssignmentEnhancerService {

    @Throws(BluePrintException::class)
    fun enhanceBluePrint(
        bluePrintTypeEnhancerService: BluePrintTypeEnhancerService,
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        resourceAssignments: List<ResourceAssignment>
    )
}

/**
 * ResourceAssignmentEnhancerDefaultService.
 *
 * @author Brinda Santh
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ResourceAssignmentEnhancerServiceImpl(private val resourceDefinitionRepoService: ResourceDefinitionRepoService) :
    ResourceAssignmentEnhancerService {

    private val log = logger(ResourceAssignmentEnhancerServiceImpl::class)

    /**
     * Get the defined source instance from the ResourceAssignment,
     * then get the NodeType of the Sources assigned
     */
    override fun enhanceBluePrint(
        bluePrintTypeEnhancerService: BluePrintTypeEnhancerService,
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        resourceAssignments: List<ResourceAssignment>
    ) {

        val uniqueSourceNodeTypeNames = hashSetOf<String>()

        // Iterate the Resource Assignment and
        resourceAssignments.map { resourceAssignment ->
            val dictionaryName = resourceAssignment.dictionaryName!!
            val dictionarySource = resourceAssignment.dictionarySource!!
            log.debug("Enriching assignment name(${resourceAssignment.name}), dictionary name($dictionaryName), source($dictionarySource)")
            val sourceNodeTypeName = ResourceSourceMappingFactory.getRegisterSourceMapping(dictionarySource)

            // Add Unique Node Types
            uniqueSourceNodeTypeNames.add(sourceNodeTypeName)

            // TODO("Candidate for Optimisation")
            if (checkResourceDefinitionNeeded(resourceAssignment)) {

                bluePrintTypeEnhancerService.enhancePropertyDefinition(
                    bluePrintRuntimeService, resourceAssignment.name,
                    resourceAssignment.property!!
                )

                // Get the Resource Definition from Repo
                val resourceDefinition: ResourceDefinition = getResourceDefinition(dictionaryName)

                val sourceNodeTemplate = resourceDefinition.sources[dictionarySource]
                    ?: throw BluePrintException("failed to get assigned dictionarySource($dictionarySource) from resourceDefinition($dictionaryName)")

                // Enrich as NodeTemplate
                bluePrintTypeEnhancerService.enhanceNodeTemplate(bluePrintRuntimeService, dictionarySource, sourceNodeTemplate)
            }
        }
        // Enrich the ResourceSource NodeTypes
        uniqueSourceNodeTypeNames.map { nodeTypeName ->
            val nodeType = resourceDefinitionRepoService.getNodeType(nodeTypeName)
            bluePrintTypeEnhancerService.enhanceNodeType(bluePrintRuntimeService, nodeTypeName, nodeType)
        }
    }

    private fun checkResourceDefinitionNeeded(resourceAssignment: ResourceAssignment): Boolean {
        return !(
            (
                resourceAssignment.dictionarySource.equals(ResourceDictionaryConstants.SOURCE_INPUT) ||
                    resourceAssignment.dictionarySource.equals(ResourceDictionaryConstants.SOURCE_DEFAULT)
                ) &&
                BluePrintTypes.validPrimitiveOrCollectionPrimitive(resourceAssignment.property!!)
            )
    }

    private fun getResourceDefinition(name: String): ResourceDefinition {
        return resourceDefinitionRepoService.getResourceDefinition(name)
    }
}

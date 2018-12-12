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
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintError
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.apps.controllerblueprints.core.format
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDictionaryConstants
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.factory.ResourceSourceMappingFactory
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.service.ResourceDefinitionRepoService
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
    fun enhanceBluePrint(bluePrintTypeEnhancerService: BluePrintTypeEnhancerService,
                         bluePrintContext: BluePrintContext, error: BluePrintError,
                         resourceAssignments: List<ResourceAssignment>)
}

/**
 * ResourceAssignmentEnhancerDefaultService.
 *
 * @author Brinda Santh
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ResourceAssignmentEnhancerServiceImpl(private val resourceDefinitionRepoService: ResourceDefinitionRepoService)
    : ResourceAssignmentEnhancerService {
    private val log: EELFLogger = EELFManager.getInstance().getLogger(ResourceAssignmentEnhancerServiceImpl::class.java)

    /**
     * Get the defined source instance from the ResourceAssignment,
     * then get the NodeType of the Sources assigned
     */
    override fun enhanceBluePrint(bluePrintTypeEnhancerService: BluePrintTypeEnhancerService,
                                  bluePrintContext: BluePrintContext, error: BluePrintError,
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

                bluePrintTypeEnhancerService.enhancePropertyDefinition(bluePrintContext, error, resourceAssignment.name,
                        resourceAssignment.property!!);

                // Get the Resource Definition from Repo
                val resourceDefinition: ResourceDefinition = getResourceDefinition(dictionaryName)

                val sourceNodeTemplate = resourceDefinition.sources.get(dictionarySource)
                        ?: throw BluePrintException(format("failed to get assigned dictionarySource({}) from resourceDefinition({})", dictionarySource, dictionaryName))

                // Enrich as NodeTemplate
                bluePrintTypeEnhancerService.enhanceNodeTemplate(bluePrintContext, error, dictionarySource, sourceNodeTemplate)
            }
        }
        // Enrich the ResourceSource NodeTypes
        uniqueSourceNodeTypeNames.map { nodeTypeName ->
            val nodeType = resourceDefinitionRepoService.getNodeType(nodeTypeName)
            bluePrintTypeEnhancerService.enhanceNodeType(bluePrintContext, error, nodeTypeName, nodeType)
        }

    }

    /*
        override fun enhanceBluePrint(resourceAssignments: List<ResourceAssignment>): ServiceTemplate {
            val bluePrintEnhancerService = BluePrintEnhancerServiceImpl(resourceDefinitionRepoService)
            bluePrintEnhancerService.serviceTemplate = ServiceTemplate()
            bluePrintEnhancerService.initialCleanUp()
            enhanceBluePrint(bluePrintEnhancerService, resourceAssignments)
            return bluePrintEnhancerService.serviceTemplate
        }
    */
    private fun checkResourceDefinitionNeeded(resourceAssignment: ResourceAssignment): Boolean {
        return !((resourceAssignment.dictionarySource.equals(ResourceDictionaryConstants.SOURCE_INPUT)
                || resourceAssignment.dictionarySource.equals(ResourceDictionaryConstants.SOURCE_DEFAULT))
                && BluePrintTypes.validPrimitiveOrCollectionPrimitive(resourceAssignment.property!!))
    }

    private fun getResourceDefinition(name: String): ResourceDefinition {
        return resourceDefinitionRepoService.getResourceDefinition(name)
    }
}
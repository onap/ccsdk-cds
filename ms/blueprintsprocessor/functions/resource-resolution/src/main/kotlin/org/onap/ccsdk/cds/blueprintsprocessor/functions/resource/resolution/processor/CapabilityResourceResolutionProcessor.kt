/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Modifications Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor

import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.CapabilityResourceSource
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("${PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-capability")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class CapabilityResourceResolutionProcessor(private var componentFunctionScriptingService: ComponentFunctionScriptingService) :
    ResourceAssignmentProcessor() {

    private val log = LoggerFactory.getLogger(CapabilityResourceResolutionProcessor::class.java)

    var componentResourceAssignmentProcessor: ResourceAssignmentProcessor? = null

    override fun getName(): String {
        return "${PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-capability"
    }

    override suspend fun processNB(resourceAssignment: ResourceAssignment) {
        // Execute only if value is not in Input
        if (!setFromInput(resourceAssignment)) {
            val dName = resourceAssignment.dictionaryName!!
            val dSource = resourceAssignment.dictionarySource!!
            val resourceDefinition = resourceDefinition(resourceAssignment.dictionaryName!!)

            /** Check Resource Assignment has the source definitions, If not get from Resource Definition **/
            val resourceSource = resourceAssignment.dictionarySourceDefinition
                ?: resourceDefinition?.sources?.get(dSource)
                ?: throw BlueprintProcessorException("couldn't get resource definition $dName source($dSource)")

            val resourceSourceProps =
                checkNotNull(resourceSource.properties) { "failed to get $resourceSource properties" }

            /**
             * Get the Capability Resource Source Info from Property Definitions.
             */
            val capabilityResourceSourceProperty = JacksonUtils
                .getInstanceFromMap(resourceSourceProps, CapabilityResourceSource::class.java)

            val scriptType = capabilityResourceSourceProperty.scriptType
            val scriptClassReference = capabilityResourceSourceProperty.scriptClassReference
            val instanceDependencies = capabilityResourceSourceProperty.instanceDependencies ?: listOf()

            componentResourceAssignmentProcessor =
                scriptInstance(scriptType, scriptClassReference, instanceDependencies)

            checkNotNull(componentResourceAssignmentProcessor) {
                "failed to get capability resource assignment processor($scriptClassReference)"
            }

            // Assign Current Blueprint runtime and ResourceDictionaries
            componentResourceAssignmentProcessor!!.scriptType = scriptType
            componentResourceAssignmentProcessor!!.raRuntimeService = raRuntimeService
            componentResourceAssignmentProcessor!!.resourceAssignments = resourceAssignments
            componentResourceAssignmentProcessor!!.resourceDictionaries = resourceDictionaries

            // Invoke componentResourceAssignmentProcessor
            componentResourceAssignmentProcessor!!.executeScript(resourceAssignment)
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, resourceAssignment: ResourceAssignment) {
        raRuntimeService.getBlueprintError()
            .addError("Failed in CapabilityResourceResolutionProcessor : ${runtimeException.message}")
        ResourceAssignmentUtils.setFailedResourceDataValue(resourceAssignment, runtimeException.message)
    }

    suspend fun scriptInstance(scriptType: String, scriptClassReference: String, instanceDependencies: List<String>):
        ResourceAssignmentProcessor {

            log.info("creating resource resolution of script type($scriptType), reference name($scriptClassReference)")

            val scriptComponent = componentFunctionScriptingService
                .scriptInstance<ResourceAssignmentProcessor>(
                    raRuntimeService.bluePrintContext(), scriptType,
                    scriptClassReference
                )

            return scriptComponent
        }
}

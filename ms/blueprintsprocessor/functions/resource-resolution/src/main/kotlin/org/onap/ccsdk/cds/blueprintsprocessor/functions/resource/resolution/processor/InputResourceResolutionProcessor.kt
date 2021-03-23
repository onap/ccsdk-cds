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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor

import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.DatabaseResourceSource
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.isNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

/**
 * InputResourceResolutionProcessor
 *
 * @author Kapil Singal
 */
@Service("${PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-input")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class InputResourceResolutionProcessor : ResourceAssignmentProcessor() {
    companion object InputResourceResolutionProcessor {
        private val logger = LoggerFactory.getLogger(InputResourceResolutionProcessor::class.toString())
    }

    override fun getName(): String {
        return "${PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-input"
    }

    override suspend fun processNB(resourceAssignment: ResourceAssignment) {
        try {
            if (isNotEmpty(resourceAssignment.name) && !setFromInput(resourceAssignment)) {
                setFromKeyDependencies(resourceAssignment)
            }
            // Check the value has populated for mandatory case
            ResourceAssignmentUtils.assertTemplateKeyValueNotNull(resourceAssignment)
        } catch (e: BlueprintProcessorException) {
            val errorMsg = "Failed to process input resource resolution in template key ($resourceAssignment) assignments."
            ResourceAssignmentUtils.setFailedResourceDataValue(resourceAssignment, e.message)
            logger.error(errorMsg)
        } catch (e: Exception) {
            ResourceAssignmentUtils.setFailedResourceDataValue(resourceAssignment, e.message)
            throw BlueprintProcessorException("Failed in template key ($resourceAssignment) assignments with : (${e.message})", e)
        }
    }

    // usecase: where input data attribute doesn't match with resourceName, and needs an alternate mapping provided under key-dependencies.
    private fun setFromKeyDependencies(resourceAssignment: ResourceAssignment) {
        val dName = resourceAssignment.dictionaryName!!
        val dSource = resourceAssignment.dictionarySource!!
        val resourceDefinition = resourceDefinition(dName)

        /** Check Resource Assignment has the source definitions, If not get from Resource Definition **/
        val resourceSource = resourceAssignment.dictionarySourceDefinition
            ?: resourceDefinition?.sources?.get(dSource)
            ?: throw BlueprintProcessorException("couldn't get resource definition $dName source($dSource)")
        try {

            val resourceSourceProperties = checkNotNull(resourceSource.properties) {
                "failed to get source properties for $dName "
            }
            val sourceProperties =
                JacksonUtils.getInstanceFromMap(resourceSourceProperties, DatabaseResourceSource::class.java)

            val keyDependency = checkNotNull(sourceProperties.keyDependencies) {
                "failed to get input-key-mappings for $dName under $dSource properties"
            }
            // keyDependency = service-instance.service-instance-id
            setFromInputKeyDependencies(keyDependency, resourceAssignment); // New API which picks attribute from Input
        } catch (e: IllegalStateException) {
            throw BlueprintProcessorException(e)
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, resourceAssignment: ResourceAssignment) {
        addError(runtimeException.message!!)
    }
}

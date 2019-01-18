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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.processor

import com.fasterxml.jackson.databind.node.NullNode
import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * DefaultResourceAssignmentProcessor
 *
 * @author Kapil Singal
 */
@Service("resource-assignment-processor-default")
open class DefaultResourceAssignmentProcessor : ResourceAssignmentProcessor() {

    private val logger = LoggerFactory.getLogger(DefaultResourceAssignmentProcessor::class.java)

    override fun getName(): String {
        return "resource-assignment-processor-default"
    }

    override fun process(resourceAssignment: ResourceAssignment) {
        try {
            // Check if It has Input
            var value: Any? = raRuntimeService.getInputValue(resourceAssignment.name)

            // If value is null get it from default source
            if (value == null || value is NullNode) {
                logger.info("Looking for defaultValue as couldn't find value in input For template key (${resourceAssignment.name})")
                value = resourceAssignment.property?.defaultValue
            }

            logger.info("For template key (${resourceAssignment.name}) setting value as ($value)")
            ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, value)

            // Check the value has populated for mandatory case
            ResourceAssignmentUtils.assertTemplateKeyValueNotNull(resourceAssignment)
        } catch (e: Exception) {
            ResourceAssignmentUtils.setFailedResourceDataValue(resourceAssignment, e.message)
            throw BluePrintProcessorException("Failed in template key ($resourceAssignment) assignments with: ${e.message}", e)
        }

    }

    override fun recover(runtimeException: RuntimeException, resourceAssignment: ResourceAssignment) {
    }
}
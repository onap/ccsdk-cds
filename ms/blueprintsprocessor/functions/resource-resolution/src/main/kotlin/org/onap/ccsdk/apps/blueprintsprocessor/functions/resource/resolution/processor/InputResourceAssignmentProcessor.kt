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

import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.utils.ResourceResolutionUtils
import org.onap.ccsdk.apps.controllerblueprints.core.*
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.node.NullNode

/**
 * InputResourceAssignmentProcessor
 *
 * @author Brinda Santh
 */
@Service("resource-assignment-processor-input")
open class InputResourceAssignmentProcessor : ResourceAssignmentProcessor() {

    override fun getName(): String {
        return "resource-assignment-processor-input"
    }

    override fun process(executionRequest: ResourceAssignment) {
        try {
            if (checkNotEmpty(executionRequest.name)) {
                val value = bluePrintRuntimeService!!.getInputValue(executionRequest.name)
                // if value is null don't call setResourceDataValue to populate the value
                if (value != null && value !is NullNode) {
                    ResourceResolutionUtils.setResourceDataValue(executionRequest, value)
                }
            }
            // Check the value has populated for mandatory case
            ResourceResolutionUtils.assertTemplateKeyValueNotNull(executionRequest)
        } catch (e: Exception) {
            ResourceResolutionUtils.setFailedResourceDataValue(executionRequest, e.message)
            throw BluePrintProcessorException("Failed in template key ($executionRequest) assignments with : (${e.message})", e)
        }
    }

    override fun recover(runtimeException: RuntimeException, executionRequest: ResourceAssignment) {
    }
}
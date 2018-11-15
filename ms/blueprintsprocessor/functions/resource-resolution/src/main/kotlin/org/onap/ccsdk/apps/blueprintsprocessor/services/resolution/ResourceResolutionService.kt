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

package org.onap.ccsdk.apps.blueprintsprocessor.services.resolution

import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ResourceResolutionInput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ResourceResolutionOutput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.apps.blueprintsprocessor.core.factory.ResourceAssignmentProcessorFactory
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.utils.BulkResourceSequencingUtils
import org.springframework.stereotype.Service

/**
 * ResourceResolutionService
 * @author Brinda Santh
 * 8/14/2018
 */

@Service
class ResourceResolutionService(private val resourceAssignmentProcessorFactory: ResourceAssignmentProcessorFactory) {

    fun resolveResource(resourceResolutionInput: ResourceResolutionInput): ResourceResolutionOutput {
        val resourceResolutionOutput = ResourceResolutionOutput()
        resourceResolutionOutput.actionIdentifiers = resourceResolutionInput.actionIdentifiers
        resourceResolutionOutput.commonHeader = resourceResolutionInput.commonHeader
        resourceResolutionOutput.resourceAssignments = resourceResolutionInput.resourceAssignments

        val context = hashMapOf<String, Any>()

        process(resourceResolutionOutput.resourceAssignments, context)

        val status = Status()
        status.code = 200
        status.message = "Success"
        resourceResolutionOutput.status = status

        return resourceResolutionOutput
    }

    fun process(resourceAssignments: MutableList<ResourceAssignment>, context: MutableMap<String, Any>): Unit {

        val bulkSequenced = BulkResourceSequencingUtils.process(resourceAssignments)

        bulkSequenced.map { batchResourceAssignments ->
            batchResourceAssignments.filter { it.name != "*" && it.name != "start" }
                    .map { resourceAssignment ->
                        val dictionarySource = resourceAssignment.dictionarySource
                        val processorInstanceName = "resource-assignment-processor-".plus(dictionarySource)
                        val resourceAssignmentProcessor = resourceAssignmentProcessorFactory.getInstance(processorInstanceName)
                                ?: throw BluePrintProcessorException("failed to get resource processor for instance name($processorInstanceName) " +
                                        "for resource assignment(${resourceAssignment.name})")
                        try {
                            resourceAssignmentProcessor.validate(resourceAssignment, context)
                            resourceAssignmentProcessor.process(resourceAssignment, context)
                        } catch (e: Exception) {
                            resourceAssignmentProcessor.errorHandle(resourceAssignment, context)
                            throw BluePrintProcessorException(e)
                        }

                    }
        }
    }
}

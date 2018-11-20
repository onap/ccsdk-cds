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
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignmentProcessor
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.utils.BulkResourceSequencingUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

/**
 * ResourceResolutionService
 * @author Brinda Santh
 * 8/14/2018
 */

@Service
class ResourceResolutionService {


    @Autowired
    private lateinit var applicationContext: ApplicationContext

    fun resolveResource(resourceResolutionInput: ResourceResolutionInput): ResourceResolutionOutput {
        val resourceResolutionOutput = ResourceResolutionOutput()
        resourceResolutionOutput.actionIdentifiers = resourceResolutionInput.actionIdentifiers
        resourceResolutionOutput.commonHeader = resourceResolutionInput.commonHeader
        resourceResolutionOutput.resourceAssignments = resourceResolutionInput.resourceAssignments

        process(resourceResolutionOutput.resourceAssignments)

        val status = Status()
        status.code = 200
        status.message = "Success"
        resourceResolutionOutput.status = status

        return resourceResolutionOutput
    }

    fun registeredResourceSources(): List<String> {
        return applicationContext.getBeanNamesForType(ResourceAssignmentProcessor::class.java)
                .filter { it.startsWith(ResourceResolutionConstants.PREFIX_RESOURCE_ASSIGNMENT_PROCESSOR) }
                .map { it.substringAfter(ResourceResolutionConstants.PREFIX_RESOURCE_ASSIGNMENT_PROCESSOR) }
    }

    fun process(resourceAssignments: MutableList<ResourceAssignment>) {

        val bulkSequenced = BulkResourceSequencingUtils.process(resourceAssignments)

        bulkSequenced.map { batchResourceAssignments ->
            batchResourceAssignments.filter { it.name != "*" && it.name != "start" }
                    .map { resourceAssignment ->
                        val dictionarySource = resourceAssignment.dictionarySource
                        val processorInstanceName = ResourceResolutionConstants.PREFIX_RESOURCE_ASSIGNMENT_PROCESSOR.plus(dictionarySource)

                        val resourceAssignmentProcessor = applicationContext.getBean(processorInstanceName) as? ResourceAssignmentProcessor
                                ?: throw BluePrintProcessorException("failed to get resource processor for instance name($processorInstanceName) " +
                                        "for resource assignment(${resourceAssignment.name})")
                        try {
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

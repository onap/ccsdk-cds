/*
 *  Copyright © 2018 IBM.
 *  Modifications Copyright © 2017-2018 AT&T Intellectual Property.
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

import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition
import org.slf4j.LoggerFactory

abstract class ResourceAssignmentProcessor : BlueprintFunctionNode<ResourceAssignment, ResourceAssignment> {

    private val log = LoggerFactory.getLogger(ResourceAssignmentProcessor::class.java)

    lateinit var raRuntimeService: ResourceAssignmentRuntimeService
    lateinit var resourceDictionaries: MutableMap<String, ResourceDefinition>

    var scriptPropertyInstances: Map<String, Any> = hashMapOf()

    /**
     * This will be called from the scripts to serve instance from runtime to scripts.
     */
    open fun <T> scriptPropertyInstanceType(name: String): T {
        return scriptPropertyInstances as? T
                ?: throw BluePrintProcessorException("couldn't get script property instance ($name)")
    }

    open fun resourceDefinition(name: String): ResourceDefinition {
        return resourceDictionaries[name]
                ?: throw BluePrintProcessorException("couldn't get resource definition for ($name)")
    }

    override fun prepareRequest(resourceAssignment: ResourceAssignment): ResourceAssignment {
        log.info("prepareRequest for ${resourceAssignment.name}, dictionary(${resourceAssignment.dictionaryName})," +
                "source(${resourceAssignment.dictionarySource})")
        return resourceAssignment
    }

    override fun prepareResponse(): ResourceAssignment {
        log.info("Preparing Response...")
        return ResourceAssignment()
    }

    override fun apply(executionServiceInput: ResourceAssignment): ResourceAssignment {
        prepareRequest(executionServiceInput)
        process(executionServiceInput)
        return prepareResponse()
    }

}
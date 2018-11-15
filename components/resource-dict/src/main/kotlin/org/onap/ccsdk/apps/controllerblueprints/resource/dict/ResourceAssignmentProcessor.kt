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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict

import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.slf4j.LoggerFactory

abstract class ResourceAssignmentProcessor : BlueprintFunctionNode<ResourceAssignment, ResourceAssignment> {

    private val log = LoggerFactory.getLogger(ResourceAssignmentProcessor::class.java)

    private var bluePrintRuntimeService: BluePrintRuntimeService<*>? = null

    open fun setBlueprintRuntimeService(bluePrintRuntimeService: BluePrintRuntimeService<*>) {
        this.bluePrintRuntimeService = bluePrintRuntimeService
    }

    open fun getBlueprintRuntimeService(): BluePrintRuntimeService<*> {
        return this.bluePrintRuntimeService!!
    }

    override fun prepareRequest(resourceAssignment: ResourceAssignment): ResourceAssignment {
        log.info("prepareRequest...")
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

    override abstract fun process(executionRequest: ResourceAssignment)

    override abstract fun recover(runtimeException: RuntimeException, executionRequest: ResourceAssignment)
}
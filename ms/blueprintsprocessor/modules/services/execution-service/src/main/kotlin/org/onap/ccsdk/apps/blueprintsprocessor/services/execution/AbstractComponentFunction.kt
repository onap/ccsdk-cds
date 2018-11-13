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

package org.onap.ccsdk.apps.blueprintsprocessor.services.execution


import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.slf4j.LoggerFactory

/**
 * AbstractComponentFunction
 * @author Brinda Santh
 */
abstract class AbstractComponentFunction : BlueprintFunctionNode<ExecutionServiceInput, ExecutionServiceOutput> {
    private val log = LoggerFactory.getLogger(AbstractComponentFunction::class.java)

    override fun prepareRequest(executionRequest: ExecutionServiceInput): ExecutionServiceInput {
        log.info("prepareRequest...")
        return executionRequest
    }

    override fun process(executionRequest: ExecutionServiceInput) {
        log.info("Processing...")
    }

    override fun recover(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Recovering...")
    }

    override fun prepareResponse(): ExecutionServiceOutput {
        log.info("Preparing Response...")
        return ExecutionServiceOutput()
    }

    override fun apply(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        prepareRequest(executionServiceInput)
        process(executionServiceInput)
        return prepareResponse()
    }
}
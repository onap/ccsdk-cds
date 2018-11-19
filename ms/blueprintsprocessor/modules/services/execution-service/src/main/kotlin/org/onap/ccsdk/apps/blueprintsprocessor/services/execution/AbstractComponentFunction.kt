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
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.getAsString
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.slf4j.LoggerFactory

/**
 * AbstractComponentFunction
 * @author Brinda Santh
 */
abstract class AbstractComponentFunction : BlueprintFunctionNode<ExecutionServiceInput, ExecutionServiceOutput> {
    private val log = LoggerFactory.getLogger(AbstractComponentFunction::class.java)

    var executionServiceInput: ExecutionServiceInput? = null
    val executionServiceOutput = ExecutionServiceOutput()
    var bluePrintRuntimeService: BluePrintRuntimeService<*>? = null
    var processId: String = ""
    var workflowName: String = ""
    var stepName: String = ""
    var interfaceName: String = ""
    var operationName: String = ""
    var nodeTemplateName: String = ""


    override fun prepareRequest(executionServiceInput: ExecutionServiceInput): ExecutionServiceInput {

        this.executionServiceInput = this.executionServiceInput

        processId = executionServiceInput.commonHeader.requestId
        workflowName = executionServiceInput.actionIdentifiers.actionName

        val metadata = executionServiceInput.metadata
        stepName = metadata.getAsString(BluePrintConstants.PROPERTY_CURRENT_STEP)
        nodeTemplateName = metadata.getAsString(BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE)
        interfaceName = metadata.getAsString(BluePrintConstants.PROPERTY_CURRENT_INTERFACE)
        operationName = metadata.getAsString(BluePrintConstants.PROPERTY_CURRENT_OPERATION)

        checkNotNull(bluePrintRuntimeService) { "failed to prepare blueprint runtime" }

        log.info("prepareRequest...")
        return executionServiceInput
    }

    override fun prepareResponse(): ExecutionServiceOutput {
        log.info("Preparing Response...")
        return this.executionServiceOutput
    }

    override fun apply(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        prepareRequest(executionServiceInput)
        process(executionServiceInput)
        return prepareResponse()
    }
}
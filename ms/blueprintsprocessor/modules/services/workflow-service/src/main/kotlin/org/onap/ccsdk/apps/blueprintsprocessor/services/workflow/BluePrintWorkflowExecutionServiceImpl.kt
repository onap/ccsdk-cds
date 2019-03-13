/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.apps.blueprintsprocessor.services.workflow

import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintWorkflowExecutionService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service("bluePrintWorkflowExecutionService")
open class BluePrintWorkflowExecutionServiceImpl(
        private val componentWorkflowExecutionService: ComponentWorkflowExecutionService,
        private val dgWorkflowExecutionService: DGWorkflowExecutionService
) : BluePrintWorkflowExecutionService<ExecutionServiceInput, ExecutionServiceOutput> {

    private val log = LoggerFactory.getLogger(BluePrintWorkflowExecutionServiceImpl::class.java)!!

    override suspend fun executeBluePrintWorkflow(bluePrintRuntimeService: BluePrintRuntimeService<*>,
                                                  executionServiceInput: ExecutionServiceInput,
                                                  properties: MutableMap<String, Any>): ExecutionServiceOutput {

        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val workflowName = executionServiceInput.actionIdentifiers.actionName

        // Get the DG Node Template
        val nodeTemplateName = bluePrintContext.workflowFirstStepNodeTemplate(workflowName)

        val derivedFrom = bluePrintContext.nodeTemplateNodeType(nodeTemplateName).derivedFrom

        log.info("Executing workflow($workflowName) NodeTemplate($nodeTemplateName), derived from($derivedFrom)")

        val executionServiceOutput: ExecutionServiceOutput = when {
            derivedFrom.startsWith(BluePrintConstants.MODEL_TYPE_NODE_COMPONENT, true) -> {
                componentWorkflowExecutionService
                        .executeBluePrintWorkflow(bluePrintRuntimeService, executionServiceInput, properties)
            }
            derivedFrom.startsWith(BluePrintConstants.MODEL_TYPE_NODE_DG, true) -> {
                dgWorkflowExecutionService
                        .executeBluePrintWorkflow(bluePrintRuntimeService, executionServiceInput, properties)
            }
            else -> {
                throw BluePrintProcessorException("couldn't execute workflow($workflowName) step mapped " +
                        "to node template($nodeTemplateName) derived from($derivedFrom)")
            }
        }

        executionServiceOutput.commonHeader = executionServiceInput.commonHeader
        executionServiceOutput.actionIdentifiers = executionServiceInput.actionIdentifiers
        // TODO("Populate Response Payload and status")
        return executionServiceOutput
    }

}
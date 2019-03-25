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
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintWorkflowExecutionService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.springframework.stereotype.Service

@Service("componentWorkflowExecutionService")
open class ComponentWorkflowExecutionService(private val nodeTemplateExecutionService: NodeTemplateExecutionService)
    : BluePrintWorkflowExecutionService<ExecutionServiceInput, ExecutionServiceOutput> {

    override suspend fun executeBluePrintWorkflow(bluePrintRuntimeService: BluePrintRuntimeService<*>,
                                                  executionServiceInput: ExecutionServiceInput,
                                                  properties: MutableMap<String, Any>): ExecutionServiceOutput {

        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val workflowName = executionServiceInput.actionIdentifiers.actionName

        // Get the DG Node Template
        val nodeTemplateName = bluePrintContext.workflowFirstStepNodeTemplate(workflowName)

        return nodeTemplateExecutionService.executeNodeTemplate(bluePrintRuntimeService,
                nodeTemplateName, executionServiceInput)
    }

}
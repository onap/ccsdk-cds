/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.services.workflow

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.services.workflow.utils.SvcGraphUtils
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintWorkflowExecutionService
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service("dgWorkflowExecutionService")
open class DGWorkflowExecutionService(private val blueprintSvcLogicService: BlueprintSvcLogicService) :
    BlueprintWorkflowExecutionService<ExecutionServiceInput, ExecutionServiceOutput> {

    private val log = LoggerFactory.getLogger(DGWorkflowExecutionService::class.java)

    override suspend fun executeBlueprintWorkflow(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        executionServiceInput: ExecutionServiceInput,
        properties: MutableMap<String, Any>
    ): ExecutionServiceOutput {

        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val workflowName = executionServiceInput.actionIdentifiers.actionName

        // Get the DG Node Template
        val nodeTemplateName = bluePrintContext.workflowFirstStepNodeTemplate(workflowName)

        log.info("Executing workflow($workflowName) directed graph NodeTemplate($nodeTemplateName)")

        // Get the DG file info
        val artifactDefinition = bluePrintContext.nodeTemplateArtifactForArtifactType(
            nodeTemplateName,
            WorkflowServiceConstants.ARTIFACT_TYPE_DIRECTED_GRAPH
        )

        // Populate the DG Path
        val dgFilePath = normalizedPathName(bluePrintContext.rootPath, artifactDefinition.file)

        log.info("Executing directed graph ($dgFilePath)")

        // Create DG instance
        val graph = SvcGraphUtils.getSvcGraphFromFile(dgFilePath)

        // Execute the DG
        return blueprintSvcLogicService.execute(
            graph,
            bluePrintRuntimeService,
            executionServiceInput
        ) as ExecutionServiceOutput
    }
}

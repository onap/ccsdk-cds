/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.blueprintsprocessor.services.workflow

import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.apps.blueprintsprocessor.services.workflow.utils.SvcGraphUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File


interface BlueprintDGExecutionService {

    fun executeDirectedGraph(bluePrintRuntimeService: BluePrintRuntimeService<*>,
                             executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput

}

@Service
class DefaultBlueprintDGExecutionService(val blueprintSvcLogicService: BlueprintSvcLogicService) : BlueprintDGExecutionService {

    private val log = LoggerFactory.getLogger(DefaultBlueprintDGExecutionService::class.java)

    override fun executeDirectedGraph(bluePrintRuntimeService: BluePrintRuntimeService<*>,
                                      executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {

        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val workflowName = executionServiceInput.actionIdentifiers.actionName

        // Get the DG Node Template
        val nodeTemplateName = bluePrintContext.workflowFirstStepNodeTemplate(workflowName)

        log.info("Executing workflow($workflowName) directed graph NodeTemplate($nodeTemplateName)")

        // Get the DG file info
        val artifactDefinition = bluePrintContext.nodeTemplateArtifactForArtifactType(nodeTemplateName,
                WorkflowServiceConstants.ARTIFACT_TYPE_DIRECTED_GRAPH)

        // Populate the DG Path
        val dgFilePath = bluePrintRuntimeService.getAsString(BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH)
                .plus(File.separator).plus(artifactDefinition.file)

        log.info("Executing directed graph ($dgFilePath)")

        // Create DG instance
        val graph = SvcGraphUtils.getSvcGraphFromFile(dgFilePath)

        // Execute the DG
        return blueprintSvcLogicService.execute(graph, bluePrintRuntimeService, executionServiceInput) as ExecutionServiceOutput

    }

}
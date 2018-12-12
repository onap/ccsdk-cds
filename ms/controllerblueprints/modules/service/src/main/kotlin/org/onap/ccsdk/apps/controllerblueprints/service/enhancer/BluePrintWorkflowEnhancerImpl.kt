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

package org.onap.ccsdk.apps.controllerblueprints.service.enhancer

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintError
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.data.DataType
import org.onap.ccsdk.apps.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintWorkflowEnhancer
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintWorkflowEnhancerImpl(private val bluePrintRepoService: BluePrintRepoService,
                                         private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService,
                                         private val resourceAssignmentEnhancerService: ResourceAssignmentEnhancerService)
    : BluePrintWorkflowEnhancer {

    lateinit var bluePrintContext: BluePrintContext
    lateinit var error: BluePrintError

    private val workflowDataTypes: MutableMap<String, DataType> = hashMapOf()

    override fun enhance(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, workflow: Workflow) {
        this.bluePrintContext = bluePrintContext
        this.error = error

        // Enrich Only for Resource Assignment and Dynamic Input Properties if any
        enhanceStepTargets(workflow)

        // Enrich Workflow Inputs
        enhanceWorkflowInputs(name, workflow)
    }

    open fun enhanceWorkflowInputs(name: String, workflow: Workflow) {
        val dynamicPropertyName = "$name-properties"
        workflow.inputs?.let { inputs ->
            // TODO("Filter Dynamic Properties")
            bluePrintTypeEnhancerService.enhancePropertyDefinitions(bluePrintContext, error, inputs)
        }
    }

    private fun enhanceStepTargets(workflow: Workflow) {

        val workflowNodeTemplates = workflowTargets(workflow)

        workflowNodeTemplates.forEach { nodeTemplate ->
            val artifactFiles = bluePrintContext.nodeTemplateByName(nodeTemplate).artifacts?.filter {
                it.value.type == "artifact-mapping-resource"
            }?.map {
                it.value.file
            }

            artifactFiles?.let { fileName ->
                val absoluteFilePath = "${bluePrintContext.rootPath}/$fileName"
                // Enhance Resource Assignment File
                enhanceResourceAssignmentFile(absoluteFilePath)

            }
        }
    }

    private fun workflowTargets(workflow: Workflow): List<String> {
        return workflow.steps?.map {
            it.value.target
        }?.filterNotNull() ?: arrayListOf()
    }

    open fun enhanceResourceAssignmentFile(filePath: String) {
        val resourceAssignments: MutableList<ResourceAssignment> = JacksonUtils.getListFromFile(filePath, ResourceAssignment::class.java)
                as? MutableList<ResourceAssignment>
                ?: throw BluePrintProcessorException("couldn't get ResourceAssignment definitions for the file($filePath)")
        resourceAssignmentEnhancerService.enhanceBluePrint(bluePrintTypeEnhancerService, bluePrintContext, error, resourceAssignments)
    }
}
/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.validation

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.TopologyTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTopologyTemplateValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("default-topology-template-validator")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BlueprintTopologyTemplateValidatorImpl(private val bluePrintTypeValidatorService: BlueprintTypeValidatorService) :
    BlueprintTopologyTemplateValidator {

    private val log = LoggerFactory.getLogger(BlueprintServiceTemplateValidatorImpl::class.toString())

    lateinit var bluePrintRuntimeService: BlueprintRuntimeService<*>

    override fun validate(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, topologyTemplate: TopologyTemplate) {
        log.trace("Validating Topology Template..")
        this.bluePrintRuntimeService = bluePrintRuntimeService

        // Validate Inputs
        topologyTemplate.inputs?.let { validateInputs(topologyTemplate.inputs!!) }
        // Validate Node Templates
        topologyTemplate.nodeTemplates?.let { validateNodeTemplates(topologyTemplate.nodeTemplates!!) }
        // Validate Workflow
        topologyTemplate.workflows?.let { validateWorkflows(topologyTemplate.workflows!!) }
    }

    @Throws(BlueprintException::class)
    fun validateInputs(inputs: MutableMap<String, PropertyDefinition>) {
        bluePrintTypeValidatorService.validatePropertyDefinitions(bluePrintRuntimeService, inputs)
    }

    @Throws(BlueprintException::class)
    fun validateNodeTemplates(nodeTemplates: MutableMap<String, NodeTemplate>) {

        nodeTemplates.forEach { nodeTemplateName, nodeTemplate ->
            // Validate Single Node Template
            bluePrintTypeValidatorService.validateNodeTemplate(bluePrintRuntimeService, nodeTemplateName, nodeTemplate)
        }
    }

    @Throws(BlueprintException::class)
    open fun validateWorkflows(workflows: MutableMap<String, Workflow>) {

        workflows.forEach { workflowName, workflow ->
            // Validate Single workflow
            bluePrintTypeValidatorService.validateWorkflow(bluePrintRuntimeService, workflowName, workflow)
        }
    }
}

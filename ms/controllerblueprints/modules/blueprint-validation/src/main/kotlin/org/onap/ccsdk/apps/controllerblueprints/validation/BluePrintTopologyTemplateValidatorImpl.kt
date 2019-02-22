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

package org.onap.ccsdk.apps.controllerblueprints.validation

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.apps.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.data.TopologyTemplate
import org.onap.ccsdk.apps.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTopologyTemplateValidator
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("default-topology-template-validator")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintTopologyTemplateValidatorImpl(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService) : BluePrintTopologyTemplateValidator {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintServiceTemplateValidatorImpl::class.toString())

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>

    override fun validate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, topologyTemplate: TopologyTemplate) {
        log.trace("Validating Topology Template..")
        this.bluePrintRuntimeService = bluePrintRuntimeService

        // Validate Inputs
        topologyTemplate.inputs?.let { validateInputs(topologyTemplate.inputs!!) }
        // Validate Node Templates
        topologyTemplate.nodeTemplates?.let { validateNodeTemplates(topologyTemplate.nodeTemplates!!) }
        // Validate Workflow
        topologyTemplate.workflows?.let { validateWorkflows(topologyTemplate.workflows!!) }
    }

    @Throws(BluePrintException::class)
    fun validateInputs(inputs: MutableMap<String, PropertyDefinition>) {
        bluePrintTypeValidatorService.validatePropertyDefinitions(bluePrintRuntimeService, inputs)
    }


    @Throws(BluePrintException::class)
    fun validateNodeTemplates(nodeTemplates: MutableMap<String, NodeTemplate>) {

        nodeTemplates.forEach { nodeTemplateName, nodeTemplate ->
            // Validate Single Node Template
            bluePrintTypeValidatorService.validateNodeTemplate(bluePrintRuntimeService, nodeTemplateName, nodeTemplate)
        }
    }

    @Throws(BluePrintException::class)
    open fun validateWorkflows(workflows: MutableMap<String, Workflow>) {

        workflows.forEach { workflowName, workflow ->
            // Validate Single workflow
            bluePrintTypeValidatorService.validateWorkflow(bluePrintRuntimeService, workflowName, workflow)
        }
    }

}
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
import org.onap.ccsdk.apps.controllerblueprints.core.data.TopologyTemplate
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTopologyTemplateEnhancer
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintTopologyTemplateEnhancerImpl(private val bluePrintRepoService: BluePrintRepoService,
                                                 private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService) : BluePrintTopologyTemplateEnhancer {

    lateinit var bluePrintContext: BluePrintContext
    lateinit var error: BluePrintError

    override fun enhance(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, type: TopologyTemplate) {
        this.bluePrintContext = bluePrintContext
        this.error = error

        enhanceTopologyTemplateInputs(type)
        enhanceTopologyTemplateNodeTemplates(type)
        enhanceTopologyTemplateWorkflowss(type)
    }

    open fun enhanceTopologyTemplateInputs(topologyTemplate: TopologyTemplate) {
        topologyTemplate.inputs?.let { inputs ->
            bluePrintTypeEnhancerService.enhancePropertyDefinitions(bluePrintContext, error, inputs)
        }
    }

    open fun enhanceTopologyTemplateNodeTemplates(topologyTemplate: TopologyTemplate) {
        topologyTemplate.nodeTemplates?.forEach { nodeTemplateName, nodeTemplate ->
            bluePrintTypeEnhancerService.enhanceNodeTemplate(bluePrintContext, error, nodeTemplateName, nodeTemplate)
        }
    }

    open fun enhanceTopologyTemplateWorkflowss(topologyTemplate: TopologyTemplate) {
        topologyTemplate.workflows?.forEach { workflowName, workflow ->
            bluePrintTypeEnhancerService.enhanceWorkflow(bluePrintContext, error, workflowName, workflow)
        }
    }

}
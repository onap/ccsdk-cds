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

package org.onap.ccsdk.apps.controllerblueprints.core.interfaces

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintError
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext

interface BluePrintEnhancer<T> {
    fun enhance(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, type: T)
}

interface BluePrintServiceTemplateEnhancer : BluePrintEnhancer<ServiceTemplate>

interface BluePrintTopologyTemplateEnhancer : BluePrintEnhancer<TopologyTemplate>

interface BluePrintWorkflowEnhancer : BluePrintEnhancer<Workflow>

interface BluePrintNodeTemplateEnhancer : BluePrintEnhancer<NodeTemplate>

interface BluePrintNodeTypeEnhancer : BluePrintEnhancer<NodeType>

interface BluePrintPolicyTypeEnhancer : BluePrintEnhancer<PolicyType>

interface BluePrintPropertyDefinitionEnhancer : BluePrintEnhancer<PropertyDefinition>

interface BluePrintAttributeDefinitionEnhancer : BluePrintEnhancer<AttributeDefinition>


interface BluePrintEnhancerService {

    @Throws(BluePrintException::class)
    fun enhance(basePath: String, enrichedBasePath: String): BluePrintContext

    @Throws(BluePrintException::class)
    fun enhance(basePath: String): BluePrintContext

    @Throws(BluePrintException::class)
    fun enhance(serviceTemplate: ServiceTemplate): ServiceTemplate
}

interface BluePrintTypeEnhancerService {

    fun getServiceTemplateEnhancers(): List<BluePrintServiceTemplateEnhancer>

    fun getTopologyTemplateEnhancers(): List<BluePrintTopologyTemplateEnhancer>

    fun getWorkflowEnhancers(): List<BluePrintWorkflowEnhancer>

    fun getNodeTemplateEnhancers(): List<BluePrintNodeTemplateEnhancer>

    fun getNodeTypeEnhancers(): List<BluePrintNodeTypeEnhancer>

    fun getPolicyTypeEnhancers(): List<BluePrintPolicyTypeEnhancer>

    fun getPropertyDefinitionEnhancers(): List<BluePrintPropertyDefinitionEnhancer>

    fun getAttributeDefinitionEnhancers(): List<BluePrintAttributeDefinitionEnhancer>

    fun enhanceServiceTemplate(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, serviceTemplate: ServiceTemplate) {
        val enhancers = getServiceTemplateEnhancers()
        doEnhancement(bluePrintContext, error, name, serviceTemplate, enhancers)
    }

    fun enhanceTopologyTemplate(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, topologyTemplate: TopologyTemplate) {
        val enhancers = getTopologyTemplateEnhancers()
        doEnhancement(bluePrintContext, error, name, topologyTemplate, enhancers)
    }

    fun enhanceWorkflow(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, workflow: Workflow) {
        val enhancers = getWorkflowEnhancers()
        doEnhancement(bluePrintContext, error, name, workflow, enhancers)
    }

    fun enhanceNodeTemplate(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, nodeTemplate: NodeTemplate) {
        val enhancers = getNodeTemplateEnhancers()
        doEnhancement(bluePrintContext, error, name, nodeTemplate, enhancers)
    }

    fun enhanceNodeType(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, nodeType: NodeType) {
        val enhancers = getNodeTypeEnhancers()
        doEnhancement(bluePrintContext, error, name, nodeType, enhancers)
    }

    fun enhancePolicyType(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, policyType: PolicyType) {
        val enhancers = getPolicyTypeEnhancers()
        doEnhancement(bluePrintContext, error, name, policyType, enhancers)
    }

    fun enhancePropertyDefinition(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, propertyDefinition: PropertyDefinition) {
        val enhancers = getPropertyDefinitionEnhancers()
        doEnhancement(bluePrintContext, error, name, propertyDefinition, enhancers)
    }

    fun enhanceAttributeDefinition(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, attributeDefinition: AttributeDefinition) {
        val enhancers = getAttributeDefinitionEnhancers()
        doEnhancement(bluePrintContext, error, name, attributeDefinition, enhancers)
    }

    private fun <T> doEnhancement(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, definition: Any, enhancers: List<BluePrintEnhancer<T>>) {
        if (enhancers.isNotEmpty()) {
            enhancers.forEach {
                it.enhance(bluePrintContext, error, name, definition as T)
            }
        }
    }
}
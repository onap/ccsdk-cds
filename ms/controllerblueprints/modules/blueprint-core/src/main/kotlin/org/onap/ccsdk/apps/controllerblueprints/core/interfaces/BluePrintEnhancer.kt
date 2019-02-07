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

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService

interface BluePrintEnhancer<T> {
    fun enhance(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, type: T)
}

interface BluePrintServiceTemplateEnhancer : BluePrintEnhancer<ServiceTemplate>

interface BluePrintTopologyTemplateEnhancer : BluePrintEnhancer<TopologyTemplate>

interface BluePrintWorkflowEnhancer : BluePrintEnhancer<Workflow>

interface BluePrintNodeTemplateEnhancer : BluePrintEnhancer<NodeTemplate>

interface BluePrintNodeTypeEnhancer : BluePrintEnhancer<NodeType>

interface BluePrintArtifactDefinitionEnhancer : BluePrintEnhancer<ArtifactDefinition>

interface BluePrintPolicyTypeEnhancer : BluePrintEnhancer<PolicyType>

interface BluePrintPropertyDefinitionEnhancer : BluePrintEnhancer<PropertyDefinition>

interface BluePrintAttributeDefinitionEnhancer : BluePrintEnhancer<AttributeDefinition>


interface BluePrintEnhancerService {

    @Throws(BluePrintException::class)
    fun enhance(basePath: String, enrichedBasePath: String): BluePrintContext

    @Throws(BluePrintException::class)
    fun enhance(basePath: String): BluePrintContext
}

interface BluePrintTypeEnhancerService {

    fun getServiceTemplateEnhancers(): List<BluePrintServiceTemplateEnhancer>

    fun getTopologyTemplateEnhancers(): List<BluePrintTopologyTemplateEnhancer>

    fun getWorkflowEnhancers(): List<BluePrintWorkflowEnhancer>

    fun getNodeTemplateEnhancers(): List<BluePrintNodeTemplateEnhancer>

    fun getNodeTypeEnhancers(): List<BluePrintNodeTypeEnhancer>

    fun getArtifactDefinitionEnhancers(): List<BluePrintArtifactDefinitionEnhancer>

    fun getPolicyTypeEnhancers(): List<BluePrintPolicyTypeEnhancer>

    fun getPropertyDefinitionEnhancers(): List<BluePrintPropertyDefinitionEnhancer>

    fun getAttributeDefinitionEnhancers(): List<BluePrintAttributeDefinitionEnhancer>

    fun enhanceServiceTemplate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, serviceTemplate: ServiceTemplate) {
        val enhancers = getServiceTemplateEnhancers()
        doEnhancement(bluePrintRuntimeService, name, serviceTemplate, enhancers)
    }

    fun enhanceTopologyTemplate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, topologyTemplate: TopologyTemplate) {
        val enhancers = getTopologyTemplateEnhancers()
        doEnhancement(bluePrintRuntimeService, name, topologyTemplate, enhancers)
    }

    fun enhanceWorkflow(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, workflow: Workflow) {
        val enhancers = getWorkflowEnhancers()
        doEnhancement(bluePrintRuntimeService, name, workflow, enhancers)
    }

    fun enhanceNodeTemplate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, nodeTemplate: NodeTemplate) {
        val enhancers = getNodeTemplateEnhancers()
        doEnhancement(bluePrintRuntimeService, name, nodeTemplate, enhancers)
    }

    fun enhanceNodeType(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, nodeType: NodeType) {
        val enhancers = getNodeTypeEnhancers()
        doEnhancement(bluePrintRuntimeService, name, nodeType, enhancers)
    }

    fun enhanceArtifactDefinition(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, artifactDefinition: ArtifactDefinition) {
        val enhancers = getArtifactDefinitionEnhancers()
        doEnhancement(bluePrintRuntimeService, name, artifactDefinition, enhancers)
    }

    fun enhancePolicyType(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, policyType: PolicyType) {
        val enhancers = getPolicyTypeEnhancers()
        doEnhancement(bluePrintRuntimeService, name, policyType, enhancers)
    }

    fun enhancePropertyDefinitions(bluePrintRuntimeService: BluePrintRuntimeService<*>, properties: MutableMap<String, PropertyDefinition>) {
        properties.forEach { propertyName, propertyDefinition ->
            enhancePropertyDefinition(bluePrintRuntimeService, propertyName, propertyDefinition)
        }
    }

    fun enhancePropertyDefinition(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, propertyDefinition: PropertyDefinition) {
        val enhancers = getPropertyDefinitionEnhancers()
        doEnhancement(bluePrintRuntimeService, name, propertyDefinition, enhancers)
    }

    fun enhanceAttributeDefinitions(bluePrintRuntimeService: BluePrintRuntimeService<*>, attributes: MutableMap<String, AttributeDefinition>) {
        attributes.forEach { attributeName, attributeDefinition ->
            enhanceAttributeDefinition(bluePrintRuntimeService, attributeName, attributeDefinition)
        }
    }

    fun enhanceAttributeDefinition(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, attributeDefinition: AttributeDefinition) {
        val enhancers = getAttributeDefinitionEnhancers()
        doEnhancement(bluePrintRuntimeService, name, attributeDefinition, enhancers)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> doEnhancement(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, definition: Any, enhancers: List<BluePrintEnhancer<T>>) {
        if (enhancers.isNotEmpty()) {
            enhancers.forEach {
                it.enhance(bluePrintRuntimeService, name, definition as T)
            }
        }
    }
}
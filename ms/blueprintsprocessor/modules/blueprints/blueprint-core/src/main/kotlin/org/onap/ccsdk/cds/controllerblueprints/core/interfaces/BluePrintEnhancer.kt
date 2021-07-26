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

package org.onap.ccsdk.cds.controllerblueprints.core.interfaces

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.AttributeDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.PolicyType
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.TopologyTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService

interface BluePrintEnhancer<T> {

    fun enhance(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, type: T)
}

interface BluePrintServiceTemplateEnhancer : BluePrintEnhancer<ServiceTemplate>

interface BluePrintTopologyTemplateEnhancer : BluePrintEnhancer<TopologyTemplate>

interface BluePrintWorkflowEnhancer : BluePrintEnhancer<Workflow>

interface BluePrintNodeTemplateEnhancer : BluePrintEnhancer<NodeTemplate>

interface BluePrintNodeTypeEnhancer : BluePrintEnhancer<NodeType>

interface BluePrintRelationshipTemplateEnhancer : BluePrintEnhancer<RelationshipTemplate>

interface BluePrintRelationshipTypeEnhancer : BluePrintEnhancer<RelationshipType>

interface BluePrintArtifactDefinitionEnhancer : BluePrintEnhancer<ArtifactDefinition>

interface BluePrintPolicyTypeEnhancer : BluePrintEnhancer<PolicyType>

interface BluePrintPropertyDefinitionEnhancer : BluePrintEnhancer<PropertyDefinition>

interface BluePrintAttributeDefinitionEnhancer : BluePrintEnhancer<AttributeDefinition>

interface BluePrintEnhancerService {

    @Throws(BluePrintException::class)
    suspend fun enhance(basePath: String, enrichedBasePath: String): BluePrintContext

    @Throws(BluePrintException::class)
    suspend fun enhance(basePath: String): BluePrintContext
}

interface BluePrintTypeEnhancerService {

    fun getServiceTemplateEnhancers(): List<BluePrintServiceTemplateEnhancer>

    fun getTopologyTemplateEnhancers(): List<BluePrintTopologyTemplateEnhancer>

    fun getWorkflowEnhancers(): List<BluePrintWorkflowEnhancer>

    fun getNodeTemplateEnhancers(): List<BluePrintNodeTemplateEnhancer>

    fun getNodeTypeEnhancers(): List<BluePrintNodeTypeEnhancer>

    fun getRelationshipTemplateEnhancers(): List<BluePrintRelationshipTemplateEnhancer>

    fun getRelationshipTypeEnhancers(): List<BluePrintRelationshipTypeEnhancer>

    fun getArtifactDefinitionEnhancers(): List<BluePrintArtifactDefinitionEnhancer>

    fun getPolicyTypeEnhancers(): List<BluePrintPolicyTypeEnhancer>

    fun getPropertyDefinitionEnhancers(): List<BluePrintPropertyDefinitionEnhancer>

    fun getAttributeDefinitionEnhancers(): List<BluePrintAttributeDefinitionEnhancer>

    fun enhanceServiceTemplate(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        name: String,
        serviceTemplate: ServiceTemplate
    ) {
        val enhancers = getServiceTemplateEnhancers()
        doEnhancement(bluePrintRuntimeService, name, serviceTemplate, enhancers)
    }

    fun enhanceTopologyTemplate(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        name: String,
        topologyTemplate: TopologyTemplate
    ) {
        val enhancers = getTopologyTemplateEnhancers()
        doEnhancement(bluePrintRuntimeService, name, topologyTemplate, enhancers)
    }

    fun enhanceWorkflow(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, workflow: Workflow) {
        val enhancers = getWorkflowEnhancers()
        doEnhancement(bluePrintRuntimeService, name, workflow, enhancers)
    }

    fun enhanceNodeTemplate(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        name: String,
        nodeTemplate: NodeTemplate
    ) {
        val enhancers = getNodeTemplateEnhancers()
        doEnhancement(bluePrintRuntimeService, name, nodeTemplate, enhancers)
    }

    fun enhanceNodeType(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, nodeType: NodeType) {
        val enhancers = getNodeTypeEnhancers()
        doEnhancement(bluePrintRuntimeService, name, nodeType, enhancers)
    }

    fun enhanceRelationshipTemplate(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        name: String,
        relationshipTemplate: RelationshipTemplate
    ) {
        val enhancers = getRelationshipTemplateEnhancers()
        doEnhancement(bluePrintRuntimeService, name, relationshipTemplate, enhancers)
    }

    fun enhanceRelationshipType(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        name: String,
        relationshipType: RelationshipType
    ) {
        val enhancers = getRelationshipTypeEnhancers()
        doEnhancement(bluePrintRuntimeService, name, relationshipType, enhancers)
    }

    fun enhanceArtifactDefinition(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        name: String,
        artifactDefinition: ArtifactDefinition
    ) {
        val enhancers = getArtifactDefinitionEnhancers()
        doEnhancement(bluePrintRuntimeService, name, artifactDefinition, enhancers)
    }

    fun enhancePolicyType(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, policyType: PolicyType) {
        val enhancers = getPolicyTypeEnhancers()
        doEnhancement(bluePrintRuntimeService, name, policyType, enhancers)
    }

    fun enhancePropertyDefinitions(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        properties: MutableMap<String, PropertyDefinition>
    ) {
        val errorMap = linkedMapOf<String, BluePrintException>()
        properties.forEach { propertyName, propertyDefinition ->
            try {
                enhancePropertyDefinition(bluePrintRuntimeService, propertyName, propertyDefinition)
            } catch (e: BluePrintException) {
                errorMap[propertyName] = e
            }
        }
        if (errorMap.isNotEmpty()) {
            val nestedErrors = errorMap.keys.map { "[ property: ${errorMap[it]?.message} ]" }.joinToString(";")
            throw BluePrintException("Failed to enhance properties $nestedErrors")
        }
    }

    fun enhancePropertyDefinition(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        name: String,
        propertyDefinition: PropertyDefinition
    ) {
        val enhancers = getPropertyDefinitionEnhancers()
        doEnhancement(bluePrintRuntimeService, name, propertyDefinition, enhancers)
    }

    fun enhanceAttributeDefinitions(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        attributes: MutableMap<String, AttributeDefinition>
    ) {
        val errorMap = linkedMapOf<String, BluePrintException>()
        attributes.forEach { attributeName, attributeDefinition ->
            try {
                enhanceAttributeDefinition(bluePrintRuntimeService, attributeName, attributeDefinition)
            } catch (e: BluePrintException) {
                errorMap[attributeName] = e
            }
        }
        if (errorMap.isNotEmpty()) {
            val nestedErrors = errorMap.keys.map { "[ attribute: ${errorMap[it]?.message} ]" }.joinToString(";")
            throw BluePrintException("Failed to enhance attributes $nestedErrors")
        }
    }

    fun enhanceAttributeDefinition(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        name: String,
        attributeDefinition: AttributeDefinition
    ) {
        val enhancers = getAttributeDefinitionEnhancers()
        doEnhancement(bluePrintRuntimeService, name, attributeDefinition, enhancers)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> doEnhancement(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        name: String,
        definition: Any,
        enhancers: List<BluePrintEnhancer<T>>
    ) {
        if (enhancers.isNotEmpty()) {
            val errorMap = linkedMapOf<String, BluePrintException>()
            enhancers.forEach {
                try {
                    it.enhance(bluePrintRuntimeService, name, definition as T)
                } catch (e: BluePrintException) {
                    errorMap[name] = e
                }
            }
            if (errorMap.isNotEmpty()) {
                val nestedErrors = errorMap.keys.map {
                    "${errorMap[it]?.message ?: errorMap[it].toString()}"
                }.joinToString(";")
                throw BluePrintException("$name-->$nestedErrors")
            }
        }
    }
}

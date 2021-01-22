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

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
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
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService

interface BlueprintEnhancer<T> {

    fun enhance(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, type: T)
}

interface BlueprintServiceTemplateEnhancer : BlueprintEnhancer<ServiceTemplate>

interface BlueprintTopologyTemplateEnhancer : BlueprintEnhancer<TopologyTemplate>

interface BlueprintWorkflowEnhancer : BlueprintEnhancer<Workflow>

interface BlueprintNodeTemplateEnhancer : BlueprintEnhancer<NodeTemplate>

interface BlueprintNodeTypeEnhancer : BlueprintEnhancer<NodeType>

interface BlueprintRelationshipTemplateEnhancer : BlueprintEnhancer<RelationshipTemplate>

interface BlueprintRelationshipTypeEnhancer : BlueprintEnhancer<RelationshipType>

interface BlueprintArtifactDefinitionEnhancer : BlueprintEnhancer<ArtifactDefinition>

interface BlueprintPolicyTypeEnhancer : BlueprintEnhancer<PolicyType>

interface BlueprintPropertyDefinitionEnhancer : BlueprintEnhancer<PropertyDefinition>

interface BlueprintAttributeDefinitionEnhancer : BlueprintEnhancer<AttributeDefinition>

interface BlueprintEnhancerService {

    @Throws(BlueprintException::class)
    suspend fun enhance(basePath: String, enrichedBasePath: String): BlueprintContext

    @Throws(BlueprintException::class)
    suspend fun enhance(basePath: String): BlueprintContext
}

interface BlueprintTypeEnhancerService {

    fun getServiceTemplateEnhancers(): List<BlueprintServiceTemplateEnhancer>

    fun getTopologyTemplateEnhancers(): List<BlueprintTopologyTemplateEnhancer>

    fun getWorkflowEnhancers(): List<BlueprintWorkflowEnhancer>

    fun getNodeTemplateEnhancers(): List<BlueprintNodeTemplateEnhancer>

    fun getNodeTypeEnhancers(): List<BlueprintNodeTypeEnhancer>

    fun getRelationshipTemplateEnhancers(): List<BlueprintRelationshipTemplateEnhancer>

    fun getRelationshipTypeEnhancers(): List<BlueprintRelationshipTypeEnhancer>

    fun getArtifactDefinitionEnhancers(): List<BlueprintArtifactDefinitionEnhancer>

    fun getPolicyTypeEnhancers(): List<BlueprintPolicyTypeEnhancer>

    fun getPropertyDefinitionEnhancers(): List<BlueprintPropertyDefinitionEnhancer>

    fun getAttributeDefinitionEnhancers(): List<BlueprintAttributeDefinitionEnhancer>

    fun enhanceServiceTemplate(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        serviceTemplate: ServiceTemplate
    ) {
        val enhancers = getServiceTemplateEnhancers()
        doEnhancement(bluePrintRuntimeService, name, serviceTemplate, enhancers)
    }

    fun enhanceTopologyTemplate(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        topologyTemplate: TopologyTemplate
    ) {
        val enhancers = getTopologyTemplateEnhancers()
        doEnhancement(bluePrintRuntimeService, name, topologyTemplate, enhancers)
    }

    fun enhanceWorkflow(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, workflow: Workflow) {
        val enhancers = getWorkflowEnhancers()
        doEnhancement(bluePrintRuntimeService, name, workflow, enhancers)
    }

    fun enhanceNodeTemplate(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        nodeTemplate: NodeTemplate
    ) {
        val enhancers = getNodeTemplateEnhancers()
        doEnhancement(bluePrintRuntimeService, name, nodeTemplate, enhancers)
    }

    fun enhanceNodeType(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, nodeType: NodeType) {
        val enhancers = getNodeTypeEnhancers()
        doEnhancement(bluePrintRuntimeService, name, nodeType, enhancers)
    }

    fun enhanceRelationshipTemplate(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        relationshipTemplate: RelationshipTemplate
    ) {
        val enhancers = getRelationshipTemplateEnhancers()
        doEnhancement(bluePrintRuntimeService, name, relationshipTemplate, enhancers)
    }

    fun enhanceRelationshipType(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        relationshipType: RelationshipType
    ) {
        val enhancers = getRelationshipTypeEnhancers()
        doEnhancement(bluePrintRuntimeService, name, relationshipType, enhancers)
    }

    fun enhanceArtifactDefinition(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        artifactDefinition: ArtifactDefinition
    ) {
        val enhancers = getArtifactDefinitionEnhancers()
        doEnhancement(bluePrintRuntimeService, name, artifactDefinition, enhancers)
    }

    fun enhancePolicyType(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, policyType: PolicyType) {
        val enhancers = getPolicyTypeEnhancers()
        doEnhancement(bluePrintRuntimeService, name, policyType, enhancers)
    }

    fun enhancePropertyDefinitions(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        properties: MutableMap<String, PropertyDefinition>
    ) {
        val errorMap = linkedMapOf<String, BlueprintException>()
        properties.forEach { propertyName, propertyDefinition ->
            try {
                enhancePropertyDefinition(bluePrintRuntimeService, propertyName, propertyDefinition)
            } catch (e: BlueprintException) {
                errorMap[propertyName] = e
            }
        }
        if (errorMap.isNotEmpty()) {
            val nestedErrors = errorMap.keys.map { "[ property: ${errorMap[it]?.message} ]" }.joinToString(";")
            throw BlueprintException("Failed to enhance properties $nestedErrors")
        }
    }

    fun enhancePropertyDefinition(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        propertyDefinition: PropertyDefinition
    ) {
        val enhancers = getPropertyDefinitionEnhancers()
        doEnhancement(bluePrintRuntimeService, name, propertyDefinition, enhancers)
    }

    fun enhanceAttributeDefinitions(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        attributes: MutableMap<String, AttributeDefinition>
    ) {
        val errorMap = linkedMapOf<String, BlueprintException>()
        attributes.forEach { attributeName, attributeDefinition ->
            try {
                enhanceAttributeDefinition(bluePrintRuntimeService, attributeName, attributeDefinition)
            } catch (e: BlueprintException) {
                errorMap[attributeName] = e
            }
        }
        if (errorMap.isNotEmpty()) {
            val nestedErrors = errorMap.keys.map { "[ attribute: ${errorMap[it]?.message} ]" }.joinToString(";")
            throw BlueprintException("Failed to enhance attributes $nestedErrors")
        }
    }

    fun enhanceAttributeDefinition(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        attributeDefinition: AttributeDefinition
    ) {
        val enhancers = getAttributeDefinitionEnhancers()
        doEnhancement(bluePrintRuntimeService, name, attributeDefinition, enhancers)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> doEnhancement(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        definition: Any,
        enhancers: List<BlueprintEnhancer<T>>
    ) {
        if (enhancers.isNotEmpty()) {
            val errorMap = linkedMapOf<String, BlueprintException>()
            enhancers.forEach {
                try {
                    it.enhance(bluePrintRuntimeService, name, definition as T)
                } catch (e: BlueprintException) {
                    errorMap[name] = e
                }
            }
            if (errorMap.isNotEmpty()) {
                val nestedErrors = errorMap.keys.map {
                    "${errorMap[it]?.message ?: errorMap[it].toString()}"
                }.joinToString(";")
                throw BlueprintException("$name-->$nestedErrors")
            }
        }
    }
}

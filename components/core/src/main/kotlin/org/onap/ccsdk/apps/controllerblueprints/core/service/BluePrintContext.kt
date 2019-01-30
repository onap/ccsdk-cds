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
@file:Suppress("unused")

package org.onap.ccsdk.apps.controllerblueprints.core.service

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils

/**
 *
 *
 * @author Brinda Santh
 */
class BluePrintContext(val serviceTemplate: ServiceTemplate) {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())

    /**
     * Blueprint CBA extracted file location
     */
    var rootPath = "."
    /**
     * Root Definition file path
     */
    var entryDefinition = ""

    val imports: List<ImportDefinition>? = serviceTemplate.imports

    val metadata: MutableMap<String, String>? = serviceTemplate.metadata

    val dataTypes: MutableMap<String, DataType>? = serviceTemplate.dataTypes

    val inputs: MutableMap<String, PropertyDefinition>? = serviceTemplate.topologyTemplate?.inputs

    fun blueprintJson(pretty: Boolean = false): String = print("json", pretty)

    private fun print(type: String? = "json", pretty: Boolean = false): String {
        return JacksonUtils.getJson(serviceTemplate, pretty)
    }

    fun name(): String = metadata?.get(BluePrintConstants.METADATA_TEMPLATE_NAME)
            ?: throw BluePrintException("could't get template name from meta data")

    fun version(): String = metadata?.get(BluePrintConstants.METADATA_TEMPLATE_VERSION)
            ?: throw BluePrintException("could't get template version from meta data")

    fun author(): String = metadata?.get(BluePrintConstants.METADATA_TEMPLATE_AUTHOR)
            ?: throw BluePrintException("could't get template author from meta data")

    // Workflow
    val workflows: MutableMap<String, Workflow>? = serviceTemplate.topologyTemplate?.workflows

    fun workflowByName(workFlowName: String): Workflow = workflows?.get(workFlowName)
            ?: throw BluePrintException("could't get workflow($workFlowName)")

    fun workflowInputs(workFlowName: String) = workflowByName(workFlowName).inputs

    fun workflowStepByName(workFlowName: String, stepName: String): Step {
        return workflowByName(workFlowName).steps?.get(stepName)
                ?: throw BluePrintException("could't get step($stepName) for workflow($workFlowName)")
    }

    fun workflowStepNodeTemplate(workFlowName: String, stepName: String): String {
        return workflowStepByName(workFlowName, stepName).target
                ?: throw BluePrintException("could't get node template name for workflow($workFlowName)'s step($stepName)")
    }

    fun workflowFirstStepNodeTemplate(workFlowName: String): String {
        val firstStepName = workflowByName(workFlowName).steps?.keys?.first()
                ?: throw BluePrintException("could't get first step for workflow($workFlowName)")
        return workflowStepNodeTemplate(workFlowName, firstStepName)
    }

    fun workflowStepFirstCallOperation(workFlowName: String, stepName: String): String {
        return workflowStepByName(workFlowName, stepName).activities?.filter { it.callOperation != null }?.single()?.callOperation
                ?: throw BluePrintException("could't get first callOperation for WorkFlow($workFlowName) ")
    }

    // Data Type
    fun dataTypeByName(name: String): DataType? = dataTypes?.get(name)

    // Artifact Type
    val artifactTypes: MutableMap<String, ArtifactType>? = serviceTemplate.artifactTypes

    // Policy Types
    val policyTypes: MutableMap<String, PolicyType>? = serviceTemplate.policyTypes

    fun policyTypeByName(policyName: String) = policyTypes?.get(policyName)
            ?: throw BluePrintException("could't get policy type for the name($policyName)")

    fun policyTypesDerivedFrom(name: String): MutableMap<String, PolicyType>? {
        return policyTypes?.filterValues { policyType -> policyType.derivedFrom == name }?.toMutableMap()
    }

    fun policyTypesTarget(target: String): MutableMap<String, PolicyType>? {
        return policyTypes?.filterValues { it.targets.contains(target) }?.toMutableMap()
    }

    fun policyTypesTargetNDerivedFrom(target: String, derivedFrom: String): MutableMap<String, PolicyType>? {
        return policyTypesDerivedFrom(derivedFrom)?.filterValues {
            it.targets.contains(target)
        }?.toMutableMap()
    }

    // Node Type Methods
    val nodeTypes: MutableMap<String, NodeType>? = serviceTemplate.nodeTypes

    fun nodeTypeByName(name: String): NodeType =
            nodeTypes?.get(name)
                    ?: throw BluePrintException("could't get node type for the name($name)")

    fun nodeTypeDerivedFrom(name: String): MutableMap<String, NodeType>? {
        return nodeTypes?.filterValues { nodeType -> nodeType.derivedFrom == name }?.toMutableMap()
    }

    fun nodeTypeInterface(nodeTypeName: String, interfaceName: String): InterfaceDefinition {
        return nodeTypeByName(nodeTypeName).interfaces?.get(interfaceName)
                ?: throw BluePrintException("could't get node type($nodeTypeName)'s interface definition($interfaceName)")
    }

    fun nodeTypeInterfaceOperation(nodeTypeName: String, interfaceName: String, operationName: String): OperationDefinition {
        return nodeTypeInterface(nodeTypeName, interfaceName).operations?.get(operationName)
                ?: throw BluePrintException("could't get node type($nodeTypeName)'s interface definition($interfaceName) operation definition($operationName)")
    }

    fun interfaceNameForNodeType(nodeTypeName: String): String {
        return nodeTypeByName(nodeTypeName).interfaces?.keys?.first()
                ?: throw BluePrintException("could't get NodeType($nodeTypeName)'s first InterfaceDefinition name")
    }

    fun nodeTypeInterfaceOperationInputs(nodeTypeName: String, interfaceName: String, operationName: String): MutableMap<String, PropertyDefinition>? {
        return nodeTypeInterfaceOperation(nodeTypeName, interfaceName, operationName).inputs
    }

    fun nodeTypeInterfaceOperationOutputs(nodeTypeName: String, interfaceName: String, operationName: String): MutableMap<String, PropertyDefinition>? {
        return nodeTypeInterfaceOperation(nodeTypeName, interfaceName, operationName).outputs
    }

    // Node Template Methods
    val nodeTemplates: MutableMap<String, NodeTemplate>? = serviceTemplate.topologyTemplate?.nodeTemplates

    fun nodeTemplateByName(name: String): NodeTemplate =
            nodeTemplates?.get(name) ?: throw BluePrintException("could't get node template for the name($name)")

    fun nodeTemplateForNodeType(name: String): MutableMap<String, NodeTemplate>? {
        return nodeTemplates?.filterValues { nodeTemplate -> nodeTemplate.type == name }?.toMutableMap()
    }

    fun nodeTemplateNodeType(nodeTemplateName: String): NodeType {
        val nodeTemplateType: String = nodeTemplateByName(nodeTemplateName).type
        return nodeTypeByName(nodeTemplateType)
    }

    fun nodeTemplateProperty(nodeTemplateName: String, propertyName: String): Any? {
        return nodeTemplateByName(nodeTemplateName).properties?.get(propertyName)
    }

    fun nodeTemplateArtifacts(nodeTemplateName: String): MutableMap<String, ArtifactDefinition>? {
        return nodeTemplateByName(nodeTemplateName).artifacts
    }

    fun nodeTemplateArtifact(nodeTemplateName: String, artifactName: String): ArtifactDefinition {
        return nodeTemplateArtifacts(nodeTemplateName)?.get(artifactName)
                ?: throw BluePrintException("could't get NodeTemplate($nodeTemplateName)'s ArtifactDefinition($artifactName)")
    }

    fun nodeTemplateArtifactForArtifactType(nodeTemplateName: String, artifactType: String): ArtifactDefinition {
        return nodeTemplateArtifacts(nodeTemplateName)?.filter { it.value.type == artifactType }?.map { it.value }?.get(0)
                ?: throw BluePrintException("could't get NodeTemplate($nodeTemplateName)'s Artifact Type($artifactType)")
    }

    fun nodeTemplateFirstInterface(nodeTemplateName: String): InterfaceAssignment {
        return nodeTemplateByName(nodeTemplateName).interfaces?.values?.first()
                ?: throw BluePrintException("could't get NodeTemplate($nodeTemplateName)'s first InterfaceAssignment")
    }

    fun nodeTemplateFirstInterfaceName(nodeTemplateName: String): String {
        return nodeTemplateByName(nodeTemplateName).interfaces?.keys?.first()
                ?: throw BluePrintException("could't get NodeTemplate($nodeTemplateName)'s first InterfaceAssignment name")
    }

    fun nodeTemplateFirstInterfaceFirstOperationName(nodeTemplateName: String): String {
        return nodeTemplateFirstInterface(nodeTemplateName).operations?.keys?.first()
                ?: throw BluePrintException("could't get NodeTemplate($nodeTemplateName)'s first InterfaceAssignment's first OperationAssignment name")
    }

    fun nodeTemplateInterfaceOperationInputs(nodeTemplateName: String, interfaceName: String, operationName: String): MutableMap<String, JsonNode>? {
        return nodeTemplateInterfaceOperation(nodeTemplateName, interfaceName, operationName).inputs
    }

    fun nodeTemplateInterfaceOperationOutputs(nodeTemplateName: String, interfaceName: String, operationName: String): MutableMap<String, JsonNode>? {
        return nodeTemplateInterfaceOperation(nodeTemplateName, interfaceName, operationName).outputs
    }

    fun nodeTemplateInterface(nodeTemplateName: String, interfaceName: String): InterfaceAssignment {
        return nodeTemplateByName(nodeTemplateName).interfaces?.get(interfaceName)
                ?: throw BluePrintException("could't get NodeTemplate($nodeTemplateName)'s InterfaceAssignment($interfaceName)")
    }

    fun nodeTemplateInterfaceOperation(nodeTemplateName: String, interfaceName: String, operationName: String): OperationAssignment {
        return nodeTemplateInterface(nodeTemplateName, interfaceName).operations?.get(operationName)
                ?: throw BluePrintException("could't get NodeTemplate($nodeTemplateName)'s InterfaceAssignment($interfaceName) OperationAssignment($operationName)")
    }

    fun nodeTemplateCapability(nodeTemplateName: String, capabilityName: String): CapabilityAssignment {
        return nodeTemplateByName(nodeTemplateName).capabilities?.get(capabilityName)
                ?: throw BluePrintException("could't get NodeTemplate($nodeTemplateName)'s CapabilityAssignment($capabilityName)")
    }

    fun nodeTemplateRequirement(nodeTemplateName: String, requirementName: String): RequirementAssignment {
        return nodeTemplateByName(nodeTemplateName).requirements?.get(requirementName)
                ?: throw BluePrintException("could't get NodeTemplate($nodeTemplateName)'s first RequirementAssignment($requirementName)")
    }

    fun nodeTemplateRequirementNode(nodeTemplateName: String, requirementName: String): NodeTemplate {
        val filteredNodeTemplateName: String = nodeTemplateByName(nodeTemplateName).requirements?.get(requirementName)?.node
                ?: throw BluePrintException("could't NodeTemplate for NodeTemplate's($nodeTemplateName) requirement's ($requirementName) ")
        return nodeTemplateByName(filteredNodeTemplateName)
    }

    fun nodeTemplateCapabilityProperty(nodeTemplateName: String, capabilityName: String, propertyName: String): Any? {
        return nodeTemplateCapability(nodeTemplateName, capabilityName).properties?.get(propertyName)
    }

    // Chained Functions

    fun nodeTypeChained(nodeTypeName: String): NodeType {
        return BluePrintChainedService(this).nodeTypeChained(nodeTypeName)
    }

    fun nodeTypeChainedProperties(nodeTypeName: String): MutableMap<String, PropertyDefinition>? {
        return BluePrintChainedService(this).nodeTypeChainedProperties(nodeTypeName)
    }

}
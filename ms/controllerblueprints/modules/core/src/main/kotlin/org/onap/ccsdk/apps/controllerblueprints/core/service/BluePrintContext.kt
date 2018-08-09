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

package org.onap.ccsdk.apps.controllerblueprints.core.service

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 *
 *
 * @author Brinda Santh
 */
class BluePrintContext(serviceTemplate: ServiceTemplate) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.toString())

    val serviceTemplate: ServiceTemplate = serviceTemplate

    val imports: List<ImportDefinition>? = serviceTemplate.imports

    val metadata: MutableMap<String, String>? = serviceTemplate.metadata

    val dataTypes: MutableMap<String, DataType>? = serviceTemplate.dataTypes

    val inputs: MutableMap<String, PropertyDefinition>? = serviceTemplate.topologyTemplate?.inputs

    val workflows: MutableMap<String, Workflow>? = serviceTemplate.topologyTemplate?.workflows

    fun blueprintJson(pretty: Boolean = false): String = print("json", pretty)

    fun blueprintYaml(pretty: Boolean = false): String = print("yaml", pretty)

    private fun print(type: String? = "json", pretty: Boolean = false): String {
        return JacksonUtils.getJson(serviceTemplate, pretty)
    }

    // Workflow
    fun workflowByName(name: String): Workflow? = workflows?.get(name)

    // Data Type
    fun dataTypeByName(name: String): DataType? = dataTypes?.get(name)

    // Artifact Type
    val artifactTypes: MutableMap<String, ArtifactType>? = serviceTemplate.artifactTypes

    // Node Type Methods
    val nodeTypes: MutableMap<String, NodeType>? = serviceTemplate.nodeTypes

    fun nodeTypeByName(name: String): NodeType =
            nodeTypes?.get(name) ?: throw BluePrintException(String.format("Failed to get node type for the name : %s", name))

    fun nodeTypeDerivedFrom(name: String): MutableMap<String, NodeType>? {
        return nodeTypes?.filterValues { nodeType -> nodeType.derivedFrom == name }?.toMutableMap()
    }

    fun nodeTypeInterface(nodeTypeName: String, interfaceName: String): InterfaceDefinition? {
        return nodeTypeByName(nodeTypeName).interfaces?.values?.first()
    }

    fun nodeTypeInterfaceOperation(nodeTypeName: String, interfaceName: String, operationName: String): OperationDefinition? {
        return nodeTypeInterface(nodeTypeName, interfaceName)?.operations?.get(operationName)
    }

    fun interfaceNameForNodeType(nodeTypeName: String): String? {
        return nodeTypeByName(nodeTypeName).interfaces?.keys?.first()
    }

    fun nodeTypeInterfaceOperationInputs(nodeTypeName: String, interfaceName: String, operationName: String): MutableMap<String, PropertyDefinition>? {
        return nodeTypeInterfaceOperation(nodeTypeName, interfaceName, operationName)?.inputs
    }

    fun nodeTypeInterfaceOperationOutputs(nodeTypeName: String, interfaceName: String, operationName: String): MutableMap<String, PropertyDefinition>? {
        return nodeTypeInterfaceOperation(nodeTypeName, interfaceName, operationName)?.outputs
    }

    // Node Template Methods
    val nodeTemplates: MutableMap<String, NodeTemplate>? = serviceTemplate.topologyTemplate?.nodeTemplates

    fun nodeTemplateByName(name: String): NodeTemplate =
            nodeTemplates?.get(name) ?: throw BluePrintException("Failed to get node template for the name " + name)

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

    fun nodeTemplateArtifact(nodeTemplateName: String, artifactName: String): ArtifactDefinition? {
        return nodeTemplateArtifacts(nodeTemplateName)?.get(artifactName)
    }

    fun nodeTemplateFirstInterface(nodeTemplateName: String): InterfaceAssignment? {
        return nodeTemplateByName(nodeTemplateName).interfaces?.values?.first()
    }

    fun nodeTemplateFirstInterfaceName(nodeTemplateName: String): String? {
        return nodeTemplateByName(nodeTemplateName).interfaces?.keys?.first()
    }

    fun nodeTemplateFirstInterfaceFirstOperationName(nodeTemplateName: String): String? {
        return nodeTemplateFirstInterface(nodeTemplateName)?.operations?.keys?.first()
    }

    fun nodeTemplateInterfaceOperationInputs(nodeTemplateName: String, interfaceName: String, operationName: String): MutableMap<String, JsonNode>? {
        return nodeTemplateByName(nodeTemplateName).interfaces?.get(interfaceName)?.operations?.get(operationName)?.inputs
    }

    fun nodeTemplateInterfaceOperationOutputs(nodeTemplateName: String, interfaceName: String, operationName: String): MutableMap<String, JsonNode>? {
        return nodeTemplateByName(nodeTemplateName).interfaces?.get(interfaceName)?.operations?.get(operationName)?.outputs
    }

    fun nodeTemplateInterface(nodeTemplateName: String, interfaceName: String): InterfaceAssignment? {
        return nodeTemplateByName(nodeTemplateName).interfaces?.get(interfaceName)
    }


    fun nodeTemplateInterfaceOperation(nodeTemplateName: String, interfaceName: String, operationName: String): OperationAssignment? {
        return nodeTemplateInterface(nodeTemplateName, interfaceName)?.operations?.get(operationName)
    }

    fun nodeTemplateCapability(nodeTemplateName: String, capabilityName: String): CapabilityAssignment? {
        return nodeTemplateByName(nodeTemplateName).capabilities?.get(capabilityName)
    }

    fun nodeTemplateRequirement(nodeTemplateName: String, requirementName: String): RequirementAssignment? {
        return nodeTemplateByName(nodeTemplateName).requirements?.get(requirementName)
    }

    fun nodeTemplateRequirementNode(nodeTemplateName: String, requirementName: String): NodeTemplate {
        val nodeTemplateName: String = nodeTemplateByName(nodeTemplateName).requirements?.get(requirementName)?.node
                ?: throw BluePrintException(String.format("failed to get node name for node template's (%s) requirement's (%s) " + nodeTemplateName, requirementName))
        return nodeTemplateByName(nodeTemplateName)
    }

    fun nodeTemplateCapabilityProperty(nodeTemplateName: String, capabilityName: String, propertyName: String): Any? {
        return nodeTemplateCapability(nodeTemplateName, capabilityName)?.properties?.get(propertyName)
    }

    // Chained Functions

    fun nodeTypeChained(nodeTypeName: String): NodeType {
        return BluePrintChainedService(this).nodeTypeChained(nodeTypeName)
    }

    fun nodeTypeChainedProperties(nodeTypeName: String): MutableMap<String, PropertyDefinition>? {
        return BluePrintChainedService(this).nodeTypeChainedProperties(nodeTypeName)
    }

}
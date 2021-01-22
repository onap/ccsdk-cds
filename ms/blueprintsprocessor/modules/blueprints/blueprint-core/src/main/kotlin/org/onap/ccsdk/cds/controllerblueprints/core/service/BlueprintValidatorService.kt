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

package org.onap.ccsdk.cds.controllerblueprints.core.service

import com.fasterxml.jackson.databind.JsonNode
import com.google.common.base.Preconditions
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.CapabilityAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.CapabilityDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.Implementation
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.RequirementAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.RequirementDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.TopologyTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.cds.controllerblueprints.core.format
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import java.io.Serializable

/**
 *
 *
 * @author Brinda Santh
 */
interface BlueprintValidatorService : Serializable {

    @Throws(BlueprintException::class)
    fun validateBlueprint(bluePrintContext: BlueprintContext, properties: MutableMap<String, Any>)

    @Throws(BlueprintException::class)
    fun validateBlueprint(serviceTemplate: ServiceTemplate, properties: MutableMap<String, Any>)
}

@Deprecated("Decomposed implementation moved to blueprint-validation module")
open class BlueprintValidatorDefaultService : BlueprintValidatorService {

    val log = LoggerFactory.getLogger(BlueprintValidatorDefaultService::class.toString())

    lateinit var bluePrintContext: BlueprintContext
    lateinit var serviceTemplate: ServiceTemplate
    lateinit var properties: MutableMap<String, Any>
    var message: StringBuilder = StringBuilder()
    private val separator: String = BlueprintConstants.PATH_DIVIDER
    var paths: MutableList<String> = arrayListOf()

    @Throws(BlueprintException::class)
    override fun validateBlueprint(bluePrintContext: BlueprintContext, properties: MutableMap<String, Any>) {
        validateBlueprint(bluePrintContext.serviceTemplate, properties)
    }

    @Throws(BlueprintException::class)
    override fun validateBlueprint(serviceTemplate: ServiceTemplate, properties: MutableMap<String, Any>) {
        this.bluePrintContext = BlueprintContext(serviceTemplate)
        this.serviceTemplate = serviceTemplate
        this.properties = properties
        try {
            message.appendln("-> Config Blueprint")
            serviceTemplate.metadata?.let { validateMetadata(serviceTemplate.metadata!!) }
            serviceTemplate.artifactTypes?.let { validateArtifactTypes(serviceTemplate.artifactTypes!!) }
            serviceTemplate.dataTypes?.let { validateDataTypes(serviceTemplate.dataTypes!!) }
            serviceTemplate.nodeTypes?.let { validateNodeTypes(serviceTemplate.nodeTypes!!) }
            serviceTemplate.topologyTemplate?.let { validateTopologyTemplate(serviceTemplate.topologyTemplate!!) }
        } catch (e: Exception) {
            log.error("validation failed in the path : {}", paths.joinToString(separator), e)
            log.error("validation trace message :{} ", message)
            throw BlueprintException(
                e,
                format("failed to validate blueprint on path ({}) with message {}", paths.joinToString(separator), e.message)
            )
        }
    }

    @Throws(BlueprintException::class)
    open fun validateMetadata(metaDataMap: MutableMap<String, String>) {
        paths.add("metadata")

        val templateName = metaDataMap[BlueprintConstants.METADATA_TEMPLATE_NAME]
        val templateVersion = metaDataMap[BlueprintConstants.METADATA_TEMPLATE_VERSION]
        val templateTags = metaDataMap[BlueprintConstants.METADATA_TEMPLATE_TAGS]
        val templateAuthor = metaDataMap[BlueprintConstants.METADATA_TEMPLATE_AUTHOR]

        Preconditions.checkArgument(StringUtils.isNotBlank(templateName), "failed to get template name metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateVersion), "failed to get template version metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateTags), "failed to get template tags metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateAuthor), "failed to get template author metadata")
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateArtifactTypes(artifactTypes: MutableMap<String, ArtifactType>) {
        paths.add("artifact_types")
        artifactTypes.forEach { artifactName, artifactType ->
            paths.add(artifactName)
            message.appendln("--> Artifact Type :" + paths.joinToString(separator))
            artifactType.properties?.let { validatePropertyDefinitions(artifactType.properties!!) }
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateDataTypes(dataTypes: MutableMap<String, DataType>) {
        paths.add("dataTypes")
        dataTypes.forEach { dataTypeName, dataType ->
            paths.add(dataTypeName)
            message.appendln("--> DataType :" + paths.joinToString(separator))
            dataType.properties?.let { validatePropertyDefinitions(dataType.properties!!) }
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateNodeTypes(nodeTypes: MutableMap<String, NodeType>) {
        paths.add("nodeTypes")
        nodeTypes.forEach { nodeTypeName, nodeType ->
            // Validate Single Node Type
            validateNodeType(nodeTypeName, nodeType)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateNodeType(nodeTypeName: String, nodeType: NodeType) {
        paths.add(nodeTypeName)
        message.appendln("--> Node Type :" + paths.joinToString(separator))
        val derivedFrom: String = nodeType.derivedFrom
        // Check Derived From
        checkValidNodeTypesDerivedFrom(nodeTypeName, derivedFrom)

        if (!BlueprintTypes.rootNodeTypes().contains(derivedFrom)) {
            serviceTemplate.nodeTypes?.get(derivedFrom)
                ?: throw BlueprintException(
                    format(
                        "Failed to get derivedFrom NodeType({})'s for NodeType({}) ",
                        derivedFrom, nodeTypeName
                    )
                )
        }

        nodeType.properties?.let { validatePropertyDefinitions(nodeType.properties!!) }
        nodeType.capabilities?.let { validateCapabilityDefinitions(nodeTypeName, nodeType) }
        nodeType.requirements?.let { validateRequirementDefinitions(nodeTypeName, nodeType) }
        nodeType.interfaces?.let { validateInterfaceDefinitions(nodeType.interfaces!!) }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun checkValidNodeTypesDerivedFrom(nodeTypeName: String, derivedFrom: String) {
        check(BlueprintTypes.validNodeTypeDerivedFroms.contains(derivedFrom)) {
            throw BlueprintException(format("Failed to get node type ({})'s  derivedFrom({}) definition ", nodeTypeName, derivedFrom))
        }
    }

    @Throws(BlueprintException::class)
    open fun validateTopologyTemplate(topologyTemplate: TopologyTemplate) {
        paths.add("topology")
        message.appendln("--> Topology Template")
        topologyTemplate.inputs?.let { validateInputs(topologyTemplate.inputs!!) }
        topologyTemplate.nodeTemplates?.let { validateNodeTemplates(topologyTemplate.nodeTemplates!!) }
        topologyTemplate.workflows?.let { validateWorkFlows(topologyTemplate.workflows!!) }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateInputs(inputs: MutableMap<String, PropertyDefinition>) {
        paths.add("inputs")
        message.appendln("---> Input :" + paths.joinToString(separator))
        validatePropertyDefinitions(inputs)
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateNodeTemplates(nodeTemplates: MutableMap<String, NodeTemplate>) {
        paths.add("nodeTemplates")
        nodeTemplates.forEach { nodeTemplateName, nodeTemplate ->
            validateNodeTemplate(nodeTemplateName, nodeTemplate)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateNodeTemplate(nodeTemplateName: String, nodeTemplate: NodeTemplate) {
        paths.add(nodeTemplateName)
        message.appendln("---> NodeTemplate :" + paths.joinToString(separator))
        val type: String = nodeTemplate.type

        val nodeType: NodeType = serviceTemplate.nodeTypes?.get(type)
            ?: throw BlueprintException(format("Failed to get NodeType({}) definition for NodeTemplate({})", type, nodeTemplateName))

        nodeTemplate.artifacts?.let { validateArtifactDefinitions(nodeTemplate.artifacts!!) }
        nodeTemplate.properties?.let { validatePropertyAssignments(nodeType.properties!!, nodeTemplate.properties!!) }
        nodeTemplate.capabilities?.let { validateCapabilityAssignments(nodeType, nodeTemplateName, nodeTemplate) }
        nodeTemplate.requirements?.let { validateRequirementAssignments(nodeType, nodeTemplateName, nodeTemplate) }
        nodeTemplate.interfaces?.let { validateInterfaceAssignments(nodeType, nodeTemplateName, nodeTemplate) }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateArtifactDefinitions(artifacts: MutableMap<String, ArtifactDefinition>) {
        paths.add("artifacts")
        artifacts.forEach { artifactDefinitionName, artifactDefinition ->
            paths.add(artifactDefinitionName)
            message.appendln("Validating artifact " + paths.joinToString(separator))
            val type: String = artifactDefinition.type
                ?: throw BlueprintException(format("type is missing for ArtifactDefinition({})", artifactDefinitionName))
            // Check Artifact Type
            checkValidArtifactType(artifactDefinitionName, type)

            val file: String = artifactDefinition.file
                ?: throw BlueprintException(format("file is missing for ArtifactDefinition({})", artifactDefinitionName))

            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateWorkFlows(workflows: MutableMap<String, Workflow>) {
        paths.add("workflows")
        workflows.forEach { workflowName, workflow ->

            // Validate Single workflow
            validateWorkFlow(workflowName, workflow)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateWorkFlow(workflowName: String, workflow: Workflow) {
        paths.add(workflowName)
        message.appendln("---> Workflow :" + paths.joinToString(separator))
        // Step Validation Start
        paths.add("steps")
        workflow.steps?.forEach { stepName, _ ->
            paths.add(stepName)
            message.appendln("----> Steps :" + paths.joinToString(separator))
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
        // Step Validation Ends
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validatePropertyDefinitions(properties: MutableMap<String, PropertyDefinition>) {
        paths.add("properties")
        properties.forEach { propertyName, propertyDefinition ->
            paths.add(propertyName)
            val dataType: String = propertyDefinition.type
            when {
                BlueprintTypes.validPrimitiveTypes().contains(dataType) -> {
                    // Do Nothing
                }
                BlueprintTypes.validCollectionTypes().contains(dataType) -> {
                    val entrySchemaType: String = propertyDefinition.entrySchema?.type
                        ?: throw BlueprintException(format("Entry schema for DataType ({}) for the property ({}) not found", dataType, propertyName))
                    checkPrimitiveOrComplex(entrySchemaType, propertyName)
                }
                else -> checkPropertyDataType(dataType, propertyName)
            }
            message.appendln("property " + paths.joinToString(separator) + " of type " + dataType)
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validatePropertyAssignments(
        nodeTypeProperties: MutableMap<String, PropertyDefinition>,
        properties: MutableMap<String, JsonNode>
    ) {
        properties.forEach { propertyName, propertyAssignment ->
            val propertyDefinition: PropertyDefinition = nodeTypeProperties[propertyName]
                ?: throw BlueprintException(format("failed to get definition for the property ({})", propertyName))

            validatePropertyAssignment(propertyName, propertyDefinition, propertyAssignment)
        }
    }

    @Throws(BlueprintException::class)
    open fun validatePropertyAssignment(
        propertyName: String,
        propertyDefinition: PropertyDefinition,
        propertyAssignment: JsonNode
    ) {
        // Check and Validate if Expression Node
        val expressionData = BlueprintExpressionService.getExpressionData(propertyAssignment)
        if (!expressionData.isExpression) {
            checkPropertyValue(propertyName, propertyDefinition, propertyAssignment)
        }
    }

    @Throws(BlueprintException::class)
    open fun validateCapabilityAssignments(nodeType: NodeType, nodeTemplateName: String, nodeTemplate: NodeTemplate) {
        val capabilities = nodeTemplate.capabilities
        paths.add("capabilities")
        capabilities?.forEach { capabilityName, capabilityAssignment ->
            paths.add(capabilityName)

            val capabilityDefinition = nodeType.capabilities?.get(capabilityName)
                ?: throw BlueprintException(
                    format(
                        "Failed to get NodeTemplate({}) capability definition ({}) " +
                            "from NodeType({}) ",
                        nodeTemplateName, capabilityName, nodeTemplate.type
                    )
                )

            validateCapabilityAssignment(nodeTemplateName, capabilityName, capabilityDefinition, capabilityAssignment)

            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateCapabilityAssignment(
        nodeTemplateName: String,
        capabilityName: String,
        capabilityDefinition: CapabilityDefinition,
        capabilityAssignment: CapabilityAssignment
    ) {

        capabilityAssignment.properties?.let { validatePropertyAssignments(capabilityDefinition.properties!!, capabilityAssignment.properties!!) }
    }

    @Throws(BlueprintException::class)
    open fun validateRequirementAssignments(nodeType: NodeType, nodeTemplateName: String, nodeTemplate: NodeTemplate) {
        val requirements = nodeTemplate.requirements
        paths.add("requirements")
        requirements?.forEach { requirementName, requirementAssignment ->
            paths.add(requirementName)
            val requirementDefinition = nodeType.requirements?.get(requirementName)
                ?: throw BlueprintException(
                    format(
                        "Failed to get NodeTemplate({}) requirement definition ({}) from" +
                            " NodeType({}) ",
                        nodeTemplateName, requirementName, nodeTemplate.type
                    )
                )
            // Validate Requirement Assignment
            validateRequirementAssignment(nodeTemplateName, requirementName, requirementDefinition, requirementAssignment)
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateRequirementAssignment(
        nodeTemplateName: String,
        requirementAssignmentName: String,
        requirementDefinition: RequirementDefinition,
        requirementAssignment: RequirementAssignment
    ) {
        log.info("Validating NodeTemplate({}) requirement assignment ({}) ", nodeTemplateName, requirementAssignmentName)
        val requirementNodeTemplateName = requirementAssignment.node!!
        val capabilityName = requirementAssignment.capability
        val relationship = requirementAssignment.relationship!!

        check(BlueprintTypes.validRelationShipDerivedFroms.contains(relationship)) {
            throw BlueprintException(
                format(
                    "Failed to get relationship type ({}) for NodeTemplate({})'s requirement({}) ",
                    relationship, nodeTemplateName, requirementAssignmentName
                )
            )
        }

        val relationShipNodeTemplate = serviceTemplate.topologyTemplate?.nodeTemplates?.get(requirementNodeTemplateName)
            ?: throw BlueprintException(
                format(
                    "Failed to get requirement NodeTemplate({})'s for NodeTemplate({}) requirement({}) ",
                    requirementNodeTemplateName, nodeTemplateName, requirementAssignmentName
                )
            )

        relationShipNodeTemplate.capabilities?.get(capabilityName)
            ?: throw BlueprintException(
                format(
                    "Failed to get requirement NodeTemplate({})'s capability({}) for NodeTemplate ({})'s requirement({}) ",
                    requirementNodeTemplateName, capabilityName, nodeTemplateName, requirementAssignmentName
                )
            )
    }

    @Throws(BlueprintException::class)
    open fun validateInterfaceAssignments(nodeType: NodeType, nodeTemplateName: String, nodeTemplate: NodeTemplate) {

        val interfaces = nodeTemplate.interfaces
        paths.add("interfaces")
        interfaces?.forEach { interfaceAssignmentName, interfaceAssignment ->
            paths.add(interfaceAssignmentName)
            val interfaceDefinition = nodeType.interfaces?.get(interfaceAssignmentName)
                ?: throw BlueprintException(
                    format(
                        "Failed to get NodeTemplate({}) interface definition ({}) from" +
                            " NodeType({}) ",
                        nodeTemplateName, interfaceAssignmentName, nodeTemplate.type
                    )
                )

            validateInterfaceAssignment(
                nodeTemplateName, interfaceAssignmentName, interfaceDefinition,
                interfaceAssignment
            )
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateInterfaceAssignment(
        nodeTemplateName: String,
        interfaceAssignmentName: String,
        interfaceDefinition: InterfaceDefinition,
        interfaceAssignment: InterfaceAssignment
    ) {

        val operations = interfaceAssignment.operations
        operations?.let {
            validateInterfaceOperationsAssignment(
                nodeTemplateName, interfaceAssignmentName, interfaceDefinition,
                interfaceAssignment
            )
        }
    }

    @Throws(BlueprintException::class)
    open fun validateInterfaceOperationsAssignment(
        nodeTemplateName: String,
        interfaceAssignmentName: String,
        interfaceDefinition: InterfaceDefinition,
        interfaceAssignment: InterfaceAssignment
    ) {

        val operations = interfaceAssignment.operations
        operations?.let {
            it.forEach { operationAssignmentName, operationAssignments ->

                val operationDefinition = interfaceDefinition.operations?.get(operationAssignmentName)
                    ?: throw BlueprintException(
                        format(
                            "Failed to get NodeTemplate({}) operation definition ({}) ",
                            nodeTemplateName, operationAssignmentName
                        )
                    )

                log.info(
                    "Validation NodeTemplate({}) Interface({}) Operation ({})", nodeTemplateName,
                    interfaceAssignmentName, operationAssignmentName
                )

                val inputs = operationAssignments.inputs
                val outputs = operationAssignments.outputs

                inputs?.forEach { propertyName, propertyAssignment ->
                    val propertyDefinition = operationDefinition.inputs?.get(propertyName)
                        ?: throw BlueprintException(
                            format(
                                "Failed to get NodeTemplate({}) operation definition ({}) " +
                                    "property definition({})",
                                nodeTemplateName, operationAssignmentName, propertyName
                            )
                        )
                    // Check the property values with property definition
                    validatePropertyAssignment(propertyName, propertyDefinition, propertyAssignment)
                }

                outputs?.forEach { propertyName, propertyAssignment ->
                    val propertyDefinition = operationDefinition.outputs?.get(propertyName)
                        ?: throw BlueprintException(
                            format(
                                "Failed to get NodeTemplate({}) operation definition ({}) " +
                                    "output property definition({})",
                                nodeTemplateName, operationAssignmentName,
                                propertyName
                            )
                        )
                    // Check the property values with property definition
                    validatePropertyAssignment(propertyName, propertyDefinition, propertyAssignment)
                }
            }
        }
    }

    @Throws(BlueprintException::class)
    open fun validateCapabilityDefinitions(nodeTypeName: String, nodeType: NodeType) {
        val capabilities = nodeType.capabilities
        paths.add("capabilities")
        capabilities?.forEach { capabilityName, capabilityDefinition ->
            paths.add(capabilityName)

            validateCapabilityDefinition(nodeTypeName, nodeType, capabilityName, capabilityDefinition)

            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateCapabilityDefinition(
        nodeTypeName: String,
        nodeType: NodeType,
        capabilityName: String,
        capabilityDefinition: CapabilityDefinition
    ) {
        val capabilityType = capabilityDefinition.type
        check(BlueprintTypes.validCapabilityTypes.contains(capabilityType)) {
            throw BlueprintException(
                format(
                    "Failed to get CapabilityType({}) for NodeType({})",
                    capabilityType, nodeTypeName
                )
            )
        }
    }

    @Throws(BlueprintException::class)
    open fun validateRequirementDefinitions(nodeName: String, nodeType: NodeType) {
        paths.add("requirements")
        val requirements = nodeType.requirements

        requirements?.forEach { requirementDefinitionName, requirementDefinition ->
            paths.add(requirementDefinitionName)
            message.appendln("Validating : " + paths.joinToString(separator))
            validateRequirementDefinition(nodeName, nodeType, requirementDefinitionName, requirementDefinition)
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateRequirementDefinition(
        nodeTypeName: String,
        nodeType: NodeType,
        requirementDefinitionName: String,
        requirementDefinition: RequirementDefinition
    ) {

        log.info("Validating NodeType({}) RequirementDefinition ({}) ", nodeTypeName, requirementDefinitionName)
        val requirementNodeTypeName = requirementDefinition.node!!
        val capabilityName = requirementDefinition.capability
        val relationship = requirementDefinition.relationship!!

        check(BlueprintTypes.validRelationShipDerivedFroms.contains(relationship)) {
            throw BlueprintException(
                format(
                    "Failed to get relationship({}) for NodeType({})'s requirement({}) ",
                    relationship, nodeTypeName, requirementDefinitionName
                )
            )
        }

        val relationShipNodeType = serviceTemplate.nodeTypes?.get(requirementNodeTypeName)
            ?: throw BlueprintException(
                format(
                    "Failed to get requirement NodeType({})'s for requirement({}) ",
                    requirementNodeTypeName, requirementDefinitionName
                )
            )

        relationShipNodeType.capabilities?.get(capabilityName)
            ?: throw BlueprintException(
                format(
                    "Failed to get requirement NodeType({})'s capability({}) for NodeType ({})'s requirement({}) ",
                    requirementNodeTypeName, capabilityName, nodeTypeName, requirementDefinitionName
                )
            )
    }

    @Throws(BlueprintException::class)
    open fun validateInterfaceDefinitions(interfaces: MutableMap<String, InterfaceDefinition>) {
        paths.add("interfaces")
        interfaces.forEach { interfaceName, interfaceDefinition ->
            paths.add(interfaceName)
            message.appendln("Validating : " + paths.joinToString(separator))
            interfaceDefinition.operations?.let { validateOperationDefinitions(interfaceDefinition.operations!!) }
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateOperationDefinitions(operations: MutableMap<String, OperationDefinition>) {
        paths.add("operations")
        operations.forEach { opertaionName, operationDefinition ->
            paths.add(opertaionName)
            message.appendln("Validating : " + paths.joinToString(separator))
            operationDefinition.implementation?.let { validateImplementation(operationDefinition.implementation!!) }
            operationDefinition.inputs?.let { validatePropertyDefinitions(operationDefinition.inputs!!) }
            operationDefinition.outputs?.let { validatePropertyDefinitions(operationDefinition.outputs!!) }
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BlueprintException::class)
    open fun validateImplementation(implementation: Implementation) {
        checkNotEmpty(implementation.primary) { "couldn't get implementation" }
    }

    @Throws(BlueprintException::class)
    open fun checkValidArtifactType(artifactDefinitionName: String, artifactTypeName: String) {

        val artifactType = serviceTemplate.artifactTypes?.get(artifactTypeName)
            ?: throw BlueprintException("failed to artifactType($artifactTypeName) for ArtifactDefinition($artifactDefinitionName)")

        checkValidArtifactTypeDerivedFrom(artifactTypeName, artifactType.derivedFrom)
    }

    @Throws(BlueprintException::class)
    open fun checkValidArtifactTypeDerivedFrom(artifactTypeName: String, derivedFrom: String) {
        check(BlueprintTypes.validArtifactTypeDerivedFroms.contains(derivedFrom)) {
            throw BlueprintException("failed to get artifactType($artifactTypeName)'s derivedFrom($derivedFrom) definition")
        }
    }

    @Throws(BlueprintException::class)
    open fun checkValidDataTypeDerivedFrom(dataTypeName: String, derivedFrom: String) {
        check(BlueprintTypes.validDataTypeDerivedFroms.contains(derivedFrom)) {
            throw BlueprintException(format("Failed to get DataType({})'s  derivedFrom({}) definition ", dataTypeName, derivedFrom))
        }
    }

    @Throws(BlueprintException::class)
    open fun checkValidRelationshipTypeDerivedFrom(relationshipTypeName: String, derivedFrom: String) {
        check(BlueprintTypes.validRelationShipDerivedFroms.contains(derivedFrom)) {
            throw BlueprintException(format("Failed to get relationship type ({})'s  derivedFrom({}) definition ", relationshipTypeName, derivedFrom))
        }
    }

    open fun checkPropertyValue(propertyName: String, propertyDefinition: PropertyDefinition, propertyAssignment: JsonNode) {
        val propertyType = propertyDefinition.type
        val isValid: Boolean

        if (BlueprintTypes.validPrimitiveTypes().contains(propertyType)) {
            isValid = JacksonUtils.checkJsonNodeValueOfPrimitiveType(propertyType, propertyAssignment)
        } else if (BlueprintTypes.validCollectionTypes().contains(propertyType)) {

            val entrySchemaType = propertyDefinition.entrySchema?.type
                ?: throw BlueprintException(format("Failed to get EntrySchema type for the collection property ({})", propertyName))

            if (!BlueprintTypes.validPropertyTypes().contains(entrySchemaType)) {
                checkPropertyDataType(entrySchemaType, propertyName)
            }
            isValid = JacksonUtils.checkJsonNodeValueOfCollectionType(propertyType, propertyAssignment)
        } else {
            checkPropertyDataType(propertyType, propertyName)
            isValid = true
        }

        check(isValid) {
            throw BlueprintException(
                format(
                    "property({}) defined of type({}) is not comptable with the value ({})",
                    propertyName, propertyType, propertyAssignment
                )
            )
        }
    }

    private fun checkPropertyDataType(dataTypeName: String, propertyName: String) {

        val dataType = serviceTemplate.dataTypes?.get(dataTypeName)
            ?: throw BlueprintException(format("DataType ({}) for the property ({}) not found", dataTypeName, propertyName))

        checkValidDataTypeDerivedFrom(propertyName, dataType.derivedFrom)
    }

    private fun checkPrimitiveOrComplex(dataType: String, propertyName: String): Boolean {
        if (BlueprintTypes.validPrimitiveTypes().contains(dataType) || checkDataType(dataType)) {
            return true
        } else {
            throw BlueprintException(format("DataType({}) for the property({}) is not valid", dataType, propertyName))
        }
    }

    private fun checkDataType(key: String): Boolean {
        return serviceTemplate.dataTypes?.containsKey(key) ?: false
    }
}

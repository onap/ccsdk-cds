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
import com.google.common.base.Preconditions
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.apps.controllerblueprints.core.*
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import java.io.Serializable

/**
 *
 *
 * @author Brinda Santh
 */
interface BluePrintValidatorService : Serializable {

    @Throws(BluePrintException::class)
    fun validateBlueprint(bluePrintContext: BluePrintContext, properties: MutableMap<String, Any>)

    @Throws(BluePrintException::class)
    fun validateBlueprint(serviceTemplate: ServiceTemplate, properties: MutableMap<String, Any>)
}

open class BluePrintValidatorDefaultService : BluePrintValidatorService {

    val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintValidatorDefaultService::class.toString())

    lateinit var bluePrintContext: BluePrintContext
    lateinit var serviceTemplate: ServiceTemplate
    lateinit var properties: MutableMap<String, Any>
    var message: StringBuilder = StringBuilder()
    private val separator: String = BluePrintConstants.PATH_DIVIDER
    var paths: MutableList<String> = arrayListOf()

    @Throws(BluePrintException::class)
    override fun validateBlueprint(bluePrintContext: BluePrintContext, properties: MutableMap<String, Any>) {
        validateBlueprint(bluePrintContext.serviceTemplate, properties)
    }

    @Throws(BluePrintException::class)
    override fun validateBlueprint(serviceTemplate: ServiceTemplate, properties: MutableMap<String, Any>) {
        this.bluePrintContext = BluePrintContext(serviceTemplate)
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
            throw BluePrintException(e,
                    format("failed to validate blueprint on path ({}) with message {}"
                            , paths.joinToString(separator), e.message))
        }
    }

    @Throws(BluePrintException::class)
    open fun validateMetadata(metaDataMap: MutableMap<String, String>) {
        paths.add("metadata")

        val templateName = metaDataMap[BluePrintConstants.METADATA_TEMPLATE_NAME]
        val templateVersion = metaDataMap[BluePrintConstants.METADATA_TEMPLATE_VERSION]
        val templateTags = metaDataMap[BluePrintConstants.METADATA_TEMPLATE_TAGS]
        val templateAuthor = metaDataMap[BluePrintConstants.METADATA_TEMPLATE_AUTHOR]

        Preconditions.checkArgument(StringUtils.isNotBlank(templateName), "failed to get template name metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateVersion), "failed to get template version metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateTags), "failed to get template tags metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateAuthor), "failed to get template author metadata")
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
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

    @Throws(BluePrintException::class)
    open fun validateDataTypes(dataTypes: MutableMap<String, DataType>) {
        paths.add("dataTypes")
        dataTypes.forEach { dataTypeName, dataType ->
            paths.add(dataTypeName)
            message.appendln("--> Data Type :" + paths.joinToString(separator))
            dataType.properties?.let { validatePropertyDefinitions(dataType.properties!!) }
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateNodeTypes(nodeTypes: MutableMap<String, NodeType>) {
        paths.add("nodeTypes")
        nodeTypes.forEach { nodeTypeName, nodeType ->
            // Validate Single Node Type
            validateNodeType(nodeTypeName, nodeType)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateNodeType(nodeTypeName: String, nodeType: NodeType) {
        paths.add(nodeTypeName)
        message.appendln("--> Node Type :" + paths.joinToString(separator))
        val derivedFrom: String = nodeType.derivedFrom
        //Check Derived From
        checkValidNodeTypesDerivedFrom(nodeTypeName, derivedFrom)

        nodeType.properties?.let { validatePropertyDefinitions(nodeType.properties!!) }
        nodeType.interfaces?.let { validateInterfaceDefinitions(nodeType.interfaces!!) }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun checkValidNodeTypesDerivedFrom(nodeTypeName: String, derivedFrom: String) {
        check(BluePrintTypes.validNodeTypeDerivedFroms.contains(derivedFrom)) {
            throw BluePrintException(format("Failed to get node type ({})'s  derived from({}) definition ", nodeTypeName, derivedFrom))
        }
    }

    @Throws(BluePrintException::class)
    open fun validateTopologyTemplate(topologyTemplate: TopologyTemplate) {
        paths.add("topology")
        message.appendln("--> Topology Template")
        topologyTemplate.inputs?.let { validateInputs(topologyTemplate.inputs!!) }
        topologyTemplate.nodeTemplates?.let { validateNodeTemplates(topologyTemplate.nodeTemplates!!) }
        topologyTemplate.workflows?.let { validateWorkFlows(topologyTemplate.workflows!!) }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateInputs(inputs: MutableMap<String, PropertyDefinition>) {
        paths.add("inputs")
        message.appendln("---> Input :" + paths.joinToString(separator))
        validatePropertyDefinitions(inputs)
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateNodeTemplates(nodeTemplates: MutableMap<String, NodeTemplate>) {
        paths.add("nodeTemplates")
        nodeTemplates.forEach { nodeTemplateName, nodeTemplate ->
            validateNodeTemplate(nodeTemplateName, nodeTemplate)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateNodeTemplate(nodeTemplateName: String, nodeTemplate: NodeTemplate) {
        paths.add(nodeTemplateName)
        message.appendln("---> Node Template :" + paths.joinToString(separator))
        val type: String = nodeTemplate.type

        val nodeType: NodeType = serviceTemplate.nodeTypes?.get(type)
                ?: throw BluePrintException(format("Failed to get node type definition  for node template : {}", nodeTemplateName))

        nodeTemplate.artifacts?.let { validateArtifactDefinitions(nodeTemplate.artifacts!!) }
        nodeTemplate.properties?.let { validatePropertyAssignments(nodeType.properties!!, nodeTemplate.properties!!) }
        nodeTemplate.capabilities?.let { validateCapabilityAssignments(nodeTemplate.capabilities!!) }
        nodeTemplate.requirements?.let { validateRequirementAssignments(nodeTemplate.requirements!!) }
        nodeTemplate.interfaces?.let { validateInterfaceAssignments(nodeTemplate.interfaces!!) }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateArtifactDefinitions(artifacts: MutableMap<String, ArtifactDefinition>) {
        paths.add("artifacts")
        artifacts.forEach { artifactDefinitionName, artifactDefinition ->
            paths.add(artifactDefinitionName)
            message.appendln("Validating artifact " + paths.joinToString(separator))
            val type: String = artifactDefinition.type
                    ?: throw BluePrintException("type is missing for artifact definition :" + artifactDefinitionName)
            // Check Artifact Type
            checkValidArtifactType(artifactDefinitionName, type)

            val file: String = artifactDefinition.file
                    ?: throw BluePrintException(format("file is missing for artifact definition : {}", artifactDefinitionName))

            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateWorkFlows(workflows: MutableMap<String, Workflow>) {
        paths.add("workflows")
        workflows.forEach { workflowName, workflow ->

            // Validate Single workflow
            validateWorkFlow(workflowName, workflow)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateWorkFlow(workflowName: String, workflow: Workflow) {
        paths.add(workflowName)
        message.appendln("---> Workflow :" + paths.joinToString(separator))
        // Step Validation Start
        paths.add("steps")
        workflow.steps?.forEach { stepName, step ->
            paths.add(stepName)
            message.appendln("----> Steps :" + paths.joinToString(separator))
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
        // Step Validation Ends
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validatePropertyDefinitions(properties: MutableMap<String, PropertyDefinition>) {
        paths.add("properties")
        properties.forEach { propertyName, propertyDefinition ->
            paths.add(propertyName)
            val dataType: String = propertyDefinition.type
            when {
                BluePrintTypes.validPrimitiveTypes().contains(dataType) -> {
                    // Do Nothing
                }
                BluePrintTypes.validCollectionTypes().contains(dataType) -> {
                    val entrySchemaType: String = propertyDefinition.entrySchema?.type
                            ?: throw BluePrintException(format("Entry schema for data type ({}) for the property ({}) not found", dataType, propertyName))
                    checkPrimitiveOrComplex(entrySchemaType, propertyName)
                }
                else -> checkPropertyDataType(dataType, propertyName)
            }
            message.appendln("property " + paths.joinToString(separator) + " of type " + dataType)
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validatePropertyAssignments(nodeTypeProperties: MutableMap<String, PropertyDefinition>,
                                         properties: MutableMap<String, JsonNode>) {
        properties.forEach { propertyName, propertyAssignment ->
            val propertyDefinition: PropertyDefinition = nodeTypeProperties[propertyName]
                    ?: throw BluePrintException(format("failed to get definition for the property ({})", propertyName))

            validatePropertyAssignment(propertyName, propertyDefinition, propertyAssignment)

        }
    }

    @Throws(BluePrintException::class)
    open fun validatePropertyAssignment(propertyName: String, propertyDefinition: PropertyDefinition,
                                        propertyAssignment: JsonNode) {
        // Check and Validate if Expression Node
        val expressionData = BluePrintExpressionService.getExpressionData(propertyAssignment)
        if (!expressionData.isExpression) {
            checkPropertyValue(propertyName, propertyDefinition, propertyAssignment)
        }
    }

    @Throws(BluePrintException::class)
    open fun validateCapabilityAssignments(capabilities: MutableMap<String, CapabilityAssignment>) {

    }

    @Throws(BluePrintException::class)
    open fun validateRequirementAssignments(requirements: MutableMap<String, RequirementAssignment>) {

    }

    @Throws(BluePrintException::class)
    open fun validateInterfaceAssignments(interfaces: MutableMap<String, InterfaceAssignment>) {

    }

    @Throws(BluePrintException::class)
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

    @Throws(BluePrintException::class)
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

    @Throws(BluePrintException::class)
    open fun validateImplementation(implementation: Implementation) {
        checkNotEmptyNThrow(implementation.primary)
    }

    @Throws(BluePrintException::class)
    open fun checkValidArtifactType(artifactDefinitionName: String, artifactTypeName: String) {

        val artifactType = serviceTemplate.artifactTypes?.get(artifactTypeName)
                ?: throw BluePrintException(format("Failed to artifact type for artifact definition : {}", artifactDefinitionName))

        checkValidArtifactTypeDerivedFrom(artifactTypeName, artifactType.derivedFrom)
    }

    @Throws(BluePrintException::class)
    open fun checkValidArtifactTypeDerivedFrom(artifactTypeName: String, derivedFrom: String) {
        check(BluePrintTypes.validArtifactTypeDerivedFroms.contains(derivedFrom)) {
            throw BluePrintException(format("Failed to get artifact type ({})'s  derived from({}) definition ", artifactTypeName, derivedFrom))
        }
    }

    @Throws(BluePrintException::class)
    open fun checkValidDataTypeDerivedFrom(dataTypeName: String, derivedFrom: String) {
        check(BluePrintTypes.validDataTypeDerivedFroms.contains(derivedFrom)) {
            throw BluePrintException(format("Failed to get data type ({})'s  derived from({}) definition ", dataTypeName, derivedFrom))
        }
    }

    open fun checkPropertyValue(propertyName: String, propertyDefinition: PropertyDefinition, propertyAssignment: JsonNode) {
        val propertyType = propertyDefinition.type
        val isValid: Boolean

        if (BluePrintTypes.validPrimitiveTypes().contains(propertyType)) {
            isValid = JacksonUtils.checkJsonNodeValueOfPrimitiveType(propertyType, propertyAssignment)

        } else if (BluePrintTypes.validCollectionTypes().contains(propertyType)) {

            isValid = JacksonUtils.checkJsonNodeValueOfCollectionType(propertyType, propertyAssignment)
            val entrySchemaType = propertyDefinition.entrySchema?.type
                    ?: throw BluePrintException(format("Failed to get Entry Schema type for the collection property ({})", propertyName))

            if (!BluePrintTypes.validPropertyTypes().contains(entrySchemaType)) {
                checkPropertyDataType(entrySchemaType, propertyName)
            }

        } else {
            checkPropertyDataType(propertyType, propertyName)
            isValid = true
        }

        check(isValid) {
            throw BluePrintException(format("property({}) defined of type({}) is not compatable with the value ({})",
                    propertyName, propertyType, propertyAssignment))
        }
    }

    private fun checkPropertyDataType(dataType: String, propertyName: String) {

        val dataType = serviceTemplate.dataTypes?.get(dataType)
                ?: throw BluePrintException(format("Data type ({}) for the property ({}) not found", dataType, propertyName))

        checkValidDataTypeDerivedFrom(propertyName, dataType.derivedFrom)

    }

    private fun checkPrimitiveOrComplex(dataType: String, propertyName: String): Boolean {
        if (BluePrintTypes.validPrimitiveTypes().contains(dataType) || checkDataType(dataType)) {
            return true
        } else {
            throw BluePrintException(format("Data type ({}) for the property ({}) is not valid", dataType))
        }
    }

    private fun checkDataType(key: String): Boolean {
        return serviceTemplate.dataTypes?.containsKey(key) ?: false
    }

}
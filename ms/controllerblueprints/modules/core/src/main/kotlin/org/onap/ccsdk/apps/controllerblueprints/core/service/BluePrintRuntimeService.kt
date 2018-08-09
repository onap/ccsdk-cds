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
import com.fasterxml.jackson.databind.node.NullNode
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.OrchestratorException
import org.onap.ccsdk.apps.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.apps.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 *
 *
 * @author Brinda Santh
 */
class BluePrintRuntimeService(var bluePrintContext: BluePrintContext, var context: MutableMap<String, Any> = hashMapOf()) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.toString())

    /*
        Get the Node Type Definition for the Node Template, Then iterate Node Type Properties and resolve the expressing
     */
    fun resolveNodeTemplateProperties(nodeTemplateName: String): MutableMap<String, Any?> {
        logger.info("resolveNodeTemplatePropertyValues for node template ({})", nodeTemplateName)
        val propertyAssignmentValue: MutableMap<String, Any?> = hashMapOf()

        val nodeTemplate: NodeTemplate = bluePrintContext.nodeTemplateByName(nodeTemplateName)

        val propertyAssignments: MutableMap<String, Any?> =
                nodeTemplate.properties as MutableMap<String, Any?>

        // Get the Node Type Definitions
        val nodeTypeProperties: MutableMap<String, PropertyDefinition> =
                bluePrintContext.nodeTypeChainedProperties(nodeTemplate.type)!!

        // Iterate Node Type Properties
        nodeTypeProperties.forEach { nodeTypePropertyName, nodeTypeProperty ->
            // Get the Express or Value for the Node Template
            val propertyAssignment: Any? = propertyAssignments[nodeTypePropertyName]

            var resolvedValue: JsonNode = NullNode.getInstance()
            if (propertyAssignment != null) {
                // Resolve the Expressing
                val propertyAssignmentExpression = PropertyAssignmentService(context, this)
                resolvedValue = propertyAssignmentExpression.resolveAssignmentExpression(nodeTemplateName, nodeTypePropertyName, propertyAssignment)
            } else {
                // Assign default value to the Operation
                nodeTypeProperty.defaultValue?.let {
                    resolvedValue = JacksonUtils.jsonNodeFromObject(nodeTypeProperty.defaultValue!!)
                }
            }
            // Set for Return of method
            propertyAssignmentValue[nodeTypePropertyName] = resolvedValue
        }
        logger.info("resolved property definition for node template ({}), values ({})", nodeTemplateName, propertyAssignmentValue)
        return propertyAssignmentValue
    }

    fun resolveNodeTemplateInterfaceOperationInputs(nodeTemplateName: String,
                                                    interfaceName: String, operationName: String): MutableMap<String, Any?> {
        logger.info("nodeTemplateInterfaceOperationInputsResolvedExpression for node template ({}),interface name ({}), " +
                "operationName({})", nodeTemplateName, interfaceName, operationName)

        val propertyAssignmentValue: MutableMap<String, Any?> = hashMapOf()

        val propertyAssignments: MutableMap<String, Any> =
                bluePrintContext.nodeTemplateInterfaceOperationInputs(nodeTemplateName, interfaceName, operationName) as? MutableMap<String, Any>
                        ?: throw BluePrintException(String.format("failed to get input definitions for node template (%s), " +
                                "interface name (%s), operationName(%s)", nodeTemplateName, interfaceName, operationName))

        val nodeTypeName = bluePrintContext.nodeTemplateByName(nodeTemplateName).type

        val nodeTypeInterfaceOperationInputs: MutableMap<String, PropertyDefinition> =
                bluePrintContext.nodeTypeInterfaceOperationInputs(nodeTypeName, interfaceName, operationName)
                        ?: throw BluePrintException(String.format("failed to get input definitions for node type (%s), " +
                                "interface name (%s), operationName(%s)", nodeTypeName, interfaceName, operationName))

        logger.info("input definition for node template ({}), values ({})", nodeTemplateName, propertyAssignments)

        // Iterate Node Type Properties
        nodeTypeInterfaceOperationInputs.forEach { nodeTypePropertyName, nodeTypeProperty ->
            // Get the Express or Value for the Node Template
            val propertyAssignment: Any? = propertyAssignments[nodeTypePropertyName]

            var resolvedValue: JsonNode = NullNode.getInstance()
            if (propertyAssignment != null) {
                // Resolve the Expressing
                val propertyAssignmentExpression = PropertyAssignmentService( context, this)
                resolvedValue = propertyAssignmentExpression.resolveAssignmentExpression(nodeTemplateName, nodeTypePropertyName, propertyAssignment)
            } else {
                // Assign default value to the Operation
                nodeTypeProperty.defaultValue?.let {
                    resolvedValue = JacksonUtils.jsonNodeFromObject(nodeTypeProperty.defaultValue!!)
                }
            }
            // Set for Return of method
            propertyAssignmentValue[nodeTypePropertyName] = resolvedValue
        }
        logger.info("resolved input assignments for node template ({}), values ({})", nodeTemplateName, propertyAssignmentValue)

        return propertyAssignmentValue
    }


    fun resolveNodeTemplateInterfaceOperationOutputs(nodeTemplateName: String,
                                                     interfaceName: String, operationName: String, componentContext: MutableMap<String, Any?>): Unit {
        logger.info("nodeTemplateInterfaceOperationInputsResolvedExpression for node template ({}),interface name ({}), " +
                "operationName({})", nodeTemplateName, interfaceName, operationName)

        val nodeTypeName = bluePrintContext.nodeTemplateByName(nodeTemplateName).type

        val nodeTypeInterfaceOperationOutputs: MutableMap<String, PropertyDefinition> =
                bluePrintContext.nodeTypeInterfaceOperationOutputs(nodeTypeName, interfaceName, operationName)
                        ?: throw BluePrintException(String.format("failed to get input definitions for node type (%s), " +
                                "interface name (%s), operationName(%s)", nodeTypeName, interfaceName, operationName))

        // Iterate Node Type Properties
        nodeTypeInterfaceOperationOutputs.forEach { nodeTypePropertyName, nodeTypeProperty ->

            val operationOutputPropertyName: String = StringBuilder().append(nodeTemplateName)
                    .append(".").append(interfaceName)
                    .append(".").append(operationName)
                    .append(".").append(nodeTypePropertyName).toString()
            // Get the Value from component context
            val resolvedValue: JsonNode = componentContext[operationOutputPropertyName] as? JsonNode
                    ?: NullNode.getInstance()
            // Store  operation output values into context
            setNodeTemplateOperationPropertyValue(nodeTemplateName, interfaceName, operationName, nodeTypePropertyName, resolvedValue)
            logger.debug("resolved output assignments for node template ({}), property name ({}), value ({})", nodeTemplateName, nodeTypePropertyName, resolvedValue)
        }
    }

    fun resolveNodeTemplateArtifact(nodeTemplateName: String,
                                    artifactName: String): String {
        val nodeTemplate = bluePrintContext.nodeTemplateByName(nodeTemplateName)

        val artifactDefinition: ArtifactDefinition = nodeTemplate.artifacts?.get(artifactName)
                ?: throw OrchestratorException(String.format("failed to get artifat definition {} from the node template"
                        , artifactName))
        val propertyAssignmentExpression = PropertyAssignmentService( context, this)
        return propertyAssignmentExpression.artifactContent(artifactDefinition)
    }


    fun setInputValue(propertyName: String, propertyDefinition: PropertyDefinition, value: JsonNode): Unit {
        val path = StringBuilder(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_INPUTS)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        logger.trace("setting input path ({}), values ({})", path, value)
        context[path] = value
    }

    fun setWorkflowInputValue(workflowName: String, propertyName: String, value: JsonNode): Unit {
        val path: String = StringBuilder(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_NODE_WORKFLOWS).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(workflowName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_INPUTS)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_PROPERTIES)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        context[path] = value
    }

    fun setNodeTemplatePropertyValue(nodeTemplateName: String, propertyName: String, value: JsonNode): Unit {

        val path: String = StringBuilder(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_NODE_TEMPLATES).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_PROPERTIES)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        context[path] = value
    }

    fun setNodeTemplateOperationPropertyValue(nodeTemplateName: String, interfaceName: String, operationName: String, propertyName: String,
                                              value: JsonNode): Unit {
        val path: String = StringBuilder(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_NODE_TEMPLATES).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_INTERFACES).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(interfaceName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_OPERATIONS).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(operationName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_PROPERTIES)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        logger.trace("setting operation property path ({}), values ({})", path, value)
        context[path] = value
    }

    fun setNodeTemplateOperationInputValue(nodeTemplateName: String, interfaceName: String, operationName: String, propertyName: String,
                                           value: JsonNode): Unit {
        val path: String = StringBuilder(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_NODE_TEMPLATES).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_INTERFACES).append(interfaceName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_OPERATIONS).append(operationName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_INPUTS)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_PROPERTIES)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        context[path] = value
    }

    fun setNodeTemplateOperationOutputValue(nodeTemplateName: String, interfaceName: String, operationName: String, propertyName: String,
                                            value: JsonNode): Unit {
        val path: String = StringBuilder(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_NODE_TEMPLATES).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_INTERFACES).append(interfaceName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_OPERATIONS).append(operationName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_OUTPUTS)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_PROPERTIES)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        context[path] = value
    }


    fun getInputValue(propertyName: String): JsonNode {
        val path = StringBuilder(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_INPUTS)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        return context[path] as? JsonNode ?: NullNode.instance
    }

    fun getNodeTemplateOperationOutputValue(nodeTemplateName: String, interfaceName: String, operationName: String, propertyName: String): JsonNode {
        val path: String = StringBuilder(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_NODE_TEMPLATES).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_INTERFACES).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(interfaceName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_OPERATIONS).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(operationName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_PROPERTIES)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        return context[path] as JsonNode
    }

    fun getPropertyValue(nodeTemplateName: String, propertyName: String): JsonNode? {
        val path: String = StringBuilder(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_NODE_TEMPLATES).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_PROPERTIES)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        return context[path] as JsonNode
    }

    fun getRequirementPropertyValue(nodeTemplateName: String, requirementName: String, propertyName: String): JsonNode? {
        val path: String = StringBuilder(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_NODE_TEMPLATES).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_REQUIREMENTS).append(requirementName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_PROPERTIES)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        return context[path] as JsonNode
    }

    fun getCapabilityPropertyValue(nodeTemplateName: String, capabilityName: String, propertyName: String): JsonNode? {
        val path: String = StringBuilder(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_NODE_TEMPLATES).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_CAPABILITIES).append(capabilityName)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_PROPERTIES)
                .append(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        return context[path] as JsonNode
    }

    fun assignInputs(jsonNode: JsonNode): Unit {
        logger.info("assignInputs from input JSON ({})", jsonNode.toString())
        bluePrintContext.inputs?.forEach { propertyName, property ->
            val valueNode: JsonNode = jsonNode.at("/" + propertyName) ?: NullNode.getInstance()
            setInputValue(propertyName, property, valueNode)
        }
    }

    fun assignWorkflowInputs(workflowName: String, jsonNode: JsonNode): Unit {
        logger.info("assign workflow {} input value ({})", workflowName, jsonNode.toString())
        bluePrintContext.workflowByName(workflowName)?.inputs?.forEach { propertyName, property ->
            val valueNode: JsonNode = jsonNode.at("/" + propertyName) ?: NullNode.getInstance()
            setWorkflowInputValue(workflowName, propertyName, valueNode)
        }
    }
}
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

package org.onap.ccsdk.apps.controllerblueprints.core.service


import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.apps.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 *
 *
 * @author Brinda Santh
 */
open class BluePrintRuntimeService(var bluePrintContext: BluePrintContext, var context: MutableMap<String, Any> = hashMapOf()) {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintRuntimeService::class.toString())

    /*
        Get the Node Type Definition for the Node Template, Then iterate Node Type Properties and resolve the expressing
     */
    open fun resolveNodeTemplateProperties(nodeTemplateName: String): MutableMap<String, Any?> {
        log.info("resolveNodeTemplatePropertyValues for node template ({})", nodeTemplateName)
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
                nodeTypeProperty.defaultValue?.let { defaultValue ->
                    resolvedValue = defaultValue
                }
            }
            // Set for Return of method
            propertyAssignmentValue[nodeTypePropertyName] = resolvedValue
        }
        log.info("resolved property definition for node template ({}), values ({})", nodeTemplateName, propertyAssignmentValue)
        return propertyAssignmentValue
    }

    open fun resolveNodeTemplateInterfaceOperationInputs(nodeTemplateName: String,
                                                         interfaceName: String, operationName: String): MutableMap<String, Any?> {
        log.info("resolveNodeTemplateInterfaceOperationInputs for node template ({}),interface name ({}), " +
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

        log.info("input definition for node template ({}), values ({})", nodeTemplateName, propertyAssignments)

        // Iterate Node Type Properties
        nodeTypeInterfaceOperationInputs.forEach { nodeTypePropertyName, nodeTypeProperty ->
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
        log.trace("resolved input assignments for node template ({}), values ({})", nodeTemplateName,
                propertyAssignmentValue)

        return propertyAssignmentValue
    }


    open fun resolveNodeTemplateInterfaceOperationOutputs(nodeTemplateName: String,
                                                          interfaceName: String, operationName: String): MutableMap<String, Any?>  {
        log.info("resolveNodeTemplateInterfaceOperationOutputs for node template ({}),interface name ({}), " +
                "operationName({})", nodeTemplateName, interfaceName, operationName)

        val propertyAssignmentValue: MutableMap<String, Any?> = hashMapOf()

        val propertyAssignments: MutableMap<String, Any> =
                bluePrintContext.nodeTemplateInterfaceOperationOutputs(nodeTemplateName, interfaceName, operationName) as? MutableMap<String, Any>
                        ?: throw BluePrintException(String.format("failed to get output definitions for node template (%s), " +
                                "interface name (%s), operationName(%s)", nodeTemplateName, interfaceName, operationName))

        val nodeTypeName = bluePrintContext.nodeTemplateByName(nodeTemplateName).type

        val nodeTypeInterfaceOperationOutputs: MutableMap<String, PropertyDefinition> =
                bluePrintContext.nodeTypeInterfaceOperationOutputs(nodeTypeName, interfaceName, operationName)
                        ?: throw BluePrintException(String.format("failed to get input definitions for node type (%s), " +
                                "interface name (%s), operationName(%s)", nodeTypeName, interfaceName, operationName))

        // Iterate Node Type Properties
        nodeTypeInterfaceOperationOutputs.forEach { nodeTypePropertyName, nodeTypeProperty ->

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

            // Store  operation output values into context
            setNodeTemplateOperationOutputValue(nodeTemplateName, interfaceName, operationName, nodeTypePropertyName, resolvedValue)
            log.trace("resolved output assignments for node template ({}), property name ({}), value ({})", nodeTemplateName, nodeTypePropertyName, resolvedValue)
        }
        return propertyAssignmentValue
    }

    open fun resolveNodeTemplateArtifact(nodeTemplateName: String,
                                         artifactName: String): String {
        val nodeTemplate = bluePrintContext.nodeTemplateByName(nodeTemplateName)

        val artifactDefinition: ArtifactDefinition = nodeTemplate.artifacts?.get(artifactName)
                ?: throw BluePrintProcessorException(String.format("failed to get artifat definition {} from the node template"
                        , artifactName))
        val propertyAssignmentExpression = PropertyAssignmentService(context, this)
        return propertyAssignmentExpression.artifactContent(artifactDefinition)
    }


    open fun setInputValue(propertyName: String, propertyDefinition: PropertyDefinition, value: JsonNode) {
        val path = StringBuilder(BluePrintConstants.PATH_INPUTS)
                .append(BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        log.trace("setting input path ({}), values ({})", path, value)
        context[path] = value
    }

    open fun setWorkflowInputValue(workflowName: String, propertyName: String, value: JsonNode) {
        val path: String = StringBuilder(BluePrintConstants.PATH_NODE_WORKFLOWS).append(BluePrintConstants.PATH_DIVIDER).append(workflowName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_INPUTS)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_PROPERTIES)
                .append(BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        context[path] = value
    }

    open fun setNodeTemplatePropertyValue(nodeTemplateName: String, propertyName: String, value: JsonNode) {

        val path: String = StringBuilder(BluePrintConstants.PATH_NODE_TEMPLATES).append(BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_PROPERTIES)
                .append(BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        context[path] = value
    }

    open fun setNodeTemplateAttributeValue(nodeTemplateName: String, attributeName: String, value: JsonNode) {

        val path: String = StringBuilder(BluePrintConstants.PATH_NODE_TEMPLATES).append(BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_ATTRIBUTES)
                .append(BluePrintConstants.PATH_DIVIDER).append(attributeName).toString()
        context[path] = value
    }

    open fun setNodeTemplateOperationPropertyValue(nodeTemplateName: String, interfaceName: String, operationName: String, propertyName: String,
                                                   value: JsonNode) {
        val path: String = StringBuilder(BluePrintConstants.PATH_NODE_TEMPLATES).append(BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_INTERFACES).append(BluePrintConstants.PATH_DIVIDER).append(interfaceName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_OPERATIONS).append(BluePrintConstants.PATH_DIVIDER).append(operationName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_PROPERTIES)
                .append(BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        log.trace("setting operation property path ({}), values ({})", path, value)
        context[path] = value
    }

    open fun setNodeTemplateOperationInputValue(nodeTemplateName: String, interfaceName: String, operationName: String, propertyName: String,
                                                value: JsonNode) {
        val path: String = StringBuilder(BluePrintConstants.PATH_NODE_TEMPLATES).append(BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_INTERFACES).append(BluePrintConstants.PATH_DIVIDER).append(interfaceName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_OPERATIONS).append(BluePrintConstants.PATH_DIVIDER).append(operationName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_INPUTS)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_PROPERTIES)
                .append(BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        context[path] = value
    }

    open fun setNodeTemplateOperationOutputValue(nodeTemplateName: String, interfaceName: String, operationName: String, propertyName: String,
                                                 value: JsonNode) {
        val path: String = StringBuilder(BluePrintConstants.PATH_NODE_TEMPLATES).append(BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_INTERFACES).append(BluePrintConstants.PATH_DIVIDER).append(interfaceName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_OPERATIONS).append(BluePrintConstants.PATH_DIVIDER).append(operationName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_OUTPUTS)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_PROPERTIES)
                .append(BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        context[path] = value
    }


    open fun getInputValue(propertyName: String): JsonNode {
        val path = StringBuilder(BluePrintConstants.PATH_INPUTS)
                .append(BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        return context[path] as? JsonNode ?: NullNode.instance
    }

    open fun getNodeTemplateOperationOutputValue(nodeTemplateName: String, interfaceName: String, operationName: String, propertyName: String): JsonNode {
        val path: String = StringBuilder(BluePrintConstants.PATH_NODE_TEMPLATES).append(BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_INTERFACES).append(BluePrintConstants.PATH_DIVIDER).append(interfaceName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_OPERATIONS).append(BluePrintConstants.PATH_DIVIDER).append(operationName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_OUTPUTS).append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_PROPERTIES)
                .append(BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        return context[path] as JsonNode
    }

    open fun getNodeTemplatePropertyValue(nodeTemplateName: String, propertyName: String): JsonNode? {
        val path: String = StringBuilder(BluePrintConstants.PATH_NODE_TEMPLATES).append(BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_PROPERTIES)
                .append(BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        return context[path] as JsonNode
    }

    open fun getNodeTemplateAttributeValue(nodeTemplateName: String, attributeName: String): JsonNode? {
        val path: String = StringBuilder(BluePrintConstants.PATH_NODE_TEMPLATES).append(BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_ATTRIBUTES)
                .append(BluePrintConstants.PATH_DIVIDER).append(attributeName).toString()
        return context[path] as JsonNode
    }

    open fun getNodeTemplateRequirementPropertyValue(nodeTemplateName: String, requirementName: String, propertyName:
    String): JsonNode? {
        val path: String = StringBuilder(BluePrintConstants.PATH_NODE_TEMPLATES).append(BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_REQUIREMENTS).append(requirementName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_PROPERTIES)
                .append(BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        return context[path] as JsonNode
    }

    open fun getNodeTemplateCapabilityPropertyValue(nodeTemplateName: String, capabilityName: String, propertyName:
    String): JsonNode? {
        val path: String = StringBuilder(BluePrintConstants.PATH_NODE_TEMPLATES).append(BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_CAPABILITIES).append(capabilityName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_PROPERTIES)
                .append(BluePrintConstants.PATH_DIVIDER).append(propertyName).toString()
        return context[path] as JsonNode
    }

    open fun assignInputs(jsonNode: JsonNode) {
        log.info("assignInputs from input JSON ({})", jsonNode.toString())
        bluePrintContext.inputs?.forEach { propertyName, property ->
            val valueNode: JsonNode = jsonNode.at(BluePrintConstants.PATH_DIVIDER + propertyName)
                    ?: NullNode.getInstance()
            setInputValue(propertyName, property, valueNode)
        }
    }

    open fun assignWorkflowInputs(workflowName: String, jsonNode: JsonNode) {
        log.info("assign workflow {} input value ({})", workflowName, jsonNode.toString())
        bluePrintContext.workflowByName(workflowName)?.inputs?.forEach { propertyName, _ ->
            val valueNode: JsonNode = jsonNode.at(BluePrintConstants.PATH_DIVIDER + propertyName)
                    ?: NullNode.getInstance()
            setWorkflowInputValue(workflowName, propertyName, valueNode)
        }
    }

    open fun getJsonForNodeTemplateAttributeProperties(nodeTemplateName: String, keys: List<String>): JsonNode {

        val jsonNode: ObjectNode = jacksonObjectMapper().createObjectNode()
        val path: String = StringBuilder(BluePrintConstants.PATH_NODE_TEMPLATES).append(BluePrintConstants.PATH_DIVIDER).append(nodeTemplateName)
                .append(BluePrintConstants.PATH_DIVIDER).append(BluePrintConstants.PATH_ATTRIBUTES)
                .append(BluePrintConstants.PATH_DIVIDER).toString()
        context.keys.filter {
            it.startsWith(path)
        }.map {
            val key = it.replace(path, "")
            if (keys.contains(key)) {
                val value = context[it] as JsonNode
                jsonNode.set(key, value)
            }
        }
        return jsonNode
    }


}
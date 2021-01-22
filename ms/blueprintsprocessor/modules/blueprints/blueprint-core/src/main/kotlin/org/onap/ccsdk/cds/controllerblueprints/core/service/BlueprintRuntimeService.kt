/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018-2019 IBM, Bell Canada.
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
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintError
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.common.ApplicationConstants.LOG_REDACTED
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.returnNullIfMissing
import org.onap.ccsdk.cds.controllerblueprints.core.rootFieldsToMap
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.PropertyDefinitionUtils.Companion.hasLogProtect
import org.slf4j.LoggerFactory
import java.io.File

interface BlueprintRuntimeService<T> {

    fun id(): String

    fun bluePrintContext(): BlueprintContext

    fun getExecutionContext(): T

    fun setExecutionContext(executionContext: T)

    fun put(key: String, value: JsonNode)

    fun get(key: String): JsonNode?

    fun check(key: String): Boolean

    fun cleanRuntime()

    fun getAsString(key: String): String?

    fun getAsBoolean(key: String): Boolean?

    fun getAsInt(key: String): Int?

    fun getAsDouble(key: String): Double?

    fun getBlueprintError(): BlueprintError

    fun setBlueprintError(bluePrintError: BlueprintError)

    fun loadEnvironments(type: String, fileName: String)

    fun resolveWorkflowOutputs(workflowName: String): MutableMap<String, JsonNode>

    fun resolveDSLExpression(dslPropertyName: String): JsonNode

    /** Resolve Property Definition [definitionName] for type [definitionType] with [propertyDefinitions]
     * Definition Type may be : node_template, relationship_template, dsl, workflow
     * Assumption is Definition holds the expressions or value assigned in it. Mainly used for workflow outputs.
     */
    fun resolvePropertyDefinitions(
        definitionType: String,
        definitionName: String,
        propertyDefinitions: MutableMap<String, PropertyDefinition>
    ): MutableMap<String, JsonNode>

    /** Resolve Property Assignments [definitionName] for type [definitionType] with [propertyDefinitions]
     * and [propertyAssignments]
     * Definition Type may be : node_template, relationship_template, dsl, workflow
     */
    fun resolvePropertyAssignments(
        definitionType: String,
        definitionName: String,
        propertyDefinitions: MutableMap<String, PropertyDefinition>,
        propertyAssignments: MutableMap<String, JsonNode>
    ): MutableMap<String, JsonNode>

    /** Resolve Property Assignments [definitionName] for type [definitionType] with  [propertyAssignments]
     *  Definition Type may be : node_template, relationship_template, dsl, workflow
     */
    fun resolvePropertyAssignments(
        definitionType: String,
        definitionName: String,
        propertyAssignments: MutableMap<String, JsonNode>
    ): MutableMap<String, JsonNode>

    /** Resolve Node Template [nodeTemplateName] Property Assignments */
    fun resolveNodeTemplateProperties(nodeTemplateName: String): MutableMap<String, JsonNode>

    /** Resolve Node Template [nodeTemplateName] Property Assignments with [propertyDefinitions] and  [propertyAssignments]*/
    fun resolveNodeTemplatePropertyAssignments(
        nodeTemplateName: String,
        propertyDefinitions: MutableMap<String, PropertyDefinition>,
        propertyAssignments: MutableMap<String, JsonNode>
    ): MutableMap<String, JsonNode>

    fun resolveNodeTemplateCapabilityProperties(nodeTemplateName: String, capabilityName: String): MutableMap<String,
        JsonNode>

    fun resolveNodeTemplateInterfaceOperationInputs(
        nodeTemplateName: String,
        interfaceName: String,
        operationName: String
    ): MutableMap<String, JsonNode>

    fun resolveNodeTemplateInterfaceOperationOutputs(
        nodeTemplateName: String,
        interfaceName: String,
        operationName: String
    ): MutableMap<String, JsonNode>

    suspend fun resolveNodeTemplateArtifact(nodeTemplateName: String, artifactName: String): String

    fun resolveNodeTemplateArtifactDefinition(nodeTemplateName: String, artifactName: String): ArtifactDefinition

    /** Resolve Node Template [relationshipTemplateName] Property Assignments */
    fun resolveRelationshipTemplateProperties(relationshipTemplateName: String): MutableMap<String, JsonNode>

    /** Resolve Relationship Template [relationshipTemplateName] Property Assignments with
     * [propertyDefinitions] and  [propertyAssignments] */
    fun resolveRelationshipTemplatePropertyAssignments(
        relationshipTemplateName: String,
        propertyDefinitions: MutableMap<String, PropertyDefinition>,
        propertyAssignments: MutableMap<String, JsonNode>
    ): MutableMap<String, JsonNode>

    fun setInputValue(propertyName: String, value: JsonNode)

    fun setWorkflowInputValue(
        workflowName: String,
        propertyName: String,
        propertyDefinition: PropertyDefinition,
        value: JsonNode
    )

    fun setNodeTemplatePropertyValue(nodeTemplateName: String, propertyName: String, value: JsonNode)

    fun setNodeTemplateAttributeValue(nodeTemplateName: String, attributeName: String, value: JsonNode)

    fun setNodeTemplateOperationPropertyValue(
        nodeTemplateName: String,
        interfaceName: String,
        operationName: String,
        propertyName: String,
        value: JsonNode
    )

    fun setNodeTemplateOperationInputValue(
        nodeTemplateName: String,
        interfaceName: String,
        operationName: String,
        propertyName: String,
        value: JsonNode
    )

    fun setNodeTemplateOperationOutputValue(
        nodeTemplateName: String,
        interfaceName: String,
        operationName: String,
        propertyName: String,
        value: JsonNode
    )

    fun getInputValue(propertyName: String): JsonNode

    fun getNodeTemplateOperationOutputValue(
        nodeTemplateName: String,
        interfaceName: String,
        operationName: String,
        propertyName: String
    ): JsonNode

    fun getNodeTemplatePropertyValue(nodeTemplateName: String, propertyName: String): JsonNode?

    fun getNodeTemplateAttributeValue(nodeTemplateName: String, attributeName: String): JsonNode?

    fun getRelationshipTemplatePropertyValue(relationshipTemplateName: String, propertyName: String): JsonNode?

    fun getRelationshipTemplateAttributeValue(relationshipTemplateName: String, attributeName: String): JsonNode?

    fun assignInputs(jsonNode: JsonNode)

    fun assignWorkflowInputs(workflowName: String, jsonNode: JsonNode)

    fun getJsonForNodeTemplateAttributeProperties(nodeTemplateName: String, keys: List<String>): JsonNode

    suspend fun close()
}

/**
 *
 *
 * @author Brinda Santh
 */
open class DefaultBlueprintRuntimeService(private var id: String, private var bluePrintContext: BlueprintContext) :
    BlueprintRuntimeService<MutableMap<String, JsonNode>> {

    @Transient
    private val log = LoggerFactory.getLogger(BlueprintRuntimeService::class.toString())

    private var store: MutableMap<String, JsonNode> = hashMapOf()

    private var bluePrintError = BlueprintError()

    init {
        /**
         * Load Blueprint Environments Properties
         */
        val absoluteEnvFilePath = bluePrintContext.rootPath.plus(File.separator)
            .plus(BlueprintConstants.TOSCA_ENVIRONMENTS_DIR)
        loadEnvironments(BlueprintConstants.PROPERTY_BPP, absoluteEnvFilePath)
    }

    override fun id(): String {
        return id
    }

    override fun bluePrintContext(): BlueprintContext {
        return bluePrintContext
    }

    override fun getExecutionContext(): MutableMap<String, JsonNode> {
        return store
    }

    @Suppress("UNCHECKED_CAST")
    override fun setExecutionContext(executionContext: MutableMap<String, JsonNode>) {
        this.store = executionContext
    }

    override fun put(key: String, value: JsonNode) {
        store[key] = value
    }

    override fun get(key: String): JsonNode {
        return store[key] ?: throw BlueprintProcessorException("failed to get execution property($key)")
    }

    override fun check(key: String): Boolean {
        return store.containsKey(key)
    }

    override fun cleanRuntime() {
        store.clear()
    }

    private fun getJsonNode(key: String): JsonNode {
        return get(key)
    }

    override fun getAsString(key: String): String? {
        return get(key).asText()
    }

    override fun getAsBoolean(key: String): Boolean? {
        return get(key).asBoolean()
    }

    override fun getAsInt(key: String): Int? {
        return get(key).asInt()
    }

    override fun getAsDouble(key: String): Double? {
        return get(key).asDouble()
    }

    override fun getBlueprintError(): BlueprintError {
        return this.bluePrintError
    }

    override fun setBlueprintError(bluePrintError: BlueprintError) {
        this.bluePrintError = bluePrintError
    }

    override fun loadEnvironments(type: String, fileName: String) {
        BlueprintMetadataUtils.environmentFileProperties(fileName).forEach { key, value ->
            setNodeTemplateAttributeValue(type, key.toString(), value.asJsonType())
        }
    }

    override fun resolveWorkflowOutputs(workflowName: String): MutableMap<String, JsonNode> {
        log.info("resolveWorkflowOutputs for workflow($workflowName)")
        val outputs = bluePrintContext.workflowByName(workflowName).outputs ?: mutableMapOf()
        return resolvePropertyDefinitions(BlueprintConstants.MODEL_DEFINITION_TYPE_WORKFLOW, "WORKFLOW", outputs)
    }

    /**
     * Read the DSL Property reference, If there is any expression, then resolve those expression and return as Json
     * Type
     */
    override fun resolveDSLExpression(dslPropertyName: String): JsonNode {
        val propertyAssignments = bluePrintContext.dslPropertiesByName(dslPropertyName)
        return if (BlueprintExpressionService.checkContainsExpression(propertyAssignments) &&
            propertyAssignments is ObjectNode
        ) {
            val rootKeyMap = propertyAssignments.rootFieldsToMap()
            val propertyAssignmentValue: MutableMap<String, JsonNode> = hashMapOf()
            rootKeyMap.forEach { (propertyName, propertyValue) ->
                val propertyAssignmentExpression = PropertyAssignmentService(this)
                propertyAssignmentValue[propertyName] = propertyAssignmentExpression
                    .resolveAssignmentExpression(
                        BlueprintConstants.MODEL_DEFINITION_TYPE_DSL,
                        "DSL",
                        propertyName,
                        propertyValue
                    )
            }
            propertyAssignmentValue.asJsonNode()
        } else {
            propertyAssignments
        }
    }

    override fun resolvePropertyDefinitions(
        definitionType: String,
        definitionName: String,
        propertyDefinitions: MutableMap<String, PropertyDefinition>
    ): MutableMap<String, JsonNode> {
        val propertyAssignmentValue: MutableMap<String, JsonNode> = hashMapOf()

        propertyDefinitions.forEach { (propertyName, propertyDefinition) ->
            val propertyAssignmentExpression = PropertyAssignmentService(this)
            val expression = propertyDefinition.value ?: propertyDefinition.defaultValue
            if (expression != null) {
                propertyAssignmentValue[propertyName] =
                    propertyAssignmentExpression.resolveAssignmentExpression(
                        definitionType,
                        definitionName,
                        propertyName,
                        expression
                    )
            }
        }
        return propertyAssignmentValue
    }

    override fun resolvePropertyAssignments(
        definitionType: String,
        definitionName: String,
        propertyDefinitions: MutableMap<String, PropertyDefinition>,
        propertyAssignments: MutableMap<String, JsonNode>
    ): MutableMap<String, JsonNode> {

        val propertyAssignmentValue: MutableMap<String, JsonNode> = hashMapOf()

        propertyDefinitions.forEach { (nodeTypePropertyName, nodeTypeProperty) ->
            // Get the Express or Value for the Node Template
            val propertyAssignment: JsonNode? = propertyAssignments[nodeTypePropertyName]

            var resolvedValue: JsonNode = NullNode.getInstance()
            if (propertyAssignment != null) {
                // Resolve the Expressing
                val propertyAssignmentExpression = PropertyAssignmentService(this)
                resolvedValue = propertyAssignmentExpression.resolveAssignmentExpression(
                    definitionType, definitionName, nodeTypePropertyName, propertyAssignment
                )
            }

            // Set default value if null
            if (resolvedValue is NullNode) {
                nodeTypeProperty.defaultValue?.let { resolvedValue = nodeTypeProperty.defaultValue!! }
            }

            /** If property is Map type, then resolve the property value, It may have expressions */
            if (nodeTypeProperty.type == BlueprintConstants.DATA_TYPE_MAP &&
                resolvedValue.returnNullIfMissing() != null
            ) {
                val mapResolvedValue = resolvePropertyAssignments(
                    definitionType, definitionName, resolvedValue.rootFieldsToMap()
                )
                resolvedValue = mapResolvedValue.asJsonNode()
            }

            // Set for Return of method
            propertyAssignmentValue[nodeTypePropertyName] = resolvedValue
        }
        return propertyAssignmentValue
    }

    override fun resolvePropertyAssignments(
        definitionType: String,
        definitionName: String,
        propertyAssignments: MutableMap<String, JsonNode>
    ): MutableMap<String, JsonNode> {
        val propertyAssignmentValue: MutableMap<String, JsonNode> = hashMapOf()

        propertyAssignments.forEach { (propertyName, propertyExpression) ->
            val propertyAssignmentExpression = PropertyAssignmentService(this)
            propertyAssignmentValue[propertyName] =
                propertyAssignmentExpression.resolveAssignmentExpression(
                    definitionType, definitionName, propertyName, propertyExpression
                )
        }
        return propertyAssignmentValue
    }

    override fun resolveNodeTemplateProperties(nodeTemplateName: String): MutableMap<String, JsonNode> {
        log.info("resolveNodeTemplatePropertyValues for node template ({})", nodeTemplateName)

        val nodeTemplate: NodeTemplate = bluePrintContext.nodeTemplateByName(nodeTemplateName)

        val propertyAssignments: MutableMap<String, JsonNode> = nodeTemplate.properties!!

        // Get the Node Type Definitions
        val nodeTypePropertiesDefinitions: MutableMap<String, PropertyDefinition> = bluePrintContext
            .nodeTypeChainedProperties(nodeTemplate.type)!!

        /**
         * Resolve the NodeTemplate Property Assignment Values.
         */
        return resolveNodeTemplatePropertyAssignments(
            nodeTemplateName,
            nodeTypePropertiesDefinitions,
            propertyAssignments
        )
    }

    /**
     * Resolve any property assignments for the node
     */
    override fun resolveNodeTemplatePropertyAssignments(
        nodeTemplateName: String,
        propertyDefinitions: MutableMap<String, PropertyDefinition>,
        propertyAssignments: MutableMap<String, JsonNode>
    ): MutableMap<String, JsonNode> {
        return resolvePropertyAssignments(
            BlueprintConstants.MODEL_DEFINITION_TYPE_NODE_TEMPLATE,
            nodeTemplateName, propertyDefinitions, propertyAssignments
        )
    }

    override fun resolveNodeTemplateCapabilityProperties(nodeTemplateName: String, capabilityName: String):
        MutableMap<String, JsonNode> {
            log.info("resolveNodeTemplateCapabilityProperties for node template($nodeTemplateName) capability($capabilityName)")
            val nodeTemplate: NodeTemplate = bluePrintContext.nodeTemplateByName(nodeTemplateName)

            val propertyAssignments = nodeTemplate.capabilities?.get(capabilityName)?.properties ?: hashMapOf()

            val propertyDefinitions = bluePrintContext.nodeTemplateNodeType(nodeTemplateName)
                .capabilities?.get(capabilityName)?.properties ?: hashMapOf()

            /**
             * Resolve the Capability Property Assignment Values.
             */
            return resolveNodeTemplatePropertyAssignments(nodeTemplateName, propertyDefinitions, propertyAssignments)
        }

    override fun resolveNodeTemplateInterfaceOperationInputs(
        nodeTemplateName: String,
        interfaceName: String,
        operationName: String
    ): MutableMap<String, JsonNode> {
        log.info(
            "resolveNodeTemplateInterfaceOperationInputs for node template ($nodeTemplateName), " +
                "interface name($interfaceName), operationName($operationName)"
        )

        val propertyAssignments: MutableMap<String, JsonNode> =
            bluePrintContext.nodeTemplateInterfaceOperationInputs(nodeTemplateName, interfaceName, operationName)
                ?: hashMapOf()

        val nodeTypeName = bluePrintContext.nodeTemplateByName(nodeTemplateName).type

        val nodeTypeInterfaceOperationInputs: MutableMap<String, PropertyDefinition> =
            bluePrintContext.nodeTypeInterfaceOperationInputs(nodeTypeName, interfaceName, operationName)
                ?: hashMapOf()

        log.info("input definition for node template ($nodeTemplateName), values ($propertyAssignments)")

        /**
         * Resolve the Property Input Assignment Values.
         */
        return resolveNodeTemplatePropertyAssignments(
            nodeTemplateName,
            nodeTypeInterfaceOperationInputs,
            propertyAssignments
        )
    }

    override fun resolveNodeTemplateInterfaceOperationOutputs(
        nodeTemplateName: String,
        interfaceName: String,
        operationName: String
    ): MutableMap<String, JsonNode> {
        log.info(
            "resolveNodeTemplateInterfaceOperationOutputs for node template ($nodeTemplateName),interface name " +
                "($interfaceName), operationName($operationName)"
        )

        val propertyAssignments: MutableMap<String, JsonNode> =
            bluePrintContext.nodeTemplateInterfaceOperationOutputs(nodeTemplateName, interfaceName, operationName)
                ?: hashMapOf()

        val nodeTypeName = bluePrintContext.nodeTemplateByName(nodeTemplateName).type

        val nodeTypeInterfaceOperationOutputs: MutableMap<String, PropertyDefinition> =
            bluePrintContext.nodeTypeInterfaceOperationOutputs(nodeTypeName, interfaceName, operationName)
                ?: hashMapOf()

        /**
         * Resolve the Property Output Assignment Values.
         */
        val propertyAssignmentValue =
            resolveNodeTemplatePropertyAssignments(
                nodeTemplateName,
                nodeTypeInterfaceOperationOutputs,
                propertyAssignments
            )

        // Store  operation output values into context
        propertyAssignmentValue.forEach { (key, value) ->
            setNodeTemplateOperationOutputValue(nodeTemplateName, interfaceName, operationName, key, value)
        }
        return propertyAssignmentValue
    }

    override suspend fun resolveNodeTemplateArtifact(nodeTemplateName: String, artifactName: String): String {
        val artifactDefinition: ArtifactDefinition =
            resolveNodeTemplateArtifactDefinition(nodeTemplateName, artifactName)
        val propertyAssignmentExpression = PropertyAssignmentService(this)
        return propertyAssignmentExpression.artifactContent(artifactDefinition)
    }

    override fun resolveNodeTemplateArtifactDefinition(
        nodeTemplateName: String,
        artifactName: String
    ): ArtifactDefinition {
        val nodeTemplate = bluePrintContext.nodeTemplateByName(nodeTemplateName)

        return nodeTemplate.artifacts?.get(artifactName)
            ?: throw BlueprintProcessorException(
                "failed to get artifact definition($artifactName) from the node template"
            )
    }

    override fun resolveRelationshipTemplateProperties(relationshipTemplateName: String): MutableMap<String, JsonNode> {
        log.info("resolveRelationshipTemplateProperties for relationship template ({})", relationshipTemplateName)

        val relationshipTemplate = bluePrintContext.relationshipTemplateByName(relationshipTemplateName)

        val propertyAssignments = relationshipTemplate.properties!!

        // Get the Relationship Type Definitions
        val propertiesDefinitions = bluePrintContext.relationshipTypeByName(relationshipTemplate.type).properties
            ?: throw BlueprintProcessorException("failed to get ${relationshipTemplate.type} properties.")

        /**
         * Resolve the RelationshipTemplate Property Assignment Values.
         */
        return resolveRelationshipTemplatePropertyAssignments(
            relationshipTemplateName,
            propertiesDefinitions,
            propertyAssignments
        )
    }

    override fun resolveRelationshipTemplatePropertyAssignments(
        relationshipTemplateName: String,
        propertyDefinitions: MutableMap<String, PropertyDefinition>,
        propertyAssignments: MutableMap<String, JsonNode>
    ): MutableMap<String, JsonNode> {
        return resolvePropertyAssignments(
            BlueprintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE,
            relationshipTemplateName, propertyDefinitions, propertyAssignments
        )
    }

    override fun setInputValue(propertyName: String, value: JsonNode) {
        val path = """${BlueprintConstants.PATH_INPUTS}${BlueprintConstants.PATH_DIVIDER}$propertyName"""
        put(path, value)
    }

    override fun setWorkflowInputValue(
        workflowName: String,
        propertyName: String,
        propertyDefinition: PropertyDefinition,
        value: JsonNode
    ) {
        val path: String = StringBuilder(BlueprintConstants.PATH_NODE_WORKFLOWS)
            .append(BlueprintConstants.PATH_DIVIDER).append(workflowName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_INPUTS)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_PROPERTIES)
            .append(BlueprintConstants.PATH_DIVIDER).append(propertyName).toString()
        put(path, value)
    }

    override fun setNodeTemplatePropertyValue(nodeTemplateName: String, propertyName: String, value: JsonNode) {
        val path: String = StringBuilder(BlueprintConstants.PATH_NODE_TEMPLATES)
            .append(BlueprintConstants.PATH_DIVIDER).append(nodeTemplateName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_PROPERTIES)
            .append(BlueprintConstants.PATH_DIVIDER).append(propertyName).toString()
        put(path, value)
    }

    override fun setNodeTemplateAttributeValue(nodeTemplateName: String, attributeName: String, value: JsonNode) {
        val path: String = StringBuilder(BlueprintConstants.PATH_NODE_TEMPLATES)
            .append(BlueprintConstants.PATH_DIVIDER).append(nodeTemplateName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_ATTRIBUTES)
            .append(BlueprintConstants.PATH_DIVIDER).append(attributeName).toString()
        put(path, value)
    }

    override fun setNodeTemplateOperationPropertyValue(
        nodeTemplateName: String,
        interfaceName: String,
        operationName: String,
        propertyName: String,
        value: JsonNode
    ) {
        val path: String = StringBuilder(BlueprintConstants.PATH_NODE_TEMPLATES)
            .append(BlueprintConstants.PATH_DIVIDER).append(nodeTemplateName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_INTERFACES)
            .append(BlueprintConstants.PATH_DIVIDER).append(interfaceName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_OPERATIONS)
            .append(BlueprintConstants.PATH_DIVIDER).append(operationName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_PROPERTIES)
            .append(BlueprintConstants.PATH_DIVIDER).append(propertyName).toString()
        log.trace("setting operation property path ({}), values ({})", path, value)
        put(path, value)
    }

    override fun setNodeTemplateOperationInputValue(
        nodeTemplateName: String,
        interfaceName: String,
        operationName: String,
        propertyName: String,
        value: JsonNode
    ) {
        val path: String = StringBuilder(BlueprintConstants.PATH_NODE_TEMPLATES)
            .append(BlueprintConstants.PATH_DIVIDER).append(nodeTemplateName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_INTERFACES)
            .append(BlueprintConstants.PATH_DIVIDER).append(interfaceName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_OPERATIONS)
            .append(BlueprintConstants.PATH_DIVIDER).append(operationName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_INPUTS)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_PROPERTIES)
            .append(BlueprintConstants.PATH_DIVIDER).append(propertyName).toString()
        put(path, value)
    }

    override fun setNodeTemplateOperationOutputValue(
        nodeTemplateName: String,
        interfaceName: String,
        operationName: String,
        propertyName: String,
        value: JsonNode
    ) {
        val path: String = StringBuilder(BlueprintConstants.PATH_NODE_TEMPLATES)
            .append(BlueprintConstants.PATH_DIVIDER).append(nodeTemplateName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_INTERFACES)
            .append(BlueprintConstants.PATH_DIVIDER).append(interfaceName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_OPERATIONS)
            .append(BlueprintConstants.PATH_DIVIDER).append(operationName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_OUTPUTS)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_PROPERTIES)
            .append(BlueprintConstants.PATH_DIVIDER).append(propertyName).toString()
        put(path, value)
    }

    override fun getInputValue(propertyName: String): JsonNode {
        val path = StringBuilder(BlueprintConstants.PATH_INPUTS)
            .append(BlueprintConstants.PATH_DIVIDER).append(propertyName).toString()
        return getJsonNode(path)
    }

    override fun getNodeTemplateOperationOutputValue(
        nodeTemplateName: String,
        interfaceName: String,
        operationName: String,
        propertyName: String
    ): JsonNode {
        val path: String = StringBuilder(BlueprintConstants.PATH_NODE_TEMPLATES)
            .append(BlueprintConstants.PATH_DIVIDER).append(nodeTemplateName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_INTERFACES)
            .append(BlueprintConstants.PATH_DIVIDER).append(interfaceName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_OPERATIONS)
            .append(BlueprintConstants.PATH_DIVIDER).append(operationName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_OUTPUTS)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_PROPERTIES)
            .append(BlueprintConstants.PATH_DIVIDER).append(propertyName).toString()
        return getJsonNode(path)
    }

    override fun getNodeTemplatePropertyValue(nodeTemplateName: String, propertyName: String): JsonNode {
        val path: String = StringBuilder(BlueprintConstants.PATH_NODE_TEMPLATES)
            .append(BlueprintConstants.PATH_DIVIDER).append(nodeTemplateName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_PROPERTIES)
            .append(BlueprintConstants.PATH_DIVIDER).append(propertyName).toString()
        return getJsonNode(path)
    }

    override fun getNodeTemplateAttributeValue(nodeTemplateName: String, attributeName: String): JsonNode {
        val path: String = StringBuilder(BlueprintConstants.PATH_NODE_TEMPLATES)
            .append(BlueprintConstants.PATH_DIVIDER).append(nodeTemplateName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_ATTRIBUTES)
            .append(BlueprintConstants.PATH_DIVIDER).append(attributeName).toString()
        return getJsonNode(path)
    }

    override fun getRelationshipTemplatePropertyValue(
        relationshipTemplateName: String,
        propertyName: String
    ): JsonNode? {
        val path: String = StringBuilder(BlueprintConstants.PATH_RELATIONSHIP_TEMPLATES)
            .append(BlueprintConstants.PATH_DIVIDER).append(relationshipTemplateName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_PROPERTIES)
            .append(BlueprintConstants.PATH_DIVIDER).append(propertyName).toString()
        return getJsonNode(path)
    }

    override fun getRelationshipTemplateAttributeValue(
        relationshipTemplateName: String,
        attributeName: String
    ): JsonNode? {
        val path: String = StringBuilder(BlueprintConstants.PATH_RELATIONSHIP_TEMPLATES)
            .append(BlueprintConstants.PATH_DIVIDER).append(relationshipTemplateName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_ATTRIBUTES)
            .append(BlueprintConstants.PATH_DIVIDER).append(attributeName).toString()
        return getJsonNode(path)
    }

    override fun assignInputs(jsonNode: JsonNode) {
        log.info("assignInputs from input JSON ({})", jsonNode.toString())
        bluePrintContext.inputs()?.forEach { propertyName, property ->
            val valueNode: JsonNode = jsonNode.at(BlueprintConstants.PATH_DIVIDER + propertyName)
                ?: property.defaultValue
                ?: NullNode.getInstance()
            setInputValue(propertyName, valueNode)
        }
    }

    override fun assignWorkflowInputs(workflowName: String, jsonNode: JsonNode) {
        log.info("Deriving input data for workflow: ($workflowName)")

        val dynamicInputPropertiesName = "$workflowName-properties"

        bluePrintContext.workflowByName(workflowName).inputs
            ?.filter { (propertyName, property) -> propertyName != dynamicInputPropertiesName }
            ?.forEach { propertyName, property -> findAndSetInputValue(propertyName, property, jsonNode) }
        // Load Dynamic data Types
        jsonNode.get(dynamicInputPropertiesName)?.let {
            bluePrintContext.dataTypeByName("dt-$dynamicInputPropertiesName")
                ?.properties
                ?.forEach { propertyName, property -> findAndSetInputValue(propertyName, property, it) }
        }
    }

    private fun findAndSetInputValue(propertyName: String, property: PropertyDefinition, jsonNode: JsonNode) {
        val valueNode = jsonNode.at(BlueprintConstants.PATH_DIVIDER + propertyName)
            .returnNullIfMissing()
            ?: property.defaultValue
            ?: NullNode.getInstance()
        val loggableValue = if (hasLogProtect(property)) LOG_REDACTED else valueNode.toString()
        log.trace("Setting input data - attribute:($propertyName) value:($loggableValue)")
        setInputValue(propertyName, valueNode)
    }

    override fun getJsonForNodeTemplateAttributeProperties(nodeTemplateName: String, keys: List<String>): JsonNode {

        val jsonNode: ObjectNode = jacksonObjectMapper().createObjectNode()
        val path: String = StringBuilder(BlueprintConstants.PATH_NODE_TEMPLATES)
            .append(BlueprintConstants.PATH_DIVIDER).append(nodeTemplateName)
            .append(BlueprintConstants.PATH_DIVIDER).append(BlueprintConstants.PATH_ATTRIBUTES)
            .append(BlueprintConstants.PATH_DIVIDER).toString()
        store.keys.filter {
            it.startsWith(path)
        }.map {
            val key = it.replace(path, "")
            if (keys.contains(key)) {
                val value = store[it] as JsonNode
                jsonNode.set<JsonNode>(key, value)
            }
        }
        return jsonNode
    }

    override suspend fun close() {
        store.clear()
    }
}

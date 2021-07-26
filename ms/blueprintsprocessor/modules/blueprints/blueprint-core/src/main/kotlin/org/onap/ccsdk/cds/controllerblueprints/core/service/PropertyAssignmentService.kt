/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 - 2019 IBM, Bell Canada.
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
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactExpression
import org.onap.ccsdk.cds.controllerblueprints.core.data.AttributeExpression
import org.onap.ccsdk.cds.controllerblueprints.core.data.ExpressionData
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationOutputExpression
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyExpression
import org.onap.ccsdk.cds.controllerblueprints.core.format
import org.onap.ccsdk.cds.controllerblueprints.core.isComplexType
import org.onap.ccsdk.cds.controllerblueprints.core.jsonPathParse
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.ResourceResolverUtils
import org.slf4j.LoggerFactory

/**
 *
 *
 * @author Brinda Santh
 */
open class PropertyAssignmentService(var bluePrintRuntimeService: BluePrintRuntimeService<MutableMap<String, JsonNode>>) {

    private val log = LoggerFactory.getLogger(this::class.toString())

    private var bluePrintContext: BluePrintContext = bluePrintRuntimeService.bluePrintContext()

    /*

    If Property Assignment is Expression.
        Get the Expression
        Recursively resolve the expression
     */

    fun resolveAssignmentExpression(
        definitionType: String,
        definitionName: String,
        assignmentName: String,
        assignment: JsonNode
    ): JsonNode {
        log.trace("Assignment ({})", assignment)
        val expressionData = BluePrintExpressionService.getExpressionData(assignment)

        return if (expressionData.isExpression) {
            resolveExpression(definitionType, definitionName, assignmentName, expressionData)
        } else {
            expressionData.valueNode
        }
    }

    fun resolveExpression(
        definitionType: String,
        definitionName: String,
        propName: String,
        expressionData: ExpressionData
    ): JsonNode {

        var valueNode: JsonNode = NullNode.getInstance()

        if (expressionData.isExpression) {
            val command = expressionData.command!!

            when (command) {
                BluePrintConstants.EXPRESSION_GET_INPUT -> {
                    valueNode = bluePrintRuntimeService.getInputValue(expressionData.inputExpression?.propertyName!!)
                }
                BluePrintConstants.EXPRESSION_GET_ATTRIBUTE -> {
                    valueNode =
                        resolveAttributeExpression(definitionType, definitionName, expressionData.attributeExpression!!)
                }
                BluePrintConstants.EXPRESSION_GET_PROPERTY -> {
                    valueNode =
                        resolvePropertyExpression(definitionType, definitionName, expressionData.propertyExpression!!)
                }
                BluePrintConstants.EXPRESSION_GET_OPERATION_OUTPUT -> {
                    valueNode =
                        resolveOperationOutputExpression(definitionName, expressionData.operationOutputExpression!!)
                }
                BluePrintConstants.EXPRESSION_GET_ARTIFACT -> {
                    valueNode = resolveArtifactExpression(definitionName, expressionData.artifactExpression!!)
                }
                BluePrintConstants.EXPRESSION_DSL_REFERENCE -> {
                    valueNode =
                        bluePrintRuntimeService.resolveDSLExpression(expressionData.dslExpression!!.propertyName)
                }
                BluePrintConstants.EXPRESSION_GET_NODE_OF_TYPE -> {
                }
                else -> {
                    throw BluePrintException(
                        "for $definitionType($definitionName) property ($propName), " +
                            "command ($command) is not supported "
                    )
                }
            }
        }
        return valueNode
    }

    /*
    get_attribute: [ <modelable_entity_name>, <optional_req_or_cap_name>, <property_name>,
    <nested_property_name_or_index_1>, ..., <nested_property_name_or_index_n> ]
 */
    fun resolveAttributeExpression(
        definitionType: String,
        definitionName: String,
        attributeExpression: AttributeExpression
    ): JsonNode {
        var valueNode: JsonNode

        val attributeName = attributeExpression.attributeName
        val subAttributeName: String? = attributeExpression.subAttributeName

        var attributeDefinitionName = definitionName
        /**
         * Attributes are dynamic runtime properties information. There are multiple types of Attributes,
         * ENV : Environment Variables
         * APP : Application properties ( ie Spring resolved properties )
         * BPP : Blueprint Properties, Specific to Blue Print execution.
         * SELF : Current Node Template properties.
         */
        when (attributeExpression.modelableEntityName) {
            BluePrintConstants.PROPERTY_ENV -> {
                val environmentValue = System.getenv(attributeName)
                valueNode = environmentValue.asJsonPrimitive()
            }
            BluePrintConstants.PROPERTY_APP -> {
                val environmentValue = System.getProperty(attributeName)
                valueNode = environmentValue.asJsonPrimitive()
            }
            BluePrintConstants.PROPERTY_BPP -> {
                valueNode = bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    BluePrintConstants.PROPERTY_BPP,
                    attributeName
                ) ?: throw BluePrintException("failed to get env attribute name ($attributeName) ")
            }
            else -> {
                if (!attributeExpression.modelableEntityName.equals(BluePrintConstants.PROPERTY_SELF, true)) {
                    attributeDefinitionName = attributeExpression.modelableEntityName
                }

                /** This block is to Validate, if Attribute definition is present */
                when (definitionType) {
                    BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TEMPLATE,
                    BluePrintConstants.MODEL_DEFINITION_TYPE_WORKFLOW,
                    BluePrintConstants.MODEL_DEFINITION_TYPE_DSL ->
                        bluePrintContext.nodeTemplateNodeType(attributeDefinitionName).attributes
                    BluePrintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TEMPLATE ->
                        bluePrintContext.relationshipTemplateRelationshipType(attributeDefinitionName).attributes
                    else -> throw BluePrintException("failed to understand template type($definitionType), it is not supported")
                }?.get(attributeName)
                    ?: throw BluePrintException(
                        "failed to get attribute definitions for " +
                            "$definitionType ($attributeDefinitionName)'s attribute name ($attributeName) "
                    )

                valueNode = when (definitionType) {
                    BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TEMPLATE,
                    BluePrintConstants.MODEL_DEFINITION_TYPE_WORKFLOW,
                    BluePrintConstants.MODEL_DEFINITION_TYPE_DSL ->
                        bluePrintRuntimeService.getNodeTemplateAttributeValue(attributeDefinitionName, attributeName)
                    BluePrintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TEMPLATE ->
                        bluePrintRuntimeService.getRelationshipTemplateAttributeValue(
                            attributeDefinitionName,
                            attributeName
                        )
                    else -> throw BluePrintException("failed to understand template type($definitionType), it is not supported")
                }
                    ?: throw BluePrintException("failed to get node template ($attributeDefinitionName)'s attribute name ($attributeName) ")
            }
        }
        if (subAttributeName != null) {
            if (valueNode.isComplexType())
                valueNode = valueNode.jsonPathParse(subAttributeName)
        }
        return valueNode
    }

    /*
        get_property: [ <modelable_entity_name>, <optional_req_or_cap_name>, <property_name>,
        <nested_property_name_or_index_1>, ..., <nested_property_name_or_index_n> ]
     */
    fun resolvePropertyExpression(
        definitionType: String,
        definitionName: String,
        propertyExpression: PropertyExpression
    ): JsonNode {
        var valueNode: JsonNode

        val propertyName = propertyExpression.propertyName
        val subPropertyName: String? = propertyExpression.subPropertyName

        var propertyDefinitionName = definitionName

        if (!propertyExpression.modelableEntityName.equals(BluePrintConstants.PROPERTY_SELF, true)) {
            propertyDefinitionName = propertyExpression.modelableEntityName
        }

        val nodeTemplatePropertyExpression = when (definitionType) {
            BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TEMPLATE,
            BluePrintConstants.MODEL_DEFINITION_TYPE_WORKFLOW,
            BluePrintConstants.MODEL_DEFINITION_TYPE_DSL ->
                bluePrintContext.nodeTemplateByName(propertyDefinitionName).properties
            BluePrintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TEMPLATE ->
                bluePrintContext.relationshipTemplateByName(propertyDefinitionName).properties
            else -> throw BluePrintException("failed to understand template type($definitionType), it is not supported")
        }?.get(propertyName)
            ?: throw BluePrintException("failed to get property assignment for node template ($definitionName)'s property name ($propertyName).")

        /** This block is to Validate, if Property definition is present */
        when (definitionType) {
            BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TEMPLATE,
            BluePrintConstants.MODEL_DEFINITION_TYPE_WORKFLOW,
            BluePrintConstants.MODEL_DEFINITION_TYPE_DSL ->
                bluePrintContext.nodeTemplateNodeType(propertyDefinitionName).properties
            BluePrintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TEMPLATE ->
                bluePrintContext.relationshipTemplateRelationshipType(propertyDefinitionName).properties
            else -> throw BluePrintException("failed to understand template type($definitionType), it is not supported")
        }?.get(propertyName)
            ?: throw BluePrintException("failed to get property definition for node template ($definitionName)'s property name ($propertyName).")

        log.info(
            "$definitionType($propertyDefinitionName), property($propertyName) resolved value ($nodeTemplatePropertyExpression)"
        )

        // Check it it is a nested expression
        valueNode = resolveAssignmentExpression(
            definitionType,
            propertyDefinitionName,
            propertyName,
            nodeTemplatePropertyExpression
        )

        if (subPropertyName != null) {
            if (valueNode.isComplexType())
                valueNode = valueNode.jsonPathParse(subPropertyName)
        }
        return valueNode
    }

    /*
    get_operation_output: <modelable_entity_name>, <interface_name>, <operation_name>, <output_variable_name>
     */
    fun resolveOperationOutputExpression(
        nodeTemplateName: String,
        operationOutputExpression: OperationOutputExpression
    ): JsonNode {
        var outputNodeTemplateName = nodeTemplateName
        if (!operationOutputExpression.modelableEntityName.equals("SELF", true)) {
            outputNodeTemplateName = operationOutputExpression.modelableEntityName
        }

        var valueNode = bluePrintRuntimeService.getNodeTemplateOperationOutputValue(
            outputNodeTemplateName,
            operationOutputExpression.interfaceName, operationOutputExpression.operationName,
            operationOutputExpression.propertyName
        )

        val subPropertyName: String? = operationOutputExpression.subPropertyName
        if (subPropertyName != null) {
            if (valueNode.isComplexType())
                valueNode = valueNode.jsonPathParse(subPropertyName)
        }
        return valueNode
    }

    /*
    get_artifact: [ <modelable_entity_name>, <artifact_name>, <location>, <remove> ]
     */
    fun resolveArtifactExpression(nodeTemplateName: String, artifactExpression: ArtifactExpression): JsonNode {

        var artifactNodeTemplateName = nodeTemplateName
        if (!artifactExpression.modelableEntityName.equals("SELF", true)) {
            artifactNodeTemplateName = artifactExpression.modelableEntityName
        }
        val artifactDefinition: ArtifactDefinition = bluePrintContext.nodeTemplateByName(artifactNodeTemplateName)
            .artifacts?.get(artifactExpression.artifactName)
            ?: throw BluePrintException(
                format(
                    "failed to get artifact definitions for node template ({})'s " +
                        "artifact name ({}) ",
                    nodeTemplateName, artifactExpression.artifactName
                )
            )

        return JacksonUtils.jsonNodeFromObject(artifactContent(artifactDefinition))
    }

    fun artifactContent(artifactDefinition: ArtifactDefinition): String {
        val bluePrintBasePath: String = bluePrintContext.rootPath

        if (artifactDefinition.repository != null) {
            TODO()
        } else if (artifactDefinition.file != null) {
            return ResourceResolverUtils.getFileContent(artifactDefinition.file, bluePrintBasePath)
        }
        return ""
    }
}

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
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactExpression
import org.onap.ccsdk.cds.controllerblueprints.core.data.AttributeExpression
import org.onap.ccsdk.cds.controllerblueprints.core.data.DSLExpression
import org.onap.ccsdk.cds.controllerblueprints.core.data.ExpressionData
import org.onap.ccsdk.cds.controllerblueprints.core.data.InputExpression
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationOutputExpression
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyExpression
import org.slf4j.LoggerFactory

/**
 *
 *
 * @author Brinda Santh
 */
object BluePrintExpressionService {

    val log = LoggerFactory.getLogger(this::class.toString())

    @JvmStatic
    fun checkContainsExpression(propertyAssignmentNode: JsonNode): Boolean {
        val json = propertyAssignmentNode.toString()
        // FIXME("Check if any Optimisation needed")
        return (
            json.contains(BluePrintConstants.EXPRESSION_GET_INPUT) ||
                json.contains(BluePrintConstants.EXPRESSION_GET_ATTRIBUTE) ||
                json.contains(BluePrintConstants.EXPRESSION_GET_PROPERTY)
            )
    }

    @JvmStatic
    fun getExpressionData(propertyAssignmentNode: JsonNode): ExpressionData {
        log.trace("Assignment Data/Expression : {}", propertyAssignmentNode)
        val expressionData = ExpressionData(valueNode = propertyAssignmentNode)
        if (propertyAssignmentNode is ObjectNode) {
            val commands: Set<String> = propertyAssignmentNode.fieldNames().asSequence().toList().intersect(BluePrintTypes.validCommands())
            if (commands.isNotEmpty()) {
                expressionData.isExpression = true
                expressionData.command = commands.first()
                expressionData.expressionNode = propertyAssignmentNode

                when (expressionData.command) {
                    BluePrintConstants.EXPRESSION_GET_INPUT -> {
                        expressionData.inputExpression = populateInputExpression(propertyAssignmentNode)
                    }
                    BluePrintConstants.EXPRESSION_GET_ATTRIBUTE -> {
                        expressionData.attributeExpression = populateAttributeExpression(propertyAssignmentNode)
                    }
                    BluePrintConstants.EXPRESSION_GET_PROPERTY -> {
                        expressionData.propertyExpression = populatePropertyExpression(propertyAssignmentNode)
                    }
                    BluePrintConstants.EXPRESSION_GET_OPERATION_OUTPUT -> {
                        expressionData.operationOutputExpression = populateOperationOutputExpression(propertyAssignmentNode)
                    }
                    BluePrintConstants.EXPRESSION_GET_ARTIFACT -> {
                        expressionData.artifactExpression = populateArtifactExpression(propertyAssignmentNode)
                    }
                }
            }
        } else if (propertyAssignmentNode is TextNode &&
            propertyAssignmentNode.textValue().startsWith(BluePrintConstants.EXPRESSION_DSL_REFERENCE)
        ) {
            expressionData.isExpression = true
            expressionData.command = BluePrintConstants.EXPRESSION_DSL_REFERENCE
            expressionData.dslExpression = populateDSLExpression(propertyAssignmentNode)
        }
        return expressionData
    }

    @JvmStatic
    fun populateDSLExpression(jsonNode: TextNode): DSLExpression {
        return DSLExpression(
            propertyName = jsonNode.textValue()
                .removePrefix(BluePrintConstants.EXPRESSION_DSL_REFERENCE)
        )
    }

    @JvmStatic
    fun populateInputExpression(jsonNode: JsonNode): InputExpression {
        return InputExpression(propertyName = jsonNode.first().textValue())
    }

    @JvmStatic
    fun populatePropertyExpression(jsonNode: JsonNode): PropertyExpression {
        val arrayNode: ArrayNode = jsonNode.first() as ArrayNode
        check(arrayNode.size() >= 2) {
            throw BluePrintException(
                String.format(
                    "missing property expression, " +
                        "it should be [ <modelable_entity_name>, <optional_req_or_cap_name>, <property_name>, " +
                        "<nested_property_name_or_index_1>, ..., <nested_property_name_or_index_n> ] , but present {}",
                    jsonNode
                )
            )
        }
        var reqOrCapEntityName: String? = null
        var propertyName = ""
        var subProperty: String? = null
        when {
            arrayNode.size() == 2 -> propertyName = arrayNode[1].textValue()
            arrayNode.size() == 3 -> {
                reqOrCapEntityName = arrayNode[1].textValue()
                propertyName = arrayNode[2].textValue()
            }
            arrayNode.size() > 3 -> {
                reqOrCapEntityName = arrayNode[1].textValue()
                propertyName = arrayNode[2].textValue()
                val propertyPaths: List<String> = arrayNode.filterIndexed { index, _ ->
                    index >= 3
                }.map { it.textValue() }
                subProperty = propertyPaths.joinToString(".")
            }
        }

        return PropertyExpression(
            modelableEntityName = arrayNode[0].asText(),
            reqOrCapEntityName = reqOrCapEntityName,
            propertyName = propertyName,
            subPropertyName = subProperty
        )
    }

    @JvmStatic
    fun populateAttributeExpression(jsonNode: JsonNode): AttributeExpression {
        val arrayNode: ArrayNode = jsonNode.first() as ArrayNode
        check(arrayNode.size() >= 2) {
            throw BluePrintException(
                String.format(
                    "missing attribute expression, " +
                        "it should be [ <modelable_entity_name>, <optional_req_or_cap_name>, <attribute_name>," +
                        " <nested_attribute_name_or_index_1>, ..., <nested_attribute_name_or_index_n> ] , but present {}",
                    jsonNode
                )
            )
        }

        var reqOrCapEntityName: String? = null
        var attributeName = ""
        var subAttributeName: String? = null
        when {
            arrayNode.size() == 2 -> attributeName = arrayNode[1].textValue()
            arrayNode.size() == 3 -> {
                reqOrCapEntityName = arrayNode[1].textValue()
                attributeName = arrayNode[2].textValue()
            }
            arrayNode.size() > 3 -> {
                reqOrCapEntityName = arrayNode[1].textValue()
                attributeName = arrayNode[2].textValue()
                val propertyPaths: List<String> = arrayNode.filterIndexed { index, _ ->
                    index >= 3
                }.map { it.textValue() }
                subAttributeName = propertyPaths.joinToString(".")
            }
        }
        return AttributeExpression(
            modelableEntityName = arrayNode[0].asText(),
            reqOrCapEntityName = reqOrCapEntityName,
            attributeName = attributeName,
            subAttributeName = subAttributeName
        )
    }

    @JvmStatic
    fun populateOperationOutputExpression(jsonNode: JsonNode): OperationOutputExpression {
        val arrayNode: ArrayNode = jsonNode.first() as ArrayNode

        check(arrayNode.size() >= 4) {
            throw BluePrintException(
                String.format(
                    "missing operation output expression, " +
                        "it should be (<modelable_entity_name>, <interface_name>, <operation_name>, <output_variable_name>) , but present {}",
                    jsonNode
                )
            )
        }

        var subPropertyName: String? = null
        if (arrayNode.size() == 5)
            subPropertyName = arrayNode[4].asText()

        return OperationOutputExpression(
            modelableEntityName = arrayNode[0].asText(),
            interfaceName = arrayNode[1].asText(),
            operationName = arrayNode[2].asText(),
            propertyName = arrayNode[3].asText(),
            subPropertyName = subPropertyName
        )
    }

    @JvmStatic
    fun populateArtifactExpression(jsonNode: JsonNode): ArtifactExpression {
        val arrayNode: ArrayNode = jsonNode.first() as ArrayNode

        check(arrayNode.size() >= 2) {
            throw BluePrintException(
                String.format(
                    "missing artifact expression, " +
                        "it should be [ <modelable_entity_name>, <artifact_name>, <location>, <remove> ] , but present {}",
                    jsonNode
                )
            )
        }
        return ArtifactExpression(
            modelableEntityName = arrayNode[0].asText(),
            artifactName = arrayNode[1].asText(),
            location = arrayNode[2]?.asText() ?: "LOCAL_FILE",
            remove = arrayNode[3]?.asBoolean() ?: false
        )
    }
}

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


import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import org.onap.ccsdk.apps.controllerblueprints.core.format
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.ResourceResolverUtils

/**
 *
 *
 * @author Brinda Santh
 */
class PropertyAssignmentService(var bluePrintRuntimeService: BluePrintRuntimeService<MutableMap<String, JsonNode>>) {
    private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())

    private var bluePrintContext: BluePrintContext = bluePrintRuntimeService.bluePrintContext()

/*

If Property Assignment is Expression.
    Get the Expression
    Recursively resolve the expression
 */

    fun resolveAssignmentExpression(nodeTemplateName: String, assignmentName: String,
                                    assignment: Any): JsonNode {
        val valueNode: JsonNode
        log.trace("Assignment ({})", assignment)
        val expressionData = BluePrintExpressionService.getExpressionData(assignment)

        if (expressionData.isExpression) {
            valueNode = resolveExpression(nodeTemplateName, assignmentName, expressionData)
        } else {
            valueNode = expressionData.valueNode
        }
        return valueNode
    }

    fun resolveExpression(nodeTemplateName: String, propName: String, expressionData: ExpressionData): JsonNode {

        var valueNode: JsonNode = NullNode.getInstance()

        if (expressionData.isExpression) {
            val command = expressionData.command!!

            when (command) {
                BluePrintConstants.EXPRESSION_GET_INPUT -> {
                    valueNode = bluePrintRuntimeService.getInputValue(expressionData.inputExpression?.propertyName!!)
                }
                BluePrintConstants.EXPRESSION_GET_ATTRIBUTE -> {
                    valueNode = resolveAttributeExpression(nodeTemplateName, expressionData.attributeExpression!!)
                }
                BluePrintConstants.EXPRESSION_GET_PROPERTY -> {
                    valueNode = resolvePropertyExpression(nodeTemplateName, expressionData.propertyExpression!!)
                }
                BluePrintConstants.EXPRESSION_GET_OPERATION_OUTPUT -> {
                    valueNode = resolveOperationOutputExpression(nodeTemplateName, expressionData.operationOutputExpression!!)
                }
                BluePrintConstants.EXPRESSION_GET_ARTIFACT -> {
                    valueNode = resolveArtifactExpression(nodeTemplateName, expressionData.artifactExpression!!)
                }
                BluePrintConstants.EXPRESSION_GET_NODE_OF_TYPE -> {

                }
                else -> {
                    throw BluePrintException(format("for property ({}), command ({}) is not supported ", propName, command))
                }
            }
        }
        return valueNode
    }

    /*
    get_property: [ <modelable_entity_name>, <optional_req_or_cap_name>, <property_name>,
    <nested_property_name_or_index_1>, ..., <nested_property_name_or_index_n> ]
 */
    fun resolveAttributeExpression(nodeTemplateName: String, attributeExpression: AttributeExpression): JsonNode {
        val valueNode: JsonNode

        val attributeName = attributeExpression.attributeName
        val subAttributeName: String? = attributeExpression.subAttributeName

        var attributeNodeTemplateName = nodeTemplateName
        when (attributeExpression.modelableEntityName) {
            "ENV" -> {
                val environmentValue = System.getProperty(attributeName)
                valueNode = JacksonUtils.jsonNode(environmentValue)
            }
            else -> {
                if (!attributeExpression.modelableEntityName.equals("SELF", true)) {
                    attributeNodeTemplateName = attributeExpression.modelableEntityName
                }
                /* Enable in ONAP Dublin Release
                val nodeTemplateAttributeExpression = bluePrintContext.nodeTemplateByName(attributeNodeTemplateName).attributes?.get(attributeName)
                        ?: throw BluePrintException(format("failed to get attribute definitions for node template " +
                                "({})'s property name ({}) ", nodeTemplateName, attributeName))

                var attributeDefinition: AttributeDefinition = bluePrintContext.nodeTemplateNodeType(attributeNodeTemplateName).attributes?.get(attributeName)!!

                log.info("node template name ({}), property Name ({}) resolved value ({})", attributeNodeTemplateName, attributeName, nodeTemplateAttributeExpression)
                */

                valueNode = bluePrintRuntimeService.getNodeTemplateAttributeValue(attributeNodeTemplateName, attributeName)
                        ?: throw BluePrintException(format("failed to get node template ({})'s attribute ({}) ", nodeTemplateName, attributeName))
            }

        }
//        subPropertyName?.let {
//            valueNode = valueNode.at(JsonPointer.valueOf(subPropertyName))
//        }
        return valueNode
    }

    /*
        get_property: [ <modelable_entity_name>, <optional_req_or_cap_name>, <property_name>,
        <nested_property_name_or_index_1>, ..., <nested_property_name_or_index_n> ]
     */
    fun resolvePropertyExpression(nodeTemplateName: String, propertyExpression: PropertyExpression): JsonNode {
        val valueNode: JsonNode

        val propertyName = propertyExpression.propertyName
        val subPropertyName: String? = propertyExpression.subPropertyName

        var propertyNodeTemplateName = nodeTemplateName
        if (!propertyExpression.modelableEntityName.equals("SELF", true)) {
            propertyNodeTemplateName = propertyExpression.modelableEntityName
        }

        val nodeTemplatePropertyExpression = bluePrintContext.nodeTemplateByName(propertyNodeTemplateName).properties?.get(propertyName)
                ?: throw BluePrintException(format("failed to get property definitions for node template ({})'s property name ({}) ", nodeTemplateName, propertyName))

        var propertyDefinition: PropertyDefinition = bluePrintContext.nodeTemplateNodeType(propertyNodeTemplateName).properties?.get(propertyName)!!

        log.info("node template name ({}), property Name ({}) resolved value ({})", propertyNodeTemplateName, propertyName, nodeTemplatePropertyExpression)

        // Check it it is a nested expression
        valueNode = resolveAssignmentExpression(propertyNodeTemplateName, propertyName, nodeTemplatePropertyExpression)

//        subPropertyName?.let {
//            valueNode = valueNode.at(JsonPointer.valueOf(subPropertyName))
//        }
        return valueNode
    }

    /*
    get_operation_output: <modelable_entity_name>, <interface_name>, <operation_name>, <output_variable_name>
     */
    fun resolveOperationOutputExpression(nodeTemplateName: String, operationOutputExpression: OperationOutputExpression): JsonNode {
        var outputNodeTemplateName = nodeTemplateName
        if (!operationOutputExpression.modelableEntityName.equals("SELF", true)) {
            outputNodeTemplateName = operationOutputExpression.modelableEntityName
        }
        return bluePrintRuntimeService.getNodeTemplateOperationOutputValue(outputNodeTemplateName,
                operationOutputExpression.interfaceName, operationOutputExpression.operationName,
                operationOutputExpression.propertyName)
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
                ?: throw BluePrintException(format("failed to get artifact definitions for node template ({})'s " +
                        "artifact name ({}) ", nodeTemplateName, artifactExpression.artifactName))

        return JacksonUtils.jsonNodeFromObject(artifactContent(artifactDefinition))
    }

    fun artifactContent(artifactDefinition: ArtifactDefinition): String {
        val bluePrintBasePath: String = bluePrintRuntimeService.get(BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH) as? String
                ?: throw BluePrintException(format("failed to get property (%s) from context", BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH))

        if (artifactDefinition.repository != null) {
            TODO()
        } else if (artifactDefinition.file != null) {
            return ResourceResolverUtils.getFileContent(artifactDefinition.file!!, bluePrintBasePath)
        }
        return ""
    }
}


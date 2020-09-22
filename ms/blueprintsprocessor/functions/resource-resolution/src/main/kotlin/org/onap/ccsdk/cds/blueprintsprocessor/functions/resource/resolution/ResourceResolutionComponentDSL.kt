/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.AbstractNodeTemplateOperationImplBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertiesAssignmentBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.ServiceTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.TopologyTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.nodeType
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType

/** Component Extensions **/
fun ServiceTemplateBuilder.nodeTypeComponentResourceResolution() {
    val nodeType = BluePrintTypes.nodeTypeComponentResourceResolution()
    if (this.nodeTypes == null) this.nodeTypes = hashMapOf()
    this.nodeTypes!![nodeType.id!!] = nodeType
}

fun BluePrintTypes.nodeTypeComponentResourceResolution(): NodeType {
    return nodeType(
        id = "component-resource-resolution", version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_NODE_COMPONENT,
        description = "Resource Assignment Component"
    ) {

        attribute(
            ResourceResolutionComponent.ATTRIBUTE_ASSIGNMENT_PARAM, BluePrintConstants.DATA_TYPE_STRING,
            true
        )
        attribute(
            ResourceResolutionComponent.ATTRIBUTE_STATUS, BluePrintConstants.DATA_TYPE_STRING,
            true
        )

        operation("ResourceResolutionComponent", "ResourceResolutionComponent Operation") {
            inputs {
                property(
                    ResourceResolutionComponent.INPUT_REQUEST_ID, BluePrintConstants.DATA_TYPE_STRING,
                    true, "Request Id, Unique Id for the request."
                )

                property(
                    ResourceResolutionComponent.INPUT_RESOURCE_ID, BluePrintConstants.DATA_TYPE_STRING,
                    false, "Resource Id."
                )

                property(
                    ResourceResolutionComponent.INPUT_ACTION_NAME, BluePrintConstants.DATA_TYPE_STRING,
                    false, "Action Name of the process"
                )

                property(
                    ResourceResolutionComponent.INPUT_DYNAMIC_PROPERTIES, BluePrintConstants.DATA_TYPE_JSON,
                    false, "Dynamic Json Content or DSL Json reference."
                )

                property(
                    ResourceResolutionComponent.INPUT_RESOLUTION_KEY, BluePrintConstants.DATA_TYPE_STRING,
                    false, "Key for service instance related correlation."
                )

                property(
                    ResourceResolutionComponent.INPUT_RESOLUTION_SUMMARY, BluePrintConstants.DATA_TYPE_BOOLEAN,
                    false, "Enables ResolutionSummary output"
                )

                property(
                    ResourceResolutionComponent.INPUT_OCCURRENCE, BluePrintConstants.DATA_TYPE_INTEGER,
                    false, "Number of time to perform the resolution."
                ) {
                    defaultValue(1)
                }

                property(
                    ResourceResolutionComponent.INPUT_STORE_RESULT, BluePrintConstants.DATA_TYPE_BOOLEAN,
                    false, "Whether or not to store the output."
                )

                property(
                    ResourceResolutionComponent.INPUT_RESOURCE_TYPE, BluePrintConstants.DATA_TYPE_STRING,
                    false, "Request type."
                )

                property(
                    ResourceResolutionComponent.INPUT_ARTIFACT_PREFIX_NAMES, BluePrintConstants.DATA_TYPE_LIST,
                    true, "Template , Resource Assignment Artifact Prefix names"
                ) {
                    entrySchema(BluePrintConstants.DATA_TYPE_STRING)
                }
            }
            outputs {
                property(
                    ResourceResolutionComponent.OUTPUT_RESOURCE_ASSIGNMENT_PARAMS, BluePrintConstants.DATA_TYPE_STRING,
                    true, "Output Response"
                )
                property(
                    ResourceResolutionComponent.OUTPUT_RESOURCE_ASSIGNMENT_MAP, BluePrintConstants.DATA_TYPE_MAP,
                    true, "Output Resolved Values"
                )
                property(
                    ResourceResolutionComponent.OUTPUT_STATUS, BluePrintConstants.DATA_TYPE_STRING,
                    true, "Status of the Component Execution ( success or failure )"
                )
            }
        }
    }
}

/** Component Builder */
fun TopologyTemplateBuilder.nodeTemplateComponentResourceResolution(
    id: String,
    description: String,
    block: ComponentResourceResolutionNodeTemplateBuilder.() -> Unit
) {
    val nodeTemplate = BluePrintTypes.nodeTemplateComponentResourceResolution(
        id, description,
        block
    )
    if (nodeTemplates == null) nodeTemplates = hashMapOf()
    nodeTemplates!![nodeTemplate.id!!] = nodeTemplate
}

fun BluePrintTypes.nodeTemplateComponentResourceResolution(
    id: String,
    description: String,
    block: ComponentResourceResolutionNodeTemplateBuilder.() -> Unit
): NodeTemplate {
    return ComponentResourceResolutionNodeTemplateBuilder(id, description).apply(block).build()
}

class ComponentResourceResolutionNodeTemplateBuilder(id: String, description: String) :
    AbstractNodeTemplateOperationImplBuilder<PropertiesAssignmentBuilder,
        ComponentResourceResolutionNodeTemplateBuilder.InputsBuilder,
        ComponentResourceResolutionNodeTemplateBuilder.OutputsBuilder>(
        id, "component-script-executor",
        "ComponentResourceResolution",
        description
    ) {

    class InputsBuilder : PropertiesAssignmentBuilder() {

        fun requestId(requestId: String) = requestId(requestId.asJsonPrimitive())

        fun requestId(requestId: JsonNode) {
            property(ResourceResolutionComponent.INPUT_REQUEST_ID, requestId)
        }

        fun resourceId(resourceId: String) = resourceId(resourceId.asJsonPrimitive())

        fun resourceId(resourceId: JsonNode) {
            property(ResourceResolutionComponent.INPUT_RESOURCE_ID, resourceId)
        }

        fun actionName(actionName: String) = actionName(actionName.asJsonPrimitive())

        fun actionName(actionName: JsonNode) {
            property(ResourceResolutionComponent.INPUT_ACTION_NAME, actionName)
        }

        fun resolutionKey(resolutionKey: String) = resolutionKey(resolutionKey.asJsonPrimitive())

        fun resolutionKey(resolutionKey: JsonNode) {
            property(ResourceResolutionComponent.INPUT_RESOLUTION_KEY, resolutionKey)
        }

        fun resolutionSummary(resolutionSummary: Boolean) = resolutionSummary(resolutionSummary.asJsonPrimitive())

        fun resolutionSummary(resolutionSummary: JsonNode) {
            property(ResourceResolutionComponent.INPUT_RESOLUTION_SUMMARY, resolutionSummary)
        }

        fun dynamicProperties(dynamicProperties: String) = dynamicProperties(dynamicProperties.asJsonType())

        fun dynamicProperties(dynamicProperties: JsonNode) {
            property(ResourceResolutionComponent.INPUT_DYNAMIC_PROPERTIES, dynamicProperties)
        }

        fun occurrence(occurrence: Int) = occurrence(occurrence.asJsonPrimitive())

        fun occurrence(resolutionKey: JsonNode) {
            property(ResourceResolutionComponent.INPUT_OCCURRENCE, resolutionKey)
        }

        fun storeResult(storeResult: Boolean) = storeResult(storeResult.asJsonPrimitive())

        fun storeResult(storeResult: JsonNode) {
            property(ResourceResolutionComponent.INPUT_STORE_RESULT, storeResult)
        }

        fun resourceType(resourceType: String) = resourceType(resourceType.asJsonPrimitive())

        fun resourceType(resourceType: JsonNode) {
            property(ResourceResolutionComponent.INPUT_RESOURCE_TYPE, resourceType)
        }

        fun artifactPrefixNames(artifactPrefixNames: String) = artifactPrefixNames(artifactPrefixNames.jsonAsJsonType())

        fun artifactPrefixNames(artifactPrefixNameList: List<String>) =
            artifactPrefixNames(artifactPrefixNameList.asJsonString())

        fun artifactPrefixNames(artifactPrefixNames: JsonNode) {
            property(ResourceResolutionComponent.INPUT_ARTIFACT_PREFIX_NAMES, artifactPrefixNames)
        }
    }

    class OutputsBuilder : PropertiesAssignmentBuilder() {

        fun status(status: String) = status(status.asJsonPrimitive())

        fun status(status: JsonNode) {
            property(ResourceResolutionComponent.OUTPUT_STATUS, status)
        }

        fun resourceAssignmentMap(resourceAssignmentMap: String) =
            resourceAssignmentMap(resourceAssignmentMap.asJsonType())

        fun resourceAssignmentMap(resourceAssignmentMap: JsonNode) {
            property(ResourceResolutionComponent.OUTPUT_RESOURCE_ASSIGNMENT_MAP, resourceAssignmentMap)
        }

        fun resourceAssignmentParams(resourceAssignmentParams: String) =
            resourceAssignmentParams(resourceAssignmentParams.asJsonType())

        fun resourceAssignmentParams(resourceAssignmentParams: JsonNode) {
            property(ResourceResolutionComponent.OUTPUT_RESOURCE_ASSIGNMENT_PARAMS, resourceAssignmentParams)
        }
    }
}

/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.AbstractNodeTemplateOperationImplBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertiesAssignmentBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.ServiceTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.TopologyTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.nodeType

/** Component Extensions **/
fun ServiceTemplateBuilder.nodeTypeComponentRemoteScriptExecutor() {
    val nodeType = BlueprintTypes.nodeTypeComponentRemoteScriptExecutor()
    if (this.nodeTypes == null) this.nodeTypes = hashMapOf()
    this.nodeTypes!![nodeType.id!!] = nodeType
}

fun BlueprintTypes.nodeTypeComponentRemoteScriptExecutor(): NodeType {
    return nodeType(
        id = "component-remote-script-executor", version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_NODE_COMPONENT,
        description = "Generic Remote Script Component Executor"
    ) {
        /** Attribute definitions */
        attribute(
            ComponentRemoteScriptExecutor.ATTRIBUTE_RESPONSE_DATA, BlueprintConstants.DATA_TYPE_JSON, false,
            "Remote executed response data."
        )
        attribute(
            ComponentRemoteScriptExecutor.ATTRIBUTE_STATUS, BlueprintConstants.DATA_TYPE_STRING, true,
            "Remote execution status."
        )

        /** Operation definitions */
        operation("ComponentRemoteScriptExecutor", "ComponentRemoteScriptExecutor Operation") {
            inputs {
                property(
                    ComponentRemoteScriptExecutor.INPUT_SELECTOR, BlueprintConstants.DATA_TYPE_JSON,
                    true, "Remote GRPC selector or DSL reference or GRPC Json config."
                )
                property(
                    ComponentRemoteScriptExecutor.INPUT_BLUEPRINT_NAME, BlueprintConstants.DATA_TYPE_STRING,
                    true, "Blueprint name."
                )
                property(
                    ComponentRemoteScriptExecutor.INPUT_BLUEPRINT_VERSION, BlueprintConstants.DATA_TYPE_STRING,
                    true, "Blueprint version."
                )
                property(
                    ComponentRemoteScriptExecutor.INPUT_BLUEPRINT_ACTION, BlueprintConstants.DATA_TYPE_STRING,
                    true, "Blueprint action name."
                )
                property(
                    ComponentRemoteScriptExecutor.INPUT_TIMEOUT, BlueprintConstants.DATA_TYPE_INTEGER,
                    true, "Remote execution timeout in sec."
                ) {
                    defaultValue(180)
                }
                property(
                    ComponentRemoteScriptExecutor.INPUT_REQUEST_DATA, BlueprintConstants.DATA_TYPE_JSON,
                    false, "Dynamic Json Content or DSL Json reference."
                )
            }
            outputs {
                property(
                    ComponentRemoteScriptExecutor.OUTPUT_STATUS, BlueprintConstants.DATA_TYPE_STRING,
                    true, "Status of the Component Execution ( success or failure )"
                )
            }
        }
    }
}

/** Component Builder */
fun TopologyTemplateBuilder.nodeTemplateComponentRemoteScriptExecutor(
    id: String,
    description: String,
    block: ComponentRemoteScriptExecutorNodeTemplateBuilder.() -> Unit
) {
    val nodeTemplate = BlueprintTypes.nodeTemplateComponentRemoteScriptExecutor(
        id, description,
        block
    )
    if (nodeTemplates == null) nodeTemplates = hashMapOf()
    nodeTemplates!![nodeTemplate.id!!] = nodeTemplate
}

fun BlueprintTypes.nodeTemplateComponentRemoteScriptExecutor(
    id: String,
    description: String,
    block: ComponentRemoteScriptExecutorNodeTemplateBuilder.() -> Unit
): NodeTemplate {
    return ComponentRemoteScriptExecutorNodeTemplateBuilder(id, description).apply(block).build()
}

class ComponentRemoteScriptExecutorNodeTemplateBuilder(id: String, description: String) :
    AbstractNodeTemplateOperationImplBuilder<PropertiesAssignmentBuilder,
        ComponentRemoteScriptExecutorNodeTemplateBuilder.InputsBuilder,
        ComponentRemoteScriptExecutorNodeTemplateBuilder.OutputsBuilder>(
        id, "component-remote-script-executor",
        "ComponentRemoteScriptExecutor",
        description
    ) {

    class InputsBuilder : PropertiesAssignmentBuilder() {

        fun selector(selector: String) = selector(selector.asJsonPrimitive())

        fun selector(selector: JsonNode) = property(ComponentRemoteScriptExecutor.INPUT_SELECTOR, selector)

        fun blueprintName(blueprintName: String) = property(
            ComponentRemoteScriptExecutor.INPUT_BLUEPRINT_NAME,
            blueprintName.asJsonPrimitive()
        )

        fun blueprintVersion(blueprintVersion: String) = property(
            ComponentRemoteScriptExecutor.INPUT_BLUEPRINT_VERSION,
            blueprintVersion.asJsonPrimitive()
        )

        fun blueprintAction(blueprintAction: String) = property(
            ComponentRemoteScriptExecutor.INPUT_BLUEPRINT_ACTION,
            blueprintAction.asJsonPrimitive()
        )

        fun timeout(timeout: Int) = property(
            ComponentRemoteScriptExecutor.INPUT_TIMEOUT,
            timeout.asJsonPrimitive()
        )

        fun requestData(requestData: String) = requestData(requestData.asJsonType())

        fun requestData(requestData: JsonNode) {
            property(ComponentRemoteScriptExecutor.INPUT_REQUEST_DATA, requestData)
        }
    }

    class OutputsBuilder : PropertiesAssignmentBuilder() {

        fun status(status: String) = status(status.asJsonPrimitive())

        fun status(status: JsonNode) {
            property(ComponentRemoteScriptExecutor.OUTPUT_STATUS, status)
        }
    }
}

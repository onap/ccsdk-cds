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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.AbstractNodeTemplateOperationImplBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertiesAssignmentBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.nodeType
import kotlin.reflect.KClass

/** Component Extensions **/

fun BluePrintTypes.nodeTypeComponentScriptExecutor(): NodeType {
    return nodeType(
        id = "component-script-executor", version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_NODE_COMPONENT,
        description = "Generic Script Component Executor"
    ) {
        attribute(ComponentScriptExecutor.ATTRIBUTE_RESPONSE_DATA, BluePrintConstants.DATA_TYPE_JSON, false)
        attribute(ComponentScriptExecutor.ATTRIBUTE_STATUS, BluePrintConstants.DATA_TYPE_STRING, true)

        operation("ComponentScriptExecutor", "ComponentScriptExecutor Operation") {
            inputs {
                property(
                    ComponentScriptExecutor.INPUT_SCRIPT_TYPE, BluePrintConstants.DATA_TYPE_STRING,
                    true, "Script Type"
                ) {
                    defaultValue(BluePrintConstants.SCRIPT_INTERNAL)
                    constrain {
                        validValues(
                            listOf(
                                BluePrintConstants.SCRIPT_INTERNAL.asJsonPrimitive(),
                                BluePrintConstants.SCRIPT_JYTHON.asJsonPrimitive(),
                                BluePrintConstants.SCRIPT_KOTLIN.asJsonPrimitive()
                            )
                        )
                    }
                }
                property(
                    ComponentScriptExecutor.INPUT_SCRIPT_CLASS_REFERENCE, BluePrintConstants.DATA_TYPE_STRING,
                    true, "Kotlin Script class name or jython script name."
                )
                property(
                    ComponentScriptExecutor.INPUT_DYNAMIC_PROPERTIES, BluePrintConstants.DATA_TYPE_JSON,
                    false, "Dynamic Json Content or DSL Json reference."
                )
            }
            outputs {
                property(
                    ComponentScriptExecutor.OUTPUT_RESPONSE_DATA, BluePrintConstants.DATA_TYPE_JSON,
                    false, "Output Response"
                )
                property(
                    ComponentScriptExecutor.OUTPUT_STATUS, BluePrintConstants.DATA_TYPE_STRING,
                    true, "Status of the Component Execution ( success or failure )"
                )
            }
        }
    }
}

/** Component Builder */
fun BluePrintTypes.nodeTemplateComponentScriptExecutor(
    id: String,
    description: String,
    block: ComponentScriptExecutorNodeTemplateBuilder.() -> Unit
):
        NodeTemplate {
    return ComponentScriptExecutorNodeTemplateBuilder(id, description).apply(block).build()
}

class ComponentScriptExecutorNodeTemplateBuilder(id: String, description: String) :
    AbstractNodeTemplateOperationImplBuilder<PropertiesAssignmentBuilder,
            ComponentScriptExecutorNodeTemplateBuilder.InputsBuilder,
            ComponentScriptExecutorNodeTemplateBuilder.OutputsBuilder>(
        id, "component-script-executor",
        "ComponentScriptExecutor",
        description
    ) {

    class InputsBuilder : PropertiesAssignmentBuilder() {

        fun type(type: String) = type(type.asJsonPrimitive())

        fun type(type: JsonNode) {
            property(ComponentScriptExecutor.INPUT_SCRIPT_TYPE, type)
        }

        fun scriptClassReference(scriptClassReference: KClass<*>) {
            scriptClassReference(scriptClassReference.qualifiedName!!)
        }

        fun scriptClassReference(scriptClassReference: String) = scriptClassReference(scriptClassReference.asJsonPrimitive())

        fun scriptClassReference(scriptClassReference: JsonNode) {
            property(ComponentScriptExecutor.INPUT_SCRIPT_CLASS_REFERENCE, scriptClassReference)
        }

        fun dynamicProperties(dynamicProperties: String) = dynamicProperties(dynamicProperties.asJsonType())

        fun dynamicProperties(dynamicProperties: JsonNode) {
            property(ComponentScriptExecutor.INPUT_DYNAMIC_PROPERTIES, dynamicProperties)
        }
    }

    class OutputsBuilder : PropertiesAssignmentBuilder() {

        fun status(status: String) = status(status.asJsonPrimitive())

        fun status(status: JsonNode) {
            property(ComponentScriptExecutor.OUTPUT_STATUS, status)
        }

        fun responseData(responseData: String) = responseData(responseData.asJsonType())

        fun responseData(responseData: JsonNode) {
            property(ComponentScriptExecutor.OUTPUT_RESPONSE_DATA, responseData)
        }
    }
}

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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.AbstractNodeTemplateOperationImplBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertiesAssignmentBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.dataType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.nodeType
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils

/** Component Extensions **/
fun BluePrintTypes.nodeTypeComponentRemotePythonExecutor(): NodeType {
    return nodeType(
        id = "component-remote-python-executor", version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_NODE_COMPONENT,
        description = "This is Remote Python Execution Component."
    ) {

        attribute(
            ComponentRemotePythonExecutor.ATTRIBUTE_PREPARE_ENV_LOG, BluePrintConstants.DATA_TYPE_STRING,
            false
        )
        attribute(
            ComponentRemotePythonExecutor.ATTRIBUTE_EXEC_CMD_LOG, BluePrintConstants.DATA_TYPE_LIST,
            false
        ) {
            entrySchema(BluePrintConstants.DATA_TYPE_STRING)
        }
        attribute(
            ComponentRemotePythonExecutor.ATTRIBUTE_RESPONSE_DATA, BluePrintConstants.DATA_TYPE_JSON,
            false
        )

        operation("ComponentRemotePythonExecutor", "ComponentRemotePythonExecutor Operation") {
            inputs {
                property(
                    ComponentRemotePythonExecutor.INPUT_ENDPOINT_SELECTOR, BluePrintConstants.DATA_TYPE_STRING,
                    false, "Remote Container or Server selector name."
                ) {
                    defaultValue(ComponentRemotePythonExecutor.DEFAULT_SELECTOR)
                }
                property(
                    ComponentRemotePythonExecutor.INPUT_DYNAMIC_PROPERTIES, BluePrintConstants.DATA_TYPE_JSON,
                    false, "Dynamic Json Content or DSL Json reference."
                )

                property(
                    ComponentRemotePythonExecutor.INPUT_ARGUMENT_PROPERTIES, BluePrintConstants.DATA_TYPE_JSON,
                    false, "Argument Json Content or DSL Json reference."
                )

                property(
                    ComponentRemotePythonExecutor.INPUT_COMMAND, BluePrintConstants.DATA_TYPE_STRING,
                    true, "Command to execute."
                )

                property(
                    ComponentRemotePythonExecutor.INPUT_PACKAGES, BluePrintConstants.DATA_TYPE_LIST,
                    false, "Packages to install based on type."
                ) {
                    entrySchema("dt-system-packages")
                }
            }
        }
    }
}

fun BluePrintTypes.dataTypeDtSystemPackages(): DataType {
    return dataType(
        id = "dt-system-packages", version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT,
        description = "This represent System Package Data Type"
    ) {
        property("type", BluePrintConstants.DATA_TYPE_LIST, true, "") {
            constrain {
                entrySchema(BluePrintConstants.DATA_TYPE_STRING)
                validValues(arrayListOf("ansible_galaxy".asJsonPrimitive(), "pip".asJsonPrimitive()))
            }
        }
        property("package", BluePrintConstants.DATA_TYPE_LIST, true, "") {
            entrySchema(BluePrintConstants.DATA_TYPE_STRING)
        }
    }
}

/** Component Builder */
fun BluePrintTypes.nodeTemplateComponentRemotePythonExecutor(
    id: String,
    description: String,
    block: ComponentRemotePythonExecutorNodeTemplateBuilder.() -> Unit
):
    NodeTemplate {
        return ComponentRemotePythonExecutorNodeTemplateBuilder(id, description).apply(block).build()
    }

class DtSystemPackageDataTypeBuilder : PropertiesAssignmentBuilder() {

    fun type(type: String) = type(type.asJsonPrimitive())

    fun type(type: JsonNode) {
        property("type", type)
    }

    fun packages(packages: String) = packages(packages.asJsonType())

    fun packages(packages: List<String>) = packages(packages.asJsonType())

    fun packages(packages: JsonNode) {
        property("package", packages)
    }
}

class ComponentRemotePythonExecutorNodeTemplateBuilder(id: String, description: String) :
    AbstractNodeTemplateOperationImplBuilder<PropertiesAssignmentBuilder, ComponentRemotePythonExecutorNodeTemplateBuilder.InputsBuilder,
        ComponentRemotePythonExecutorNodeTemplateBuilder.OutputsBuilder>(
        id, "component-remote-python-executor",
        "ComponentRemotePythonExecutor", description
    ) {

    class InputsBuilder : PropertiesAssignmentBuilder() {

        private var packageList: ArrayNode? = null

        fun endpointSelector(endpointSelector: String) = endpointSelector(endpointSelector.asJsonPrimitive())

        fun endpointSelector(endpointSelector: JsonNode) {
            property(ComponentRemotePythonExecutor.INPUT_ENDPOINT_SELECTOR, endpointSelector)
        }

        fun dynamicProperties(dynamicProperties: String) = dynamicProperties(dynamicProperties.asJsonType())

        fun dynamicProperties(dynamicProperties: JsonNode) {
            property(ComponentRemotePythonExecutor.INPUT_DYNAMIC_PROPERTIES, dynamicProperties)
        }

        fun argumentProperties(argumentProperties: String) = argumentProperties(argumentProperties.asJsonType())

        fun argumentProperties(argumentProperties: JsonNode) {
            property(ComponentRemotePythonExecutor.INPUT_ARGUMENT_PROPERTIES, argumentProperties)
        }

        fun command(command: String) = command(command.asJsonPrimitive())

        fun command(command: JsonNode) {
            property(ComponentRemotePythonExecutor.INPUT_COMMAND, command)
        }

        fun packages(block: DtSystemPackageDataTypeBuilder.() -> Unit) {
            if (packageList == null)
                packageList = JacksonUtils.objectMapper.createArrayNode()
            val dtSysyemPackagePropertyAssignments = DtSystemPackageDataTypeBuilder().apply(block).build()
            packageList!!.add(dtSysyemPackagePropertyAssignments.asJsonType())
        }

        override fun build(): MutableMap<String, JsonNode> {
            val propertyAssignments = super.build()
            if (packageList != null) {
                propertyAssignments[ComponentRemotePythonExecutor.INPUT_PACKAGES] = packageList!!
            }
            return propertyAssignments
        }
    }

    class OutputsBuilder : PropertiesAssignmentBuilder()
}

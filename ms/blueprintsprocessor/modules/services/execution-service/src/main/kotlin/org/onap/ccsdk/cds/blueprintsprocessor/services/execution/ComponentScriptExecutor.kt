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
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.Implementation
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.ArtifactDefinitionBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.nodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.nodeType
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * This is generic Script Component Executor function
 * @author Brinda Santh
 */
@Component("component-script-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentScriptExecutor(private var componentFunctionScriptingService: ComponentFunctionScriptingService)
    : AbstractComponentFunction() {

    companion object {
        const val SCRIPT_TYPE = "script-type"
        const val SCRIPT_CLASS_REFERENCE = "script-class-reference"
        const val DYNAMIC_PROPERTIES = "dynamic-properties"
        const val RESPONSE_DATA = "response-data"
        const val STATUS = "status"
    }

    lateinit var scriptComponentFunction: AbstractScriptComponentFunction

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        val scriptType = operationInputs.getAsString(SCRIPT_TYPE)
        val scriptClassReference = operationInputs.getAsString(SCRIPT_CLASS_REFERENCE)

        val scriptDependencies: MutableList<String> = arrayListOf()
        populateScriptDependencies(scriptDependencies)

        scriptComponentFunction = componentFunctionScriptingService.scriptInstance(this, scriptType,
                scriptClassReference, scriptDependencies)

        // Handles both script processing and error handling
        scriptComponentFunction.executeScript(executionServiceInput)
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBluePrintError()
                .addError("Failed in ComponentCliExecutor : ${runtimeException.message}")

    }

    open fun populateScriptDependencies(scriptDependencies: MutableList<String>) {
        /** Place holder for Child to add extra dependencies */
    }
}

/** Component Extensions **/

fun BluePrintTypes.componentScriptExecutor(): NodeType {
    return nodeType(id = "component-script-executor", version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_NODE_COMPONENT,
            description = "Generic Script Component Executor") {
        attribute(ComponentScriptExecutor.RESPONSE_DATA, BluePrintConstants.DATA_TYPE_JSON, false)
        attribute(ComponentScriptExecutor.STATUS, BluePrintConstants.DATA_TYPE_STRING, true)

        operation("ComponentScriptExecutor", "ComponentScriptExecutor Operation") {
            inputs {
                property(ComponentScriptExecutor.SCRIPT_TYPE, BluePrintConstants.DATA_TYPE_STRING, true,
                        "Script Type") {
                    defaultValue(BluePrintConstants.SCRIPT_INTERNAL)
                    constrain {
                        validValues(listOf(BluePrintConstants.SCRIPT_INTERNAL.asJsonPrimitive(),
                                BluePrintConstants.SCRIPT_JYTHON.asJsonPrimitive(),
                                BluePrintConstants.SCRIPT_KOTLIN.asJsonPrimitive()))
                    }
                }
                property(ComponentScriptExecutor.SCRIPT_CLASS_REFERENCE, BluePrintConstants.DATA_TYPE_STRING,
                        true, "Kotlin Script class name or jython script name.")
                property(ComponentScriptExecutor.DYNAMIC_PROPERTIES, BluePrintConstants.DATA_TYPE_JSON, false,
                        "Dynamic Json Content or DSL Json reference.")
            }
            outputs {
                property(ComponentScriptExecutor.RESPONSE_DATA, BluePrintConstants.DATA_TYPE_JSON, false,
                        "Output Response")
                property(ComponentScriptExecutor.STATUS, BluePrintConstants.DATA_TYPE_STRING, true,
                        "Status of the Component Execution ( success or failure )")
            }
        }
    }
}

/** Component Builder */

fun componentScriptExecutor(id: String, description: String,
                            block: ComponentScriptExecutorBuilder.() -> Unit): NodeTemplate {
    return ComponentScriptExecutorBuilder(id, description).apply(block).build()
}

class ComponentScriptExecutorBuilder(private val id: String, private val description: String) {
    private var implementation: Implementation? = null
    private var inputs: MutableMap<String, JsonNode>? = null
    private var outputs: MutableMap<String, JsonNode>? = null
    private var artifacts: MutableMap<String, ArtifactDefinition>? = null

    fun implementation(timeout: Int, operationHost: String? = BluePrintConstants.PROPERTY_SELF) {
        val implementation = Implementation().apply {
            this.operationHost = operationHost!!
            this.timeout = timeout
        }
        this.implementation = implementation
    }

    fun inputs(block: InputAssignmentBuilder.() -> Unit) {
        this.inputs = InputAssignmentBuilder().apply(block).build()
    }

    fun outputs(block: OutputAssignmentBuilder.() -> Unit) {
        this.outputs = OutputAssignmentBuilder().apply(block).build()
    }

    fun artifact(id: String, type: String, file: String) {
        if (artifacts == null)
            artifacts = hashMapOf()
        artifacts!![id] = ArtifactDefinitionBuilder(id, type, file).build()
    }

    fun artifact(id: String, type: String, file: String, block: ArtifactDefinitionBuilder.() -> Unit) {
        if (artifacts == null)
            artifacts = hashMapOf()
        artifacts!![id] = ArtifactDefinitionBuilder(id, type, file).apply(block).build()
    }

    fun build(): NodeTemplate {
        return nodeTemplate(id, "component-script-executor", description) {
            operation("ComponentScriptExecutor") {
                implementation(implementation)
                inputs(inputs)
                outputs(outputs)
            }
            artifacts(artifacts)
        }
    }

    class InputAssignmentBuilder {
        val properties: MutableMap<String, JsonNode> = hashMapOf()

        fun type(type: String) {
            properties[ComponentScriptExecutor.SCRIPT_TYPE] = type.asJsonPrimitive()
        }

        fun scriptClassReference(scriptClassReference: String) {
            properties[ComponentScriptExecutor.SCRIPT_CLASS_REFERENCE] = scriptClassReference.asJsonPrimitive()
        }

        fun dynamicProperty(dynamicProperty: Any) {
            dynamicProperty(dynamicProperty.asJsonType())
        }

        fun dynamicProperty(dynamicProperty: JsonNode) {
            properties[ComponentScriptExecutor.DYNAMIC_PROPERTIES] = dynamicProperty
        }

        fun build(): MutableMap<String, JsonNode> {
            return properties
        }
    }

    class OutputAssignmentBuilder {
        val properties: MutableMap<String, JsonNode> = hashMapOf()

        fun status(status: String) {
            properties[ComponentScriptExecutor.STATUS] = status.asJsonPrimitive()
        }

        fun responseData(responseData: Any) {
            responseData(responseData.asJsonType())
        }

        fun responseData(responseData: JsonNode) {
            properties[ComponentScriptExecutor.RESPONSE_DATA] = responseData
        }

        fun build(): MutableMap<String, JsonNode> {
            return properties
        }
    }
}
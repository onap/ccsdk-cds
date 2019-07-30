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
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.AbstractNodeTemplateImplBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertiesAssignmentBuilder
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

fun BluePrintTypes.nodeTemplateComponentScriptExecutor(): NodeType {
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
fun BluePrintTypes.nodeTemplateComponentScriptExecutor(id: String,
                                                       description: String,
                                                       block: ComponentScriptExecutorNodeTemplateImplBuilder.() -> Unit)
        : NodeTemplate {
    return ComponentScriptExecutorNodeTemplateImplBuilder(id, description).apply(block).build()
}

class ComponentScriptExecutorNodeTemplateImplBuilder(id: String, description: String) :
        AbstractNodeTemplateImplBuilder<ComponentScriptExecutorInputAssignmentBuilder,
                ComponentScriptExecutorOutputAssignmentBuilder>(id, "component-script-executor",
                "ComponentScriptExecutor",
                description)

class ComponentScriptExecutorInputAssignmentBuilder : PropertiesAssignmentBuilder() {

    fun type(type: String) {
        type(type.asJsonPrimitive())
    }

    fun type(type: JsonNode) {
        property(ComponentScriptExecutor.SCRIPT_TYPE, type)
    }

    fun scriptClassReference(scriptClassReference: String) {
        scriptClassReference(scriptClassReference.asJsonPrimitive())
    }

    fun scriptClassReference(scriptClassReference: JsonNode) {
        property(ComponentScriptExecutor.SCRIPT_CLASS_REFERENCE, scriptClassReference)
    }

    fun dynamicProperty(dynamicProperty: String) {
        dynamicProperty(dynamicProperty.asJsonType())
    }

    fun dynamicProperty(dynamicProperty: JsonNode) {
        property(ComponentScriptExecutor.DYNAMIC_PROPERTIES, dynamicProperty)
    }
}

class ComponentScriptExecutorOutputAssignmentBuilder : PropertiesAssignmentBuilder() {

    fun status(status: String) {
        status(status.asJsonPrimitive())
    }

    fun status(status: JsonNode) {
        property(ComponentScriptExecutor.STATUS, status)
    }

    fun responseData(responseData: String) {
        responseData(responseData.asJsonType())
    }

    fun responseData(responseData: JsonNode) {
        property(ComponentScriptExecutor.RESPONSE_DATA, responseData)
    }
}
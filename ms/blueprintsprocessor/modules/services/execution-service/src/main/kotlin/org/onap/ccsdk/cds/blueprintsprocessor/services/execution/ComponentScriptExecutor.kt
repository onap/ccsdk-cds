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

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.core.getAsString
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * This is generic Script Component Executor function
 * @author Brinda Santh
 */
@Component("component-script-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentScriptExecutor(private var componentFunctionScriptingService: ComponentFunctionScriptingService) :
    AbstractComponentFunction() {

    companion object {

        const val INPUT_SCRIPT_TYPE = "script-type"
        const val INPUT_SCRIPT_CLASS_REFERENCE = "script-class-reference"
        const val INPUT_DYNAMIC_PROPERTIES = "dynamic-properties"

        const val ATTRIBUTE_RESPONSE_DATA = "response-data"
        const val ATTRIBUTE_STATUS = "status"

        const val OUTPUT_RESPONSE_DATA = "response-data"
        const val OUTPUT_STATUS = "status"
    }

    lateinit var scriptComponentFunction: AbstractScriptComponentFunction

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        val scriptType = operationInputs.getAsString(INPUT_SCRIPT_TYPE)
        val scriptClassReference = operationInputs.getAsString(INPUT_SCRIPT_CLASS_REFERENCE)

        val scriptDependencies: MutableList<String> = arrayListOf()
        populateScriptDependencies(scriptDependencies)

        scriptComponentFunction = componentFunctionScriptingService.scriptInstance(
            this, scriptType,
            scriptClassReference, scriptDependencies
        )

        // Handles both script processing and error handling
        scriptComponentFunction.executeScript(executionServiceInput)
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBluePrintError()
            .addError("Failed in ComponentScriptExecutor : ${runtimeException.message}")
    }

    open fun populateScriptDependencies(scriptDependencies: MutableList<String>) {
        /** Place holder for Child to add extra dependencies */
    }
}

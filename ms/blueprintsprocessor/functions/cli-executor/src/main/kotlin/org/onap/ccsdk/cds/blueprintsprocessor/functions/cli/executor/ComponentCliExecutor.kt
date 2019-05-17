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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.cli.executor

import com.fasterxml.jackson.databind.node.ArrayNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.SshLibConstants
import org.onap.ccsdk.cds.controllerblueprints.core.getAsString
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component("component-cli-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentCliExecutor(private var componentFunctionScriptingService: ComponentFunctionScriptingService)
    : AbstractComponentFunction() {

    companion object {
        const val SCRIPT_TYPE = "script-type"
        const val SCRIPT_CLASS_REFERENCE = "script-class-reference"
        const val INSTANCE_DEPENDENCIES = "instance-dependencies"
        const val RESPONSE_DATA = "response-data"
    }

    private lateinit var scriptComponent: CliComponentFunction

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        val scriptType = operationInputs.getAsString(SCRIPT_TYPE)
        val scriptClassReference = operationInputs.getAsString(SCRIPT_CLASS_REFERENCE)
        val instanceDependenciesNode = operationInputs[INSTANCE_DEPENDENCIES] as? ArrayNode

        val scriptDependencies: MutableList<String> = arrayListOf()
        scriptDependencies.add(SshLibConstants.SERVICE_BLUEPRINT_SSH_LIB_PROPERTY)
        // May be injected from model, not by default
        //scriptDependencies.add(ResourceResolutionConstants.SERVICE_RESOURCE_RESOLUTION)

        instanceDependenciesNode?.forEach { instanceName ->
            scriptDependencies.add(instanceName.textValue())
        }

        scriptComponent = componentFunctionScriptingService.scriptInstance(this, scriptType,
                scriptClassReference, scriptDependencies)


        // Handles both script processing and error handling
        scriptComponent.executeScript(executionServiceInput)
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBluePrintError()
                .addError("Failed in ComponentCliExecutor : ${runtimeException.message}")

    }
}
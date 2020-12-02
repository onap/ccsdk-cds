/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Modifications Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor

import com.fasterxml.jackson.databind.node.ArrayNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.onap.ccsdk.cds.controllerblueprints.core.getAsString
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component("component-netconf-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentNetconfExecutor(private var componentFunctionScriptingService: ComponentFunctionScriptingService) :
    AbstractComponentFunction() {

    companion object {

        const val SCRIPT_TYPE = "script-type"
        const val SCRIPT_CLASS_REFERENCE = "script-class-reference"
        const val INSTANCE_DEPENDENCIES = "instance-dependencies"
    }

    lateinit var scriptComponent: AbstractScriptComponentFunction

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        val scriptType = operationInputs.getAsString(SCRIPT_TYPE)
        val scriptClassReference = operationInputs.getAsString(SCRIPT_CLASS_REFERENCE)
        val instanceDependenciesNode = operationInputs.get(INSTANCE_DEPENDENCIES) as? ArrayNode

        val scriptDependencies: MutableList<String> = arrayListOf()
        scriptDependencies.add(ResourceResolutionConstants.SERVICE_RESOURCE_RESOLUTION)

        instanceDependenciesNode?.forEach { instanceName ->
            scriptDependencies.add(instanceName.textValue())
        }

        scriptComponent = componentFunctionScriptingService
                .scriptInstance<AbstractScriptComponentFunction>(
                    this, scriptType,
                    scriptClassReference, scriptDependencies
                )

        checkNotNull(scriptComponent) { "failed to get netconf script component" }

        // Handles both script processing and error handling
        scriptComponent.executeScript(executionServiceInput)
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBluePrintError()
            .addError("Failed in ComponentNetconfExecutor : ${runtimeException.message}")
    }
}

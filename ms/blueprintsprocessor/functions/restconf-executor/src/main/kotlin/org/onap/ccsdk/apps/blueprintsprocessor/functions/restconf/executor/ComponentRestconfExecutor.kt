/*
 *  Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.restconf.executor

import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.BlueprintJythonService
import org.onap.ccsdk.apps.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.apps.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.getAsString
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintScriptsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component("component-restconf-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentRestconfExecutor(private var applicationContext: ApplicationContext,
                                     private val blueprintJythonService: BlueprintJythonService,
                                     private val bluePrintScriptsService: BluePrintScriptsService,
                                     private var bluePrintRestLibPropertyService: BluePrintRestLibPropertyService) :
        AbstractComponentFunction() {

    private val log = LoggerFactory.getLogger(ComponentRestconfExecutor::class.java)

    lateinit var scriptComponent: RestconfComponentFunction

    companion object {
        const val SCRIPT_TYPE = "script-type"
        const val SCRIPT_CLASS_REFERENCE = "script-class-reference"
    }

    override fun process(executionRequest: ExecutionServiceInput) {

        val scriptType = operationInputs.getAsString(SCRIPT_TYPE)
        val scriptClassReference = operationInputs.getAsString(SCRIPT_CLASS_REFERENCE)
        /**
         * Populate the Script Instance based on the Type
         */
        restconfComponentFunction(scriptType, scriptClassReference)
        checkNotNull(scriptComponent) { "failed to get restconf script component" }

        scriptComponent.bluePrintRuntimeService = bluePrintRuntimeService
        scriptComponent.processId = processId
        scriptComponent.workflowName = workflowName
        scriptComponent.stepName = stepName
        scriptComponent.interfaceName = interfaceName
        scriptComponent.operationName = operationName
        scriptComponent.nodeTemplateName = nodeTemplateName
        scriptComponent.operationInputs = operationInputs

        // Set the Rest Lib Property Service
        scriptComponent.bluePrintRestLibPropertyService = bluePrintRestLibPropertyService

        scriptComponent.process(executionServiceInput)
    }

    override fun recover(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        scriptComponent.recover(runtimeException, executionRequest)
    }

    fun restconfComponentFunction(scriptType: String, scriptClassReference: String): RestconfComponentFunction {
        log.info("processing restconf script type($scriptType), reference name($scriptClassReference)")
        when (scriptType) {
            BluePrintConstants.SCRIPT_INTERNAL -> {
                scriptComponent = bluePrintScriptsService.scriptInstance<RestconfComponentFunction>(scriptClassReference)
            }
            BluePrintConstants.SCRIPT_KOTLIN -> {
                scriptComponent = bluePrintScriptsService.scriptInstance<RestconfComponentFunction>(bluePrintRuntimeService
                        .bluePrintContext(), scriptClassReference, false)
            }
            BluePrintConstants.SCRIPT_JYTHON -> {
                scriptComponent = blueprintJythonService.jythonComponentInstance(this) as RestconfComponentFunction
            }
            else -> {
                throw BluePrintProcessorException("script type($scriptType) is not supported")
            }
        }
        return scriptComponent
    }
}
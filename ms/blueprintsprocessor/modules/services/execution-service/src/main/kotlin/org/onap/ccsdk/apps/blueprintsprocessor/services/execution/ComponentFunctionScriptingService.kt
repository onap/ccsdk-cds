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

package org.onap.ccsdk.apps.blueprintsprocessor.services.execution

import org.onap.ccsdk.apps.blueprintsprocessor.services.execution.scripts.BlueprintJythonService
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintScriptsService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class ComponentFunctionScriptingService(private val applicationContext: ApplicationContext,
                                        private val bluePrintScriptsService: BluePrintScriptsService,
                                        private val blueprintJythonService: BlueprintJythonService) {

    private val log = LoggerFactory.getLogger(ComponentFunctionScriptingService::class.java)

    fun <T : AbstractComponentFunction> scriptInstance(componentFunction: AbstractComponentFunction, scriptType: String,
                                                       scriptClassReference: String,
                                                       instanceDependencies: MutableList<String>): T {
        log.info("creating component function of script type($scriptType), reference name($scriptClassReference) and " +
                "instanceDependencies($instanceDependencies)")

        val scriptComponent: T = scriptInstance(componentFunction.bluePrintRuntimeService.bluePrintContext(),
                scriptType, scriptClassReference)
        populateScriptDependencies(scriptComponent, instanceDependencies)
        return scriptComponent
    }


    fun <T : AbstractComponentFunction> scriptInstance(bluePrintContext: BluePrintContext, scriptType: String,
                                                       scriptClassReference: String): T {
        var scriptComponent: T? = null

        when (scriptType) {
            BluePrintConstants.SCRIPT_INTERNAL -> {
                scriptComponent = bluePrintScriptsService.scriptInstance<T>(scriptClassReference)
            }
            BluePrintConstants.SCRIPT_KOTLIN -> {
                scriptComponent = bluePrintScriptsService.scriptInstance<T>(bluePrintContext, scriptClassReference, false)
            }
            BluePrintConstants.SCRIPT_JYTHON -> {
                scriptComponent = blueprintJythonService.jythonComponentInstance(bluePrintContext, scriptClassReference) as T
            }
            else -> {
                throw BluePrintProcessorException("script type($scriptType) is not supported")
            }
        }
        return scriptComponent
    }


    private fun populateScriptDependencies(componentFunction: AbstractComponentFunction,
                                   instanceDependencies: MutableList<String>) {
        instanceDependencies.forEach { instanceDependency ->
            componentFunction.functionDependencyInstances[instanceDependency] = applicationContext
                    .getBean(instanceDependency)
        }
    }
}
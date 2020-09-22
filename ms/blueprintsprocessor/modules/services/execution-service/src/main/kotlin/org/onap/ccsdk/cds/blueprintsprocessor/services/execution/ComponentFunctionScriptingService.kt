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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts.BlueprintJythonService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintScriptsService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BluePrintScriptsServiceImpl
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class ComponentFunctionScriptingService(
    private val applicationContext: ApplicationContext,
    private val blueprintJythonService: BlueprintJythonService
) {

    private val log = LoggerFactory.getLogger(ComponentFunctionScriptingService::class.java)

    suspend fun <T : AbstractScriptComponentFunction> scriptInstance(
        componentFunction: AbstractComponentFunction,
        scriptType: String,
        scriptClassReference: String,
        instanceDependencies: List<String>
    ): T {

        log.info(
            "creating component function of script type($scriptType), reference name($scriptClassReference) and " +
                "instanceDependencies($instanceDependencies)"
        )

        val scriptComponent: T = scriptInstance(
            componentFunction.bluePrintRuntimeService.bluePrintContext(),
            scriptType, scriptClassReference
        )

        checkNotNull(scriptComponent) { "failed to initialize script component" }

        scriptComponent.bluePrintRuntimeService = componentFunction.bluePrintRuntimeService
        scriptComponent.processId = componentFunction.processId
        scriptComponent.workflowName = componentFunction.workflowName
        scriptComponent.stepName = componentFunction.stepName
        scriptComponent.interfaceName = componentFunction.interfaceName
        scriptComponent.operationName = componentFunction.operationName
        scriptComponent.nodeTemplateName = componentFunction.nodeTemplateName
        scriptComponent.operationInputs = componentFunction.operationInputs
        scriptComponent.executionServiceInput = componentFunction.executionServiceInput
        scriptComponent.scriptType = scriptType

        // Populate Instance Properties
        instanceDependencies.forEach { instanceDependency ->
            scriptComponent.functionDependencyInstances[instanceDependency] = applicationContext
                .getBean(instanceDependency)
        }
        return scriptComponent
    }

    suspend fun <T : BlueprintFunctionNode<*, *>> scriptInstance(
        bluePrintContext: BluePrintContext,
        scriptType: String,
        scriptClassReference: String
    ): T {
        var scriptComponent: T? = null

        when (scriptType) {
            BluePrintConstants.SCRIPT_INTERNAL -> {
                val bluePrintScriptsService: BluePrintScriptsService = BluePrintScriptsServiceImpl()
                scriptComponent = bluePrintScriptsService.scriptInstance<T>(scriptClassReference)
            }
            BluePrintConstants.SCRIPT_KOTLIN -> {
                val bluePrintScriptsService: BluePrintScriptsService = BluePrintScriptsServiceImpl()
                scriptComponent = bluePrintScriptsService.scriptInstance<T>(
                    bluePrintContext.rootPath,
                    bluePrintContext.name(), bluePrintContext.version(), scriptClassReference, false
                )
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

    suspend fun cleanupInstance(bluePrintContext: BluePrintContext, scriptType: String) {
        if (scriptType == BluePrintConstants.SCRIPT_KOTLIN) {
            BluePrintScriptsServiceImpl().cleanupInstance(bluePrintContext.rootPath)
        }
    }
}

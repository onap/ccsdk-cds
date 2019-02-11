/*
 * Copyright © 2019 IBM, Bell Canada.
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
package org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor

import org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.plugin.BlueprintPythonHost
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service("jython-executor-service")
class BlueprintPythonService(val pythonExecutorProperty: PythonExecutorProperty){

    val log: Logger = LoggerFactory.getLogger(BlueprintPythonService::class.java)

    inline fun <reified T> jythonInstance(blueprintContext: BluePrintContext, pythonClassName: String, content: String,
                                          dependencyInstanceNames: MutableMap<String, Any>?): T {

        val blueprintBasePath: String = blueprintContext.rootPath
        val pythonPath: MutableList<String> = arrayListOf()
        pythonPath.add(blueprintBasePath)
        pythonPath.addAll(pythonExecutorProperty.modulePaths)

        val blueprintPythonConfigurations = BluePrintPython(pythonExecutorProperty.executionPath, pythonPath, arrayListOf())

        val blueprintPythonHost = BlueprintPythonHost(blueprintPythonConfigurations)
        val pyObject = blueprintPythonHost.getPythonComponent(content, pythonClassName, dependencyInstanceNames)

        log.info("Component Object {}", pyObject)

        return pyObject.__tojava__(T::class.java) as T
    }

}
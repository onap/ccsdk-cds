/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

import org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.utils.PythonExecutorUtils
import org.onap.ccsdk.apps.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class JythonExecutionService(private val pythonExecutorProperty: PythonExecutorProperty) {


    private val log = LoggerFactory.getLogger(ComponentJythonExecutor::class.java)

    @Autowired
    lateinit var applicationContext: ApplicationContext


    fun processJythonNodeTemplate(pythonClassName: String, content: String, pythonPath: MutableList<String>,
                                  jythonContextInstance: MutableMap<String, Any>,
                                  dependencyInstanceNames: List<String>): AbstractComponentFunction {


        dependencyInstanceNames.forEach { instanceName ->
            jythonContextInstance[instanceName] = applicationContext.getBean(instanceName)

        }

        return PythonExecutorUtils.getPythonComponent(pythonExecutorProperty.executionPath,
                pythonPath, content, pythonClassName, jythonContextInstance)

    }

}
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
package org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor.scripts

import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ExecutionServiceDomains
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.updateErrorMessage
import org.python.core.PyObject
import org.python.util.PythonInterpreter

@Deprecated("CDS won't support JythonService")
open class BlueprintPythonHost(private val bluePrintPython: BlueprintPython) {

    private val blueprintPythonInterpreterProxy: BlueprintPythonInterpreterProxy

    init {
        PythonInterpreter.initialize(System.getProperties(), bluePrintPython.props, bluePrintPython.argv.toTypedArray())
        blueprintPythonInterpreterProxy = BlueprintPythonInterpreterProxy(bluePrintPython)
    }

    /**
     * getPythonComponent Purpose: execute the python script and return the python interpreter object
     *
     * @param content String
     * @param interfaceName String
     * @param properties MutableMap<String, Any>
     * @return pyObject PyObject
     */
    fun getPythonComponent(content: String?, interfaceName: String, properties: MutableMap<String, Any>?): PyObject {
        bluePrintPython.content = content!!
        bluePrintPython.pythonClassName = interfaceName
        bluePrintPython.moduleName = "Blueprint Python Script [Class Name = $interfaceName]"
        try {
            return blueprintPythonInterpreterProxy.getPythonInstance(properties)
        } catch (e: BlueprintProcessorException) {
            val errorMsg = "Failed to get python instance."
            throw e.updateErrorMessage(
                ExecutionServiceDomains.PYTHON_EXECUTOR, errorMsg,
                "Error in environment properties"
            )
        } catch (e: Exception) {
            throw BlueprintProcessorException("Failed to execute Jython component $e", e)
        }
    }

    // TODO Check potential errors in python scripts
}

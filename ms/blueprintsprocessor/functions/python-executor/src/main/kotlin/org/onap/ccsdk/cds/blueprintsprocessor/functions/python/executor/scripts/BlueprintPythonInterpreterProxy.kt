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

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.python.core.PyObject
import org.python.core.PySyntaxError
import org.python.util.PythonInterpreter

@Deprecated("CDS won't support JythonService")
open class BlueprintPythonInterpreterProxy(private val bluePrintPython: BlueprintPython) : PythonInterpreter() {

    fun getPythonInstance(properties: MutableMap<String, Any>?): PyObject {
        properties?.forEach { (name, value) ->
            this.set(name, value)
        }

        this.exec("import sys")

        bluePrintPython.content.let {
            try {
                this.exec(bluePrintPython.content)
            } catch (e: PySyntaxError) {
                throw BlueprintProcessorException("Error executing Jython code! Python error: '$e'", e)
            }
        }

        val initCommand = bluePrintPython.pythonClassName.plus(" = ").plus(
            bluePrintPython.pythonClassName
        ).plus("()")
        this.exec(initCommand)

        return this.get(bluePrintPython.pythonClassName)
    }
}

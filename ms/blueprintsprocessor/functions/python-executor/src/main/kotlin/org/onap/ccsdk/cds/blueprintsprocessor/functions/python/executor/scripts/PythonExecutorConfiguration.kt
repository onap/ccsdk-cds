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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor.scripts

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import java.io.File
import java.util.Properties

@Deprecated("CDS won't support JythonService")
@Configuration
@ComponentScan
@EnableConfigurationProperties
open class PythonExecutorConfiguration

@Deprecated("CDS won't support JythonService")
@Configuration
open class PythonExecutorProperty {

    @Value("\${blueprints.processor.functions.python.executor.executionPath}")
    lateinit var executionPath: String

    @Value("#{'\${blueprints.processor.functions.python.executor.modulePaths}'.split(',')}")
    lateinit var modulePaths: List<String>
}

@Deprecated("CDS won't support JythonService")
class PythonExecutorConstants {

    companion object {

        const val INPUT_INSTANCE_DEPENDENCIES = "instance-dependencies"
    }
}

@Deprecated("CDS won't support JythonService")
open class BlueprintPython(
    executablePath: String,
    blueprintPythonPlatform: MutableList<String>,
    val argv: MutableList<String>
) {

    lateinit var moduleName: String
    lateinit var pythonClassName: String
    lateinit var content: String
    var props: Properties = Properties()

    init {
        // Build up the python.path
        val sb = StringBuilder()
        sb.append(System.getProperty("java.class.path"))

        for (p in blueprintPythonPlatform) {
            sb.append(File.pathSeparator).append(p)
        }

        props["python.import.site"] = "true"
        props.setProperty("python.path", sb.toString())
        props.setProperty("python.verbose", "error")
        props.setProperty("python.executable", executablePath)
    }
}

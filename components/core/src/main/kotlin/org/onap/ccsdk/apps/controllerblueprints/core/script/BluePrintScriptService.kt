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

package org.onap.ccsdk.apps.controllerblueprints.core.script

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import java.io.File
import java.io.InputStream
import java.io.Reader
import javax.script.ScriptEngineManager
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.resultOrNull
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate


open class BluePrintScriptService(classLoader: ClassLoader? = Thread.currentThread().contextClassLoader) {

    /**
     * Get the Script Class instance
     */
    inline fun <reified T> scriptClassNewInstance(scriptFile: File, scriptClassName: String): T {

        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ComponentScript>()

        val scriptEvaluator = BluePrintScriptEvaluator(scriptClassName)

        val evalResponse = BlueprintScriptingHost(scriptEvaluator).eval(scriptFile.toScriptSource(), compilationConfiguration,
                null)

        when (evalResponse) {
            is ResultWithDiagnostics.Success -> {
                val returnValue = evalResponse.resultOrNull()?.returnValue as ResultValue.Value
                return returnValue.value.castOrError()
            }
            is ResultWithDiagnostics.Failure -> {
                throw BluePrintProcessorException(evalResponse.reports.joinToString("\n"))
            }
            else -> {
                throw BluePrintProcessorException("Failed to process script ${scriptFile.absolutePath}")
            }
        }

    }

    val engine = ScriptEngineManager(classLoader).getEngineByExtension("kts")

    inline fun <R> safeEval(evaluation: () -> R?) = try {
        evaluation()
    } catch (e: Exception) {
        throw BluePrintProcessorException("Cannot load script", e)
    }

    inline fun <reified T> Any?.castOrError() = takeIf { it is T }?.let { it as T }
            ?: throw IllegalArgumentException("Cannot cast $this to expected type ${T::class}")

    inline fun <reified T> load(script: String): T = safeEval { engine.eval(script) }.castOrError()

    inline fun <reified T> load(reader: Reader): T = safeEval { engine.eval(reader) }.castOrError()

    inline fun <reified T> load(inputStream: InputStream): T = load(inputStream.reader())

    inline fun <reified T> loadAll(vararg inputStream: InputStream): List<T> = inputStream.map(::load)
}

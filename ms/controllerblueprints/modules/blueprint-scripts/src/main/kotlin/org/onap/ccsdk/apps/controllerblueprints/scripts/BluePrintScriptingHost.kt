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

package org.onap.ccsdk.apps.controllerblueprints.scripts

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.BasicScriptingHost
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.impl.withDefaults

val blueprintScriptCompiler = JvmScriptCompiler(defaultJvmScriptingHostConfiguration,
        BluePrintsCompilerProxy(defaultJvmScriptingHostConfiguration.withDefaults()))

open class BlueprintScriptingHost(evaluator: ScriptEvaluator) : BasicScriptingHost(blueprintScriptCompiler, evaluator) {

    override fun eval(
            script: SourceCode,
            scriptCompilationConfiguration: ScriptCompilationConfiguration,
            configuration: ScriptEvaluationConfiguration?
    ): ResultWithDiagnostics<EvaluationResult> =

            runInCoroutineContext {

                compiler(script, scriptCompilationConfiguration)
                        .onSuccess {
                            evaluator(it, configuration)
                        }.onFailure { failedResult ->
                            val messages = failedResult.reports?.joinToString("\n")
                            throw BluePrintProcessorException(messages)
                        }
            }
}

open class BluePrintScriptEvaluator(private val scriptClassName: String) : ScriptEvaluator {

    private val log = LoggerFactory.getLogger(BluePrintScriptEvaluator::class.java)!!

    override suspend operator fun invoke(
            compiledScript: CompiledScript<*>,
            scriptEvaluationConfiguration: ScriptEvaluationConfiguration?
    ): ResultWithDiagnostics<EvaluationResult> =
            try {
                log.debug("Getting script class name($scriptClassName) from the compiled sources ")
                val bluePrintCompiledScript = compiledScript as BluePrintCompiledScript
                bluePrintCompiledScript.scriptClassFQName = scriptClassName

                val res = compiledScript.getClass(scriptEvaluationConfiguration)
                when (res) {
                    is ResultWithDiagnostics.Failure -> res
                    is ResultWithDiagnostics.Success -> {

                        val scriptClass = res.value
                        val args = ArrayList<Any?>()
                        scriptEvaluationConfiguration?.get(ScriptEvaluationConfiguration.providedProperties)?.forEach {
                            args.add(it.value)
                        }
                        scriptEvaluationConfiguration?.get(ScriptEvaluationConfiguration.implicitReceivers)?.let {
                            args.addAll(it)
                        }
                        scriptEvaluationConfiguration?.get(ScriptEvaluationConfiguration.constructorArgs)?.let {
                            args.addAll(it)
                        }

                        val instance = scriptClass.java.constructors.single().newInstance(*args.toArray())
                                ?: throw BluePrintProcessorException("failed to create instance from the script")

                        log.info("Created script instance of type ${instance.javaClass}")

                        ResultWithDiagnostics.Success(EvaluationResult(ResultValue.Value(scriptClass.qualifiedName!!,
                                instance, "", instance),
                                scriptEvaluationConfiguration))
                    }
                }
            } catch (e: Throwable) {
                ResultWithDiagnostics.Failure(e.asDiagnostics("Error evaluating script"))
            }
}
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
import org.slf4j.LoggerFactory
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.BasicScriptingHost
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvmhost.JvmScriptCompiler

val defaultBlueprintScriptCompiler = JvmScriptCompiler(defaultJvmScriptingHostConfiguration)

open class BlueprintScriptingHost(evaluator: ScriptEvaluator
) : BasicScriptingHost(defaultBlueprintScriptCompiler, evaluator) {

    override fun eval(
            script: SourceCode,
            scriptCompilationConfiguration: ScriptCompilationConfiguration,
            configuration: ScriptEvaluationConfiguration?
    ): ResultWithDiagnostics<EvaluationResult> =

            runInCoroutineContext {

                compiler(script, scriptCompilationConfiguration)
                        .onSuccess {
                            evaluator(it, configuration)
                        }
            }
}


open class BluePrintScriptEvaluator(private val scriptClassName: String) : ScriptEvaluator {

    val log = LoggerFactory.getLogger(BluePrintScriptEvaluator::class.java)!!

    override suspend operator fun invoke(
            compiledScript: CompiledScript<*>,
            scriptEvaluationConfiguration: ScriptEvaluationConfiguration?
    ): ResultWithDiagnostics<EvaluationResult> =
            try {
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

                        val completeScriptClass = "Script\$$scriptClassName"
                        log.info("Searching for class type($completeScriptClass)")
                        /**
                         * Search for Class Name
                         */
                        val instanceClass = scriptClass.java.classes
                                .single { it.name == completeScriptClass }
                                //.single { it.name == "Script\$SampleBlueprintsFunctionNode" }


                        val instance = instanceClass.newInstance()
                                ?: throw BluePrintProcessorException("failed to create instance from the script")

                        ResultWithDiagnostics.Success(EvaluationResult(ResultValue.Value(completeScriptClass,
                                instance, instance.javaClass.typeName),
                                scriptEvaluationConfiguration))
                    }
                }
            } catch (e: Throwable) {
                ResultWithDiagnostics.Failure(e.asDiagnostics("Error evaluating script"))
            }
}
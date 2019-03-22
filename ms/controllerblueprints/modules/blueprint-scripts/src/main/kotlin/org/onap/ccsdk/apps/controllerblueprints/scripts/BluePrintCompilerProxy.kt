/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.*
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.util.classpathFromClasspathProperty
import kotlin.script.experimental.jvmhost.KJvmCompilerProxy

open class BluePrintsCompilerProxy(private val hostConfiguration: ScriptingHostConfiguration) : KJvmCompilerProxy {

    private val log = LoggerFactory.getLogger(BluePrintsCompilerProxy::class.java)!!

    override fun compile(script: SourceCode, scriptCompilationConfiguration: ScriptCompilationConfiguration)
            : ResultWithDiagnostics<CompiledScript<*>> {

        val messageCollector = ScriptDiagnosticsMessageCollector()

        fun failure(vararg diagnostics: ScriptDiagnostic): ResultWithDiagnostics.Failure =
                ResultWithDiagnostics.Failure(*messageCollector.diagnostics.toTypedArray(), *diagnostics)

        // Compile the Code
        try {

            log.trace("Scripting Host Configuration : $hostConfiguration")

            setIdeaIoUseFallback()

            val blueprintSourceCode = script as BluePrintSourceCode

            val compiledJarFile = blueprintSourceCode.targetJarFile

            if (!compiledJarFile.exists() || blueprintSourceCode.regenerate) {

                var environment: KotlinCoreEnvironment? = null

                val rootDisposable = Disposer.newDisposable()

                try {

                    val compilerConfiguration = CompilerConfiguration().apply {

                        put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
                        put(CommonConfigurationKeys.MODULE_NAME, blueprintSourceCode.moduleName)
                        put(JVMConfigurationKeys.OUTPUT_JAR, compiledJarFile)
                        put(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, false)

                        // Load Current Class loader to Compilation Class loader
                        val currentClassLoader = classpathFromClasspathProperty()
                        currentClassLoader?.forEach {
                            add(CLIConfigurationKeys.CONTENT_ROOTS, JvmClasspathRoot(it))
                        }

                        // Add all Kotlin Sources
                        addKotlinSourceRoots(blueprintSourceCode.blueprintKotlinSources)

                        languageVersionSettings = LanguageVersionSettingsImpl(
                                LanguageVersion.LATEST_STABLE, ApiVersion.LATEST_STABLE, mapOf(AnalysisFlags.skipMetadataVersionCheck to true)
                        )
                    }

                    //log.info("Executing with compiler configuration : $compilerConfiguration")

                    environment = KotlinCoreEnvironment.createForProduction(rootDisposable, compilerConfiguration,
                            EnvironmentConfigFiles.JVM_CONFIG_FILES)

                    // Compile Kotlin Sources
                    val compiled = KotlinToJVMBytecodeCompiler.compileBunchOfSources(environment)

                    val analyzerWithCompilerReport = AnalyzerWithCompilerReport(messageCollector,
                            environment.configuration.languageVersionSettings)

                    if (analyzerWithCompilerReport.hasErrors()) {
                        return ResultWithDiagnostics.Failure(messageCollector.diagnostics)
                    }
                } finally {
                    rootDisposable.dispose()
                }
            }

            val res = BluePrintCompiledScript<File>(scriptCompilationConfiguration, compiledJarFile)

            return ResultWithDiagnostics.Success(res, messageCollector.diagnostics)

        } catch (ex: Throwable) {
            return failure(ex.asDiagnostics())
        }
    }
}

class ScriptDiagnosticsMessageCollector : MessageCollector {

    private val _diagnostics = arrayListOf<ScriptDiagnostic>()

    val diagnostics: List<ScriptDiagnostic> get() = _diagnostics

    override fun clear() {
        _diagnostics.clear()
    }

    override fun hasErrors(): Boolean =
            _diagnostics.any { it.severity == ScriptDiagnostic.Severity.ERROR }


    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation?) {
        val mappedSeverity = when (severity) {
            CompilerMessageSeverity.EXCEPTION,
            CompilerMessageSeverity.ERROR -> ScriptDiagnostic.Severity.ERROR
            CompilerMessageSeverity.STRONG_WARNING,
            CompilerMessageSeverity.WARNING -> ScriptDiagnostic.Severity.WARNING
            CompilerMessageSeverity.INFO -> ScriptDiagnostic.Severity.INFO
            CompilerMessageSeverity.LOGGING -> ScriptDiagnostic.Severity.DEBUG
            else -> null
        }
        if (mappedSeverity != null) {
            val mappedLocation = location?.let {
                if (it.line < 0 && it.column < 0) null // special location created by CompilerMessageLocation.create
                else SourceCode.Location(SourceCode.Position(it.line, it.column))
            }
            _diagnostics.add(ScriptDiagnostic(message, mappedSeverity, location?.path, mappedLocation))
        }
    }
}


/*
 * Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core.scripts

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.parseCommandLineArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.checkFileExists
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import java.io.File
import java.net.URLClassLoader
import java.util.ArrayList
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.jvm.util.classpathFromClasspathProperty
import kotlin.system.measureTimeMillis

open class BluePrintCompileService {

    val log = logger(BluePrintCompileService::class)

    companion object {

        val classPaths = classpathFromClasspathProperty()?.joinToString(File.pathSeparator) {
            it.absolutePath
        }
        val mutexCache: LoadingCache<String, Mutex> = CacheBuilder.newBuilder()
                .build(CacheLoader.from { s -> Mutex() })
    }

    /** Compile the [bluePrintSourceCode] and get the [kClassName] instance for the constructor [args] */
    suspend fun <T> eval(
        bluePrintSourceCode: BluePrintSourceCode,
        kClassName: String,
        args: ArrayList<Any?>?
    ): T {
        /** Compile the source code if needed */
        log.debug("Jar Exists : ${bluePrintSourceCode.targetJarFile.exists()}, Regenerate : ${bluePrintSourceCode.regenerate}")

        mutexCache.get(bluePrintSourceCode.targetJarFile.absolutePath).withLock {
            if (!bluePrintSourceCode.targetJarFile.exists() || bluePrintSourceCode.regenerate) {
                compile(bluePrintSourceCode)
            }
        }

        val classLoaderWithDependencies = BluePrintCompileCache.classLoader(bluePrintSourceCode.cacheKey)

        /** Create the instance from the class loader */
        return instance(classLoaderWithDependencies, kClassName, args)
    }

    /** Compile [bluePrintSourceCode] and put into cache */
    suspend fun compile(bluePrintSourceCode: BluePrintSourceCode) {
        // TODO("Include Multiple folders")
        val sourcePath = bluePrintSourceCode.blueprintKotlinSources.first()
        val compiledJarFile = bluePrintSourceCode.targetJarFile

        log.info("compiling for cache key(${bluePrintSourceCode.cacheKey})")
        coroutineScope {
            val timeTaken = measureTimeMillis {
                /** Create compile arguments */
                val args = mutableListOf<String>().apply {
                    add("-no-stdlib")
                    add("-no-reflect")
                    add("-module-name")
                    add(bluePrintSourceCode.moduleName)
                    add("-cp")
                    add(classPaths!!)
                    add(sourcePath)
                    add("-d")
                    add(compiledJarFile.absolutePath)
                }
                val deferredCompile = async {
                    val k2jvmCompiler = K2JVMCompiler()

                    /** Construct Arguments */
                    val arguments = k2jvmCompiler.createArguments()
                    parseCommandLineArguments(args, arguments)
                    val messageCollector = CompilationMessageCollector()

                    /** Compile with arguments */
                    val exitCode: ExitCode = k2jvmCompiler.exec(messageCollector, Services.EMPTY, arguments)
                    when (exitCode) {
                        ExitCode.OK -> {
                            checkFileExists(compiledJarFile) { "couldn't generate compiled jar(${compiledJarFile.absolutePath})" }
                        }
                        else -> {
                            throw BluePrintException("$exitCode :${messageCollector.errors().joinToString("\n")}")
                        }
                    }
                }
                deferredCompile.await()
            }
            log.info("compiled in ($timeTaken)mSec for cache key(${bluePrintSourceCode.cacheKey})")
        }
    }

    /** create class [kClassName] instance from [classLoader] */
    fun <T> instance(classLoader: URLClassLoader, kClassName: String, args: ArrayList<Any?>? = arrayListOf()): T {
        val kClazz = classLoader.loadClass(kClassName)
            ?: throw BluePrintException("failed to load class($kClassName) from current class loader.")

        val instance = if (args.isNullOrEmpty()) {
            kClazz.newInstance()
        } else {
            kClazz.constructors
                .single().newInstance(*args.toArray())
        } ?: throw BluePrintException("failed to create class($kClassName) instance for constructor argument($args).")

        return instance as T
    }
}

/** Compile source code information */
open class BluePrintSourceCode : SourceCode {

    lateinit var blueprintKotlinSources: MutableList<String>
    lateinit var moduleName: String
    lateinit var targetJarFile: File
    lateinit var cacheKey: String
    var regenerate: Boolean = false

    override val text: String
        get() = ""

    override val locationId: String? = null

    override val name: String?
        get() = moduleName
}

/** Class to collect compilation Data */
data class CompiledMessageData(
    val severity: CompilerMessageSeverity,
    val message: String,
    val location: CompilerMessageLocation?
)

/** Class to collect compilation results */
class CompilationMessageCollector : MessageCollector {

    private val compiledMessages: MutableList<CompiledMessageData> = arrayListOf()

    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation?) {
        synchronized(compiledMessages) {
            compiledMessages.add(CompiledMessageData(severity, message, location))
        }
    }

    override fun hasErrors() =
        synchronized(compiledMessages) {
            compiledMessages.any { it.severity.isError }
        }

    override fun clear() {
        synchronized(compiledMessages) {
            compiledMessages.clear()
        }
    }

    fun errors(): List<CompiledMessageData> = compiledMessages.filter { it.severity == CompilerMessageSeverity.ERROR }
}

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

package org.onap.ccsdk.cds.controllerblueprints.core.scripts

import java.io.File
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*

open class BluePrintCompiledScript<out JarFile : File>(
        private val scriptCompilationConfiguration: ScriptCompilationConfiguration,
        private val compiledJar: File) :
        CompiledScript<JarFile>, Serializable {

    lateinit var compiledCacheKey: String
    lateinit var scriptClassFQName: String

    override val compilationConfiguration: ScriptCompilationConfiguration
        get() = scriptCompilationConfiguration

    override suspend fun getClass(scriptEvaluationConfiguration: ScriptEvaluationConfiguration?)
            : ResultWithDiagnostics<KClass<*>> = try {

        /** Get the class loader from the cache */
        val classLoaderWithDependencies = BluePrintCompileCache.classLoader(compiledCacheKey)

        val clazz = classLoaderWithDependencies.loadClass(scriptClassFQName).kotlin
        clazz.asSuccess()
    } catch (e: Throwable) {
        ResultWithDiagnostics.Failure(
                ScriptDiagnostic(
                        "Unable to instantiate class $scriptClassFQName",
                        exception = e
                )
        )
    }

}


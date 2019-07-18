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

package org.onap.ccsdk.cds.controllerblueprints.core.scripts

import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintScriptsService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintFileUtils
import java.util.*
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.resultOrNull
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

open class BluePrintScriptsServiceImpl : BluePrintScriptsService {

    val log = logger(BluePrintScriptsServiceImpl::class)

    override suspend fun <T> scriptInstance(blueprintContext: BluePrintContext, scriptClassName: String,
                                            reCompile: Boolean): T {

        val scriptSource = BluePrintSourceCode()

        val sources: MutableList<String> = arrayListOf()
        sources.add(BluePrintFileUtils.scriptPath(blueprintContext))
        scriptSource.blueprintKotlinSources = sources
        scriptSource.moduleName = "${blueprintContext.name()}-${blueprintContext.version()}-cba-kts"
        scriptSource.cacheKey = BluePrintFileUtils.compileJarFilePath(blueprintContext)
        scriptSource.targetJarFile = normalizedFile(scriptSource.cacheKey)
        scriptSource.regenerate = reCompile

        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<BluePrintKotlinScript>()
        val scriptEvaluator = BluePrintScriptEvaluator(scriptSource.cacheKey, scriptClassName)

        val compiledResponse = BlueprintScriptingHost(scriptEvaluator)
                .eval(scriptSource, compilationConfiguration, null)

        val returnValue = compiledResponse.resultOrNull()?.returnValue as? ResultValue.Value

        return returnValue?.value!! as T
    }

    override suspend fun <T> scriptInstance(scriptClassName: String): T {
        val args = ArrayList<Any?>()
        return Thread.currentThread().contextClassLoader.loadClass(scriptClassName).constructors
                .single().newInstance(*args.toArray()) as T
    }
}
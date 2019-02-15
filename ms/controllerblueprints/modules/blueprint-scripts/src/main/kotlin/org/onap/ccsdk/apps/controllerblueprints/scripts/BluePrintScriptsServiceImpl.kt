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

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintScriptsService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.springframework.stereotype.Service
import java.io.File
import java.util.*
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.resultOrNull
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

@Service
open class BluePrintScriptsServiceImpl : BluePrintScriptsService {

    override fun <T> scriptInstance(blueprintContext: BluePrintContext, scriptClassName: String,
                                    reCompile: Boolean): T {

        val kotlinScriptPath = blueprintContext.rootPath.plus(File.separator)
                .plus(BluePrintConstants.TOSCA_SCRIPTS_KOTLIN_DIR)

        val compiledJar = kotlinScriptPath.plus(File.separator)
                .plus(getBluePrintScriptsJarName(blueprintContext))

        val scriptSource = BluePrintSourceCode()

        val sources: MutableList<String> = arrayListOf()
        sources.add(kotlinScriptPath)
        scriptSource.blueprintKotlinSources = sources
        scriptSource.moduleName = "${blueprintContext.name()}-${blueprintContext.version()}-cba-kts"
        scriptSource.targetJarFile = File(compiledJar)
        scriptSource.regenerate = reCompile

        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<BluePrintKotlinScript>()
        val scriptEvaluator = BluePrintScriptEvaluator(scriptClassName)

        val compiledResponse = BlueprintScriptingHost(scriptEvaluator).eval(scriptSource, compilationConfiguration,
                null)

        val returnValue = compiledResponse.resultOrNull()?.returnValue as? ResultValue.Value

        return returnValue?.value!! as T
    }

    override fun <T> scriptInstance(scriptClassName: String): T {
        val args = ArrayList<Any?>()
        return Thread.currentThread().contextClassLoader.loadClass(scriptClassName).constructors
                .single().newInstance(*args.toArray()) as T
    }
}

fun getBluePrintScriptsJarName(blueprintContext: BluePrintContext): String {
    return "${blueprintContext.name()}-${blueprintContext.version()}-cba-kts.jar"
}
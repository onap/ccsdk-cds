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

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintScriptsService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintFileUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintMetadataUtils
import java.util.ArrayList

open class BlueprintScriptsServiceImpl : BlueprintScriptsService {

    val log = logger(BlueprintScriptsServiceImpl::class)

    override suspend fun <T> scriptInstance(bluePrintSourceCode: BlueprintSourceCode, scriptClassName: String): T {
        val bluePrintCompileService = BlueprintCompileService()
        return bluePrintCompileService.eval(bluePrintSourceCode, scriptClassName, null)
    }

    override suspend fun <T> scriptInstance(
        blueprintBasePath: String,
        artifactName: String,
        artifactVersion: String,
        scriptClassName: String,
        reCompile: Boolean
    ): T {

        val sources: MutableList<String> = arrayListOf()
        sources.add(normalizedPathName(blueprintBasePath, BlueprintConstants.TOSCA_SCRIPTS_KOTLIN_DIR))

        val scriptSource = BlueprintSourceCode()
        scriptSource.blueprintKotlinSources = sources
        scriptSource.moduleName = "$artifactName-$artifactVersion-cba-kts"
        scriptSource.cacheKey = BlueprintFileUtils.compileCacheKey(blueprintBasePath)
        scriptSource.targetJarFile = BlueprintFileUtils.compileJarFile(blueprintBasePath, artifactName, artifactVersion)
        scriptSource.regenerate = reCompile
        return scriptInstance(scriptSource, scriptClassName)
    }

    override suspend fun <T> scriptInstance(
        blueprintBasePath: String,
        scriptClassName: String,
        reCompile: Boolean
    ): T {
        val toscaMetaData = BlueprintMetadataUtils.toscaMetaData(blueprintBasePath)
        checkNotNull(toscaMetaData.templateName) { "couldn't find 'Template-Name' key in TOSCA.meta" }
        checkNotNull(toscaMetaData.templateVersion) { "couldn't find 'Template-Version' key in TOSCA.meta" }
        return scriptInstance(
            blueprintBasePath, toscaMetaData.templateName!!, toscaMetaData.templateVersion!!,
            scriptClassName, reCompile
        )
    }

    override suspend fun <T> scriptInstance(cacheKey: String, scriptClassName: String): T {
        val args = ArrayList<Any?>()
        return BlueprintCompileCache.classLoader(cacheKey).loadClass(scriptClassName).constructors
            .single().newInstance(*args.toArray()) as T
    }

    override suspend fun <T> scriptInstance(scriptClassName: String): T {
        val args = ArrayList<Any?>()
        return Thread.currentThread().contextClassLoader.loadClass(scriptClassName).constructors
            .single().newInstance(*args.toArray()) as T
    }
}

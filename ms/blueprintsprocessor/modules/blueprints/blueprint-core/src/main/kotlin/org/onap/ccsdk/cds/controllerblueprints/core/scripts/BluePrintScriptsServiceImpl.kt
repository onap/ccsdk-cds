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

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintScriptsService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintFileUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import java.util.ArrayList

open class BluePrintScriptsServiceImpl : BluePrintScriptsService {

    val log = logger(BluePrintScriptsServiceImpl::class)

    override suspend fun <T> scriptInstance(bluePrintSourceCode: BluePrintSourceCode, scriptClassName: String): T {
        val bluePrintCompileService = BluePrintCompileService()
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
        sources.add(normalizedPathName(blueprintBasePath, BluePrintConstants.TOSCA_SCRIPTS_KOTLIN_DIR))

        val scriptSource = BluePrintSourceCode()
        scriptSource.blueprintKotlinSources = sources
        scriptSource.moduleName = "$artifactName-$artifactVersion-cba-kts"
        scriptSource.cacheKey = BluePrintFileUtils.compileCacheKey(blueprintBasePath)
        scriptSource.targetJarFile = BluePrintFileUtils.compileJarFile(blueprintBasePath, artifactName, artifactVersion)
        scriptSource.regenerate = reCompile
        return scriptInstance(scriptSource, scriptClassName)
    }

    override suspend fun <T> scriptInstance(
        blueprintBasePath: String,
        scriptClassName: String,
        reCompile: Boolean
    ): T {
        val toscaMetaData = BluePrintMetadataUtils.toscaMetaData(blueprintBasePath)
        checkNotNull(toscaMetaData.templateName) { "couldn't find 'Template-Name' key in TOSCA.meta" }
        checkNotNull(toscaMetaData.templateVersion) { "couldn't find 'Template-Version' key in TOSCA.meta" }
        return scriptInstance(
            blueprintBasePath, toscaMetaData.templateName!!, toscaMetaData.templateVersion!!,
            scriptClassName, reCompile
        )
    }

    override suspend fun <T> scriptInstance(cacheKey: String, scriptClassName: String): T {
        val args = ArrayList<Any?>()
        return BluePrintCompileCache.classLoader(cacheKey).loadClass(scriptClassName).constructors
            .single().newInstance(*args.toArray()) as T
    }

    override suspend fun <T> scriptInstance(scriptClassName: String): T {
        val args = ArrayList<Any?>()
        return Thread.currentThread().contextClassLoader.loadClass(scriptClassName).constructors
            .single().newInstance(*args.toArray()) as T
    }
}

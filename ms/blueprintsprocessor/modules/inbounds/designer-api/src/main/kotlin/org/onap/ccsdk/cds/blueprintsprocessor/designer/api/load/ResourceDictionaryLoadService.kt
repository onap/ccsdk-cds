/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.load

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.text.StrBuilder
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler.ResourceDictionaryHandler
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.readNBText
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
open class ResourceDictionaryLoadService(private val resourceDictionaryHandler: ResourceDictionaryHandler) {

    private val log = LoggerFactory.getLogger(ResourceDictionaryLoadService::class.java)

    open suspend fun loadPathsResourceDictionary(paths: List<String>) {
        coroutineScope {
            val deferred = paths.map {
                async {
                    loadPathResourceDictionary(it)
                }
            }
            deferred.awaitAll()
        }
    }

    open suspend fun loadPathResourceDictionary(path: String) {
        log.info(" ******* loadResourceDictionary($path) ********")
        val files = normalizedFile(path).listFiles()
        val errorBuilder = StrBuilder()

        coroutineScope() {
            val deferred = files.map {
                async {
                    loadResourceDictionary(errorBuilder, it)
                }
            }
            deferred.awaitAll()
        }

        if (!errorBuilder.isEmpty) {
            log.error(errorBuilder.toString())
        }
    }

    private suspend fun loadResourceDictionary(errorBuilder: StrBuilder, file: File) {
        try {
            log.trace("Loading Resource Dictionary(${file.name}}")
            val definitionContent = file.readNBText()
            val resourceDefinition = JacksonUtils.readValue(definitionContent, ResourceDefinition::class.java)
            if (resourceDefinition != null) {
                resourceDictionaryHandler.saveResourceDefinition(resourceDefinition)
                log.trace("Resource dictionary(${file.name}) loaded successfully ")
            } else {
                throw BlueprintException("couldn't get dictionary from content information")
            }
        } catch (e: Exception) {
            errorBuilder.appendln("Couldn't load Resource dictionary (${file.name}: ${e.message})")
        }
    }
}

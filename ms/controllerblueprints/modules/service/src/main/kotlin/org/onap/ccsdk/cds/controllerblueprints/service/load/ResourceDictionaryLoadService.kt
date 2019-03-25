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

package org.onap.ccsdk.cds.controllerblueprints.service.load

import com.att.eelf.configuration.EELFManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.text.StrBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.readNBText
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.cds.controllerblueprints.service.domain.ResourceDictionary
import org.onap.ccsdk.cds.controllerblueprints.service.handler.ResourceDictionaryHandler
import org.springframework.stereotype.Service
import java.io.File

@Service
open class ResourceDictionaryLoadService(private val resourceDictionaryHandler: ResourceDictionaryHandler) {

    private val log = EELFManager.getInstance().getLogger(ResourceDictionaryLoadService::class.java)

    open suspend fun loadPathsResourceDictionary(paths: List<String>) {
        paths.forEach {
            loadPathResourceDictionary(it)
        }
    }

    open suspend fun loadPathResourceDictionary(path: String) {
        log.info(" *************************** loadResourceDictionary **********************")
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
            log.trace("Loading NodeType(${file.name}}")
            val definitionContent = file.readNBText()
            val resourceDefinition = JacksonUtils.readValue(definitionContent, ResourceDefinition::class.java)
            if (resourceDefinition != null) {

                checkNotNull(resourceDefinition.property) { "Failed to get Property Definition" }
                val resourceDictionary = ResourceDictionary()
                resourceDictionary.name = resourceDefinition.name
                resourceDictionary.definition = resourceDefinition

                checkNotNull(resourceDefinition.property) { "Property field is missing" }
                resourceDictionary.description = resourceDefinition.property.description
                resourceDictionary.dataType = resourceDefinition.property.type

                if (resourceDefinition.property.entrySchema != null) {
                    resourceDictionary.entrySchema = resourceDefinition.property.entrySchema!!.type
                }
                resourceDictionary.updatedBy = resourceDefinition.updatedBy

                if (StringUtils.isBlank(resourceDefinition.tags)) {
                    resourceDictionary.tags = (resourceDefinition.name + ", " + resourceDefinition.updatedBy
                            + ", " + resourceDefinition.updatedBy)

                } else {
                    resourceDictionary.tags = resourceDefinition.tags
                }
                resourceDictionaryHandler.saveResourceDictionary(resourceDictionary)

                log.trace("Resource dictionary(${file.name}) loaded successfully ")
            } else {
                throw BluePrintException("couldn't get dictionary from content information")
            }
        } catch (e: Exception) {
            errorBuilder.appendln("Couldn't load Resource dictionary (${file.name}: ${e.message}")
        }
    }

}
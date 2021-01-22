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

package org.onap.ccsdk.cds.controllerblueprints.core.service

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.data.ImportDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.utils.ServiceTemplateUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.Charset

class BlueprintImportService(private val parentServiceTemplate: ServiceTemplate, private val blueprintBasePath: String) {
    companion object {

        private const val PARENT_SERVICE_TEMPLATE: String = "parent"
    }

    private val log: Logger = LoggerFactory.getLogger(this::class.toString())

    private var importServiceTemplateMap: MutableMap<String, ServiceTemplate> = hashMapOf()

    suspend fun getImportResolvedServiceTemplate(): ServiceTemplate {
        // Populate Imported Service Templates
        traverseSchema(PARENT_SERVICE_TEMPLATE, parentServiceTemplate)

        importServiceTemplateMap.forEach { key, serviceTemplate ->
            ServiceTemplateUtils.merge(parentServiceTemplate, serviceTemplate)
            log.debug("merged service template $key")
        }
        return parentServiceTemplate
    }

    private suspend fun traverseSchema(key: String, serviceTemplate: ServiceTemplate) {
        if (key != PARENT_SERVICE_TEMPLATE) {
            importServiceTemplateMap[key] = serviceTemplate
        }
        val imports: List<ImportDefinition>? = serviceTemplate.imports

        imports?.let {
            serviceTemplate.imports?.forEach { importDefinition ->
                val childServiceTemplate = resolveImportDefinition(importDefinition)
                val keyName: String = importDefinition.file
                traverseSchema(keyName, childServiceTemplate)
            }
        }
    }

    private suspend fun resolveImportDefinition(importDefinition: ImportDefinition): ServiceTemplate {
        var serviceTemplate: ServiceTemplate? = null
        val file: String = importDefinition.file
        val decodedSystemId: String = URLDecoder.decode(file, Charset.defaultCharset().toString())
        log.trace("file ({}), decodedSystemId ({}) ", file, decodedSystemId)
        try {
            if (decodedSystemId.startsWith("http", true) ||
                decodedSystemId.startsWith("https", true)
            ) {
                val givenUrl: String = URL(decodedSystemId).toString()
                val systemUrl: String = File(".").toURI().toURL().toString()
                log.trace("givenUrl ({}), systemUrl ({}) ", givenUrl, systemUrl)
                if (givenUrl.startsWith(systemUrl)) {
                }
            } else {
                if (!decodedSystemId.startsWith("/")) {
                    importDefinition.file = StringBuilder().append(blueprintBasePath).append(File.separator).append(file).toString()
                }
                serviceTemplate = ServiceTemplateUtils.getServiceTemplate(importDefinition.file)
            }
        } catch (e: Exception) {
            throw BlueprintException("failed to populate service template for ${importDefinition.file} original error: ${e.message}", e)
        }
        if (serviceTemplate == null) {
            throw BlueprintException("failed to populate service template for :  ${importDefinition.file}")
        }
        return serviceTemplate
    }
}

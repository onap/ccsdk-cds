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

import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.controllerblueprints.core.config.BlueprintLoadConfiguration
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
open class BlueprintDatabaseLoadService(
    private val bluePrintLoadConfiguration: BlueprintLoadConfiguration,
    private val modelTypeLoadService: ModelTypeLoadService,
    private val resourceDictionaryLoadService: ResourceDictionaryLoadService,
    private val bluePrintCatalogLoadService: BlueprintCatalogLoadService
) {

    private val log = LoggerFactory.getLogger(BlueprintDatabaseLoadService::class.java)

    open fun init() = runBlocking {
        initModelTypes()
        initResourceDictionary()
        initBlueprintCatalog()
    }

    open suspend fun initModelTypes() {
        log.info("model types load from paths(${bluePrintLoadConfiguration.loadModeTypePaths})")

        val paths = bluePrintLoadConfiguration.loadModeTypePaths?.split(",")
        paths?.let {
            modelTypeLoadService.loadPathsModelType(paths)
        }
    }

    open suspend fun initResourceDictionary() {
        log.info("resource dictionary load from paths(${bluePrintLoadConfiguration.loadResourceDictionaryPaths})")

        val paths = bluePrintLoadConfiguration.loadResourceDictionaryPaths?.split(",")
        paths?.let {
            resourceDictionaryLoadService.loadPathsResourceDictionary(paths)
        }
    }

    open suspend fun initBlueprintCatalog() {
        log.info("cba load from paths(${bluePrintLoadConfiguration.loadBlueprintPaths})")

        val paths = bluePrintLoadConfiguration.loadBlueprintPaths?.split(",")
        paths?.let {
            bluePrintCatalogLoadService.loadPathsBlueprintModelCatalog(paths)
        }
    }
}

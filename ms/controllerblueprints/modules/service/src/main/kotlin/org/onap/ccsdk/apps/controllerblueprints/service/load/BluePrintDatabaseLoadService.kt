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

package org.onap.ccsdk.apps.controllerblueprints.service.load

import com.att.eelf.configuration.EELFManager
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
open class BluePrintDatabaseLoadService(private val bluePrintLoadConfiguration: BluePrintLoadConfiguration,
                                        private val modelTypeLoadService: ModelTypeLoadService,
                                        private val resourceDictionaryLoadService: ResourceDictionaryLoadService,
                                        private val bluePrintCatalogLoadService: BluePrintCatalogLoadService) {

    private val log = EELFManager.getInstance().getLogger(BluePrintDatabaseLoadService::class.java)


    @EventListener(ApplicationReadyEvent::class)
    open fun init() {
        if (bluePrintLoadConfiguration.loadInitialData) {
            initModelTypes()
            initResourceDictionary()
            initBluePrintCatalog()
        } else {
            log.info("Initial data load is disabled")
        }
    }

    open fun initModelTypes() {
        log.info("model types load configuration(${bluePrintLoadConfiguration.loadModelType}) " +
                "under paths(${bluePrintLoadConfiguration.loadModeTypePaths})")

        if (bluePrintLoadConfiguration.loadModelType) {
            val paths = bluePrintLoadConfiguration.loadModeTypePaths?.split(",")
            paths?.let {
                modelTypeLoadService.loadPathsModelType(paths)
            }
        }
    }

    open fun initResourceDictionary() {
        log.info("resource dictionary load configuration(${bluePrintLoadConfiguration.loadResourceDictionary}) " +
                "under paths(${bluePrintLoadConfiguration.loadResourceDictionaryPaths})")

        if (bluePrintLoadConfiguration.loadResourceDictionary) {
            val paths = bluePrintLoadConfiguration.loadResourceDictionaryPaths?.split(",")
            paths?.let {
                resourceDictionaryLoadService.loadPathsResourceDictionary(paths)
            }
        }
    }

    open fun initBluePrintCatalog() {
        log.info("blueprint load configuration(${bluePrintLoadConfiguration.loadBluePrint}) " +
                "under paths(${bluePrintLoadConfiguration.loadBluePrintPaths})")

        if (bluePrintLoadConfiguration.loadBluePrint) {
            val paths = bluePrintLoadConfiguration.loadBluePrintPaths?.split(",")
            paths?.let {
                bluePrintCatalogLoadService.loadPathsBluePrintModelCatalog(paths)
            }
        }
    }
}
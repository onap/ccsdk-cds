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

import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
open class BluePrintCatalogLoadService(private val bluePrintCatalogService: BluePrintCatalogService) {

    private val log = LoggerFactory.getLogger(BluePrintCatalogLoadService::class.java)

    open suspend fun loadPathsBluePrintModelCatalog(paths: List<String>) {
        paths.forEach { loadPathBluePrintModelCatalog(it) }
    }

    open suspend fun loadPathBluePrintModelCatalog(path: String) {

        val files = normalizedFile(path).listFiles()
        val errors = mutableListOf<String>()
        files.forEach {
            loadBluePrintModelCatalog(errors, it)
        }
        if (!errors.isEmpty()) {
            log.error(errors.joinToString("\n"))
        }
    }

    open suspend fun loadBluePrintModelCatalog(errorBuilder: MutableList<String>, file: File) {
        try {
            log.info("loading blueprint cba(${file.absolutePath})")
            bluePrintCatalogService.saveToDatabase(UUID.randomUUID().toString(), file)
        } catch (e: Exception) {
            errorBuilder.add("Couldn't load BlueprintModel(${file.name}: ${e.message}")
        }
    }

}
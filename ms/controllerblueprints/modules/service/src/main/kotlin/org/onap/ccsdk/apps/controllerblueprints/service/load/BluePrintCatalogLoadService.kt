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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.text.StrBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
open class BluePrintCatalogLoadService(private val bluePrintCatalogService: BluePrintCatalogService) {

    private val log = EELFManager.getInstance().getLogger(BluePrintCatalogLoadService::class.java)

    open fun loadPathsBluePrintModelCatalog(paths: List<String>) {
        paths.forEach { loadPathBluePrintModelCatalog(it) }
    }

    open fun loadPathBluePrintModelCatalog(path: String) {

        val files = File(path).listFiles()
        runBlocking {
            val errorBuilder = StrBuilder()
            val deferredResults = mutableListOf<Deferred<Unit>>()

            for (file in files) {
                deferredResults += async {
                    loadBluePrintModelCatalog(errorBuilder, file)
                }
            }

            for (deferredResult in deferredResults) {
                deferredResult.await()
            }

            if (!errorBuilder.isEmpty) {
                log.error(errorBuilder.toString())
            }
        }
    }

    open suspend fun loadBluePrintModelCatalog(errorBuilder: StrBuilder, file: File) {
        try {
            bluePrintCatalogService.saveToDatabase(UUID.randomUUID().toString(), file)
        } catch (e: Exception) {
            errorBuilder.appendln("Couldn't load BlueprintModel(${file.name}: ${e.message}")
        }
    }

}
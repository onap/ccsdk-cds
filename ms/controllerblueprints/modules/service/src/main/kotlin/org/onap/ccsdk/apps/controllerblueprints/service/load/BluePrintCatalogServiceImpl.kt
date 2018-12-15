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

import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintArchiveUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
class BluePrintCatalogServiceImpl(private val bluePrintLoadConfiguration: BluePrintLoadConfiguration,
                                  private val bluePrintValidatorService: BluePrintValidatorService) : BluePrintCatalogService {

    override fun uploadToDataBase(file: String): String {
        val id = UUID.randomUUID().toString()
        val blueprintFile = File(file)
        // If the file is directory
        if (blueprintFile.isDirectory) {
            val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(id, blueprintFile.absolutePath)
            val valid = bluePrintValidatorService.validateBluePrints(bluePrintRuntimeService)
            if (valid) {
                val zipFile = File("${bluePrintLoadConfiguration.blueprintArchivePath}/${id}.zip")
                // zip the directory
                BluePrintArchiveUtils.compress(blueprintFile, zipFile, true)

                // TODO(Upload to the Data Base)

                // After Upload to Database delete the zip file
                zipFile.deleteOnExit()
            }
        } else {
            // If the file is ZIP
            // TODO(Upload to the Data Base)
        }
        return id
    }

    override fun downloadFromDataBase(name: String, version: String, path: String): String {
        // If path ends with zip, then compress otherwise download as extracted folder

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prepareBluePrint(name: String, version: String): String {
        val preparedPath = "${bluePrintLoadConfiguration.blueprintDeployPath}/$name/$version"
        downloadFromDataBase(name, version, preparedPath)
        return preparedPath
    }
}
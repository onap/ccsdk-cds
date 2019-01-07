/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.apps.controllerblueprints.db.resources

import org.onap.ccsdk.apps.controllerblueprints.core.config.BluePrintLoadConfiguration
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintArchiveUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintFileUtils
import java.io.File
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BlueprintCatalogServiceImpl(private val bluePrintLoadConfiguration: BluePrintLoadConfiguration) : BluePrintCatalogService {

    override fun uploadToDataBase(file: String, validate: Boolean): String {
        // The file name provided here is unique as we transform to UUID before storing
        val blueprintFile = File(file)
        val fileName = blueprintFile.name
        val id = BluePrintFileUtils.stripFileExtension(fileName)
        // If the file is directory
        if (blueprintFile.isDirectory) {

            val zipFile = File("${bluePrintLoadConfiguration.blueprintArchivePath}/$fileName")
            // zip the directory
            BluePrintArchiveUtils.compress(blueprintFile, zipFile, true)

            // Upload to the Data Base
            saveToDataBase(blueprintFile, id, zipFile)

            // After Upload to Database delete the zip file
            zipFile.delete()

        } else {
            // If the file is ZIP
            // unzip the CBA file to validate before store in database
            val targetDir = "${bluePrintLoadConfiguration.blueprintDeployPath}/$id/"
            val extractedDirectory = BluePrintArchiveUtils.deCompress(blueprintFile, targetDir)

            // Upload to the Data Base
            saveToDataBase(extractedDirectory, id, blueprintFile)

            // After Upload to Database delete the zip file
            blueprintFile.delete()
            extractedDirectory.delete()
        }

        return id
    }

    override fun downloadFromDataBase(name: String, version: String, path: String): String {
        // If path ends with zip, then compress otherwise download as extracted folder

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun downloadFromDataBase(uuid: String, path: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prepareBluePrint(name: String, version: String): String {
        val preparedPath = "${bluePrintLoadConfiguration.blueprintDeployPath}/$name/$version"
        downloadFromDataBase(name, version, preparedPath)
        return preparedPath
    }

    abstract fun saveToDataBase(extractedDirectory: File, id: String, archiveFile: File, checkValidity: Boolean? = false)
}
/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.cds.controllerblueprints.db.resources

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintArchiveUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintFileUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import java.io.File
import java.nio.file.Path
import java.util.*
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BlueprintCatalogServiceImpl(private val blueprintValidator: BluePrintValidatorService)
    : BluePrintCatalogService {

    override fun saveToDatabase(blueprintFile: File, validate: Boolean): String {
        val extractedDirectory: File
        val archivedDirectory: File
        val toDeleteDirectory: File
        val blueprintId = UUID.randomUUID().toString()

        if (blueprintFile.isDirectory) {
            extractedDirectory = blueprintFile
            archivedDirectory = File("$blueprintFile.zip")
            toDeleteDirectory = archivedDirectory

            if (!BluePrintArchiveUtils.compress(blueprintFile, archivedDirectory, true)) {
                throw BluePrintException("Fail to compress blueprint")
            }
        } else {
            val targetDir = "${blueprintFile.parent}/${BluePrintFileUtils.stripFileExtension(blueprintFile.name)}"

            extractedDirectory = BluePrintArchiveUtils.deCompress(blueprintFile, targetDir)
            archivedDirectory = blueprintFile
            toDeleteDirectory = extractedDirectory
        }

        var valid = BluePrintConstants.FLAG_N
        if (validate) {
            blueprintValidator.validateBluePrints(extractedDirectory.path)
            valid = BluePrintConstants.FLAG_Y
        }

        val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(blueprintId, extractedDirectory.path)
        val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!
        metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID] = blueprintId
        metadata[BluePrintConstants.PROPERTY_BLUEPRINT_VALID] = valid

        save(metadata, archivedDirectory)

        toDeleteDirectory.deleteRecursively()

        return blueprintId
    }

    override fun getFromDatabase(name: String, version: String, extract: Boolean): Path = get(name, version, extract)
            ?: throw BluePrintException("Could not find blueprint $name:$version from database")

    override fun deleteFromDatabase(name: String, version: String) = delete(name, version)

    abstract fun save(metadata: MutableMap<String, String>, archiveFile: File)
    abstract fun get(name: String, version: String, extract: Boolean): Path?
    abstract fun delete(name: String, version: String)

}
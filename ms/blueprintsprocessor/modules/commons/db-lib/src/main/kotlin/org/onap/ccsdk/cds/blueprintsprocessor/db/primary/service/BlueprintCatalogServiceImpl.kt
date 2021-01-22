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

package org.onap.ccsdk.cds.blueprintsprocessor.db.primary.service

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.config.BlueprintLoadConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.deCompress
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintArchiveUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintMetadataUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BlueprintCatalogServiceImpl(
    private val bluePrintLoadConfiguration: BlueprintLoadConfiguration,
    private val blueprintValidator: BlueprintValidatorService
) : BlueprintCatalogService {

    private val log = LoggerFactory.getLogger(BlueprintCatalogServiceImpl::class.java)!!

    override suspend fun saveToDatabase(processingId: String, blueprintFile: File, validate: Boolean): String {

        var archiveFile: File? = null
        var workingDir: String? = null

        if (blueprintFile.isDirectory) {
            log.info("Save processing($processingId) Working Dir(${blueprintFile.absolutePath})")
            workingDir = blueprintFile.absolutePath
            archiveFile = normalizedFile(bluePrintLoadConfiguration.blueprintArchivePath, processingId, "cba.zip")

            if (!BlueprintArchiveUtils.compress(blueprintFile, archiveFile)) {
                throw BlueprintException("Fail to compress blueprint")
            }
        } else {
            // Compressed File
            log.info("Save processing($processingId) CBA(${blueprintFile.absolutePath})")
            workingDir = normalizedPathName(bluePrintLoadConfiguration.blueprintWorkingPath, processingId)
            archiveFile = blueprintFile
            // Decompress the CBA file to working Directory
            blueprintFile.deCompress(workingDir)
        }

        var valid = BlueprintConstants.FLAG_N
        if (validate) {
            blueprintValidator.validateBlueprints(workingDir!!)
            valid = BlueprintConstants.FLAG_Y
        }

        val bluePrintRuntimeService = BlueprintMetadataUtils.getBlueprintRuntime(processingId, workingDir!!)
        val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!
        metadata[BlueprintConstants.PROPERTY_BLUEPRINT_PROCESS_ID] = processingId
        metadata[BlueprintConstants.PROPERTY_BLUEPRINT_VALID] = valid

        save(metadata, archiveFile)

        return processingId
    }

    override suspend fun getFromDatabase(name: String, version: String, extract: Boolean): Path = get(
        name, version,
        extract
    )
        ?: throw BlueprintException("Could not find blueprint $name:$version from database")

    override suspend fun deleteFromDatabase(name: String, version: String) = delete(name, version)

    abstract suspend fun save(metadata: MutableMap<String, String>, archiveFile: File)
    abstract suspend fun get(name: String, version: String, extract: Boolean): Path?
    abstract suspend fun delete(name: String, version: String)
}

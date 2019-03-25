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
import org.onap.ccsdk.cds.controllerblueprints.core.config.BluePrintPathConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintArchiveUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BlueprintCatalogServiceImpl(
        private val bluePrintPathConfiguration: BluePrintPathConfiguration,
        private val blueprintValidator: BluePrintValidatorService) : BluePrintCatalogService {

    private val log = LoggerFactory.getLogger(BlueprintCatalogServiceImpl::class.java)!!

    override suspend fun saveToDatabase(processingId: String, blueprintFile: File, validate: Boolean): String {

        var archiveFile: File? = null
        var workingDir: String? = null

        if (blueprintFile.isDirectory) {
            log.info("Save processing($processingId) Working Dir(${blueprintFile.absolutePath})")
            workingDir = blueprintFile.absolutePath
            archiveFile = normalizedFile(bluePrintPathConfiguration.blueprintArchivePath, processingId, "cba.zip")

            if (!BluePrintArchiveUtils.compress(blueprintFile, archiveFile, true)) {
                throw BluePrintException("Fail to compress blueprint")
            }
        } else {
            // Compressed File
            log.info("Save processing($processingId) CBA(${blueprintFile.absolutePath})")
            workingDir = normalizedPathName(bluePrintPathConfiguration.blueprintWorkingPath, processingId)
            archiveFile = blueprintFile
            // Decompress the CBA file to working Directory
            BluePrintArchiveUtils.deCompress(blueprintFile, workingDir)
        }

        var valid = BluePrintConstants.FLAG_N
        if (validate) {
            blueprintValidator.validateBluePrints(workingDir!!)
            valid = BluePrintConstants.FLAG_Y
        }

        val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(processingId, workingDir!!)
        val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!
        metadata[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID] = processingId
        metadata[BluePrintConstants.PROPERTY_BLUEPRINT_VALID] = valid

        save(metadata, archiveFile)

        return processingId
    }

    override suspend fun getFromDatabase(name: String, version: String, extract: Boolean): Path = get(name, version,
            extract)
            ?: throw BluePrintException("Could not find blueprint $name:$version from database")

    override suspend fun deleteFromDatabase(name: String, version: String) = delete(name, version)

    abstract suspend fun save(metadata: MutableMap<String, String>, archiveFile: File)
    abstract suspend fun get(name: String, version: String, extract: Boolean): Path?
    abstract suspend fun delete(name: String, version: String)

}
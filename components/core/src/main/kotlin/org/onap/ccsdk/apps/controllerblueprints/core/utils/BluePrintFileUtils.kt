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

package org.onap.ccsdk.apps.controllerblueprints.core.utils

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import java.io.File
import java.io.FileFilter
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class BluePrintFileUtils {
    companion object {

        private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())

        fun createEmptyBluePrint(basePath: String) {

            val blueprintDir = File(basePath)
            FileUtils.deleteDirectory(blueprintDir)

            Files.createDirectories(blueprintDir.toPath())

            val metaDataDir = File(blueprintDir.absolutePath.plus(File.separator).plus(BluePrintConstants.TOSCA_METADATA_DIR))
            Files.createDirectories(metaDataDir.toPath())

            val metafile = File(blueprintDir.absolutePath.plus(File.separator).plus(BluePrintConstants.TOSCA_METADATA_ENTRY_DEFINITION_FILE))
            Files.write(metafile.toPath(), getMetaDataContent().toByteArray(), StandardOpenOption.CREATE_NEW)

            val definitionsDir = File(blueprintDir.absolutePath.plus(File.separator).plus(BluePrintConstants.TOSCA_DEFINITIONS_DIR))
            Files.createDirectories(definitionsDir.toPath())

            val scriptsDir = File(blueprintDir.absolutePath.plus(File.separator).plus(BluePrintConstants.TOSCA_SCRIPTS_DIR))
            Files.createDirectories(scriptsDir.toPath())

            val plansDir = File(blueprintDir.absolutePath.plus(File.separator).plus(BluePrintConstants.TOSCA_PLANS_DIR))
            Files.createDirectories(plansDir.toPath())

            val templatesDir = File(blueprintDir.absolutePath.plus(File.separator).plus(BluePrintConstants.TOSCA_TEMPLATES_DIR))
            Files.createDirectories(templatesDir.toPath())

        }

        fun copyBluePrint(sourcePath: String, targetPath: String) {
            val sourceFile = File(sourcePath)
            val targetFile = File(targetPath)
            sourceFile.copyRecursively(targetFile, true)
        }

        fun deleteBluePrintTypes(basePath: String) {
            val definitionPath = basePath.plus(File.separator).plus(BluePrintConstants.TOSCA_DEFINITIONS_DIR)
            log.info("deleting definition types under : $definitionPath")

            val definitionDir = File(definitionPath)
            // Find the Type Definitions
            val fileFilter = FileFilter { pathname -> pathname.absolutePath.endsWith("_types.json") }
            // Delete the Type Files
            definitionDir.listFiles(fileFilter).forEach {
                Files.deleteIfExists(it.toPath())
            }
        }

        fun writeBluePrintTypes(blueprintContext: BluePrintContext) {

            val basePath = blueprintContext.rootPath
            val definitionPath = basePath.plus(File.separator).plus(BluePrintConstants.TOSCA_DEFINITIONS_DIR)
            val definitionDir = File(definitionPath)

            check(definitionDir.exists()) {
                throw BluePrintException("couldn't get definition file under path(${definitionDir.absolutePath})")
            }

            blueprintContext.dataTypes.let {
                val dataTypesContent = JacksonUtils.getWrappedJson(BluePrintConstants.PATH_DATA_TYPES, blueprintContext.dataTypes!!, true)
                writeFile(definitionDir.absolutePath, BluePrintConstants.PATH_DATA_TYPES, dataTypesContent)

            }

            blueprintContext.artifactTypes.let {
                val artifactTypesContent = JacksonUtils.getWrappedJson(BluePrintConstants.PATH_ARTIFACT_TYPES, blueprintContext.artifactTypes!!, true)
                writeFile(definitionDir.absolutePath, BluePrintConstants.PATH_ARTIFACT_TYPES, artifactTypesContent)
            }

            blueprintContext.nodeTypes.let {
                val nodeTypesContent = JacksonUtils.getWrappedJson(BluePrintConstants.PATH_NODE_TYPES, blueprintContext.nodeTypes!!, true)
                writeFile(definitionDir.absolutePath, BluePrintConstants.PATH_NODE_TYPES, nodeTypesContent)
            }

        }

        private fun writeFile(definitionPath: String, type: String, content: String) = runBlocking {
            val typeFile = File(definitionPath.plus(File.separator).plus("$type.json"))

            Files.write(typeFile.toPath(), content.toByteArray(), StandardOpenOption.CREATE_NEW)
            check(typeFile.exists()) {
                throw BluePrintException("couldn't write $type.json file under path(${typeFile.absolutePath})")
            }
        }

        private fun getMetaDataContent(): String {
            return "TOSCA-Meta-File-Version: 1.0.0" +
                    "\nCSAR-Version: <VERSION>" +
                    "\nCreated-By: <AUTHOR NAME>" +
                    "\nEntry-Definitions: Definitions/<BLUEPRINT_NAME>.json" +
                    "\nTemplate-Tags: <TAGS>"
        }

    }
}
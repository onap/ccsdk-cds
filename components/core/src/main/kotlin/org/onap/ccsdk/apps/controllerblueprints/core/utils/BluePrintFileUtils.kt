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
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.ImportDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import java.io.File
import java.io.FileFilter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.text.MessageFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.ZoneId
import java.time.format.DateTimeFormatter



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

        fun populateDefaultImports(blueprintContext: BluePrintContext) {
            // Get the Default Types
            val types = arrayListOf(BluePrintConstants.PATH_DATA_TYPES, BluePrintConstants.PATH_ARTIFACT_TYPES,
                    BluePrintConstants.PATH_NODE_TYPES, BluePrintConstants.PATH_POLICY_TYPES)

            // Clean Type Imports
            cleanImportTypes(blueprintContext.serviceTemplate)

            val imports = mutableListOf<ImportDefinition>()
            types.forEach { typeName ->
                val import = ImportDefinition()
                import.file = BluePrintConstants.TOSCA_DEFINITIONS_DIR.plus("/$typeName.json")
                imports.add(import)
            }

            blueprintContext.serviceTemplate.imports = imports
        }

        fun cleanImportTypes(serviceTemplate: ServiceTemplate) {
            // Clean the Type imports
            val toDeleteTypes = serviceTemplate.imports?.filter {
                it.file.endsWith("_types.json")
            }

            if (toDeleteTypes != null && toDeleteTypes.isNotEmpty()) {
                serviceTemplate.imports?.removeAll(toDeleteTypes)
            }
        }

        fun writeEnhancedBluePrint(blueprintContext: BluePrintContext) {

            // Write Blueprint Types
            writeBluePrintTypes(blueprintContext)
            // Re Populate the Imports
            populateDefaultImports(blueprintContext)
            // Rewrite the Entry Definition Files
            writeEntryDefinitionFile(blueprintContext)

        }

        fun writeBluePrintTypes(blueprintContext: BluePrintContext) {

            val basePath = blueprintContext.rootPath
            val definitionPath = basePath.plus(File.separator).plus(BluePrintConstants.TOSCA_DEFINITIONS_DIR)
            val definitionDir = File(definitionPath)

            check(definitionDir.exists()) {
                throw BluePrintException("couldn't get definition file under path(${definitionDir.absolutePath})")
            }

            blueprintContext.serviceTemplate.dataTypes?.let {
                val dataTypesContent = JacksonUtils.getWrappedJson(BluePrintConstants.PATH_DATA_TYPES, it.toSortedMap(), true)
                writeTypeFile(definitionDir.absolutePath, BluePrintConstants.PATH_DATA_TYPES, dataTypesContent)
            }

            blueprintContext.serviceTemplate.artifactTypes?.let {
                val artifactTypesContent = JacksonUtils.getWrappedJson(BluePrintConstants.PATH_ARTIFACT_TYPES, it.toSortedMap(), true)
                writeTypeFile(definitionDir.absolutePath, BluePrintConstants.PATH_ARTIFACT_TYPES, artifactTypesContent)
            }

            blueprintContext.serviceTemplate.nodeTypes?.let {
                val nodeTypesContent = JacksonUtils.getWrappedJson(BluePrintConstants.PATH_NODE_TYPES, it.toSortedMap(), true)
                writeTypeFile(definitionDir.absolutePath, BluePrintConstants.PATH_NODE_TYPES, nodeTypesContent)
            }

            blueprintContext.serviceTemplate.policyTypes?.let {
                val nodeTypesContent = JacksonUtils.getWrappedJson(BluePrintConstants.PATH_POLICY_TYPES, it.toSortedMap(), true)
                writeTypeFile(definitionDir.absolutePath, BluePrintConstants.PATH_POLICY_TYPES, nodeTypesContent)
            }
        }

        fun writeEntryDefinitionFile(blueprintContext: BluePrintContext) {

            val absoluteEntryDefinitionFile = blueprintContext.rootPath.plus(File.separator).plus(blueprintContext.entryDefinition)

            val serviceTemplate = blueprintContext.serviceTemplate

            // Clone the Service Template
            val writeServiceTemplate = serviceTemplate.clone()
            writeServiceTemplate.dataTypes = null
            writeServiceTemplate.artifactTypes = null
            writeServiceTemplate.policyTypes = null
            writeServiceTemplate.nodeTypes = null

            // Write the Serivice Template
            writeDefinitionFile(absoluteEntryDefinitionFile, JacksonUtils.getJson(writeServiceTemplate, true))
        }

        fun writeDefinitionFile(definitionFile: String, content: String) = runBlocking {
            val definitionFile = File(definitionFile)
            // Delete the File If exists
            Files.deleteIfExists(definitionFile.toPath())

            Files.write(definitionFile.toPath(), content.toByteArray(), StandardOpenOption.CREATE_NEW)
            check(definitionFile.exists()) {
                throw BluePrintException("couldn't write definition file under path(${definitionFile.absolutePath})")
            }
        }

        private fun writeTypeFile(definitionPath: String, type: String, content: String) = runBlocking {
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
       
        fun getBluePrintFile(fileName: String, targetPath: Path) : File {
            val filePath = targetPath.resolve(fileName).toString()
            val file = File(filePath)
            check(file.exists()) {
                throw BluePrintException("couldn't get definition file under path(${file.absolutePath})")
            }
            return file
        }

        fun getCBAGeneratedFileName(fileName: String, prefix: String): String {
            val DATE_FORMAT = "yyyyMMddHHmmss"
            val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
            val datePrefix = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime().format(formatter)
            return MessageFormat.format(prefix, datePrefix, fileName)
        }

        fun getCbaStorageDirectory(path: String): Path {
            check(StringUtils.isNotBlank(path)) {
                throw BluePrintException("CBA Path is missing.")
            }

            val fileStorageLocation = Paths.get(path).toAbsolutePath().normalize()

            if (!Files.exists(fileStorageLocation))
                Files.createDirectories(fileStorageLocation)

            return fileStorageLocation
        }

        fun stripFileExtension(fileName: String): String {
            val dotIndexe = fileName.lastIndexOf('.')

            // In case dot is in first position, we are dealing with a hidden file rather than an extension
            return if (dotIndexe > 0) fileName.substring(0, dotIndexe) else fileName
        }

    }
}
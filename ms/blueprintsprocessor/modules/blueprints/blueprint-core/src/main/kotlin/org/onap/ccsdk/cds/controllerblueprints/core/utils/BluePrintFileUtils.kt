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

package org.onap.ccsdk.cds.controllerblueprints.core.utils

import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.data.ErrorCode
import org.onap.ccsdk.cds.controllerblueprints.core.data.ImportDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileFilter
import java.io.FileNotFoundException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.NotDirectoryException
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class BluePrintFileUtils {
    companion object {

        const val COMPILED_JAR_SUFFIX = "cba-kts.jar"

        private val log = LoggerFactory.getLogger(this::class.toString())

        fun createEmptyBluePrint(basePath: String) {

            val blueprintDir = File(basePath)
            FileUtils.deleteDirectory(blueprintDir)

            Files.createDirectories(blueprintDir.toPath())

            val metaDataDir = File(blueprintDir.absolutePath.plus(File.separator).plus(BluePrintConstants.TOSCA_METADATA_DIR))
            Files.createDirectories(metaDataDir.toPath())

            val metaFile = File(
                blueprintDir.absolutePath.plus(File.separator).plus(
                    BluePrintConstants
                        .TOSCA_METADATA_ENTRY_DEFINITION_FILE
                )
            )
            Files.write(metaFile.toPath(), getMetaDataContent().toByteArray(), StandardOpenOption.CREATE_NEW)

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
                throw BluePrintException(
                    ErrorCode.BLUEPRINT_PATH_MISSING.value,
                    "couldn't get definition file under " +
                        "path(${definitionDir.absolutePath})"
                )
            }

            blueprintContext.serviceTemplate.dataTypes?.let {
                val dataTypesContent = JacksonUtils.getWrappedJson(BluePrintConstants.PATH_DATA_TYPES, it.toSortedMap(), true)
                writeTypeFile(definitionDir.absolutePath, BluePrintConstants.PATH_DATA_TYPES, dataTypesContent)
            }

            blueprintContext.serviceTemplate.relationshipTypes?.let {
                val nodeTypesContent = JacksonUtils.getWrappedJson(BluePrintConstants.PATH_RELATIONSHIP_TYPES, it.toSortedMap(), true)
                writeTypeFile(definitionDir.absolutePath, BluePrintConstants.PATH_RELATIONSHIP_TYPES, nodeTypesContent)
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

        private fun populateDefaultImports(blueprintContext: BluePrintContext) {
            // Get the Default Types
            val types = arrayListOf(
                BluePrintConstants.PATH_DATA_TYPES, BluePrintConstants.PATH_RELATIONSHIP_TYPES,
                BluePrintConstants.PATH_ARTIFACT_TYPES, BluePrintConstants.PATH_NODE_TYPES,
                BluePrintConstants.PATH_POLICY_TYPES
            )

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

        /**
         * Re Generate the Blueprint Service Template Definition file based on BluePrint Context.
         */
        private fun writeEntryDefinitionFile(blueprintContext: BluePrintContext) {

            val absoluteEntryDefinitionFile = blueprintContext.rootPath.plus(File.separator).plus(blueprintContext.entryDefinition)

            val serviceTemplate = blueprintContext.serviceTemplate

            // Clone the Service Template
            val writeServiceTemplate = serviceTemplate.clone()
            writeServiceTemplate.dataTypes = null
            writeServiceTemplate.artifactTypes = null
            writeServiceTemplate.policyTypes = null
            writeServiceTemplate.relationshipTypes = null
            writeServiceTemplate.nodeTypes = null

            // Write the Service Template
            writeDefinitionFile(absoluteEntryDefinitionFile, JacksonUtils.getJson(writeServiceTemplate, true))
        }

        fun writeDefinitionFile(definitionFileName: String, content: String) = runBlocking {
            val definitionFile = File(definitionFileName)
            // Delete the File If exists
            Files.deleteIfExists(definitionFile.toPath())

            Files.write(definitionFile.toPath(), content.toByteArray(), StandardOpenOption.CREATE_NEW)
            check(definitionFile.exists()) {
                throw BluePrintException(
                    ErrorCode.BLUEPRINT_WRITING_FAIL.value,
                    "couldn't write definition file under " +
                        "path(${definitionFile.absolutePath})"
                )
            }
        }

        private fun writeTypeFile(definitionPath: String, type: String, content: String) = runBlocking {
            val typeFile = File(definitionPath.plus(File.separator).plus("$type.json"))

            Files.write(typeFile.toPath(), content.toByteArray(), StandardOpenOption.CREATE_NEW)
            check(typeFile.exists()) {
                throw BluePrintException(
                    ErrorCode.BLUEPRINT_WRITING_FAIL.value,
                    "couldn't write $type.json file under " +
                        "path(${typeFile.absolutePath})"
                )
            }
        }

        private fun getMetaDataContent(): String {
            return "TOSCA-Meta-File-Version: 1.0.0" +
                "\nCSAR-Version: <VERSION>" +
                "\nCreated-By: <AUTHOR NAME>" +
                "\nEntry-Definitions: Definitions/<BLUEPRINT_NAME>.json" +
                "\nTemplate-Name: <BLUEPRINT_NAME>" +
                "\nTemplate-Version: <BLUEPRINT_VERSION>" +
                "\nTemplate-Type: <BLUEPRINT_TYPE>" +
                "\nTemplate-Tags: <TAGS>"
        }

        fun getBluePrintFile(fileName: String, targetPath: Path): File {
            val filePath = targetPath.resolve(fileName).toString()
            val file = File(filePath)
            check(file.exists()) {
                throw BluePrintException(
                    ErrorCode.BLUEPRINT_PATH_MISSING.value,
                    "couldn't get definition file under " +
                        "path(${file.absolutePath})"
                )
            }
            return file
        }

        fun getCbaStorageDirectory(path: String): Path {
            check(StringUtils.isNotBlank(path)) {
                throw BluePrintException(
                    ErrorCode.BLUEPRINT_PATH_MISSING.value,
                    "couldn't get " +
                        "Blueprint folder under path($path)"
                )
            }

            val fileStorageLocation = Paths.get(path).toAbsolutePath().normalize()

            if (!Files.exists(fileStorageLocation))
                Files.createDirectories(fileStorageLocation)

            return fileStorageLocation
        }

        fun compileCacheKey(basePath: String): String {
            return normalizedPathName(basePath)
        }

        private fun compileJarFileName(artifactName: String, artifactVersion: String): String {
            return "$artifactName-$artifactVersion-$COMPILED_JAR_SUFFIX"
        }

        fun compileJarFilePathName(basePath: String, artifactName: String, artifactVersion: String): String {
            return normalizedPathName(
                basePath, BluePrintConstants.TOSCA_SCRIPTS_KOTLIN_DIR,
                compileJarFileName(artifactName, artifactVersion)
            )
        }

        fun compileJarFile(basePath: String, artifactName: String, artifactVersion: String): File {
            return normalizedFile(
                compileJarFilePathName(
                    basePath,
                    artifactName, artifactVersion
                )
            )
        }

        fun stripFileExtension(fileName: String): String {
            val dotIndexe = fileName.lastIndexOf('.')

            // In case dot is in first position, we are dealing with a hidden file rather than an extension
            return if (dotIndexe > 0) fileName.substring(0, dotIndexe) else fileName
        }

        fun getURLClassLoaderFromDirectory(directory: File): URLClassLoader {
            if (!directory.exists()) {
                throw FileNotFoundException(directory.absolutePath)
            } else if (!directory.isDirectory) {
                throw NotDirectoryException(directory.absolutePath)
            }

            val urls = arrayListOf<URL>()
            directory.walkTopDown()
                .filter { it.name.endsWith(COMPILED_JAR_SUFFIX) }
                .forEach {
                    log.debug("Adding (${it.absolutePath}) to classLoader (${directory.absolutePath})")

                    urls.add(it.toURI().toURL())
                }
            return URLClassLoader(urls.toTypedArray(), this.javaClass.classLoader)
        }

        fun getURLClassLoaderFromDirectory(path: String): URLClassLoader {
            val directory = normalizedFile(path)
            return getURLClassLoaderFromDirectory(directory)
        }
    }
}

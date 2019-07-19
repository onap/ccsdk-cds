/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018-2019 IBM.
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


import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.data.ToscaMetaData
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintDefinitions
import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BluePrintScriptsServiceImpl
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintImportService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBluePrintRuntimeService
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class BluePrintMetadataUtils {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.toString())


        suspend fun toscaMetaData(basePath: String): ToscaMetaData {
            val toscaMetaPath = basePath.plus(BluePrintConstants.PATH_DIVIDER)
                    .plus(BluePrintConstants.TOSCA_METADATA_ENTRY_DEFINITION_FILE)
            return toscaMetaDataFromMetaFile(toscaMetaPath)
        }

        suspend fun entryDefinitionFile(basePath: String): String {
            val toscaMetaPath = basePath.plus(BluePrintConstants.PATH_DIVIDER)
                    .plus(BluePrintConstants.TOSCA_METADATA_ENTRY_DEFINITION_FILE)
            return toscaMetaDataFromMetaFile(toscaMetaPath).entityDefinitions
        }

        fun bluePrintEnvProperties(basePath: String): Properties {
            val blueprintsEnvFilePath = basePath.plus(File.separator)
                    .plus(BluePrintConstants.TOSCA_ENVIRONMENTS_DIR)
            return environmentFileProperties(blueprintsEnvFilePath)
        }

        fun environmentFileProperties(pathName: String): Properties {
            val properties = Properties()
            val envDir = normalizedFile(pathName)
            // Verify if the environment directory exists
            if (envDir.exists() && envDir.isDirectory) {
                //Find all available environment files
                envDir.listFiles()
                        .filter { it.name.endsWith(".properties") }
                        .forEach {
                            val istream = it.inputStream()
                            properties.load(istream)
                            istream.close()
                        }
            }
            return properties
        }

        private suspend fun toscaMetaDataFromMetaFile(metaFilePath: String): ToscaMetaData {
            val toscaMetaData = ToscaMetaData()
            val lines = normalizedFile(metaFilePath).readNBLines()
            lines.forEach { line ->
                if (line.contains(":")) {
                    val keyValue = line.split(":")
                    if (keyValue.size == 2) {
                        val value: String = keyValue[1].trim()
                        when (keyValue[0]) {
                            "TOSCA-Meta-File-Version" -> toscaMetaData.toscaMetaFileVersion = value
                            "CSAR-Version" -> toscaMetaData.csarVersion = value
                            "Created-By" -> toscaMetaData.createdBy = value
                            "Entry-Definitions" -> toscaMetaData.entityDefinitions = value
                            "Template-Name" -> toscaMetaData.templateName = value
                            "Template-Version" -> toscaMetaData.templateVersion = value
                            "Template-Tags" -> toscaMetaData.templateTags = value
                        }
                    }
                }

            }
            return toscaMetaData
        }

        fun getBluePrintRuntime(id: String, blueprintBasePath: String): BluePrintRuntimeService<MutableMap<String, JsonNode>> {

            val bluePrintContext: BluePrintContext = getBluePrintContext(blueprintBasePath)

            val bluePrintRuntimeService = DefaultBluePrintRuntimeService(id, bluePrintContext)
            bluePrintRuntimeService.put(BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH, blueprintBasePath.asJsonPrimitive())
            bluePrintRuntimeService.put(BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID, id.asJsonPrimitive())

            return bluePrintRuntimeService
        }

        suspend fun getBaseEnhancementBluePrintRuntime(id: String, blueprintBasePath: String)
                : BluePrintRuntimeService<MutableMap<String, JsonNode>> {

            val bluePrintContext: BluePrintContext = getBaseEnhancementBluePrintContext(blueprintBasePath)

            val bluePrintRuntimeService = DefaultBluePrintRuntimeService(id, bluePrintContext)
            bluePrintRuntimeService.put(BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH, blueprintBasePath.asJsonPrimitive())
            bluePrintRuntimeService.put(BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID, id.asJsonPrimitive())

            return bluePrintRuntimeService
        }

        fun getBluePrintRuntime(id: String, blueprintBasePath: String, executionContext: MutableMap<String, JsonNode>):
                BluePrintRuntimeService<MutableMap<String, JsonNode>> {
            val bluePrintContext: BluePrintContext = getBluePrintContext(blueprintBasePath)
            val bluePrintRuntimeService = DefaultBluePrintRuntimeService(id, bluePrintContext)
            executionContext.forEach {
                bluePrintRuntimeService.put(it.key, it.value)
            }

            bluePrintRuntimeService.setExecutionContext(executionContext)
            return bluePrintRuntimeService
        }

        fun getBluePrintContext(blueprintBasePath: String): BluePrintContext = runBlocking {

            val toscaMetaData: ToscaMetaData = toscaMetaData(blueprintBasePath)

            log.info("Reading blueprint path($blueprintBasePath) and entry definition file (${toscaMetaData.entityDefinitions})")

            // If the EntryDefinition is Kotlin file, compile and get Service Template
            if (toscaMetaData.entityDefinitions.endsWith("kt")) {
                readBlueprintKotlinFile(toscaMetaData, blueprintBasePath)
            } else {
                readBlueprintFile(toscaMetaData.entityDefinitions, blueprintBasePath)
            }
        }

        private suspend fun getBaseEnhancementBluePrintContext(blueprintBasePath: String): BluePrintContext {
            val toscaMetaData: ToscaMetaData = toscaMetaData(blueprintBasePath)
            // Clean Type files
            BluePrintFileUtils.deleteBluePrintTypes(blueprintBasePath)
            val rootFilePath: String = blueprintBasePath.plus(File.separator).plus(toscaMetaData.entityDefinitions)
            val rootServiceTemplate = ServiceTemplateUtils.getServiceTemplate(rootFilePath)

            // Clean the Import Definitions
            BluePrintFileUtils.cleanImportTypes(rootServiceTemplate)

            val blueprintContext = BluePrintContext(rootServiceTemplate)
            blueprintContext.rootPath = blueprintBasePath
            blueprintContext.entryDefinition = toscaMetaData.entityDefinitions
            return blueprintContext
        }

        private suspend fun readBlueprintFile(entityDefinitions: String, basePath: String): BluePrintContext {
            val normalizedBasePath = normalizedPathName(basePath)
            val rootFilePath = normalizedPathName(normalizedBasePath, entityDefinitions)
            val rootServiceTemplate = ServiceTemplateUtils.getServiceTemplate(rootFilePath)
            // Recursively Import Template files
            val schemaImportResolverUtils = BluePrintImportService(rootServiceTemplate, normalizedBasePath)
            val completeServiceTemplate = schemaImportResolverUtils.getImportResolvedServiceTemplate()
            val blueprintContext = BluePrintContext(completeServiceTemplate)
            blueprintContext.rootPath = normalizedBasePath
            blueprintContext.entryDefinition = entityDefinitions
            return blueprintContext
        }

        /** Reade the Service Template Definitions from the Kotlin file */
        private suspend fun readBlueprintKotlinFile(toscaMetaData: ToscaMetaData, basePath: String): BluePrintContext {

            checkNotNull(toscaMetaData.templateName) { "couldn't find 'Template-Name' key in TOSCA.meta" }
            checkNotNull(toscaMetaData.templateVersion) { "couldn't find 'Template-Version' key in TOSCA.meta" }

            val definitionClassName = toscaMetaData.entityDefinitions.removeSuffix(".kt")
            val normalizedBasePath = normalizedPathName(basePath)

            val bluePrintScriptsService = BluePrintScriptsServiceImpl()
            val bluePrintDefinitions = bluePrintScriptsService
                    .scriptInstance<BluePrintDefinitions>(normalizedBasePath, toscaMetaData.templateName!!,
                            toscaMetaData.templateVersion!!, definitionClassName, true)
            // Get the Service Template
            val serviceTemplate = bluePrintDefinitions.serviceTemplate()

            // Clean the Default type import Definitions
            BluePrintFileUtils.cleanImportTypes(serviceTemplate)

            val blueprintContext = BluePrintContext(serviceTemplate)
            blueprintContext.rootPath = normalizedBasePath
            blueprintContext.entryDefinition = toscaMetaData.entityDefinitions
            blueprintContext.otherDefinitions = bluePrintDefinitions.otherDefinitions()
            return blueprintContext
        }
    }
}
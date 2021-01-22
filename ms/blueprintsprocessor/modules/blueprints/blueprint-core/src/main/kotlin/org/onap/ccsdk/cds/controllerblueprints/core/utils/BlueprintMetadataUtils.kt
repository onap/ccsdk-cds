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
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.ToscaMetaData
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintDefinitions
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.readNBLines
import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BlueprintScriptsServiceImpl
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintImportService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBlueprintRuntimeService
import org.slf4j.LoggerFactory
import java.io.File
import java.util.Properties

class BlueprintMetadataUtils {
    companion object {

        private val log = LoggerFactory.getLogger(this::class.toString())

        suspend fun toscaMetaData(basePath: String): ToscaMetaData {
            val toscaMetaPath = basePath.plus(BlueprintConstants.PATH_DIVIDER)
                .plus(BlueprintConstants.TOSCA_METADATA_ENTRY_DEFINITION_FILE)
            return toscaMetaDataFromMetaFile(toscaMetaPath)
        }

        suspend fun entryDefinitionFile(basePath: String): String {
            val toscaMetaPath = basePath.plus(BlueprintConstants.PATH_DIVIDER)
                .plus(BlueprintConstants.TOSCA_METADATA_ENTRY_DEFINITION_FILE)
            return toscaMetaDataFromMetaFile(toscaMetaPath).entityDefinitions
        }

        fun bluePrintEnvProperties(basePath: String): Properties {
            val blueprintsEnvFilePath = basePath.plus(File.separator)
                .plus(BlueprintConstants.TOSCA_ENVIRONMENTS_DIR)
            return environmentFileProperties(blueprintsEnvFilePath)
        }

        fun environmentFileProperties(pathName: String): Properties {
            val properties = Properties()
            val envDir = normalizedFile(pathName)
            // Verify if the environment directory exists
            if (envDir.exists() && envDir.isDirectory) {
                // Find all available environment files
                envDir.listFiles()!!
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
                            "Template-Type" -> toscaMetaData.templateType = value
                        }
                    }
                }
            }
            return toscaMetaData
        }

        /** Get the default blueprint runtime for [id] and [blueprintBasePath] */
        suspend fun getBlueprintRuntime(id: String, blueprintBasePath: String):
            BlueprintRuntimeService<MutableMap<String, JsonNode>> {
                val bluePrintContext: BlueprintContext = getBlueprintContext(blueprintBasePath)
                return getBlueprintRuntime(id, bluePrintContext)
            }

        /** Get the default blocking blueprint runtime api for [id] and [blueprintBasePath] used in testing */
        fun bluePrintRuntime(id: String, blueprintBasePath: String):
            BlueprintRuntimeService<MutableMap<String, JsonNode>> = runBlocking {
                val bluePrintContext: BlueprintContext = getBlueprintContext(blueprintBasePath)
                getBlueprintRuntime(id, bluePrintContext)
            }

        /** Get the default blueprint runtime from [bluePrintContext] */
        fun getBlueprintRuntime(id: String, bluePrintContext: BlueprintContext):
            BlueprintRuntimeService<MutableMap<String, JsonNode>> {
                checkNotEmpty(bluePrintContext.rootPath) { "blueprint context root path is missing." }
                checkNotEmpty(bluePrintContext.entryDefinition) { "blueprint context entry definition is missing." }
                val blueprintBasePath = bluePrintContext.rootPath
                val bluePrintRuntimeService = DefaultBlueprintRuntimeService(id, bluePrintContext)
                bluePrintRuntimeService.put(
                    BlueprintConstants.PROPERTY_BLUEPRINT_BASE_PATH,
                    blueprintBasePath.asJsonPrimitive()
                )
                bluePrintRuntimeService.put(BlueprintConstants.PROPERTY_BLUEPRINT_PROCESS_ID, id.asJsonPrimitive())
                return bluePrintRuntimeService
            }

        /** Get the blueprint runtime for enhancement start for [id] and [blueprintBasePath] */
        suspend fun getBaseEnhancementBlueprintRuntime(id: String, blueprintBasePath: String):
            BlueprintRuntimeService<MutableMap<String, JsonNode>> {

                val bluePrintContext: BlueprintContext = getBaseEnhancementBlueprintContext(blueprintBasePath)

                val bluePrintRuntimeService = DefaultBlueprintRuntimeService(id, bluePrintContext)
                bluePrintRuntimeService.put(
                    BlueprintConstants.PROPERTY_BLUEPRINT_BASE_PATH,
                    blueprintBasePath.asJsonPrimitive()
                )
                bluePrintRuntimeService.put(BlueprintConstants.PROPERTY_BLUEPRINT_PROCESS_ID, id.asJsonPrimitive())

                return bluePrintRuntimeService
            }

        /** Get the default blueprint runtime for enhancement start for [id],  [blueprintBasePath] and [executionContext] */
        suspend fun getBlueprintRuntime(
            id: String,
            blueprintBasePath: String,
            executionContext: MutableMap<String, JsonNode>
        ):
            BlueprintRuntimeService<MutableMap<String, JsonNode>> {
                val bluePrintContext: BlueprintContext = getBlueprintContext(blueprintBasePath)
                val bluePrintRuntimeService = DefaultBlueprintRuntimeService(id, bluePrintContext)
                executionContext.forEach {
                    bluePrintRuntimeService.put(it.key, it.value)
                }

                bluePrintRuntimeService.setExecutionContext(executionContext)
                return bluePrintRuntimeService
            }

        /** Get the default blueprint context for [blueprintBasePath]*/
        suspend fun getBlueprintContext(blueprintBasePath: String): BlueprintContext {

            val toscaMetaData: ToscaMetaData = toscaMetaData(blueprintBasePath)

            log.info(
                "Reading blueprint type(${toscaMetaData.templateType}) path($blueprintBasePath) " +
                    "and entry definition file (${toscaMetaData.entityDefinitions})"
            )

            // If the EntryDefinition is Kotlin file, compile and get Service Template
            val bluePrintContext = when (toscaMetaData.templateType.toUpperCase()) {
                BlueprintConstants.BLUEPRINT_TYPE_KOTLIN_DSL -> readBlueprintKotlinFile(
                    toscaMetaData,
                    blueprintBasePath
                )
                BlueprintConstants.BLUEPRINT_TYPE_GENERIC_SCRIPT -> readBlueprintGenericScript(
                    toscaMetaData,
                    blueprintBasePath
                )
                BlueprintConstants.BLUEPRINT_TYPE_DEFAULT -> readBlueprintFile(
                    toscaMetaData.entityDefinitions,
                    blueprintBasePath
                )
                else ->
                    throw BlueprintException(
                        "Unknown blueprint type(${toscaMetaData.templateType}), " +
                            "It should be any one of these types[${BlueprintConstants.BLUEPRINT_TYPE_KOTLIN_DSL}," +
                            "${BlueprintConstants.BLUEPRINT_TYPE_GENERIC_SCRIPT}, " +
                            "${BlueprintConstants.BLUEPRINT_TYPE_DEFAULT}]"
                    )
            }
            // Copy the metadata info
            copyMetaInfoToServiceTemplate(toscaMetaData, bluePrintContext.serviceTemplate)

            return bluePrintContext
        }

        private suspend fun getBaseEnhancementBlueprintContext(blueprintBasePath: String): BlueprintContext {
            val toscaMetaData: ToscaMetaData = toscaMetaData(blueprintBasePath)

            // Clean Type files
            BlueprintFileUtils.deleteBlueprintTypes(blueprintBasePath)
            val rootFilePath: String = blueprintBasePath.plus(File.separator).plus(toscaMetaData.entityDefinitions)
            val rootServiceTemplate = ServiceTemplateUtils.getServiceTemplate(rootFilePath)

            // Copy the metadata info
            copyMetaInfoToServiceTemplate(toscaMetaData, rootServiceTemplate)

            // Clean the Import Definitions
            BlueprintFileUtils.cleanImportTypes(rootServiceTemplate)

            val blueprintContext = BlueprintContext(rootServiceTemplate)
            blueprintContext.rootPath = blueprintBasePath
            blueprintContext.entryDefinition = toscaMetaData.entityDefinitions
            return blueprintContext
        }

        /** copy metadata defined in [toscaMetaData] to [serviceTemplate] */
        private fun copyMetaInfoToServiceTemplate(toscaMetaData: ToscaMetaData, serviceTemplate: ServiceTemplate) {
            if (serviceTemplate.metadata == null) serviceTemplate.metadata = mutableMapOf()
            val metadata = serviceTemplate.metadata!!
            metadata[BlueprintConstants.METADATA_TEMPLATE_AUTHOR] = toscaMetaData.createdBy
            metadata[BlueprintConstants.METADATA_TEMPLATE_NAME] = toscaMetaData.templateName
            metadata[BlueprintConstants.METADATA_TEMPLATE_VERSION] = toscaMetaData.templateVersion
            metadata[BlueprintConstants.METADATA_TEMPLATE_TAGS] = toscaMetaData.templateTags
            metadata[BlueprintConstants.METADATA_TEMPLATE_TYPE] = toscaMetaData.templateType
        }

        private suspend fun readBlueprintFile(entityDefinitions: String, basePath: String): BlueprintContext {
            val normalizedBasePath = normalizedPathName(basePath)
            val rootFilePath = normalizedPathName(normalizedBasePath, entityDefinitions)
            val rootServiceTemplate = ServiceTemplateUtils.getServiceTemplate(rootFilePath)

            // Recursively Import Template files
            val schemaImportResolverUtils = BlueprintImportService(rootServiceTemplate, normalizedBasePath)
            val completeServiceTemplate = schemaImportResolverUtils.getImportResolvedServiceTemplate()
            val blueprintContext = BlueprintContext(completeServiceTemplate)
            blueprintContext.rootPath = normalizedBasePath
            blueprintContext.entryDefinition = entityDefinitions
            return blueprintContext
        }

        /** Reade the Service Template Definitions from the Kotlin file */
        private suspend fun readBlueprintKotlinFile(toscaMetaData: ToscaMetaData, basePath: String): BlueprintContext {

            val definitionClassName = toscaMetaData.entityDefinitions.removeSuffix(".kt")
            val normalizedBasePath = normalizedPathName(basePath)

            val bluePrintScriptsService = BlueprintScriptsServiceImpl()
            val bluePrintDefinitions = bluePrintScriptsService
                .scriptInstance<BlueprintDefinitions>(
                    normalizedBasePath, toscaMetaData.templateName,
                    toscaMetaData.templateVersion, definitionClassName, false
                )
            // Get the Service Template
            val serviceTemplate = bluePrintDefinitions.serviceTemplate()

            // Clean the Default type import Definitions
            BlueprintFileUtils.cleanImportTypes(serviceTemplate)

            val blueprintContext = BlueprintContext(serviceTemplate)
            blueprintContext.rootPath = normalizedBasePath
            blueprintContext.entryDefinition = toscaMetaData.entityDefinitions
            blueprintContext.otherDefinitions = bluePrintDefinitions.otherDefinitions()
            return blueprintContext
        }

        /** Reade the Service Template Definitions from the generic script types */
        private fun readBlueprintGenericScript(toscaMetaData: ToscaMetaData, basePath: String): BlueprintContext {
            return BlueprintContext(ServiceTemplate())
        }
    }
}

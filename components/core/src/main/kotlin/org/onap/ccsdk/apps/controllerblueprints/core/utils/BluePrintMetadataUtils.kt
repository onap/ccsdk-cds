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
import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.io.FileUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.apps.controllerblueprints.core.data.ToscaMetaData
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.core.service.DefaultBluePrintRuntimeService
import java.io.File
import java.nio.charset.Charset

object BluePrintMetadataUtils {
    private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())

    @JvmStatic
    fun toscaMetaData(basePath: String): ToscaMetaData {
        val toscaMetaPath = basePath.plus(BluePrintConstants.PATH_DIVIDER).plus("TOSCA-Metadata/TOSCA.meta")
        return toscaMetaDataFromMetaFile(toscaMetaPath)
    }

    @JvmStatic
    fun toscaMetaDataFromMetaFile(metaFilePath: String): ToscaMetaData {
        val toscaMetaData = ToscaMetaData()
        val lines: MutableList<String> = FileUtils.readLines(File(metaFilePath), Charset.defaultCharset())
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
                        "Template-Tags" -> toscaMetaData.templateTags = value
                    }
                }
            }

        }
        return toscaMetaData
    }

    @JvmStatic
    fun getBluePrintContext(blueprintBasePath: String): BluePrintContext {

        val toscaMetaData: ToscaMetaData = toscaMetaData(blueprintBasePath)

        log.info("Processing blueprint base path ($blueprintBasePath) and entry definition file (${toscaMetaData.entityDefinitions})")

        return readBlueprintFile(toscaMetaData.entityDefinitions, blueprintBasePath)
    }

    @JvmStatic
    fun getBluePrintRuntime(id: String, blueprintBasePath: String): BluePrintRuntimeService<MutableMap<String, JsonNode>> {

        val bluePrintContext: BluePrintContext = getBluePrintContext(blueprintBasePath)

        val context: MutableMap<String, JsonNode> = hashMapOf()
        context[BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH] = blueprintBasePath.asJsonPrimitive()
        context[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID] = id.asJsonPrimitive()

        val bluePrintRuntimeService = DefaultBluePrintRuntimeService(id, bluePrintContext)
        bluePrintRuntimeService.setExecutionContext(context)

        return bluePrintRuntimeService
    }

    @JvmStatic
    fun getBluePrintRuntime(id: String, blueprintBasePath: String, executionContext: MutableMap<String, JsonNode>): BluePrintRuntimeService<MutableMap<String, JsonNode>> {
        val bluePrintContext: BluePrintContext = getBluePrintContext(blueprintBasePath)
        val bluePrintRuntimeService = DefaultBluePrintRuntimeService(id, bluePrintContext)
        bluePrintRuntimeService.setExecutionContext(executionContext)
        return bluePrintRuntimeService
    }

    @JvmStatic
    fun readBlueprintFile(entityDefinitions: String, basePath: String): BluePrintContext {
        val rootFilePath: String = basePath.plus(File.separator).plus(entityDefinitions)
        val rootServiceTemplate = ServiceTemplateUtils.getServiceTemplate(rootFilePath)
        // TODO ("Fix for Multiple Service Template file definitions")
//        val schemaImportResolverUtils = BluePrintResolverService(rootServiceTemplate, basePath)
//        val completeServiceTemplate = schemaImportResolverUtils.getImportResolvedServiceTemplate()
        return BluePrintContext(rootServiceTemplate)
    }
}
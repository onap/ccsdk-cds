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


import org.apache.commons.io.FileUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.data.ToscaMetaData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset

object BluePrintMetadataUtils {
    private val logger: Logger = LoggerFactory.getLogger(this::class.toString())

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

    /*
    fun getBluePrintContext(blueprintBasePath: String): BluePrintContext {

        val metaDataFile = StringBuilder().append(blueprintBasePath).append(File.separator)
                .append(BluePrintConstants.DEFAULT_TOSCA_METADATA_ENTRY_DEFINITION_FILE).toString()

        val toscaMetaData: ToscaMetaData = BluePrintMetadataUtils.toscaMetaData(metaDataFile)

        logger.info("Processing blueprint base path ({}) and entry definition file ({})", blueprintBasePath, toscaMetaData.entityDefinitions)

        return BluePrintParserFactory.instance(BluePrintConstants.TYPE_DEFAULT)!!
                .readBlueprintFile(toscaMetaData.entityDefinitions!!, blueprintBasePath)
    }

    fun getBluePrintRuntime(requestId: String, blueprintBasePath: String): BluePrintRuntimeService {

        val metaDataFile = StringBuilder().append(blueprintBasePath).append(File.separator)
                .append(BluePrintConstants.DEFAULT_TOSCA_METADATA_ENTRY_DEFINITION_FILE).toString()

        val toscaMetaData: ToscaMetaData = BluePrintMetadataUtils.toscaMetaData(metaDataFile)

        logger.info("Processing blueprint base path ({}) and entry definition file ({})", blueprintBasePath, toscaMetaData.entityDefinitions)

        val bluePrintContext: BluePrintContext = BluePrintParserFactory.instance(BluePrintConstants.TYPE_DEFAULT)!!
                .readBlueprintFile(toscaMetaData.entityDefinitions!!, blueprintBasePath)

        val context: MutableMap<String, Any> = hashMapOf()
        context[BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH] = blueprintBasePath
        context[BluePrintConstants.PROPERTY_BLUEPRINT_PROCESS_ID] = requestId

        val bluePrintRuntimeService: BluePrintRuntimeService = BluePrintRuntimeService(bluePrintContext, context)

        return bluePrintRuntimeService
    }
    */
}
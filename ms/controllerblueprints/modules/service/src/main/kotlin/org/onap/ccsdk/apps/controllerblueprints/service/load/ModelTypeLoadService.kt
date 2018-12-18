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

package org.onap.ccsdk.apps.controllerblueprints.service.load

import com.att.eelf.configuration.EELFManager
import kotlinx.coroutines.*
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.text.StrBuilder
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.apps.controllerblueprints.core.data.DataType
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.service.ModelTypeService
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType
import org.springframework.stereotype.Service
import java.io.File
import java.nio.charset.Charset

@Service
open class ModelTypeLoadService(private val modelTypeService: ModelTypeService) {

    private val log = EELFManager.getInstance().getLogger(ModelTypeLoadService::class.java)
    private val updateBySystem = "System"

    open fun loadPathsModelType(modelTypePaths: List<String>) {
        modelTypePaths.forEach { loadPathModelType(it) }
    }

    open fun loadPathModelType(modelTypePath: String) = runBlocking {
        log.info(" *************************** loadModelType **********************")
        try {
            val errorBuilder = StrBuilder()

            coroutineScope {
                val dataTypeFiles = File("$modelTypePath/data_type").listFiles()

                val deferredResults = mutableListOf<Deferred<Unit>>()

                for (file in dataTypeFiles) deferredResults += async { loadDataType(file, errorBuilder) }

                deferredResults.awaitAll()
            }

            coroutineScope {
                val artifactTypefiles = File("$modelTypePath/artifact_type").listFiles()

                val deferredResults = mutableListOf<Deferred<Unit>>()

                for (file in artifactTypefiles) deferredResults += async { loadArtifactType(file, errorBuilder) }

                deferredResults.awaitAll()
            }

            coroutineScope {
                val nodeTypeFiles = File("$modelTypePath/node_type").listFiles()

                val deferredResults = mutableListOf<Deferred<Unit>>()

                for (file in nodeTypeFiles) deferredResults += async { loadNodeType(file, errorBuilder) }
                deferredResults.awaitAll()
            }

            if (!errorBuilder.isEmpty) {
                log.error(errorBuilder.toString())
            }
        } catch (e: Exception) {
            log.error("Failed to loade ModelTypes under($modelTypePath)", e)
        }
    }

    private fun loadDataType(file: File, errorBuilder: StrBuilder) {
        try {
            log.trace("Loading DataType(${file.name}")
            val dataKey = FilenameUtils.getBaseName(file.name)
            val definitionContent = file.readText(Charset.defaultCharset())
            val dataType = JacksonUtils.readValue(definitionContent, DataType::class.java)
            checkNotNull(dataType) { "failed to get data type from file : ${file.name}" }

            val modelType = ModelType()
            modelType.definitionType = BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE
            modelType.derivedFrom = dataType.derivedFrom
            modelType.description = dataType.description
            modelType.definition = JacksonUtils.jsonNode(definitionContent)
            modelType.modelName = dataKey
            modelType.version = dataType.version
            modelType.updatedBy = updateBySystem
            modelType.tags = (dataKey + "," + dataType.derivedFrom + "," + BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE)
            modelTypeService.saveModel(modelType)
            log.trace("DataType(${file.name}) loaded successfully ")
        } catch (e: Exception) {
            errorBuilder.appendln("Couldn't load DataType(${file.name}: ${e.message}")
        }
    }

    private fun loadArtifactType(file: File, errorBuilder: StrBuilder) {
        try {
            log.trace("Loading ArtifactType(${file.name}")
            val dataKey = FilenameUtils.getBaseName(file.name)
            val definitionContent = file.readText(Charset.defaultCharset())
            val artifactType = JacksonUtils.readValue(definitionContent, ArtifactType::class.java)
            checkNotNull(artifactType) { "failed to get artifact type from file : ${file.name}" }

            val modelType = ModelType()
            modelType.definitionType = BluePrintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE
            modelType.derivedFrom = artifactType.derivedFrom
            modelType.description = artifactType.description
            modelType.definition = JacksonUtils.jsonNode(definitionContent)
            modelType.modelName = dataKey
            modelType.version = artifactType.version
            modelType.updatedBy = updateBySystem
            modelType.tags = (dataKey + "," + artifactType.derivedFrom + "," + BluePrintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE)
            modelTypeService.saveModel(modelType)
            log.trace("ArtifactType(${file.name}) loaded successfully ")
        } catch (e: Exception) {
            errorBuilder.appendln("Couldn't load ArtifactType(${file.name}: ${e.message}")
        }
    }

    private fun loadNodeType(file: File, errorBuilder: StrBuilder) {
        try {
            log.trace("Loading NodeType(${file.name}")
            val nodeKey = FilenameUtils.getBaseName(file.name)
            val definitionContent = file.readText(Charset.defaultCharset())
            val nodeType = JacksonUtils.readValue(definitionContent, NodeType::class.java)
            checkNotNull(nodeType) { "failed to get node type from file : ${file.name}" }

            val modelType = ModelType()
            modelType.definitionType = BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE
            modelType.derivedFrom = nodeType.derivedFrom
            modelType.description = nodeType.description
            modelType.definition = JacksonUtils.jsonNode(definitionContent)
            modelType.modelName = nodeKey
            modelType.version = nodeType.version
            modelType.updatedBy = updateBySystem
            modelType.tags = (nodeKey + "," + BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE + "," + nodeType.derivedFrom)
            modelTypeService.saveModel(modelType)
            log.trace("NodeType(${file.name}) loaded successfully ")
        } catch (e: Exception) {
            errorBuilder.appendln("Couldn't load NodeType(${file.name}: ${e.message}")
        }
    }

}
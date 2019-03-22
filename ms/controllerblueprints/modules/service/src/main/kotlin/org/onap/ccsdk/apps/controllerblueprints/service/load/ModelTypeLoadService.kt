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
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType
import org.onap.ccsdk.apps.controllerblueprints.service.handler.ModelTypeHandler
import org.springframework.stereotype.Service
import java.io.File
import java.nio.charset.Charset

@Service
open class ModelTypeLoadService(private val modelTypeHandler: ModelTypeHandler) {

    private val log = EELFManager.getInstance().getLogger(ModelTypeLoadService::class.java)
    private val updateBySystem = "System"

    open suspend fun loadPathsModelType(modelTypePaths: List<String>) {
        modelTypePaths.forEach { runBlocking { loadPathModelType(it) } }
    }

    /**
     * Load the Model Type file content from the defined path, Load of sequencing should be maintained.
     */
    open suspend fun loadPathModelType(modelTypePath: String) = runBlocking {
        log.info(" *************************** loadModelType **********************")
        try {
            val errorBuilder = StrBuilder()

            coroutineScope {
                val dataTypeFiles = File("$modelTypePath/data_type").listFiles()

                val deferredResults = mutableListOf<Deferred<Unit>>()

                for (file in dataTypeFiles) deferredResults += async {
                    loadModelType(file, DataType::class.java, errorBuilder)
                }

                deferredResults.awaitAll()
            }

            coroutineScope {
                val artifactTypeFiles = File("$modelTypePath/artifact_type").listFiles()

                val deferredResults = mutableListOf<Deferred<Unit>>()

                for (file in artifactTypeFiles) deferredResults += async {
                    loadModelType(file,
                            ArtifactType::class.java, errorBuilder)
                }

                deferredResults.awaitAll()
            }

            coroutineScope {
                val relationshipTypeFiles = File("$modelTypePath/relationship_type").listFiles()

                val deferredResults = mutableListOf<Deferred<Unit>>()

                for (file in relationshipTypeFiles) deferredResults += async {
                    loadModelType(file,
                            RelationshipType::class.java, errorBuilder)
                }

                deferredResults.awaitAll()
            }

            coroutineScope {
                val nodeTypeFiles = File("$modelTypePath/node_type").listFiles()

                val deferredResults = mutableListOf<Deferred<Unit>>()

                for (file in nodeTypeFiles) deferredResults += async {
                    loadModelType(file,
                            NodeType::class.java, errorBuilder)
                }
                deferredResults.awaitAll()
            }

            if (!errorBuilder.isEmpty) {
                log.error(errorBuilder.toString())
            }
        } catch (e: Exception) {
            log.error("Failed to loade ModelTypes under($modelTypePath)", e)
        }
    }

    private inline fun <reified T> loadModelType(file: File, classType: Class<T>, errorBuilder: StrBuilder) {
        try {
            log.trace("Loading ${classType.name} (${file.name})")
            val dataKey = FilenameUtils.getBaseName(file.name)
            val definitionContent = file.readText(Charset.defaultCharset())
            val definition = JacksonUtils.readValue(definitionContent, classType) as EntityType
            //checkNotNull(definition) { "failed to get data type from file : ${file.name}" }

            val modelType = ModelType()
            val definitionType: String?
            when (T::class) {
                DataType::class -> {
                    definitionType = BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE
                }
                RelationshipType::class -> {
                    definitionType = BluePrintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE
                }
                ArtifactType::class -> {
                    definitionType = BluePrintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE
                }
                NodeType::class -> {
                    definitionType = BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE
                }
                else -> {
                    throw BluePrintException("couldn't process model type($classType) definition")
                }
            }
            modelType.definitionType = definitionType
            modelType.derivedFrom = definition.derivedFrom
            modelType.description = definition.description
            modelType.definition = JacksonUtils.jsonNode(definitionContent)
            modelType.modelName = dataKey
            modelType.version = definition.version
            modelType.updatedBy = updateBySystem
            modelType.tags = (dataKey + "," + definition.derivedFrom + "," + definitionType)
            modelTypeHandler.saveModel(modelType)
            log.trace("${classType.name}(${file.name}) loaded successfully ")
        } catch (e: Exception) {
            errorBuilder.appendln("Couldn't load ${classType.name}(${file.name}: ${e.message}")
        }
    }
}
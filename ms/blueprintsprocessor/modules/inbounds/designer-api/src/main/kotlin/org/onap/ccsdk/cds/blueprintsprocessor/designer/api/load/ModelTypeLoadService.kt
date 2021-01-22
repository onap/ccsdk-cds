/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.load

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.text.StrBuilder
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ModelType
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler.ModelTypeHandler
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.EntityType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.readNBText
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
open class ModelTypeLoadService(private val modelTypeHandler: ModelTypeHandler) {

    private val log = LoggerFactory.getLogger(ModelTypeLoadService::class.java)
    private val updateBySystem = "System"

    open suspend fun loadPathsModelType(modelTypePaths: List<String>) {
        modelTypePaths.forEach {
            loadPathModelType(it)
        }
    }

    /**
     * Load the Model Type file content from the defined path, Load of sequencing should be maintained.
     */
    open suspend fun loadPathModelType(modelTypePath: String) {
        log.info(" ****** loadModelType($modelTypePath) ********")
        try {
            val errorBuilder = StrBuilder()

            coroutineScope {
                val dataTypeFiles = normalizedFile("$modelTypePath", "data_type").listFiles()
                val deferred = dataTypeFiles.map {
                    async {
                        loadModelType(it, DataType::class.java, errorBuilder)
                    }
                }
                deferred.awaitAll()
            }

            coroutineScope {
                val artifactTypeFiles = normalizedFile("$modelTypePath", "artifact_type").listFiles()
                val deferred = artifactTypeFiles.map {
                    async {
                        loadModelType(it, ArtifactType::class.java, errorBuilder)
                    }
                }
                deferred.awaitAll()
            }

            coroutineScope {
                val relationshipTypeFiles = normalizedFile("$modelTypePath", "relationship_type").listFiles()
                val deferred = relationshipTypeFiles.map {
                    async {
                        loadModelType(it, RelationshipType::class.java, errorBuilder)
                    }
                }
                deferred.awaitAll()
            }

            coroutineScope {
                val nodeTypeFiles = normalizedFile("$modelTypePath", "node_type").listFiles()
                val deferred = nodeTypeFiles.map {
                    async {
                        loadModelType(it, NodeType::class.java, errorBuilder)
                    }
                }
                deferred.awaitAll()
            }

            if (!errorBuilder.isEmpty) {
                log.error(errorBuilder.toString())
            }
        } catch (e: Exception) {
            log.error("Failed to loade ModelTypes under($modelTypePath)", e)
        }
    }

    private suspend inline fun <reified T> loadModelType(file: File, classType: Class<T>, errorBuilder: StrBuilder) {
        try {
            log.trace("Loading ${classType.name} (${file.name})")
            val dataKey = FilenameUtils.getBaseName(file.name)
            val definitionContent = file.readNBText()
            val definition = JacksonUtils.readValue(definitionContent, classType) as EntityType
            // checkNotNull(definition) { "failed to get data type from file : ${file.name}" }

            val modelType = ModelType()
            val definitionType: String?
            when (T::class) {
                DataType::class -> {
                    definitionType = BlueprintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE
                }
                RelationshipType::class -> {
                    definitionType = BlueprintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE
                }
                ArtifactType::class -> {
                    definitionType = BlueprintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE
                }
                NodeType::class -> {
                    definitionType = BlueprintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE
                }
                else -> {
                    throw BlueprintException("couldn't process model type($classType) definition")
                }
            }
            modelType.definitionType = definitionType
            modelType.derivedFrom = definition.derivedFrom
            modelType.description = definition.description!!
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

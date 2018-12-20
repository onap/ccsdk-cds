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

package org.onap.ccsdk.apps.controllerblueprints.service

import com.fasterxml.jackson.databind.JsonNode
import com.google.common.base.Preconditions
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ModelTypeRepository
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ResourceDictionaryRepository
import org.springframework.stereotype.Service

interface ResourceDefinitionRepoService : BluePrintRepoService {

    @Throws(BluePrintException::class)
    fun getResourceDefinition(resourceDefinitionName: String): ResourceDefinition
}

@Service
open class BluePrintRepoFileService(private val modelTypeRepository: ModelTypeRepository,
                                    private val resourceDictionaryRepository: ResourceDictionaryRepository) : ResourceDefinitionRepoService {

    @Throws(BluePrintException::class)
    override fun getNodeType(nodeTypeName: String): NodeType {
        return getModelType(nodeTypeName, NodeType::class.java)
                ?: throw BluePrintException("couldn't get NodeType($nodeTypeName)")
    }

    @Throws(BluePrintException::class)
    override fun getDataType(dataTypeName: String): DataType {
        return getModelType(dataTypeName, DataType::class.java)
                ?: throw BluePrintException("couldn't get DataType($dataTypeName)")
    }

    @Throws(BluePrintException::class)
    override fun getArtifactType(artifactTypeName: String): ArtifactType {
        return getModelType(artifactTypeName, ArtifactType::class.java)
                ?: throw BluePrintException("couldn't get ArtifactType($artifactTypeName)")
    }

    @Throws(BluePrintException::class)
    override fun getRelationshipType(relationshipTypeName: String): RelationshipType {
        return getModelType(relationshipTypeName, RelationshipType::class.java)
                ?: throw BluePrintException("couldn't get RelationshipType($relationshipTypeName)")
    }

    @Throws(BluePrintException::class)
    override fun getCapabilityDefinition(capabilityDefinitionName: String): CapabilityDefinition {
        return getModelType(capabilityDefinitionName, CapabilityDefinition::class.java)
                ?: throw BluePrintException("couldn't get CapabilityDefinition($capabilityDefinitionName)")
    }

    @Throws(BluePrintException::class)
    override fun getResourceDefinition(resourceDefinitionName: String): ResourceDefinition {
        val dbResourceDictionary = resourceDictionaryRepository.findByName(resourceDefinitionName)
        return if (dbResourceDictionary.isPresent) {
            dbResourceDictionary.get().definition
        } else {
            throw BluePrintException(String.format("failed to get resource dictionary (%s) from repo", resourceDefinitionName))
        }
    }

    @Throws(BluePrintException::class)
    private fun <T> getModelType(modelName: String, valueClass: Class<T>): T? {
        Preconditions.checkArgument(StringUtils.isNotBlank(modelName),
                "Failed to get model from repo, model name is missing")

        val modelDefinition = getModelDefinition(modelName)
        Preconditions.checkNotNull(modelDefinition,
                String.format("Failed to get model content for model name (%s)", modelName))

        return JacksonUtils.readValue(modelDefinition, valueClass)
    }

    @Throws(BluePrintException::class)
    private fun getModelDefinition(modelName: String): JsonNode {
        val modelDefinition: JsonNode
        val modelTypeDb = modelTypeRepository.findByModelName(modelName)
        if (modelTypeDb != null) {
            modelDefinition = modelTypeDb.definition
        } else {
            throw BluePrintException(String.format("failed to get model definition (%s) from repo", modelName))
        }
        return modelDefinition
    }
}
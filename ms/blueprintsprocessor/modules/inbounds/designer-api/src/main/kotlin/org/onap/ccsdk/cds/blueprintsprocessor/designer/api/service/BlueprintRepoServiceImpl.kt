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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.service

import com.fasterxml.jackson.databind.JsonNode
import com.google.common.base.Preconditions
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.repository.ModelTypeRepository
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.repository.ResourceDictionaryRepository
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.CapabilityDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintRepoService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.springframework.stereotype.Service

interface ResourceDefinitionRepoService : BlueprintRepoService {

    @Throws(BlueprintException::class)
    fun getResourceDefinition(resourceDefinitionName: String): ResourceDefinition
}

@Service
open class BlueprintRepoFileService(
    private val modelTypeRepository: ModelTypeRepository,
    private val resourceDictionaryRepository: ResourceDictionaryRepository
) : ResourceDefinitionRepoService {

    @Throws(BlueprintException::class)
    override fun getNodeType(nodeTypeName: String): NodeType {
        return getModelType(nodeTypeName, NodeType::class.java)
            ?: throw BlueprintException("couldn't get NodeType($nodeTypeName)")
    }

    @Throws(BlueprintException::class)
    override fun getDataType(dataTypeName: String): DataType {
        return getModelType(dataTypeName, DataType::class.java)
            ?: throw BlueprintException("couldn't get DataType($dataTypeName)")
    }

    @Throws(BlueprintException::class)
    override fun getArtifactType(artifactTypeName: String): ArtifactType {
        return getModelType(artifactTypeName, ArtifactType::class.java)
            ?: throw BlueprintException("couldn't get ArtifactType($artifactTypeName)")
    }

    @Throws(BlueprintException::class)
    override fun getRelationshipType(relationshipTypeName: String): RelationshipType {
        return getModelType(relationshipTypeName, RelationshipType::class.java)
            ?: throw BlueprintException("couldn't get RelationshipType($relationshipTypeName)")
    }

    @Throws(BlueprintException::class)
    override fun getCapabilityDefinition(capabilityDefinitionName: String): CapabilityDefinition {
        return getModelType(capabilityDefinitionName, CapabilityDefinition::class.java)
            ?: throw BlueprintException("couldn't get CapabilityDefinition($capabilityDefinitionName)")
    }

    @Throws(BlueprintException::class)
    override fun getResourceDefinition(resourceDefinitionName: String): ResourceDefinition {
        val dbResourceDictionary = resourceDictionaryRepository.findByName(resourceDefinitionName)
        return if (dbResourceDictionary != null) {
            dbResourceDictionary.definition
        } else {
            throw BlueprintException(String.format("failed to get resource dictionary (%s) from repo", resourceDefinitionName))
        }
    }

    @Throws(BlueprintException::class)
    private fun <T> getModelType(modelName: String, valueClass: Class<T>): T? {
        Preconditions.checkArgument(
            StringUtils.isNotBlank(modelName),
            "Failed to get model from repo, model name is missing"
        )

        val modelDefinition = getModelDefinition(modelName)
        Preconditions.checkNotNull(
            modelDefinition,
            String.format("Failed to get model content for model name (%s)", modelName)
        )

        return JacksonUtils.readValue(modelDefinition, valueClass)
    }

    @Throws(BlueprintException::class)
    private fun getModelDefinition(modelName: String): JsonNode {
        val modelDefinition: JsonNode
        val modelTypeDb = modelTypeRepository.findByModelName(modelName)
        if (modelTypeDb != null) {
            modelDefinition = modelTypeDb.definition
        } else {
            throw BlueprintException(String.format("failed to get model definition (%s) from repo", modelName))
        }
        return modelDefinition
    }
}

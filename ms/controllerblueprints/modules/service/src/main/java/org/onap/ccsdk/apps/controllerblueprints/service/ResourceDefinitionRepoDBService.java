/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.controllerblueprints.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.data.*;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.service.ResourceDefinitionRepoService;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ResourceDictionary;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ModelTypeRepository;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ResourceDictionaryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ResourceDefinitionRepoDBService
 *
 * @author Brinda Santh
 */
@Service
@SuppressWarnings("unused")
public class ResourceDefinitionRepoDBService implements ResourceDefinitionRepoService {

    private ModelTypeRepository modelTypeRepository;
    private ResourceDictionaryRepository resourceDictionaryRepository;

    @SuppressWarnings("unused")
    public ResourceDefinitionRepoDBService(ModelTypeRepository modelTypeRepository,
                                           ResourceDictionaryRepository resourceDictionaryRepository) {
        this.modelTypeRepository = modelTypeRepository;
        this.resourceDictionaryRepository = resourceDictionaryRepository;
    }

    @Override
    public NodeType getNodeType(@NotNull String nodeTypeName) throws BluePrintException {
        return getModelType(nodeTypeName, NodeType.class);
    }

    @Override
    public DataType getDataType(@NotNull String dataTypeName) throws BluePrintException {
        return getModelType(dataTypeName, DataType.class);
    }

    @Override
    public ArtifactType getArtifactType(@NotNull String artifactTypeName) throws BluePrintException {
        return getModelType(artifactTypeName, ArtifactType.class);
    }

    @Override
    public RelationshipType getRelationshipType(@NotNull String relationshipTypeName) throws BluePrintException {
        return getModelType(relationshipTypeName, RelationshipType.class);
    }

    @Override
    public CapabilityDefinition getCapabilityDefinition(@NotNull String capabilityDefinitionName) throws BluePrintException {
        return getModelType(capabilityDefinitionName, CapabilityDefinition.class);
    }

    @NotNull
    @Override
    public ResourceDefinition getResourceDefinition(@NotNull String resourceDefinitionName) throws BluePrintException {
        Optional<ResourceDictionary> dbResourceDictionary = resourceDictionaryRepository.findByName(resourceDefinitionName);
        if (dbResourceDictionary.isPresent()) {
            return dbResourceDictionary.get().getDefinition();
        } else {
            throw new BluePrintException(String.format("failed to get resource dictionary (%s) from repo", resourceDefinitionName));
        }
    }

    private <T> T getModelType(String modelName, Class<T> valueClass) throws BluePrintException {
        Preconditions.checkArgument(StringUtils.isNotBlank(modelName),
                "Failed to get model from repo, model name is missing");

        JsonNode modelDefinition = getModelDefinition(modelName);
        Preconditions.checkNotNull(modelDefinition,
                String.format("Failed to get model content for model name (%s)", modelName));

        return JacksonUtils.readValue(modelDefinition, valueClass);
    }

    private JsonNode getModelDefinition(String modelName) throws BluePrintException {
        JsonNode modelDefinition;
        Optional<ModelType> modelTypeDb = modelTypeRepository.findByModelName(modelName);
        if (modelTypeDb.isPresent()) {
            modelDefinition = modelTypeDb.get().getDefinition();
        } else {
            throw new BluePrintException(String.format("failed to get model definition (%s) from repo", modelName));
        }
        return modelDefinition;
    }
}

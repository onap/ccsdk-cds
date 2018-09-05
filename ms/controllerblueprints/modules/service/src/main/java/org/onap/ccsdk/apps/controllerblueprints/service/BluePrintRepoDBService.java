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
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRepoService;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ModelTypeRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * BluePrintRepoDBService
 *
 * @author Brinda Santh
 */
@Service
@SuppressWarnings("unused")
public class BluePrintRepoDBService implements BluePrintRepoService {

    private ModelTypeRepository modelTypeRepository;
    @SuppressWarnings("unused")
    public BluePrintRepoDBService(ModelTypeRepository modelTypeRepository) {
        this.modelTypeRepository = modelTypeRepository;
    }

    @Override
    public Mono<NodeType> getNodeType(@NotNull String nodeTypeName) throws BluePrintException {
        return getModelType(nodeTypeName, NodeType.class);
    }

    @Override
    public Mono<DataType> getDataType(@NotNull String dataTypeName) throws BluePrintException {
        return getModelType(dataTypeName, DataType.class);
    }

    @Override
    public Mono<ArtifactType> getArtifactType(@NotNull String artifactTypeName) throws BluePrintException {
        return getModelType(artifactTypeName, ArtifactType.class);
    }

    @Override
    public Mono<RelationshipType> getRelationshipType(@NotNull String relationshipTypeName) throws BluePrintException {
        return getModelType(relationshipTypeName, RelationshipType.class);
    }

    @Override
    public Mono<CapabilityDefinition> getCapabilityDefinition(@NotNull String capabilityDefinitionName) throws BluePrintException {
        return getModelType(capabilityDefinitionName, CapabilityDefinition.class);
    }

    private <T> Mono<T> getModelType(String modelName, Class<T> valueClass) throws BluePrintException {
        Preconditions.checkArgument(StringUtils.isNotBlank(modelName),
                "Failed to get model from repo, model name is missing");

        return getModelDefinition(modelName).map(modelDefinition -> {
                    Preconditions.checkNotNull(modelDefinition,
                            String.format("Failed to get model content for model name (%s)", modelName));
                    return JacksonUtils.readValue(modelDefinition, valueClass);
                }
        );
    }

    private Mono<JsonNode> getModelDefinition(String modelName) throws BluePrintException {
        JsonNode modelDefinition;
        Optional<ModelType> modelTypeDb = modelTypeRepository.findByModelName(modelName);
        if (modelTypeDb.isPresent()) {
            modelDefinition = modelTypeDb.get().getDefinition();
        } else {
            throw new BluePrintException(String.format("failed to get model definition (%s) from repo", modelName));
        }
        return Mono.just(modelDefinition);
    }
}

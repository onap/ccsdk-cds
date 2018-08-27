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

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.data.*;
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRepoService;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ModelTypeRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * BluePrintRepoDBService
 *
 * @author Brinda Santh
 */
@Service
public class BluePrintRepoDBService implements BluePrintRepoService {

    private ModelTypeRepository modelTypeRepository;

    public BluePrintRepoDBService(ModelTypeRepository modelTypeRepository) {
        this.modelTypeRepository = modelTypeRepository;
    }

   
    @Override
    public NodeType getNodeType(String nodeTypeName) throws BluePrintException {
        Preconditions.checkArgument(StringUtils.isNotBlank(nodeTypeName), "NodeType name is missing");
        String content = getModelDefinitions(nodeTypeName);
        Preconditions.checkArgument(StringUtils.isNotBlank(content), "NodeType content is missing");
        return JacksonUtils.readValue(content, NodeType.class);
    }

   
    @Override
    public DataType getDataType(String dataTypeName) throws BluePrintException {
        Preconditions.checkArgument(StringUtils.isNotBlank(dataTypeName), "DataType name is missing");
        String content = getModelDefinitions(dataTypeName);
        Preconditions.checkArgument(StringUtils.isNotBlank(content), "DataType content is missing");
        return JacksonUtils.readValue(content, DataType.class);
    }

   
    @Override
    public ArtifactType getArtifactType(String artifactTypeName) throws BluePrintException {
        Preconditions.checkArgument(StringUtils.isNotBlank(artifactTypeName), "ArtifactType name is missing");
        String content = getModelDefinitions(artifactTypeName);
        Preconditions.checkArgument(StringUtils.isNotBlank(content), "ArtifactType content is missing");
        return JacksonUtils.readValue(content, ArtifactType.class);
    }

   
    @Override
    public RelationshipType getRelationshipType(String relationshipTypeName) throws BluePrintException {
        Preconditions.checkArgument(StringUtils.isNotBlank(relationshipTypeName), "RelationshipType name is missing");
        String content = getModelDefinitions(relationshipTypeName);
        Preconditions.checkArgument(StringUtils.isNotBlank(content), "RelationshipType content is missing");
        return JacksonUtils.readValue(content, RelationshipType.class);
    }

   
    @Override
    public CapabilityDefinition getCapabilityDefinition(String capabilityDefinitionName) throws BluePrintException {
        Preconditions.checkArgument(StringUtils.isNotBlank(capabilityDefinitionName), "CapabilityDefinition name is missing");
        String content = getModelDefinitions(capabilityDefinitionName);
        Preconditions.checkArgument(StringUtils.isNotBlank(content), "CapabilityDefinition content is missing");
        return JacksonUtils.readValue(content, CapabilityDefinition.class);
    }

    private String getModelDefinitions(String modelName) throws BluePrintException {
        String modelDefinition = null;
        Optional<ModelType> modelTypedb = modelTypeRepository.findByModelName(modelName);
        if (modelTypedb.isPresent()) {
            modelDefinition = modelTypedb.get().getDefinition();
        } else {
            throw new BluePrintException(String.format("failed to get model definition (%s) from repo", modelName));
        }
        return modelDefinition;
    }
}

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
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ModelTypeRepository;
import org.onap.ccsdk.apps.controllerblueprints.service.validator.ModelTypeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ModelTypeService.java Purpose: Provide ModelTypeService Service ModelTypeService
 *
 * @author Brinda Santh
 * @version 1.0
 */

@Service
@Transactional
public class ModelTypeService {

    private ModelTypeRepository modelTypeRepository;

    /**
     * This is a ModelTypeService, used to save and get the model types stored in database
     *
     * @param modelTypeRepository modelTypeRepository
     */
    public ModelTypeService(ModelTypeRepository modelTypeRepository) {
        this.modelTypeRepository = modelTypeRepository;
    }


    /**
     * This is a getModelTypeByName service
     *
     * @param modelTypeName modelTypeName
     * @return ModelType
     */
    public ModelType getModelTypeByName(String modelTypeName) {
        ModelType modelType = null;
        Preconditions.checkArgument(StringUtils.isNotBlank(modelTypeName), "Model Name Information is missing.");
        Optional<ModelType> modelTypeOption = modelTypeRepository.findByModelName(modelTypeName);
        if (modelTypeOption.isPresent()) {
            modelType = modelTypeOption.get();
        }
        return modelType;
    }


    /**
     * This is a searchModelTypes service
     *
     * @param tags tags
     * @return List<ModelType>
     */
    public List<ModelType> searchModelTypes(String tags) {
        Preconditions.checkArgument(StringUtils.isNotBlank(tags), "No Search Information provide");
        return modelTypeRepository.findByTagsContainingIgnoreCase(tags);
    }

    /**
     * This is a saveModel service
     *
     * @param modelType modelType
     * @return ModelType
     * @throws BluePrintException BluePrintException
     */
    public ModelType saveModel(ModelType modelType) throws BluePrintException {

        Preconditions.checkNotNull(modelType, "Model Type Information is missing.");

        ModelTypeValidator.validateModelType(modelType);

        Optional<ModelType> dbModelType = modelTypeRepository.findByModelName(modelType.getModelName());
        if (dbModelType.isPresent()) {
            ModelType dbModel = dbModelType.get();
            dbModel.setDescription(modelType.getDescription());
            dbModel.setDefinition(modelType.getDefinition());
            dbModel.setDefinitionType(modelType.getDefinitionType());
            dbModel.setDerivedFrom(modelType.getDerivedFrom());
            dbModel.setTags(modelType.getTags());
            dbModel.setVersion(modelType.getVersion());
            dbModel.setUpdatedBy(modelType.getUpdatedBy());
            modelType = modelTypeRepository.save(dbModel);
        } else {
            modelType = modelTypeRepository.save(modelType);
        }
        return modelType;
    }


    /**
     * This is a deleteByModelName service
     *
     * @param modelName modelName
     */
    public void deleteByModelName(String modelName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(modelName), "Model Name Information is missing.");
        modelTypeRepository.deleteByModelName(modelName);

    }

    /**
     * This is a getModelTypeByDefinitionType service
     *
     * @param definitionType definitionType
     * @return List<ModelType>
     */
    public List<ModelType> getModelTypeByDefinitionType(String definitionType) {
        Preconditions.checkArgument(StringUtils.isNotBlank(definitionType), "Model definitionType Information is missing.");
        return modelTypeRepository.findByDefinitionType(definitionType);
    }

    /**
     * This is a getModelTypeByDerivedFrom service
     *
     * @param derivedFrom derivedFrom
     * @return List<ModelType>
     */
    public List<ModelType> getModelTypeByDerivedFrom(String derivedFrom) {
        Preconditions.checkArgument(StringUtils.isNotBlank(derivedFrom), "Model derivedFrom Information is missing.");
        return modelTypeRepository.findByDerivedFrom(derivedFrom);
    }


}

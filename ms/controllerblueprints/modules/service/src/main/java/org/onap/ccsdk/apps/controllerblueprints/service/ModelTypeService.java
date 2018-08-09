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

package org.onap.ccsdk.apps.controllerblueprints.service;

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
     * @param modelTypeRepository
     */
    public ModelTypeService(ModelTypeRepository modelTypeRepository) {
        this.modelTypeRepository = modelTypeRepository;
    }


    /**
     * This is a getModelTypeByName service
     *
     * @param modelTypeName
     * @return ModelType
     * @throws BluePrintException
     */
    public ModelType getModelTypeByName(String modelTypeName) throws BluePrintException {
        ModelType modelType = null;
        if (StringUtils.isNotBlank(modelTypeName)) {
            Optional<ModelType> modelTypeOption = modelTypeRepository.findByModelName(modelTypeName);
            if (modelTypeOption.isPresent()) {
                modelType = modelTypeOption.get();
            }
        } else {
            throw new BluePrintException("Model Name Information is missing.");
        }
        return modelType;
    }


    /**
     * This is a searchModelTypes service
     *
     * @param tags
     * @return List<ModelType>
     * @throws BluePrintException
     */
    public List<ModelType> searchModelTypes(String tags) throws BluePrintException {
        if (tags != null) {
            return modelTypeRepository.findByTagsContainingIgnoreCase(tags);
        } else {
            throw new BluePrintException("No Search Information provide");
        }
    }

    /**
     * This is a saveModel service
     *
     * @param modelType
     * @return ModelType
     * @throws BluePrintException
     */
    public ModelType saveModel(ModelType modelType) throws BluePrintException {

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
     * @param modelName
     * @throws BluePrintException
     */
    public void deleteByModelName(String modelName) throws BluePrintException {
        if (modelName != null) {
            modelTypeRepository.deleteByModelName(modelName);
        } else {
            throw new BluePrintException("Model Name Information is missing.");
        }
    }

    /**
     * This is a getModelTypeByTags service
     *
     * @param tags
     * @return List<ModelType>
     * @throws BluePrintException
     */
    public List<ModelType> getModelTypeByTags(String tags) throws BluePrintException {
        if (StringUtils.isNotBlank(tags)) {
            return modelTypeRepository.findByTagsContainingIgnoreCase(tags);
        } else {
            throw new BluePrintException("Model Tag Information is missing.");
        }
    }

    /**
     * This is a getModelTypeByDefinitionType service
     *
     * @param definitionType
     * @return List<ModelType>
     * @throws BluePrintException
     */
    public List<ModelType> getModelTypeByDefinitionType(String definitionType) throws BluePrintException {
        if (StringUtils.isNotBlank(definitionType)) {
            return modelTypeRepository.findByDefinitionType(definitionType);
        } else {
            throw new BluePrintException("Model definitionType Information is missing.");
        }
    }

    /**
     * This is a getModelTypeByDerivedFrom service
     *
     * @param derivedFrom
     * @return List<ModelType>
     * @throws BluePrintException
     */
    public List<ModelType> getModelTypeByDerivedFrom(String derivedFrom) throws BluePrintException {
        if (StringUtils.isNotBlank(derivedFrom)) {
            return modelTypeRepository.findByDerivedFrom(derivedFrom);
        } else {
            throw new BluePrintException("Model derivedFrom Information is missing.");
        }
    }


}

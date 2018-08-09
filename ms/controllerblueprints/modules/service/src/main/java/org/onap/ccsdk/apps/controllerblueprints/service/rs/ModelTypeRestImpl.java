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

package org.onap.ccsdk.apps.controllerblueprints.service.rs;

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.service.ModelTypeService;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@inheritDoc}
 */
@Service
public class ModelTypeRestImpl implements ModelTypeRest {

    private ModelTypeService modelTypeService;

    /**
     * This is a ModelTypeResourceImpl, used to save and get the model types stored in database
     *
     * @param modelTypeService Model Type Service
     */
    public ModelTypeRestImpl(ModelTypeService modelTypeService) {
        this.modelTypeService = modelTypeService;
    }

    @Override
    public ModelType getModelTypeByName(String modelName) throws BluePrintException {
        try {
            return modelTypeService.getModelTypeByName(modelName);
        } catch (Exception e) {
            throw new BluePrintException(1000, e.getMessage(), e);
        }
    }

    @Override
    public List<ModelType> searchModelTypes(String tags) throws BluePrintException {
        try {
            return modelTypeService.searchModelTypes(tags);
        } catch (Exception e) {
            throw new BluePrintException(1001, e.getMessage(), e);
        }
    }

    @Override
    public List<ModelType> getModelTypeByDefinitionType(String definitionType) throws BluePrintException {
        try {
            return modelTypeService.getModelTypeByDefinitionType(definitionType);
        } catch (Exception e) {
            throw new BluePrintException(1002, e.getMessage(), e);
        }
    }

    @Override
    public ModelType saveModelType(ModelType modelType) throws BluePrintException {
        try {
            return modelTypeService.saveModel(modelType);
        } catch (Exception e) {
            throw new BluePrintException(1100, e.getMessage(), e);
        }
    }

    @Override
    public void deleteModelTypeByName(String name) throws BluePrintException {
        try {
            modelTypeService.deleteByModelName(name);
        } catch (Exception e) {
            throw new BluePrintException(1400, e.getMessage(), e);
        }
    }
}

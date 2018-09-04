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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@inheritDoc}
 */
@RestController
@RequestMapping(value = "/api/v1/model-type")
public class ModelTypeRest {

    private ModelTypeService modelTypeService;

    /**
     * This is a ModelTypeResourceImpl, used to save and get the model types stored in database
     *
     * @param modelTypeService Model Type Service
     */
    public ModelTypeRest(ModelTypeService modelTypeService) {
        this.modelTypeService = modelTypeService;
    }

    @GetMapping(path = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelType getModelTypeByName(@PathVariable(value = "name") String name) throws BluePrintException {
        try {
            return modelTypeService.getModelTypeByName(name);
        } catch (Exception e) {
            throw new BluePrintException(1000, e.getMessage(), e);
        }
    }

    @GetMapping(path = "/search/{tags}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ModelType> searchModelTypes(@PathVariable(value = "tags") String tags) throws BluePrintException {
        try {
            return modelTypeService.searchModelTypes(tags);
        } catch (Exception e) {
            throw new BluePrintException(1001, e.getMessage(), e);
        }
    }

    @GetMapping(path = "/by-definition/{definitionType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<ModelType> getModelTypeByDefinitionType(@PathVariable(value = "definitionType") String definitionType) throws BluePrintException {
        try {
            return modelTypeService.getModelTypeByDefinitionType(definitionType);
        } catch (Exception e) {
            throw new BluePrintException(1002, e.getMessage(), e);
        }
    }

    @PostMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ModelType saveModelType(@RequestBody ModelType modelType) throws BluePrintException {
        try {
            return modelTypeService.saveModel(modelType);
        } catch (Exception e) {
            throw new BluePrintException(1100, e.getMessage(), e);
        }
    }

    @DeleteMapping(path = "/{name}")
    public void deleteModelTypeByName(@PathVariable(value = "name") String name) throws BluePrintException {
        try {
            modelTypeService.deleteByModelName(name);
        } catch (Exception e) {
            throw new BluePrintException(1400, e.getMessage(), e);
        }
    }
}

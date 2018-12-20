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

package org.onap.ccsdk.apps.controllerblueprints.service.rs;

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;
import org.onap.ccsdk.apps.controllerblueprints.service.handler.ModelTypeHandler;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@inheritDoc}
 */
@Deprecated
//@RestController
//@RequestMapping(value = "/api/v1/model-type")
public class ModelTypeRest {

    private ModelTypeHandler modelTypeService;

    /**
     * This is a ModelTypeResourceImpl, used to save and get the model types stored in database
     *
     * @param modelTypeService Model Type Service
     */
    public ModelTypeRest(ModelTypeHandler modelTypeService) {
        this.modelTypeService = modelTypeService;
    }

    @GetMapping(path = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelType getModelTypeByName(@PathVariable(value = "name") String name) {
        return modelTypeService.getModelTypeByName(name);
    }

    @GetMapping(path = "/search/{tags}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ModelType> searchModelTypes(@PathVariable(value = "tags") String tags) {
        return modelTypeService.searchModelTypes(tags);
    }

    @GetMapping(path = "/by-definition/{definitionType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<ModelType> getModelTypeByDefinitionType(@PathVariable(value = "definitionType") String definitionType) {
        return modelTypeService.getModelTypeByDefinitionType(definitionType);
    }

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ModelType saveModelType(@RequestBody ModelType modelType) throws BluePrintException {
        return modelTypeService.saveModel(modelType);
    }

    @DeleteMapping(path = "/{name}")
    public void deleteModelTypeByName(@PathVariable(value = "name") String name) {
        modelTypeService.deleteByModelName(name);
    }
}

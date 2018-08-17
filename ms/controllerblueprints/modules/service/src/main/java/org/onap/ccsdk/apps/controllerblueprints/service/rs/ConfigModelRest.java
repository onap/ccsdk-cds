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
import org.onap.ccsdk.apps.controllerblueprints.service.ConfigModelService;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModel;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@inheritDoc}
 */
@RestController
@RequestMapping("/api/v1/config-model")
public class ConfigModelRest {

    private ConfigModelService configModelService;

    /**
     * This is a ConfigModelRest constructor.
     *
     * @param configModelService Config Model Service
     */
    public ConfigModelRest(ConfigModelService configModelService) {
        this.configModelService = configModelService;

    }

    @GetMapping(path = "/initial/{name}")
    public @ResponseBody
    ConfigModel getInitialConfigModel(@PathVariable(value = "name") String name) throws BluePrintException {
        try {
            return this.configModelService.getInitialConfigModel(name);
        } catch (Exception e) {
            throw new BluePrintException(2000, e.getMessage(), e);
        }
    }

    @PostMapping(path = "/")
    public @ResponseBody
    ConfigModel saveConfigModel(@RequestBody ConfigModel configModel) throws BluePrintException {
        try {
            return this.configModelService.saveConfigModel(configModel);
        } catch (Exception e) {
            throw new BluePrintException(2200, e.getMessage(), e);
        }
    }

    @DeleteMapping(path = "/{id}")
    public void deleteConfigModel(@PathVariable(value = "id") Long id) throws BluePrintException {
        try {
            this.configModelService.deleteConfigModel(id);
        } catch (Exception e) {
            throw new BluePrintException(4000, e.getMessage(), e);
        }
    }

    @GetMapping(path = "/publish/{id}")
    public @ResponseBody
    ConfigModel publishConfigModel(@PathVariable(value = "id") Long id) throws BluePrintException {
        try {
            return this.configModelService.publishConfigModel(id);
        } catch (Exception e) {
            throw new BluePrintException(2500, e.getMessage(), e);
        }
    }

    @GetMapping(path = "/{id}")
    public @ResponseBody
    ConfigModel getConfigModel(@PathVariable(value = "id") Long id) throws BluePrintException {
        try {
            return this.configModelService.getConfigModel(id);
        } catch (Exception e) {
            throw new BluePrintException(2001, e.getMessage(), e);
        }
    }

    @GetMapping(path = "/by-name/{name}/version/{version}")
    public @ResponseBody
    ConfigModel getConfigModelByNameAndVersion(@PathVariable(value = "name") String name,
                                               @PathVariable(value = "version") String version) throws BluePrintException {
        try {
            return this.configModelService.getConfigModelByNameAndVersion(name, version);
        } catch (Exception e) {
            throw new BluePrintException(2002, e.getMessage(), e);
        }
    }

    @GetMapping(path = "/search/{tags}")
    public @ResponseBody
    List<ConfigModel> searchConfigModels(@PathVariable(value = "tags") String tags) throws BluePrintException {
        try {
            return this.configModelService.searchConfigModels(tags);
        } catch (Exception e) {
            throw new BluePrintException(2003, e.getMessage(), e);
        }
    }

    @GetMapping(path = "/clone/{id}")
    public @ResponseBody
    ConfigModel getCloneConfigModel(@PathVariable(value = "id") Long id) throws BluePrintException {
        try {
            return this.configModelService.getCloneConfigModel(id);
        } catch (Exception e) {
            throw new BluePrintException(2004, e.getMessage(), e);
        }
    }

}

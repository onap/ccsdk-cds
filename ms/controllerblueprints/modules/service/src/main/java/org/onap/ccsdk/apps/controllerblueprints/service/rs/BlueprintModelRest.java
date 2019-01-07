/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
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
import org.onap.ccsdk.apps.controllerblueprints.service.BlueprintModelService;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModelSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * {@inheritDoc}
 */
@RestController
@RequestMapping(value = "/api/v1/blueprint-model")
public class BlueprintModelRest {

    @Autowired
    private BlueprintModelService blueprintModelService;

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody
    Mono<BlueprintModelSearch> saveBluePrint(@RequestPart("file") FilePart file) throws BluePrintException{
        return blueprintModelService.saveBlueprintModel(file);
    }

    @DeleteMapping(path = "/{id}")
    public void deleteBluePrint(@PathVariable(value = "id") String id) throws BluePrintException {
        this.blueprintModelService.deleteBlueprintModel(id);
    }

    @GetMapping(path = "/by-name/{name}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    BlueprintModelSearch getBluePrintByNameAndVersion(@PathVariable(value = "name") String name,
                                                          @PathVariable(value = "version") String version) throws BluePrintException {
        return this.blueprintModelService.getBlueprintModelByNameAndVersion(name, version);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    BlueprintModelSearch getCBA(@PathVariable(value = "id") String id) throws BluePrintException {
        return this.blueprintModelService.getBlueprintModelSearch(id);
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<BlueprintModelSearch> getAllCBA() {
        return this.blueprintModelService.getAllBlueprintModel();
    }

    @GetMapping(path = "/download/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<Resource> downloadBluePrint(@PathVariable(value = "id") String id) throws BluePrintException {
        return this.blueprintModelService.downloadBlueprintModelFile(id);
    }

    @GetMapping(path = "/publish/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    BlueprintModelSearch publishBlueprintModel(@PathVariable(value = "id") String id) throws BluePrintException {
        return this.blueprintModelService.publishBlueprintModel(id);
    }

    @GetMapping(path = "/search/{tags}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<BlueprintModelSearch> searchBlueprintModels(@PathVariable(value = "tags") String tags) {
        return this.blueprintModelService.searchBlueprintModels(tags);
    }
}

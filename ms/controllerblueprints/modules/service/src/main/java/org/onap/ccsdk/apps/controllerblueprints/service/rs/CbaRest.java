/*
 * Copyright © 2018 IBM Intellectual Property.
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
import org.onap.ccsdk.apps.controllerblueprints.service.CbaService;
import org.onap.ccsdk.apps.controllerblueprints.service.model.BlueprintModelResponse;
import org.onap.ccsdk.apps.controllerblueprints.service.model.ItemCbaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * CbaRest.java Purpose: Provide a REST API to upload single and multiple CBA
 *
 * @author Steve Siani
 * @version 1.0
 */
@RestController
@RequestMapping(value = "/api/v1/cba")
public class CbaRest {

    @Autowired
    private CbaService cbaService;

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public  Flux<BlueprintModelResponse> uploadCBA(@RequestBody Flux<Part> parts) {
        return parts.filter(part -> part instanceof FilePart) // only retain file parts
            .ofType(FilePart.class) // convert the flux to FilePart
            .flatMap(filePart -> cbaService.uploadCBAFile(filePart)); // save each file and flatmap it to a flux of results
    }

    @DeleteMapping(path = "/{id}")
    public void deleteCBA(@PathVariable(value = "id") Long id) throws BluePrintException {
        this.cbaService.deleteCBA(id);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ItemCbaResponse getCBA(@PathVariable(value = "id") String id) {
        return this.cbaService.findCBAByID(id);
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<ItemCbaResponse> getAllCBA() {
        return this.cbaService.findAllCBA();
    }

    @GetMapping(path = "/by-name/{name}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ItemCbaResponse getCBAByNameAndVersion(@PathVariable(value = "name") String name,
                                                          @PathVariable(value = "version") String version) throws BluePrintException {
        return this.cbaService.findCBAByNameAndVersion(name, version);
    }

    @GetMapping(path = "/download/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<Resource> downloadCBA(@PathVariable(value = "id") String id) {
        return this.cbaService.downloadCBAFile(id);
    }
}

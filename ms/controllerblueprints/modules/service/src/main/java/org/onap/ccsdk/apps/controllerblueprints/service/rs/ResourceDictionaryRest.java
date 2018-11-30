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
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceSourceMapping;
import org.onap.ccsdk.apps.controllerblueprints.service.ResourceDictionaryService;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ResourceDictionary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@inheritDoc}
 */
@RestController
@RequestMapping(value = "/api/v1/dictionary")
public class ResourceDictionaryRest {


    private ResourceDictionaryService resourceDictionaryService;

    /**
     * This is a DataDictionaryRestImpl, used to save and get the Resource Mapping stored in database
     *
     * @param dataDictionaryService Data Dictionary Service
     */
    public ResourceDictionaryRest(ResourceDictionaryService dataDictionaryService) {
        this.resourceDictionaryService = dataDictionaryService;
    }

    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResourceDictionary saveResourceDictionary(@RequestBody ResourceDictionary dataDictionary) {
        return resourceDictionaryService.saveResourceDictionary(dataDictionary);
    }

    @DeleteMapping(path = "/{name}")
    public void deleteResourceDictionaryByName(@PathVariable(value = "name") String name) {
        resourceDictionaryService.deleteResourceDictionary(name);
    }

    @GetMapping(path = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResourceDictionary getResourceDictionaryByName(@PathVariable(value = "name") String name) throws BluePrintException {
        return resourceDictionaryService.getResourceDictionaryByName(name);
    }

    @PostMapping(path = "/by-names", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<ResourceDictionary> searchResourceDictionaryByNames(@RequestBody List<String> names) {
        return resourceDictionaryService.searchResourceDictionaryByNames(names);
    }

    @GetMapping(path = "/search/{tags}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<ResourceDictionary> searchResourceDictionaryByTags(@PathVariable(value = "tags") String tags) {
        return resourceDictionaryService.searchResourceDictionaryByTags(tags);

    }

    @GetMapping(path = "/source-mapping", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResourceSourceMapping getResourceSourceMapping() {
        return resourceDictionaryService.getResourceSourceMapping();
    }

}

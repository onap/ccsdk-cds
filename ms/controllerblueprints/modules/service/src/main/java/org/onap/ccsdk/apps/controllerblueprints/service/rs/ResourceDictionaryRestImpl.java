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
import org.onap.ccsdk.apps.controllerblueprints.service.ResourceDictionaryService;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ResourceDictionary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@inheritDoc}
 */
@Service
public class ResourceDictionaryRestImpl implements ResourceDictionaryRest {


    private ResourceDictionaryService resourceDictionaryService;

    /**
     * This is a DataDictionaryRestImpl, used to save and get the Resource Mapping stored in database
     *
     * @param dataDictionaryService Data Dictionary Service
     */
    public ResourceDictionaryRestImpl(ResourceDictionaryService dataDictionaryService) {
        this.resourceDictionaryService = dataDictionaryService;
    }

    @Override
    public ResourceDictionary saveResourceDictionary(ResourceDictionary dataDictionary)
            throws BluePrintException {
        try {
            return resourceDictionaryService.saveResourceDictionary(dataDictionary);
        } catch (Exception e) {
            throw new BluePrintException(4100, e.getMessage(), e);
        }
    }

    @Override
    public void deleteResourceDictionaryByName(String name) throws BluePrintException {
        try {
            resourceDictionaryService.deleteResourceDictionary(name);
        } catch (Exception e) {
            throw new BluePrintException(4400, e.getMessage(), e);
        }
    }

    @Override
    public ResourceDictionary getResourceDictionaryByName(String resourcePath) throws BluePrintException {
        try {
            return resourceDictionaryService.getResourceDictionaryByName(resourcePath);
        } catch (Exception e) {
            throw new BluePrintException(4001, e.getMessage(), e);
        }
    }

    @Override
    public List<ResourceDictionary> searchResourceDictionaryByNames(List<String> names)
            throws BluePrintException {
        try {
            return resourceDictionaryService.searchResourceDictionaryByNames(names);
        } catch (Exception e) {
            throw new BluePrintException(4002, e.getMessage(), e);
        }
    }

    @Override
    public List<ResourceDictionary> searchResourceDictionaryByTags(String tags) throws BluePrintException {
        try {
            return resourceDictionaryService.searchResourceDictionaryByTags(tags);
        } catch (Exception e) {
            throw new BluePrintException(4003, e.getMessage(), e);
        }
    }

}

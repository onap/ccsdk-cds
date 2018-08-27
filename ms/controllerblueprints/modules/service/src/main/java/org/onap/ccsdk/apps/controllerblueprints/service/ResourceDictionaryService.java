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

import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.data.EntrySchema;
import org.onap.ccsdk.apps.controllerblueprints.core.data.PropertyDefinition;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ResourceDictionary;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ResourceDictionaryRepository;
import org.onap.ccsdk.apps.controllerblueprints.service.validator.ResourceDictionaryValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ResourceDictionaryService.java Purpose: Provide DataDictionaryService Service
 * DataDictionaryService
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Service
public class ResourceDictionaryService {

    private ResourceDictionaryRepository resourceDictionaryRepository;

    private ResourceDictionaryValidationService resourceDictionaryValidationService;

    /**
     * This is a DataDictionaryService, used to save and get the Resource Mapping stored in database
     *
     * @param dataDictionaryRepository
     * @param resourceDictionaryValidationService
     */
    public ResourceDictionaryService(ResourceDictionaryRepository dataDictionaryRepository,
                                     ResourceDictionaryValidationService resourceDictionaryValidationService) {
        this.resourceDictionaryRepository = dataDictionaryRepository;
        this.resourceDictionaryValidationService = resourceDictionaryValidationService;
    }

    /**
     * This is a getDataDictionaryByName service
     *
     * @param name
     * @return DataDictionary
     * @throws BluePrintException
     */
    public ResourceDictionary getResourceDictionaryByName(String name) throws BluePrintException {
        if (StringUtils.isNotBlank(name)) {
            return resourceDictionaryRepository.findByName(name).get();
        } else {
            throw new BluePrintException("Resource Mapping Name Information is missing.");
        }
    }

    /**
     * This is a searchResourceDictionaryByNames service
     *
     * @param names
     * @return List<ResourceDictionary>
     * @throws BluePrintException
     */
    public List<ResourceDictionary> searchResourceDictionaryByNames(List<String> names)
            throws BluePrintException {
        if (names != null && !names.isEmpty()) {
            return resourceDictionaryRepository.findByNameIn(names);
        } else {
            throw new BluePrintException("No Search Information provide");
        }
    }

    /**
     * This is a searchResourceDictionaryByTags service
     *
     * @param tags
     * @return List<ResourceDictionary>
     * @throws BluePrintException
     */
    public List<ResourceDictionary> searchResourceDictionaryByTags(String tags) throws BluePrintException {
        if (StringUtils.isNotBlank(tags)) {
            return resourceDictionaryRepository.findByTagsContainingIgnoreCase(tags);
        } else {
            throw new BluePrintException("No Search Information provide");
        }
    }

    /**
     * This is a saveDataDictionary service
     *
     * @param resourceDictionary
     * @return DataDictionary
     * @throws BluePrintException
     */
    public ResourceDictionary saveResourceDictionary(ResourceDictionary resourceDictionary)
            throws BluePrintException {
        if (resourceDictionary != null) {
            ResourceDictionaryValidator.validateResourceDictionary(resourceDictionary);

            ResourceDefinition resourceDefinition =
                    JacksonUtils.readValue(resourceDictionary.getDefinition(), ResourceDefinition.class);
            // Check the Source already Present
            resourceDictionaryValidationService.validate(resourceDefinition);

            if (resourceDefinition == null) {
                throw new BluePrintException(
                        "Resource dictionary definition is not valid content " + resourceDictionary.getDefinition());
            }

            resourceDefinition.setName(resourceDictionary.getName());
            resourceDefinition.setResourcePath(resourceDictionary.getResourcePath());
            resourceDefinition.setResourceType(resourceDictionary.getResourceType());

            PropertyDefinition propertyDefinition = new PropertyDefinition();
            propertyDefinition.setType(resourceDictionary.getDataType());
            propertyDefinition.setDescription(resourceDictionary.getDescription());
            if (StringUtils.isNotBlank(resourceDictionary.getEntrySchema())) {
                EntrySchema entrySchema = new EntrySchema();
                entrySchema.setType(resourceDictionary.getEntrySchema());
                propertyDefinition.setEntrySchema(entrySchema);
            } else {
                propertyDefinition.setEntrySchema(null);
            }
            resourceDefinition.setTags(resourceDictionary.getTags());
            resourceDefinition.setUpdatedBy(resourceDictionary.getUpdatedBy());

            String definitionContent = JacksonUtils.getJson(resourceDefinition, true);
            resourceDictionary.setDefinition(definitionContent);

            Optional<ResourceDictionary> dbResourceDictionaryData =
                    resourceDictionaryRepository.findByName(resourceDictionary.getName());
            if (dbResourceDictionaryData.isPresent()) {
                ResourceDictionary dbResourceDictionary = dbResourceDictionaryData.get();

                dbResourceDictionary.setName(resourceDictionary.getName());
                dbResourceDictionary.setDefinition(resourceDictionary.getDefinition());
                dbResourceDictionary.setDescription(resourceDictionary.getDescription());
                dbResourceDictionary.setResourceType(resourceDictionary.getResourceType());
                dbResourceDictionary.setResourcePath(resourceDictionary.getResourcePath());
                dbResourceDictionary.setDataType(resourceDictionary.getDataType());
                dbResourceDictionary.setEntrySchema(resourceDictionary.getEntrySchema());
                dbResourceDictionary.setTags(resourceDictionary.getTags());
                dbResourceDictionary.setValidValues(resourceDictionary.getValidValues());
                resourceDictionary = resourceDictionaryRepository.save(dbResourceDictionary);
            } else {
                resourceDictionary = resourceDictionaryRepository.save(resourceDictionary);
            }
        } else {
            throw new BluePrintException("Resource Dictionary information is missing");
        }
        return resourceDictionary;
    }

    /**
     * This is a deleteResourceDictionary service
     *
     * @param name
     * @throws BluePrintException
     */
    public void deleteResourceDictionary(String name) throws BluePrintException {
        if (name != null) {
            resourceDictionaryRepository.deleteByName(name);
        } else {
            throw new BluePrintException("Resource Mapping Id Information is missing.");
        }

    }
}

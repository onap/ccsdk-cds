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

import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.data.PropertyDefinition;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceSourceMapping;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.factory.ResourceSourceMappingFactory;
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

    private ResourceDefinitionValidationService resourceDictionaryValidationService;

    /**
     * This is a DataDictionaryService, used to save and get the Resource Mapping stored in database
     *
     * @param dataDictionaryRepository            dataDictionaryRepository
     * @param resourceDictionaryValidationService resourceDictionaryValidationService
     */
    public ResourceDictionaryService(ResourceDictionaryRepository dataDictionaryRepository,
                                     ResourceDefinitionValidationService resourceDictionaryValidationService) {
        this.resourceDictionaryRepository = dataDictionaryRepository;
        this.resourceDictionaryValidationService = resourceDictionaryValidationService;
    }

    /**
     * This is a getDataDictionaryByName service
     *
     * @param name name
     * @return DataDictionary
     * @throws BluePrintException BluePrintException
     */
    public ResourceDictionary getResourceDictionaryByName(String name) throws BluePrintException {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "Resource dictionary Name Information is missing.");
        Optional<ResourceDictionary> resourceDictionaryDb = resourceDictionaryRepository.findByName(name);
        if (resourceDictionaryDb.isPresent()) {
            return resourceDictionaryDb.get();
        } else {
            throw new BluePrintException(String.format("couldn't get resource dictionary for name (%s)", name));
        }
    }

    /**
     * This is a searchResourceDictionaryByNames service
     *
     * @param names names
     * @return List<ResourceDictionary>
     */
    public List<ResourceDictionary> searchResourceDictionaryByNames(List<String> names) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(names), "No Search Information provide");
        return resourceDictionaryRepository.findByNameIn(names);
    }

    /**
     * This is a searchResourceDictionaryByTags service
     *
     * @param tags tags
     * @return List<ResourceDictionary>
     */
    public List<ResourceDictionary> searchResourceDictionaryByTags(String tags) {
        Preconditions.checkArgument(StringUtils.isNotBlank(tags), "No search tag information provide");
        return resourceDictionaryRepository.findByTagsContainingIgnoreCase(tags);
    }

    /**
     * This is a saveDataDictionary service
     *
     * @param resourceDictionary resourceDictionary
     * @return DataDictionary
     */
    public ResourceDictionary saveResourceDictionary(ResourceDictionary resourceDictionary) {
        Preconditions.checkNotNull(resourceDictionary, "Resource Dictionary information is missing");
        Preconditions.checkNotNull(resourceDictionary.getDefinition(), "Resource Dictionary definition information is missing");

        ResourceDefinition resourceDefinition = resourceDictionary.getDefinition();
        Preconditions.checkNotNull(resourceDefinition, "failed to get resource definition from content");
        // Validate the Resource Definitions
        resourceDictionaryValidationService.validate(resourceDefinition);

        resourceDictionary.setTags(resourceDefinition.getTags());
        resourceDefinition.setUpdatedBy(resourceDictionary.getUpdatedBy());
        // Set the Property Definitions
        PropertyDefinition propertyDefinition = resourceDefinition.getProperty();
        resourceDictionary.setDescription(propertyDefinition.getDescription());
        resourceDictionary.setDataType(propertyDefinition.getType());
        if (propertyDefinition.getEntrySchema() != null) {
            resourceDictionary.setEntrySchema(propertyDefinition.getEntrySchema().getType());
        }

        ResourceDictionaryValidator.validateResourceDictionary(resourceDictionary);

        Optional<ResourceDictionary> dbResourceDictionaryData =
                resourceDictionaryRepository.findByName(resourceDictionary.getName());
        if (dbResourceDictionaryData.isPresent()) {
            ResourceDictionary dbResourceDictionary = dbResourceDictionaryData.get();

            dbResourceDictionary.setName(resourceDictionary.getName());
            dbResourceDictionary.setDefinition(resourceDictionary.getDefinition());
            dbResourceDictionary.setDescription(resourceDictionary.getDescription());
            dbResourceDictionary.setTags(resourceDictionary.getTags());
            dbResourceDictionary.setUpdatedBy(resourceDictionary.getUpdatedBy());
            dbResourceDictionary.setDataType(resourceDictionary.getDataType());
            dbResourceDictionary.setEntrySchema(resourceDictionary.getEntrySchema());
            resourceDictionary = resourceDictionaryRepository.save(dbResourceDictionary);
        } else {
            resourceDictionary = resourceDictionaryRepository.save(resourceDictionary);
        }

        return resourceDictionary;
    }

    /**
     * This is a deleteResourceDictionary service
     *
     * @param name name
     */
    public void deleteResourceDictionary(String name) {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "Resource dictionary Name Information is missing.");
        resourceDictionaryRepository.deleteByName(name);
    }

    /**
     * This is a getResourceSourceMapping service
     *
     */
    public ResourceSourceMapping getResourceSourceMapping() {
        return ResourceSourceMappingFactory.INSTANCE.getRegisterSourceMapping();
    }
}

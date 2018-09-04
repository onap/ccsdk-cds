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
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.utils.ResourceDictionaryUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ResourceDictionary;
import org.onap.ccsdk.apps.controllerblueprints.service.model.AutoMapResponse;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ResourceDictionaryRepository;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AutoResourceMappingService.java Purpose: Provide Automapping of Resource Assignments AutoResourceMappingService
 *
 * @author Brinda Santh
 * @version 1.0
 */

@Service
@SuppressWarnings("unused")
public class AutoResourceMappingService {

    private static EELFLogger log = EELFManager.getInstance().getLogger(AutoResourceMappingService.class);

    private ResourceDictionaryRepository dataDictionaryRepository;

    /**
     * This is a AutoResourceMappingService constructor
     *
     * @param dataDictionaryRepository dataDictionaryRepository
     */
    public AutoResourceMappingService(ResourceDictionaryRepository dataDictionaryRepository) {
        this.dataDictionaryRepository = dataDictionaryRepository;
    }

    /**
     * This is a autoMap service to map the template keys automatically to Dictionary fields.
     *
     * @param resourceAssignments resourceAssignments
     * @return AutoMapResponse
     */
    public AutoMapResponse autoMap(List<ResourceAssignment> resourceAssignments) throws BluePrintException {
        AutoMapResponse autoMapResponse = new AutoMapResponse();
        try {
            if (CollectionUtils.isNotEmpty(resourceAssignments)) {

                // Create the Dictionary definitions for the ResourceAssignment Names
                Map<String, ResourceDictionary> dictionaryMap = getDictionaryDefinitions(resourceAssignments);

                for (ResourceAssignment resourceAssignment : resourceAssignments) {
                    if (resourceAssignment != null && StringUtils.isNotBlank(resourceAssignment.getName())
                            && StringUtils.isBlank(resourceAssignment.getDictionaryName())) {

                        populateDictionaryMapping(dictionaryMap, resourceAssignment);

                        log.info("Mapped Resource : {}", resourceAssignment);

                    }
                }
            }
            List<ResourceDictionary> dictionaries = getDictionaryDefinitionsList(resourceAssignments);
            List<ResourceAssignment> resourceAssignmentsFinal = getAllAutomapResourceAssignments(resourceAssignments);
            autoMapResponse.setDataDictionaries(dictionaries);
            autoMapResponse.setResourceAssignments(resourceAssignmentsFinal);
        } catch (Exception e) {
            log.error(String.format("Failed in auto process %s", e.getMessage()));
            throw new BluePrintException(e.getMessage(), e);
        }
        return autoMapResponse;
    }

    private void populateDictionaryMapping(Map<String, ResourceDictionary> dictionaryMap, ResourceAssignment resourceAssignment) {
        ResourceDictionary dbDataDictionary = dictionaryMap.get(resourceAssignment.getName());
        if (dbDataDictionary != null && StringUtils.isNotBlank(dbDataDictionary.getDefinition())) {

            ResourceDefinition dictionaryDefinition = JacksonUtils.readValue(dbDataDictionary.getDefinition(), ResourceDefinition.class);

            if (dictionaryDefinition != null && StringUtils.isNotBlank(dictionaryDefinition.getName())
                    && StringUtils.isBlank(resourceAssignment.getDictionaryName())) {

                resourceAssignment.setDictionaryName(dbDataDictionary.getName());
                ResourceDictionaryUtils.populateSourceMapping(resourceAssignment, dictionaryDefinition);
            }
        }
    }

    private Map<String, ResourceDictionary> getDictionaryDefinitions(List<ResourceAssignment> resourceAssignments) {
        Map<String, ResourceDictionary> dictionaryMap = new HashMap<>();
        List<String> names = new ArrayList<>();
        for (ResourceAssignment resourceAssignment : resourceAssignments) {
            if (resourceAssignment != null && StringUtils.isNotBlank(resourceAssignment.getName())) {
                names.add(resourceAssignment.getName());
            }
        }
        if (CollectionUtils.isNotEmpty(names)) {

            List<ResourceDictionary> dictionaries = dataDictionaryRepository.findByNameIn(names);
            if (CollectionUtils.isNotEmpty(dictionaries)) {
                for (ResourceDictionary dataDictionary : dictionaries) {
                    if (dataDictionary != null && StringUtils.isNotBlank(dataDictionary.getName())) {
                        dictionaryMap.put(dataDictionary.getName(), dataDictionary);
                    }
                }
            }
        }
        return dictionaryMap;

    }

    private List<ResourceDictionary> getDictionaryDefinitionsList(List<ResourceAssignment> resourceAssignments) {
        List<ResourceDictionary> dictionaries = null;
        List<String> names = new ArrayList<>();
        for (ResourceAssignment resourceAssignment : resourceAssignments) {
            if (resourceAssignment != null && StringUtils.isNotBlank(resourceAssignment.getDictionaryName())) {

                if (!names.contains(resourceAssignment.getDictionaryName())) {
                    names.add(resourceAssignment.getDictionaryName());
                }

                if (resourceAssignment.getDependencies() != null && !resourceAssignment.getDependencies().isEmpty()) {
                    List<String> dependencyNames = resourceAssignment.getDependencies();
                    for (String dependencyName : dependencyNames) {
                        if (StringUtils.isNotBlank(dependencyName) && !names.contains(dependencyName)) {
                            names.add(dependencyName);
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(names)) {
            dictionaries = dataDictionaryRepository.findByNameIn(names);
        }
        return dictionaries;

    }

    private List<ResourceAssignment> getAllAutomapResourceAssignments(List<ResourceAssignment> resourceAssignments) {
        List<ResourceDictionary> dictionaries = null;
        List<String> names = new ArrayList<>();
        for (ResourceAssignment resourceAssignment : resourceAssignments) {
            if (resourceAssignment != null && StringUtils.isNotBlank(resourceAssignment.getDictionaryName())) {
                if (resourceAssignment.getDependencies() != null && !resourceAssignment.getDependencies().isEmpty()) {
                    List<String> dependencieNames = resourceAssignment.getDependencies();
                    for (String dependencieName : dependencieNames) {
                        if (StringUtils.isNotBlank(dependencieName) && !names.contains(dependencieName)
                                && !checkAssignmentsExists(resourceAssignments, dependencieName)) {
                            names.add(dependencieName);
                        }
                    }
                }
            }
        }

        if (!names.isEmpty()) {
            dictionaries = dataDictionaryRepository.findByNameIn(names);
        }
        if (dictionaries != null) {
            for (ResourceDictionary resourcedictionary : dictionaries) {
                ResourceDefinition dictionaryDefinition = JacksonUtils.readValue(resourcedictionary.getDefinition(), ResourceDefinition.class);
                Preconditions.checkNotNull(dictionaryDefinition, "failed to get Resource Definition from dictionary definition");
                PropertyDefinition property = new PropertyDefinition();
                property.setRequired(true);
                ResourceAssignment resourceAssignment = new ResourceAssignment();
                resourceAssignment.setName(resourcedictionary.getName());
                resourceAssignment.setDictionaryName(resourcedictionary
                        .getName());
                resourceAssignment.setVersion(0);
                resourceAssignment.setProperty(property);
                ResourceDictionaryUtils.populateSourceMapping(resourceAssignment, dictionaryDefinition);
                resourceAssignments.add(resourceAssignment);
            }
        }
        return resourceAssignments;

    }


    private boolean checkAssignmentsExists(List<ResourceAssignment> resourceAssignmentsWithDepencies, String resourceName) {
        return resourceAssignmentsWithDepencies.stream().anyMatch(names -> names.getName().equalsIgnoreCase(resourceName));
    }

}

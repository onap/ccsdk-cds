/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.onap.ccsdk.config.assignment.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.data.dict.ResourceDefinition;
import org.onap.ccsdk.config.model.domain.ResourceDictionary;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorConstants;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorException;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ResourceDictionaryService {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ResourceDictionaryService.class);
    private ConfigRestAdaptorService configRestAdaptorService;
    
    public ResourceDictionaryService(ConfigRestAdaptorService configRestAdaptorService) {
        this.configRestAdaptorService = configRestAdaptorService;
    }
    
    @SuppressWarnings("squid:S3776")
    public Map<String, ResourceDefinition> getDataDictionaryDefinitions(List<ResourceAssignment> resourceAssignments)
            throws SvcLogicException {
        try {
            Map<String, ResourceDefinition> dictionaries = new HashMap<>();
            if (resourceAssignments != null) {
                List<String> names = new ArrayList<>();
                for (ResourceAssignment resourceAssignment : resourceAssignments) {
                    if (resourceAssignment != null && StringUtils.isNotBlank(resourceAssignment.getDictionaryName())) {
                        
                        if (!names.contains(resourceAssignment.getDictionaryName())) {
                            names.add(resourceAssignment.getDictionaryName());
                        }
                        
                        if (resourceAssignment.getDependencies() != null
                                && !resourceAssignment.getDependencies().isEmpty()) {
                            List<String> dependencieNames = resourceAssignment.getDependencies();
                            for (String dependencieName : dependencieNames) {
                                if (StringUtils.isNotBlank(dependencieName) && !names.contains(dependencieName)) {
                                    names.add(dependencieName);
                                }
                            }
                        }
                    }
                }
                queryResourceDictionaryDefinitions(dictionaries, names);
            }
            return dictionaries;
        } catch (Exception e) {
            throw new SvcLogicException("Failed in getting resource data dictionary : " + e.getMessage());
        }
        
    }
    
    @SuppressWarnings("squid:S3776")
    private void queryResourceDictionaryDefinitions(Map<String, ResourceDefinition> dictionaries, List<String> names)
            throws SvcLogicException, ConfigRestAdaptorException {
        logger.info("Getting resource dictionary definition for the names ({})", names);
        if (!names.isEmpty()) {
            
            String dictionaryContents = configRestAdaptorService.postResource(
                    ConfigRestAdaptorConstants.SELECTOR_MODEL_SERVICE, "dictionarybynames", names, String.class);
            
            if (StringUtils.isNotBlank(dictionaryContents)) {
                List<ResourceDictionary> dataDictionaries =
                        TransformationUtils.getListfromJson(dictionaryContents, ResourceDictionary.class);
                if (dataDictionaries != null) {
                    for (ResourceDictionary dataDictionary : dataDictionaries) {
                        if (dataDictionary != null && StringUtils.isNotBlank(dataDictionary.getName())
                                && StringUtils.isNotBlank(dataDictionary.getDefinition())) {
                            ResourceDefinition resourceDefinition = TransformationUtils
                                    .readValue(dataDictionary.getDefinition(), ResourceDefinition.class);
                            if (resourceDefinition != null && StringUtils.isNotBlank(resourceDefinition.getName())) {
                                dictionaries.put(resourceDefinition.getName(), resourceDefinition);
                            } else {
                                throw new SvcLogicException(
                                        "Failed in getting resource data dictionary definition for : "
                                                + dataDictionary.getName());
                            }
                        }
                    }
                }
            } else {
                logger.warn("No resource dictionary definition found for the names ({})", names);
            }
        }
    }
}

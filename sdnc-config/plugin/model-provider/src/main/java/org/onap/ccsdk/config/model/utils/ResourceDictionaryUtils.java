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

package org.onap.ccsdk.config.model.utils;

import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.data.PropertyDefinition;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.data.dict.ResourceDefinition;
import org.onap.ccsdk.config.model.data.dict.ResourceSources;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * ResourceDictionaryUtils.java Purpose to provide ResourceDictionaryUtils
 *
 * @version 1.0
 */
public class ResourceDictionaryUtils {
    
    private ResourceDictionaryUtils() {
        // Do nothing
    }
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ResourceDictionaryUtils.class);
    
    /**
     * This Method is to assign the source name to the Dictionary Definition Check to see if the source
     * definition is not present then assign, if more than one source then assign only one source.
     *
     * @param resourceAssignment
     * @param resourceDefinition
     */
    @SuppressWarnings("squid:S3776")
    public static void populateSourceMapping(ResourceAssignment resourceAssignment,
            ResourceDefinition resourceDefinition) {
        
        if (resourceAssignment != null && resourceDefinition != null
                && StringUtils.isBlank(resourceAssignment.getDictionarySource())) {
            
            setProperty(resourceAssignment, resourceDefinition);
            ResourceSources resourceSources = resourceDefinition.getSources();
            
            if (resourceSources != null) {
                ObjectNode dictionarySourceNode =
                        (ObjectNode) TransformationUtils.getJsonNodeForObject(resourceSources);
                if (dictionarySourceNode != null && dictionarySourceNode.size() == 1) {
                    
                    if (resourceSources.getInput() != null) {
                        resourceAssignment.setDictionarySource(ConfigModelConstant.SOURCE_INPUT);
                    } else if (resourceSources.getDefaultSystem() != null) {
                        resourceAssignment.setDictionarySource(ConfigModelConstant.SOURCE_DEFAULT);
                    } else if (resourceSources.getDb() != null) {
                        resourceAssignment.setDictionarySource(ConfigModelConstant.SOURCE_DB);
                        if (resolve(() -> resourceSources.getDb().getProperties().getDependencies()).isPresent()
                                && CollectionUtils
                                        .isNotEmpty(resourceSources.getDb().getProperties().getDependencies())) {
                            resourceAssignment
                                    .setDependencies(resourceSources.getDb().getProperties().getDependencies());
                        }
                    } else if (resourceSources.getMdsal() != null) {
                        resourceAssignment.setDictionarySource(ConfigModelConstant.SOURCE_MDSAL);
                        if (resolve(() -> resourceSources.getMdsal().getProperties().getDependencies()).isPresent()
                                && CollectionUtils
                                        .isNotEmpty(resourceSources.getMdsal().getProperties().getDependencies())) {
                            resourceAssignment
                                    .setDependencies(resourceSources.getMdsal().getProperties().getDependencies());
                        }
                    }
                } else {
                    // Do nothing
                }
                logger.info("automapped resourceAssignment : {}", resourceAssignment);
            }
        }
    }
    
    public static <T> Optional<T> resolve(Supplier<T> resolver) {
        try {
            T result = resolver.get();
            return Optional.ofNullable(result);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Overriding ResourceAssignment Properties with properties defined in Dictionary
     */
    private static void setProperty(ResourceAssignment resourceAssignment, ResourceDefinition resourceDefinition) {
        if (StringUtils.isNotBlank(resourceDefinition.getProperty().getType())) {
            PropertyDefinition property = resourceAssignment.getProperty();
            if (property == null) {
                property = new PropertyDefinition();
            }
            if (resourceDefinition.getProperty() != null) {
                property.setType(resourceDefinition.getProperty().getType());
                if (resourceDefinition.getProperty().getEntrySchema() != null) {
                    property.setEntrySchema(resourceDefinition.getProperty().getEntrySchema());
                }
                resourceAssignment.setProperty(property);
            }
        }
    }
    
}

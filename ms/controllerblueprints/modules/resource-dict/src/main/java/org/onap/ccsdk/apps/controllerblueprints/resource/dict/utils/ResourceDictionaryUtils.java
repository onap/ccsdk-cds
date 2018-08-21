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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.utils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.data.EntrySchema;
import org.onap.ccsdk.apps.controllerblueprints.core.data.PropertyDefinition;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDictionaryConstants;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.data.DictionaryDefinition;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.data.DictionaryDependency;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.data.ResourceSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * ResourceDictionaryUtils.java Purpose to provide ResourceDictionaryUtils
 *
 * @author Brinda Santh
 * @version 1.0
 */
public class ResourceDictionaryUtils {

    private ResourceDictionaryUtils() {
        // Do nothing
    }

    private static final Logger log = LoggerFactory.getLogger(ResourceDictionaryUtils.class);

    /**
     * This Method is to assign the source name to the Dictionary Definition Check to see if the source
     * definition is not present then assign, if more than one source then assign only one first source.
     *
     * @param resourceAssignment
     * @param dictionaryDefinition
     */
    @SuppressWarnings("squid:S3776")
    public static void populateSourceMapping(ResourceAssignment resourceAssignment,
                                             DictionaryDefinition dictionaryDefinition) {

        if (resourceAssignment != null && dictionaryDefinition != null
                && StringUtils.isBlank(resourceAssignment.getDictionarySource())) {

            // Overwrite the Property Definitions from Dictionary
            setProperty(resourceAssignment, dictionaryDefinition);

            Map<String, ResourceSource> dictionarySource = dictionaryDefinition.getSource();
            Map<String, DictionaryDependency> dictionaryDependencyMap = dictionaryDefinition.getDependency();

            if (MapUtils.isNotEmpty(dictionarySource)) {
                String source = findFirstSource(dictionarySource);

                // Populate and Assign First Source
                if (StringUtils.isNotBlank(source)) {
                    // Set Dictionary Source
                    resourceAssignment.setDictionarySource(source);

                    if (MapUtils.isNotEmpty(dictionaryDependencyMap)) {
                        // Set Dependencies
                        DictionaryDependency dictionaryDependency = dictionaryDependencyMap.get(source);
                        if (dictionaryDependency != null) {
                            resourceAssignment.setDependencies(dictionaryDependency.getNames());
                        }
                    }
                } else {
                    resourceAssignment.setDictionarySource(ResourceDictionaryConstants.SOURCE_INPUT);
                }
                log.info("auto map resourceAssignment : {}", resourceAssignment);
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

    private static String findFirstSource(Map<String, ResourceSource> dictionarySource) {
        String source = null;
        if (MapUtils.isNotEmpty(dictionarySource)) {
            source = dictionarySource.keySet().stream().findFirst().get();
        }
        return source;
    }

    /**
     * Overriding ResourceAssignment Properties with properties defined in Dictionary
     */
    private static void setProperty(ResourceAssignment resourceAssignment, DictionaryDefinition dictionaryDefinition) {
        if (StringUtils.isNotBlank(dictionaryDefinition.getDataType())) {
            PropertyDefinition property = resourceAssignment.getProperty();
            if (property == null) {
                property = new PropertyDefinition();
            }
            property.setDefaultValue(dictionaryDefinition.getDefaultValue());
            property.setType(dictionaryDefinition.getDataType());
            if (StringUtils.isNotBlank(dictionaryDefinition.getEntrySchema())) {
                EntrySchema entrySchema = new EntrySchema();
                entrySchema.setType(dictionaryDefinition.getEntrySchema());
                property.setEntrySchema(entrySchema);
            }
            resourceAssignment.setProperty(property);
        }
    }

}

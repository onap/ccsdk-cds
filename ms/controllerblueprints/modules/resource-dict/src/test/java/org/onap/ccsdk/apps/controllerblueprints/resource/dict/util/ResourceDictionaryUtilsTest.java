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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.util;


import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants;
import org.onap.ccsdk.apps.controllerblueprints.core.ConfigModelConstant;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.data.*;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.utils.ResourceDictionaryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ResourceDictionaryUtilsTest {
    private static final Logger log = LoggerFactory.getLogger(ResourceDictionaryUtilsTest.class);

    @Test
    public void validateSingleInputSource() {
        try {
            log.info(" **************** Validating validateSingleSource *****************");
            ResourceAssignment resourceAssignment = new ResourceAssignment();
            resourceAssignment.setName("test-input-key");
            DictionaryDefinition dictionaryDefinition = new DictionaryDefinition();
            dictionaryDefinition.setDataType(BluePrintConstants.DATA_TYPE_STRING);

            Map<String, JsonNode> source = new HashMap<>();
            SourceInput sourceInput = new SourceInput();
            source.put(ConfigModelConstant.SOURCE_INPUT, JacksonUtils.jsonNodeFromObject(sourceInput));
            dictionaryDefinition.setSource(source);

            ResourceDictionaryUtils.populateSourceMapping(resourceAssignment, dictionaryDefinition);
            Assert.assertNotNull("Resource assignment input source is missing ",
                    resourceAssignment.getDictionarySource());
            Assert.assertNotNull("Resource assignment input source property is missing ",
                    resourceAssignment.getProperty());
            Assert.assertNotNull("Resource assignment input source property type is missing ",
                    resourceAssignment.getProperty().getType());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void validateSingleDbSource() {
        try {
            log.info(" **************** Validating validateSingleSource *****************");
            ResourceAssignment resourceAssignment = new ResourceAssignment();
            resourceAssignment.setName("test-db-key");
            DictionaryDefinition dictionaryDefinition = new DictionaryDefinition();
            dictionaryDefinition.setDataType(BluePrintConstants.DATA_TYPE_STRING);

            Map<String, JsonNode> source = new HashMap<>();
            SourceDb sourceDb = new SourceDb();
            source.put(ConfigModelConstant.SOURCE_DB, JacksonUtils.jsonNodeFromObject(sourceDb));
            dictionaryDefinition.setSource(source);

            Map<String, DictionaryDependency> dependency = new HashMap<>();
            DictionaryDependency dependencyDb = new DictionaryDependency();
            dependencyDb.setNames(Arrays.asList("vnf-id", "vnf-name"));
            dependency.put(ConfigModelConstant.SOURCE_DB, dependencyDb);
            dictionaryDefinition.setDependency(dependency);

            DecryptionRule decryptionRule = new DecryptionRule();
            decryptionRule.setDecryptType("sample Type");
            decryptionRule.setPath("$.");
            decryptionRule.setRule("Sample Rule");
            decryptionRule.setSources(Arrays.asList("vnf-id"));
            dictionaryDefinition.setDecryptionRules(Arrays.asList(decryptionRule));

            ResourceDictionaryUtils.populateSourceMapping(resourceAssignment, dictionaryDefinition);
            Assert.assertNotNull("Resource assignment db source source is missing ",
                    resourceAssignment.getDictionarySource());
            Assert.assertNotNull("Resource assignment db source source property is missing ",
                    resourceAssignment.getProperty());
            Assert.assertNotNull("Resource assignment db source source property type is missing ",
                    resourceAssignment.getProperty().getType());

            Assert.assertNotNull("Resource assignment db dependecy is missing ", resourceAssignment.getDependencies());
            Assert.assertEquals("Resource assignment db dependecy count mismatch ", 2,
                    resourceAssignment.getDependencies().size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSourceDefault() {
        ResourceAssignment resourceAssignment = new ResourceAssignment();
        resourceAssignment.setName("test-input-key");

        DictionaryDefinition dictionaryDefinition = new DictionaryDefinition();
        dictionaryDefinition.setDataType(BluePrintConstants.DATA_TYPE_STRING);

        Map<String, JsonNode> source = new HashMap<>();
        SourceDefault sourceDefault = new SourceDefault();
        source.put(ConfigModelConstant.SOURCE_DEFAULT, JacksonUtils.jsonNodeFromObject(sourceDefault));
        dictionaryDefinition.setSource(source);

        Map<String, DictionaryDependency> dependency = new HashMap<>();
        DictionaryDependency dependencyDefault = new DictionaryDependency();
        dependencyDefault.setNames(Arrays.asList(new String[]{"vnf-id", "vnf-name"}));
        dependency.put(ConfigModelConstant.SOURCE_DEFAULT, dependencyDefault);
        dictionaryDefinition.setDependency(dependency);

        ResourceDictionaryUtils.populateSourceMapping(resourceAssignment, dictionaryDefinition);

        Assert.assertNotNull("Resource assignment default source is missing ",
                resourceAssignment.getDictionarySource());
        Assert.assertNotNull("Resource assignment default source property is missing ",
                resourceAssignment.getProperty());
        Assert.assertNotNull("Resource assignment default source property type is missing ",
                resourceAssignment.getProperty().getType());
    }

    @Test
    public void testSourceMdsal() {
        ResourceAssignment resourceAssignment = new ResourceAssignment();
        resourceAssignment.setName("test-input-key");
        DictionaryDefinition dictionaryDefinition = new DictionaryDefinition();
        dictionaryDefinition.setDataType(BluePrintConstants.DATA_TYPE_STRING);

        Map<String, JsonNode> source = new HashMap<>();
        SourceMdsal sourceMdsal = new SourceMdsal();
        source.put(ConfigModelConstant.SOURCE_MDSAL, JacksonUtils.jsonNodeFromObject(sourceMdsal));
        dictionaryDefinition.setSource(source);

        Map<String, DictionaryDependency> dependency = new HashMap<>();
        DictionaryDependency dependencyMdsal = new DictionaryDependency();
        dependencyMdsal.setNames(Arrays.asList(new String[]{"vnf-id", "vnf-name"}));
        dependency.put(ConfigModelConstant.SOURCE_MDSAL, dependencyMdsal);
        dictionaryDefinition.setDependency(dependency);

        ResourceDictionaryUtils.populateSourceMapping(resourceAssignment, dictionaryDefinition);

        Assert.assertNotNull("Resource assignment mdsal source is missing ", resourceAssignment.getDictionarySource());
        Assert.assertNotNull("Resource assignment mdsal source property is missing ", resourceAssignment.getProperty());
        Assert.assertNotNull("Resource assignment mdsal source property type is missing ",
                resourceAssignment.getProperty().getType());
    }

}

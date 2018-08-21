/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDictionaryConstants;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictionaryDefinitionTest {
    private Logger log = LoggerFactory.getLogger(DictionaryDefinitionTest.class);
    String basePath = "load/resource_dictionary";

    @Before
    public void setup(){
        SourceDeserializer.registerSource(ResourceDictionaryConstants.SOURCE_DB, SourceDb.class);
        SourceDeserializer.registerSource(ResourceDictionaryConstants.SOURCE_INPUT, SourceInput.class);
        SourceDeserializer.registerSource(ResourceDictionaryConstants.SOURCE_MDSAL, SourceMdsal.class);
        SourceDeserializer.registerSource(ResourceDictionaryConstants.SOURCE_DEFAULT,SourceDefault.class);
    }

    @Test
    public void testDictionaryDefinitionInputSource(){

        String fileName = basePath + "/input-source.json";
        DictionaryDefinition dictionaryDefinition = JacksonUtils.readValueFromFile(fileName, DictionaryDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for input type", dictionaryDefinition);
    }

    @Test
    public void testDictionaryDefinitionDefaultSource(){

        String fileName = basePath + "/default-source.json";
        DictionaryDefinition dictionaryDefinition = JacksonUtils.readValueFromFile(fileName, DictionaryDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for default type", dictionaryDefinition);
    }

    @Test
    public void testDictionaryDefinitionDBSource(){

        String fileName = basePath + "/db-source.json";
        DictionaryDefinition dictionaryDefinition = JacksonUtils.readValueFromFile(fileName, DictionaryDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for db type", dictionaryDefinition);
    }

    @Test
    public void testDictionaryDefinitionMDSALSource(){
        String fileName = basePath + "/mdsal-source.json";
        DictionaryDefinition dictionaryDefinition = JacksonUtils.readValueFromFile(fileName, DictionaryDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for mdsal type", dictionaryDefinition);
    }
}

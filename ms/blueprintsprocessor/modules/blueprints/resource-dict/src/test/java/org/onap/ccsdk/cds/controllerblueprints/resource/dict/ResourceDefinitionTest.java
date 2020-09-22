/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.resource.dict;

import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceDefinitionTest {
    private Logger log = LoggerFactory.getLogger(ResourceDefinitionTest.class);
    private String basePath = "./../../../../../components/model-catalog/resource-dictionary/starter-dictionary";

    @Test
    public void testDictionaryDefinitionInputSource() {

        String fileName = basePath + "/input-source.json";
        ResourceDefinition resourceDefinition =
                JacksonUtils.Companion.readValueFromFile(fileName, ResourceDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for input type", resourceDefinition);
    }

    @Test
    public void testDictionaryDefinitionDefaultSource() {

        String fileName = basePath + "/default-source.json";
        ResourceDefinition resourceDefinition =
                JacksonUtils.Companion.readValueFromFile(fileName, ResourceDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for default type", resourceDefinition);
    }

    @Test
    public void testDictionaryDefinitionDBSource() {

        String fileName = basePath + "/db-source.json";
        ResourceDefinition resourceDefinition =
                JacksonUtils.Companion.readValueFromFile(fileName, ResourceDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for processor-db type", resourceDefinition);
    }

    @Test
    public void testDictionaryDefinitionMDSALSource() {
        String fileName = basePath + "/mdsal-source.json";
        ResourceDefinition resourceDefinition =
                JacksonUtils.Companion.readValueFromFile(fileName, ResourceDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for mdsal type", resourceDefinition);
    }

}

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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict;

import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ResourceDefinitionTest {
    private EELFLogger log = EELFManager.getInstance().getLogger(ResourceDefinitionTest.class);
    private String basePath = "load/resource_dictionary";

    @Test
    public void testDictionaryDefinitionInputSource(){

        String fileName = basePath + "/input-source.json";
        ResourceDefinition resourceDefinition = JacksonUtils.Companion.readValueFromFile(fileName, ResourceDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for input type", resourceDefinition);
    }

    @Test
    public void testDictionaryDefinitionDefaultSource(){

        String fileName = basePath + "/default-source.json";
        ResourceDefinition resourceDefinition = JacksonUtils.Companion.readValueFromFile(fileName, ResourceDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for default type", resourceDefinition);
    }

    @Test
    public void testDictionaryDefinitionDBSource(){

        String fileName = basePath + "/db-source.json";
        ResourceDefinition resourceDefinition = JacksonUtils.Companion.readValueFromFile(fileName, ResourceDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for db type", resourceDefinition);
    }

    @Test
    public void testDictionaryDefinitionMDSALSource(){
        String fileName = basePath + "/mdsal-source.json";
        ResourceDefinition resourceDefinition = JacksonUtils.Companion.readValueFromFile(fileName, ResourceDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for mdsal type", resourceDefinition);
    }
}

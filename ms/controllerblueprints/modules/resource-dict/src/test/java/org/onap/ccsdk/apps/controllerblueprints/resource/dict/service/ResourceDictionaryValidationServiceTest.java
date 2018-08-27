/*
 *  Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.service;

import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRepoFileService;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition;

public class ResourceDictionaryValidationServiceTest {
    private String basePath = "load/model_type";
    String dictionaryPath = "load/resource_dictionary";
    BluePrintRepoFileService bluePrintRepoFileService = new BluePrintRepoFileService(basePath);

    @Test
    public void testValidateSource() throws Exception {

        String inputFileName = dictionaryPath + "/db-source.json";
        testValidate(inputFileName);

        String dbFileName = dictionaryPath + "/db-source.json";
        testValidate(dbFileName);

        String defaultFileName = dictionaryPath + "/default-source.json";
        testValidate(defaultFileName);

        String restFileName = dictionaryPath + "/mdsal-source.json";
        testValidate(restFileName);
    }

    private void testValidate(String fileName) throws Exception {

        ResourceDefinition resourceDefinition = JacksonUtils.readValueFromFile(fileName, ResourceDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for  type", resourceDefinition);

        ResourceDictionaryValidationService resourceDictionaryValidationService =
                new ResourceDictionaryDefaultValidationService(bluePrintRepoFileService);
        resourceDictionaryValidationService.validate(resourceDefinition);
        Assert.assertNotNull(String.format("Failed to populate dictionaryDefinition for : %s", fileName), resourceDefinition);
    }
}

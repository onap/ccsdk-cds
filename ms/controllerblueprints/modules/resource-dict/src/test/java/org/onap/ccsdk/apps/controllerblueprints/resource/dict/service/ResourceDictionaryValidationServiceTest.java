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

    @Test
    public void testValidate() throws Exception {
        BluePrintRepoFileService bluePrintRepoFileService = new BluePrintRepoFileService(basePath);

        String fileName = dictionaryPath + "/db-source.json";
        ResourceDefinition resourceDefinition = JacksonUtils.readValueFromFile(fileName, ResourceDefinition.class);
        Assert.assertNotNull("Failed to populate dictionaryDefinition for db type", resourceDefinition);

        ResourceDictionaryValidationService resourceDictionaryValidationService =
                new ResourceDictionaryDefaultValidationService(bluePrintRepoFileService);
        resourceDictionaryValidationService.validate(resourceDefinition);

    }
}

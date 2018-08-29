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

package org.onap.ccsdk.apps.controllerblueprints.service.validator;


import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.apps.controllerblueprints.service.utils.ConfigModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

public class ServiceTemplateValidationTest {
    private static Logger log = LoggerFactory.getLogger(ServiceTemplateValidationTest.class);

    @Test
    public void testBluePrintDirs() {
        List<String> dirs = ConfigModelUtils.getBlueprintNames("load/blueprints");
        Assert.assertNotNull("Failed to get blueprint directories", dirs);
        Assert.assertEquals("Failed to get actual directories", 2, dirs.size());
    }

    @Test
    public void validateServiceTemplate() throws Exception {
        String file = "load/blueprints/baseconfiguration/Definitions/activation-blueprint.json";
        String serviceTemplateContent =
                FileUtils.readFileToString(new File(file), Charset.defaultCharset());
        ServiceTemplateValidator serviceTemplateValidator = new ServiceTemplateValidator();
        serviceTemplateValidator.validateServiceTemplate(serviceTemplateContent);
        Assert.assertNotNull("Failed to validate blueprints", serviceTemplateValidator);
    }
}

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
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.utils.ConfigModelUtils;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

public class ServiceTemplateValidationTest {
    private static EELFLogger log = EELFManager.getInstance().getLogger(ServiceTemplateValidationTest.class);

    @Test
    public void testBluePrintDirs() {
        List<String> dirs = ConfigModelUtils.getBlueprintNames("load/blueprints");
        Assert.assertNotNull("Failed to get blueprint directories", dirs);
        Assert.assertEquals("Failed to get actual directories", 2, dirs.size());
    }

    @Test
    public void validateServiceTemplate() throws Exception {
        validateServiceTemplate("load/blueprints/baseconfiguration/Definitions/activation-blueprint.json");
        validateServiceTemplate("load/blueprints/vrr-test/Definitions/vrr-test.json");
    }

    //@Test
    public void validateEnhancedServiceTemplate() throws Exception {
        ServiceTemplate serviceTemplate = JacksonUtils
                .readValueFromClassPathFile("enhance/enhanced-template.json", ServiceTemplate.class);
        ServiceTemplateValidator serviceTemplateValidator = new ServiceTemplateValidator();
        Boolean valid = serviceTemplateValidator.validateServiceTemplate(serviceTemplate);
        Assert.assertTrue("Failed to validate blueprints", valid);
    }

    private void validateServiceTemplate(String fileName) throws Exception {
        String serviceTemplateContent =
                FileUtils.readFileToString(new File(fileName), Charset.defaultCharset());
        ServiceTemplateValidator serviceTemplateValidator = new ServiceTemplateValidator();
        serviceTemplateValidator.validateServiceTemplate(serviceTemplateContent);
        Assert.assertNotNull("Failed to validate blueprints", serviceTemplateValidator);
    }
}

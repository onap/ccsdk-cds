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

package org.onap.ccsdk.apps.controllerblueprints.service.enhancer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.apps.controllerblueprints.TestApplication;
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintEnhancerService;
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext;
import org.onap.ccsdk.apps.controllerblueprints.service.load.ModelTypeLoadService;
import org.onap.ccsdk.apps.controllerblueprints.service.load.ResourceDictionaryLoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestApplication.class})
@TestPropertySource(locations = {"classpath:application.properties"})
public class BluePrintEnhancerServiceImplTest {

    @Autowired
    private ModelTypeLoadService modelTypeLoadService;

    @Autowired
    private ResourceDictionaryLoadService resourceDictionaryLoadService;

    @Autowired
    private BluePrintEnhancerService bluePrintEnhancerService;

    @Before
    public void init() {
        modelTypeLoadService.loadModelType("./../../../../components/model-catalog/definition-type/starter-type");
        resourceDictionaryLoadService.loadResourceDictionary("./../../../../components/model-catalog/resource-dictionary/starter-dictionary");
    }

    @Test
    public void testEnhancement() throws Exception {

        String basePath = "./../../../../components/model-catalog/blueprint-model/starter-blueprint/baseconfiguration";

        String targetPath = Paths.get("target", "bp-enhance").toUri().getPath();

        BluePrintContext bluePrintContext = bluePrintEnhancerService.enhance(basePath, targetPath);
        Assert.assertNotNull("failed to get blueprintContext ", bluePrintContext);


    }
}
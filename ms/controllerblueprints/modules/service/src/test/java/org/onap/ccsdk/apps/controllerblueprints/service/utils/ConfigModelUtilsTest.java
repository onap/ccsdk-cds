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

package org.onap.ccsdk.apps.controllerblueprints.service.utils;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModel;

public class ConfigModelUtilsTest {

    @Test
    public void testConfigModel() throws Exception {

        ConfigModel configModel = ConfigModelUtils.getConfigModel("load/blueprints/vrr-test");
        Assert.assertNotNull("Failed to prepare config model", configModel);
        Assert.assertTrue("Failed to prepare config model contents", CollectionUtils.isNotEmpty(configModel.getConfigModelContents()));
    }
}

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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.factory;

import org.junit.Test;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceSourceMapping;
import org.springframework.util.Assert;

public class ResourceSourceMappingFactoryTest {

    @Test
    public void testRegisterResourceMapping() {

        ResourceSourceMappingFactory.INSTANCE.registerSourceMapping("primary-db", "source-primary-db");
        ResourceSourceMappingFactory.INSTANCE.registerSourceMapping("input", "source-input");
        ResourceSourceMappingFactory.INSTANCE.registerSourceMapping("default", "source-default");
        ResourceSourceMappingFactory.INSTANCE.registerSourceMapping("mdsal", "source-rest");

        String nodeTypeName = ResourceSourceMappingFactory.INSTANCE.getRegisterSourceMapping("primary-db");
        Assert.notNull(nodeTypeName, "Failed to get primary-db mapping");

        ResourceSourceMapping resourceSourceMapping = ResourceSourceMappingFactory.INSTANCE.getRegisterSourceMapping();
        Assert.notNull(resourceSourceMapping, "Failed to get resource source mapping");
        Assert.notNull(resourceSourceMapping.getResourceSourceMappings(), "Failed to get resource source mappings");

    }

}

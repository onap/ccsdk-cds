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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.service;

import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeType;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition;

public class ResourceDefinitionRepoServiceTest {

    @Test
    public void testGetResourceDefinition() throws Exception{
        ResourceDefinitionRepoService resourceDefinitionRepoService = new ResourceDefinitionFileRepoService("./../model-catalog");
        ResourceDefinition resourceDefinition = resourceDefinitionRepoService
                .getResourceDefinition("db-source").block();
        Assert.assertNotNull("Failed to get Resource Definition db-source", resourceDefinition);

        NodeType nodeType = resourceDefinitionRepoService.getNodeType("source-db").block();
        Assert.assertNotNull("Failed to get Node Type source-db", resourceDefinition);
    }
}
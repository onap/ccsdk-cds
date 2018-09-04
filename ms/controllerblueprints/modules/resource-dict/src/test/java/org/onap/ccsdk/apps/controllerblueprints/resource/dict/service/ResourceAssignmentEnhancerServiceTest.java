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
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonReactorUtils;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment;

import java.util.List;

/**
 * ResourceAssignmentEnhancerService.
 *
 * @author Brinda Santh
 */
public class ResourceAssignmentEnhancerServiceTest {

    @Test
    public void testEnhanceBluePrint() throws BluePrintException {

        List<ResourceAssignment> resourceAssignments = JacksonReactorUtils
                .getListFromClassPathFile("enrich/simple-enrich.json", ResourceAssignment.class).block();
        Assert.assertNotNull("Failed to get Resource Assignment", resourceAssignments);
        ResourceDefinitionRepoService resourceDefinitionRepoService = new ResourceDefinitionFileRepoService("load");
        ResourceAssignmentEnhancerService resourceAssignmentEnhancerService =
                new ResourceAssignmentEnhancerDefaultService(resourceDefinitionRepoService);
        ServiceTemplate serviceTemplate = resourceAssignmentEnhancerService.enhanceBluePrint(resourceAssignments);
        Assert.assertNotNull("Failed to get Enriched service Template", serviceTemplate);
    }
}


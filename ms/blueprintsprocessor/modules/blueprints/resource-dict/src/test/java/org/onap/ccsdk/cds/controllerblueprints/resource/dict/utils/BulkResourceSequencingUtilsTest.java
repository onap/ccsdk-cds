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

package org.onap.ccsdk.cds.controllerblueprints.resource.dict.utils;

import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment;

import java.util.List;

/**
 * BulkResourceSequencingUtils.
 *
 * @author Brinda Santh
 */
public class BulkResourceSequencingUtilsTest {

    @Test
    public void testProcess() {
        List<ResourceAssignment> assignments =
                JacksonUtils.Companion.getListFromClassPathFile("validation/success.json", ResourceAssignment.class);
        Assert.assertNotNull("failed to get ResourceAssignment from validation/success.json ", assignments);
        BulkResourceSequencingUtils.process(assignments);
    }

}

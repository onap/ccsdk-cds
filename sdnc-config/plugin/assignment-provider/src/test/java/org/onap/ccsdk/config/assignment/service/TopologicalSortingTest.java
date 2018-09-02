/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.onap.ccsdk.config.assignment.service;

import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.config.assignment.processor.ResourceAssignmentProcessor;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class TopologicalSortingTest {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(TopologicalSortingTest.class);
    
    @Test
    public void testBulkSequencingMapping() {
        try {
            logger.info(" **************** Bulk Sequencing Default *****************");
            String resourceMapping = IOUtils.toString(
                    TopologicalSortingTest.class.getClassLoader().getResourceAsStream("mapping/dependency.json"),
                    Charset.defaultCharset());
            
            List<ResourceAssignment> assignments =
                    TransformationUtils.getListfromJson(resourceMapping, ResourceAssignment.class);
            if (assignments != null) {
                SvcLogicContext ctx = new SvcLogicContext();
                ResourceAssignmentProcessor resourceAssignmentProcessor =
                        new ResourceAssignmentProcessor(assignments, ctx);
                List<List<ResourceAssignment>> sequenceBatchResourceAssignment = resourceAssignmentProcessor.process();
                
                Assert.assertNotNull("Failed to populate Sequence Bulk Mappings", sequenceBatchResourceAssignment);
                Assert.assertNotEquals("Failed to populate Sequence Bulk Mappings size ",
                        (sequenceBatchResourceAssignment.size() > 0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

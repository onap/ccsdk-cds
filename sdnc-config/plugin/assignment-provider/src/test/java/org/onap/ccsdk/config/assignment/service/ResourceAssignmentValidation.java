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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.ccsdk.config.model.ConfigModelException;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.config.model.validator.ResourceAssignmentValidator;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ResourceAssignmentValidation {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ResourceAssignmentValidation.class);
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void testValidateSuccess() {
        try {
            logger.info(" **************** testValidateSuccess *****************");
            String resourceMapping = IOUtils.toString(
                    TopologicalSortingTest.class.getClassLoader().getResourceAsStream("validation/success.json"),
                    Charset.defaultCharset());
            
            List<ResourceAssignment> assignments =
                    TransformationUtils.getListfromJson(resourceMapping, ResourceAssignment.class);
            if (assignments != null) {
                ResourceAssignmentValidator resourceAssignmentValidator = new ResourceAssignmentValidator(assignments);
                
                boolean result = resourceAssignmentValidator.validateResourceAssignment();
                Assert.assertTrue("Failed to Validate", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test(expected = ConfigModelException.class)
    public void testValidateDuplicate() throws IOException, ConfigModelException {
        logger.info(" **************** testValidateDuplicate *****************");
        String resourceMapping = IOUtils.toString(
                TopologicalSortingTest.class.getClassLoader().getResourceAsStream("validation/duplicate.json"),
                Charset.defaultCharset());
        
        List<ResourceAssignment> assignments =
                TransformationUtils.getListfromJson(resourceMapping, ResourceAssignment.class);
        if (assignments != null) {
            ResourceAssignmentValidator resourceAssignmentValidator = new ResourceAssignmentValidator(assignments);
            resourceAssignmentValidator.validateResourceAssignment();
        }
        
    }
    
    @Test(expected = ConfigModelException.class)
    public void testValidateCyclic() throws IOException, ConfigModelException {
        logger.info(" ****************  testValidateCyclic *****************");
        String resourceMapping = IOUtils.toString(
                TopologicalSortingTest.class.getClassLoader().getResourceAsStream("validation/cyclic.json"),
                Charset.defaultCharset());
        
        List<ResourceAssignment> assignments =
                TransformationUtils.getListfromJson(resourceMapping, ResourceAssignment.class);
        if (assignments != null) {
            ResourceAssignmentValidator resourceAssignmentValidator = new ResourceAssignmentValidator(assignments);
            
            resourceAssignmentValidator.validateResourceAssignment();
        }
        
    }
}

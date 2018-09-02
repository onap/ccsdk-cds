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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.ValidTypes;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.utils.ResourceAssignmentUtils;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;

public class ResourceAssignmentGenerationTest {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ResourceAssignmentGenerationTest.class);
    
    @Test
    public void testResourceDataSetting() {
        try {
            logger.info(" **************** testResourceDataSetting *****************");
            String resourceAssignmentContents = IOUtils.toString(TopologicalSortingTest.class.getClassLoader()
                    .getResourceAsStream("assignments/alltype-empty-value-mapping.json"), Charset.defaultCharset());
            
            List<ResourceAssignment> assignments =
                    TransformationUtils.getListfromJson(resourceAssignmentContents, ResourceAssignment.class);
            if (assignments != null) {
                Map<String, Object> componentContext = new HashMap<>();
                componentContext.put(ConfigModelConstant.PROPERTY_ACTION_NAME, "sample-recipe");
                for (ResourceAssignment resourceAssignment : assignments) {
                    if (resourceAssignment != null && resourceAssignment.getProperty() != null) {
                        String type = resourceAssignment.getProperty().getType();
                        Object value = null;
                        if (ValidTypes.DATA_TYPE_STRING.equals(type)) {
                            value = new String("abcdef");
                        } else if (ValidTypes.DATA_TYPE_INTEGER.equals(type)) {
                            value = new Integer(1234);
                        } else if (ValidTypes.DATA_TYPE_BOOLEAN.equals(type)) {
                            value = new Boolean(true);
                        } else if (ValidTypes.DATA_TYPE_LIST.equals(type)) {
                            String entityType = resourceAssignment.getProperty().getEntrySchema().getType();
                            if (ValidTypes.DATA_TYPE_STRING.equals(entityType)) {
                                value = "[\"abcd-array\"]";
                            } else {
                                String content = "[{\"name\" : \"abcd-array-complex\"}]";
                                JsonNode node = TransformationUtils.getJsonNodeForString(content);
                                value = node;
                            }
                        } else {
                            String content = "{\"name\" : \"abcd-complex\"}";
                            JsonNode node = TransformationUtils.getJsonNodeForString(content);
                            value = node;
                        }
                        ResourceAssignmentUtils.setResourceDataValue(componentContext, resourceAssignment, value);
                    }
                }
                String generatedData = ResourceAssignmentUtils.generateResourceDataForAssignments(assignments);
                logger.trace("Generated Data " + generatedData);
                
                Assert.assertNotNull("Failed to generate resource data", generatedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testGenerateResourceData() {
        try {
            logger.info(" **************** testGenerateResourceData *****************");
            String resourceAssignmentContents = IOUtils.toString(TopologicalSortingTest.class.getClassLoader()
                    .getResourceAsStream("assignments/alltype-mapping.json"), Charset.defaultCharset());
            
            List<ResourceAssignment> assignments =
                    TransformationUtils.getListfromJson(resourceAssignmentContents, ResourceAssignment.class);
            if (assignments != null) {
                
                String generatedData = ResourceAssignmentUtils.generateResourceDataForAssignments(assignments);
                logger.trace("Generated Data " + generatedData);
                
                Assert.assertNotNull("Failed to generate resource data", generatedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

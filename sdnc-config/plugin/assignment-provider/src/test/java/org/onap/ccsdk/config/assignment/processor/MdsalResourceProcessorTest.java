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

package org.onap.ccsdk.config.assignment.processor;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.onap.ccsdk.config.assignment.service.ConfigResourceAssignmentTestUtils;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.data.dict.ResourceDefinition;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

@RunWith(MockitoJUnitRunner.class)
public class MdsalResourceProcessorTest {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(MdsalResourceProcessorTest.class);
    
    @Mock
    private ConfigRestAdaptorService configRestAdaptorService;
    
    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMdsalSimpleProcess() throws Exception {
        logger.info(" *******************************  testMdsalSimpleProcess  ***************************");
        
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                String response = null;
                if (args != null) {
                    response = FileUtils.readFileToString(
                            new File("src/test/resources/mapping/Mdsal/simple-response.json"),
                            Charset.defaultCharset());
                    logger.info(" Returning response :" + response);
                }
                return response;
            }
        }).when(configRestAdaptorService).getResource(Matchers.anyString(), Matchers.anyString(),
                Matchers.any(Class.class));
        
        String recipeName = "sample-recipe";
        
        String resourceassignmentContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/Mdsal/resource-assignments-simple.json"),
                Charset.defaultCharset());
        List<ResourceAssignment> batchResourceAssignment =
                TransformationUtils.getListfromJson(resourceassignmentContent, ResourceAssignment.class);
        
        String dictionaryContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/Mdsal/mdsal-simple.json"), Charset.defaultCharset());
        Map<String, ResourceDefinition> dictionaries =
                ConfigResourceAssignmentTestUtils.getMapfromJson(dictionaryContent);
        MdsalResourceProcessor mdsalResourceProcessor = new MdsalResourceProcessor(configRestAdaptorService);
        Map<String, Object> componentContext = new HashMap<>();
        componentContext.put(ConfigModelConstant.PROPERTY_RESOURCE_ASSIGNMENTS, batchResourceAssignment);
        componentContext.put(ConfigModelConstant.PROPERTY_ACTION_NAME, recipeName);
        componentContext.put(ConfigModelConstant.PROPERTY_TEMPLATE_NAME, "sample-template");
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARIES, dictionaries);
        
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".profile_name", "sample");
        
        Map<String, String> inParams = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        mdsalResourceProcessor.process(inParams, ctx, componentContext);
        
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testMDSALComplexProcess() throws Exception {
        logger.info(" *******************************  testMDSALComplexProcess  ***************************");
        
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                String response = null;
                if (args != null) {
                    response = FileUtils.readFileToString(
                            new File("src/test/resources/mapping/Mdsal/complex-response.json"),
                            Charset.defaultCharset());
                }
                return response;
            }
        }).when(configRestAdaptorService).getResource(Matchers.anyString(), Matchers.anyString(),
                Matchers.any(Class.class));
        
        String recipeName = "sample-recipe";
        
        String resourceassignmentContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/Mdsal/resource-assignments-complex.json"),
                Charset.defaultCharset());
        List<ResourceAssignment> batchResourceAssignment =
                TransformationUtils.getListfromJson(resourceassignmentContent, ResourceAssignment.class);
        
        String dictionaryContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/Mdsal/mdsal-complex.json"), Charset.defaultCharset());
        Map<String, ResourceDefinition> dictionaries =
                ConfigResourceAssignmentTestUtils.getMapfromJson(dictionaryContent);
        MdsalResourceProcessor dbResourceProcessor = new MdsalResourceProcessor(configRestAdaptorService);
        Map<String, Object> componentContext = new HashMap<>();
        componentContext.put(ConfigModelConstant.PROPERTY_RESOURCE_ASSIGNMENTS, batchResourceAssignment);
        componentContext.put(ConfigModelConstant.PROPERTY_ACTION_NAME, recipeName);
        componentContext.put(ConfigModelConstant.PROPERTY_TEMPLATE_NAME, "sample-template");
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARIES, dictionaries);
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".profile_name", "sample");
        
        Map<String, String> inParams = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        String datatypeContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/Mdsal/dt-location.json"), Charset.defaultCharset());
        ctx.setAttribute("data_types.dt-location", datatypeContent);
        dbResourceProcessor.process(inParams, ctx, componentContext);
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMDSALArrayComplexProcess() throws Exception {
        logger.info(" *******************************  testMDSALArrayComplexProcess  ***************************");
        
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                String response = null;
                if (args != null) {
                    response = FileUtils.readFileToString(
                            new File("src/test/resources/mapping/Mdsal/array-complex-response.json"),
                            Charset.defaultCharset());
                }
                return response;
            }
        }).when(configRestAdaptorService).getResource(Matchers.anyString(), Matchers.anyString(),
                Matchers.any(Class.class));
        
        String recipeName = "sample-recipe";
        
        String resourceassignmentContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/Mdsal/resource-assignments-array.json"), Charset.defaultCharset());
        List<ResourceAssignment> batchResourceAssignment =
                TransformationUtils.getListfromJson(resourceassignmentContent, ResourceAssignment.class);
        
        String dictionaryContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/Mdsal/mdsal-array.json"), Charset.defaultCharset());
        
        Map<String, ResourceDefinition> dictionaries =
                ConfigResourceAssignmentTestUtils.getMapfromJson(dictionaryContent);
        MdsalResourceProcessor dbResourceProcessor = new MdsalResourceProcessor(configRestAdaptorService);
        Map<String, Object> componentContext = new HashMap<>();
        componentContext.put(ConfigModelConstant.PROPERTY_RESOURCE_ASSIGNMENTS, batchResourceAssignment);
        componentContext.put(ConfigModelConstant.PROPERTY_ACTION_NAME, recipeName);
        componentContext.put(ConfigModelConstant.PROPERTY_TEMPLATE_NAME, "sample-template");
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARIES, dictionaries);
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".profile_name", "sample");
        
        Map<String, String> inParams = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        String datatypeContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/Mdsal/dt-location.json"), Charset.defaultCharset());
        ctx.setAttribute("data_types.dt-location", datatypeContent);
        dbResourceProcessor.process(inParams, ctx, componentContext);
        logger.info("Component Context = ({})", componentContext);
        Assert.assertNotNull("failed to populate Array Complex response ", componentContext);
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMDSALArraySimpleProcess() throws Exception {
        logger.info(" *******************************  testMDSALArrayComplexProcess  ***************************");
        
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                String response = null;
                if (args != null) {
                    response = FileUtils.readFileToString(
                            new File("src/test/resources/mapping/Mdsal/array-complex-v4-assigned-response.json"),
                            Charset.defaultCharset());
                }
                return response;
            }
        }).when(configRestAdaptorService).getResource(Matchers.anyString(), Matchers.anyString(),
                Matchers.any(Class.class));
        
        String recipeName = "sample-recipe";
        
        String resourceassignmentContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/Mdsal/resource-assignments-complex-simple.json"),
                Charset.defaultCharset());
        List<ResourceAssignment> batchResourceAssignment =
                TransformationUtils.getListfromJson(resourceassignmentContent, ResourceAssignment.class);
        
        String dictionaryContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/Mdsal/mdsal-array-v4iplist.json"), Charset.defaultCharset());
        Map<String, ResourceDefinition> dictionaries =
                ConfigResourceAssignmentTestUtils.getMapfromJson(dictionaryContent);
        MdsalResourceProcessor mdsalResourceProcessor = new MdsalResourceProcessor(configRestAdaptorService);
        Map<String, Object> componentContext = new HashMap<>();
        componentContext.put(ConfigModelConstant.PROPERTY_RESOURCE_ASSIGNMENTS, batchResourceAssignment);
        componentContext.put(ConfigModelConstant.PROPERTY_ACTION_NAME, recipeName);
        componentContext.put(ConfigModelConstant.PROPERTY_TEMPLATE_NAME, "sample-template");
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARIES, dictionaries);
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".service-instance-id",
                "3c8d5a63-a793-4206-a67c-4b2e8e648196");
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".oam-network-role",
                "sample");
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".oam-ipv4-ip-type",
                "sample");
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".profile_name", "sample");
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".oam-vm-type", "sample");
        
        Map<String, String> inParams = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        // String datatypeContent = FileUtils.readFileToString(new
        // File("src/test/resources/mapping/Mdsal/dt-v4-assigned-ip-list.json"), Charset.defaultCharset() );
        // ctx.setAttribute("data_types.dt-v4-assigned-ip-list", datatypeContent);
        mdsalResourceProcessor.process(inParams, ctx, componentContext);
        logger.info("Component Context = ({})", componentContext);
        Assert.assertNotNull("failed to populate Array Complex response ", componentContext);
        Assert.assertEquals("Compare String ", "10.66.1.152",
                componentContext.get(ConfigModelConstant.PROPERTY_RECIPE_KEY_DOT + recipeName + ".v4-ip-prefix"));
        
    }
    
}

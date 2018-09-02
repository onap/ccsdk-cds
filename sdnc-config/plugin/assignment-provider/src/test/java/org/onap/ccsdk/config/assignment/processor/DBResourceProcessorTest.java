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

import static org.mockito.Matchers.any;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.onap.ccsdk.config.assignment.service.ConfigResourceAssignmentTestUtils;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.data.dict.ResourceDefinition;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

@RunWith(MockitoJUnitRunner.class)
public class DBResourceProcessorTest {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(DBResourceProcessorTest.class);
    
    @Mock
    private ConfigResourceService configResourceService;
    
    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        
        try {
            Mockito.doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                    Object[] args = invocationOnMock.getArguments();
                    if (args != null) {
                        logger.trace("Transaction info " + Arrays.asList(args));
                    }
                    return null;
                }
            }).when(configResourceService).save(any(TransactionLog.class));
            
        } catch (SvcLogicException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testDbSimpleProcess() throws Exception {
        logger.info(" *******************************  testDbSimpleProcess  ***************************");
        
        Mockito.doAnswer(new Answer<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                List<Map<String, Object>> results = new ArrayList<>();
                if (args != null) {
                    logger.info("Query " + Arrays.asList(args));
                    Map<String, Object> record = new HashMap<>();
                    record.put("country", "US");
                    results.add(record);
                }
                return results;
            }
        }).when(configResourceService).query(Matchers.anyString(), Matchers.<Map<String, Object>>any());
        
        String recipeName = "sample-recipe";
        
        String resourceassignmentContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/db/resource-assignments-simple.json"), Charset.defaultCharset());
        List<ResourceAssignment> batchResourceAssignment =
                TransformationUtils.getListfromJson(resourceassignmentContent, ResourceAssignment.class);
        
        String dictionaryContent = FileUtils.readFileToString(new File("src/test/resources/mapping/db/db-simple.json"),
                Charset.defaultCharset());
        Map<String, ResourceDefinition> dictionaries =
                ConfigResourceAssignmentTestUtils.getMapfromJson(dictionaryContent);
        DBResourceProcessor dbResourceProcessor = new DBResourceProcessor(configResourceService);
        Map<String, Object> componentContext = new HashMap<>();
        componentContext.put(ConfigModelConstant.PROPERTY_RESOURCE_ASSIGNMENTS, batchResourceAssignment);
        componentContext.put(ConfigModelConstant.PROPERTY_ACTION_NAME, recipeName);
        componentContext.put(ConfigModelConstant.PROPERTY_TEMPLATE_NAME, "sample-template");
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARIES, dictionaries);
        
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".profile_name", "sample");
        
        Map<String, String> inParams = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        dbResourceProcessor.process(inParams, ctx, componentContext);
        
        logger.info(" Context " + componentContext);
        
    }
    
    @Test
    public void testDbComplexProcess() throws Exception {
        logger.info(" *******************************  testDbComplexProcess  ***************************");
        
        Mockito.doAnswer(new Answer<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                List<Map<String, Object>> results = new ArrayList<>();
                if (args != null) {
                    logger.info("Query " + Arrays.asList(args));
                    Map<String, Object> record = new HashMap<>();
                    record.put("db-country", "US");
                    record.put("db-state", "NJ");
                    results.add(record);
                }
                return results;
            }
        }).when(configResourceService).query(Matchers.anyString(), Matchers.<Map<String, Object>>any());
        
        String recipeName = "sample-recipe";
        
        String resourceassignmentContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/db/resource-assignments-complex.json"), Charset.defaultCharset());
        List<ResourceAssignment> batchResourceAssignment =
                TransformationUtils.getListfromJson(resourceassignmentContent, ResourceAssignment.class);
        
        String dictionaryContent = FileUtils.readFileToString(new File("src/test/resources/mapping/db/db-complex.json"),
                Charset.defaultCharset());
        Map<String, ResourceDefinition> dictionaries =
                ConfigResourceAssignmentTestUtils.getMapfromJson(dictionaryContent);
        DBResourceProcessor dbResourceProcessor = new DBResourceProcessor(configResourceService);
        Map<String, Object> componentContext = new HashMap<>();
        componentContext.put(ConfigModelConstant.PROPERTY_RESOURCE_ASSIGNMENTS, batchResourceAssignment);
        componentContext.put(ConfigModelConstant.PROPERTY_ACTION_NAME, recipeName);
        componentContext.put(ConfigModelConstant.PROPERTY_TEMPLATE_NAME, "sample-template");
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARIES, dictionaries);
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".profile_name", "sample");
        
        Map<String, String> inParams = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        String datatypeContent = FileUtils.readFileToString(new File("src/test/resources/mapping/db/dt-location.json"),
                Charset.defaultCharset());
        ctx.setAttribute("data_types.dt-location", datatypeContent);
        dbResourceProcessor.process(inParams, ctx, componentContext);
        
    }
    
    @Test
    public void testDbArrayComplexProcess() throws Exception {
        logger.info(" *******************************  testDbArrayComplexProcess  ***************************");
        Mockito.doAnswer(new Answer<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                List<Map<String, Object>> results = new ArrayList<>();
                if (args != null) {
                    logger.info("Query " + Arrays.asList(args));
                    Map<String, Object> record = new HashMap<>();
                    record.put("db-country", "US");
                    record.put("db-state", "NJ");
                    results.add(record);
                    
                    Map<String, Object> record2 = new HashMap<>();
                    record2.put("db-country", "INDIA");
                    record2.put("db-state", "TN");
                    results.add(record2);
                }
                return results;
            }
        }).when(configResourceService).query(Matchers.anyString(), Matchers.<Map<String, Object>>any());
        
        String recipeName = "sample-recipe";
        
        String resourceassignmentContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/db/resource-assignments-array.json"), Charset.defaultCharset());
        List<ResourceAssignment> batchResourceAssignment =
                TransformationUtils.getListfromJson(resourceassignmentContent, ResourceAssignment.class);
        
        String dictionaryContent = FileUtils.readFileToString(new File("src/test/resources/mapping/db/db-array.json"),
                Charset.defaultCharset());
        Map<String, ResourceDefinition> dictionaries =
                ConfigResourceAssignmentTestUtils.getMapfromJson(dictionaryContent);
        DBResourceProcessor dbResourceProcessor = new DBResourceProcessor(configResourceService);
        Map<String, Object> componentContext = new HashMap<>();
        componentContext.put(ConfigModelConstant.PROPERTY_RESOURCE_ASSIGNMENTS, batchResourceAssignment);
        componentContext.put(ConfigModelConstant.PROPERTY_ACTION_NAME, recipeName);
        componentContext.put(ConfigModelConstant.PROPERTY_TEMPLATE_NAME, "sample-template");
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARIES, dictionaries);
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".profile_name", "sample");
        
        Map<String, String> inParams = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        String datatypeContent = FileUtils.readFileToString(new File("src/test/resources/mapping/db/dt-location.json"),
                Charset.defaultCharset());
        ctx.setAttribute("data_types.dt-location", datatypeContent);
        dbResourceProcessor.process(inParams, ctx, componentContext);
        logger.info("Component Context = ({})", componentContext);
        Assert.assertNotNull("faile dto populate Array Complex response ", componentContext);
        
    }
    
}

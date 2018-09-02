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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class DefaultResourceProcessorTest {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(DefaultResourceProcessorTest.class);
    
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
    public void testDefaultSimpleProcess() throws Exception {
        logger.info(" *******************************  testDefaultSimpleProcess  ***************************");
        
        String recipeName = "sample-recipe";
        
        String resourceassignmentContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/default/resource-assignments-simple.json"),
                Charset.defaultCharset());
        List<ResourceAssignment> batchResourceAssignment =
                TransformationUtils.getListfromJson(resourceassignmentContent, ResourceAssignment.class);
        
        String dictionaryContent = FileUtils.readFileToString(
                new File("src/test/resources/mapping/default/default-simple.json"), Charset.defaultCharset());
        Map<String, ResourceDefinition> dictionaries =
                ConfigResourceAssignmentTestUtils.getMapfromJson(dictionaryContent);
        
        DefaultResourceProcessor defaultResourceProcessor = new DefaultResourceProcessor(configResourceService);
        Map<String, Object> componentContext = new HashMap<>();
        componentContext.put(ConfigModelConstant.PROPERTY_RESOURCE_ASSIGNMENTS, batchResourceAssignment);
        componentContext.put(ConfigModelConstant.PROPERTY_ACTION_NAME, recipeName);
        componentContext.put(ConfigModelConstant.PROPERTY_TEMPLATE_NAME, "sample-template");
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARIES, dictionaries);
        componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".profile_name", "sample");
        
        Map<String, String> inParams = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        defaultResourceProcessor.process(inParams, ctx, componentContext);
        logger.trace(" componentContext " + componentContext);
        
        Assert.assertEquals("Failed to populate default country value ", "US",
                componentContext.get(ConfigModelConstant.PROPERTY_RECIPE_KEY_DOT + "sample-recipe.country"));
        Assert.assertEquals("Failed to populate default country value ", "US",
                componentContext.get(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + "sample-recipe.country"));
        
        Assert.assertEquals("Failed to populate default port value ", 830,
                componentContext.get(ConfigModelConstant.PROPERTY_RECIPE_KEY_DOT + "sample-recipe.port"));
        Assert.assertEquals("Failed to populate default port value ", 830,
                componentContext.get(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + "sample-recipe.port"));
        
        Assert.assertEquals("Failed to populate default voip-enabled value ", true,
                componentContext.get(ConfigModelConstant.PROPERTY_RECIPE_KEY_DOT + "sample-recipe.voip-enabled"));
        Assert.assertEquals("Failed to populate default voip-enabled value ", true,
                componentContext.get(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + "sample-recipe.voip-enabled"));
        
    }
    
}

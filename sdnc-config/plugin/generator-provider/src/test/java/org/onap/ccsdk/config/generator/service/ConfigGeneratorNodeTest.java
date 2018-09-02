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

package org.onap.ccsdk.config.generator.service;

import static org.mockito.Matchers.any;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
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
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.generator.ConfigGeneratorConstant;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.service.ConfigModelService;
import org.onap.ccsdk.config.model.service.ConfigModelServiceImpl;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

@RunWith(MockitoJUnitRunner.class)
public class ConfigGeneratorNodeTest {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigGeneratorNodeTest.class);
    
    @Mock
    private ConfigResourceService configResourceService;
    
    @Mock
    private ConfigRestAdaptorService configRestAdaptorService;
    
    private ConfigModelService configModelService;
    
    @Before
    public void setUp() throws Exception {
        
        configModelService = new ConfigModelServiceImpl(configRestAdaptorService);
        
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
            
            Mockito.doAnswer(new Answer<List<ConfigResource>>() {
                @Override
                public List<ConfigResource> answer(InvocationOnMock invocationOnMock) throws Throwable {
                    List<ConfigResource> configResources = new ArrayList<>();
                    Object[] args = invocationOnMock.getArguments();
                    if (args != null) {
                        logger.trace("Transaction info " + Arrays.asList(args));
                        String resourceData = IOUtils.toString(ConfigGeneratorNodeTest.class.getClassLoader()
                                .getResourceAsStream("service_templates/configdata.json"), Charset.defaultCharset());
                        ConfigResource configResource = (ConfigResource) args[0];
                        configResource.setRecipeName("Sample-recipe");
                        configResource.setResourceData(resourceData);
                        configResources.add(configResource);
                    }
                    return configResources;
                }
            }).when(configResourceService).getConfigResource(any(ConfigResource.class));
            
        } catch (SvcLogicException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testInputTemplateContentNData() throws Exception {
        
        ConfigGeneratorNode configGeneratorNode = new ConfigGeneratorNode(configResourceService, configModelService);
        
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigModelConstant.PROPERTY_SELECTOR, "test");
        
        String jsonData = IOUtils.toString(
                ConfigGeneratorNodeTest.class.getClassLoader().getResourceAsStream("service_templates/configdata.json"),
                Charset.defaultCharset());
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA, jsonData);
        
        String templateData = IOUtils.toString(ConfigGeneratorNodeTest.class.getClassLoader()
                .getResourceAsStream("service_templates/velocity/base-config-template.vtl"), Charset.defaultCharset());
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_CONTENT, templateData);
        
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, Object> componentContext = new HashMap<>();
        configGeneratorNode.process(inParams, ctx, componentContext);
        Assert.assertEquals("Failed to generate Configuration Status as Failure",
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS,
                ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS));
        Assert.assertNotNull("Failed to generate Configuration",
                ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_GENERATED_CONFIG));
        
        logger.trace("Generated Configuration:\n "
                + ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_GENERATED_CONFIG));
        logger.trace("Generated Configuration:\n "
                + ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_MASK_INFO));
        
    }
    
    @Test
    public void testInputTemplateWithNullData() throws Exception {
        
        ConfigGeneratorNode configGeneratorNode = new ConfigGeneratorNode(configResourceService, configModelService);
        
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigModelConstant.PROPERTY_SELECTOR, "test");
        
        String jsonData = IOUtils.toString(ConfigGeneratorNodeTest.class.getClassLoader()
                .getResourceAsStream("service_templates/configdata_with_null.json"), Charset.defaultCharset());
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA, jsonData);
        
        String templateData = IOUtils.toString(ConfigGeneratorNodeTest.class.getClassLoader()
                .getResourceAsStream("service_templates/velocity/base-config-template.vtl"), Charset.defaultCharset());
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_CONTENT, templateData);
        
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, Object> componentContext = new HashMap<>();
        configGeneratorNode.process(inParams, ctx, componentContext);
        Assert.assertEquals("Failed to generate Configuration Status as Failure",
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS,
                ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS));
        Assert.assertNotNull("Failed to generate Configuration",
                ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_GENERATED_CONFIG));
        
        logger.trace("Generated Configuration:\n "
                + ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_GENERATED_CONFIG));
        logger.trace("Generated Configuration:\n "
                + ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_MASK_INFO));
        
    }
    
    @Test
    public void testDBTemplateContentNData() throws Exception {
        
        String fileContent = IOUtils.toString(ConfigGeneratorNodeTest.class.getClassLoader()
                .getResourceAsStream("service_templates/generate_configuration.json"), Charset.defaultCharset());
        
        String baseConfigTemplateContent = IOUtils.toString(ConfigGeneratorNodeTest.class.getClassLoader()
                .getResourceAsStream("service_templates/velocity/base-config-template.vtl"), Charset.defaultCharset());
        
        Map<String, String> context = new HashMap<>();
        context = configModelService.convertServiceTemplate2Properties(fileContent, context);
        
        context.put("node_templates.base-config-template.content", baseConfigTemplateContent);
        
        Assert.assertNotNull("Failed to Prepare Context : ", context);
        
        context.put("request-id", "12345");
        context.put("vnf-id", "vnf12345");
        context.put("action-name", "config-generator-action");
        
        Map<String, String> inparams = new HashMap<String, String>();
        inparams.put(ConfigModelConstant.PROPERTY_SELECTOR, "generate-configuration");
        
        SvcLogicContext inputContext = new SvcLogicContext();
        context.forEach((name, value) -> {
            inputContext.setAttribute(name, value);
        });
        
        TransformationUtils.printMap(context);
        configModelService.assignInParamsFromModel(inputContext, inparams);
        ConfigGeneratorNode configGeneratorNode = new ConfigGeneratorNode(configResourceService, configModelService);
        
        Map<String, Object> componentContext = new HashMap<>();
        configGeneratorNode.process(inparams, inputContext, componentContext);
        
        Assert.assertEquals("Failed to generate Configuration Status as Failure",
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS,
                inputContext.getAttribute("generate-configuration." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS));
        Assert.assertNotNull("Failed to generate Configuration", inputContext
                .getAttribute("generate-configuration." + ConfigGeneratorConstant.OUTPUT_PARAM_GENERATED_CONFIG));
        
        logger.trace("Generated Configuration:\n " + inputContext
                .getAttribute("generate-configuration." + ConfigGeneratorConstant.OUTPUT_PARAM_GENERATED_CONFIG));
    }
    
    @Test
    public void testTemplateContentNDataForMask() throws Exception {
        
    }
    
}

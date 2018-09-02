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

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.config.assignment.ConfigAssignmentConstants;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.generator.service.ConfigGeneratorService;
import org.onap.ccsdk.config.generator.service.ConfigGeneratorServiceImpl;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.service.ComponentNodeDelegate;
import org.onap.ccsdk.config.model.service.ComponentNodeService;
import org.onap.ccsdk.config.model.service.ComponentNodeServiceImpl;
import org.onap.ccsdk.config.model.service.ConfigModelService;
import org.onap.ccsdk.config.model.service.ConfigModelServiceImpl;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.osgi.framework.BundleContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

@RunWith(MockitoJUnitRunner.class)
public class ConfigAssignmentNodeTest {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigAssignmentNodeTest.class);
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Mock
    private ConfigResourceService configResourceService;
    
    @Mock
    private ConfigRestAdaptorService configRestAdaptorService;
    
    private ConfigModelService configModelService;
    
    private ComponentNodeService componentNodeService;
    
    private ConfigGeneratorService configGeneratorService;
    
    BundleContext bundleContext = MockOsgi.newBundleContext();
    
    @SuppressWarnings("unchecked")
    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);
        /*
         * ConfigAssignmentNode configAssignmentNode = new ConfigAssignmentNode(componentNodeService,
         * configResourceService, configModelService, configRestAdaptorService, configGeneratorService);
         * bundleContext.registerService(ConfigAssignmentNode.class, configAssignmentNode, null);
         */
    }
    
    @Test
    public void testResourceAssignment() throws Exception {
        ConfigResourceAssignmentTestUtils.injectTransactionLogSaveMock(configResourceService);
        
        ConfigResourceAssignmentTestUtils.injectConfigModelMock(configRestAdaptorService, "resource_assignment");
        
        ConfigResourceAssignmentTestUtils.injectResourceDictionaryMock(configRestAdaptorService,
                "assignments/empty-dictionary.json");
        
        ConfigResourceAssignmentTestUtils.injectConfigResourceSaveMock(configResourceService);
        
        componentNodeService =
                new ComponentNodeServiceImpl(bundleContext, configResourceService, configRestAdaptorService);
        configModelService = new ConfigModelServiceImpl(configRestAdaptorService);
        configGeneratorService = new ConfigGeneratorServiceImpl(configResourceService);
        
        ConfigAssignmentNode configAssignmentNode = new ConfigAssignmentNode(configResourceService,
                configRestAdaptorService, configModelService, componentNodeService, configGeneratorService);
        
        String inputContent = FileUtils.readFileToString(
                new File("src/test/resources/service_templates/input/input.json"), Charset.defaultCharset());
        
        Map<String, String> inParams = new HashMap<>();
        inParams.put(ConfigModelConstant.PROPERTY_SELECTOR, "test");
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_REQUEST_ID, "1234");
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_RESOURCE_ID, "resourceid-1234");
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_RESOURCE_TYPE, "vnf-type");
        inParams.put(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_NAME, "vpe-201802-baseconfig");
        inParams.put(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_VERSION, "1.0.0");
        inParams.put(ConfigModelConstant.PROPERTY_ACTION_NAME, "resource-assignment-action");
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_INPUT_DATA, inputContent);
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_TEMPLATE_NAMES, "[\"base-config-template\"]");
        
        // Populate the SvcContext ( Simulation)
        SvcLogicContext svcLogicContext = new SvcLogicContext();
        Map<String, String> context = new HashMap<>();
        context.put(ConfigModelConstant.PROPERTY_ACTION_NAME, "resource-assignment-action");
        context = configModelService.prepareContext(context, inputContent, "vpe-201802-baseconfig", "1.0.0");
        context.forEach((key, value) -> svcLogicContext.setAttribute(key, value));
        
        Map<String, Object> componentContext = new HashMap<>();
        configAssignmentNode.process(inParams, svcLogicContext, componentContext);
        Assert.assertNotNull("Failed to get response status", svcLogicContext.getAttribute("test.status"));
        
    }
    
    @Test
    public void testSimplePreview() throws Exception {
        ConfigResourceAssignmentTestUtils.injectTransactionLogSaveMock(configResourceService);
        
        ConfigResourceAssignmentTestUtils.injectConfigModelMock(configRestAdaptorService, "resource_assignment");
        
        ConfigResourceAssignmentTestUtils.injectResourceDictionaryMock(configRestAdaptorService,
                "assignments/empty-dictionary.json");
        
        ConfigResourceAssignmentTestUtils.injectConfigResourceSaveMock(configResourceService);
        
        componentNodeService =
                new ComponentNodeServiceImpl(bundleContext, configResourceService, configRestAdaptorService);
        configModelService = new ConfigModelServiceImpl(configRestAdaptorService);
        configGeneratorService = new ConfigGeneratorServiceImpl(configResourceService);
        
        ConfigAssignmentNode configAssignmentNode = new ConfigAssignmentNode(configResourceService,
                configRestAdaptorService, configModelService, componentNodeService, configGeneratorService);
        
        String inputContent = FileUtils.readFileToString(
                new File("src/test/resources/service_templates/input/input.json"), Charset.defaultCharset());
        
        Map<String, String> inParams = new HashMap<>();
        inParams.put(ConfigModelConstant.PROPERTY_SELECTOR, "test");
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_REQUEST_ID, "1234");
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_RESOURCE_ID, "resourceid-1234");
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_RESOURCE_TYPE, "vnf-type");
        inParams.put(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_NAME, "vpe-201802-baseconfig");
        inParams.put(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_VERSION, "1.0.0");
        inParams.put(ConfigModelConstant.PROPERTY_ACTION_NAME, "resource-assignment-action");
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_INPUT_DATA, inputContent);
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_TEMPLATE_NAMES, "[\"base-config-template\"]");
        SvcLogicContext ctx = new SvcLogicContext();
        configAssignmentNode.process(inParams, ctx);
        Assert.assertNotNull("Failed to get response mashed Content",
                ctx.getAttribute("test.mashed-data.base-config-template"));
    }
    
    @Test
    public void testComplexPreview() throws Exception {
        ConfigResourceAssignmentTestUtils.injectTransactionLogSaveMock(configResourceService);
        
        ConfigResourceAssignmentTestUtils.injectConfigModelMock(configRestAdaptorService, "vpe-201802-baseconfig");
        
        ConfigResourceAssignmentTestUtils.injectResourceDictionaryMock(configRestAdaptorService,
                "service_templates/vpe-201802-baseconfig/dict.json");
        
        ConfigResourceAssignmentTestUtils.injectConfigResourceSaveMock(configResourceService);
        
        componentNodeService =
                new ComponentNodeServiceImpl(bundleContext, configResourceService, configRestAdaptorService);
        configModelService = new ConfigModelServiceImpl(configRestAdaptorService);
        configGeneratorService = new ConfigGeneratorServiceImpl(configResourceService);
        
        ConfigAssignmentNode configAssignmentNode = new ConfigAssignmentNode(configResourceService,
                configRestAdaptorService, configModelService, componentNodeService, configGeneratorService);
        
        String inputContent = FileUtils.readFileToString(
                new File("src/test/resources/service_templates/vpe-201802-baseconfig/input-complex.json"),
                Charset.defaultCharset());
        
        Map<String, String> inParams = new HashMap<>();
        inParams.put(ConfigModelConstant.PROPERTY_SELECTOR, "complex-test");
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_REQUEST_ID, "request-1234");
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_RESOURCE_ID, "resourceid-1234");
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_RESOURCE_TYPE, "vnf-type");
        inParams.put(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_NAME, "vpe-201802-baseconfig");
        inParams.put(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_VERSION, "1.0.0");
        inParams.put(ConfigModelConstant.PROPERTY_ACTION_NAME, "resource-assignment-action");
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_INPUT_DATA, inputContent);
        inParams.put(ConfigAssignmentConstants.INPUT_PARAM_TEMPLATE_NAMES, "[\"base-config-template\"]");
        SvcLogicContext ctx = new SvcLogicContext();
        configAssignmentNode.process(inParams, ctx);
        Assert.assertNotNull("Failed to get response mashed Content",
                ctx.getAttribute("complex-test.mashed-data.base-config-template"));
        
    }
    
    @Test
    public void inputValidator() {
        SvcLogicContext ctx = new SvcLogicContext();
        try {
            
            logger.info(" *******************************  inputValidator  ***************************");
            String serviceTemplateContent = FileUtils.readFileToString(
                    new File("src/test/resources/service_templates/resource_assignment.json"),
                    Charset.defaultCharset());
            
            String inputcontent = FileUtils.readFileToString(
                    new File("src/test/resources/service_templates/input/inputValidateTest.json"),
                    Charset.defaultCharset());
            
            Map<String, String> context = new HashMap<>();
            configModelService.prepareContext(context, inputcontent, serviceTemplateContent);
            
            // TransformationUtils.printMap(context);
            
            context.forEach((name, value) -> {
                if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)) {
                    ctx.setAttribute(name, value);
                }
            });
            
            ComponentNodeDelegate componentNodeDelegate = new ComponentNodeDelegate(componentNodeService);
            Map<String, String> inParams = new HashMap<>();
            inParams.put(ConfigModelConstant.PROPERTY_SELECTOR, "resource-assignment");
            componentNodeDelegate.process(inParams, ctx);
            Assert.fail();
        } catch (Exception e) {
            logger.error("Failed in inputValidator" + e.getMessage());
            logger.info("** ctx.getAttribute Check for **" + ctx.getAttribute("resource-assignment.error-message"));
        }
        
    }
    
}

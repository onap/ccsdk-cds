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
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.config.assignment.data.ResourceAssignmentData;
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.generator.service.ConfigGeneratorService;
import org.onap.ccsdk.config.generator.service.ConfigGeneratorServiceImpl;
import org.onap.ccsdk.config.model.service.ConfigModelService;
import org.onap.ccsdk.config.model.service.ConfigModelServiceImpl;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

@RunWith(MockitoJUnitRunner.class)
public class ConfigPreviewServiceTest {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigPreviewServiceTest.class);
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Mock
    private ConfigResourceService configResourceService;
    
    @Mock
    private ConfigRestAdaptorService configRestAdaptorService;
    
    private ConfigModelService configModelService;
    private ConfigGeneratorService configGeneratorService;
    
    @SuppressWarnings("unchecked")
    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testGenerateTemplateResourceMash() throws Exception {
        
        ConfigResourceAssignmentTestUtils.injectTransactionLogSaveMock(configResourceService);
        
        ConfigResourceAssignmentTestUtils.injectConfigModelMock(configRestAdaptorService, "resource_assignment");
        
        ConfigResourceAssignmentTestUtils.injectResourceDictionaryMock(configRestAdaptorService,
                "assignments/empty-dictionary.json");
        
        ConfigResource configResourceQuery = new ConfigResource();
        configResourceQuery.setServiceTemplateVersion("sample-serviceTemplateName");
        configResourceQuery.setServiceTemplateVersion("1.0.0");
        configResourceQuery.setRecipeName("sample-action");
        configResourceQuery.setResourceId("123-resourceId");
        configResourceQuery.setResourceType("sample-resourceType");
        configResourceQuery.setTemplateName("base-config-template");
        String inputContent = FileUtils.readFileToString(
                new File("src/test/resources/service_templates/input/input.json"), Charset.defaultCharset());
        configResourceQuery.setResourceData(inputContent);
        
        ConfigResourceAssignmentTestUtils.injectGetConfigResourceMock(configResourceService, configResourceQuery);
        
        configModelService = new ConfigModelServiceImpl(configRestAdaptorService);
        configGeneratorService = new ConfigGeneratorServiceImpl(configResourceService);
        
        ConfigPreviewService configPreviewService =
                new ConfigPreviewService(configResourceService, configModelService, configGeneratorService);
        
        ResourceAssignmentData resourceAssignmentData = new ResourceAssignmentData();
        resourceAssignmentData.setResourceId("123-resourceId");
        resourceAssignmentData.setResourceType("sample-resourceType");
        resourceAssignmentData.setServiceTemplateName("sample-serviceTemplateName");
        resourceAssignmentData.setServiceTemplateVersion("1.0.0");
        resourceAssignmentData.setActionName("sample-action");
        
        resourceAssignmentData = configPreviewService.generateTemplateResourceMash(resourceAssignmentData);
        
        Assert.assertNotNull("Failed to get GenerateTemplateResourceMash response.", resourceAssignmentData);
        Assert.assertNotNull("Failed to get template mashed contents.",
                resourceAssignmentData.getTemplatesMashedContents());
        
        Assert.assertNotNull("Failed to get base-config template mashed contents.",
                resourceAssignmentData.getTemplatesMashedContents().get("base-config-template"));
    }
}

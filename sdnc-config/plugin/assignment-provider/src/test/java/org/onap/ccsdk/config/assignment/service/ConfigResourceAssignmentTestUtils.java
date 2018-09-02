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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.data.dict.ResourceDefinition;
import org.onap.ccsdk.config.model.domain.ConfigModel;
import org.onap.ccsdk.config.model.domain.ConfigModelContent;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigResourceAssignmentTestUtils {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigResourceAssignmentTestUtils.class);
    
    public static void injectTransactionLogSaveMock(ConfigResourceService configResourceService) throws Exception {
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
    }
    
    public static void injectConfigModelMock(ConfigRestAdaptorService configRestAdaptorService,
            String serviceTemplateName) throws Exception {
        
        Mockito.doAnswer(new Answer<ConfigModel>() {
            @Override
            public org.onap.ccsdk.config.model.domain.ConfigModel answer(InvocationOnMock invocationOnMock)
                    throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                org.onap.ccsdk.config.model.domain.ConfigModel serviceArtifact = null;
                if (args != null && args.length == 3) {
                    
                    logger.info("Artifact info " + Arrays.asList(args));
                    String modelContent = IOUtils.toString(
                            ConfigResourceAssignmentTestUtils.class.getClassLoader().getResourceAsStream(
                                    "service_templates/" + serviceTemplateName + "/" + serviceTemplateName + ".json"),
                            Charset.defaultCharset());
                    
                    ConfigModelContent configModelContent = new ConfigModelContent();
                    configModelContent.setContent(modelContent);
                    configModelContent.setContentType(ConfigModelConstant.MODEL_CONTENT_TYPE_TOSCA_JSON);
                    
                    List<ConfigModelContent> configModelContents = new ArrayList<>();
                    configModelContents.add(configModelContent);
                    
                    String velocityDir = ConfigResourceAssignmentTestUtils.class.getClassLoader()
                            .getResource("service_templates/" + serviceTemplateName + "/velocity").getPath();
                    
                    Collection<File> templateFiles =
                            FileUtils.listFiles(new File(velocityDir), new String[] {"vtl"}, true);
                    logger.info("Template Files info " + templateFiles);
                    for (File templateFile : templateFiles) {
                        String templateContent = FileUtils.readFileToString(templateFile, Charset.defaultCharset());
                        ConfigModelContent configModelTemplateContent = new ConfigModelContent();
                        configModelTemplateContent.setContent(templateContent);
                        configModelTemplateContent.setName(FilenameUtils.getBaseName(templateFile.getName()));
                        configModelTemplateContent.setContentType(ConfigModelConstant.MODEL_CONTENT_TYPE_TEMPLATE);
                        configModelContents.add(configModelTemplateContent);
                    }
                    
                    serviceArtifact = new org.onap.ccsdk.config.model.domain.ConfigModel();
                    serviceArtifact.setArtifactName(String.valueOf(args[0]));
                    serviceArtifact.setArtifactVersion(String.valueOf(args[1]));
                    serviceArtifact.setPublished("Y");
                    serviceArtifact.setConfigModelContents(configModelContents);
                }
                
                return serviceArtifact;
            }
        }).when(configRestAdaptorService).getResource(anyString(), anyString(), Matchers.any(Class.class));
    }
    
    public static void injectResourceDictionaryMock(ConfigRestAdaptorService configRestAdaptorService,
            String dictionaryFileName) throws Exception {
        
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                String dictionaties = "[]";
                if (args != null) {
                    logger.info("getResourceDictionaryByNames " + Arrays.asList(args));
                    dictionaties = IOUtils.toString(
                            ConfigAssignmentNodeTest.class.getClassLoader().getResourceAsStream(dictionaryFileName),
                            Charset.defaultCharset());
                }
                return dictionaties;
            }
        }).when(configRestAdaptorService).postResource(Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any(Class.class));
    }
    
    public static void injectConfigResourceSaveMock(ConfigResourceService configResourceService) throws Exception {
        Mockito.doAnswer(new Answer<ConfigResource>() {
            @Override
            public ConfigResource answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                ConfigResource configResource = null;
                if (args != null) {
                    configResource = (ConfigResource) args[0];
                    logger.info("Config Resource Save info " + configResource);
                    return configResource;
                }
                return configResource;
            }
        }).when(configResourceService).saveConfigResource(any(ConfigResource.class));
    }
    
    public static void injectGetConfigResourceMock(ConfigResourceService configResourceService,
            ConfigResource configResource) throws Exception {
        Mockito.doAnswer(new Answer<List<ConfigResource>>() {
            @Override
            public List<ConfigResource> answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                List<ConfigResource> configResources = new ArrayList<>();
                if (args != null) {
                    configResources.add(configResource);
                }
                return configResources;
            }
        }).when(configResourceService).getConfigResource(any(ConfigResource.class));
    }
    
    public static String getFileContent(String filePath) throws Exception {
        return IOUtils.toString(ConfigResourceAssignmentTestUtils.class.getClassLoader().getResourceAsStream(filePath),
                Charset.defaultCharset());
    }
    
    public static Map<String, ResourceDefinition> getMapfromJson(String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content, new TypeReference<Map<String, ResourceDefinition>>() {});
        } catch (Exception e) {
            logger.info("getMapfromJson Exception ({})", e);
        }
        return null;
    }
}

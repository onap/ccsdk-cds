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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.assignment.data.ResourceAssignmentData;
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.generator.data.ConfigGeneratorInfo;
import org.onap.ccsdk.config.generator.service.ConfigGeneratorService;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.service.ConfigModelService;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ConfigPreviewService {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigAssignmentPersistService.class);
    private ConfigResourceService configResourceService;
    private ConfigModelService configModelService;
    private ConfigGeneratorService configGeneratorService;
    
    public ConfigPreviewService(ConfigResourceService configResourceService, ConfigModelService configModelService,
            ConfigGeneratorService configGeneratorService) {
        this.configResourceService = configResourceService;
        this.configModelService = configModelService;
        this.configGeneratorService = configGeneratorService;
    }
    
    public String generatePreview(String templateContent, String templateData) throws SvcLogicException {
        String mashedData = "";
        ConfigGeneratorInfo configGeneratorInfo =
                configGeneratorService.generateConfiguration(templateContent, templateData);
        if (configGeneratorInfo != null) {
            mashedData = configGeneratorInfo.getMashedData();
        }
        return mashedData;
    }
    
    public ResourceAssignmentData generateTemplateResourceMash(ResourceAssignmentData resourceAssignmentData)
            throws SvcLogicException {
        if (resourceAssignmentData == null) {
            throw new SvcLogicException("Resource assignment data is missing");
        }
        if (StringUtils.isBlank(resourceAssignmentData.getServiceTemplateName())) {
            throw new SvcLogicException("Service template name is missing");
        }
        if (StringUtils.isBlank(resourceAssignmentData.getServiceTemplateVersion())) {
            throw new SvcLogicException("Service template version is missing");
        }
        if (StringUtils.isBlank(resourceAssignmentData.getResourceType())) {
            throw new SvcLogicException("Resource type is missing");
        }
        if (StringUtils.isBlank(resourceAssignmentData.getResourceId())) {
            throw new SvcLogicException("Resource Id is missing");
        }
        if (StringUtils.isBlank(resourceAssignmentData.getActionName())) {
            throw new SvcLogicException("Action name is missing");
        }
        
        String serviceTemplateName = resourceAssignmentData.getServiceTemplateName();
        String serviceTemplateVersion = resourceAssignmentData.getServiceTemplateVersion();
        String actionName = resourceAssignmentData.getActionName();
        String resourceId = resourceAssignmentData.getResourceId();
        String resourceType = resourceAssignmentData.getResourceType();
        String inputData = "{}";
        
        Map<String, String> context = new HashMap<>();
        context.put(ConfigModelConstant.PROPERTY_ACTION_NAME, actionName);
        context = configModelService.prepareContext(context, inputData, serviceTemplateName, serviceTemplateVersion);
        
        ConfigResource configResourceQuery = new ConfigResource();
        configResourceQuery.setServiceTemplateVersion(serviceTemplateName);
        configResourceQuery.setServiceTemplateVersion(serviceTemplateVersion);
        configResourceQuery.setRecipeName(actionName);
        configResourceQuery.setResourceId(resourceId);
        configResourceQuery.setResourceType(resourceType);
        
        List<ConfigResource> configResources = configResourceService.getConfigResource(configResourceQuery);
        if (CollectionUtils.isNotEmpty(configResources)) {
            for (ConfigResource cr : configResources) {
                String templateContent = context
                        .get(ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + cr.getTemplateName() + ".content");
                String templateData = cr.getResourceData();
                String previewContent = generatePreview(templateContent, templateData);
                resourceAssignmentData.getTemplatesMashedContents().put(cr.getTemplateName(), previewContent);
                logger.info("Preview generated for template name ({}) ", cr.getTemplateName());
                logger.trace("Preview generated for preview ({}) ", previewContent);
            }
        } else {
            logger.info(
                    "Couldn't get config resource for service template name ({}) service template version ({})"
                            + " action ({}) resource id ({}) resource type ({})",
                    serviceTemplateName, serviceTemplateVersion, actionName, resourceId, resourceType);
        }
        return resourceAssignmentData;
        
    }
    
}

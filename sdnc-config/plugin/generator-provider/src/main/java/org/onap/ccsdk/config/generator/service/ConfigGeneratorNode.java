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

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.generator.ConfigGeneratorConstant;
import org.onap.ccsdk.config.generator.data.ConfigGeneratorInfo;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.service.ComponentNode;
import org.onap.ccsdk.config.model.service.ConfigModelService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ConfigGeneratorNode implements ComponentNode {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigGeneratorNode.class);
    
    private ConfigModelService configModelService;
    private ConfigResourceService configResourceService;
    private ConfigGeneratorService configGeneratorService;
    
    public ConfigGeneratorNode(ConfigResourceService configResourceService, ConfigModelService configModelService) {
        this.configResourceService = configResourceService;
        this.configModelService = configModelService;
        this.configGeneratorService = new ConfigGeneratorServiceImpl(this.configResourceService);
    }
    
    @Override
    public Boolean preCondition(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        logger.trace("Received generateConfiguration preCondition call with params : ({})", inParams);
        return Boolean.TRUE;
    }
    
    @Override
    public void preProcess(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        logger.trace("Received generateConfiguration preProcess call with params : ({})", inParams);
    }
    
    @Override
    public void process(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        logger.trace("Received generateConfiguration process call with params : ({})", inParams);
    }
    
    @SuppressWarnings("squid:S3776")
    @Override
    public void process(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        logger.trace("Received generateConfiguration process with params : ({})", inParams);
        String prifix = inParams.get(ConfigModelConstant.PROPERTY_SELECTOR);
        try {
            prifix = StringUtils.isNotBlank(prifix) ? (prifix + ".") : "";
            
            String templateContent = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_CONTENT);
            String templateData = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA);
            String requestId = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_REQUEST_ID);
            String resourceId = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESOURCE_ID);
            String resourceType = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESOURCE_TYPE);
            String recipeName = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_ACTION_NAME);
            String templateName = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_NAME);
            
            ConfigGeneratorInfo configGeneratorInfo = null;
            if (StringUtils.isNotBlank(templateContent) && StringUtils.isNotBlank(templateData)) {
                configGeneratorInfo = this.configGeneratorService.generateConfiguration(templateContent, templateData);
            } else {
                if (StringUtils.isBlank(requestId)) {
                    throw new SvcLogicException("Config Generator Request Id is missing.");
                }
                if (StringUtils.isBlank(resourceId)) {
                    throw new SvcLogicException("Config Generator Resource Id is missing.");
                }
                if (StringUtils.isBlank(resourceType)) {
                    throw new SvcLogicException("Config Generator Resource Type is missing.");
                }
                if (StringUtils.isBlank(recipeName)) {
                    throw new SvcLogicException("Config Generator Action Name is missing.");
                }
                if (StringUtils.isBlank(templateName)) {
                    throw new SvcLogicException("Config Generator Template Name Id is missing.");
                }
                
                templateContent = configModelService.getNodeTemplateContent(ctx, templateName);
                
                if (StringUtils.isBlank(templateContent)) {
                    throw new SvcLogicException(
                            "Failed to get the Template Content for the Temaple Name :" + templateName);
                }
                
                configGeneratorInfo = new ConfigGeneratorInfo();
                configGeneratorInfo.setRequestId(requestId);
                configGeneratorInfo.setResourceId(resourceId);
                configGeneratorInfo.setResourceType(resourceType);
                configGeneratorInfo.setRecipeName(recipeName);
                configGeneratorInfo.setTemplateName(templateName);
                configGeneratorInfo.setTemplateContent(templateContent);
                
                this.configGeneratorService.generateConfiguration(configGeneratorInfo);
            }
            if (configGeneratorInfo != null) {
                ctx.setAttribute(prifix + ConfigGeneratorConstant.OUTPUT_PARAM_GENERATED_CONFIG,
                        configGeneratorInfo.getMashedData());
                if (StringUtils.isNotBlank(configGeneratorInfo.getMaskData())) {
                    ctx.setAttribute(prifix + ConfigGeneratorConstant.OUTPUT_PARAM_MASK_INFO,
                            configGeneratorInfo.getMaskData());
                }
            }
            ctx.setAttribute(prifix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                    ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
        } catch (Exception e) {
            ctx.setAttribute(prifix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                    ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(prifix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            logger.error("Failed in generateConfiguration ({})", e);
            throw new SvcLogicException(e.getMessage());
        }
    }
    
    @Override
    public void postProcess(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        logger.info("Received generateConfiguration postProcess with params : ({})", inParams);
    }
    
}

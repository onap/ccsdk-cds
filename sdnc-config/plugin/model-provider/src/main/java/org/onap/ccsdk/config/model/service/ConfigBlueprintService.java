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

package org.onap.ccsdk.config.model.service;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.domain.ConfigModel;
import org.onap.ccsdk.config.model.domain.ConfigModelContent;
import org.onap.ccsdk.config.model.utils.PrepareContextUtils;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorConstants;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorException;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ConfigBlueprintService {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigBlueprintService.class);
    
    private final ConfigRestAdaptorService configRestAdaptorService;
    
    public ConfigBlueprintService(ConfigRestAdaptorService configRestAdaptorService) {
        this.configRestAdaptorService = configRestAdaptorService;
    }
    
    public Map<String, String> prepareContext(Map<String, String> context, String input, String serviceTemplateName,
            String serviceTemplateVersion) throws SvcLogicException {
        try {
            PrepareContextUtils prepareContextUtils = new PrepareContextUtils();
            String serviceTemplateContent = getServiceModel(context, serviceTemplateName, serviceTemplateVersion);
            
            if (StringUtils.isBlank(serviceTemplateContent)) {
                throw new SvcLogicException(String.format("Failed to get the Service Template (%s), version (%s)",
                        serviceTemplateName, serviceTemplateVersion));
            }
            
            return prepareContextUtils.prepareContext(context, input, serviceTemplateContent);
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("squid:S3776")
    private String getServiceModel(Map<String, String> context, String serviceTemplateName,
            String serviceTemplateVersion) throws SvcLogicException, ConfigRestAdaptorException {
        String content = null;
        
        logger.info("Getting service template ({}) of version ({}) ", serviceTemplateName, serviceTemplateVersion);
        
        String path = "configmodelbyname/" + serviceTemplateName + "/version/" + serviceTemplateVersion;
        
        ConfigModel configModel = configRestAdaptorService
                .getResource(ConfigRestAdaptorConstants.SELECTOR_MODEL_SERVICE, path, ConfigModel.class);
        
        if (configModel == null || configModel.getConfigModelContents() == null
                || configModel.getConfigModelContents().isEmpty()) {
            throw new SvcLogicException("Service template model is missing for  service template name ("
                    + serviceTemplateName + "), service template version (" + serviceTemplateVersion + ") ");
        } else {
            if (configModel.getPublished() == null || !configModel.getPublished().equalsIgnoreCase("Y")) {
                throw new SvcLogicException(String.format(
                        "Service template model is not published for service template (%s) ", serviceTemplateName));
            }
            
            List<ConfigModelContent> configModelContents = configModel.getConfigModelContents();
            for (ConfigModelContent configModelContent : configModelContents) {
                if (configModelContent != null) {
                    if (ConfigModelConstant.MODEL_CONTENT_TYPE_TOSCA_JSON.equals(configModelContent.getContentType())) {
                        content = configModelContent.getContent();
                    } else if (ConfigModelConstant.MODEL_CONTENT_TYPE_TEMPLATE
                            .equals(configModelContent.getContentType())) {
                        context.put(ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + configModelContent.getName()
                                + ".content", configModelContent.getContent());
                    }
                }
            }
            logger.trace("Service model data : {} ", content);
        }
        return content;
    }
    
}

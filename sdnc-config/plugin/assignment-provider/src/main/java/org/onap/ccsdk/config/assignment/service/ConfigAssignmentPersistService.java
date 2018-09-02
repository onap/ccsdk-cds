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

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.data.adaptor.DataAdaptorConstants;
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.config.data.adaptor.domain.ResourceAssignmentData;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.utils.ResourceAssignmentUtils;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ConfigAssignmentPersistService {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigAssignmentPersistService.class);
    
    private ConfigResourceService configResourceService;
    
    public ConfigAssignmentPersistService(ConfigResourceService configResourceService) {
        this.configResourceService = configResourceService;
    }
    
    public void saveResourceMapping(org.onap.ccsdk.config.assignment.data.ResourceAssignmentData resourceAssignmentData,
            String templateName, List<ResourceAssignment> resourceAssignments) throws SvcLogicException {
        try {
            
            if (resourceAssignmentData == null) {
                throw new SvcLogicException("Resource assignment data is missing");
            }
            
            if (StringUtils.isBlank(resourceAssignmentData.getRequestId())) {
                logger.warn("Request Id ({}) is missing, may be getting request for resource update.",
                        resourceAssignmentData.getRequestId());
            }
            
            if (StringUtils.isBlank(resourceAssignmentData.getResourceId())) {
                throw new SvcLogicException("Resource Id is missing");
            }
            
            if (StringUtils.isBlank(resourceAssignmentData.getResourceType())) {
                throw new SvcLogicException("Resource type is missing");
            }
            
            if (StringUtils.isBlank(resourceAssignmentData.getActionName())) {
                throw new SvcLogicException("Action name is missing");
            }
            
            if (StringUtils.isBlank(templateName)) {
                throw new SvcLogicException("template name is missing");
            }
            
            StringBuilder builder = new StringBuilder();
            builder.append("Resource Assignment for Template Name :");
            builder.append(templateName);
            builder.append("\n");
            builder.append(TransformationUtils.getJson(resourceAssignments, true));
            
            configResourceService.save(new TransactionLog(resourceAssignmentData.getRequestId(),
                    DataAdaptorConstants.LOG_MESSAGE_TYPE_LOG, builder.toString()));
            
            // Resource Data should be Regenerated based on the new Updates
            String resourceData = ResourceAssignmentUtils.generateResourceDataForAssignments(resourceAssignments);
            
            List<ResourceAssignmentData> resourceAssignmentDataList =
                    ConfigAssignmentUtils.convertResoureAssignmentList(resourceAssignments);
            
            ConfigResource configResource = new ConfigResource();
            configResource.setRequestId(resourceAssignmentData.getRequestId());
            configResource.setServiceTemplateName(resourceAssignmentData.getServiceTemplateName());
            configResource.setServiceTemplateVersion(resourceAssignmentData.getServiceTemplateVersion());
            configResource.setRecipeName(resourceAssignmentData.getActionName());
            configResource.setResourceId(resourceAssignmentData.getResourceId());
            configResource.setResourceType(resourceAssignmentData.getResourceType());
            configResource.setResourceData(resourceData);
            configResource.setTemplateName(templateName);
            configResource.setStatus(ConfigModelConstant.STATUS_SUCCESS);
            configResource.setUpdatedBy(ConfigModelConstant.USER_SYSTEM);
            
            if (CollectionUtils.isNotEmpty(resourceAssignmentDataList)) {
                configResource.setResourceAssignments(resourceAssignmentDataList);
            }
            configResource = configResourceService.saveConfigResource(configResource);
            logger.info("Resource data saved successfully for the template ({}) with resource id ({})", templateName,
                    configResource.getResourceId());
            
            builder = new StringBuilder();
            builder.append("Resource Data Template Name :");
            builder.append(templateName);
            builder.append("\n");
            builder.append(resourceData);
            configResourceService.save(new TransactionLog(resourceAssignmentData.getRequestId(),
                    DataAdaptorConstants.LOG_MESSAGE_TYPE_LOG, builder.toString()));
            
        } catch (Exception e) {
            throw new SvcLogicException("ConfigAssignmentPersistService : " + e.getMessage(), e);
        }
        
    }
    
}

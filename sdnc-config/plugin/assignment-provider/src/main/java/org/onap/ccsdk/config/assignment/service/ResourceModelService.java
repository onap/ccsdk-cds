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
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.service.ConfigModelService;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.config.model.validator.ResourceAssignmentValidator;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ResourceModelService {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ResourceModelService.class);
    
    private ConfigModelService configModelService;
    
    public ResourceModelService(ConfigModelService configModelService) {
        this.configModelService = configModelService;
    }
    
    public Map<String, String> getTemplatesContents(SvcLogicContext ctx, List<String> templateNames)
            throws SvcLogicException {
        Map<String, String> templatesContents = new HashMap<>();
        try {
            if (CollectionUtils.isNotEmpty(templateNames)) {
                for (String templateName : templateNames) {
                    String templateContent = this.configModelService.getNodeTemplateContent(ctx, templateName);
                    logger.trace("Processing template ({}) with  content : {}", templateName, templateContent);
                    templatesContents.put(templateName, templateContent);
                }
            }
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
        return templatesContents;
    }
    
    public Map<String, List<ResourceAssignment>> getTemplatesResourceAssignments(SvcLogicContext ctx,
            List<String> templateNames) throws SvcLogicException {
        Map<String, List<ResourceAssignment>> templatesResourceAssignments = new HashMap<>();
        try {
            if (CollectionUtils.isNotEmpty(templateNames)) {
                for (String templateName : templateNames) {
                    String resourceMappingContent = this.configModelService.getNodeTemplateMapping(ctx, templateName);
                    logger.info("Processing template ({}) with resource assignment content : {}", templateName,
                            resourceMappingContent);
                    
                    if (StringUtils.isNotBlank(resourceMappingContent)) {
                        
                        List<ResourceAssignment> resourceAssignments =
                                TransformationUtils.getListfromJson(resourceMappingContent, ResourceAssignment.class);
                        
                        if (resourceAssignments != null) {
                            ResourceAssignmentValidator resourceAssignmentValidator =
                                    new ResourceAssignmentValidator(resourceAssignments);
                            resourceAssignmentValidator.validateResourceAssignment();
                            logger.info("Resource assignment validated successfully for the template ({})",
                                    templateName);
                            templatesResourceAssignments.put(templateName, resourceAssignments);
                        } else {
                            throw new SvcLogicException(String.format(
                                    "Failed to convert assignment content (%s) to object", resourceMappingContent));
                        }
                    } else {
                        // Do nothing, because som e templates may not have mappings
                    }
                }
            }
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
        
        return templatesResourceAssignments;
    }
}

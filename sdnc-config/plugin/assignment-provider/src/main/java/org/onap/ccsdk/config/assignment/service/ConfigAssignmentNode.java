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
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.assignment.ConfigAssignmentConstants;
import org.onap.ccsdk.config.assignment.data.ResourceAssignmentData;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.generator.service.ConfigGeneratorService;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.service.ComponentNode;
import org.onap.ccsdk.config.model.service.ComponentNodeService;
import org.onap.ccsdk.config.model.service.ConfigModelService;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ConfigAssignmentNode implements ComponentNode {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigAssignmentNode.class);
    
    private ComponentNodeService componentNodeService;
    private ConfigResourceService configResourceService;
    private ConfigModelService configModelService;
    private ConfigRestAdaptorService configRestAdaptorService;
    private ConfigGeneratorService configGeneratorService;
    
    public ConfigAssignmentNode(ConfigResourceService configResourceService,
            ConfigRestAdaptorService configRestAdaptorService, ConfigModelService configModelService,
            ComponentNodeService componentNodeService, ConfigGeneratorService configGeneratorService) {
        logger.info("{} Constrctor Initiated", "ConfigAssignmentNode");
        this.componentNodeService = componentNodeService;
        this.configResourceService = configResourceService;
        this.configModelService = configModelService;
        this.configRestAdaptorService = configRestAdaptorService;
        this.configGeneratorService = configGeneratorService;
    }
    
    @Override
    public Boolean preCondition(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        return Boolean.TRUE;
    }
    
    @Override
    public void preProcess(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        // Auto-generated method stub
    }
    
    /**
     * This method is used to resolve the resources defined in the template. Generic Resource API DG
     * calls this execute node.
     *
     * @param inParams This is the input parameter to process this node
     * 
     *        <pre>
    request-id                  (string):           Tracking Id 
    resource-type               (string):           Resource Type ( ex : vnf-type) 
    resource-id                 (string):           Resource Id 
    service-template-name       (string):           Blueprint Name 
    service-template-version    (string):           Blueprint Version 
    action-name                 (string): 
    template-names              (List of string):   Template Names / Artifact Node Names to resolve. ["template1", "template2"] 
    input-data                  (string):           Input Data in JSON String, for the substitution in the Template. 
    prifix                      (string):           Return Value selector
     *        </pre>
     * 
     * @param ctx This is the service logger context, Output will be stored (
     *        <responsePrefix>.resource-assignment-params.<template-name> : Output Data in JSON String.
     *        <responsePrefix>.status <responsePrefix>.error-message )
     * @throws SvcLogicException On processing error.
     */
    
    @Override
    public void process(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        
        final String responsePrefix = StringUtils.isNotBlank(inParams.get(ConfigModelConstant.PROPERTY_SELECTOR))
                ? (inParams.get(ConfigModelConstant.PROPERTY_SELECTOR) + ".")
                : "";
        try {
            
            ResourceAssignmentData resourceAssignmentData = populateResourceData(inParams);
            resourceAssignmentData.setSvcLogicContext(ctx);
            Map<String, Object> componentContext = new HashMap<>();
            resourceAssignmentData.setContext(componentContext);
            resourceAssignmentData.setReloadModel(true);
            
            ConfigAssignmentProcessService configAssignmentProcessService =
                    new ConfigAssignmentProcessService(configResourceService, configRestAdaptorService,
                            configModelService, componentNodeService, configGeneratorService);
            configAssignmentProcessService.resolveResources(resourceAssignmentData);
            
            if (MapUtils.isNotEmpty(resourceAssignmentData.getTemplatesMashedContents())) {
                resourceAssignmentData.getTemplatesMashedContents().forEach((templateName, previewContent) -> {
                    logger.debug("For Template name : ({}),\n Preview Content is : ({})", templateName, previewContent);
                    ctx.setAttribute(
                            responsePrefix + ConfigAssignmentConstants.OUTPUT_PARAM_MASHED_DATA + "." + templateName,
                            previewContent);
                });
            }
            ctx.setAttribute(responsePrefix + ConfigAssignmentConstants.OUTPUT_PARAM_STATUS,
                    ConfigAssignmentConstants.OUTPUT_STATUS_SUCCESS);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigAssignmentConstants.OUTPUT_PARAM_STATUS,
                    ConfigAssignmentConstants.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigAssignmentConstants.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            throw new SvcLogicException(e.getMessage(), e);
        }
    }
    
    @Override
    public void process(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        
        final String responsePrefix = StringUtils.isNotBlank(inParams.get(ConfigModelConstant.PROPERTY_SELECTOR))
                ? (inParams.get(ConfigModelConstant.PROPERTY_SELECTOR) + ".")
                : "";
        try {
            
            ResourceAssignmentData resourceAssignmentData = populateResourceData(inParams);
            resourceAssignmentData.setSvcLogicContext(ctx);
            resourceAssignmentData.setContext(componentContext);
            resourceAssignmentData.setReloadModel(false);
            
            ConfigAssignmentProcessService configAssignmentProcessService =
                    new ConfigAssignmentProcessService(configResourceService, configRestAdaptorService,
                            configModelService, componentNodeService, configGeneratorService);
            configAssignmentProcessService.resolveResources(resourceAssignmentData);
            ctx.setAttribute(responsePrefix + ConfigAssignmentConstants.OUTPUT_PARAM_STATUS,
                    ConfigAssignmentConstants.OUTPUT_STATUS_SUCCESS);
            
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigAssignmentConstants.OUTPUT_PARAM_STATUS,
                    ConfigAssignmentConstants.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigAssignmentConstants.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            throw new SvcLogicException(e.getMessage(), e);
        }
    }
    
    @Override
    public void postProcess(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        // Do Nothing
    }
    
    private ResourceAssignmentData populateResourceData(Map<String, String> inParams) throws SvcLogicException {
        validateInputParams(inParams);
        
        String requestId = inParams.get(ConfigAssignmentConstants.INPUT_PARAM_REQUEST_ID);
        String resourceId = inParams.get(ConfigAssignmentConstants.INPUT_PARAM_RESOURCE_ID);
        String resourceType = inParams.get(ConfigAssignmentConstants.INPUT_PARAM_RESOURCE_TYPE);
        String serviceTemplateName = inParams.get(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_NAME);
        String serviceTemplateVersion = inParams.get(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_VERSION);
        String actionName = inParams.get(ConfigModelConstant.PROPERTY_ACTION_NAME);
        String inputData = inParams.get(ConfigAssignmentConstants.INPUT_PARAM_INPUT_DATA);
        
        String templateNamesStr = inParams.get(ConfigAssignmentConstants.INPUT_PARAM_TEMPLATE_NAMES);
        List<String> templateNames = TransformationUtils.getListfromJson(templateNamesStr, String.class);
        
        ResourceAssignmentData resourceAssignmentData = new ResourceAssignmentData();
        resourceAssignmentData.setRequestId(requestId);
        resourceAssignmentData.setResourceId(resourceId);
        resourceAssignmentData.setResourceType(resourceType);
        resourceAssignmentData.setServiceTemplateName(serviceTemplateName);
        resourceAssignmentData.setServiceTemplateVersion(serviceTemplateVersion);
        resourceAssignmentData.setActionName(actionName);
        resourceAssignmentData.setInputData(inputData);
        resourceAssignmentData.setTemplateNames(templateNames);
        
        return resourceAssignmentData;
    }
    
    private void validateInputParams(Map<String, String> inParams) throws SvcLogicException {
        if (inParams == null) {
            throw new SvcLogicException("Input parameters missing");
        }
        
        String requestId = inParams.get(ConfigAssignmentConstants.INPUT_PARAM_REQUEST_ID);
        if (StringUtils.isBlank(requestId)) {
            throw new SvcLogicException("Request id parameters missing");
        }
        String resourceId = inParams.get(ConfigAssignmentConstants.INPUT_PARAM_RESOURCE_ID);
        if (StringUtils.isBlank(resourceId)) {
            throw new SvcLogicException("Resource id parameter is missing");
        }
        String resourceType = inParams.get(ConfigAssignmentConstants.INPUT_PARAM_RESOURCE_TYPE);
        if (StringUtils.isBlank(resourceType)) {
            throw new SvcLogicException("Resource type parameter is missing");
        }
        String recipeName = inParams.get(ConfigAssignmentConstants.INPUT_PARAM_ACTION_NAME);
        if (StringUtils.isBlank(recipeName)) {
            throw new SvcLogicException("Action name is parameter is missing");
        }
        String templateNames = inParams.get(ConfigAssignmentConstants.INPUT_PARAM_TEMPLATE_NAMES);
        if (StringUtils.isBlank(templateNames)) {
            throw new SvcLogicException("Template names parameter missing");
        }
        String serviceTemplateName = inParams.get(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_NAME);
        if (StringUtils.isBlank(serviceTemplateName)) {
            throw new SvcLogicException("Service Template name parameter missing");
        }
        String serviceTemplateVersion = inParams.get(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_VERSION);
        if (StringUtils.isBlank(serviceTemplateVersion)) {
            throw new SvcLogicException("Service Template version parameter missing");
        }
        
    }
    
}

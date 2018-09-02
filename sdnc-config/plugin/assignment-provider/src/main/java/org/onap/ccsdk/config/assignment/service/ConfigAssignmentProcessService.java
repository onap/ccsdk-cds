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
import org.onap.ccsdk.config.assignment.ConfigAssignmentConstants;
import org.onap.ccsdk.config.assignment.data.ResourceAssignmentData;
import org.onap.ccsdk.config.assignment.processor.ProcessorFactory;
import org.onap.ccsdk.config.assignment.processor.ResourceAssignmentProcessor;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.generator.service.ConfigGeneratorService;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.data.dict.ResourceDefinition;
import org.onap.ccsdk.config.model.service.ComponentNode;
import org.onap.ccsdk.config.model.service.ComponentNodeService;
import org.onap.ccsdk.config.model.service.ConfigModelService;
import org.onap.ccsdk.config.model.utils.ResourceAssignmentUtils;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ConfigAssignmentProcessService {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigAssignmentProcessService.class);
    
    private ComponentNodeService componentNodeService;
    private ConfigResourceService configResourceService;
    private ConfigModelService configModelService;
    private ConfigRestAdaptorService configRestAdaptorService;
    private ConfigGeneratorService configGeneratorService;
    
    public ConfigAssignmentProcessService(ConfigResourceService configResourceService,
            ConfigRestAdaptorService configRestAdaptorService, ConfigModelService configModelService,
            ComponentNodeService componentNodeService, ConfigGeneratorService configGeneratorService) {
        this.componentNodeService = componentNodeService;
        this.configResourceService = configResourceService;
        this.configModelService = configModelService;
        this.configRestAdaptorService = configRestAdaptorService;
        this.configGeneratorService = configGeneratorService;
    }
    
    @SuppressWarnings("squid:S1141")
    public void resolveResources(ResourceAssignmentData resourceAssignmentData) throws SvcLogicException {
        try {
            validateInputParams(resourceAssignmentData);
            
            String serviceTemplateName = resourceAssignmentData.getServiceTemplateName();
            String serviceTemplateVersion = resourceAssignmentData.getServiceTemplateVersion();
            String actionName = resourceAssignmentData.getActionName();
            String inputData = resourceAssignmentData.getInputData();
            SvcLogicContext svcLogicContext = resourceAssignmentData.getSvcLogicContext();
            List<String> templateNames = resourceAssignmentData.getTemplateNames();
            
            if (resourceAssignmentData.isReloadModel()) {
                Map<String, String> context = new HashMap<>();
                context.put(ConfigModelConstant.PROPERTY_ACTION_NAME, actionName);
                context = configModelService.prepareContext(context, inputData, serviceTemplateName,
                        serviceTemplateVersion);
                context.forEach((key, value) -> svcLogicContext.setAttribute(key, value));
                logger.info("List of Resources provided in input: {}", svcLogicContext.toProperties());
            }
            
            Map<String, Object> componentContext = resourceAssignmentData.getContext();
            
            if (CollectionUtils.isNotEmpty(templateNames)) {
                // Get the Resource Assignments for templates and Validate the mappings
                ResourceModelService resourceModelService = new ResourceModelService(configModelService);
                
                // Get the Resource Assignment
                Map<String, List<ResourceAssignment>> templatesResourceAssignments =
                        resourceModelService.getTemplatesResourceAssignments(svcLogicContext, templateNames);
                
                // Get the Template Contents
                Map<String, String> templatesContents =
                        resourceModelService.getTemplatesContents(svcLogicContext, templateNames);
                
                // Process each template
                for (String templateName : templateNames) {
                    List<ResourceAssignment> resourceAssignments = templatesResourceAssignments.get(templateName);
                    String templateContent = templatesContents.get(templateName);
                    if (resourceAssignments != null) {
                        String templateData = null;
                        try {
                            // Populate the Dictionary
                            ResourceDictionaryService resourceDictionaryService =
                                    new ResourceDictionaryService(configRestAdaptorService);
                            Map<String, ResourceDefinition> dictionaries =
                                    resourceDictionaryService.getDataDictionaryDefinitions(resourceAssignments);
                            
                            processResourceAssignments(resourceAssignmentData, svcLogicContext, componentContext,
                                    templateName, resourceAssignments, dictionaries);
                            
                            logger.info("decrypting config data for templateName {}", templateName);
                            templateData =
                                    ResourceAssignmentUtils.generateResourceDataForAssignments(resourceAssignments);
                        } finally {
                            saveResourceMapping(resourceAssignmentData, templateName, resourceAssignments);
                        }
                        
                        logger.info("generating config preview for templateName {}", templateName);
                        ConfigPreviewService configPreviewService = new ConfigPreviewService(configResourceService,
                                configModelService, configGeneratorService);
                        String mashedData = configPreviewService.generatePreview(templateContent, templateData);
                        resourceAssignmentData.getTemplatesMashedContents().put(templateName, mashedData);
                        resourceAssignmentData.getTemplatesData().put(templateName, templateData);
                        
                    } else {
                        // Do nothing for Mapping not found
                        logger.warn("No resource Assignment mappings to resolve for templateName {}", templateName);
                    }
                }
            }
            
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage(), e);
        }
    }
    
    private void processResourceAssignments(ResourceAssignmentData resourceAssignmentData, SvcLogicContext ctx,
            Map<String, Object> componentContext, String templateName, List<ResourceAssignment> resourceAssignments,
            Map<String, ResourceDefinition> dictionaries) throws SvcLogicException {
        
        String recipeName = resourceAssignmentData.getActionName();
        
        ResourceAssignmentProcessor resourceAssignmentProcessor =
                new ResourceAssignmentProcessor(resourceAssignments, ctx);
        List<List<ResourceAssignment>> sequenceBatchResourceAssignment = resourceAssignmentProcessor.process();
        
        logger.debug("Resource dictionary Info  ({})", dictionaries);
        
        if (sequenceBatchResourceAssignment != null) {
            componentContext.put(ConfigModelConstant.PROPERTY_ACTION_NAME, recipeName);
            componentContext.put(ConfigModelConstant.PROPERTY_TEMPLATE_NAME, templateName);
            componentContext.put(ConfigModelConstant.PROPERTY_DICTIONARIES, dictionaries);
            for (List<ResourceAssignment> batchResourceAssignment : sequenceBatchResourceAssignment) {
                
                processBatchResourceAssignments(resourceAssignmentData, ctx, componentContext, batchResourceAssignment);
                
                logger.debug("Batch Resource data status ({})", TransformationUtils.getJson(batchResourceAssignment));
            }
        }
    }
    
    private void processBatchResourceAssignments(ResourceAssignmentData resourceAssignmentData, SvcLogicContext ctx,
            Map<String, Object> componentContext, List<ResourceAssignment> batchResourceAssignment)
            throws SvcLogicException {
        
        if (CollectionUtils.isNotEmpty(batchResourceAssignment)) {
            
            ResourceAssignment batchFirstResourceAssignment = batchResourceAssignment.get(0);
            if (batchFirstResourceAssignment != null
                    && StringUtils.isNotBlank(batchFirstResourceAssignment.getDictionarySource())) {
                String source = batchFirstResourceAssignment.getDictionarySource();
                // Processing their Source
                logger.info("Processing source ({})  with batch ({}) ", source, batchResourceAssignment);
                componentContext.put(ConfigModelConstant.PROPERTY_RESOURCE_ASSIGNMENTS, batchResourceAssignment);
                
                ProcessorFactory factory =
                        new ProcessorFactory(configResourceService, configRestAdaptorService, componentNodeService);
                
                ComponentNode processor = factory.getInstance(source);
                
                Map<String, String> inParams = new HashMap<>();
                inParams.put(ConfigAssignmentConstants.INPUT_PARAM_REQUEST_ID, resourceAssignmentData.getRequestId());
                inParams.put(ConfigAssignmentConstants.INPUT_PARAM_RESOURCE_ID, resourceAssignmentData.getResourceId());
                inParams.put(ConfigAssignmentConstants.INPUT_PARAM_RESOURCE_TYPE,
                        resourceAssignmentData.getResourceType());
                inParams.put(ConfigAssignmentConstants.INPUT_PARAM_ACTION_NAME, resourceAssignmentData.getActionName());
                inParams.put(ConfigAssignmentConstants.INPUT_PARAM_TEMPLATE_NAMES,
                        resourceAssignmentData.getTemplateNames().toString());
                processor.process(inParams, ctx, componentContext);
            }
        }
    }
    
    private void saveResourceMapping(ResourceAssignmentData resourceAssignmentData, String templateName,
            List<ResourceAssignment> resourceAssignments) throws SvcLogicException {
        if (resourceAssignmentData != null && StringUtils.isNotBlank(templateName)) {
            
            ConfigAssignmentPersistService configAssignmentPersistService =
                    new ConfigAssignmentPersistService(configResourceService);
            configAssignmentPersistService.saveResourceMapping(resourceAssignmentData, templateName,
                    resourceAssignments);
        }
    }
    
    private void validateInputParams(ResourceAssignmentData resourceAssignmentData) throws SvcLogicException {
        if (resourceAssignmentData == null) {
            throw new SvcLogicException("Input parameters missing");
        }
        
        String requestId = resourceAssignmentData.getRequestId();
        if (StringUtils.isBlank(requestId)) {
            throw new SvcLogicException("Request id parameters missing");
        }
        String resourceId = resourceAssignmentData.getResourceId();
        if (StringUtils.isBlank(resourceId)) {
            throw new SvcLogicException("Resource id parameter is missing");
        }
        String resourceType = resourceAssignmentData.getResourceType();
        if (StringUtils.isBlank(resourceType)) {
            throw new SvcLogicException("Resource type parameter is missing");
        }
        String actionName = resourceAssignmentData.getActionName();
        if (StringUtils.isBlank(actionName)) {
            throw new SvcLogicException("Action name is parameter is missing");
        }
        
        List<String> templatesNames = resourceAssignmentData.getTemplateNames();
        if (CollectionUtils.isEmpty(templatesNames)) {
            throw new SvcLogicException("Template names parameter missing");
        }
    }
    
}

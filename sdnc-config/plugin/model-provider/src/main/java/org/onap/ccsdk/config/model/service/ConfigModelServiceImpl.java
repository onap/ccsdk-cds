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
import org.onap.ccsdk.config.model.ConfigModelException;
import org.onap.ccsdk.config.model.data.CapabilityAssignment;
import org.onap.ccsdk.config.model.data.NodeTemplate;
import org.onap.ccsdk.config.model.data.NodeType;
import org.onap.ccsdk.config.model.data.ServiceTemplate;
import org.onap.ccsdk.config.model.utils.NodePropertyUtils;
import org.onap.ccsdk.config.model.utils.PrepareContextUtils;
import org.onap.ccsdk.config.model.utils.ServiceTemplateUtils;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.config.model.validator.ServiceTemplateValidator;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ConfigModelServiceImpl implements ConfigModelService {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigModelServiceImpl.class);
    private static final String CLASS_NAME = "ConfigModelServiceImpl";
    
    private final ConfigRestAdaptorService configRestAdaptorService;
    
    public ConfigModelServiceImpl(ConfigRestAdaptorService configRestAdaptorService) {
        logger.info("{} Constuctor Initated...", CLASS_NAME);
        this.configRestAdaptorService = configRestAdaptorService;
    }
    
    @Override
    public Boolean validateServiceTemplate(ServiceTemplate serviceTemplate) throws SvcLogicException {
        try {
            ServiceTemplateValidator serviceTemplateValidator = new ServiceTemplateValidator();
            return serviceTemplateValidator.validateServiceTemplate(serviceTemplate);
        } catch (ConfigModelException e) {
            throw new SvcLogicException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, String> prepareContext(Map<String, String> context, String input, String serviceTemplateName,
            String serviceTemplateVersion) throws SvcLogicException {
        
        ConfigBlueprintService configBlueprintService = new ConfigBlueprintService(configRestAdaptorService);
        return configBlueprintService.prepareContext(context, input, serviceTemplateName, serviceTemplateVersion);
    }
    
    @Override
    public Map<String, String> prepareContext(Map<String, String> context, String input, String serviceTemplateContent)
            throws SvcLogicException {
        try {
            PrepareContextUtils prepareContextUtils = new PrepareContextUtils();
            return prepareContextUtils.prepareContext(context, input, serviceTemplateContent);
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, String> convertJson2properties(Map<String, String> context, String jsonContent,
            List<String> blockKeys) throws SvcLogicException {
        try {
            return TransformationUtils.convertJson2Properties(context, jsonContent, blockKeys);
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, String> convertServiceTemplate2Properties(String serviceTemplateContent,
            final Map<String, String> context) throws SvcLogicException {
        try {
            ServiceTemplateUtils serviceTemplateUtils = new ServiceTemplateUtils();
            return serviceTemplateUtils.convertServiceTemplate2Properties(serviceTemplateContent, context);
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, String> convertServiceTemplate2Properties(ServiceTemplate serviceTemplate,
            final Map<String, String> context) throws SvcLogicException {
        try {
            ServiceTemplateUtils serviceTemplateUtils = new ServiceTemplateUtils();
            return serviceTemplateUtils.convertServiceTemplate2Properties(serviceTemplate, context);
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
    }
    
    @SuppressWarnings("squid:S3776")
    @Override
    public SvcLogicContext assignInParamsFromModel(final SvcLogicContext context, final Map<String, String> inParams)
            throws SvcLogicException {
        logger.debug("Processing component input param ({})", inParams);
        try {
            if (context != null && inParams != null && inParams.containsKey(ConfigModelConstant.PROPERTY_SELECTOR)) {
                String componentKey = inParams.get(ConfigModelConstant.PROPERTY_SELECTOR);
                if (StringUtils.isNotBlank(componentKey)) {
                    String nodeTemplateContent =
                            context.getAttribute(ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + componentKey);
                    logger.info("Processing node template content ({})", nodeTemplateContent);
                    if (StringUtils.isNotBlank(nodeTemplateContent)) {
                        NodeTemplate nodeTemplate =
                                TransformationUtils.readValue(nodeTemplateContent, NodeTemplate.class);
                        if (nodeTemplate != null && StringUtils.isNotBlank(nodeTemplate.getType())) {
                            String nodeTypeContent = context
                                    .getAttribute(ConfigModelConstant.PROPERTY_NODE_TYPES_DOT + nodeTemplate.getType());
                            NodeType nodetype = TransformationUtils.readValue(nodeTypeContent, NodeType.class);
                            if (nodetype != null) {
                                inParams.put(ConfigModelConstant.PROPERTY_CURRENT_NODETYPE_DERIVED_FROM,
                                        nodetype.getDerivedFrom());
                                NodePropertyUtils nodePropertyUtils = new NodePropertyUtils(context, inParams);
                                nodePropertyUtils.assignInParamsFromModel(nodetype, nodeTemplate);
                            } else {
                                throw new SvcLogicException(
                                        String.format("Failed to get node type (%s) for node template (%s).",
                                                nodeTemplate.getType(), componentKey));
                            }
                            
                        } else {
                            throw new SvcLogicException(String
                                    .format("Failed to convert content (%s) to node template.", nodeTemplateContent));
                        }
                    } else {
                        throw new SvcLogicException(String
                                .format("Couldn't get node template content for component key (%s).", componentKey));
                    }
                } else {
                    throw new SvcLogicException(
                            String.format("Couldn't get component key (prefix) from inparam (%s)", inParams));
                }
            }
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
        return context;
    }
    
    @Override
    public SvcLogicContext assignOutParamsFromModel(final SvcLogicContext context, final Map<String, String> inParams)
            throws SvcLogicException {
        try {
            if (context != null && inParams != null && inParams.containsKey(ConfigModelConstant.PROPERTY_SELECTOR)) {
                String componentKey = inParams.get(ConfigModelConstant.PROPERTY_SELECTOR);
                logger.info("Processing component output for prefix key ({})", componentKey);
                if (StringUtils.isNotBlank(componentKey)) {
                    String nodeTemplateContent =
                            context.getAttribute(ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + componentKey);
                    if (StringUtils.isNotBlank(nodeTemplateContent)) {
                        NodeTemplate nodeTemplate =
                                TransformationUtils.readValue(nodeTemplateContent, NodeTemplate.class);
                        if (nodeTemplate != null && StringUtils.isNotBlank(nodeTemplate.getType())) {
                            String nodeTypeContent = context
                                    .getAttribute(ConfigModelConstant.PROPERTY_NODE_TYPES_DOT + nodeTemplate.getType());
                            NodeType nodetype = TransformationUtils.readValue(nodeTypeContent, NodeType.class);
                            NodePropertyUtils nodePropertyUtils = new NodePropertyUtils(context, inParams);
                            nodePropertyUtils.assignOutParamsFromModel(nodetype, nodeTemplate);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
        return context;
    }
    
    @Override
    public String getNodeTemplateContent(final SvcLogicContext context, String templateName) throws SvcLogicException {
        String content = null;
        try {
            if (context != null && StringUtils.isNotBlank(templateName)) {
                logger.info("Processing Artifact Node Template  for content : ({})", templateName);
                content = context
                        .getAttribute(ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + templateName + ".content");
            }
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
        return content;
    }
    
    @SuppressWarnings("squid:S3776")
    @Override
    public String getNodeTemplateMapping(final SvcLogicContext context, String templateName) throws SvcLogicException {
        String mapping = null;
        try {
            if (context != null && StringUtils.isNotBlank(templateName)) {
                logger.info("Processing artifact node template for mapping : ({})", templateName);
                if (StringUtils.isNotBlank(templateName)) {
                    String nodeTemplateContent =
                            context.getAttribute(ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + templateName);
                    if (StringUtils.isNotBlank(nodeTemplateContent)) {
                        
                        NodeTemplate nodeTemplate =
                                TransformationUtils.readValue(nodeTemplateContent, NodeTemplate.class);
                        
                        if (nodeTemplate != null && nodeTemplate.getCapabilities() != null && nodeTemplate
                                .getCapabilities().containsKey(ConfigModelConstant.CAPABILITY_PROPERTY_MAPPING)) {
                            
                            CapabilityAssignment capability =
                                    nodeTemplate.getCapabilities().get(ConfigModelConstant.CAPABILITY_PROPERTY_MAPPING);
                            if (capability.getProperties() != null) {
                                List mappingList = (List) capability.getProperties()
                                        .get(ConfigModelConstant.CAPABILITY_PROPERTY_MAPPING);
                                if (mappingList != null) {
                                    mapping = TransformationUtils.getJson(mappingList);
                                }
                                
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
        return mapping;
    }
    
}

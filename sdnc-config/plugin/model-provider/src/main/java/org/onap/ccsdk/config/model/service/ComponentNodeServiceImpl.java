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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.data.adaptor.DataAdaptorConstants;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.data.InterfaceAssignment;
import org.onap.ccsdk.config.model.data.NodeTemplate;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ComponentNodeServiceImpl implements ComponentNodeService {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ComponentNodeServiceImpl.class);
    
    private BundleContext bcontext;
    private ConfigResourceService configResourceService;
    private ConfigModelService configModelService;
    
    public ComponentNodeServiceImpl(BundleContext blueprintBundleContext, ConfigResourceService configResourceService,
            ConfigRestAdaptorService configRestAdaptorService) {
        logger.info("{} Constructor Initiated", "ComponentNodeServiceImpl");
        this.bcontext = blueprintBundleContext;
        this.configResourceService = configResourceService;
        this.configModelService = new ConfigModelServiceImpl(configRestAdaptorService);
    }
    
    @Override
    public void process(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        String requestId = null;
        String selector = null;
        try {
            selector = inParams.get(ConfigModelConstant.PROPERTY_SELECTOR);
            requestId = ctx.getAttribute(ConfigModelConstant.PROPERTY_REQUEST_ID);
            
            logger.info("Component execution started with input params  ({})", inParams);
            configModelService.assignInParamsFromModel(ctx, inParams);
            
            String currentInterface = inParams.get(ConfigModelConstant.PROPERTY_CURRENT_INTERFACE);
            String currentNodeDerivedFrom = inParams.get(ConfigModelConstant.PROPERTY_CURRENT_NODETYPE_DERIVED_FROM);
            
            configResourceService.save(new TransactionLog(requestId, DataAdaptorConstants.LOG_MESSAGE_TYPE_COMPONENT,
                    String.format("Executing Component (%s) derived from (%s) with Params :  (%s) ", currentInterface,
                            currentNodeDerivedFrom, inParams)));
            
            ComponentNode handler = getComponentNodeInterface(currentInterface, currentNodeDerivedFrom);
            
            if (handler == null) {
                throw new SvcLogicException(
                        String.format("Could not find Component for Interface %s", currentInterface));
            }
            if (componentContext == null) {
                componentContext = new HashMap<>();
            }
            
            logger.debug("Executing component ({})", currentInterface);
            
            if (handler.preCondition(inParams, ctx, componentContext)) {
                handler.preProcess(inParams, ctx, componentContext);
                handler.process(inParams, ctx, componentContext);
                handler.postProcess(inParams, ctx, componentContext);
                logger.debug("Executed component ({}) successfully.", currentInterface);
                configResourceService
                        .save(new TransactionLog(requestId, DataAdaptorConstants.LOG_MESSAGE_TYPE_COMPONENT,
                                String.format("Component (%s) executed successfully. ", currentInterface)));
                
                ctx.setAttribute(selector + ConfigModelConstant.PROPERTY_DOT_STATUS,
                        ConfigModelConstant.STATUS_SUCCESS);
            } else {
                logger.info("Skipped component execution  ({})", handler.getClass());
                configResourceService
                        .save(new TransactionLog(requestId, DataAdaptorConstants.LOG_MESSAGE_TYPE_COMPONENT,
                                String.format("Skipping component (%s) execution.", handler.getClass())));
                ctx.setAttribute(selector + ConfigModelConstant.PROPERTY_DOT_STATUS,
                        ConfigModelConstant.STATUS_SKIPPED);
            }
            
            configModelService.assignOutParamsFromModel(ctx, inParams);
            ctx.setStatus(ConfigModelConstant.STATUS_SUCCESS);
        } catch (Exception e) {
            logger.error(String.format("Failed in component (%s) execution for request id (%s) with error %s", selector,
                    requestId, e.getMessage()));
            configResourceService.save(new TransactionLog(requestId, DataAdaptorConstants.LOG_MESSAGE_TYPE_COMPONENT,
                    String.format("Failed in component (%s) execution for request id (%s) with error %s", selector,
                            requestId, e.getMessage())));
            
            ctx.setAttribute(selector + ConfigModelConstant.PROPERTY_DOT_STATUS, ConfigModelConstant.STATUS_FAILURE);
            ctx.setAttribute(selector + ConfigModelConstant.PROPERTY_DOT_ERROR_MESSAGE, e.getMessage());
            ctx.setAttribute(ConfigModelConstant.PROPERTY_ERROR_MESSAGE, e.getMessage());
            ctx.setStatus(ConfigModelConstant.STATUS_FAILURE);
            throw new SvcLogicException(e.getMessage(), e);
        }
    }
    
    @Override
    public ComponentNode getComponentNodeInterface(String pluginName, String componentType) throws SvcLogicException {
        
        logger.info("Searching for component node plugin ({}) component type ({})", pluginName, componentType);
        
        if (StringUtils.isBlank(pluginName)) {
            throw new SvcLogicException(
                    String.format("Could not get Interface Name from Service Template :  %s ", pluginName));
        }
        
        pluginName = pluginName.replace("-", ".");
        ServiceReference sref = bcontext.getServiceReference(pluginName);
        
        if (sref == null) {
            throw new SvcLogicException(
                    String.format("Could not find service reference object for plugin %s", pluginName));
        }
        return (ComponentNode) bcontext.getService(sref);
    }
    
    @Override
    public ComponentNode getComponentNode(SvcLogicContext ctx, String componentKey) throws SvcLogicException {
        
        if (StringUtils.isBlank(componentKey)) {
            logger.warn("Can't get node template content for a component key ({})", componentKey);
            return null;
        }
        
        String nodeTemplateContent = ctx.getAttribute(ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + componentKey);
        logger.info("Processing component template : ({})", nodeTemplateContent);
        
        if (StringUtils.isBlank(nodeTemplateContent)) {
            logger.warn("Couldn't get node template content for component key ({})", componentKey);
            return null;
        }
        
        NodeTemplate nodeTemplate = TransformationUtils.readValue(nodeTemplateContent, NodeTemplate.class);
        if (nodeTemplate == null || StringUtils.isBlank(nodeTemplate.getType())) {
            logger.warn("Failed to convert content ({}) to node template.", nodeTemplateContent);
            return null;
        }
        
        ComponentNode componentNode = null;
        for (Map.Entry<String, InterfaceAssignment> nodeTemplateInterface : nodeTemplate.getInterfaces().entrySet()) {
            if (nodeTemplateInterface != null && nodeTemplateInterface.getValue() != null) {
                String pluginName = nodeTemplateInterface.getKey();
                componentNode = getComponentNodeInterface(pluginName, ConfigModelConstant.MODEL_TYPE_NODE_COMPONENT);
            }
        }
        return componentNode;
    }
    
}

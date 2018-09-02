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

package org.onap.ccsdk.config.assignment.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.assignment.service.ConfigAssignmentUtils;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.ConfigModelException;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.data.dict.ResourceDefinition;
import org.onap.ccsdk.config.model.data.dict.SourceMdsal;
import org.onap.ccsdk.config.model.service.ComponentNode;
import org.onap.ccsdk.config.model.utils.ResourceAssignmentUtils;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorConstants;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;

public class MdsalResourceProcessor implements ComponentNode {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(MdsalResourceProcessor.class);
    private ConfigRestAdaptorService configRestAdaptorService;
    private Map<String, ResourceDefinition> dictionaries;
    
    public MdsalResourceProcessor(ConfigRestAdaptorService configRestAdaptorService) {
        this.configRestAdaptorService = configRestAdaptorService;
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
    
    @Override
    public void process(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        // Auto-generated method stub
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void process(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        try {
            List<ResourceAssignment> batchResourceAssignment =
                    (List<ResourceAssignment>) componentContext.get(ConfigModelConstant.PROPERTY_RESOURCE_ASSIGNMENTS);
            dictionaries =
                    (Map<String, ResourceDefinition>) componentContext.get(ConfigModelConstant.PROPERTY_DICTIONARIES);
            
            if (CollectionUtils.isNotEmpty(batchResourceAssignment)) {
                for (ResourceAssignment resourceAssignment : batchResourceAssignment) {
                    processResourceAssignmnet(ctx, componentContext, resourceAssignment);
                }
            }
        } catch (Exception e) {
            throw new SvcLogicException(String.format("MdsalResourceProcessor Exception : (%s) ", e), e);
        }
    }
    
    private void processResourceAssignmnet(SvcLogicContext ctx, Map<String, Object> componentContext,
            ResourceAssignment resourceAssignment) throws ConfigModelException, SvcLogicException {
        
        try {
            // Validating Resource Assignment and Dictionary Definition data
            validate(resourceAssignment);
            
            // Check if It has Input
            Object value = ConfigAssignmentUtils.getContextKeyValue(ctx, resourceAssignment.getName());
            if (value != null) {
                logger.info("mdsal source template key ({}) found from input and value is ({})",
                        resourceAssignment.getName(), value);
                ResourceAssignmentUtils.setResourceDataValue(componentContext, resourceAssignment, value);
                return;
            }
            
            ResourceDefinition resourceDefinition = dictionaries.get(resourceAssignment.getDictionaryName());
            SourceMdsal sourceMdsal = resourceDefinition.getSources().getMdsal();
            String urlPath = sourceMdsal.getProperties().getUrlPath();
            String path = sourceMdsal.getProperties().getPath();
            Map<String, String> inputKeyMapping = sourceMdsal.getProperties().getInputKeyMapping();
            Map<String, String> outputKeyMapping = sourceMdsal.getProperties().getOutputKeyMapping();
            
            logger.info(
                    "mdsal dictionary information : urlpath ({}), path({}), inputKeyMapping ({}), outputKeyMapping ({})",
                    urlPath, path, inputKeyMapping, outputKeyMapping);
            
            // Resolving url Variables
            Map<String, Object> urlVariables = populateUrlVariables(inputKeyMapping, componentContext);
            for (Map.Entry<String, Object> entry : urlVariables.entrySet()) {
                urlPath = urlPath.replaceAll("\\$" + entry.getKey(), entry.getValue().toString());
            }
            
            String restResponse = fetchResourceFromMDSAL(urlPath);
            // if restResponse is null don't call processMdsalResults to populate the value
            if (StringUtils.isNotBlank(restResponse)) {
                // Processing MDSAL Response
                processMdsalResults(ctx, componentContext, resourceAssignment, sourceMdsal, restResponse);
            } else {
                logger.warn("Coudn't get proper mdsal Response content ({}) for Resource Name ({}) for URI ({})",
                        restResponse, resourceAssignment.getDictionaryName(), urlPath);
            }
            
            // Check the value has populated for mandatory case
            ResourceAssignmentUtils.assertTemplateKeyValueNotNull(componentContext, resourceAssignment);
        } catch (Exception e) {
            ResourceAssignmentUtils.setFailedResourceDataValue(componentContext, resourceAssignment, e.getMessage());
            throw new SvcLogicException(
                    String.format("Failed in assignments for (%s) with (%s)", resourceAssignment, e), e);
        }
    }
    
    private String fetchResourceFromMDSAL(String urlPath) {
        String response = null;
        try {
            response = configRestAdaptorService.getResource(ConfigRestAdaptorConstants.SELECTOR_RESTCONF, urlPath,
                    String.class);
        } catch (Exception e) {
            logger.warn("Fetching MDSAL data for URL ({}) failed with Error ({})", urlPath, e);
        }
        return response;
    }
    
    private void validate(ResourceAssignment resourceAssignment) throws SvcLogicException {
        if (resourceAssignment == null) {
            throw new SvcLogicException("resource assignment is not defined");
        }
        
        if (StringUtils.isBlank(resourceAssignment.getName())) {
            throw new SvcLogicException("resource assignment template key is not defined");
        }
        
        if (StringUtils.isBlank(resourceAssignment.getDictionaryName())) {
            throw new SvcLogicException(
                    String.format("resource assignment dictionary name is not defined for template key (%s)",
                            resourceAssignment.getName()));
        }
        
        if (!ConfigModelConstant.SOURCE_MDSAL.equalsIgnoreCase(resourceAssignment.getDictionarySource())) {
            throw new SvcLogicException(String.format("resource assignment source is not mdsal, it is (%s)",
                    resourceAssignment.getDictionarySource()));
        }
        
        ResourceDefinition resourceDefinition = dictionaries.get(resourceAssignment.getDictionaryName());
        if (resourceDefinition == null) {
            throw new SvcLogicException(String.format("missing resource dictionary definition for name (%s)  ",
                    resourceAssignment.getDictionaryName()));
        }
        
        if (StringUtils.isBlank(resourceDefinition.getProperty().getType())) {
            throw new SvcLogicException(String.format(String.format("Failed to get dictionary (%s) data type info.",
                    resourceAssignment.getDictionaryName())));
        }
        
        if (resourceDefinition.getSources() == null || resourceDefinition.getSources().getMdsal() == null) {
            throw new SvcLogicException(
                    String.format("missing resource dictionary mdsal source definition for name (%s)  ",
                            resourceAssignment.getDictionaryName()));
        }
        
        SourceMdsal sourceMdsal = resourceDefinition.getSources().getMdsal();
        
        if (StringUtils.isBlank(sourceMdsal.getProperties().getUrlPath())) {
            throw new SvcLogicException(String.format("Failed to get request URL Path for dictionary (%s)",
                    resourceAssignment.getDictionaryName()));
        }
        
        if (StringUtils.isBlank(sourceMdsal.getProperties().getPath())) {
            throw new SvcLogicException(String.format("Failed to get request Path for dictionary (%s)",
                    resourceAssignment.getDictionaryName()));
        }
    }
    
    private Map<String, Object> populateUrlVariables(Map<String, String> inputKeyMapping,
            Map<String, Object> componentContext) {
        Map<String, Object> urlVariables = new HashMap<>();
        if (MapUtils.isNotEmpty(inputKeyMapping)) {
            
            for (Map.Entry<String, String> mapping : inputKeyMapping.entrySet()) {
                ResourceDefinition referenceDictionaryDefinition = dictionaries.get(mapping.getValue());
                Object expressionValue =
                        ResourceAssignmentUtils.getDictionaryKeyValue(componentContext, referenceDictionaryDefinition);
                logger.trace("Reference dictionary key ({}), value ({})", mapping.getKey(), expressionValue);
                urlVariables.put(mapping.getKey(), expressionValue);
            }
        }
        return urlVariables;
    }
    
    private void processMdsalResults(SvcLogicContext ctx, Map<String, Object> componentContext,
            ResourceAssignment resourceAssignment, SourceMdsal sourceMdsal, String restResponse)
            throws SvcLogicException, ConfigModelException {
        
        Map<String, String> outputKeyMapping = sourceMdsal.getProperties().getOutputKeyMapping();
        JsonNode responseNode = TransformationUtils.getJsonNodeForString(restResponse);
        if (StringUtils.isNotBlank(sourceMdsal.getProperties().getPath())) {
            responseNode = responseNode.at(sourceMdsal.getProperties().getPath());
        }
        if (responseNode != null) {
            ConfigAssignmentUtils.populateValueForOutputMapping(ctx, componentContext, resourceAssignment,
                    outputKeyMapping, responseNode);
        }
    }
    
    @Override
    public void postProcess(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        // Do Nothing
    }
    
}

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
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.ConfigModelException;
import org.onap.ccsdk.config.model.ValidTypes;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.data.dict.ResourceDefinition;
import org.onap.ccsdk.config.model.data.dict.SourceDb;
import org.onap.ccsdk.config.model.service.ComponentNode;
import org.onap.ccsdk.config.model.utils.JsonUtils;
import org.onap.ccsdk.config.model.utils.ResourceAssignmentUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DBResourceProcessor implements ComponentNode {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(DBResourceProcessor.class);
    private ConfigResourceService configResourceService;
    private Map<String, ResourceDefinition> dictionaries;
    
    public DBResourceProcessor(ConfigResourceService configResourceService) {
        this.configResourceService = configResourceService;
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
                    processResourceAssignment(ctx, componentContext, resourceAssignment);
                }
            }
        } catch (Exception e) {
            throw new SvcLogicException(String.format("DBResourceProcessor Exception : (%s)", e), e);
        }
    }
    
    private void processResourceAssignment(SvcLogicContext ctx, Map<String, Object> componentContext,
            ResourceAssignment resourceAssignment) throws SvcLogicException, ConfigModelException {
        if (resourceAssignment != null) {
            try {
                validate(resourceAssignment);
                
                // Check if It has Input
                Object value = ConfigAssignmentUtils.getContextKeyValue(ctx, resourceAssignment.getName());
                if (value != null) {
                    logger.info("db source template key ({}) found from input and value is ({})",
                            resourceAssignment.getName(), value);
                    ResourceAssignmentUtils.setResourceDataValue(componentContext, resourceAssignment, value);
                    return;
                }
                
                ResourceDefinition resourceDefinition = dictionaries.get(resourceAssignment.getDictionaryName());
                
                SourceDb sourceDb = resourceDefinition.getSources().getDb();
                if (StringUtils.isBlank(sourceDb.getProperties().getQuery())) {
                    throw new SvcLogicException("db query property is missing");
                }
                
                String sql = sourceDb.getProperties().getQuery();
                Map<String, String> inputKeyMapping = sourceDb.getProperties().getInputKeyMapping();
                
                logger.info("Db dictionary information : ({}), ({}), ({})", sql, inputKeyMapping,
                        sourceDb.getProperties().getOutputKeyMapping());
                
                Map<String, Object> namedParameters = populateNamedParameter(componentContext, inputKeyMapping);
                
                logger.info("Parameter information : ({})", namedParameters);
                List<Map<String, Object>> rows = configResourceService.query(sql, namedParameters);
                if (rows != null && !rows.isEmpty()) {
                    processDBResults(ctx, componentContext, resourceAssignment, sourceDb, rows);
                } else {
                    logger.warn("Failed to get db result for dictionary name ({}) the query ({})",
                            resourceAssignment.getDictionaryName(), sql);
                }
                
                // Check the value has populated for mandatory case
                ResourceAssignmentUtils.assertTemplateKeyValueNotNull(componentContext, resourceAssignment);
            } catch (Exception e) {
                ResourceAssignmentUtils.setFailedResourceDataValue(componentContext, resourceAssignment,
                        e.getMessage());
                throw new SvcLogicException(
                        String.format("Failed in template key (%s) assignments : (%s)", resourceAssignment, e), e);
            }
        } else {
            // Do Nothing
        }
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
        
        if (!ConfigModelConstant.SOURCE_DB.equalsIgnoreCase(resourceAssignment.getDictionarySource())) {
            throw new SvcLogicException(String.format("resource assignment source is not db, it is (%s)",
                    resourceAssignment.getDictionarySource()));
        }
        
        ResourceDefinition resourceDefinition = dictionaries.get(resourceAssignment.getDictionaryName());
        if (resourceDefinition == null) {
            throw new SvcLogicException(String.format("missing resource dictionary definition for name (%s)  ",
                    resourceAssignment.getDictionaryName()));
        }
        
        if (resourceDefinition.getSources() == null || resourceDefinition.getSources().getDb() == null) {
            throw new SvcLogicException(String.format("missing resource dictionary db source definition for name (%s) ",
                    resourceAssignment.getDictionaryName()));
        }
        
    }
    
    private Map<String, Object> populateNamedParameter(Map<String, Object> componentContext,
            Map<String, String> inputKeyMapping) {
        Map<String, Object> namedParameters = new HashMap<>();
        if (MapUtils.isNotEmpty(inputKeyMapping)) {
            
            for (Map.Entry<String, String> mapping : inputKeyMapping.entrySet()) {
                ResourceDefinition referenceDictionaryDefinition = dictionaries.get(mapping.getValue());
                Object expressionValue =
                        ResourceAssignmentUtils.getDictionaryKeyValue(componentContext, referenceDictionaryDefinition);
                logger.trace("Reference dictionary key ({}), value ({})", mapping.getKey(), expressionValue);
                namedParameters.put(mapping.getKey(), expressionValue);
            }
        }
        return namedParameters;
    }
    
    @SuppressWarnings("squid:S3776")
    private void processDBResults(SvcLogicContext ctx, Map<String, Object> componentContext,
            ResourceAssignment resourceAssignment, SourceDb sourceDb, List<Map<String, Object>> rows)
            throws SvcLogicException, ConfigModelException {
        
        Map<String, String> outputKeyMapping = sourceDb.getProperties().getOutputKeyMapping();
        String type = resourceAssignment.getProperty().getType();
        String entrySchema = null;
        logger.info("Response processing type({})", type);
        // Primitive Types
        if (ValidTypes.getPrimitivePropertType().contains(type)) {
            
            Map<String, Object> row = rows.get(0);
            String dbColumnName = outputKeyMapping.get(resourceAssignment.getDictionaryName());
            Object dbColumnValue = row.get(dbColumnName);
            ResourceAssignmentUtils.setResourceDataValue(componentContext, resourceAssignment, dbColumnValue);
            
        } else if (ValidTypes.getListPropertType().contains(type)) {
            // Array Types
            if (resourceAssignment.getProperty().getEntrySchema() != null) {
                entrySchema = resourceAssignment.getProperty().getEntrySchema().getType();
            }
            
            if (StringUtils.isNotBlank(entrySchema)) {
                ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
                
                for (Map<String, Object> row : rows) {
                    if (ValidTypes.getPrimitivePropertType().contains(entrySchema)) {
                        String dbColumnName = outputKeyMapping.get(resourceAssignment.getDictionaryName());
                        Object dbColumnValue = row.get(dbColumnName);
                        // Add Array JSON
                        JsonUtils.populatePrimitiveValues(dbColumnValue, entrySchema, arrayNode);
                    } else {
                        ObjectNode arrayChildNode = JsonNodeFactory.instance.objectNode();
                        for (Map.Entry<String, String> mapping : outputKeyMapping.entrySet()) {
                            Object dbColumnValue = row.get(mapping.getKey());
                            String propertyTypeForDataType =
                                    ConfigAssignmentUtils.getPropertyType(ctx, entrySchema, mapping.getKey());
                            JsonUtils.populatePrimitiveValues(mapping.getKey(), dbColumnValue, propertyTypeForDataType,
                                    arrayChildNode);
                        }
                        arrayNode.add(arrayChildNode);
                    }
                }
                // Set the List of Complex Values
                ResourceAssignmentUtils.setResourceDataValue(componentContext, resourceAssignment, arrayNode);
            } else {
                throw new SvcLogicException(String.format("Entry schema is not defined for dictionary (%s) info",
                        resourceAssignment.getDictionaryName()));
            }
        } else {
            // Complex Types
            Map<String, Object> row = rows.get(0);
            ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
            for (Map.Entry<String, String> mapping : outputKeyMapping.entrySet()) {
                Object dbColumnValue = row.get(mapping.getKey());
                String propertyTypeForDataType = ConfigAssignmentUtils.getPropertyType(ctx, type, mapping.getKey());
                JsonUtils.populatePrimitiveValues(mapping.getKey(), dbColumnValue, propertyTypeForDataType, objectNode);
            }
            ResourceAssignmentUtils.setResourceDataValue(componentContext, resourceAssignment, objectNode);
        }
    }
    
    @Override
    public void postProcess(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        // Auto-generated method stub
    }
    
}

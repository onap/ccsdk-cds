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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.data.adaptor.domain.ResourceAssignmentData;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.ConfigModelException;
import org.onap.ccsdk.config.model.ValidTypes;
import org.onap.ccsdk.config.model.data.DataType;
import org.onap.ccsdk.config.model.data.EntrySchema;
import org.onap.ccsdk.config.model.data.PropertyDefinition;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.data.dict.ResourceDefinition;
import org.onap.ccsdk.config.model.domain.ResourceDictionary;
import org.onap.ccsdk.config.model.utils.JsonUtils;
import org.onap.ccsdk.config.model.utils.ResourceAssignmentUtils;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ConfigAssignmentUtils {
    
    private ConfigAssignmentUtils() {
        
    }
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigAssignmentUtils.class);
    
    public static synchronized Object getContextKeyValue(SvcLogicContext context, String key) {
        Object value = null;
        if (context != null && key != null) {
            if (context.getAttributeKeySet().contains(key)) {
                String strValue = context.getAttribute(key);
                if (StringUtils.isNotBlank(strValue)) {
                    value = strValue;
                }
            } else {
                // Do Nothing
            }
        }
        return value;
    }
    
    /*
     * Populate the Field property type for the Data type
     */
    public static synchronized String getPropertyType(SvcLogicContext ctx, String dataTypeName, String propertyName)
            throws SvcLogicException {
        String type = ValidTypes.DATA_TYPE_STRING;
        try {
            if (ctx != null && StringUtils.isNotBlank(dataTypeName) && StringUtils.isNotBlank(propertyName)) {
                String dataTypeContent = ctx.getAttribute(ConfigModelConstant.PROPERTY_DATA_TYPES_DOT + dataTypeName);
                if (StringUtils.isNotBlank(dataTypeContent)) {
                    DataType dataType = TransformationUtils.readValue(dataTypeContent, DataType.class);
                    if (dataType != null && dataType.getProperties() != null
                            && dataType.getProperties().containsKey(propertyName)) {
                        PropertyDefinition propertyDefinition = dataType.getProperties().get(propertyName);
                        if (StringUtils.isNotBlank(propertyDefinition.getType())) {
                            type = propertyDefinition.getType();
                            logger.trace("Data type({})'s property ({}) is ({})", dataTypeName, propertyName, type);
                        } else {
                            throw new SvcLogicException(String.format("Couldn't get data type (%s) ", dataTypeName));
                        }
                    }
                } else {
                    throw new SvcLogicException(String.format("Couldn't get data type (%s) content", dataTypeName));
                }
            }
        } catch (Exception e) {
            logger.error("couldn't get data type({})'s property ({}), type ({}), error message ({}).", dataTypeName,
                    propertyName, type, e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
        return type;
    }
    
    /*
     * Populate the Field property type for the Data type
     */
    public static synchronized PropertyDefinition getPropertyDefinition(SvcLogicContext ctx, String dataTypeName,
            String propertyName) throws SvcLogicException {
        PropertyDefinition propertyDefinition = null;
        try {
            if (ctx != null && StringUtils.isNotBlank(dataTypeName) && StringUtils.isNotBlank(propertyName)) {
                String dataTypeContent = ctx.getAttribute(ConfigModelConstant.PROPERTY_DATA_TYPES_DOT + dataTypeName);
                if (StringUtils.isNotBlank(dataTypeContent)) {
                    DataType dataType = TransformationUtils.readValue(dataTypeContent, DataType.class);
                    if (dataType != null && dataType.getProperties() != null
                            && dataType.getProperties().containsKey(propertyName)) {
                        propertyDefinition = dataType.getProperties().get(propertyName);
                        if (propertyDefinition == null) {
                            throw new SvcLogicException(String.format("couldn't get data type (%s) ", dataTypeName));
                        }
                    }
                } else {
                    throw new SvcLogicException(String.format("couldn't get data type (%s) content.", dataTypeName));
                }
            }
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
        return propertyDefinition;
    }
    
    public static synchronized ResourceDefinition getDictionaryDefinition(Map<String, ResourceDictionary> dictionaries,
            String dictionaryName) {
        ResourceDefinition resourceDefinition = null;
        if (dictionaries != null && StringUtils.isNotBlank(dictionaryName)) {
            ResourceDictionary resourceDictionary = dictionaries.get(dictionaryName);
            if (resourceDictionary != null && StringUtils.isNotBlank(resourceDictionary.getDefinition())) {
                resourceDefinition =
                        TransformationUtils.readValue(resourceDictionary.getDefinition(), ResourceDefinition.class);
            }
        }
        return resourceDefinition;
    }
    
    @SuppressWarnings("squid:S3776")
    public static synchronized void populateValueForOutputMapping(SvcLogicContext ctx,
            Map<String, Object> componentContext, ResourceAssignment resourceAssignment,
            Map<String, String> outputKeyMapping, JsonNode responseNode)
            throws ConfigModelException, SvcLogicException {
        if (resourceAssignment == null) {
            throw new SvcLogicException("resourceAssignment is null.");
        }
        
        if (ctx == null) {
            throw new SvcLogicException("service logic context is null.");
        }
        
        if (componentContext == null) {
            throw new SvcLogicException("component context is null.");
        }
        
        logger.info("populating value for output mapping ({}), from json ({})", outputKeyMapping, responseNode);
        String dictionaryName = resourceAssignment.getDictionaryName();
        String type = resourceAssignment.getProperty().getType();
        
        String entrySchema = null;
        if (ValidTypes.getPrimitivePropertType().contains(type)) {
            ResourceAssignmentUtils.setResourceDataValue(componentContext, resourceAssignment, responseNode);
        } else if (ValidTypes.getListPropertType().contains(type)) {
            // Array Types
            if (resourceAssignment.getProperty().getEntrySchema() != null) {
                entrySchema = resourceAssignment.getProperty().getEntrySchema().getType();
            }
            
            if (StringUtils.isNotBlank(entrySchema)) {
                ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
                if (ValidTypes.getPrimitivePropertType().contains(entrySchema)) {
                    arrayNode = (ArrayNode) responseNode;
                } else if (MapUtils.isNotEmpty(outputKeyMapping)) {
                    List<JsonNode> responseArrayNode = IteratorUtils.toList(responseNode.elements());
                    for (JsonNode responseSingleJsonNode : responseArrayNode) {
                        if (responseSingleJsonNode != null) {
                            ObjectNode arrayChildNode = JsonNodeFactory.instance.objectNode();
                            for (Map.Entry<String, String> mapping : outputKeyMapping.entrySet()) {
                                JsonNode responseKeyValue = responseSingleJsonNode.get(mapping.getKey());
                                
                                String propertyTypeForDataType =
                                        ConfigAssignmentUtils.getPropertyType(ctx, entrySchema, mapping.getKey());
                                logger.info("For List Type Resource: key ({}), value ({}), type  ({})",
                                        mapping.getKey(), responseKeyValue, propertyTypeForDataType);
                                JsonUtils.populateJsonNodeValues(mapping.getValue(), responseKeyValue,
                                        propertyTypeForDataType, arrayChildNode);
                            }
                            arrayNode.add(arrayChildNode);
                        }
                    }
                } else {
                    arrayNode = (ArrayNode) responseNode;
                }
                // Set the List of Complex Values
                ResourceAssignmentUtils.setResourceDataValue(componentContext, resourceAssignment, arrayNode);
            } else {
                throw new SvcLogicException(
                        String.format("Entry schema is not defined for dictionary (%s) info", dictionaryName));
            }
        } else {
            // Complex Types
            ObjectNode objectNode = null;
            if (MapUtils.isNotEmpty(outputKeyMapping)) {
                objectNode = JsonNodeFactory.instance.objectNode();
                for (Map.Entry<String, String> mapping : outputKeyMapping.entrySet()) {
                    JsonNode responseKeyValue = responseNode.get(mapping.getKey());
                    String propertyTypeForDataType =
                            ConfigAssignmentUtils.getPropertyType(ctx, entrySchema, mapping.getKey());
                    logger.info("For Complex Type Resource: key ({}), value ({}), type  ({})", mapping.getKey(),
                            responseKeyValue, propertyTypeForDataType);
                    JsonUtils.populateJsonNodeValues(mapping.getValue(), responseKeyValue, propertyTypeForDataType,
                            objectNode);
                }
            } else {
                objectNode = (ObjectNode) responseNode;
            }
            ResourceAssignmentUtils.setResourceDataValue(componentContext, resourceAssignment, objectNode);
        }
    }
    
    @SuppressWarnings("squid:S3776")
    public static synchronized List<ResourceAssignment> convertResoureAssignmentDataList(
            List<ResourceAssignmentData> resourceAssignmentDataList) {
        List<ResourceAssignment> assignments = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(resourceAssignmentDataList)) {
            for (ResourceAssignmentData resourceAssignmentData : resourceAssignmentDataList) {
                if (resourceAssignmentData != null) {
                    ResourceAssignment resourceAssignment = new ResourceAssignment();
                    resourceAssignment.setName(resourceAssignmentData.getTemplateKeyName());
                    resourceAssignment.setVersion(resourceAssignmentData.getVersion());
                    resourceAssignment.setUpdatedBy(resourceAssignmentData.getUpdatedBy());
                    resourceAssignment.setUpdatedDate(resourceAssignmentData.getUpdatedDate());
                    resourceAssignment.setDictionaryName(resourceAssignmentData.getResourceName());
                    resourceAssignment.setDictionarySource(resourceAssignmentData.getSource());
                    resourceAssignment.setStatus(resourceAssignmentData.getStatus());
                    resourceAssignment.setMessage(resourceAssignmentData.getMessage());
                    PropertyDefinition property = new PropertyDefinition();
                    property.setType(resourceAssignmentData.getDataType());
                    
                    if (StringUtils.isNotBlank(resourceAssignmentData.getResourceValue())) {
                        if (ValidTypes.getPrimitivePropertType().contains(resourceAssignmentData.getDataType())) {
                            property.setValue(resourceAssignmentData.getResourceValue());
                        } else {
                            JsonNode valueNode =
                                    TransformationUtils.getJsonNodeForString(resourceAssignmentData.getResourceValue());
                            property.setValue(valueNode);
                        }
                    }
                    if (StringUtils.isNotBlank(resourceAssignmentData.getEntrySchema())) {
                        EntrySchema entrySchema = new EntrySchema();
                        entrySchema.setType(resourceAssignmentData.getEntrySchema());
                        property.setEntrySchema(entrySchema);
                    } else {
                        property.setEntrySchema(null);
                    }
                    resourceAssignment.setProperty(property);
                    assignments.add(resourceAssignment);
                }
            }
            
        }
        return assignments;
    }
    
    @SuppressWarnings("squid:S3776")
    public static synchronized List<ResourceAssignmentData> convertResoureAssignmentList(
            List<ResourceAssignment> assignments) {
        List<ResourceAssignmentData> resourceAssignmentDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(assignments)) {
            for (ResourceAssignment assignment : assignments) {
                if (assignment != null) {
                    ResourceAssignmentData resourceAssignmentData = new ResourceAssignmentData();
                    resourceAssignmentData.setTemplateKeyName(assignment.getName());
                    resourceAssignmentData.setVersion(assignment.getVersion());
                    resourceAssignmentData.setUpdatedBy(assignment.getUpdatedBy());
                    resourceAssignmentData.setUpdatedDate(assignment.getUpdatedDate());
                    if (assignment.getProperty() != null) {
                        resourceAssignmentData.setDataType(assignment.getProperty().getType());
                        if (assignment.getProperty().getEntrySchema() != null) {
                            resourceAssignmentData.setEntrySchema(assignment.getProperty().getEntrySchema().getType());
                        }
                        if (assignment.getProperty().getValue() != null) {
                            String valueContent = TransformationUtils.getJson(assignment.getProperty().getValue());
                            resourceAssignmentData.setResourceValue(valueContent);
                        }
                    }
                    resourceAssignmentData.setResourceName(assignment.getDictionaryName());
                    resourceAssignmentData.setSource(assignment.getDictionarySource());
                    resourceAssignmentData.setStatus(assignment.getStatus());
                    resourceAssignmentData.setMessage(assignment.getMessage());
                    resourceAssignmentDataList.add(resourceAssignmentData);
                }
            }
        }
        return resourceAssignmentDataList;
    }
    
}

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

package org.onap.ccsdk.config.model.utils;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.ConfigModelException;
import org.onap.ccsdk.config.model.ValidTypes;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.data.dict.ResourceDefinition;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

public class ResourceAssignmentUtils {
    
    private ResourceAssignmentUtils() {
        
    }
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ResourceAssignmentUtils.class);
    
    public static String getArtifactNodeContent(String nodeTemplateName, Map<String, Object> context) {
        Preconditions.checkArgument(StringUtils.isNotBlank(nodeTemplateName),
                "getArtifactNodeContent missing template name");
        return (String) context.get(ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + nodeTemplateName + ".content");
    }
    
    public static List<ResourceAssignment> getArtifactNodeMapping(String nodeTemplateName,
            Map<String, Object> context) {
        Preconditions.checkArgument(StringUtils.isNotBlank(nodeTemplateName),
                "getArtifactNodeMapping missing template name");
        List<ResourceAssignment> resourceAssignments = null;
        String resourceMappingContent =
                (String) context.get(ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + nodeTemplateName + ".mapping");
        if (StringUtils.isNotBlank(resourceMappingContent)) {
            resourceAssignments = TransformationUtils.getListfromJson(resourceMappingContent, ResourceAssignment.class);
            
        } else {
            logger.warn("missing mapping content for node template ({})", nodeTemplateName);
        }
        return resourceAssignments;
    }
    
    // Not used Any whre
    public static synchronized void cleanContextTemplateNDictionaryKeys(Map<String, Object> componentContext) {
        String recipeName = (String) componentContext.get(ConfigModelConstant.PROPERTY_ACTION_NAME);
        Set<String> removeSet = new HashSet<>();
        componentContext.forEach((key, value) -> {
            if (StringUtils.isNotBlank(key)
                    && (key.startsWith(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + ".")
                            || key.startsWith(ConfigModelConstant.PROPERTY_RECIPE_KEY_DOT + recipeName + "."))) {
                removeSet.add(key);
            }
        });
        componentContext.keySet().removeAll(removeSet);
    }
    
    public static synchronized Object getDictionaryKeyValue(Map<String, Object> componentContext,
            ResourceAssignment resourceMapping) {
        Object value = null;
        if (resourceMapping != null && componentContext != null) {
            String recipeName = (String) componentContext.get(ConfigModelConstant.PROPERTY_ACTION_NAME);
            String dictionaryKeyName = resourceMapping.getDictionaryName();
            value = componentContext
                    .get(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + "." + dictionaryKeyName);
        }
        return value;
    }
    
    public static synchronized Object getDictionaryKeyValue(Map<String, Object> componentContext,
            ResourceDefinition resourceDictionary) {
        Object value = null;
        if (resourceDictionary != null && componentContext != null) {
            String recipeName = (String) componentContext.get(ConfigModelConstant.PROPERTY_ACTION_NAME);
            String dictionaryKeyName = resourceDictionary.getName();
            value = componentContext
                    .get(ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + "." + dictionaryKeyName);
        }
        return value;
    }
    
    public static synchronized Object getTemplateKeyValue(Map<String, Object> componentContext,
            ResourceAssignment resourceMapping) {
        Object value = null;
        if (resourceMapping != null && componentContext != null) {
            String recipeName = (String) componentContext.get(ConfigModelConstant.PROPERTY_ACTION_NAME);
            String templateKeyName = resourceMapping.getName();
            value = componentContext
                    .get(ConfigModelConstant.PROPERTY_RECIPE_KEY_DOT + recipeName + "." + templateKeyName);
        }
        return value;
    }
    
    public static synchronized void setResourceDataValue(Map<String, Object> componentContext,
            ResourceAssignment resourceAssignment, Object value) throws ConfigModelException {
        
        if (resourceAssignment != null && StringUtils.isNotBlank(resourceAssignment.getName())
                && resourceAssignment.getProperty() != null) {
            
            String recipeName = (String) componentContext.get(ConfigModelConstant.PROPERTY_ACTION_NAME);
            String templateKeyName = resourceAssignment.getName();
            String dictionaryKeyName = resourceAssignment.getDictionaryName();
            
            if (StringUtils.isBlank(dictionaryKeyName)) {
                resourceAssignment.setDictionaryName(templateKeyName);
                dictionaryKeyName = templateKeyName;
                logger.warn("Missing dictionary key, setting with template key ({}) as dictionary key ({})",
                        templateKeyName, dictionaryKeyName);
            }
            String type = resourceAssignment.getProperty().getType();
            try {
                if (StringUtils.isNotBlank(type)) {
                    Object convertedValue = convertResourceValue(type, value);
                    
                    componentContext.put(
                            ConfigModelConstant.PROPERTY_DICTIONARY_KEY_DOT + recipeName + "." + dictionaryKeyName,
                            convertedValue);
                    componentContext.put(
                            ConfigModelConstant.PROPERTY_RECIPE_KEY_DOT + recipeName + "." + templateKeyName,
                            convertedValue);
                    
                    logger.trace("Setting Resource Value ({}) for Resource Name ({}) of type ({}) ", convertedValue,
                            dictionaryKeyName, type);
                    
                    resourceAssignment.getProperty().setValue(convertedValue);
                    resourceAssignment.setUpdatedDate(new Date());
                    resourceAssignment.setUpdatedBy(ConfigModelConstant.USER_SYSTEM);
                    resourceAssignment.setStatus(ConfigModelConstant.STATUS_SUCCESS);
                }
            } catch (Exception e) {
                throw new ConfigModelException(String.format(
                        "Failed in setting value for template key (%s) and dictionary key (%s) of type (%s) with error message (%s)",
                        templateKeyName, dictionaryKeyName, type, e.getMessage()));
            }
        } else {
            throw new ConfigModelException(
                    String.format("Failed in setting resource value for resource mapping (%s)", resourceAssignment));
        }
    }
    
    private static Object convertResourceValue(String type, Object value) {
        Object convertedValue = null;
        
        if (value == null || value instanceof NullNode) {
            logger.info("Returning {} value from convertResourceValue", value);
            return null;
        }
        
        if (ValidTypes.getPrimitivePropertType().contains(type)) {
            convertedValue = convertPrimitiveResourceValue(type, value);
        } else {
            // Case where Resource is non-primitive type
            if (value instanceof JsonNode || value instanceof ObjectNode || value instanceof ArrayNode) {
                convertedValue = value;
            } else if (value instanceof String) {
                convertedValue = TransformationUtils.getJsonNodeForString((String) value);
            } else {
                convertedValue = TransformationUtils.getJsonNodeForObject(value);
            }
        }
        return convertedValue;
    }
    
    @SuppressWarnings("squid:S3776")
    private static Object convertPrimitiveResourceValue(String type, Object value) {
        Object convertedValue = value;
        
        if (value instanceof ArrayNode) {
            if (ValidTypes.DATA_TYPE_BOOLEAN.equalsIgnoreCase(type)) {
                convertedValue = ((ArrayNode) value).asBoolean();
            } else if (ValidTypes.DATA_TYPE_INTEGER.equalsIgnoreCase(type)) {
                convertedValue = ((ArrayNode) value).asInt();
            } else if (ValidTypes.DATA_TYPE_FLOAT.equalsIgnoreCase(type)) {
                convertedValue = Float.valueOf(((ArrayNode) value).toString());
            } else {
                convertedValue = ((ArrayNode) value).toString();
            }
        } else if (value instanceof JsonNode) {
            if (ValidTypes.DATA_TYPE_BOOLEAN.equalsIgnoreCase(type)) {
                convertedValue = ((JsonNode) value).asBoolean();
            } else if (ValidTypes.DATA_TYPE_INTEGER.equalsIgnoreCase(type)) {
                convertedValue = ((JsonNode) value).asInt();
            } else if (ValidTypes.DATA_TYPE_FLOAT.equalsIgnoreCase(type)) {
                convertedValue = Float.valueOf(((JsonNode) value).asText());
            } else {
                convertedValue = ((JsonNode) value).asText();
            }
        } else if (value instanceof String) {
            if (ValidTypes.DATA_TYPE_BOOLEAN.equalsIgnoreCase(type)) {
                convertedValue = Boolean.valueOf((String) value);
            } else if (ValidTypes.DATA_TYPE_INTEGER.equalsIgnoreCase(type)) {
                convertedValue = Integer.valueOf((String) value);
            } else if (ValidTypes.DATA_TYPE_FLOAT.equalsIgnoreCase(type)) {
                convertedValue = Float.valueOf((String) value);
            }
        }
        logger.info("Returning value ({}) from convertPrimitiveResourceValue", convertedValue);
        return convertedValue;
    }
    
    @SuppressWarnings("squid:S1172")
    public static synchronized void setFailedResourceDataValue(Map<String, Object> componentContext,
            ResourceAssignment resourceAssignment, String message) {
        setFailedResourceDataValue(resourceAssignment, message);
    }
    
    public static synchronized void setFailedResourceDataValue(ResourceAssignment resourceAssignment, String message) {
        if (resourceAssignment != null && StringUtils.isNotBlank(resourceAssignment.getName())
                && resourceAssignment.getProperty() != null) {
            resourceAssignment.setUpdatedDate(new Date());
            resourceAssignment.setStatus(ConfigModelConstant.STATUS_FAILURE);
            resourceAssignment.setUpdatedBy(ConfigModelConstant.USER_SYSTEM);
            resourceAssignment.setMessage(message);
        }
    }
    
    public static synchronized void assertTemplateKeyValueNotNull(Map<String, Object> componentContext,
            ResourceAssignment resourceAssignment) throws ConfigModelException {
        if (resourceAssignment != null && resourceAssignment.getProperty() != null
                && BooleanUtils.isTrue(resourceAssignment.getProperty().getRequired())
                && getTemplateKeyValue(componentContext, resourceAssignment) == null) {
            logger.error("failed to populate mandatory resource mapping ({})", resourceAssignment);
            throw new ConfigModelException(
                    String.format("failed to populate mandatory resource mapping (%s)", resourceAssignment));
        }
    }
    
    @SuppressWarnings({"squid:S3776", "squid:S1141"})
    public static synchronized String generateResourceDataForAssignments(List<ResourceAssignment> assignments)
            throws ConfigModelException {
        String result = "{}";
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(result);
            for (ResourceAssignment resourceMapping : assignments) {
                if (resourceMapping != null && resourceMapping.getName() != null
                        && resourceMapping.getProperty() != null) {
                    
                    String type = resourceMapping.getProperty().getType();
                    Object value = resourceMapping.getProperty().getValue();
                    logger.info("Generating Resource name ({}), type ({}), value ({})", resourceMapping.getName(), type,
                            value);
                    if (value == null) {
                        ((ObjectNode) root).set(resourceMapping.getName(), null);
                    } else if (value instanceof JsonNode) {
                        ((ObjectNode) root).put(resourceMapping.getName(), (JsonNode) value);
                    } else if (ValidTypes.DATA_TYPE_STRING.equalsIgnoreCase(type)) {
                        ((ObjectNode) root).put(resourceMapping.getName(), (String) value);
                    } else if (ValidTypes.DATA_TYPE_BOOLEAN.equalsIgnoreCase(type)) {
                        ((ObjectNode) root).put(resourceMapping.getName(), (Boolean) value);
                    } else if (ValidTypes.DATA_TYPE_INTEGER.equalsIgnoreCase(type)) {
                        ((ObjectNode) root).put(resourceMapping.getName(), (Integer) value);
                    } else if (ValidTypes.DATA_TYPE_FLOAT.equalsIgnoreCase(type)) {
                        ((ObjectNode) root).put(resourceMapping.getName(), (Float) value);
                    } else if (ValidTypes.DATA_TYPE_TIMESTAMP.equalsIgnoreCase(type)) {
                        ((ObjectNode) root).put(resourceMapping.getName(), (String) value);
                    } else {
                        JsonNode jsonNode = TransformationUtils.getJsonNodeForObject(value);
                        if (jsonNode != null) {
                            ((ObjectNode) root).put(resourceMapping.getName(), jsonNode);
                        } else {
                            ((ObjectNode) root).set(resourceMapping.getName(), null);
                        }
                    }
                }
            }
            result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            logger.info("Generated Resource Param Data ({})", result);
        } catch (Exception e) {
            throw new ConfigModelException(e.getMessage(), e);
        }
        return result;
    }
    
}

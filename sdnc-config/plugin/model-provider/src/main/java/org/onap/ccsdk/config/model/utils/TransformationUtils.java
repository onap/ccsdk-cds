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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

/**
 * TransformationUtils.java Purpose: Provide Configuration Generator TransformationUtils Information
 *
 * @version 1.0
 */
public class TransformationUtils {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(TransformationUtils.class);
    
    private TransformationUtils() {
        
    }
    
    /**
     * This is a getJson method
     *
     * @param configparameters
     * @return String
     */
    public static String getJson(Map<String, Object> configparameters) {
        String jsonContent = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonContent = mapper.writeValueAsString(configparameters);
        } catch (JsonProcessingException e) {
            logger.warn(e.getMessage());
        }
        return jsonContent;
    }
    
    /**
     * This is a getJsonNode method
     *
     * @param configparameters
     * @return String
     */
    public static JsonNode getJsonNode(Map<String, String> configparameters) {
        JsonNode jsonContent = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonContent = mapper.valueToTree(configparameters);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return jsonContent;
    }
    
    /**
     * This is a getJson method
     *
     * @param instance
     * @param pretty
     * @return Map<String , Object>
     */
    public static String getJson(Object instance, boolean pretty) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(Include.NON_NULL);
            if (pretty) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
            }
            return mapper.writeValueAsString(instance);
        } catch (JsonProcessingException e) {
            logger.warn(e.getMessage());
        }
        return null;
    }
    
    /**
     * This is a getJson method
     *
     * @param instance
     * @return String
     */
    public static String getJson(Object instance) {
        return getJson(instance, false);
    }
    
    /**
     * This is a getJsonNodeForobject method
     *
     * @param instance
     * @return JsonNode
     */
    public static JsonNode getJsonNodeForObject(Object instance) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(Include.NON_NULL);
            return mapper.convertValue(instance, JsonNode.class);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return null;
    }
    
    /**
     * This is a getJsonNodeForString method
     *
     * @param content
     * @return JsonNode
     */
    public static JsonNode getJsonNodeForString(String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(Include.NON_NULL);
            return mapper.readTree(content);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return null;
    }
    
    /**
     * This is a getMapfromJson method
     *
     * @param content
     * @return Map<String , Object>
     */
    public static Map<String, Object> getMapfromJson(String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            logger.warn("failed in getMapfromJson for the content ({}) with error message ({}).", content,
                    e.getMessage());
        }
        return null;
    }
    
    /**
     * This is a getMapfromJson method
     *
     * @param content
     * @return Map<String , Object>
     */
    public static Map<String, Object> getMapfromJsonString(String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.warn("failed in getMapfromJson for the content ({}) with error message ({}).", content,
                    e.getMessage());
        }
        return null;
    }
    
    /**
     * This is a getListfromJson method
     *
     * @param content
     * @return Map<String , Object>
     */
    @SuppressWarnings("squid:S1168")
    public static <T> List<T> getListfromJson(String content, Class<T> valueType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class, valueType);
            return mapper.readValue(content, javaType);
        } catch (Exception e) {
            logger.warn("failed in getListfromJson for the content ({}) with error message ({}).", content,
                    e.getMessage());
        }
        return null;
    }
    
    /**
     * This is a getJsonSchema method
     *
     * @param valueType
     * @return String
     */
    public static String getJsonSchema(Class clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
            JsonSchema schema = schemaGen.generateSchema(clazz);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
            
        } catch (Exception e) {
            logger.warn("failed in getJsonSchema  with error message ({}).", e.getMessage());
        }
        return null;
    }
    
    /**
     * This is a readValue method
     *
     * @param content
     * @param valueType
     * @return <T>
     */
    
    public static <T> T readValue(String content, Class<T> valueType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content, valueType);
        } catch (Exception e) {
            logger.warn("failed in readValue for the content ({}) with error message ({}).", content, e.getMessage());
        }
        return null;
    }
    
    /**
     * @param node
     * @param valueType
     * @param <T>
     * @return
     */
    public static <T> T treeToValue(JsonNode node, Class<T> valueType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.treeToValue(node, valueType);
        } catch (Exception e) {
            logger.warn("failed in readValue for the content ({}) with error message ({}).", node, e.getMessage());
        }
        return null;
    }
    
    /**
     * @param node
     * @param valueType
     * @param <T>
     * @return List<T>
     */
    public static <T> List<T> treeToListValue(JsonNode node, Class<T> valueType) {
        List<T> resultList = new ArrayList<>();
        if (node instanceof ArrayNode) {
            for (JsonNode subnode : node) {
                if (subnode != null) {
                    resultList.add(treeToValue(subnode, valueType));
                }
            }
        }
        return resultList;
    }
    
    /**
     * This is a removeJsonNullNode method
     *
     * @param node
     */
    public static void removeJsonNullNode(JsonNode node) {
        Iterator<JsonNode> it = node.iterator();
        while (it.hasNext()) {
            JsonNode child = it.next();
            if (child.isNull()) {
                it.remove();
            } else {
                removeJsonNullNode(child);
            }
        }
    }
    
    public static void printProperty(Properties property) {
        if (property != null) {
            Map<String, String> sortedMap = new TreeMap(property);
            StringBuilder buffer = new StringBuilder();
            sortedMap.entrySet().forEach(message -> {
                buffer.append("\n" + message.toString());
            });
            logger.debug("Property : ({})", buffer);
        }
    }
    
    public static void printMap(Map<String, String> map) {
        if (map != null) {
            Map<String, String> sortedMap = new TreeMap(map);
            StringBuilder buffer = new StringBuilder();
            sortedMap.entrySet().forEach(message -> {
                buffer.append("\n" + message.toString());
            });
            logger.debug("Map: ({})", buffer);
        }
    }
    
    @SuppressWarnings("squid:S00112")
    public static Map<String, String> convertJson2RootProperties(Map<String, String> context, String jsonContent)
            throws Exception {
        if (context == null) {
            context = new HashMap<>();
        }
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootArray = mapper.readTree(jsonContent);
        return convertJson2RootProperties(context, rootArray);
    }
    
    public static Map<String, String> convertJson2RootProperties(Map<String, String> context, JsonNode rootArray) {
        Map<String, String> sortedMap = null;
        
        if (context == null) {
            context = new HashMap<>();
        }
        if (rootArray != null) {
            Iterator<Map.Entry<String, JsonNode>> fields = rootArray.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (entry != null && entry.getValue() != null) {
                    if (entry.getValue().isTextual()) {
                        context.put(entry.getKey(), entry.getValue().textValue());
                    } else {
                        context.put(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
        }
        sortedMap = new TreeMap<>(context);
        return sortedMap;
    }
    
    @SuppressWarnings("squid:S00112")
    public static Map<String, String> convertJson2Properties(Map<String, String> context, String jsonContent,
            List<String> blockKeys) throws Exception {
        Map<String, String> sortedMap = null;
        
        if (context == null) {
            context = new HashMap<>();
        }
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootArray = mapper.readTree(jsonContent);
        
        if (rootArray != null) {
            Iterator<Map.Entry<String, JsonNode>> fields = rootArray.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                processJsonNode(context, blockKeys, entry.getKey(), entry.getValue());
            }
        }
        sortedMap = new TreeMap<>(context);
        return sortedMap;
    }
    
    public static Map<String, String> convertJson2Properties(Map<String, String> context, JsonNode rootArray,
            List<String> blockKeys) throws IOException {
        Map<String, String> sortedMap = null;
        
        if (context == null) {
            context = new HashMap<>();
        }
        
        if (blockKeys == null) {
            blockKeys = new ArrayList<>();
        }
        
        if (rootArray != null) {
            Iterator<Map.Entry<String, JsonNode>> fields = rootArray.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                processJsonNode(context, blockKeys, entry.getKey(), entry.getValue());
            }
        }
        sortedMap = new TreeMap<>(context);
        return sortedMap;
    }
    
    private static void processJsonNode(Map<String, String> propertyMap, List<String> blockKeys, String nodeName,
            JsonNode node) throws IOException {
        
        logger.trace("Block Key ({})", nodeName);
        if (node == null) {
            return;
        }
        
        String keyName = null;
        if (blockKeys != null && blockKeys.contains(nodeName)) {
            if (node.isArray() || node.isObject()) {
                propertyMap.put(nodeName, node.toString());
            } else {
                propertyMap.put(nodeName, node.asText());
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                keyName = nodeName + "[" + i + "]";
                processJsonNode(propertyMap, blockKeys, keyName, node.get(i));
            }
        } else if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                keyName = nodeName + "." + entry.getKey();
                processJsonNode(propertyMap, blockKeys, keyName, entry.getValue());
            }
        } else {
            propertyMap.put(nodeName, node.asText());
        }
    }
    
}

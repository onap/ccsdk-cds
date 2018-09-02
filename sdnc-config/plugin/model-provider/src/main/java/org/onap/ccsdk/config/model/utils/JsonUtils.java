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

import org.onap.ccsdk.config.model.ValidTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JsonUtils.java Purpose: Provide Configuration Generator JsonUtils Information
 *
 * @version 1.0
 */
public class JsonUtils {
    
    private JsonUtils() {
        
    }
    
    public static void populatePrimitiveValues(String key, Object value, String primitiveType, ObjectNode objectNode) {
        if (ValidTypes.DATA_TYPE_BOOLEAN.equals(primitiveType)) {
            objectNode.put(key, (Boolean) value);
        } else if (ValidTypes.DATA_TYPE_INTEGER.equals(primitiveType)) {
            objectNode.put(key, (Integer) value);
        } else if (ValidTypes.DATA_TYPE_FLOAT.equals(primitiveType)) {
            objectNode.put(key, (Float) value);
        } else if (ValidTypes.DATA_TYPE_TIMESTAMP.equals(primitiveType)) {
            objectNode.put(key, (String) value);
        } else {
            objectNode.put(key, (String) value);
        }
    }
    
    public static void populatePrimitiveValues(Object value, String primitiveType, ArrayNode objectNode) {
        if (ValidTypes.DATA_TYPE_BOOLEAN.equals(primitiveType)) {
            objectNode.add((Boolean) value);
        } else if (ValidTypes.DATA_TYPE_INTEGER.equals(primitiveType)) {
            objectNode.add((Integer) value);
        } else if (ValidTypes.DATA_TYPE_FLOAT.equals(primitiveType)) {
            objectNode.add((Float) value);
        } else if (ValidTypes.DATA_TYPE_TIMESTAMP.equals(primitiveType)) {
            objectNode.add((String) value);
        } else {
            objectNode.add((String) value);
        }
    }
    
    public static void populatePrimitiveDefaultValues(String key, String primitiveType, ObjectNode objectNode) {
        if (ValidTypes.DATA_TYPE_BOOLEAN.equals(primitiveType)) {
            objectNode.put(key, false);
        } else if (ValidTypes.DATA_TYPE_INTEGER.equals(primitiveType)) {
            objectNode.put(key, 0);
        } else if (ValidTypes.DATA_TYPE_FLOAT.equals(primitiveType)) {
            objectNode.put(key, 0.0);
        } else {
            objectNode.put(key, "");
        }
    }
    
    public static void populatePrimitiveDefaultValuesForArrayNode(String primitiveType, ArrayNode arrayNode) {
        if (ValidTypes.DATA_TYPE_BOOLEAN.equals(primitiveType)) {
            arrayNode.add(false);
        } else if (ValidTypes.DATA_TYPE_INTEGER.equals(primitiveType)) {
            arrayNode.add(0);
        } else if (ValidTypes.DATA_TYPE_FLOAT.equals(primitiveType)) {
            arrayNode.add(0.0);
        } else {
            arrayNode.add("");
        }
    }
    
    public static void populateJsonNodeValues(String key, JsonNode nodeValue, String type, ObjectNode objectNode) {
        if (nodeValue == null || nodeValue instanceof NullNode) {
            objectNode.put(key, nodeValue);
        } else if (ValidTypes.getPrimitivePropertType().contains(type)) {
            if (ValidTypes.DATA_TYPE_BOOLEAN.equals(type)) {
                objectNode.put(key, nodeValue.asBoolean());
            } else if (ValidTypes.DATA_TYPE_INTEGER.equals(type)) {
                objectNode.put(key, nodeValue.asInt());
            } else if (ValidTypes.DATA_TYPE_FLOAT.equals(type)) {
                objectNode.put(key, nodeValue.floatValue());
            } else if (ValidTypes.DATA_TYPE_TIMESTAMP.equals(type)) {
                objectNode.put(key, nodeValue.asText());
            } else {
                objectNode.put(key, nodeValue.asText());
            }
        } else {
            objectNode.set(key, nodeValue);
        }
    }
    
}

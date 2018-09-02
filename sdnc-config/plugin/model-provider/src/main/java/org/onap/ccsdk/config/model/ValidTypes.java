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

package org.onap.ccsdk.config.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ValidTypes.java Purpose: Provide Configuration Generator ValidTypes
 *
 * @version 1.0
 */
public class ValidTypes {
    
    public static final String DATA_TYPE_STRING = "string";
    public static final String DATA_TYPE_INTEGER = "integer";
    public static final String DATA_TYPE_FLOAT = "float";
    public static final String DATA_TYPE_BOOLEAN = "boolean";
    public static final String DATA_TYPE_TIMESTAMP = "timestamp";
    public static final String DATA_TYPE_NULL = "null";
    public static final String DATA_TYPE_LIST = "list";
    public static final String DATA_TYPE_LONGTEXT = "longtext";
    
    private ValidTypes() {
        
    }
    
    public static List<String> getValidModelTypes() {
        List<String> validTypes = new ArrayList<>();
        validTypes.add(ConfigModelConstant.MODEL_DEFINITION_TYPE_DATA_TYPE);
        validTypes.add(ConfigModelConstant.MODEL_DEFINITION_TYPE_NODE_TYPE);
        validTypes.add(ConfigModelConstant.MODEL_DEFINITION_TYPE_CAPABILITY_TYPE);
        validTypes.add(ConfigModelConstant.MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE);
        return validTypes;
    }
    
    public static List<String> getValidNodeTypes() {
        List<String> validTypes = new ArrayList<>();
        validTypes.add(ConfigModelConstant.MODEL_TYPE_NODE_DG);
        validTypes.add(ConfigModelConstant.MODEL_TYPE_NODE_COMPONENT);
        validTypes.add(ConfigModelConstant.MODEL_TYPE_NODE_VNF);
        validTypes.add(ConfigModelConstant.MODEL_TYPE_NODE_ARTIFACT);
        return validTypes;
    }
    
    public static List<String> getValidDerivedFrom() {
        List<String> validTypes = new ArrayList<>();
        validTypes.add(ConfigModelConstant.MODEL_TYPE_DATA_TYPE_DYNAMIC);
        validTypes.add(ConfigModelConstant.MODEL_TYPE_DATA_TYPE);
        validTypes.add(ConfigModelConstant.MODEL_TYPE_NODE_TYPE);
        return validTypes;
    }
    
    public static List<String> getValidDataTypeDerivedFrom() {
        List<String> validTypes = new ArrayList<>();
        validTypes.add(ConfigModelConstant.MODEL_TYPE_DATA_TYPE_DYNAMIC);
        validTypes.add(ConfigModelConstant.MODEL_TYPE_DATA_TYPE);
        return validTypes;
    }
    
    public static List<String> getValidPropertType() {
        List<String> validTypes = new ArrayList<>();
        validTypes.add(DATA_TYPE_STRING);
        validTypes.add(DATA_TYPE_INTEGER);
        validTypes.add(DATA_TYPE_FLOAT);
        validTypes.add(DATA_TYPE_BOOLEAN);
        validTypes.add(DATA_TYPE_TIMESTAMP);
        validTypes.add(DATA_TYPE_NULL);
        validTypes.add(DATA_TYPE_LIST);
        return validTypes;
    }
    
    public static List<String> getPrimitivePropertType() {
        List<String> validTypes = new ArrayList<>();
        validTypes.add(DATA_TYPE_STRING);
        validTypes.add(DATA_TYPE_INTEGER);
        validTypes.add(DATA_TYPE_FLOAT);
        validTypes.add(DATA_TYPE_BOOLEAN);
        validTypes.add(DATA_TYPE_TIMESTAMP);
        validTypes.add(DATA_TYPE_NULL);
        return validTypes;
    }
    
    public static List<String> getListPropertType() {
        List<String> validTypes = new ArrayList<>();
        validTypes.add(DATA_TYPE_LIST);
        return validTypes;
    }
    
}

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

package org.onap.ccsdk.config.model.data;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CapabilityDefinition.java Purpose: Provide CapabilityDefinition TOSCO Model POJO bean.
 *
 * @version 1.0
 */
public class CapabilityDefinition {
    @JsonIgnore
    private String id;
    private String type;
    private String description;
    private Map<String, PropertyDefinition> properties;
    @JsonProperty("valid_source_types")
    private List<String> validSourceTypes;
    private List<Object> occurrences;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, PropertyDefinition> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, PropertyDefinition> properties) {
        this.properties = properties;
    }
    
    public List<String> getValidSourceTypes() {
        return validSourceTypes;
    }
    
    public void setValidSourceTypes(List<String> validSourceTypes) {
        this.validSourceTypes = validSourceTypes;
    }
    
    public List<Object> getOccurrences() {
        return occurrences;
    }
    
    public void setOccurrences(List<Object> occurrences) {
        this.occurrences = occurrences;
    }
    
}

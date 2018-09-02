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
 * DataType.java Purpose: Provide DataType TOSCO Model POJO bean.
 *
 * @version 1.0
 */
public class DataType {
    @JsonIgnore
    private String id;
    private String version;
    private String description;
    @JsonProperty("derived_from")
    private String derivedFrom;
    private List<Map<String, Object>> constraints;
    private Map<String, PropertyDefinition> properties;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDerivedFrom() {
        return derivedFrom;
    }
    
    public void setDerivedFrom(String derivedFrom) {
        this.derivedFrom = derivedFrom;
    }
    
    public List<Map<String, Object>> getConstraints() {
        return constraints;
    }
    
    public void setConstraints(List<Map<String, Object>> constraints) {
        this.constraints = constraints;
    }
    
    public Map<String, PropertyDefinition> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, PropertyDefinition> properties) {
        this.properties = properties;
    }
    
}

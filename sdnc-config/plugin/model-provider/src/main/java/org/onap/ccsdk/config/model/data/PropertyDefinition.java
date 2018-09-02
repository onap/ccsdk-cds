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

import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * PropertyDefinition.java Purpose: Provide PropertyDefinition TOSCO Model POJO bean.
 *
 * @version 1.0
 */
public class PropertyDefinition {
    @JsonIgnore
    private String id;
    private String description;
    private Boolean required;
    private String type;
    @JsonProperty("default")
    private Object defaultValue;
    private String status;
    private List<HashMap<String, Object>> constraints;
    @JsonProperty("entry_schema")
    private EntrySchema entrySchema;
    private Object value;
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        builder.append(" type :" + getType());
        builder.append(", value :" + getValue());
        builder.append("]");
        return builder.toString();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getRequired() {
        return required;
    }
    
    public void setRequired(Boolean required) {
        this.required = required;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public List<HashMap<String, Object>> getConstraints() {
        return constraints;
    }
    
    public void setConstraints(List<HashMap<String, Object>> constraints) {
        this.constraints = constraints;
    }
    
    public EntrySchema getEntrySchema() {
        return entrySchema;
    }
    
    public void setEntrySchema(EntrySchema entrySchema) {
        this.entrySchema = entrySchema;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
}

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

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * NodeType.java Purpose: Provide NodeType TOSCO Model POJO bean.
 *
 * @version 1.0
 */
public class NodeType {
    @JsonIgnore
    private String id;
    private String description;
    private String version;
    private Map<String, String> metadata;
    @JsonProperty("derived_from")
    private String derivedFrom;
    private Map<String, PropertyDefinition> properties;
    private Map<String, CapabilityDefinition> capabilities;
    private Map<String, RequirementDefinition> requirements;
    private Map<String, InterfaceDefinition> interfaces;
    
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
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public String getDerivedFrom() {
        return derivedFrom;
    }
    
    public void setDerivedFrom(String derivedFrom) {
        this.derivedFrom = derivedFrom;
    }
    
    public Map<String, PropertyDefinition> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, PropertyDefinition> properties) {
        this.properties = properties;
    }
    
    public Map<String, CapabilityDefinition> getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(Map<String, CapabilityDefinition> capabilities) {
        this.capabilities = capabilities;
    }
    
    public Map<String, RequirementDefinition> getRequirements() {
        return requirements;
    }
    
    public void setRequirements(Map<String, RequirementDefinition> requirements) {
        this.requirements = requirements;
    }
    
    public Map<String, InterfaceDefinition> getInterfaces() {
        return interfaces;
    }
    
    public void setInterfaces(Map<String, InterfaceDefinition> interfaces) {
        this.interfaces = interfaces;
    }
    
}

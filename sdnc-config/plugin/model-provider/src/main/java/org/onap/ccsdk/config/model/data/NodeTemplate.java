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

/**
 * NodeTemplate.java Purpose: Provide NodeTemplate TOSCO Model POJO bean.
 *
 * @version 1.0
 */
public class NodeTemplate {
    @JsonIgnore
    private String id;
    private String description;
    private String type;
    private Map<String, String> metadata;
    private Map<String, Object> properties;
    private Map<String, InterfaceAssignment> interfaces;
    private Map<String, ArtifactDefinition> artifacts;
    private Map<String, CapabilityAssignment> capabilities;
    private Map<String, RequirementAssignment> requirements;
    
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
    public Map<String, InterfaceAssignment> getInterfaces() {
        return interfaces;
    }
    
    public void setInterfaces(Map<String, InterfaceAssignment> interfaces) {
        this.interfaces = interfaces;
    }
    
    public Map<String, ArtifactDefinition> getArtifacts() {
        return artifacts;
    }
    
    public void setArtifacts(Map<String, ArtifactDefinition> artifacts) {
        this.artifacts = artifacts;
    }
    
    public Map<String, CapabilityAssignment> getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(Map<String, CapabilityAssignment> capabilities) {
        this.capabilities = capabilities;
    }
    
    public Map<String, RequirementAssignment> getRequirements() {
        return requirements;
    }
    
    public void setRequirements(Map<String, RequirementAssignment> requirements) {
        this.requirements = requirements;
    }
    
}

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
 * TopologyTemplate.java Purpose: Provide TopologyTemplate TOSCO Model POJO bean.
 *
 * @version 1.0
 */
public class TopologyTemplate {
    @JsonIgnore
    private String id;
    private String description;
    private Map<String, PropertyDefinition> inputs;
    @JsonProperty("node_templates")
    private Map<String, NodeTemplate> nodeTemplates;
    @JsonProperty("relationship_templates")
    private Map<String, RelationshipTemplate> relationshipTemplates;
    private Map<String, CapabilityAssignment> capabilities;
    private Map<String, Workflow> workflows;
    
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
    
    public Map<String, PropertyDefinition> getInputs() {
        return inputs;
    }
    
    public void setInputs(Map<String, PropertyDefinition> inputs) {
        this.inputs = inputs;
    }
    
    public Map<String, NodeTemplate> getNodeTemplates() {
        return nodeTemplates;
    }
    
    public void setNodeTemplates(Map<String, NodeTemplate> nodeTemplates) {
        this.nodeTemplates = nodeTemplates;
    }
    
    public Map<String, RelationshipTemplate> getRelationshipTemplates() {
        return relationshipTemplates;
    }
    
    public void setRelationshipTemplates(Map<String, RelationshipTemplate> relationshipTemplates) {
        this.relationshipTemplates = relationshipTemplates;
    }
    
    public Map<String, CapabilityAssignment> getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(Map<String, CapabilityAssignment> capabilities) {
        this.capabilities = capabilities;
    }
    
    public Map<String, Workflow> getWorkflows() {
        return workflows;
    }
    
    public void setWorkflows(Map<String, Workflow> workflows) {
        this.workflows = workflows;
    }
    
}

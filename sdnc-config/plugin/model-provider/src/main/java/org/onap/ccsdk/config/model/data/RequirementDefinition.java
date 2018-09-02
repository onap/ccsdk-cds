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
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * RequirementDefinition.java Purpose: Provide RequirementDefinition TOSCO Model POJO bean.
 *
 * @version 1.0
 */
public class RequirementDefinition {
    @JsonIgnore
    private String id;
    private String description;
    private String capability;
    private String node;
    private String relationship;
    private List<Object> occurrences;
    
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
    
    public String getCapability() {
        return capability;
    }
    
    public void setCapability(String capability) {
        this.capability = capability;
    }
    
    public String getNode() {
        return node;
    }
    
    public void setNode(String node) {
        this.node = node;
    }
    
    public String getRelationship() {
        return relationship;
    }
    
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
    
    public List<Object> getOccurrences() {
        return occurrences;
    }
    
    public void setOccurrences(List<Object> occurrences) {
        this.occurrences = occurrences;
    }
    
}

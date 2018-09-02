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
 * OperationAssignment.java Purpose: Provide OperationAssignment TOSCO Model POJO bean.
 *
 * @version 1.0
 */
public class OperationAssignment {
    @JsonIgnore
    private String id;
    private String description;
    private String implementation;
    private Map<String, Object> inputs;
    private Map<String, Object> outputs;
    
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
    
    public String getImplementation() {
        return implementation;
    }
    
    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }
    
    public Map<String, Object> getInputs() {
        return inputs;
    }
    
    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }
    
    public Map<String, Object> getOutputs() {
        return outputs;
    }
    
    public void setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs;
    }
    
}

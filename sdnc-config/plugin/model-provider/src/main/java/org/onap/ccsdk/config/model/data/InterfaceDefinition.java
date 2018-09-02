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
 * InterfaceAssignment.java Purpose: Provide InterfaceAssignment TOSCO Model POJO bean.
 *
 * @version 1.0
 */
public class InterfaceDefinition {
    @JsonIgnore
    private String id;
    private String type;
    private Map<String, OperationDefinition> operations;
    
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
    
    public Map<String, OperationDefinition> getOperations() {
        return operations;
    }
    
    public void setOperations(Map<String, OperationDefinition> operations) {
        this.operations = operations;
    }
    
}

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

package org.onap.ccsdk.config.model.data.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActionIdentifiers {
    @JsonProperty("service-template-name")
    private String serviceTemplateName;
    @JsonProperty("service-template-version")
    private String serviceTemplateVersion;
    @JsonProperty("action-name")
    private String actionName;
    private String mode;
    
    public String getServiceTemplateName() {
        return serviceTemplateName;
    }
    
    public void setServiceTemplateName(String serviceTemplateName) {
        this.serviceTemplateName = serviceTemplateName;
    }
    
    public String getServiceTemplateVersion() {
        return serviceTemplateVersion;
    }
    
    public void setServiceTemplateVersion(String serviceTemplateVersion) {
        this.serviceTemplateVersion = serviceTemplateVersion;
    }
    
    public String getActionName() {
        return actionName;
    }
    
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
}

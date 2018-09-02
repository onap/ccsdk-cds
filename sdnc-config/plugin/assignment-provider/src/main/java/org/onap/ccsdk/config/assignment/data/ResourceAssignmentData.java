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

package org.onap.ccsdk.config.assignment.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class ResourceAssignmentData {
    
    private String requestId;
    private boolean reloadModel;
    private String resourceType;
    private String resourceId;
    private String serviceTemplateName;
    private String serviceTemplateVersion;
    private String actionName;
    private String inputData;
    private SvcLogicContext svcLogicContext;
    private List<String> templateNames;
    private Map<String, List<ResourceAssignment>> templatesResourceAssignments = new HashMap<>();
    private Map<String, String> templatesContents = new HashMap<>();
    private Map<String, String> templatesMashedContents = new HashMap<>();
    private Map<String, String> templatesData = new HashMap<>();
    private Map<String, Object> context = new HashMap<>();
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
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
    
    public String getInputData() {
        return inputData;
    }
    
    public void setInputData(String inputData) {
        this.inputData = inputData;
    }
    
    public List<String> getTemplateNames() {
        return templateNames;
    }
    
    public void setTemplateNames(List<String> templateNames) {
        this.templateNames = templateNames;
    }
    
    public Map<String, List<ResourceAssignment>> getTemplatesResourceAssignments() {
        return templatesResourceAssignments;
    }
    
    public void setTemplatesResourceAssignments(Map<String, List<ResourceAssignment>> templatesResourceAssignments) {
        this.templatesResourceAssignments = templatesResourceAssignments;
    }
    
    public Map<String, String> getTemplatesContents() {
        return templatesContents;
    }
    
    public void setTemplatesContents(Map<String, String> templatesContents) {
        this.templatesContents = templatesContents;
    }
    
    public Map<String, String> getTemplatesMashedContents() {
        return templatesMashedContents;
    }
    
    public void setTemplatesMashedContents(Map<String, String> templatesMashedContents) {
        this.templatesMashedContents = templatesMashedContents;
    }
    
    public Map<String, String> getTemplatesData() {
        return templatesData;
    }
    
    public void setTemplatesData(Map<String, String> templatesData) {
        this.templatesData = templatesData;
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
    
    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
    
    public SvcLogicContext getSvcLogicContext() {
        return svcLogicContext;
    }
    
    public void setSvcLogicContext(SvcLogicContext svcLogicContext) {
        this.svcLogicContext = svcLogicContext;
    }
    
    public boolean isReloadModel() {
        return reloadModel;
    }
    
    public void setReloadModel(boolean reloadModel) {
        this.reloadModel = reloadModel;
    }
}

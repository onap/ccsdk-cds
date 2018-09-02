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

package org.onap.ccsdk.config.data.adaptor.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ConfigResource implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    private String configResourceId;
    private String resourceId;
    private String resourceType;
    private String serviceTemplateName;
    private String serviceTemplateVersion;
    private String templateName;
    private String recipeName;
    private String requestId;
    private String resourceData;
    private String maskData;
    private Date createdDate = new Date();
    private String status;
    private String updatedBy;
    private List<ResourceAssignmentData> resourceAssignments;
    
    public String getConfigResourceId() {
        return configResourceId;
    }
    
    public void setConfigResourceId(String configResourceId) {
        this.configResourceId = configResourceId;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
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
    
    public String getTemplateName() {
        return templateName;
    }
    
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    public String getRecipeName() {
        return recipeName;
    }
    
    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getResourceData() {
        return resourceData;
    }
    
    public void setResourceData(String resourceData) {
        this.resourceData = resourceData;
    }
    
    public String getMaskData() {
        return maskData;
    }
    
    public void setMaskData(String maskData) {
        this.maskData = maskData;
    }
    
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public List<ResourceAssignmentData> getResourceAssignments() {
        return resourceAssignments;
    }
    
    public void setResourceAssignments(List<ResourceAssignmentData> resourceAssignments) {
        this.resourceAssignments = resourceAssignments;
    }
    
    @Override
    public String toString() {
        return "ConfigResource [configResourceId=" + configResourceId + ", resourceId=" + resourceId
                + ", serviceTemplateName=" + serviceTemplateName + ", serviceTemplateVersion=" + serviceTemplateVersion
                + ", resourceType=" + resourceType + ", templateName=" + templateName + ", recipeName=" + recipeName
                + ", requestId=" + requestId + ", resourceData= ******** , maskData=" + maskData + ", createdDate="
                + createdDate + ", updatedBy=" + updatedBy + "]";
    }
    
    public String getUniqueId() {
        return UUID.randomUUID().toString();
    }
    
}

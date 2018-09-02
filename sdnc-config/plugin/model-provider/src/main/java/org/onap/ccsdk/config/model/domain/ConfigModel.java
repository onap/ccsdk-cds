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

package org.onap.ccsdk.config.model.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ConfigModel implements Serializable {
    
    private Long id;
    private String serviceUUID;
    private String distributionId;
    private String serviceName;
    private String serviceDescription;
    private String resourceUUID;
    private String resourceInstanceName;
    private String resourceName;
    private String resourceVersion;
    private String resourceType;
    private String artifactUUId;
    private String artifactType;
    private String artifactVersion;
    private String artifactDescription;
    private Integer internalVersion;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy KK:mm:ss a Z")
    private Date createdDate = new Date();
    private String artifactName;
    private String published;
    private String updatedBy;
    private String tags;
    @SuppressWarnings("squid:S1948")
    private List<ConfigModelContent> configModelContents;
    
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("[");
        buffer.append("id = " + id);
        buffer.append(", artifactType = " + artifactType);
        buffer.append(", artifactVersion = " + artifactVersion);
        buffer.append(", artifactName = " + artifactName);
        buffer.append(", active = " + published);
        buffer.append("]");
        return buffer.toString();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getServiceUUID() {
        return serviceUUID;
    }
    
    public void setServiceUUID(String serviceUUID) {
        this.serviceUUID = serviceUUID;
    }
    
    public String getDistributionId() {
        return distributionId;
    }
    
    public void setDistributionId(String distributionId) {
        this.distributionId = distributionId;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getServiceDescription() {
        return serviceDescription;
    }
    
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }
    
    public String getResourceUUID() {
        return resourceUUID;
    }
    
    public void setResourceUUID(String resourceUUID) {
        this.resourceUUID = resourceUUID;
    }
    
    public String getResourceInstanceName() {
        return resourceInstanceName;
    }
    
    public void setResourceInstanceName(String resourceInstanceName) {
        this.resourceInstanceName = resourceInstanceName;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    public String getResourceVersion() {
        return resourceVersion;
    }
    
    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getArtifactUUId() {
        return artifactUUId;
    }
    
    public void setArtifactUUId(String artifactUUId) {
        this.artifactUUId = artifactUUId;
    }
    
    public String getArtifactType() {
        return artifactType;
    }
    
    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }
    
    public String getArtifactVersion() {
        return artifactVersion;
    }
    
    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }
    
    public String getArtifactDescription() {
        return artifactDescription;
    }
    
    public void setArtifactDescription(String artifactDescription) {
        this.artifactDescription = artifactDescription;
    }
    
    public Integer getInternalVersion() {
        return internalVersion;
    }
    
    public void setInternalVersion(Integer internalVersion) {
        this.internalVersion = internalVersion;
    }
    
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getArtifactName() {
        return artifactName;
    }
    
    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }
    
    public String getPublished() {
        return published;
    }
    
    public void setPublished(String published) {
        this.published = published;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public List<ConfigModelContent> getConfigModelContents() {
        return configModelContents;
    }
    
    public void setConfigModelContents(List<ConfigModelContent> configModelContents) {
        this.configModelContents = configModelContents;
    }
    
}

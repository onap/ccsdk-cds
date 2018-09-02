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
import java.util.UUID;

public class ResourceAssignmentData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private String id;
    private String configResourceId;
    private Integer version;
    private Date updatedDate = new Date();
    private String updatedBy;
    private String templateKeyName;
    private String resourceName;
    private String dataType;
    private String entrySchema;
    private String resourceValue;
    private String source;
    private String status;
    private String message;
    
    public String getUniqueId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public String toString() {
        return "ResourceAssignmentData [id=" + id + ", configResourceId=" + configResourceId + ", version=" + version
                + ", updatedDate=" + updatedDate + ", updatedBy=" + updatedBy + ", templateKeyName=" + templateKeyName
                + ", resourceName=" + resourceName + ", dataType=" + dataType + ", source=" + source + ", status="
                + status + "]";
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getConfigResourceId() {
        return configResourceId;
    }
    
    public void setConfigResourceId(String configResourceId) {
        this.configResourceId = configResourceId;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public Date getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public String getTemplateKeyName() {
        return templateKeyName;
    }
    
    public void setTemplateKeyName(String templateKeyName) {
        this.templateKeyName = templateKeyName;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    public String getEntrySchema() {
        return entrySchema;
    }
    
    public void setEntrySchema(String entrySchema) {
        this.entrySchema = entrySchema;
    }
    
    public String getResourceValue() {
        return resourceValue;
    }
    
    public void setResourceValue(String resourceValue) {
        this.resourceValue = resourceValue;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
}

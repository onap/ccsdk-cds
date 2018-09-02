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

package org.onap.ccsdk.config.generator.data;

import java.util.List;

public class ConfigGeneratorInfo {
    
    private String requestId;
    private String resourceId;
    private String resourceType;
    private String templateName;
    private String recipeName;
    private String resourceData;
    private String templateContent;
    private String maskData;
    private List<MaskInfo> maskInfos;
    private String mashedData;
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
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
    
    public String getResourceData() {
        return resourceData;
    }
    
    public void setResourceData(String resourceData) {
        this.resourceData = resourceData;
    }
    
    public String getTemplateContent() {
        return templateContent;
    }
    
    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }
    
    public String getMaskData() {
        return maskData;
    }
    
    public void setMaskData(String maskData) {
        this.maskData = maskData;
    }
    
    public String getMashedData() {
        return mashedData;
    }
    
    public void setMashedData(String mashedData) {
        this.mashedData = mashedData;
    }
    
    public List<MaskInfo> getMaskInfos() {
        return maskInfos;
    }
    
    public void setMaskInfos(List<MaskInfo> maskInfos) {
        this.maskInfos = maskInfos;
    }
    
    @Override
    public String toString() {
        return "ConfigGeneratorInfo [requestId=" + requestId + ", resourceId=" + resourceId + ", resourceType="
                + resourceType + ", templateName=" + templateName + ", recipeName=" + recipeName + ", resourceData="
                + resourceData + ", templateContent=" + templateContent + ", maskData=" + maskData + ", maskInfos="
                + maskInfos + ", mashedData=" + mashedData + "]";
    }
    
}

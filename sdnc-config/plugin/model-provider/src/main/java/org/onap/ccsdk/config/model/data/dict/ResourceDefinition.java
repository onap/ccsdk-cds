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

package org.onap.ccsdk.config.model.data.dict;

import org.onap.ccsdk.config.model.data.PropertyDefinition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceDefinition {
    private String tags;
    
    @JsonProperty(value = "name", required = true)
    private String name;
    
    @JsonProperty(value = "property")
    private PropertyDefinition property;
    
    @JsonProperty(value = "description")
    private String description;
    
    @JsonProperty(value = "updated-by")
    private String updatedBy;
    
    @JsonProperty(value = "resource-type", required = true)
    private String resourceType;
    
    @JsonProperty(value = "resource-path", required = true)
    private String resourcePath;
    
    @JsonProperty(value = "sources", required = true)
    private ResourceSources resourceSources;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public PropertyDefinition getProperty() {
        return property;
    }
    
    public void setProperty(PropertyDefinition property) {
        this.property = property;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getResourcePath() {
        return resourcePath;
    }
    
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }
    
    public ResourceSources getSources() {
        return resourceSources;
    }
    
    public void setSources(ResourceSources source) {
        this.resourceSources = source;
    }
    
}

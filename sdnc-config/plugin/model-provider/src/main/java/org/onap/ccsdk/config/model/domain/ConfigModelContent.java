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

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ConfigModelContent {
    
    private Long id;
    private String name;
    private String contentType;
    private String description;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy KK:mm:ss a Z")
    private Date createdDate = new Date();
    
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("[");
        buffer.append("id = " + id);
        buffer.append(", name = " + name);
        buffer.append(", type = " + contentType);
        buffer.append("]");
        return buffer.toString();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String type) {
        this.contentType = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
}

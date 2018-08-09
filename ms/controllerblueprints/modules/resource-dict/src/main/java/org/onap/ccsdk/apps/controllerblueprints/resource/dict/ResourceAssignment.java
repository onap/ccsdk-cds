/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.apps.controllerblueprints.resource.dict;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.onap.ccsdk.apps.controllerblueprints.core.data.PropertyDefinition;

import java.util.Date;
import java.util.List;

/**
 * ResourceAssignment.java Purpose: Provide ResourceAssignment Custom TOSCO Model POJO bean.
 *
 * @author Brinda Santh
 * @version 1.0
 */
public class ResourceAssignment {

    private String name;

    @JsonProperty("property")
    private PropertyDefinition property;

    @JsonProperty("input-param")
    private Boolean inputParameter;

    @JsonProperty("dictionary-name")
    private String dictionaryName;

    @JsonProperty("dictionary-source")
    private String dictionarySource;

    @JsonProperty("dependencies")
    private List<String> dependencies;

    @JsonProperty("version")
    private int version;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("updated-date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date updatedDate;

    @JsonProperty("updated-by")
    private String updatedBy;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        builder.append("name = " + name);
        builder.append(", source = " + dictionarySource);
        if (dependencies != null) {
            builder.append(", dependencies = " + dependencies);
        }
        builder.append("]");
        return builder.toString();
    }

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

    public Boolean getInputParameter() {
        return inputParameter;
    }

    public void setInputParameter(Boolean inputParameter) {
        this.inputParameter = inputParameter;
    }

    public String getDictionaryName() {
        return dictionaryName;
    }

    public void setDictionaryName(String dictionaryName) {
        this.dictionaryName = dictionaryName;
    }

    public String getDictionarySource() {
        return dictionarySource;
    }

    public void setDictionarySource(String dictionarySource) {
        this.dictionarySource = dictionarySource;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

}

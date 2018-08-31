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

package org.onap.ccsdk.apps.controllerblueprints.service.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * DataDictionary.java Purpose: Provide Configuration Generator DataDictionary Entity
 *
 * @author Brinda Santh
 * @version 1.0
 */
@EntityListeners({AuditingEntityListener.class})
@Entity
@Table(name = "RESOURCE_DICTIONARY")
public class ResourceDictionary implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "name", nullable = false)
    @ApiModelProperty(required=true)
    private String name;

    @Column(name = "resource_path", nullable = false)
    @ApiModelProperty(required=true)
    private String resourcePath;

    @Column(name = "resource_type", nullable = false)
    @ApiModelProperty(required=true)
    private String resourceType;

    @Column(name = "data_type", nullable = false)
    @ApiModelProperty(required=true)
    private String dataType;

    @Column(name = "entry_schema")
    private String entrySchema;

    @Lob
    @Column(name = "valid_values")
    private String validValues;

    @Lob
    @Column(name = "sample_value")
    private String sampleValue;

    @Lob
    @Column(name = "definition", nullable = false)
    @ApiModelProperty(required=true)
    private String definition;

    @Lob
    @Column(name = "description", nullable = false)
    @ApiModelProperty(required=true)
    private String description;

    @Lob
    @Column(name = "tags", nullable = false)
    @ApiModelProperty(required=true)
    private String tags;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "updated_by", nullable = false)
    @ApiModelProperty(required=true)
    private String updatedBy;

    @Override
    public String toString() {
        String buffer = "[" + ", name = " + name +
                ", resourcePath = " + resourcePath +
                ", resourceType = " + resourceType +
                ", dataType = " + dataType +
                ", entrySchema = " + entrySchema +
                ", validValues = " + validValues +
                ", definition =" + definition +
                ", description = " + description +
                ", updatedBy = " + updatedBy +
                ", tags = " + tags +
                ", creationDate = " + creationDate +
                "]";
        return buffer;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
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

    public String getValidValues() {
        return validValues;
    }

    public void setValidValues(String validValues) {
        this.validValues = validValues;
    }

    public String getSampleValue() {
        return sampleValue;
    }

    public void setSampleValue(String sampleValue) {
        this.sampleValue = sampleValue;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }



}

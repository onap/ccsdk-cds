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

package org.onap.ccsdk.cds.controllerblueprints.service.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition;
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

    @Column(name = "data_type", nullable = false)
    @ApiModelProperty(required=true)
    private String dataType;

    @Column(name = "entry_schema")
    private String entrySchema;

    @Lob
    @Convert(converter  = JpaResourceDefinitionConverter.class)
    @Column(name = "definition", nullable = false)
    @ApiModelProperty(required=true)
    private ResourceDefinition definition;

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
        return "[" + ", name = " + name +
                ", dataType = " + dataType +
                ", entrySchema = " + entrySchema +
                ", definition =" + definition +
                ", description = " + description +
                ", updatedBy = " + updatedBy +
                ", tags = " + tags +
                ", creationDate = " + creationDate +
                "]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public ResourceDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(ResourceDefinition definition) {
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

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
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * AsdcReference.java Purpose: Provide Configuration Generator AsdcReference Entity
 *
 * @author Brinda Santh
 * @version 1.0
 */
@EntityListeners({AuditingEntityListener.class})
@Entity
@Table(name = "MODEL_TYPE")
public class ModelType implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "model_name", nullable = false)
    @ApiModelProperty(required=true)
    private String modelName;

    @Column(name = "derived_from", nullable = false)
    @ApiModelProperty(required=true)
    private String derivedFrom;

    @Column(name = "definition_type", nullable = false)
    @ApiModelProperty(required=true)
    private String definitionType;

    @Lob
    @Convert(converter  = JpaJsonNodeConverter.class)
    @Column(name = "definition", nullable = false)
    @ApiModelProperty(required=true)
    private JsonNode definition;

    @Lob
    @Column(name = "description", nullable = false)
    @ApiModelProperty(required=true)
    private String description;

    @Column(name = "version", nullable = false)
    @ApiModelProperty(required=true)
    private String version;

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
        return "[" + ", modelName = " + modelName +
                ", derivedFrom = " + derivedFrom +
                ", definitionType = " + definitionType +
                ", description = " + description +
                ", creationDate = " + creationDate +
                ", version = " + version +
                ", updatedBy = " + updatedBy +
                ", tags = " + tags +
                "]";
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(String derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    public String getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(String definitionType) {
        this.definitionType = definitionType;
    }

    public JsonNode getDefinition() {
        return definition;
    }

    public void setDefinition(JsonNode definition) {
        this.definition = definition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

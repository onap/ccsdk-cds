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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * DataDictionary.java Purpose: Provide Configuration Generator DataDictionary Entity
 *
 * @author Brinda Santh
 * @version 1.0
 */
@EntityListeners({AuditingEntityListener.class})
@Entity
@Table(name = "CONFIG_MODEL_CONTENT")
public class ConfigModelContent {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_model_content_id")
    private Long id;

    @Column(name = "name", nullable = false)
    @ApiModelProperty(required=true)
    private String name;

    @Column(name = "content_type", nullable = false)
    @ApiModelProperty(required=true)
    private String contentType;


    @ManyToOne
    @JoinColumn(name = "config_model_id")
    @JsonBackReference
    private ConfigModel configModel;

    @Lob
    @Column(name = "description")
    private String description;

    @Deprecated
    @Lob
    @Column(name = "content", nullable = false)
    @ApiModelProperty(required=true)
    private String content;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_date")
    private Date creationDate;

    @Override
    public String toString() {
        return "[" + "id = " + id +
                ", name = " + name +
                ", contentType = " + contentType +
                "]";
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }
        if (!(o instanceof ConfigModelContent)) {
            return false;
        }
        ConfigModelContent configModelContent = (ConfigModelContent) o;
        return Objects.equals(id, configModelContent.id) && Objects.equals(name, configModelContent.name)
                && Objects.equals(contentType, configModelContent.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, contentType);
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


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public ConfigModel getConfigModel() {
        return configModel;
    }


    public void setConfigModel(ConfigModel configModel) {
        this.configModel = configModel;
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


    public Date getCreationDate() {
        return creationDate;
    }


    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

}

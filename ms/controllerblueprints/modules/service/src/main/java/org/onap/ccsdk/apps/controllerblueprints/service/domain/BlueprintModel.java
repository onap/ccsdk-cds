/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
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
import org.hibernate.annotations.Proxy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * BlueprintModel.java Purpose: Provide Configuration Generator BlueprintModel Entity
 *
 * @author Brinda Santh
 * @version 1.0
 */

@EntityListeners({AuditingEntityListener.class})
@Entity
@Table(name = "CONFIG_MODEL", uniqueConstraints=@UniqueConstraint(columnNames={"artifact_name","artifact_version"}))
@Proxy(lazy=false)
public class BlueprintModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "config_model_id")
    private String id;

    @Column(name = "service_uuid")
    private String serviceUUID;

    @Column(name = "distribution_id")
    private String distributionId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "service_description")
    private String serviceDescription;

    @Column(name = "resource_uuid")
    private String resourceUUID;

    @Column(name = "resource_instance_name")
    private String resourceInstanceName;

    @Column(name = "resource_name")
    private String resourceName;

    @Column(name = "resource_version")
    private String resourceVersion;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "artifact_uuid")
    private String artifactUUId;

    @Column(name = "artifact_type")
    private String artifactType;

    @Column(name = "artifact_version", nullable = false)
    @ApiModelProperty(required=true)
    private String artifactVersion;

    @Lob
    @Column(name = "artifact_description")
    private String artifactDescription;

    @Column(name = "internal_version")
    private Integer internalVersion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    private Date createdDate = new Date();

    @Column(name = "artifact_name", nullable = false)
    @ApiModelProperty(required=true)
    private String artifactName;

    @Column(name = "published", nullable = false)
    @ApiModelProperty(required=true)
    private String published;

    @Column(name = "updated_by", nullable = false)
    @ApiModelProperty(required=true)
    private String updatedBy;

    @Lob
    @Column(name = "tags", nullable = false)
    @ApiModelProperty(required=true)
    private String tags;

    @OneToOne(mappedBy = "blueprintModel", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private BlueprintModelContent blueprintModelContent;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public BlueprintModelContent getBlueprintModelContent() {
        return blueprintModelContent;
    }

    public void setBlueprintModelContent(BlueprintModelContent blueprintModelContent) {
        this.blueprintModelContent = blueprintModelContent;
    }
}

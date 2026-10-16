/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.Length
import org.springframework.data.annotation.LastModifiedDate
import java.io.Serializable
import java.util.Date
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType

/**
 * Provide Blueprint Model Search Entity
 *
 * @author Brinda Santh
 * @version 1.0
 */

@Entity
@Table(name = "BLUEPRINT_MODEL")
@JsonTypeName("blueprintModel")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
class BlueprintModelSearch : Serializable {

    @Schema(
        description = "ID of Blueprint model, is automatically created by CDS",
        example = "658f9a48-7f54-41ba-ae18-c69f26f3dc94",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Id
    @Column(name = "blueprint_model_id")
    var id: String? = null

    @Schema(description = "Artifact UUID, usually null", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Column(name = "artifact_uuid")
    var artifactUUId: String? = null

    @JsonSerialize
    @Schema(description = "Artifact Type, usually null", example = "SDNC_MODEL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Column(name = "artifact_type")
    var artifactType: String? = null

    @Schema(description = "Artifact Version, usually 1.0.0", example = "1.0.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "artifact_version", nullable = false)
    var artifactVersion: String? = null

    @Schema(description = "Artifact Description, usually empty", example = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Column(name = "artifact_description", length = Length.LONG32)
    var artifactDescription: String? = null

    @Schema(description = "Internal Version of CBA, usually null", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Column(name = "internal_version")
    var internalVersion: Int? = null

    @Schema(description = "Datetime of the creation of CBA in CDS", example = "2020-11-19T10:34:56.000Z", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    var createdDate = Date()

    @Schema(description = "Artifact Name, defined in Metadata", example = "pnf_netconf", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "artifact_name", nullable = false)
    var artifactName: String? = null

    @Schema(description = "Artifact Name, defined in Metadata", example = "pnf_netconf", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "published", nullable = false)
    var published: String? = null

    @Schema(description = "Name of publisher, defined in Metadata", example = "Deutsche Telekom AG", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "updated_by", nullable = false)
    var updatedBy: String? = null

    @Schema(description = "Tags to identify the CBA, defined in Metadata", example = "test", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "tags", nullable = false, columnDefinition = "TEXT")
    var tags: String? = null

    companion object {

        const val serialVersionUID = 1L
    }
}

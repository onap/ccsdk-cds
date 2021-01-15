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
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.springframework.data.annotation.LastModifiedDate
import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

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
@ApiModel
class BlueprintModelSearch : Serializable {

    @ApiModelProperty(
        value = "ID of Blueprint model, is automatically created by CDS",
        example = "\"658f9a48-7f54-41ba-ae18-c69f26f3dc94\"",
        required = true
    )
    @Id
    @Column(name = "blueprint_model_id")
    var id: String? = null

    @ApiModelProperty(value = "Artifact UUID, usually null", example = "null", required = false)
    @Column(name = "artifact_uuid")
    var artifactUUId: String? = null

    @JsonSerialize
    @ApiModelProperty(value = "Artifact Type, usually null", example = "\"SDNC_MODEL\"", required = false)
    @Column(name = "artifact_type")
    var artifactType: String? = null

    @ApiModelProperty(value = "Artifact Version, usually 1.0.0", example = "\"1.0.0\"", required = true)
    @Column(name = "artifact_version", nullable = false)
    var artifactVersion: String? = null

    @ApiModelProperty(value = "Artifact Description, usually empty", example = "\"\"", required = false)
    @Lob
    @Column(name = "artifact_description")
    var artifactDescription: String? = null

    @ApiModelProperty(value = "Internal Version of CBA, usually null", example = "null", required = false)
    @Column(name = "internal_version")
    var internalVersion: Int? = null

    @ApiModelProperty(value = "Datetime of the creation of CBA in CDS", example = "\"2020-11-19T10:34:56.000Z\"", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    var createdDate = Date()

    @ApiModelProperty(value = "Artifact Name, defined in Metadata", example = "\"pnf_netconf\"", required = true)
    @Column(name = "artifact_name", nullable = false)
    var artifactName: String? = null

    @ApiModelProperty(value = "Artifact Name, defined in Metadata", example = "\"pnf_netconf\"", required = true)
    @Column(name = "published", nullable = false)
    var published: String? = null

    @ApiModelProperty(value = "Name of publisher, defined in Metadata", example = "\"Deutsche Telekom AG\"", required = true)
    @Column(name = "updated_by", nullable = false)
    var updatedBy: String? = null

    @ApiModelProperty(value = "Tags to identify the CBA, defined in Metadata", example = "\"test\"", required = true)
    @Lob
    @Column(name = "tags", nullable = false)
    var tags: String? = null

    companion object {

        const val serialVersionUID = 1L
    }
}

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
class BlueprintModelSearch : Serializable {

    @Id
    @Column(name = "blueprint_model_id")
    var id: String? = null

    @Column(name = "artifact_uuid")
    var artifactUUId: String? = null

    @Column(name = "artifact_type")
    var artifactType: String? = null

    @Column(name = "artifact_version", nullable = false)
    var artifactVersion: String? = null

    @Lob
    @Column(name = "artifact_description")
    var artifactDescription: String? = null

    @Column(name = "internal_version")
    var internalVersion: Int? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    var createdDate = Date()

    @Column(name = "artifact_name", nullable = false)
    var artifactName: String? = null

    @Column(name = "published", nullable = false)
    var published: String? = null

    @Column(name = "updated_by", nullable = false)
    var updatedBy: String? = null

    @Lob
    @Column(name = "tags", nullable = false)
    var tags: String? = null

    companion object {

        const val serialversionuid = 1L
    }
}

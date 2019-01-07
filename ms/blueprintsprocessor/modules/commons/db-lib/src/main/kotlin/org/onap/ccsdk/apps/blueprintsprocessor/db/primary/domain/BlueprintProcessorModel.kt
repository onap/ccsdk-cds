/*
 * Copyright © 2019 Bell Canada
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

package org.onap.ccsdk.apps.blueprintsprocessor.db.primary.domain

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModelProperty
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Table
import org.hibernate.annotations.Proxy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.OneToOne
import javax.persistence.Temporal
import javax.persistence.TemporalType

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "BLUEPRINT_RUNTIME")
@Proxy(lazy = false)
class BlueprintProcessorModel : Serializable {
    @Id
    @Column(name = "config_model_id")
    var id: String? = null

    @Column(name = "artifact_uuid")
    var artifactUUId: String? = null

    @Column(name = "artifact_type")
    var artifactType: String? = null

    @Column(name = "artifact_version", nullable = false)
    @ApiModelProperty(required = true)
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
    @ApiModelProperty(required = true)
    var artifactName: String? = null

    @Column(name = "published", nullable = false)
    @ApiModelProperty(required = true)
    var published: String? = null

    @Column(name = "updated_by", nullable = false)
    @ApiModelProperty(required = true)
    var updatedBy: String? = null

    @Lob
    @Column(name = "tags", nullable = false)
    @ApiModelProperty(required = true)
    var tags: String? = null

    @OneToOne(mappedBy = "blueprintModel", fetch = FetchType.EAGER, orphanRemoval = true, cascade = [CascadeType.ALL])
    var blueprintModelContent: BlueprintProcessorModelContent? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}
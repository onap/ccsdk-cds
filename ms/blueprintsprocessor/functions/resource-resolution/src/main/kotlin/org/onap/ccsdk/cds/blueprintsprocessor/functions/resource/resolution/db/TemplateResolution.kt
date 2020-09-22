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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModelProperty
import org.hibernate.annotations.Proxy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "TEMPLATE_RESOLUTION")
@Proxy(lazy = false)
class TemplateResolution : Serializable {

    @get:ApiModelProperty(value = "Name of the CBA.", required = true)
    @Column(name = "blueprint_name", nullable = false)
    var blueprintName: String? = null

    @get:ApiModelProperty(value = "Version of the CBA.", required = true)
    @Column(name = "blueprint_version", nullable = false)
    var blueprintVersion: String? = null

    @get:ApiModelProperty(value = "Artifact name for which to retrieve a resolved resource.", required = true)
    @Column(name = "artifact_name", nullable = false)
    var artifactName: String? = null

    @get:ApiModelProperty(value = "Rendered template.", required = true)
    @Lob
    @Column(name = "result", nullable = false)
    var result: String? = null

    @get:ApiModelProperty(
        value = "Resolution Key uniquely identifying the resolution of a given artifact within a CBA.",
        required = true
    )
    @Column(name = "resolution_key", nullable = false)
    var resolutionKey: String? = null

    @get:ApiModelProperty(value = "Resolution type.", required = true, example = "ServiceInstance, VfModule, VNF")
    @Column(name = "resource_type", nullable = false)
    var resourceType: String? = null

    @get:ApiModelProperty(value = "ID associated with the resolution type in the inventory system.", required = true)
    @Column(name = "resource_id", nullable = false)
    var resourceId: String? = null

    @get:ApiModelProperty(
        value = "If resolution occurred multiple time, this field provides the index.",
        required = true
    )
    @Column(name = "occurrence", nullable = false)
    var occurrence: Int = 1

    @Id
    @Column(name = "template_resolution_id")
    var id: String? = null

    @get:ApiModelProperty(value = "Creation date of the resolution.", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    var createdDate = Date()

    companion object {

        private const val serialVersionUID = 1L
    }
}

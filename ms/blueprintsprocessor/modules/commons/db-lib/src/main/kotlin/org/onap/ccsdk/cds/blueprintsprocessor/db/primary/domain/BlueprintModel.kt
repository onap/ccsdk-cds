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
import io.swagger.annotations.ApiModelProperty
import org.hibernate.annotations.Proxy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.util.Date
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.UniqueConstraint

/**
 *  Provide BlueprintModel Entity
 *
 * @author Brinda Santh
 * @version 1.0
 */

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "BLUEPRINT_MODEL", uniqueConstraints = [UniqueConstraint(columnNames = ["artifact_name", "artifact_version"])])
@Proxy(lazy = false)
class BlueprintModel : Serializable {

    @Id
    @Column(name = "blueprint_model_id")
    var id: String? = null

    @Column(name = "service_uuid")
    var serviceUUID: String? = null

    @Column(name = "distribution_id")
    var distributionId: String? = null

    @Column(name = "service_name")
    var serviceName: String? = null

    @Column(name = "service_description")
    var serviceDescription: String? = null

    @Column(name = "resource_uuid")
    var resourceUUID: String? = null

    @Column(name = "resource_instance_name")
    var resourceInstanceName: String? = null

    @Column(name = "resource_name")
    var resourceName: String? = null

    @Column(name = "resource_version")
    var resourceVersion: String? = null

    @Column(name = "resource_type")
    var resourceType: String? = null

    @Column(name = "artifact_uuid")
    var artifactUUId: String? = null

    @Column(name = "artifact_type")
    var artifactType: String? = null

    @Column(name = "artifact_version", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var artifactVersion: String

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
    lateinit var artifactName: String

    @Column(name = "published", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var published: String

    @Column(name = "updated_by", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var updatedBy: String

    @Lob
    @Column(name = "tags", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var tags: String

    @OneToOne(mappedBy = "blueprintModel", fetch = FetchType.EAGER, orphanRemoval = true, cascade = [CascadeType.ALL])
    var blueprintModelContent: BlueprintModelContent? = null

    companion object {

        private const val serialVersionUID = 1L
    }
}

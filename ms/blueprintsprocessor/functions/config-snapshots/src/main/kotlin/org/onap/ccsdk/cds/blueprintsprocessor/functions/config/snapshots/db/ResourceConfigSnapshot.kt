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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db

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

/**
 * ResourceConfigSnapshot model
 * Stores RUNNING or CANDIDATE resource configuration snapshots, captured during the execution
 * of blueprints.  A resource is identified by an identifier and a type.
 *
 * @author Serge Simard
 * @version 1.0
 */
@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "RESOURCE_CONFIG_SNAPSHOT")
@Proxy(lazy = false)
class ResourceConfigSnapshot : Serializable {

    @get:ApiModelProperty(value = "Resource type.", required = true, example = "\"ServiceInstance\"")
    @Column(name = "resource_type", nullable = false)
    var resourceType: String? = null

    @get:ApiModelProperty(value = "ID associated with the resource type in the inventory system.", required = true)
    @Column(name = "resource_id", nullable = false)
    var resourceId: String? = null

    @get:ApiModelProperty(value = "Status of the snapshot, either running or candidate.", required = true)
    @Column(name = "status", nullable = false)
    var status: Status? = null

    @get:ApiModelProperty(value = "Snapshot of the resource as retrieved from resource.", required = true)
    @Lob
    @Column(name = "config_snapshot", nullable = false)
    var config_snapshot: String? = null

    @Id
    @Column(name = "resource_config_snapshot_id")
    var id: String? = null

    @get:ApiModelProperty(value = "Creation date of the snapshot.", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    var createdDate = Date()

    companion object {

        private const val serialVersionUID = 1L
    }

    enum class Status(val state: String) {
        RUNNING("RUNNING"),
        CANDIDATE("CANDIDATE")
    }
}

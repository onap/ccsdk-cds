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
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.annotations.Proxy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.util.*
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
@Table(name = "RESOURCE_RESOLUTION")
@Proxy(lazy = false)
class ResourceResolution : Serializable {

    @Id
    @Column(name = "resource_resolution_id")
    var id: String? = null

    @Column(name = "resolution_key", nullable = false)
    var resolutionKey: String? = null

    @Column(name = "blueprint_name", nullable = false)
    var blueprintName: String? = null

    @Column(name = "blueprint_version", nullable = false)
    var blueprintVersion: String? = null

    @Column(name = "artifact_name", nullable = false)
    var artifactName: String? = null

    @Column(name = "status", nullable = false)
    var status: String? = null

    @Column(name = "name", nullable = false)
    var name: String? = null

    @Column(name = "dictionary_vname", nullable = false)
    var dictionaryName: String? = null

    @Column(name = "dictionary_status", nullable = false)
    var dictionarySource: String? = null

    @Column(name = "dictionary_version", nullable = false)
    var dictionaryVersion: Int = 0

    @Lob
    @Column(name = "value", nullable = false)
    var value: String? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    var createdDate = Date()

    companion object {
        private const val serialVersionUID = 1L
    }
}
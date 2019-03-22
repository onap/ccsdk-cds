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
import java.io.Serializable
import java.util.Date
import java.util.Objects
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "BLUEPRINT_CONTENT_RUNTIME")
class BlueprintProcessorModelContent : Serializable {

    @Id
    @Column(name = "blueprint_content_runtime_id")
    var id: String? = null

    @Column(name = "name", nullable = false)
    @ApiModelProperty(required = true)
    var name: String? = null

    @Column(name = "content_type", nullable = false)
    @ApiModelProperty(required = true)
    var contentType: String? = null

    @OneToOne
    @JoinColumn(name = "blueprint_runtime_id")
    var blueprintModel: BlueprintProcessorModel? = null

    @Lob
    @Column(name = "description")
    var description: String? = null

    @Lob
    @Column(name = "content", nullable = false)
    @ApiModelProperty(required = true)
    var content: ByteArray? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_date")
    var creationDate = Date()

    override fun toString(): String {
        return "[" + "id = " + id +
                ", name = " + name +
                ", contentType = " + contentType +
                "]"
    }

    override fun equals(o: Any?): Boolean {

        if (o === this) {
            return true
        }
        if (o !is BlueprintProcessorModelContent) {
            return false
        }
        val blueprintModelContent = o as BlueprintProcessorModelContent?
        return (id == blueprintModelContent!!.id && name == blueprintModelContent.name
                && contentType == blueprintModelContent.contentType)
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name, contentType)
    }

    companion object {
        private const val serialVersionUID = 1L
    }

}

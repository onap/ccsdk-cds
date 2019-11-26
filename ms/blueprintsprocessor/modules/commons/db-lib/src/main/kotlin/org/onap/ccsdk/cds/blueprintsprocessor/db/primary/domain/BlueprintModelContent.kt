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
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.annotations.ApiModelProperty
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.util.*
import javax.persistence.*

/**
 * Provide Blueprint Model Content Entity
 *
 * @author Brinda Santh
 * @version 1.0
 */
@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "BLUEPRINT_MODEL_CONTENT")
class BlueprintModelContent : Serializable {

    @Id
    @Column(name = "blueprint_model_content_id")
    var id: String? = null

    @Column(name = "name", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var name: String

    @Column(name = "content_type", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var contentType: String

    @OneToOne
    @JoinColumn(name = "blueprint_model_id")
    @JsonIgnore
    var blueprintModel: BlueprintModel? = null

    @Lob
    @Column(name = "description")
    var description: String? = null

    @Lob
    @Column(name = "content", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var content: ByteArray

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
        if (o !is BlueprintModelContent) {
            return false
        }
        val blueprintModelContent = o as BlueprintModelContent?
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

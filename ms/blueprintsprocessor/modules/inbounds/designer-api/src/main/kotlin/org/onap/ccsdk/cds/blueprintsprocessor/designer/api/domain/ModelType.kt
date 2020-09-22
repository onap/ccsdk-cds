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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.JsonNode
import io.swagger.annotations.ApiModelProperty
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

/**
 * Provide ModelType Entity
 *
 * @author Brinda Santh
 * @version 1.0
 */
@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "MODEL_TYPE")
class ModelType : Serializable {

    @Id
    @Column(name = "model_name", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var modelName: String

    @Column(name = "derived_from", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var derivedFrom: String

    @Column(name = "definition_type", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var definitionType: String

    @Lob
    @Convert(converter = JpaJsonNodeConverter::class)
    @Column(name = "definition", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var definition: JsonNode

    @Lob
    @Column(name = "description", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var description: String

    @Column(name = "version", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var version: String

    @Lob
    @Column(name = "tags", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var tags: String

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    var creationDate: Date? = null

    @Column(name = "updated_by", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var updatedBy: String

    override fun toString(): String {
        return "[" + "modelName = " + modelName +
            ", derivedFrom = " + derivedFrom +
            ", definitionType = " + definitionType +
            ", description = " + description +
            ", creationDate = " + creationDate +
            ", version = " + version +
            ", updatedBy = " + updatedBy +
            ", tags = " + tags +
            "]"
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}

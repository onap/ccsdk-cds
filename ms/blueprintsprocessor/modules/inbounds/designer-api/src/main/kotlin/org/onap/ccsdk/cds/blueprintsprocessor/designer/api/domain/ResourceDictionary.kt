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
import io.swagger.annotations.ApiModelProperty
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
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
 * Provide ResourceDictionary Entity
 *
 * @author Brinda Santh
 * @version 1.0
 */
@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "RESOURCE_DICTIONARY")
class ResourceDictionary : Serializable {

    @Id
    @Column(name = "name", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var name: String

    @Column(name = "data_type", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var dataType: String

    @Column(name = "entry_schema")
    var entrySchema: String? = null

    @Column(name = "resource_dictionary_group")
    @ApiModelProperty(required = true)
    var resourceDictionaryGroup: String? = null

    @Lob
    @Convert(converter = JpaResourceDefinitionConverter::class)
    @Column(name = "definition", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var definition: ResourceDefinition

    @Lob
    @Column(name = "description", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var description: String

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
        return "[" + ", name = " + name +
            ", dataType = " + dataType +
            ", entrySchema = " + entrySchema +
            ", resourceDictionaryGroup = " + resourceDictionaryGroup +
            ", definition =" + definition +
            ", description = " + description +
            ", updatedBy = " + updatedBy +
            ", tags = " + tags +
            ", creationDate = " + creationDate +
            "]"
    }

    companion object {

        private const val serialVersionUID = 1L
    }
}

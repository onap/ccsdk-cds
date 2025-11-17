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
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.util.Date
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType

/**
 * Provide ResourceDictionary Entity
 *
 * @author Brinda Santh
 * @version 1.0
 */
@EntityListeners(AuditingEntityListener::class)
@ApiModel
@Entity
@Table(name = "RESOURCE_DICTIONARY")
class ResourceDictionary : Serializable {

    @Id
    @Column(name = "name", nullable = false)
    @ApiModelProperty(value = "Name", required = true, example = "\"sample-db-source\"")
    lateinit var name: String

    @Column(name = "data_type", nullable = false)
    @ApiModelProperty(value = "Data type", required = true, example = "\"string\"")
    lateinit var dataType: String

    @Column(name = "entry_schema")
    @ApiModelProperty(value = "Entry schema", required = true, example = "\"dt-license-key\"")
    var entrySchema: String? = null

    @Column(name = "resource_dictionary_group")
    @ApiModelProperty(value = "Resource dictionary group", required = true, example = "\"default\"")
    var resourceDictionaryGroup: String? = null

    @Lob
    @Convert(converter = JpaResourceDefinitionConverter::class)
    @Column(name = "definition", nullable = false, columnDefinition = "LONGTEXT")
    @ApiModelProperty(value = "Definition", required = true)
    lateinit var definition: ResourceDefinition

    @Lob
    @Column(name = "description", nullable = false, columnDefinition = "LONGTEXT")
    @ApiModelProperty(value = "Description", required = true, example = "\"demo_artifacts_version\"")
    lateinit var description: String

    @Column(name = "tags", nullable = false, columnDefinition = "LONGTEXT")
    @ApiModelProperty(value = "Tags", required = true, example = "\"hostname\"")
    lateinit var tags: String

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    var creationDate: Date? = null

    @Column(name = "updated_by", nullable = false)
    @ApiModelProperty(value = "Updated by", required = true, example = "\"username\"")
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

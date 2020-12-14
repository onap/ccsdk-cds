/*
 *  Copyright © 2018 IBM.
 *  Modifications Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.controllerblueprints.resource.dict

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import java.io.Serializable
import java.util.Date

@ApiModel
open class ResourceDefinition {

    @JsonProperty(value = "name", required = true)
    @ApiModelProperty(value = "Name", required = true, example = "\"default-source\"")
    lateinit var name: String

    @JsonProperty(value = "property", required = true)
    @ApiModelProperty(value = "Property", required = true)
    lateinit var property: PropertyDefinition

    var tags: String? = null

    /** The default group for Resource Definition is "default" */
    @JsonProperty(value = "group", required = true)
    @ApiModelProperty(value = "Group", required = true, example = "\"default\"")
    var group: String = "default"

    @JsonProperty(value = "updated-by")
    @ApiModelProperty(value = "Updated by", required = true, example = "\"example@onap.com\"")
    lateinit var updatedBy: String

    @JsonProperty(value = "sources", required = true)
    @ApiModelProperty(value = "Sources", required = true, example = "\"sources\"")
    lateinit var sources: MutableMap<String, NodeTemplate>
}

open class ResourceAssignment {

    @JsonProperty(value = "name", required = true)
    lateinit var name: String

    @JsonProperty(value = "property")
    var property: PropertyDefinition? = null

    @JsonProperty("input-param")
    var inputParameter: Boolean = false

    @JsonProperty("dictionary-name")
    var dictionaryName: String? = null

    @JsonProperty("dictionary-source")
    var dictionarySource: String? = null

    /** Modified Source definition,  Capability Source will use for script reference changes,
     * Rest Source will use for extra headers etc **/
    @JsonProperty("dictionary-source-definition")
    var dictionarySourceDefinition: NodeTemplate? = null

    /** Duplicate field : Shall be used directly from Dictionary Source definition dependencies. **/
    @JsonProperty("dependencies")
    var dependencies: MutableList<String>? = null

    @JsonProperty("version")
    var version: Int = 0

    @JsonProperty("status")
    var status: String? = null

    @JsonProperty("message")
    var message: String? = null

    @JsonProperty("updated-date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var updatedDate: Date? = null

    @JsonProperty("updated-by")
    var updatedBy: String? = null

    /** input & output key-mapping with their resolved values **/
    var keyIdentifiers: MutableList<KeyIdentifier> = mutableListOf()

    override fun toString(): String {
        return """
            [
                name = $name
                status = $status
                required = ${property?.required}
                dependencies = $dependencies
                dictionaryName = $dictionaryName
                dictionarySource = $dictionarySource
            ]
        """.trimIndent()
    }
}

data class KeyIdentifier(val name: String, val value: JsonNode)
data class DictionaryMetadataEntry(val name: String, val value: String)

/**
 * Data class for exposing summary of resource resolution
 */
data class ResolutionSummary(
    val name: String,
    val value: JsonNode,
    val required: Boolean,
    val type: String,
    @JsonProperty("key-identifiers")
    val keyIdentifiers: MutableList<KeyIdentifier>,
    @JsonProperty("dictionary-description")
    val dictionaryDescription: String,
    @JsonProperty("dictionary-metadata")
    val dictionaryMetadata: MutableList<DictionaryMetadataEntry>,
    @JsonProperty("dictionary-name")
    val dictionaryName: String,
    @JsonProperty("dictionary-source")
    val dictionarySource: String,
    @JsonProperty("request-payload")
    val requestPayload: JsonNode,
    @JsonProperty("status")
    val status: String,
    @JsonProperty("message")
    val message: String
)

/**
 * Interface for Source Definitions (ex Input Source,
 * Default Source, Database Source, Rest Sources, etc)
 */
interface ResourceSource : Serializable

open class ResourceSourceMapping {

    lateinit var resourceSourceMappings: MutableMap<String, String>
}

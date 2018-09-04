/*
 *  Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.apps.controllerblueprints.core.data.PropertyDefinition
import java.io.Serializable
import java.util.*

open class ResourceDefinition{

    @JsonProperty(value = "name", required = true)
    lateinit var name: String

    @JsonProperty(value = "property", required = true)
    lateinit var property : PropertyDefinition

    var tags: String? = null

    @JsonProperty(value = "updated-by")
    lateinit var updatedBy: String

    @JsonProperty(value = "resource-type", required = true)
    lateinit var resourceType: String

    @JsonProperty(value = "resource-path", required = true)
    lateinit var resourcePath: String

    @JsonProperty(value = "sources", required = true)
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
}

/**
 * Interface for Source Definitions (ex Input Source,
 * Default Source, Database Source, Rest Sources, etc)
 */
interface ResourceSource : Serializable

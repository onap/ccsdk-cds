/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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
@file:Suppress("unused")

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import com.fasterxml.jackson.annotation.JsonProperty

open class ResourceSourceProperties

open class InputResourceSource : ResourceSourceProperties() {

    lateinit var key: String

    @get:JsonProperty("key-dependencies")
    lateinit var keyDependencies: MutableList<String>
}

open class DefaultResourceSource : ResourceSourceProperties() {

    lateinit var key: String

    @get:JsonProperty("key-dependencies")
    lateinit var keyDependencies: MutableList<String>
}

open class DatabaseResourceSource : ResourceSourceProperties() {

    lateinit var type: String

    @get:JsonProperty("endpoint-selector")
    var endpointSelector: String? = null
    lateinit var query: String

    @get:JsonProperty("input-key-mapping")
    var inputKeyMapping: MutableMap<String, String>? = null

    @get:JsonProperty("output-key-mapping")
    var outputKeyMapping: MutableMap<String, String>? = null

    @get:JsonProperty("key-dependencies")
    lateinit var keyDependencies: MutableList<String>
}

open class RestResourceSource : ResourceSourceProperties() {

    lateinit var verb: String

    @get:JsonProperty("payload")
    var payload: String? = null

    @get:JsonProperty("resolved-payload")
    var resolvedPayload: String? = null
    lateinit var type: String

    @get:JsonProperty("endpoint-selector")
    var endpointSelector: String? = null

    @get:JsonProperty("url-path")
    lateinit var urlPath: String
    lateinit var path: String

    @get:JsonProperty("expression-type")
    lateinit var expressionType: String

    @get:JsonProperty("input-key-mapping")
    var inputKeyMapping: MutableMap<String, String>? = null

    @get:JsonProperty("output-key-mapping")
    var outputKeyMapping: MutableMap<String, String>? = null

    @get:JsonProperty("headers")
    var headers: Map<String, String> = emptyMap()

    @get:JsonProperty("key-dependencies")
    lateinit var keyDependencies: MutableList<String>
}

open class CapabilityResourceSource : ResourceSourceProperties() {

    @get:JsonProperty("script-type")
    lateinit var scriptType: String

    @get:JsonProperty("script-class-reference")
    lateinit var scriptClassReference: String

    @get:JsonProperty("instance-dependencies")
    var instanceDependencies: List<String>? = null

    @get:JsonProperty("key-dependencies")
    lateinit var keyDependencies: MutableList<String>
}

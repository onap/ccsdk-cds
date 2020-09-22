/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.cds.blueprintsprocessor.uat.utils

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.convertValue
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.Tag

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ProcessDefinition(
    val name: String,
    val request: JsonNode,
    val expectedResponse: JsonNode? = null,
    val responseNormalizerSpec: JsonNode? = null
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class RequestDefinition(
    val method: String,
    @JsonDeserialize(using = PathDeserializer::class)
    val path: String,
    val headers: Map<String, String> = emptyMap(),
    val body: JsonNode? = null
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ResponseDefinition(
    val status: Int = 200,
    val body: JsonNode? = null,
    val headers: Map<String, String> = mapOf("Content-Type" to "application/json")
) {

    companion object {

        val DEFAULT_RESPONSES = listOf(ResponseDefinition())
    }
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ExpectationDefinition(
    val request: RequestDefinition,
    response: ResponseDefinition?,
    responses: List<ResponseDefinition>? = null,
    val times: String = ">= 1"
) {

    val responses: List<ResponseDefinition> = resolveOneOrMany(response, responses, ResponseDefinition.DEFAULT_RESPONSES)

    companion object {

        fun <T> resolveOneOrMany(one: T?, many: List<T>?, defaultMany: List<T>): List<T> = when {
            many != null -> many
            one != null -> listOf(one)
            else -> defaultMany
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ServiceDefinition(val selector: String, val expectations: List<ExpectationDefinition>)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class UatDefinition(
    val processes: List<ProcessDefinition>,
    @JsonAlias("external-services")
    val externalServices: List<ServiceDefinition> = emptyList()
) {

    fun dump(mapper: ObjectMapper, excludedProperties: List<String> = emptyList()): String {
        val uatAsMap: Map<String, Any> = mapper.convertValue(this)
        if (excludedProperties.isNotEmpty()) {
            pruneTree(uatAsMap, excludedProperties)
        }
        return Yaml().dumpAs(uatAsMap, Tag.MAP, DumperOptions.FlowStyle.BLOCK)
    }

    fun toBare(): UatDefinition {
        val newProcesses = processes.map { p ->
            ProcessDefinition(p.name, p.request, null, p.responseNormalizerSpec)
        }
        return UatDefinition(newProcesses)
    }

    private fun pruneTree(node: Any?, excludedProperties: List<String>) {
        when (node) {
            is MutableMap<*, *> -> {
                excludedProperties.forEach { key -> node.remove(key) }
                node.forEach { (_, value) -> pruneTree(value, excludedProperties) }
            }
            is List<*> -> node.forEach { value -> pruneTree(value, excludedProperties) }
        }
    }

    companion object {

        fun load(mapper: ObjectMapper, spec: String): UatDefinition =
            mapper.convertValue(Yaml().load(spec), UatDefinition::class.java)
    }
}

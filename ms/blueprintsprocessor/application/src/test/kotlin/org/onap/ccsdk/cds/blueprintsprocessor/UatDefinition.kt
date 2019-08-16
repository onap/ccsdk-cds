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
package org.onap.ccsdk.cds.blueprintsprocessor

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.MissingNode
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import org.yaml.snakeyaml.Yaml
import java.nio.file.Path

data class ProcessDefinition(val name: String, val request: JsonNode, val expectedResponse: JsonNode,
                             val responseNormalizerSpec: JsonNode = MissingNode.getInstance())

data class RequestDefinition(val method: String,
                             @JsonDeserialize(using = PathDeserializer::class)
                             val path: String,
                             @JsonAlias("content-type")
                             val contentType: String? = null,
                             val body: JsonNode = MissingNode.getInstance()) {
    fun requestHeadersMatcher(): Map<String, String> {
        return if (contentType != null) eq(mapOf("Content-Type" to contentType)) else any()
    }
}

data class ResponseDefinition(val status: Int = 200, val body: JsonNode = MissingNode.getInstance()) {
    companion object {
        val DEFAULT_RESPONSE = ResponseDefinition()
    }
}

data class ExpectationDefinition(val request: RequestDefinition,
                                 val response: ResponseDefinition = ResponseDefinition.DEFAULT_RESPONSE)

data class ServiceDefinition(val selector: String, val expectations: List<ExpectationDefinition>)

data class UatDefinition(val processes: List<ProcessDefinition>,
                         @JsonAlias("external-services")
                         val externalServices: List<ServiceDefinition> = emptyList()) {

    companion object {
        fun load(mapper: ObjectMapper, path: Path): UatDefinition {
            return path.toFile().reader().use { reader ->
                mapper.convertValue(Yaml().load(reader), UatDefinition::class.java)
            }
        }
    }
}

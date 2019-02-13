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

package org.onap.ccsdk.apps.controllerblueprints.core.utils


import com.fasterxml.jackson.databind.JsonNode
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider

class JsonParserUtils {
    companion object {

        //TODO("Optimise this")
        val JACKSON_JSON_NODE_CONFIGURATION = Configuration.builder()
                .mappingProvider(JacksonMappingProvider()).jsonProvider(JacksonJsonNodeJsonProvider()).build()

        val PATH_CONFIGURATION = Configuration.builder().options(Option.AS_PATH_LIST).build()

        fun paths(jsonContent: String, expression: String): List<String> {
            return JsonPath.using(PATH_CONFIGURATION).parse(jsonContent).read(expression)
        }

        fun paths(jsonNode: JsonNode, expression: String): List<String> {
            return paths(jsonNode.toString(), expression)
        }

        fun parse(jsonContent: String, expression: String): JsonNode {
            return JsonPath.using(JACKSON_JSON_NODE_CONFIGURATION).parse(jsonContent).read(expression)
        }

        fun parse(jsonNode: JsonNode, expression: String): JsonNode {
            return parse(jsonNode.toString(), expression)
        }

        fun parseNSet(jsonContent: String, expression: String, value: JsonNode): JsonNode {
            return JsonPath.using(JACKSON_JSON_NODE_CONFIGURATION).parse(jsonContent).set(expression, value).json()
        }

        fun parseNSet(jsonNode: JsonNode, expression: String, valueNode: JsonNode): JsonNode {
            return parseNSet(jsonNode.toString(), expression, valueNode)
        }
    }
}
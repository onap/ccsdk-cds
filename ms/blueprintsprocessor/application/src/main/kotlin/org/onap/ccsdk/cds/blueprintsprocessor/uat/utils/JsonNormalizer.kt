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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ContainerNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.schibsted.spt.data.jslt.Parser

internal class JsonNormalizer {

    companion object {

        fun getNormalizer(mapper: ObjectMapper, jsltSpec: JsonNode?): (String) -> String {
            if (jsltSpec == null) {
                return { it }
            }
            return { s: String ->
                val input = mapper.readTree(s)
                val expandedJstlSpec =
                    expandJstlSpec(jsltSpec)
                val jslt = Parser.compileString(expandedJstlSpec)
                val output = jslt.apply(input)
                output.toString()
            }
        }

        /**
         * Creates an extended JSTL spec by appending the "*: ." wildcard pattern to every inner JSON object, and
         * removing the extra quotes added by the standard YAML/JSON converters on fields prefixed by "?".
         *
         * @param jstlSpec the JSTL spec as a structured JSON object.
         * @return the string representation of the extended JSTL spec.
         */
        private fun expandJstlSpec(jstlSpec: JsonNode): String {
            val extendedJstlSpec =
                updateObjectNodes(jstlSpec, "*", ".")
            return extendedJstlSpec.toString()
                // Handle the "?" as a prefix to literal/non-quoted values
                .replace("\"\\?([^\"]+)\"".toRegex(), "$1")
                // Also, remove the quotes added by Jackson for key and value of the wildcard matcher
                .replace("\"([.*])\"".toRegex(), "$1")
        }

        /**
         * Expands a structured JSON object, by adding the given key and value to every nested ObjectNode.
         *
         * @param jsonNode the root node.
         * @param fieldName the fixed field name.
         * @param fieldValue the fixed field value.
         */
        private fun updateObjectNodes(jsonNode: JsonNode, fieldName: String, fieldValue: String): JsonNode {
            if (jsonNode is ContainerNode<*>) {
                (jsonNode as? ObjectNode)?.put(fieldName, fieldValue)
                jsonNode.forEach { child ->
                    updateObjectNodes(
                        child,
                        fieldName,
                        fieldValue
                    )
                }
            }
            return jsonNode
        }
    }
}

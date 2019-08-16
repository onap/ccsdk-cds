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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class PathDeserializer : StdDeserializer<String>(String::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): String {
        val path = jp.codec.readValue(jp, Any::class.java)
        return flatJoin(path)
    }

    /**
     * Join a multilevel lists of strings.
     * Example: flatJoin(listOf("a", listOf("b", "c"), "d")) will result in "a/b/c/d".
     */
    private fun flatJoin(path: Any): String {
        fun flatJoinTo(sb: StringBuilder, path: Any): StringBuilder {
            when (path) {
                is List<*> -> path.filterNotNull().forEach { flatJoinTo(sb, it) }
                is String -> {
                    if (sb.isNotEmpty()) {
                        sb.append('/')
                    }
                    sb.append(path)
                }
                else -> throw IllegalArgumentException("Unsupported type: ${path.javaClass}")
            }
            return sb
        }
        return flatJoinTo(StringBuilder(), path).toString()
    }
}
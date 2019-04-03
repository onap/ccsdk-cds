/*
 * Copyright © 2019 IBM, Bell Canada.
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
package org.onap.ccsdk.cds.controllerblueprints.core.interfaces

import com.fasterxml.jackson.core.io.CharTypes
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.TextNode

interface BlueprintTemplateService {

    /**
     * Generate dynamique content using Velocity Template or Jinja template
     *
     * @param template template string content
     * @param json json string content
     * @param ignoreJsonNull Ignore Null value in the JSON content
     * @param additionalContext (Key, value) mutable map for additional variables
     * @return Content result
     *
     **/
    fun generateContent(template: String, json: String = "", ignoreJsonNull: Boolean = false,
                        additionalContext: MutableMap<String, Any> = mutableMapOf()): String


    /**
     * Generate dynamique content using Velocity Template or Jinja template
     *
     * @param templatePath template file path
     * @param jsonPath json file path
     * @param ignoreJsonNull Ignore Null value in the JSON content
     * @param additionalContext (Key, value) mutable map for additional variables
     * @return Content result
     *
     **/
    fun generateContentFromFiles(templatePath: String, jsonPath: String = "", ignoreJsonNull: Boolean = false,
                        additionalContext: MutableMap<String, Any> = mutableMapOf()): String
}

/**
 * Customise JsonNodeFactory adn TextNode, Since it introduces quotes for string data.
 */
open class BluePrintJsonNodeFactory : JsonNodeFactory() {
    override fun textNode(text: String): TextNode {
        return BluePrintTextNode(text)
    }
}

open class BluePrintTextNode(v: String) : TextNode(v) {
    override fun toString(): String {
        var len = this._value.length
        len = len + 2 + (len shr 4)
        val sb = StringBuilder(len)
        CharTypes.appendQuoted(sb, this._value)
        return sb.toString()
    }

}
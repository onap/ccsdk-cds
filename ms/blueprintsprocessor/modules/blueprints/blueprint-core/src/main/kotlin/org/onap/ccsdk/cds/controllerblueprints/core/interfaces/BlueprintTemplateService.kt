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
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService

interface BlueprintTemplateService {

    /**
     * Generate dynamique content using Velocity Template or Jinja template
     *
     * @param bluePrintRuntimeService blueprint runtime
     * @param nodeTemplateName node template
     * @param artifactName Artifact Name
     * @param jsonData json string data content to mash
     * @param ignoreJsonNull Ignore Null value in the JSON content
     * @param additionalContext (Key, value) mutable map for additional variables
     * @return Content result
     *
     **/
    suspend fun generateContent(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        nodeTemplateName: String,
        artifactName: String,
        jsonData: String = "",
        ignoreJsonNull: Boolean = false,
        additionalContext: MutableMap<String, Any> = mutableMapOf()
    ): String
}

/**
 * Customise JsonNodeFactory and TextNode, Since it introduces quotes for string data.
 */
open class BlueprintJsonNodeFactory : JsonNodeFactory() {

    override fun textNode(text: String): TextNode {
        return BlueprintTextNode(text)
    }
}

open class BlueprintTextNode(v: String) : TextNode(v) {

    override fun toString(): String {
        var len = this._value.length
        len = len + 2 + (len shr 4)
        val sb = StringBuilder(len)
        CharTypes.appendQuoted(sb, this._value)
        return sb.toString()
    }
}

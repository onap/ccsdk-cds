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

package org.onap.ccsdk.cds.controllerblueprints.core.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.hubspot.jinjava.Jinjava
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintJsonNodeFactory
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils


object BluePrintJinjaTemplateService: BlueprintTemplateService {

    override fun generateContent(template: String, json: String, ignoreJsonNull: Boolean,
                                 additionalContext: MutableMap<String, Any>): String {
        // Load template
        val jinJava = Jinjava()
        val mapper = ObjectMapper()
        val nodeFactory = BluePrintJsonNodeFactory()
        mapper.nodeFactory = nodeFactory

        // Add the JSON Data to the context
        if (json.isNotEmpty()) {
            val jsonNode = mapper.readValue<JsonNode>(json, JsonNode::class.java)
                    ?: throw BluePrintProcessorException("couldn't get json node from json")
            if (ignoreJsonNull)
                JacksonUtils.removeJsonNullNode(jsonNode)
            jsonNode.fields().forEach { entry ->
                additionalContext[entry.key] = entry.value
            }
        }

        return jinJava.render(template, additionalContext.toMap())
    }
}


/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Modifications Copyright © 2019 IBM, Bell Canada.
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
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintJsonNodeFactory
import org.onap.ccsdk.cds.controllerblueprints.core.removeNullNode
import java.io.StringWriter

object BlueprintVelocityTemplateService {

    /**
     * Generate Content from Velocity Template and JSON Content with injected API
     */
    fun generateContent(
        template: String,
        json: String,
        ignoreJsonNull: Boolean = false,
        additionalContext: MutableMap<String, Any> = mutableMapOf()
    ): String {

        // Customized Object Mapper to remove String double quotes
        val mapper = ObjectMapper()
        val nodeFactory = BlueprintJsonNodeFactory()
        mapper.nodeFactory = nodeFactory

        val jsonNode: JsonNode? = if (json.isNotEmpty()) {
            mapper.readValue(json, JsonNode::class.java)
                ?: throw BlueprintProcessorException("couldn't get json node from json")
        } else {
            null
        }
        return generateContent(template, jsonNode, ignoreJsonNull, additionalContext)
    }

    /**
     * Generate Content from Velocity Template and JSON Node with injected API
     */
    fun generateContent(
        template: String,
        jsonNode: JsonNode?,
        ignoreJsonNull: Boolean = false,
        additionalContext: MutableMap<String, Any> = mutableMapOf()
    ): String {

        /*
         *  create a new instance of the velocity engine
         */
        val velocity = VelocityEngine()

        /*
         *  initialize the engine
         */
        velocity.init()

        val velocityContext = VelocityContext()
        velocityContext.put("StringUtils", StringUtils::class.java)
        velocityContext.put("BooleanUtils", BooleanUtils::class.java)

        // Add the Custom Velocity Context API
        additionalContext.forEach { (name, value) -> velocityContext.put(name, value) }

        // Add the JSON Data to the context
        if (jsonNode != null) {
            if (ignoreJsonNull)
                jsonNode.removeNullNode()
            jsonNode.fields().forEach { entry ->
                velocityContext.put(entry.key, entry.value)
            }
        }

        val stringWriter = StringWriter()
        velocity.evaluate(velocityContext, stringWriter, "TemplateData", template)
        stringWriter.flush()
        return stringWriter.toString()
    }
}

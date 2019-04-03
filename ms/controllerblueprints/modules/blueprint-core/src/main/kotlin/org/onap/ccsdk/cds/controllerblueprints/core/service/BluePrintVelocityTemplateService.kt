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
import org.apache.velocity.app.Velocity
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintJsonNodeFactory
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import java.io.StringWriter

object BluePrintVelocityTemplateService: BlueprintTemplateService {

    /**
     * Generate Content from Velocity Template and JSON Content or property map.
     */
    override fun generateContent(template: String, json: String, ignoreJsonNull: Boolean, additionalContext:
    MutableMap<String, Any>): String {
        Velocity.init()
        val mapper = ObjectMapper()
        val nodeFactory = BluePrintJsonNodeFactory()
        mapper.nodeFactory = nodeFactory

        val velocityContext = VelocityContext()
        velocityContext.put("StringUtils", StringUtils::class.java)
        velocityContext.put("BooleanUtils", BooleanUtils::class.java)

        // Add the Custom Velocity Context API
        additionalContext.forEach { name, value -> velocityContext.put(name, value) }

        // Add the JSON Data to the context
        if (json.isNotEmpty()) {
            val jsonNode = mapper.readValue<JsonNode>(json, JsonNode::class.java)
                    ?: throw BluePrintProcessorException("couldn't get json node from json")
            if (ignoreJsonNull)
                JacksonUtils.removeJsonNullNode(jsonNode)
            jsonNode.fields().forEach { entry ->
                velocityContext.put(entry.key, entry.value)
            }
        }

        val stringWriter = StringWriter()
        Velocity.evaluate(velocityContext, stringWriter, "TemplateData", template)
        stringWriter.flush()
        return stringWriter.toString()
    }
}


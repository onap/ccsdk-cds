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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.hubspot.jinjava.Jinjava
import com.hubspot.jinjava.JinjavaConfig
import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.loader.ResourceLocator
import com.hubspot.jinjava.loader.ResourceNotFoundException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.config.BlueprintLoadConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintJsonNodeFactory
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.removeNullNode
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files.readAllBytes
import java.nio.file.Paths

object BlueprintJinjaTemplateService {

    /**
     * To enable inheritance within CBA, we need Jinja runtime to know where to load the templates.
     */
    class BlueprintRelatedTemplateLocator(
        private val bluePrintLoadConfiguration: BlueprintLoadConfiguration,
        private val artifactName: String,
        private val artifactVersion: String
    ) : ResourceLocator {

        @Throws(IOException::class)
        override fun getString(fullName: String, encoding: Charset, interpreter: JinjavaInterpreter): String {
            try {
                val deployFile =
                    normalizedPathName(
                        bluePrintLoadConfiguration.blueprintDeployPath,
                        artifactName,
                        artifactVersion,
                        fullName
                    )

                return String(readAllBytes(Paths.get(deployFile)))
            } catch (var5: IllegalArgumentException) {
                throw ResourceNotFoundException("Couldn't find resource: $fullName")
            }
        }
    }

    fun generateContent(
        template: String,
        json: String,
        ignoreJsonNull: Boolean,
        additionalContext: MutableMap<String, Any>,
        bluePrintLoadConfiguration: BlueprintLoadConfiguration,
        artifactName: String,
        artifactVersion: String
    ): String {

        return generateContent(
            template,
            json,
            ignoreJsonNull,
            additionalContext,
            BlueprintRelatedTemplateLocator(bluePrintLoadConfiguration, artifactName, artifactVersion)
        )
    }

    fun generateContent(
        template: String,
        json: String,
        ignoreJsonNull: Boolean,
        additionalContext: MutableMap<String, Any>,
        resourceLocator: ResourceLocator? = null
    ): String {
        val jinJava = Jinjava(JinjavaConfig())
        if (resourceLocator != null) {
            jinJava.resourceLocator = resourceLocator
        }

        val mapper = ObjectMapper()
        val nodeFactory = BlueprintJsonNodeFactory()
        mapper.nodeFactory = nodeFactory

        // Add the JSON Data to the context
        if (json.isNotEmpty()) {
            val jsonNode = mapper.readValue<JsonNode>(json, JsonNode::class.java)
                ?: throw BlueprintProcessorException("couldn't get json node from json")
            if (ignoreJsonNull) {
                jsonNode.removeNullNode()
            }

            val jsonMap: Map<String, Any> = mapper.readValue(json, object : TypeReference<Map<String, Any>>() {})
            additionalContext.putAll(jsonMap)
        }

        return jinJava.render(template, additionalContext)
    }
}

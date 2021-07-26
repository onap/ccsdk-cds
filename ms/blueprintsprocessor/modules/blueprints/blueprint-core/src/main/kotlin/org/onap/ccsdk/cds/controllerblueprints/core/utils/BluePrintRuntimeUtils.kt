/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.controllerblueprints.core.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.slf4j.LoggerFactory

/**
 *
 *
 * @author Brinda Santh
 */
object BluePrintRuntimeUtils {

    private val log = LoggerFactory.getLogger(this::class.toString())

    fun assignInputsFromFile(bluePrintContext: BluePrintContext, fileName: String, context: MutableMap<String, JsonNode>) {
        val jsonNode: JsonNode = JacksonUtils.jsonNodeFromFile(fileName)
        return assignInputs(bluePrintContext, jsonNode, context)
    }

    fun assignInputsFromClassPathFile(
        bluePrintContext: BluePrintContext,
        fileName: String,
        context: MutableMap<String,
            JsonNode>
    ) {
        val jsonNode = JacksonUtils.jsonNodeFromClassPathFile(fileName)
        return assignInputs(bluePrintContext, jsonNode, context)
    }

    fun assignInputsFromContent(bluePrintContext: BluePrintContext, content: String, context: MutableMap<String, JsonNode>) {
        val jsonNode: JsonNode = JacksonUtils.jsonNode(content)
        return assignInputs(bluePrintContext, jsonNode, context)
    }

    fun assignInputs(bluePrintContext: BluePrintContext, jsonNode: JsonNode, context: MutableMap<String, JsonNode>) {
        log.info("assignInputs from input JSON ({})", jsonNode.toString())
        bluePrintContext.inputs()?.forEach { propertyName, _ ->
            val valueNode: JsonNode = jsonNode.at("/".plus(propertyName)) ?: NullNode.getInstance()

            val path = BluePrintConstants.PATH_INPUTS.plus(BluePrintConstants.PATH_DIVIDER).plus(propertyName)
            log.trace("setting input path ({}), values ({})", path, valueNode)
            context[path] = valueNode
        }
    }
}

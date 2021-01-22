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

package org.onap.ccsdk.cds.blueprintsprocessor.core.utils

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.returnNullIfMissing
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils

object PayloadUtils {

    fun prepareRequestPayloadStr(workflowName: String, jsonNode: JsonNode): String {
        return prepareRequestPayload(workflowName, jsonNode).asJsonString(false)
    }

    fun prepareRequestPayload(workflowName: String, jsonNode: JsonNode): JsonNode {
        val objectNode = JacksonUtils.objectMapper.createObjectNode()
        objectNode.set<JsonNode>("$workflowName-request", jsonNode)
        return objectNode
    }

    fun getResponseDataFromPayload(workflowName: String, responsePayload: JsonNode): JsonNode {
        return responsePayload.get("$workflowName-response").returnNullIfMissing()
            ?: throw BlueprintProcessorException("failed to get property($workflowName-response)")
    }

    fun prepareInputsFromWorkflowPayload(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        payload: JsonNode,
        workflowName: String
    ) {
        val input = payload.get("$workflowName-request")
        bluePrintRuntimeService.assignWorkflowInputs(workflowName, input)
    }

    fun prepareDynamicInputsFromWorkflowPayload(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        payload: JsonNode,
        workflowName: String
    ) {
        val input = payload.get("$workflowName-request")
        val propertyFields = input.get("$workflowName-properties")
        prepareDynamicInputsFromComponentPayload(bluePrintRuntimeService, propertyFields)
    }

    fun prepareDynamicInputsFromComponentPayload(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        payload: JsonNode
    ) {
        payload.fields().forEach { property ->
            val path = StringBuilder(BlueprintConstants.PATH_INPUTS)
                .append(BlueprintConstants.PATH_DIVIDER).append(property.key).toString()
            bluePrintRuntimeService.put(path, property.value)
        }
    }
}

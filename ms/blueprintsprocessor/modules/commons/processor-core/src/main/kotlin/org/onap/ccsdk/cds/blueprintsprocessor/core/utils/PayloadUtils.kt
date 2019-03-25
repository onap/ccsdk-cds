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
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService


class PayloadUtils {

    companion object {

        fun prepareInputsFromWorkflowPayload(bluePrintRuntimeService: BluePrintRuntimeService<*>, payload: JsonNode, workflowName: String) {
            val input = payload.get("$workflowName-request")
            bluePrintRuntimeService.assignWorkflowInputs(workflowName, input)
        }

        fun prepareDynamicInputsFromWorkflowPayload(bluePrintRuntimeService: BluePrintRuntimeService<*>, payload: JsonNode, workflowName: String) {
            val input = payload.get("$workflowName-request")
            val propertyFields = input.get("$workflowName-properties")
            prepareDynamicInputsFromComponentPayload(bluePrintRuntimeService, propertyFields)
        }

        fun prepareDynamicInputsFromComponentPayload(bluePrintRuntimeService: BluePrintRuntimeService<*>, payload: JsonNode) {
            payload.fields().forEach { property ->
                val path = StringBuilder(BluePrintConstants.PATH_INPUTS)
                        .append(BluePrintConstants.PATH_DIVIDER).append(property.key).toString()
                bluePrintRuntimeService.put(path, property.value)
            }
        }
    }
}
/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.asObjectNode
import org.onap.ccsdk.cds.controllerblueprints.core.returnNullIfMissing
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component("component-resource-resolution")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ResourceResolutionComponent(private val resourceResolutionService: ResourceResolutionService) :
        AbstractComponentFunction() {

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        val occurrence = getOperationInput(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE).asInt()
        val resolutionKey = getOptionalOperationInput(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY)?.returnNullIfMissing()?.textValue() ?: ""
        val storeResult = getOptionalOperationInput(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT)?.asBoolean() ?: false
        val resourceId = getOptionalOperationInput(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID)?.returnNullIfMissing()?.textValue() ?: ""

        val resourceType = getOptionalOperationInput(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE)?.returnNullIfMissing()?.textValue() ?: ""

        val properties: MutableMap<String, Any> = mutableMapOf()
        properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT] = storeResult
        properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = resolutionKey
        properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] = resourceId
        properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = resourceType
        properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] = occurrence

        val jsonResponse = JsonNodeFactory.instance.objectNode()

        // validate inputs if we need to store the resource and template resolution.
        if (storeResult) {
            if (resolutionKey.isNotEmpty() && (resourceId.isNotEmpty() || resourceType.isNotEmpty())) {
                throw BluePrintProcessorException("Can't proceed with the resolution: either provide resolution-key OR combination of resource-id and resource-type.")
            } else if ((resourceType.isNotEmpty() && resourceId.isEmpty()) || (resourceType.isEmpty() && resourceId.isNotEmpty())) {
                throw BluePrintProcessorException("Can't proceed with the resolution: both resource-id and resource-type should be provided, one of them is missing.")
            } else if (resourceType.isEmpty() && resourceId.isEmpty() && resolutionKey.isEmpty()) {
                throw BluePrintProcessorException("Can't proceed with the resolution: can't persist resolution without a correlation key. " +
                        "Either provide a resolution-key OR combination of resource-id and resource-type OR set `storeResult` to false.")
            }
        }

        val artifactPrefixNamesNode = getOperationInput(ResourceResolutionConstants.INPUT_ARTIFACT_PREFIX_NAMES)
        val artifactPrefixNames = JacksonUtils.getListFromJsonNode(artifactPrefixNamesNode, String::class.java)

        for (j in 1..occurrence) {
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] = j

            val response = resourceResolutionService.resolveResources(bluePrintRuntimeService,
                    nodeTemplateName,
                    artifactPrefixNames,
                    properties)

            // provide indexed result in output if we have multiple resolution
            if (occurrence != 1) {
                jsonResponse.set(Integer.toString(j), response.asJsonNode())
            } else {
                jsonResponse.setAll(response.asObjectNode())
            }

        }

        // Set Output Attributes
        bluePrintRuntimeService.setNodeTemplateAttributeValue(nodeTemplateName,
                ResourceResolutionConstants.OUTPUT_ASSIGNMENT_PARAMS, jsonResponse)
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBluePrintError().addError(runtimeException.message!!)
    }
}
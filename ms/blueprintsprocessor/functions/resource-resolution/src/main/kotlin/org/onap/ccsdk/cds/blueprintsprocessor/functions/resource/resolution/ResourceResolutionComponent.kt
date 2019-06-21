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
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component("component-resource-resolution")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ResourceResolutionComponent(private val resourceResolutionService: ResourceResolutionService) :
    AbstractComponentFunction() {

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        val occurrence = getOperationInput(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE)
        val key = getOptionalOperationInput(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_KEY)
        val storeResult = getOptionalOperationInput(ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT)

        val properties: MutableMap<String, Any> = mutableMapOf()
        properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT] =
            storeResult?.asBoolean() ?: false

        val artifactPrefixNamesNode = getOperationInput(ResourceResolutionConstants.INPUT_ARTIFACT_PREFIX_NAMES)
        val artifactPrefixNames = JacksonUtils.getListFromJsonNode(artifactPrefixNamesNode, String::class.java)

        val jsonResponse = JsonNodeFactory.instance.objectNode()
        for (j in 1..occurrence.asInt()) {

            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_KEY] = key?.asText() + "-" + j

            jsonResponse.set(key?.asText() + "-" + j,
                resourceResolutionService.resolveResources(bluePrintRuntimeService,
                    nodeTemplateName,
                    artifactPrefixNames,
                    properties).asJsonNode())
        }

        // Set Output Attributes
        bluePrintRuntimeService.setNodeTemplateAttributeValue(nodeTemplateName,
            ResourceResolutionConstants.OUTPUT_ASSIGNMENT_PARAMS, jsonResponse)
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBluePrintError().addError(runtimeException.message!!)
    }
}
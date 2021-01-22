/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.cds.controllerblueprints.core.jsonPathParse
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils

/** This implementation is used to build services, which doesn't need blueprints */
abstract class AbstractServiceFunction : BlueprintFunctionNode<ExecutionServiceInput, ExecutionServiceOutput> {

    @Transient
    private val log = logger(AbstractServiceFunction::class)

    lateinit var executionServiceInput: ExecutionServiceInput
    var executionServiceOutput = ExecutionServiceOutput()
    lateinit var processId: String
    lateinit var actionName: String
    lateinit var responseActionPayload: JsonNode

    override fun getName(): String {
        return actionName
    }

    override suspend fun prepareRequestNB(executionRequest: ExecutionServiceInput): ExecutionServiceInput {

        this.executionServiceInput = executionRequest

        actionName = executionRequest.actionIdentifiers.actionName
        check(actionName.isNotEmpty()) { "couldn't get action name" }

        processId = executionRequest.commonHeader.requestId
        check(processId.isNotEmpty()) { "couldn't get process id for service action($actionName)" }

        return executionRequest
    }

    override suspend fun applyNB(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        try {
            prepareRequestNB(executionServiceInput)
            processNB(executionServiceInput)
        } catch (runtimeException: RuntimeException) {
            log.error("failed in ${getName()} : ${runtimeException.message}", runtimeException)
            recoverNB(runtimeException, executionServiceInput)
        }
        return prepareResponseNB()
    }

    override suspend fun prepareResponseNB(): ExecutionServiceOutput {
        log.debug("Preparing Response...")
        executionServiceOutput.commonHeader = executionServiceInput.commonHeader
        executionServiceOutput.actionIdentifiers = executionServiceInput.actionIdentifiers
        var status = Status()
        try {
            // Set the Response Payload
            executionServiceOutput.payload = JacksonUtils.objectMapper.createObjectNode()
            executionServiceOutput.payload.set<JsonNode>("$actionName-response", responseActionPayload)
            // Set the Default Step Status
            status.eventType = EventType.EVENT_COMPONENT_EXECUTED.name
        } catch (e: Exception) {
            status.message = BlueprintConstants.STATUS_FAILURE
            status.eventType = EventType.EVENT_COMPONENT_FAILURE.name
        }
        executionServiceOutput.status = status
        return this.executionServiceOutput
    }

    fun setResponsePayloadForAction(actionPayload: JsonNode) {
        this.responseActionPayload = actionPayload
    }

    /**
     * Get Execution Input Payload data
     */
    fun requestPayload(): JsonNode? {
        return executionServiceInput.payload
    }

    /**
     * Get Execution Input payload action property with [expression]
     * ex: requestPayloadActionProperty("data") will look for path "payload/<action-name>-request/data"
     */
    fun requestPayloadActionProperty(expression: String?): JsonNode? {
        val requestExpression = if (expression.isNullOrBlank()) {
            "$actionName-request"
        } else {
            "$actionName-request.$expression"
        }
        return executionServiceInput.payload.jsonPathParse(".$requestExpression")
    }
}

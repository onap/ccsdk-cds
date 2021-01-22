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

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.asJsonType
import org.onap.ccsdk.cds.blueprintsprocessor.core.utils.PayloadUtils
import org.onap.ccsdk.cds.blueprintsprocessor.core.utils.createActionIdentifiersProto
import org.onap.ccsdk.cds.blueprintsprocessor.core.utils.createCommonHeaderProto
import org.onap.ccsdk.cds.blueprintsprocessor.core.utils.createExecutionServiceInputProto
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * This is generic Remote Script Component Executor function
 * @author Brinda Santh
 */
@Component("component-remote-script-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentRemoteScriptExecutor(
    private var streamingRemoteExecutionService: StreamingRemoteExecutionService<
        org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput,
        org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput>
) : AbstractComponentFunction() {

    companion object {

        const val INPUT_SELECTOR = "selector"
        const val INPUT_BLUEPRINT_NAME = "blueprint-name"
        const val INPUT_BLUEPRINT_VERSION = "blueprint-version"
        const val INPUT_BLUEPRINT_ACTION = "blueprint-action"
        const val INPUT_TIMEOUT = "timeout"
        const val INPUT_REQUEST_DATA = "request-data"

        const val ATTRIBUTE_RESPONSE_DATA = "response-data"
        const val ATTRIBUTE_STATUS = "status"

        const val OUTPUT_STATUS = "status"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        val selector = getOperationInput(INPUT_SELECTOR)
        val blueprintName = getOperationInput(INPUT_BLUEPRINT_NAME).asText()
        val blueprintVersion = getOperationInput(INPUT_BLUEPRINT_VERSION).asText()
        val blueprintAction = getOperationInput(INPUT_BLUEPRINT_ACTION).asText()
        val requestData = getOperationInput(INPUT_REQUEST_DATA)
        val timeout = getOperationInput(INPUT_TIMEOUT).asLong()

        val requestPayload = PayloadUtils.prepareRequestPayloadStr(blueprintAction, requestData)

        val txId = UUID.randomUUID().toString()
        val commonHeader = createCommonHeaderProto(
            executionRequest.commonHeader.subRequestId,
            txId, BlueprintConstants.APP_NAME
        )
        val actionIdentifier = createActionIdentifiersProto(blueprintName, blueprintVersion, blueprintAction)

        val executionServiceInputProto =
            createExecutionServiceInputProto(commonHeader, actionIdentifier, requestPayload)

        /** Invoke remote implementation using GRPC */
        val executionServiceOutputProto =
            streamingRemoteExecutionService.sendNonInteractive(selector, txId, executionServiceInputProto, timeout)

        /** Set the response data */
        if (executionServiceOutputProto.payload != null) {
            val outputData = PayloadUtils.getResponseDataFromPayload(
                blueprintAction,
                executionServiceOutputProto.payload.asJsonType()
            )
            setAttribute(ATTRIBUTE_RESPONSE_DATA, outputData)
        }

        /** set node template attribute */
        val statusMessage = executionServiceOutputProto.status.message
        if (statusMessage == BlueprintConstants.STATUS_SUCCESS) {
            setAttribute(ATTRIBUTE_STATUS, BlueprintConstants.STATUS_SUCCESS.asJsonPrimitive())
        } else {
            val errorMessage = executionServiceOutputProto.status.errorMessage ?: "failed in remote execution"
            throw BlueprintProcessorException(errorMessage)
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBlueprintError()
            .addError("Failed in ComponentRemoteScriptExecutor : ${runtimeException.message}")
    }
}

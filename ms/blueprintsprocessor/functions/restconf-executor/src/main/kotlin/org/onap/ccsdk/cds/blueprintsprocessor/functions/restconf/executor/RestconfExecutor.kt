package org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.ACTION_DATASTORE
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.ACTION_INPUT
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.ACTION_OUTPUT
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.ACTION_PATH
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.ACTION_PAYLOAD
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.ACTION_TYPE
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.NODE_ID
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.FAIL_FAST
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.HTTP_SUCCESS_RANGE
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.MOUNT_PAYLOAD
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.RESTCONF_CONNECTION_CONFIG
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentRemoteScriptExecutor
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentScriptExecutor
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants.PROPERTY_CONNECTION_CONFIG
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils.Companion.jsonNodeFromObject
import org.slf4j.LoggerFactory

open class Mount : AbstractScriptComponentFunction() {

    val log = LoggerFactory.getLogger(Mount::class.java)!!

    override fun getName(): String {
        return "Mount"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("Mounting ODL restconf node process")

        val deviceInformation = relationshipProperty(RESTCONF_CONNECTION_CONFIG, PROPERTY_CONNECTION_CONFIG)
        val webclientService = restconfClientService(deviceInformation)

        val nodeId = requestPayloadActionProperty(NODE_ID)?.first()?.textValue()
            ?: throw BlueprintProcessorException("Failed to load $NODE_ID properties.")
        val mountPayload = requestPayloadActionProperty(MOUNT_PAYLOAD)?.first()
            ?: throw BlueprintProcessorException("Failed to load $MOUNT_PAYLOAD properties.")
        restconfMountDeviceJson(webclientService, nodeId, mountPayload.toString())

        setAttribute(
            ComponentRemoteScriptExecutor.ATTRIBUTE_STATUS,
            BlueprintConstants.STATUS_SUCCESS.asJsonPrimitive()
        )
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        addError("failed in restconf execution : ${runtimeException.message}")
    }
}

open class Execute : AbstractScriptComponentFunction() {

    val log = LoggerFactory.getLogger(Execute::class.java)!!

    override fun getName(): String {
        return "Execute"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        val nodeIdJson = requestPayloadActionProperty(NODE_ID)?.first()
            ?: throw BlueprintProcessorException("Failed to load $NODE_ID properties.")

        val failFastJsonNode = requestPayloadActionProperty(FAIL_FAST)!!
        val failFast = if (failFastJsonNode.isEmpty) false else failFastJsonNode.first().booleanValue()

        val deviceInformation = relationshipProperty(RESTCONF_CONNECTION_CONFIG, PROPERTY_CONNECTION_CONFIG)

        val webclientService = restconfClientService(deviceInformation)
        val nodeId = nodeIdJson.textValue()

        val actionList = requestPayloadActionProperty("action")?.first()
            ?: throw BlueprintProcessorException("Failed to load action properties.")
        validateActionList(actionList)

        val actionListResults: MutableList<Map<String, JsonNode>> = mutableListOf()

        for (action in actionList) {
            val actionTypeJsonNode = action.get(ACTION_TYPE)
            val actionPathJsonNode = action.get(ACTION_PATH)
            val actionType = RestconfRequestType.valueOf(actionTypeJsonNode.asText().toUpperCase())
            val dsJsonNode = action.get(ACTION_DATASTORE)
            val path = restconfPath(
                RestconfRequestDatastore.valueOf(dsJsonNode.asText().toUpperCase()),
                nodeId, actionPathJsonNode.asText()
            )
            val payload = action.get(ACTION_PAYLOAD)

            log.info("Processing Restconf action : $actionType $path" + if (payload != null) " $payload" else "")
            val response = executeAction(webclientService, path, actionType, payload)
            val responseBody = response.body

            val actionInput: MutableMap<String, JsonNode> = hashMapOf()
            actionInput[ACTION_TYPE] = actionTypeJsonNode
            actionInput[ACTION_PATH] = actionPathJsonNode
            actionInput[ACTION_DATASTORE] = dsJsonNode

            val actionResult: MutableMap<String, JsonNode> = hashMapOf()
            val actionResponse = responseBody.asJsonType()
            if (actionResponse !is TextNode && actionResponse.toString().isNotBlank()) {
                actionResult[ACTION_OUTPUT] = actionResponse
            }
            actionResult[ACTION_INPUT] = jsonNodeFromObject(actionInput)

            if (response.status in HTTP_SUCCESS_RANGE) {
                log.info("\nRestconf execution response : \n{}", responseBody.asJsonType().toPrettyString())
                actionResult[ComponentScriptExecutor.ATTRIBUTE_STATUS] =
                    BlueprintConstants.STATUS_SUCCESS.asJsonPrimitive()
            } else {
                actionResult[ComponentScriptExecutor.ATTRIBUTE_STATUS] =
                    BlueprintConstants.STATUS_FAILURE.asJsonPrimitive()
                addError(
                    BlueprintConstants.STATUS_FAILURE,
                    ComponentScriptExecutor.ATTRIBUTE_STATUS,
                    actionResponse.asText()
                )
                if (failFast) {
                    actionListResults.add(actionResult)
                    break
                }
            }
            actionListResults.add(actionResult)
        }

        setAttribute(ComponentScriptExecutor.ATTRIBUTE_RESPONSE_DATA, jsonNodeFromObject(actionListResults))
        setAttribute(ComponentScriptExecutor.ATTRIBUTE_STATUS, BlueprintConstants.STATUS_SUCCESS.asJsonPrimitive())
        actionListResults.forEach { actionResult ->
            val actionResultStatus = actionResult[ComponentScriptExecutor.ATTRIBUTE_STATUS]
            if (BlueprintConstants.STATUS_SUCCESS.asJsonPrimitive() != actionResultStatus) {
                setAttribute(
                    ComponentScriptExecutor.ATTRIBUTE_STATUS,
                    BlueprintConstants.STATUS_FAILURE.asJsonPrimitive()
                )
                val errorResponse = actionResult[ACTION_OUTPUT]!!
                addError(
                    BlueprintConstants.STATUS_FAILURE, ComponentScriptExecutor.ATTRIBUTE_STATUS,
                    errorResponse.asText()
                )
            }
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        addError("failed in restconf execution : ${runtimeException.message}")
    }

    private suspend fun executeAction(
        webClientService: BlueprintWebClientService,
        path: String,
        actionType: RestconfRequestType,
        payload: JsonNode?
    ): BlueprintWebClientService.WebClientResponse<String> {
        var headers = mutableMapOf("Content-Type" to "application/json")
        return when (actionType) {
            RestconfRequestType.PATCH -> {
                headers = mutableMapOf("Content-Type" to "application/yang.patch-status+json")
                genericPutPatchPostRequest(webClientService, path, actionType, payload.toString(), headers)
            }
            RestconfRequestType.PUT, RestconfRequestType.POST -> {
                genericPutPatchPostRequest(webClientService, path, actionType, payload.toString(), headers)
            }
            RestconfRequestType.GET -> {
                getRequest(webClientService, path)
            }
            RestconfRequestType.DELETE -> {
                genericGetOrDeleteRequest(webClientService, path, RestconfRequestType.DELETE)
            }
        }
    }

    private fun validateActionList(actionList: JsonNode) {
        if (actionList.isEmpty) {
            throw BlueprintProcessorException("No actions defined")
        }
        actionList.forEach { action ->
            action.get(ACTION_PATH)
                ?: throw BlueprintProcessorException("Failed to load action path.")
            action.get(ACTION_DATASTORE)
                ?: throw BlueprintProcessorException("Failed to load action datastore.")
            val actionTypeJsonNode = action.get(ACTION_TYPE)
                ?: throw BlueprintProcessorException("Failed to load action type.")
            when (val actionType = RestconfRequestType.valueOf(actionTypeJsonNode.asText().toUpperCase())) {
                RestconfRequestType.PATCH, RestconfRequestType.PUT, RestconfRequestType.POST -> {
                    action.get(ACTION_PAYLOAD)
                        ?: throw BlueprintProcessorException("Failed to load action $actionType payload.")
                }
            }
        }
    }
}

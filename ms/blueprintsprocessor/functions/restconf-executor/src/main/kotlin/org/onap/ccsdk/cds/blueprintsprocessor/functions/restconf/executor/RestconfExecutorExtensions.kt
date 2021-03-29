/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2018-2019 IBM, Bell Canada
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor

import com.fasterxml.jackson.databind.JsonNode
import org.hibernate.annotations.common.util.impl.LoggerFactory
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.RESTCONF_TOPOLOGY_CONFIG_PATH
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfConstants.Companion.RESTCONF_TOPOLOGY_OPER_PATH
import org.onap.ccsdk.cds.blueprintsprocessor.rest.restClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintRetryException
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService

/**
 * Register the Restconf module exposed dependency
 */
val log = LoggerFactory.logger(AbstractScriptComponentFunction::class.java)!!

fun AbstractScriptComponentFunction.restconfClientService(selector: String): BlueprintWebClientService {
    return BlueprintDependencyService.restClientService(selector)
}

fun AbstractScriptComponentFunction.restconfClientService(jsonNode: JsonNode): BlueprintWebClientService {
    return BlueprintDependencyService.restClientService(jsonNode)
}

/**
 * Generic Mount function
 */
suspend fun AbstractScriptComponentFunction.restconfMountDeviceJson(
    webClientService: BlueprintWebClientService,
    deviceId: String,
    payload: Any
) {
    restconfMountDevice(webClientService, deviceId, payload, mutableMapOf("Content-Type" to "application/json"))
}

/**
 * Generic Mount function
 */
suspend fun AbstractScriptComponentFunction.restconfMountDevice(
    webClientService: BlueprintWebClientService,
    deviceId: String,
    payload: Any,
    headers: Map<String, String> = mutableMapOf("Content-Type" to "application/xml")
) {
    val mountUrl = restconfDeviceConfigPath(deviceId)
    val mountCheckUrl = restconfDeviceOperPath(deviceId)
    restconfMountDevice(webClientService, payload, mountUrl, mountCheckUrl, headers)
}

/**
 * Generic Mount function
 * This function mount the given deviceId and verify if device mounted successfully.
 * This function take mount url and mount verify url as parameters.
 */
suspend fun AbstractScriptComponentFunction.restconfMountDevice(
    webClientService: BlueprintWebClientService,
    payload: Any,
    mountUrl: String,
    mountVerifyUrl: String,
    headers: Map<String, String> = mutableMapOf("Content-Type" to "application/xml"),
    expectedMountResult: String = """netconf-node-topology:connection-status":"connected"""
) {
    log.info("sending mount request, url: $mountUrl")
    log.debug("sending mount request, payload: $payload")
    val mountResult =
        webClientService.exchangeResource(RestconfRequestType.PUT.name, mountUrl, payload as String, headers)

    if (mountResult.status !in RestconfConstants.HTTP_SUCCESS_RANGE) {
        throw BlueprintProcessorException("Failed to mount device with url($mountUrl) ")
    }

    /** Check device has mounted */
    val mountCheckExecutionBlock: suspend (Int) -> String =
        { tryCount: Int ->
            val result = webClientService.exchangeResource(RestconfRequestType.GET.name, mountVerifyUrl, "")
            if (!result.body.contains(expectedMountResult)) {
                throw BlueprintRetryException("Wait for device with url($mountUrl) to mount")
            }
            log.info("NF was mounted successfully on ODL")
            result.body
        }

    log.info("url for ODL status check: $mountVerifyUrl")
    webClientService.retry(10, 0, 1000, mountCheckExecutionBlock)
}

/**
 * Generic Configure function
 * @return The WebClientResponse from the request
 */
suspend fun AbstractScriptComponentFunction.restconfApplyDeviceConfig(
    webClientService: BlueprintWebClientService,
    deviceId: String,
    configletResourcePath: String,
    configletToApply: Any,
    additionalHeaders: Map<String, String> = mutableMapOf("Content-Type" to "application/yang.patch+xml")
): BlueprintWebClientService.WebClientResponse<String> {
    log.debug("headers: $additionalHeaders")
    log.info("configuring device: $deviceId, Configlet: $configletToApply")
    val applyConfigUrl = restconfDeviceConfigPath(deviceId, configletResourcePath)
    return webClientService.exchangeResource(RestconfRequestType.PATCH.name, applyConfigUrl, configletToApply as String, additionalHeaders)
}

suspend fun AbstractScriptComponentFunction.restconfDeviceConfig(
    webClientService: BlueprintWebClientService,
    deviceId: String,
    configletResourcePath: String
): BlueprintWebClientService.WebClientResponse<String> {
    return getRequest(webClientService, restconfDeviceConfigPath(deviceId, configletResourcePath))
}

/**
 * Generic UnMount function
 */
suspend fun AbstractScriptComponentFunction.restconfUnMountDevice(
    webClientService: BlueprintWebClientService,
    deviceId: String
) {
    deleteRequest(webClientService, restconfDeviceConfigPath(deviceId))
}

/**
 * Generic PUT/PATCH/POST request function
 */
suspend fun AbstractScriptComponentFunction.genericPutPatchPostRequest(
    webClientService: BlueprintWebClientService,
    requestUrl: String,
    requestType: RestconfRequestType,
    payload: Any,
    headers: Map<String, String> = mutableMapOf("Content-Type" to "application/xml")
): BlueprintWebClientService.WebClientResponse<String> {
    when (requestType) {
        RestconfRequestType.PUT -> log.info("sending PUT request, url: $requestUrl")
        RestconfRequestType.PATCH -> log.info("sending PATCH request, url: $requestUrl")
        RestconfRequestType.POST -> log.info("sending POST request, url: $requestUrl")
        else -> throw BlueprintProcessorException("Illegal request type, only POST, PUT or PATCH allowed.")
    }
    return webClientService.exchangeResource(requestType.name, requestUrl, payload as String, headers)
}

/**
 * GET request function
 */
suspend fun AbstractScriptComponentFunction.getRequest(
    webClientService: BlueprintWebClientService,
    requestUrl: String
): BlueprintWebClientService.WebClientResponse<String> {
    val retryTimes = 10
    val mountCheckExecutionBlock: suspend (Int) -> BlueprintWebClientService.WebClientResponse<String> =
        { tryCount: Int ->
            val result = genericGetOrDeleteRequest(webClientService, requestUrl, RestconfRequestType.GET)
            if (result.status !in RestconfConstants.HTTP_SUCCESS_RANGE && tryCount < retryTimes - 1) {
                throw BlueprintRetryException("Failed to read url($requestUrl) to mount")
            }
            log.info("NF was mounted successfully on ODL")
            result
        }

    return webClientService.retry(retryTimes, 0, 1000, mountCheckExecutionBlock)
}

/**
 * DELETE request function
 */
suspend fun AbstractScriptComponentFunction.deleteRequest(
    webClientService: BlueprintWebClientService,
    requestUrl: String
): BlueprintWebClientService.WebClientResponse<String> {
    return genericGetOrDeleteRequest(webClientService, requestUrl, RestconfRequestType.DELETE)
}

/**
 * Generic GET/DELETE request function
 */
suspend fun AbstractScriptComponentFunction.genericGetOrDeleteRequest(
    webClientService: BlueprintWebClientService,
    requestUrl: String,
    requestType: RestconfRequestType
): BlueprintWebClientService.WebClientResponse<String> {
    when (requestType) {
        RestconfRequestType.GET -> log.info("sending GET request, url: $requestUrl")
        RestconfRequestType.DELETE -> log.info("sending DELETE request, url: $requestUrl")
        else -> throw BlueprintProcessorException("Illegal request type, only GET and DELETE allowed.")
    }
    return webClientService.exchangeResource(requestType.name, requestUrl, "")
}

suspend fun AbstractScriptComponentFunction.restconfPath(
    restconfDatastore: RestconfRequestDatastore,
    deviceId: String,
    specificPath: String = ""
): String {
    return when (restconfDatastore) {
        RestconfRequestDatastore.OPERATIONAL -> {
            restconfDeviceOperPath(deviceId, specificPath)
        }
        RestconfRequestDatastore.CONFIG -> {
            restconfDeviceConfigPath(deviceId, specificPath)
        }
    }
}

private fun AbstractScriptComponentFunction.restconfDeviceConfigPath(
    deviceId: String,
    specificPath: String = ""
): String {
    if (specificPath.isBlank()) {
        return "$RESTCONF_TOPOLOGY_CONFIG_PATH$deviceId"
    }
    return "$RESTCONF_TOPOLOGY_CONFIG_PATH$deviceId/$specificPath"
}

private fun AbstractScriptComponentFunction.restconfDeviceOperPath(
    deviceId: String,
    specificPath: String = ""
): String {
    if (specificPath.isBlank()) {
        return "$RESTCONF_TOPOLOGY_OPER_PATH$deviceId"
    }
    return "$RESTCONF_TOPOLOGY_OPER_PATH$deviceId/$specificPath"
}

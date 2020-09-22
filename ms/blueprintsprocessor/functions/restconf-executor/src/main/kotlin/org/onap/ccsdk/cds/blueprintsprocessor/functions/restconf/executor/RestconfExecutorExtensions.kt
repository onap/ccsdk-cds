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

import org.hibernate.annotations.common.util.impl.LoggerFactory
import org.onap.ccsdk.cds.blueprintsprocessor.rest.restClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintRetryException
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService

/**
 * Register the Restconf module exposed dependency
 */

val log = LoggerFactory.logger(AbstractScriptComponentFunction::class.java)!!

fun AbstractScriptComponentFunction.restconfClientService(selector: String): BlueprintWebClientService {
    return BluePrintDependencyService.restClientService(selector)
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

    val mountUrl = "/restconf/config/network-topology:network-topology/topology/topology-netconf/node/$deviceId"
    log.info("sending mount request, url: $mountUrl")
    webClientService.exchangeResource("PUT", mountUrl, payload as String, headers)

    /** Check device has mounted */
    val mountCheckUrl = "/restconf/operational/network-topology:network-topology/topology/topology-netconf/node/$deviceId"

    val expectedResult = """"netconf-node-topology:connection-status":"connected""""
    val mountCheckExecutionBlock: suspend (Int) -> String = { tryCount: Int ->
        val result = webClientService.exchangeResource("GET", mountCheckUrl, "")
        if (result.body.contains(expectedResult)) {
            log.info("NF was mounted successfully on ODL")
            result.body
        } else {
            throw BluePrintRetryException("Wait for device($deviceId) to mount")
        }
    }

    log.info("url for ODL status check: $mountCheckUrl")
    webClientService.retry<String>(10, 0, 1000, mountCheckExecutionBlock)
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
    val applyConfigUrl = "/restconf/config/network-topology:network-topology/topology/topology-netconf/node/" +
        "$deviceId/$configletResourcePath"
    return webClientService.exchangeResource("PATCH", applyConfigUrl, configletToApply as String, additionalHeaders)
}

suspend fun AbstractScriptComponentFunction.restconfDeviceConfig(
    webClientService: BlueprintWebClientService,
    deviceId: String,
    configletResourcePath: String
):
    BlueprintWebClientService.WebClientResponse<String> {

        val configPathUrl = "/restconf/config/network-topology:network-topology/topology/topology-netconf/node/" +
            "$deviceId/$configletResourcePath"
        log.debug("sending GET request,  url: $configPathUrl")
        return webClientService.exchangeResource("GET", configPathUrl, "")
    }

/**
 * Generic UnMount function
 */
suspend fun AbstractScriptComponentFunction.restconfUnMountDevice(
    webClientService: BlueprintWebClientService,
    deviceId: String,
    payload: String
) {
    val unMountUrl = "/restconf/config/network-topology:network-topology/topology/topology-netconf/node/$deviceId"
    log.info("sending unMount request, url: $unMountUrl")
    webClientService.exchangeResource("DELETE", unMountUrl, "")
}

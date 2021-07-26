/*
* ============LICENSE_START=======================================================
*  Copyright (C) 2020 Nordix Foundation.
* ================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
 */

package cba.pnf.config.aai

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.contentFromResolvedArtifactNB
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.storedContentFromResolvedArtifactNB
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfMountDevice
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfApplyDeviceConfig
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfUnMountDevice
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfDeviceConfig
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService.WebClientResponse

class RestconfConfigDeploy : AbstractScriptComponentFunction() {

    private val CONFIGLET_TEMPLATE_NAME = "config-assign"
    private val CONFIGLET_RESOURCE_PATH = "yang-ext:mount/mynetconf:netconflist"
    private val RESTCONF_SERVER_IDENTIFIER = "sdnc"
    private val mapper = ObjectMapper()
    private val log = logger(AbstractScriptComponentFunction::class.java)

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("Started execution of process method")
        try {
            // Extract Resolution key & Device ID
            val resolutionKey = getDynamicProperties("resolution-key").asText()
            log.info("resolution_key: $resolutionKey")
            val deviceID: String = requestPayloadActionProperty("config-deploy-properties")?.get(0)?.get("pnf-id")?.textValue()!!
            log.info("device_id: $deviceID")
            val webclientService = restconfClientService(RESTCONF_SERVER_IDENTIFIER)

            try {
                // Mount the device
                val mountPayload = contentFromResolvedArtifactNB("config-deploy")
                log.debug("Mounting Device : $deviceID")
                restconfMountDevice(webclientService, deviceID, mountPayload, mutableMapOf("Content-Type" to "application/json"))

                // Log the current configuration for the subtree
                val currentConfig: Any = restconfDeviceConfig(webclientService, deviceID, CONFIGLET_RESOURCE_PATH)
                log.info("Current configuration subtree : $currentConfig")
                // Apply configlet
                val result = restconfApplyDeviceConfig(
                    webclientService, deviceID, CONFIGLET_RESOURCE_PATH,
                    storedContentFromResolvedArtifactNB(resolutionKey, CONFIGLET_TEMPLATE_NAME),
                    mutableMapOf("Content-Type" to "application/yang.patch+json")
                ) as WebClientResponse<*>

                val jsonResult = mapper.readTree((result.body).toString())

                if (jsonResult.get("ietf-yang-patch:yang-patch-status").get("errors") != null) {
                    log.error("There was an error configuring device")
                } else {
                    log.info("Device has been configured succesfully")
                }
            } catch (err: Exception) {
                log.error("an error occurred while configuring device {}", err)
            } finally {
                // Un mount device
                restconfUnMountDevice(webclientService, deviceID, "")
            }
        } catch (bpe: BluePrintProcessorException) {
            log.error("Error looking up server identifier ", bpe)
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Recover function called!")
        log.info("Execution request : $executionRequest")
        log.error("Exception", runtimeException)
    }
}

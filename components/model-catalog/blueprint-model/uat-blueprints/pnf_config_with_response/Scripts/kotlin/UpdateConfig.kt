/*
* ============LICENSE_START=======================================================
*  Copyright (C) 2019 Nordix Foundation.
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

package cba.pnf.config.with.response

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.contentFromResolvedArtifactNB
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.storedContentFromResolvedArtifactNB
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfMountDevice
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfApplyDeviceConfig
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfUnMountDevice
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfDeviceConfig
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService.WebClientResponse


class UpdateConfig : AbstractScriptComponentFunction() {
    private val CONFIGLET_RESOURCE_PATH = "yang-ext:mount/mynetconf:netconflist"
    private val RESTCONF_SERVER_IDENTIFIER = "sdnc"
    private val log = logger(UpdateConfig::class.java)

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("Started execution of process method")

        val deviceID: String = requestPayloadActionProperty("update-config-properties")?.get(0)?.get("nfName")?.textValue()!!
        val webclientService = restconfClientService(RESTCONF_SERVER_IDENTIFIER)

        try {
            val payload = contentFromResolvedArtifactNB("update-config")
            val mountPayload = contentFromResolvedArtifactNB("restconf-mount")
            val mapper = ObjectMapper()

            restconfMountDevice(webclientService, deviceID, mountPayload, mutableMapOf("Content-Type" to "application/json"))
            log.info("Device has been mounted successfully")

            log.info("Applying configuration to device")
            val result = restconfApplyDeviceConfig(webclientService, deviceID, CONFIGLET_RESOURCE_PATH,
                    payload,mutableMapOf("Content-Type" to "application/yang.patch+json")) as WebClientResponse<*>

            val jsonResult = mapper.readTree((result.body).toString())

            if(jsonResult.get("ietf-yang-patch:yang-patch-status").get("errors") != null) {
                log.error("There was an error configuring device")
            }  else {
                log.info("Device has been configured succesfully")
            }
        } catch (err: Exception) {
            log.error("An error occurred while configuring the device {}", err)
        } finally {
            restconfUnMountDevice(webclientService, deviceID, "")
            log.info("Device has been unmounted successfully")
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Recover function called!")
        log.info("Execution request : $executionRequest")
        log.error("Exception", runtimeException)
    }
}
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


package cba.pnf.swug

import com.fasterxml.jackson.databind.node.ObjectNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.contentFromResolvedArtifactNB
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfApplyDeviceConfig
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfDeviceConfig
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfMountDevice
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.restconfUnMountDevice
import org.onap.ccsdk.cds.blueprintsprocessor.rest.restClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintRetryException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils

class RestconfSoftwareUpgrade : AbstractScriptComponentFunction() {

    private val RESTCONF_SERVER_IDENTIFIER = "sdnc"
    private val CONFIGLET_RESOURCE_PATH = "yang-ext:mount/pnf-sw-upgrade:software-upgrade"
    private val log = logger(AbstractScriptComponentFunction::class.java)
    private val TARGET_SOFTWARE_PATH = "$CONFIGLET_RESOURCE_PATH/upgrade-package/"

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        // Extract Resolution key & Device ID
        val resolutionKey = getDynamicProperties("resolution-key").asText()
        val actionName = executionRequest.actionIdentifiers.actionName;
        val properties = requestPayloadActionProperty(actionName + "-properties")!!.get(0)
        val deviceId: String = properties.get("pnf-id").textValue()
        val client = BluePrintDependencyService.restClientService(RESTCONF_SERVER_IDENTIFIER)
        val targetSwVersion: String = properties.get("target-software-version").textValue()!!

        log.info("Blueprint invoked for $resolutionKey for SW Upgrade : $actionName for sw version $targetSwVersion on pnf: $deviceId")

        try {
            val mountPayload = contentFromResolvedArtifactNB("mount-node")
            log.debug("Mount Payload : $mountPayload")
            restconfMountDevice(client, deviceId, mountPayload, mutableMapOf("Content-Type" to "application/json"))

            when (actionName) {
                Action.PRE_CHECK.action -> processPrecheck(client, deviceId)
                Action.DOWNLOAD_NE_SW.action -> processDownloadNeSw(client, deviceId, targetSwVersion)
                Action.ACTIVATE_NE_SW.action -> processActivateNeSw(client, deviceId, targetSwVersion)
                Action.POST_CHECK.action -> processPostcheck(client, deviceId, targetSwVersion)
                Action.CANCEL.action -> processCancel(client, deviceId)
                else -> throw BluePrintException("Invalid Action sent to CDS")
            }

        } catch (err: Exception) {
            log.error("an error occurred while configuring device {}", err)
        } finally {
            restconfUnMountDevice(client, deviceId, "")
        }
    }

    private suspend fun processPrecheck(client: BlueprintWebClientService, deviceId: String) {
        log.debug("In PNF SW upgrade : processPreCheck")
        //Log the current configuration for the subtree
        val currentConfig: BlueprintWebClientService.WebClientResponse<String> = restconfDeviceConfig(client, deviceId, CONFIGLET_RESOURCE_PATH)
        log.debug("Current configuration on the pnf is : ${currentConfig.body}")
        val payloadObject = JacksonUtils.jsonNode(currentConfig.body) as ObjectNode
        log.debug("Current sw version on pnf : ${payloadObject?.get("software-upgrade")?.get("upgrade-package")?.get(0)?.get("software-version")?.asText()}")
        log.info("PNF is Healthy!")
    }

    private suspend fun processDownloadNeSw(client: BlueprintWebClientService, deviceId: String, targetSwVersion: String) {
        log.debug("In PNF SW upgrade : processDownloadNeSw")
        //Check if there is existing config for the targeted software version
        val currentConfig: BlueprintWebClientService.WebClientResponse<String> = restconfDeviceConfig(client, deviceId, CONFIGLET_RESOURCE_PATH)
        val payloadObject = JacksonUtils.jsonNode(currentConfig.body) as ObjectNode
        val swIDInitialisedToDownload: String? = checkIfSwVersionInitialised(payloadObject, targetSwVersion)

        var downloadConfigPayload: String
        when {
            swIDInitialisedToDownload != null -> {
                downloadConfigPayload = contentFromResolvedArtifactNB("configure")
                downloadConfigPayload = downloadConfigPayload.replace("%id%", swIDInitialisedToDownload)
            }
            else -> downloadConfigPayload = contentFromResolvedArtifactNB("download-ne-sw")
        }
        downloadConfigPayload = downloadConfigPayload.replace("%actionName%", Action.DOWNLOAD_NE_SW.name)
        log.info("Config Payload to start download : $downloadConfigPayload")

        //Apply configlet
        restconfApplyDeviceConfig(client, deviceId, CONFIGLET_RESOURCE_PATH, downloadConfigPayload,
                mutableMapOf("Content-Type" to "application/yang.patch+json"))

        //Poll PNF until download completes
        checkExecution(Action.DOWNLOAD_NE_SW,client, deviceId, targetSwVersion)
    }

    private suspend fun processActivateNeSw(client: BlueprintWebClientService, deviceId: String, targetSwVersion: String) {
        log.debug("In PNF SW upgrade : processActivateNeSw")
        //Check if the software is downloaded and ready to be activated
        if (checkIfSwReadyToActivate(client, deviceId, targetSwVersion)) {
            var activateConfigPayload: String = contentFromResolvedArtifactNB("configure")
            activateConfigPayload = activateConfigPayload.replace("%actionName%", Action.ACTIVATE_NE_SW.name)
            log.info("Config Payload to start activate : $activateConfigPayload")
            //Apply configlet
            restconfApplyDeviceConfig(client, deviceId, CONFIGLET_RESOURCE_PATH, activateConfigPayload,
                    mutableMapOf("Content-Type" to "application/yang.patch+json"))

            //Poll PNF until Activation completes
            checkExecution(Action.ACTIVATE_NE_SW,client, deviceId, targetSwVersion)
        } else {
            throw BluePrintRetryException("Software Download not completed for device($deviceId) to activate sw version: $targetSwVersion")
        }
    }

    private suspend fun processPostcheck(client: BlueprintWebClientService, deviceId: String, targetSwVersion: String) {
        log.info("In PNF SW upgrade : processPostcheck")
        //Log the current configuration for the subtree
        val currentConfig: BlueprintWebClientService.WebClientResponse<String> = restconfDeviceConfig(client, deviceId, TARGET_SOFTWARE_PATH.plus(targetSwVersion))
        val configBody = JacksonUtils.jsonNode(currentConfig.body) as ObjectNode
        log.debug("Current sw version on pnf : ${configBody?.get("upgrade-package")?.get(0)?.get("software-version")?.asText()}")
        log.info("PNF is healthy post activation!")
    }

    private fun processCancel(client: BlueprintWebClientService, deviceID: String) {
        //This is for future implementation of cancel step during software upgrade
        log.info("In PNF SW upgrade : processCancel")
    }

    private fun checkIfSwVersionInitialised(payloadObject: ObjectNode, targetSwVersion: String): String? {
        payloadObject?.get("software-upgrade")?.get("upgrade-package")?.iterator()?.forEach { item ->
            log.debug("Each Item $item")
            if (targetSwVersion == item?.get("software-version")?.asText() &&
                    Action.PRE_CHECK.completionStatus == item?.get("current-status")?.asText()) {
                return item.get("id")?.textValue()
            }
        }
        return null
    }

    private suspend fun checkExecution(action: Action, client: BlueprintWebClientService, deviceId: String, targetSwVersion: String) {
        val checkExecutionBlock: suspend (Int) -> String = { tryCount: Int ->
            val result = restconfDeviceConfig(client, deviceId, TARGET_SOFTWARE_PATH.plus(targetSwVersion))
            if (result.body.contains(action.completionStatus)) {
                log.info("${action.name} is complete")
                result.body
            } else {
                throw BluePrintRetryException("Waiting for device($deviceId) to activate sw version $targetSwVersion")
            }
        }
        client.retry<String>(10, 0, 1000, checkExecutionBlock)

    }

    private suspend fun checkIfSwReadyToActivate(client: BlueprintWebClientService, deviceId: String, targetSwVersion: String): Boolean {
        val currentConfig: BlueprintWebClientService.WebClientResponse<String> = restconfDeviceConfig(client, deviceId, TARGET_SOFTWARE_PATH.plus(targetSwVersion))
        val configBody = JacksonUtils.jsonNode(currentConfig.body) as ObjectNode
        return configBody?.get("upgrade-package")?.get(0)?.get("current-status")?.textValue() == Action.DOWNLOAD_NE_SW.completionStatus
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Recover function called!")
        log.info("Execution request : $executionRequest")
        log.error("Exception", runtimeException)
    }
}

enum class Action(val action: String, val completionStatus: String) {
    PRE_CHECK("precheck", "INITIALIZED"),
    DOWNLOAD_NE_SW("downloadNeSw", "DOWNLOAD_COMPLETED"),
    ACTIVATE_NE_SW("activateNeSw", "ACTIVATION_COMPLETED"),
    POST_CHECK("postcheck", "ACTIVATION_COMPLETED"),
    CANCEL("cancel", "CANCELLED")
}
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

        // Extract request properties
        val model = validatedPayload(executionRequest)

        log.info(
            "Blueprint invoked for ${model.resolutionKey} for SW Upgrade : " +
                "${model.action} for sw version ${model.targetSwVersion} on pnf: ${model.deviceId}"
        )

        try {
            val mountPayload = contentFromResolvedArtifactNB("mount-node")
            log.debug("Mount Payload : $mountPayload")
            restconfMountDevice(model.client, model.deviceId, mountPayload, mutableMapOf("Content-Type" to "application/json"))

            when (model.action) {
                Action.PRE_CHECK -> processPreCheck(model)
                Action.DOWNLOAD_NE_SW -> processDownloadNESw(model)
                Action.ACTIVATE_NE_SW -> processActivateNESw(model)
                Action.POST_CHECK -> processPostCheck(model)
                Action.CANCEL -> processCancel(model)
            }
        } catch (err: Exception) {
            log.error("an error occurred while configuring device {}", err)
        } finally {
            restconfUnMountDevice(model.client, model.deviceId, "")
        }
    }

    private fun validatedPayload(executionRequest: ExecutionServiceInput): SoftwareUpgradeModel {
        val properties = requestPayloadActionProperty(executionRequest.actionIdentifiers.actionName + "-properties")!!.get(0)
        if (!properties?.get("pnf-id")?.textValue().isNullOrEmpty() &&
            !properties?.get("target-software-version")?.textValue().isNullOrEmpty()
        ) {
            return SoftwareUpgradeModel(
                getDynamicProperties("resolution-key").asText(),
                BluePrintDependencyService.restClientService(RESTCONF_SERVER_IDENTIFIER),
                properties.get("pnf-id").textValue(), properties.get("target-software-version").textValue(),
                Action.getEnumFromActionName(executionRequest.actionIdentifiers.actionName)
            )
        } else {
            throw BluePrintException("Invalid parameters sent to CDS. Request parameters pnf-id or target-software-version missing")
        }
    }

    private suspend fun processPreCheck(model: SoftwareUpgradeModel) {
        log.debug("In PNF SW upgrade : processPreCheck")
        // Log the current configuration for the subtree
        val payloadObject = getCurrentConfig(model)
        log.debug(
            "Current sw version on pnf : ${
                payloadObject.get("software-upgrade")?.get("upgrade-package")?.get(0)?.get("software-version")?.asText()
            }"
        )
        log.info("PNF is Healthy!")
    }

    private suspend fun processDownloadNESw(model: SoftwareUpgradeModel) {
        log.debug("In PNF SW upgrade : processDownloadNESw")
        // Check if there is existing config for the targeted software version

        var downloadConfigPayload: String
        if (checkIfSwReadyToPerformAction(Action.PRE_CHECK, model)) {
            downloadConfigPayload = contentFromResolvedArtifactNB("configure")
            downloadConfigPayload = downloadConfigPayload.replace("%id%", model.yangId)
        } else {
            downloadConfigPayload = contentFromResolvedArtifactNB("download-ne-sw")
            model.yangId = model.targetSwVersion
        }
        downloadConfigPayload = downloadConfigPayload.replace("%actionName%", Action.DOWNLOAD_NE_SW.name)
        log.info("Config Payload to start download : $downloadConfigPayload")

        // Apply configlet
        restconfApplyDeviceConfig(
            model.client, model.deviceId, CONFIGLET_RESOURCE_PATH, downloadConfigPayload,
            mutableMapOf("Content-Type" to "application/yang.patch+json")
        )

        // Poll PNF for Download action's progress
        checkExecution(model)
    }

    private suspend fun processActivateNESw(model: SoftwareUpgradeModel) {
        log.debug("In PNF SW upgrade : processActivateNESw")
        // Check if the software is downloaded and ready to be activated
        if (checkIfSwReadyToPerformAction(Action.DOWNLOAD_NE_SW, model)) {
            var activateConfigPayload: String = contentFromResolvedArtifactNB("configure")
            activateConfigPayload = activateConfigPayload.replace("%actionName%", Action.ACTIVATE_NE_SW.name)
            activateConfigPayload = activateConfigPayload.replace("%id%", model.yangId)
            log.info("Config Payload to start activate : $activateConfigPayload")
            // Apply configlet
            restconfApplyDeviceConfig(
                model.client, model.deviceId, CONFIGLET_RESOURCE_PATH, activateConfigPayload,
                mutableMapOf("Content-Type" to "application/yang.patch+json")
            )

            // Poll PNF for Activate action's progress
            checkExecution(model)
        } else {
            throw BluePrintRetryException("Software Download not completed for device(${model.deviceId}) to activate sw version: ${model.targetSwVersion}")
        }
    }

    private suspend fun processPostCheck(model: SoftwareUpgradeModel) {
        log.info("In PNF SW upgrade : processPostCheck")
        // Log the current configuration for the subtree
        if (checkIfSwReadyToPerformAction(Action.POST_CHECK, model)) {
            log.info("PNF is healthy post activation!")
        }
    }

    private fun processCancel(model: SoftwareUpgradeModel) {
        // This is for future implementation of cancel step during software upgrade
        log.info("In PNF SW upgrade : processCancel")
    }

    private suspend fun getCurrentConfig(model: SoftwareUpgradeModel): ObjectNode {
        val currentConfig: BlueprintWebClientService.WebClientResponse<String> =
            restconfDeviceConfig(model.client, model.deviceId, CONFIGLET_RESOURCE_PATH)
        return JacksonUtils.jsonNode(currentConfig.body) as ObjectNode
    }

    private suspend fun checkExecution(model: SoftwareUpgradeModel) {
        val checkExecutionBlock: suspend (Int) -> String = {
            val result = restconfDeviceConfig(model.client, model.deviceId, TARGET_SOFTWARE_PATH.plus(model.yangId))
            if (result.body.contains(model.action.completionStatus)) {
                log.info("${model.action.name} is complete")
                result.body
            } else {
                throw BluePrintRetryException("Waiting for device(${model.deviceId}) to activate sw version ${model.targetSwVersion}")
            }
        }
        model.client.retry<String>(10, 0, 1000, checkExecutionBlock)
    }

    private suspend fun checkIfSwReadyToPerformAction(action: Action, model: SoftwareUpgradeModel): Boolean {
        val configBody = getCurrentConfig(model)
        configBody.get("software-upgrade")?.get("upgrade-package")?.iterator()?.forEach { item ->
            if (model.targetSwVersion == item.get("software-version")?.asText() &&
                action.completionStatus == item?.get("current-status")?.asText()
            ) {
                model.yangId = item.get("id").textValue()
                return true
            }
        }
        return false
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Recover function called!")
        log.info("Execution request : $executionRequest")
        log.error("Exception", runtimeException)
    }
}

enum class Action(val actionName: String, val completionStatus: String) {
    PRE_CHECK("preCheck", "INITIALIZED"),
    DOWNLOAD_NE_SW("downloadNESw", "DOWNLOAD_COMPLETED"),
    ACTIVATE_NE_SW("activateNESw", "ACTIVATION_COMPLETED"),
    POST_CHECK("postCheck", "ACTIVATION_COMPLETED"),
    CANCEL("cancel", "CANCELLED")
    ;

    companion object {

        fun getEnumFromActionName(name: String): Action {
            for (value in values()) {
                if (value.actionName == name) return value
            }
            throw BluePrintException("Invalid Action sent to CDS")
        }
    }
}

data class SoftwareUpgradeModel(
    val resolutionKey: String,
    val client: BlueprintWebClientService,
    val deviceId: String,
    val targetSwVersion: String,
    val action: Action,
    var yangId: String = ""
)

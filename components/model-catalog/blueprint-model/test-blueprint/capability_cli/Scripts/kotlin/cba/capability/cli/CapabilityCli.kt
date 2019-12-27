/*
 *  Copyright © 2019 IBM.
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

package cba.capability.cli

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.sshClientService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService

open class Check : AbstractScriptComponentFunction() {

    private val log = logger(Check::class)

    override fun getName(): String {
        return "Check"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("Executing process : ${executionRequest.payload}")

        val data = requestPayloadActionProperty("data")?.first()
            ?: throw BluePrintProcessorException("Failed to load payload data properties.")

        log.info("Data : ${data.asJsonString()}")

        val checkCommands = mashTemplateNData("command-template", data.asJsonString())

        log.info("Check Commands :$checkCommands")

        // Get the Device Information from the Relationship Model
        val deviceInformation = relationshipProperty("ssh-connection-config", "connection-config")

        log.info("Device Info :$deviceInformation")

        // Get the Client Service
        val sshClientService = BluePrintDependencyService.sshClientService(deviceInformation)

        log.info("Client service is ready")
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Executing Recovery")
    }
}

/*
 *  Copyright © 2019 IBM
 *  Modifications Copyright © 2019 IBM, Bell Canada.
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

@file:Suppress("unused")

package internal.scripts

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.cli.executor.CliScriptComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentScriptExecutor
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.sshClientService
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.slf4j.LoggerFactory

open class TestCliScriptFunction : CliScriptComponentFunction() {

    private val log = LoggerFactory.getLogger(TestCliScriptFunction::class.java)!!

    override fun getName(): String {
        return "TestCliScriptFunction"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("Executing process ...")
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Executing Recovery")
    }
}


open class Check : CliScriptComponentFunction() {

    private val log = LoggerFactory.getLogger(AbstractScriptComponentFunction::class.java)!!

    override fun getName(): String {
        return "Check"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        // Get the Device Information from the DSL Model
        val deviceInformation = bluePrintRuntimeService.resolveDSLExpression("device-properties")

        // Get the Client Service
        val sshClientService = BluePrintDependencyService.sshClientService(deviceInformation)

        sshClientService.startSessionNB()

        // Read Commands
        val commands = readLinesFromArtifact("command-template")

        // Execute multiple Commands
        val responseLog = sshClientService.executeCommandsNB(commands, 5000)

        // Close Session
        sshClientService.closeSessionNB()

        // Set the Response Data
        setAttribute(ComponentScriptExecutor.RESPONSE_DATA, responseLog.asJsonPrimitive())

        log.info("Executing process")
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Executing Recovery")
    }
}

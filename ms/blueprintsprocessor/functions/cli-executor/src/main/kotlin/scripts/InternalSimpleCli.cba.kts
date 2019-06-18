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

@file:Suppress("unused")

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.cli.executor.CliComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.functions.cli.executor.ComponentCliExecutor
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintTemplateService
import org.slf4j.LoggerFactory

open class TestCliScriptFunction(bluePrintTemplateService: BluePrintTemplateService) : CliComponentFunction(bluePrintTemplateService) {

    private val log = LoggerFactory.getLogger(CliComponentFunction::class.java)!!

    override fun getName(): String {
        return "SimpleCliConfigure"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("Executing process")
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Executing Recovery")
    }
}


open class Check(bluePrintTemplateService: BluePrintTemplateService) : CliComponentFunction(bluePrintTemplateService) {

    private val log = LoggerFactory.getLogger(CliComponentFunction::class.java)!!

    override fun getName(): String {
        return "Check"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        // Get the Device Information from the DSL Model
        val deviceInformation = bluePrintRuntimeService.resolveDSLExpression("device-properties")

        // Get the Client Service
        val sshClientService = bluePrintSshLibPropertyService().blueprintSshClientService(deviceInformation)

        sshClientService.startSessionNB()

        // Read Commands
        val commands = readCommandLinesFromArtifact("command-template")

        // Execute multiple Commands
        val responseLog = sshClientService.executeCommandsNB(commands, 5000)

        // Close Session
        sshClientService.closeSessionNB()

        // Set the Response Data
        setAttribute(ComponentCliExecutor.RESPONSE_DATA, responseLog.asJsonPrimitive())

        log.info("Executing process")
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Executing Recovery")
    }
}

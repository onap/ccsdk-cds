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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.*
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ExecutionServiceConstant
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.RemoteScriptExecutionService
import org.onap.ccsdk.cds.controllerblueprints.command.api.ResponseStatus
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.checkFileExists
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotBlank
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.lang.Exception

@ConditionalOnBean(name = [ExecutionServiceConstant.SERVICE_GRPC_REMOTE_SCRIPT_EXECUTION])
@Component("component-remote-python-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentRemotePythonExecutor(private val remoteScriptExecutionService: RemoteScriptExecutionService)
    : AbstractComponentFunction() {

    private val log = LoggerFactory.getLogger(ComponentRemotePythonExecutor::class.java)!!

    companion object {
        const val INPUT_ENDPOINT_SELECTOR = "endpoint-selector"
        const val INPUT_DYNAMIC_PROPERTIES = "dynamic-properties"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        log.info("Processing : $operationInputs")

        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
        val blueprintName = bluePrintContext.name()
        val blueprintVersion = bluePrintContext.version()

        val operationAssignment: OperationAssignment = bluePrintContext
            .nodeTemplateInterfaceOperation(nodeTemplateName, interfaceName, operationName)

        val artifactName: String = operationAssignment.implementation?.primary
            ?: throw BluePrintProcessorException("missing primary field to get artifact name for node template ($nodeTemplateName)")

        val artifactDefinition =
            bluePrintRuntimeService.resolveNodeTemplateArtifactDefinition(nodeTemplateName, artifactName)

        checkNotBlank(artifactDefinition.file) { "couldn't get python script path($artifactName)" }

        val pythonScript = normalizedFile(bluePrintContext.rootPath, artifactDefinition.file)

        checkFileExists(pythonScript) { "python script(${pythonScript.absolutePath}) doesn't exists" }

        val endPointSelector = getOperationInput(INPUT_ENDPOINT_SELECTOR)
        val dynamicProperties = getOperationInput(INPUT_DYNAMIC_PROPERTIES)

        // TODO("Python execution command and Resolve some expressions with dynamic properties")
        val scriptCommand = pythonScript.absolutePath

        val dependencies = operationAssignment.implementation?.dependencies

        try {
            // Open GRPC Connection
            remoteScriptExecutionService.init(endPointSelector.asText())

            var executionLogs = ""

            // If dependencies are defined, then install in remote server
            if (dependencies != null && dependencies.isNotEmpty()) {
                val prepareEnvInput = PrepareRemoteEnvInput(requestId = processId,
                    remoteIdentifier = RemoteIdentifier(blueprintName = blueprintName,
                        blueprintVersion = blueprintVersion),
                    remoteScriptType = RemoteScriptType.PYTHON,
                    packages = dependencies
                )
                val prepareEnvOutput = remoteScriptExecutionService.prepareEnv(prepareEnvInput)
                check(prepareEnvOutput.status == StatusType.SUCCESS) {
                    "failed to get prepare remote env response status for requestId(${prepareEnvInput.requestId})"
                }
                executionLogs = prepareEnvOutput.response
            }

            val remoteExecutionInput = RemoteScriptExecutionInput(
                requestId = processId,
                remoteIdentifier = RemoteIdentifier(blueprintName = blueprintName, blueprintVersion = blueprintVersion),
                remoteScriptType = RemoteScriptType.PYTHON,
                command = scriptCommand)
            val remoteExecutionOutput = remoteScriptExecutionService.executeCommand(remoteExecutionInput)
            check(remoteExecutionOutput.status == StatusType.SUCCESS) {
                "failed to get prepare remote command response status for requestId(${remoteExecutionOutput.requestId})"
            }
            executionLogs += remoteExecutionOutput.response

            // Set Output Attributes
            bluePrintRuntimeService.setNodeTemplateAttributeValue(nodeTemplateName,
                "execution-logs", JacksonUtils.jsonNodeFromObject(executionLogs))

        } catch (e: Exception) {
            log.error("", e)
        } finally {
            remoteScriptExecutionService.close()
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBluePrintError()
            .addError("Failed in ComponentJythonExecutor : ${runtimeException.message}")
    }
}
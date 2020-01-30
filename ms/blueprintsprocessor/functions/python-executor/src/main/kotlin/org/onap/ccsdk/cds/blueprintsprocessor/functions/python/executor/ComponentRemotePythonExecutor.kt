/*
 *  Copyright Â© 2019 IBM.
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

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.*
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ExecutionServiceConstant
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.RemoteScriptExecutionService
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@ConditionalOnBean(name = [ExecutionServiceConstant.SERVICE_GRPC_REMOTE_SCRIPT_EXECUTION])
@Component("component-remote-python-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentRemotePythonExecutor(private val remoteScriptExecutionService: RemoteScriptExecutionService)
    : AbstractComponentFunction() {

    private val log = LoggerFactory.getLogger(ComponentRemotePythonExecutor::class.java)!!

    companion object {
        const val INPUT_ENDPOINT_SELECTOR = "endpoint-selector"
        const val INPUT_DYNAMIC_PROPERTIES = "dynamic-properties"
        const val INPUT_ARGUMENT_PROPERTIES = "argument-properties"

        const val INPUT_COMMAND = "command"
        const val INPUT_PACKAGES = "packages"
        const val DEFAULT_SELECTOR = "remote-python"
        const val INPUT_ENV_PREPARE_TIMEOUT = "env-prepare-timeout"
        const val INPUT_EXECUTE_TIMEOUT = "execution-timeout"

        const val ATTRIBUTE_EXEC_CMD_STATUS = "status"
        const val ATTRIBUTE_PREPARE_ENV_LOG = "prepare-environment-logs"
        const val ATTRIBUTE_EXEC_CMD_LOG = "execute-command-logs"
        const val ATTRIBUTE_RESPONSE_DATA = "response-data"
        const val DEFAULT_ENV_PREPARE_TIMEOUT_IN_SEC = 120
        const val DEFAULT_EXECUTE_TIMEOUT_IN_SEC = 180
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        log.debug("Processing : $operationInputs")

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
        val dynamicProperties = getOptionalOperationInput(INPUT_DYNAMIC_PROPERTIES)
        val packages = getOptionalOperationInput(INPUT_PACKAGES)?.returnNullIfMissing()

        val argsNode = getOptionalOperationInput(INPUT_ARGUMENT_PROPERTIES)?.returnNullIfMissing()

        // This prevents unescaping values, as well as quoting the each parameter, in order to allow for spaces in values
        val args = getOptionalOperationInput(INPUT_ARGUMENT_PROPERTIES)?.returnNullIfMissing()
            ?.rootFieldsToMap()?.toSortedMap()?.values?.joinToString(" ") { formatNestedJsonNode(it) }

        val command = getOperationInput(INPUT_COMMAND).asText()

        /**
         * Timeouts that are specific to the command executor.
         * Note: the interface->input->timeout is the component level timeout.
         */
        val envPrepTimeout = getOptionalOperationInput(INPUT_ENV_PREPARE_TIMEOUT)?.asInt()
            ?: DEFAULT_ENV_PREPARE_TIMEOUT_IN_SEC
        val executionTimeout = getOptionalOperationInput(INPUT_EXECUTE_TIMEOUT)?.asInt()
            ?: DEFAULT_EXECUTE_TIMEOUT_IN_SEC

        var scriptCommand = command.replace(pythonScript.name, pythonScript.absolutePath)
        if (args != null && args.isNotEmpty()) {
            scriptCommand = scriptCommand.plus(" ").plus(args)
        }

        try {
            // Open GRPC Connection
            if (DEFAULT_SELECTOR == endPointSelector.asText()) {
                remoteScriptExecutionService.init(endPointSelector.asText())
            } else {
                // Get endpoint from DSL
                val endPointSelectorJson = bluePrintRuntimeService.resolveDSLExpression(endPointSelector.asText())
                remoteScriptExecutionService.init(endPointSelectorJson)
            }

            // If packages are defined, then install in remote server
            if (packages != null) {
                val prepareEnvInput = PrepareRemoteEnvInput(requestId = processId,
                    remoteIdentifier = RemoteIdentifier(blueprintName = blueprintName,
                        blueprintVersion = blueprintVersion),
                    packages = packages,
                    timeOut = envPrepTimeout.toLong()

                )
                val prepareEnvOutput = remoteScriptExecutionService.prepareEnv(prepareEnvInput)
                log.info("$ATTRIBUTE_PREPARE_ENV_LOG - ${prepareEnvOutput.response}")
                val logs = prepareEnvOutput.response
                val logsEnv = logs.toString().asJsonPrimitive()
                setAttribute(ATTRIBUTE_PREPARE_ENV_LOG, logsEnv)

                if (prepareEnvOutput.status != StatusType.SUCCESS) {
                    setAttribute(ATTRIBUTE_EXEC_CMD_LOG, "N/A".asJsonPrimitive())
                    setNodeOutputErrors(prepareEnvOutput.status.name, logsEnv)
                } else {
                    setNodeOutputProperties(prepareEnvOutput.status.name.asJsonPrimitive(), logsEnv, "".asJsonPrimitive())
                }
            }
            else {
                //set env preparation log to empty...
                setAttribute(ATTRIBUTE_PREPARE_ENV_LOG,"".asJsonPrimitive());
            }
        } catch (grpcEx: io.grpc.StatusRuntimeException) {
            val grpcErrMsg = "Command failed during env. preparation... timeout($envPrepTimeout) requestId ($processId)."
            setAttribute(ATTRIBUTE_PREPARE_ENV_LOG, grpcErrMsg.asJsonPrimitive())
            setNodeOutputErrors(status = grpcErrMsg, message = "${grpcEx.status}".asJsonPrimitive())
            log.error(grpcErrMsg, grpcEx)
            addError(grpcErrMsg)
        } catch (e: Exception) {
            val timeoutErrMsg = "Command executor failed during env. preparation.. timeout($envPrepTimeout) requestId ($processId)."
            setAttribute(ATTRIBUTE_PREPARE_ENV_LOG, e.message.asJsonPrimitive())
            setNodeOutputErrors(status = timeoutErrMsg, message = "${e.message}".asJsonPrimitive())
            log.error("Failed to process on remote executor requestId ($processId)", e)
            addError(timeoutErrMsg)
        }
        // if Env preparation was successful, then proceed with command execution in this Env
        if (bluePrintRuntimeService.getBluePrintError().errors.isEmpty()) {
            try {
                // Populate command execution properties and pass it to the remote server
                val properties = dynamicProperties?.returnNullIfMissing()?.rootFieldsToMap() ?: hashMapOf()

                val remoteExecutionInput = RemoteScriptExecutionInput(
                    requestId = processId,
                    remoteIdentifier = RemoteIdentifier(blueprintName = blueprintName, blueprintVersion = blueprintVersion),
                    command = scriptCommand,
                    properties = properties,
                    timeOut = executionTimeout.toLong())


                val remoteExecutionOutputDeferred = GlobalScope.async {
                    remoteScriptExecutionService.executeCommand(remoteExecutionInput)
                }

                val remoteExecutionOutput = withTimeout(executionTimeout * 1000L) {
                    remoteExecutionOutputDeferred.await()
                }

                checkNotNull(remoteExecutionOutput) {
                    "Error: Request-id $processId did not return a restul from remote command execution."
                }


                val logs = JacksonUtils.jsonNodeFromObject(remoteExecutionOutput.response)
                if (remoteExecutionOutput.status != StatusType.SUCCESS) {
                    setNodeOutputErrors(remoteExecutionOutput.status.name, logs, remoteExecutionOutput.payload)
                } else {
                    setNodeOutputProperties(remoteExecutionOutput.status.name.asJsonPrimitive(), logs,
                        remoteExecutionOutput.payload)
                }
            } catch (timeoutEx: TimeoutCancellationException) {
                val timeoutErrMsg = "Command executor timed out executing after $executionTimeout seconds requestId ($processId)"
                setNodeOutputErrors(status = timeoutErrMsg, message = "".asJsonPrimitive())
                log.error(timeoutErrMsg, timeoutEx)
            } catch (grpcEx: io.grpc.StatusRuntimeException) {
                val timeoutErrMsg = "Command executor timed out executing after $executionTimeout seconds requestId ($processId)"
                setNodeOutputErrors(status = timeoutErrMsg, message = "".asJsonPrimitive())
                log.error("Command executor time out during GRPC call", grpcEx)
            } catch (e: Exception) {
                log.error("Failed to process on remote executor requestId ($processId)", e)
            }
        }
        log.debug("Trying to close GRPC channel. request ($processId)")
        remoteScriptExecutionService.close()
    }


    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBluePrintError()
            .addError("Failed in ComponentRemotePythonExecutor : ${runtimeException.message}")
    }

    private fun formatNestedJsonNode(node: JsonNode): String {
        val sb = StringBuilder()
        if (node.isValueNode) {
            sb.append(" $node")
        } else {
            node.forEach { sb.append(" $it") }
        }
        return sb.toString()
    }

    /**
     * Utility function to set the output properties of the executor node
     */
    private fun setNodeOutputProperties(status: JsonNode, message: JsonNode, artifacts: JsonNode) {
        setAttribute(ATTRIBUTE_EXEC_CMD_STATUS, status)
        log.info("Executor status   : $status")
        setAttribute(ATTRIBUTE_RESPONSE_DATA, artifacts)
        log.info("Executor artifacts: $artifacts")
        setAttribute(ATTRIBUTE_EXEC_CMD_LOG, message)
        log.info("Executor message  : $message")
    }

    /**
     * Utility function to set the output properties and errors of the executor node, in cas of errors
     */
    private fun setNodeOutputErrors(status: String, message: JsonNode, artifacts: JsonNode = "".asJsonPrimitive()) {
        setAttribute(ATTRIBUTE_EXEC_CMD_STATUS, status.asJsonPrimitive())
        log.info("Executor status   : $status")
        setAttribute(ATTRIBUTE_EXEC_CMD_LOG, message)
        log.info("Executor message  : $message")
        setAttribute(ATTRIBUTE_RESPONSE_DATA, artifacts)
        log.info("Executor artifacts: $artifacts")
        addError(status, ATTRIBUTE_EXEC_CMD_LOG, message.toString())
    }
}

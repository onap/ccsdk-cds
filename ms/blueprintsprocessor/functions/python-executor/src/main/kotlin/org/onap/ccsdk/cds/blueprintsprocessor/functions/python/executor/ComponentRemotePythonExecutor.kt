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
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.PrepareRemoteEnvInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteIdentifier
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteScriptExecutionInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StatusType
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ExecutionServiceConstant
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.RemoteScriptExecutionService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.checkFileExists
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotBlank
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.returnNullIfMissing
import org.onap.ccsdk.cds.controllerblueprints.core.rootFieldsToMap
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@ConditionalOnBean(name = [ExecutionServiceConstant.SERVICE_GRPC_REMOTE_SCRIPT_EXECUTION])
@Component("component-remote-python-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentRemotePythonExecutor(
    private val remoteScriptExecutionService: RemoteScriptExecutionService,
    private var bluePrintPropertiesService: BluePrintPropertiesService
) : AbstractComponentFunction() {

    private val log = LoggerFactory.getLogger(ComponentRemotePythonExecutor::class.java)!!

    companion object {
        const val SELECTOR_CMD_EXEC = "blueprintsprocessor.remote-script-command"
        const val INPUT_ENDPOINT_SELECTOR = "endpoint-selector"
        const val INPUT_DYNAMIC_PROPERTIES = "dynamic-properties"
        const val INPUT_ARGUMENT_PROPERTIES = "argument-properties"

        const val INPUT_COMMAND = "command"
        const val INPUT_PACKAGES = "packages"
        const val DEFAULT_SELECTOR = "remote-python"
        const val INPUT_ENV_PREPARE_TIMEOUT = "env-prepare-timeout"
        const val INPUT_EXECUTE_TIMEOUT = "execution-timeout"

        const val STEP_PREPARE_ENV = "prepare-env"
        const val STEP_EXEC_CMD = "execute-command"
        const val ATTRIBUTE_EXEC_CMD_STATUS = "status"
        const val ATTRIBUTE_PREPARE_ENV_LOG = "prepare-environment-logs"
        const val ATTRIBUTE_EXEC_CMD_LOG = "execute-command-logs"
        const val ATTRIBUTE_RESPONSE_DATA = "response-data"
        const val DEFAULT_ENV_PREPARE_TIMEOUT_IN_SEC = 120
        const val DEFAULT_EXECUTE_TIMEOUT_IN_SEC = 180
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        log.debug("Processing : $operationInputs")

        val isLogResponseEnabled = bluePrintPropertiesService.propertyBeanType("$SELECTOR_CMD_EXEC.response.log.enabled", Boolean::class.java)

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

        // component level timeout should be => env_prep_timeout + execution_timeout
        val timeout = implementation.timeout

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
                val prepareEnvInput = PrepareRemoteEnvInput(
                    requestId = processId,
                    remoteIdentifier = RemoteIdentifier(
                        blueprintName = blueprintName,
                        blueprintVersion = blueprintVersion),
                    packages = packages,
                    timeOut = envPrepTimeout.toLong()

                )
                val prepareEnvOutput = remoteScriptExecutionService.prepareEnv(prepareEnvInput)
                log.info("$ATTRIBUTE_PREPARE_ENV_LOG - ${prepareEnvOutput.response}")
                val logs = JacksonUtils.jsonNodeFromObject(prepareEnvOutput.response)
                val logsEnv = logs.toString().asJsonPrimitive()
                setAttribute(ATTRIBUTE_PREPARE_ENV_LOG, logsEnv)

                if (prepareEnvOutput.status != StatusType.SUCCESS) {
                    val errorMessage = prepareEnvOutput.payload
                    setNodeOutputErrors(prepareEnvOutput.status.name,
                        STEP_PREPARE_ENV,
                        logs,
                        errorMessage,
                        isLogResponseEnabled
                    )
                } else {
                    setNodeOutputProperties(prepareEnvOutput.status.name.asJsonPrimitive(),
                        STEP_PREPARE_ENV,
                        logsEnv,
                        "".asJsonPrimitive(),
                        isLogResponseEnabled
                    )
                }
            } else {
                // set env preparation log to empty...
                setAttribute(ATTRIBUTE_PREPARE_ENV_LOG, "".asJsonPrimitive())
            }
        } catch (grpcEx: io.grpc.StatusRuntimeException) {
            val componentLevelWarningMsg = if (timeout < envPrepTimeout) "Note: component-level timeout ($timeout) is shorter than env-prepare timeout ($envPrepTimeout). " else ""
            val grpcErrMsg = "Command failed during env. preparation... timeout($envPrepTimeout) requestId ($processId). $componentLevelWarningMsg grpcError: ${grpcEx.status}"
            setAttribute(ATTRIBUTE_PREPARE_ENV_LOG, grpcErrMsg.asJsonPrimitive())
            setNodeOutputErrors(status = StatusType.FAILURE.name, step = STEP_PREPARE_ENV, artifacts = grpcErrMsg.asJsonPrimitive(), logging = isLogResponseEnabled)
            log.error(grpcErrMsg, grpcEx)
        } catch (e: Exception) {
            val timeoutErrMsg = "Command executor failed during env. preparation.. catch-all case timeout($envPrepTimeout) requestId ($processId). exception msg: ${e.message}"
            setAttribute(ATTRIBUTE_PREPARE_ENV_LOG, e.message.asJsonPrimitive())
            setNodeOutputErrors(status = StatusType.FAILURE.name, step = STEP_PREPARE_ENV, artifacts = timeoutErrMsg.asJsonPrimitive(), logging = isLogResponseEnabled)
            log.error(timeoutErrMsg, e)
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
                    timeOut = implementation.timeout.toLong())

                val remoteExecutionOutputDeferred = GlobalScope.async {
                    remoteScriptExecutionService.executeCommand(remoteExecutionInput)
                }

                val remoteExecutionOutput = withTimeout(implementation.timeout * 1000L) {
                    remoteExecutionOutputDeferred.await()
                }

                checkNotNull(remoteExecutionOutput) {
                    "Error: Request-id $processId did not return a result from remote command execution."
                }
                val logs = JacksonUtils.jsonNodeFromObject(remoteExecutionOutput.response)
                if (remoteExecutionOutput.status != StatusType.SUCCESS) {
                    setNodeOutputErrors(remoteExecutionOutput.status.name,
                        STEP_EXEC_CMD,
                        logs,
                        remoteExecutionOutput.payload,
                        isLogResponseEnabled
                    )
                } else {
                    setNodeOutputProperties(remoteExecutionOutput.status.name.asJsonPrimitive(),
                        STEP_EXEC_CMD,
                        logs,
                        remoteExecutionOutput.payload,
                        isLogResponseEnabled
                    )
                }
            } catch (timeoutEx: TimeoutCancellationException) {
                val componentLevelWarningMsg = if (timeout < executionTimeout) "Note: component-level timeout ($timeout) is shorter than execution timeout ($executionTimeout). " else ""
                val timeoutErrMsg = "Command executor execution timeout. DetailedMessage: (${timeoutEx.message}) requestId ($processId). $componentLevelWarningMsg"
                setNodeOutputErrors(status = StatusType.FAILURE.name,
                    step = STEP_EXEC_CMD,
                    logs = "".asJsonPrimitive(),
                    artifacts = timeoutErrMsg.asJsonPrimitive(),
                    logging = isLogResponseEnabled
                )
                log.error(timeoutErrMsg, timeoutEx)
            } catch (grpcEx: io.grpc.StatusRuntimeException) {
                val timeoutErrMsg = "Command executor timed out executing after $executionTimeout seconds requestId ($processId) grpcErr: ${grpcEx.status}"
                setNodeOutputErrors(status = StatusType.FAILURE.name,
                    step = STEP_EXEC_CMD,
                    logs = "".asJsonPrimitive(),
                    artifacts = timeoutErrMsg.asJsonPrimitive(),
                    logging = isLogResponseEnabled
                )
                log.error(timeoutErrMsg, grpcEx)
            } catch (e: Exception) {
                val timeoutErrMsg = "Command executor failed during process catch-all case requestId ($processId) timeout($envPrepTimeout) exception msg: ${e.message}"
                setNodeOutputErrors(status = StatusType.FAILURE.name, step = STEP_PREPARE_ENV, artifacts = timeoutErrMsg.asJsonPrimitive(), logging = isLogResponseEnabled)
                log.error(timeoutErrMsg, e)
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
    private fun setNodeOutputProperties(
        status: JsonNode = StatusType.FAILURE.name.asJsonPrimitive(),
        step: String,
        message: JsonNode,
        artifacts: JsonNode,
        logging: Boolean = true
    ) {

        setAttribute(ATTRIBUTE_EXEC_CMD_STATUS, status)
        setAttribute(ATTRIBUTE_RESPONSE_DATA, artifacts)
        setAttribute(ATTRIBUTE_EXEC_CMD_LOG, message)

        if (logging) {
            log.info("Executor status   : $step : $status")
            log.info("Executor artifacts: $step : $artifacts")
            log.info("Executor message  : $step : $message")
        }
    }

    /**
     * Utility function to set the output properties and errors of the executor node, in cas of errors
     */
    private fun setNodeOutputErrors(
        status: String,
        step: String,
        logs: JsonNode = "N/A".asJsonPrimitive(),
        artifacts: JsonNode,
        logging: Boolean = true
    ) {
        setAttribute(ATTRIBUTE_EXEC_CMD_STATUS, status.asJsonPrimitive())
        setAttribute(ATTRIBUTE_EXEC_CMD_LOG, logs)
        setAttribute(ATTRIBUTE_RESPONSE_DATA, artifacts)

        if (logging) {
            log.info("Executor status   : $step : $status")
            log.info("Executor artifacts: $step : $artifacts")
            log.info("Executor logs     : $step : $logs")
        }

        addError(status, step, logs.toString())
    }
}

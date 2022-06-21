/*
 *  Copyright Â© 2019 IBM.
 *  Modifications Copyright © 2020 Bell Canada.
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
import com.google.protobuf.ByteString
import io.grpc.Status
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.PrepareRemoteEnvInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteIdentifier
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteScriptExecutionInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteScriptExecutionOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteScriptUploadBlueprintInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StatusType
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.repository.BlueprintModelRepository
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ExecutionServiceConstant
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.RemoteScriptExecutionService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.checkFileExists
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotBlank
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.returnNullIfMissing
import org.onap.ccsdk.cds.controllerblueprints.core.rootFieldsToMap
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.File

@ConditionalOnBean(name = [ExecutionServiceConstant.SERVICE_GRPC_REMOTE_SCRIPT_EXECUTION])
@Component("component-remote-python-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentRemotePythonExecutor(
    private val remoteScriptExecutionService: RemoteScriptExecutionService,
    private val bluePrintPropertiesService: BluePrintPropertiesService,
    private val blueprintModelRepository: BlueprintModelRepository
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
        const val STEP_UPLOAD_CBA = "upload-cba"
        const val ATTRIBUTE_EXEC_CMD_STATUS = "status"
        const val ATTRIBUTE_PREPARE_ENV_LOG = "prepare-environment-logs"
        const val ATTRIBUTE_EXEC_CMD_LOG = "execute-command-logs"
        const val ATTRIBUTE_RESPONSE_DATA = "response-data"
        const val OUTPUT_RESPONSE_DATA = "response-data"
        const val OUTPUT_STATUS = "status"
        const val DEFAULT_ENV_PREPARE_TIMEOUT_IN_SEC = 120
        const val DEFAULT_EXECUTE_TIMEOUT_IN_SEC = 180
        const val DEFAULT_CBA_UPLOAD_TIMEOUT_IN_SEC = 30
        // Prometheus metrics counters
        const val CDS_BP_CE_GRPC_ERROR_TOTAL = "cds_bp_ce_grpc_error_total"
        const val CDS_BP_CE_TIMEOUT_ERROR_TOTAL = "cds_bp_ce_timeout_error_total"
        const val CDS_BP_CE_EXECUTION_DURATION_SECONDS = "cds_bp_ce_execution_duration_seconds"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        log.debug("Processing : $operationInputs")

        val isLogResponseEnabled = bluePrintPropertiesService.propertyBeanType("$SELECTOR_CMD_EXEC.response.log.enabled", Boolean::class.java)

        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
        val blueprintName = bluePrintContext.name()
        val blueprintVersion = bluePrintContext.version()

        // fetch the template (plus cba bindata) from repository
        val cbaModel = blueprintModelRepository.findByArtifactNameAndArtifactVersion(blueprintName, blueprintVersion)
        val blueprintUUID = cbaModel?.id!!
        val cbaBinData = ByteString.copyFrom(cbaModel?.blueprintModelContent?.content)
        val archiveType = cbaModel?.blueprintModelContent?.contentType // TODO: should be enum
        val remoteIdentifier = RemoteIdentifier(blueprintName = blueprintName, blueprintVersion = blueprintVersion, blueprintUUID = blueprintUUID)
        val originatorId = executionServiceInput.commonHeader.originatorId
        val subRequestId = executionServiceInput.commonHeader.subRequestId
        val requestId = processId

        val pythonScript = getScriptFile()
        checkFileExists(pythonScript) { "python script(${pythonScript.absolutePath}) doesn't exists" }

        val endPointSelector = getOperationInput(INPUT_ENDPOINT_SELECTOR)
        val dynamicProperties = getOptionalOperationInput(INPUT_DYNAMIC_PROPERTIES)
        var packages = getOptionalOperationInput(INPUT_PACKAGES)?.returnNullIfMissing()

        // This prevents unescaping values, as well as quoting the each parameter, in order to allow for spaces in values
        val args = getOptionalOperationInput(INPUT_ARGUMENT_PROPERTIES)?.returnNullIfMissing()
            ?.rootFieldsToMap()?.toSortedMap()?.values?.joinToString(" ") { formatNestedJsonNode(it) }

        val command = getOperationInput(INPUT_COMMAND).asText()
        val cbaNameVerUuid = "blueprintName($blueprintName) blueprintVersion($blueprintVersion) blueprintUUID($blueprintUUID)"

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
            if (packages == null) {
                // set env preparation log to empty...
                setAttribute(ATTRIBUTE_PREPARE_ENV_LOG, "".asJsonPrimitive())
                packages = mutableListOf<Object>().asJsonType()
            }
            prepareEnv(originatorId, requestId, subRequestId, remoteIdentifier, packages, envPrepTimeout, cbaNameVerUuid, archiveType, cbaBinData, isLogResponseEnabled)
            // in cases where the exception is caught in BP side due to timeout, we do not have `err_msg` returned by cmd-exec (inside `payload`),
            // hence `artifact` field will be empty
        } catch (grpcEx: io.grpc.StatusRuntimeException) {
            val errorType = if (grpcEx.status.code == Status.DEADLINE_EXCEEDED.code) CDS_BP_CE_TIMEOUT_ERROR_TOTAL else CDS_BP_CE_GRPC_ERROR_TOTAL
            meterRegistry.counter(errorType, commandExecutorMetricTags(executionServiceInput, STEP_PREPARE_ENV, getScriptName())).increment()

            val componentLevelWarningMsg = if (timeout < envPrepTimeout) "Note: component-level timeout ($timeout) is shorter than env-prepare timeout ($envPrepTimeout). " else ""
            val grpcErrMsg = "Command failed during env. preparation... timeout($envPrepTimeout) requestId ($processId).$componentLevelWarningMsg grpcError: (${grpcEx.cause?.message})"
            // no execution log in case of timeout (as cmd-exec side hasn't finished to transfer output)
            // set prepare-env-log to the error msg, and cmd-exec-log to empty
            setAttribute(ATTRIBUTE_PREPARE_ENV_LOG, grpcErrMsg.asJsonPrimitive())
            setNodeOutputErrors(STEP_PREPARE_ENV, "[]".asJsonPrimitive(), "{}".asJsonPrimitive(), isLogResponseEnabled)
            addError(StatusType.FAILURE.name, STEP_PREPARE_ENV, grpcErrMsg)
            log.error(grpcErrMsg, grpcEx)
        } catch (e: Exception) {
            meterRegistry.counter(CDS_BP_CE_GRPC_ERROR_TOTAL, commandExecutorMetricTags(executionServiceInput, STEP_PREPARE_ENV, getScriptName())).increment()

            val catchallErrMsg = "Command executor failed during env. preparation.. catch-all case. timeout($envPrepTimeout) requestId ($processId). exception msg: ${e.message}"
            // no environment prepare log from executor in case of timeout (as cmd-exec side hasn't finished to transfer output), set it to error msg. Execution logs is empty.
            setAttribute(ATTRIBUTE_PREPARE_ENV_LOG, catchallErrMsg.asJsonPrimitive())
            setNodeOutputErrors(STEP_PREPARE_ENV, "[]".asJsonPrimitive(), "{}".asJsonPrimitive(), isLogResponseEnabled)
            addError(StatusType.FAILURE.name, STEP_PREPARE_ENV, catchallErrMsg)
            log.error(catchallErrMsg, e)
        }
        // if Env preparation was successful, then proceed with command execution in this Env
        if (noBlueprintErrors()) {
            try {
                // Populate command execution properties and pass it to the remote server
                val properties = dynamicProperties?.returnNullIfMissing()?.rootFieldsToMap() ?: hashMapOf()

                val remoteExecutionInput = RemoteScriptExecutionInput(
                    originatorId = executionServiceInput.commonHeader.originatorId,
                    requestId = processId,
                    subRequestId = executionServiceInput.commonHeader.subRequestId,
                    remoteIdentifier = remoteIdentifier,
                    command = scriptCommand,
                    properties = properties,
                    timeOut = executionTimeout.toLong()
                )

                val exeCmdTimer = Timer.start()
                val remoteExecutionOutput = remoteScriptExecutionService.executeCommand(remoteExecutionInput)
                exeCmdTimer.stop(meterRegistry.timer(CDS_BP_CE_EXECUTION_DURATION_SECONDS, commandExecutorMetricTags(executionServiceInput, STEP_EXEC_CMD, getScriptName())))

                checkNotNull(remoteExecutionOutput) {
                    "Error: Request-id $processId did not return a result from remote command execution."
                }
                val logs = JacksonUtils.jsonNodeFromObject(remoteExecutionOutput.response)
                val returnedPayload = remoteExecutionOutput.payload
                val returnedErrMsg = remoteExecutionOutput.errMsg
                // In case of execution, `payload` (dictionary from Python execution) is preserved in `remoteExecutionOutput.payload`;
                // It would contain `err_msg` key. It is valid to return it.
                if (remoteExecutionOutput.status != StatusType.SUCCESS) {
                    setNodeOutputErrors(STEP_EXEC_CMD, logs, returnedPayload, isLogResponseEnabled)
                    // check if the payload is set (in case user Python script handles the error and sets the payload to return
                    val retErrMsg: String = if (returnedErrMsg.isNullOrEmpty()) "cmd-exec has failed, please see cmd-exec log for more details."
                    else "cmd-exec failed and returned: $returnedErrMsg"
                    addError(StatusType.FAILURE.name, STEP_EXEC_CMD, retErrMsg)
                } else {
                    setNodeOutputProperties(remoteExecutionOutput.status, STEP_EXEC_CMD, logs, returnedPayload, isLogResponseEnabled)
                } // In timeout exception cases, we don't have payload, hence `payload` is empty value.
            } catch (grpcEx: io.grpc.StatusRuntimeException) {
                val errorType = if (grpcEx.status.code == Status.DEADLINE_EXCEEDED.code) CDS_BP_CE_TIMEOUT_ERROR_TOTAL else CDS_BP_CE_GRPC_ERROR_TOTAL
                meterRegistry.counter(errorType, commandExecutorMetricTags(executionServiceInput, STEP_EXEC_CMD, getScriptName())).increment()

                val componentLevelWarningMsg =
                    if (timeout < executionTimeout) "Note: component-level timeout ($timeout) is shorter than execution timeout ($executionTimeout). " else ""
                val timeoutErrMsg =
                    "Command executor timed out executing after $executionTimeout seconds requestId ($processId). $componentLevelWarningMsg grpcErr: ${grpcEx.status}"
                setNodeOutputErrors(STEP_EXEC_CMD, listOf(timeoutErrMsg).asJsonType(), logging = isLogResponseEnabled)
                addError(StatusType.FAILURE.name, STEP_EXEC_CMD, timeoutErrMsg)
                log.error(timeoutErrMsg, grpcEx)
            } catch (e: Exception) {
                meterRegistry.counter(CDS_BP_CE_GRPC_ERROR_TOTAL, commandExecutorMetricTags(executionServiceInput, STEP_EXEC_CMD, getScriptName())).increment()
                val catchAllErrMsg = "Command executor failed during process catch-all case requestId ($processId) timeout($envPrepTimeout) exception msg: ${e.message}"
                setNodeOutputErrors(STEP_PREPARE_ENV, listOf(catchAllErrMsg).asJsonType(), logging = isLogResponseEnabled)
                addError(StatusType.FAILURE.name, STEP_EXEC_CMD, catchAllErrMsg)
                log.error(catchAllErrMsg, e)
            }
        }
        log.debug("Trying to close GRPC channel. request ($processId)")
        remoteScriptExecutionService.close()
    }

    private fun getScriptFile(): File {
        val context = bluePrintRuntimeService.bluePrintContext()
        val operationAssignment: OperationAssignment = context
            .nodeTemplateInterfaceOperation(nodeTemplateName, interfaceName, operationName)

        val artifactName: String = operationAssignment.implementation?.primary
            ?: throw BluePrintProcessorException("missing primary field to get artifact name for node template ($nodeTemplateName)")

        val artifactDefinition =
            bluePrintRuntimeService.resolveNodeTemplateArtifactDefinition(nodeTemplateName, artifactName)
        checkNotBlank(artifactDefinition.file) { "couldn't get python script path($artifactName)" }
        return normalizedFile(context.rootPath, artifactDefinition.file)
    }

    private fun getScriptName(): String {
        return getScriptFile().name
    }

    private fun commandExecutorMetricTags(executionServiceInput: ExecutionServiceInput, step: String, scriptName: String): MutableList<Tag> =
        executionServiceInput.actionIdentifiers.let {
            mutableListOf(
                Tag.of(BluePrintConstants.METRIC_TAG_BP_NAME, it.blueprintName),
                Tag.of(BluePrintConstants.METRIC_TAG_BP_VERSION, it.blueprintVersion),
                Tag.of(BluePrintConstants.METRIC_TAG_BP_ACTION, it.actionName),
                Tag.of(BluePrintConstants.METRIC_TAG_STEP, step),
                Tag.of(BluePrintConstants.METRIC_TAG_SCRIPT_NAME, scriptName)
            )
        }

    // wrapper for call to prepare_env step on cmd-exec - reupload CBA and call prepare env again if cmd-exec reported CBA uuid mismatch
    private suspend fun prepareEnv(originatorId: String, requestId: String, subRequestId: String, remoteIdentifier: RemoteIdentifier, packages: JsonNode, envPrepTimeout: Int, cbaNameVerUuid: String, archiveType: String?, cbaBinData: ByteString?, isLogResponseEnabled: Boolean, innerCall: Boolean = false) {
        val prepareEnvInput = PrepareRemoteEnvInput(
            originatorId = originatorId,
            requestId = requestId,
            subRequestId = subRequestId,
            remoteIdentifier = remoteIdentifier,
            packages = packages,
            timeOut = envPrepTimeout.toLong()
        )
        val preEnvTimer = Timer.start()
        val prepareEnvOutput = remoteScriptExecutionService.prepareEnv(prepareEnvInput)
        preEnvTimer.stop(meterRegistry.timer(CDS_BP_CE_EXECUTION_DURATION_SECONDS, commandExecutorMetricTags(executionServiceInput, STEP_PREPARE_ENV, getScriptName())))

        log.info("$ATTRIBUTE_PREPARE_ENV_LOG - ${prepareEnvOutput.response}")
        val logs = JacksonUtils.jsonNodeFromObject(prepareEnvOutput.response)
        setAttribute(ATTRIBUTE_PREPARE_ENV_LOG, logs)

        // there are no artifacts for env. prepare, but we reuse it for err_log...
        if (prepareEnvOutput.status != StatusType.SUCCESS) {
            // Check for the flag that blueprint is mismatched first, if so, reupload the blueprint
            if (prepareEnvOutput.payload.has("reupload_cba")) {
                log.info("Cmd-exec is missing the CBA $cbaNameVerUuid, it will be reuploaded.")
                uploadCba(remoteIdentifier, requestId, subRequestId, originatorId, archiveType, cbaBinData, cbaNameVerUuid, prepareEnvOutput, isLogResponseEnabled, logs)
                // call prepare_env again.
                if (!innerCall) {
                    log.info("Calling prepare environment again")
                    prepareEnv(originatorId, requestId, subRequestId, remoteIdentifier, packages, envPrepTimeout, cbaNameVerUuid, archiveType, cbaBinData, isLogResponseEnabled)
                } else {
                    meterRegistry.counter(CDS_BP_CE_GRPC_ERROR_TOTAL, commandExecutorMetricTags(executionServiceInput, STEP_PREPARE_ENV, getScriptName())).increment()
                    val errMsg = "Something is wrong: prepare_env step attempted to call itself too many times after upload CBA step!"
                    log.error(errMsg)
                    setNodeOutputErrors(STEP_PREPARE_ENV, "[]".asJsonPrimitive(), prepareEnvOutput.payload, isLogResponseEnabled)
                    addError(StatusType.FAILURE.name, STEP_PREPARE_ENV, errMsg)
                }
            } else {
                meterRegistry.counter(CDS_BP_CE_GRPC_ERROR_TOTAL, commandExecutorMetricTags(executionServiceInput, STEP_PREPARE_ENV, getScriptName())).increment()
                setNodeOutputErrors(STEP_PREPARE_ENV, "[]".asJsonPrimitive(), prepareEnvOutput.payload, isLogResponseEnabled)
                addError(StatusType.FAILURE.name, STEP_PREPARE_ENV, logs.toString())
            }
        } else {
            setNodeOutputProperties(prepareEnvOutput.status, STEP_PREPARE_ENV, logs, prepareEnvOutput.payload, isLogResponseEnabled)
        }
    }

    private suspend fun uploadCba(remoteIdentifier: RemoteIdentifier, requestId: String, subRequestId: String, originatorId: String, archiveType: String?, cbaBinData: ByteString?, cbaNameVerUuid: String, prepareEnvOutput: RemoteScriptExecutionOutput, isLogResponseEnabled: Boolean, logs: JsonNode) {
        val uploadCbaInput = RemoteScriptUploadBlueprintInput(
            remoteIdentifier = remoteIdentifier,
            requestId = requestId,
            subRequestId = subRequestId,
            originatorId = originatorId,
            timeOut = DEFAULT_CBA_UPLOAD_TIMEOUT_IN_SEC.toLong(),
            archiveType = archiveType!!,
            binData = cbaBinData!!
        )
        val uploadCbaTimer = Timer.start()
        val cbaUploadOutput = remoteScriptExecutionService.uploadBlueprint(uploadCbaInput)
        uploadCbaTimer.stop(meterRegistry.timer(CDS_BP_CE_EXECUTION_DURATION_SECONDS, commandExecutorMetricTags(executionServiceInput, STEP_UPLOAD_CBA, getScriptName())))

        if (cbaUploadOutput.status != StatusType.SUCCESS) {
            meterRegistry.counter(CDS_BP_CE_GRPC_ERROR_TOTAL, commandExecutorMetricTags(executionServiceInput, STEP_UPLOAD_CBA, getScriptName())).increment()
            log.error("Error uploading CBA $cbaNameVerUuid error(${cbaUploadOutput.payload})")
            setNodeOutputErrors(STEP_PREPARE_ENV, "[]".asJsonPrimitive(), prepareEnvOutput.payload, isLogResponseEnabled)
            addError(StatusType.FAILURE.name, STEP_PREPARE_ENV, logs.toString())
        } else {
            log.info("Finished uploading CBA $cbaNameVerUuid")
        }
    }

    private fun noBlueprintErrors() =
        bluePrintRuntimeService.getBluePrintError().stepErrors(stepName).isNullOrEmpty()

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        addError("Failed in ComponentRemotePythonExecutor : ${runtimeException.message}")
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
        status: StatusType,
        step: String,
        executionLogs: JsonNode,
        artifacts: JsonNode,
        logging: Boolean = true
    ) {

        setAttribute(ATTRIBUTE_EXEC_CMD_STATUS, status.name.asJsonPrimitive())
        setAttribute(ATTRIBUTE_EXEC_CMD_LOG, executionLogs)
        setAttribute(ATTRIBUTE_RESPONSE_DATA, artifacts)

        if (logging) {
            log.info("Executor status : $step : $status")
            log.info("Executor logs   : $step : $executionLogs")
            log.info("Executor artifacts: $step : $artifacts")
        }
    }

    /**
     * Utility function to set the output properties and errors of the executor node, in case of errors
     */
    private fun setNodeOutputErrors(
        step: String,
        executionLogs: JsonNode = "[]".asJsonPrimitive(),
        artifacts: JsonNode = "{}".asJsonPrimitive(),
        logging: Boolean = true
    ) {
        val status = StatusType.FAILURE.name
        setAttribute(ATTRIBUTE_EXEC_CMD_STATUS, status.asJsonPrimitive())
        setAttribute(ATTRIBUTE_EXEC_CMD_LOG, executionLogs)
        setAttribute(ATTRIBUTE_RESPONSE_DATA, artifacts)

        if (logging) {
            log.info("Executor status : $step : $status")
            log.info("Executor logs   : $step : $executionLogs")
            log.info("Executor artifacts: $step : $artifacts")
        }
    }
}

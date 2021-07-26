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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.PrepareRemoteEnvInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteScriptExecutionInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteScriptExecutionOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteScriptUploadBlueprintInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.RemoteScriptUploadBlueprintOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StatusType
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.repository.BlueprintModelRepository
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.RemoteScriptExecutionService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintError
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.putJsonElement
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ComponentRemotePythonExecutorTest {

    @Test
    fun testComponentRemotePythonExecutor() {
        runBlocking {
            val remoteScriptExecutionService = MockRemoteScriptExecutionService()

            val componentRemotePythonExecutor = ComponentRemotePythonExecutor(
                remoteScriptExecutionService,
                mockk<BluePrintPropertiesService>(),
                mockk<BlueprintModelRepository>()
            )

            val executionServiceInput =
                JacksonUtils.readValueFromClassPathFile(
                    "payload/requests/sample-remote-python-request.json",
                    ExecutionServiceInput::class.java
                )!!

            val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(
                "123456-1000",
                "./../../../../components/model-catalog/blueprint-model/test-blueprint/remote_scripts"
            )

            /** Load Workflow Inputs */
            bluePrintRuntimeService.assignWorkflowInputs(
                "execute-remote-python",
                executionServiceInput.payload.get("execute-remote-python-request")
            )

            val stepMetaData: MutableMap<String, JsonNode> = hashMapOf()
            stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE, "execute-remote-python")
            stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_INTERFACE, "ComponentRemotePythonExecutor")
            stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_OPERATION, "process")
            componentRemotePythonExecutor.bluePrintRuntimeService = bluePrintRuntimeService
            val stepInputData = StepData().apply {
                name = "execute-remote-python"
                properties = stepMetaData
            }
            executionServiceInput.stepData = stepInputData
            componentRemotePythonExecutor.applyNB(executionServiceInput)
        }
    }

    /**
     * Test cases for python executor to work with the process NB of remote
     * executor.
     */
    @Test
    fun testComponentRemotePythonExecutorProcessNB() {
        runBlocking {
            val remoteScriptExecutionService = MockRemoteScriptExecutionService()
            val componentRemotePythonExecutor = ComponentRemotePythonExecutor(
                remoteScriptExecutionService,
                mockk<BluePrintPropertiesService>(),
                mockk<BlueprintModelRepository>()
            )
            val bluePrintRuntime = mockk<DefaultBluePrintRuntimeService>("123456-1000")

            every { bluePrintRuntime.getBluePrintError() } answers { BluePrintError() } // successful case.
            every { bluePrintRuntime.setNodeTemplateAttributeValue(any(), any(), any()) } answers {}

            val input = getMockedOutput(bluePrintRuntime)
            componentRemotePythonExecutor.bluePrintRuntimeService = bluePrintRuntime
            componentRemotePythonExecutor.applyNB(input)
        }
    }

    /**
     * Mocked input information for remote python executor.
     */
    fun getMockedOutput(svc: DefaultBluePrintRuntimeService):
        ExecutionServiceInput {
            val stepMetaData: MutableMap<String, JsonNode> = hashMapOf()

            stepMetaData.putJsonElement(
                BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE,
                "execute-remote-python"
            )
            stepMetaData.putJsonElement(
                BluePrintConstants.PROPERTY_CURRENT_INTERFACE,
                "ComponentRemotePythonExecutor"
            )
            stepMetaData.putJsonElement(
                BluePrintConstants.PROPERTY_CURRENT_OPERATION, "process"
            )

            val mapper = ObjectMapper()
            val rootNode = mapper.createObjectNode()
            rootNode.put("ip-address", "0.0.0.0")
            rootNode.put("type", "rest")

            val operationalInputs: MutableMap<String, JsonNode> = hashMapOf()
            operationalInputs.putJsonElement(
                BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE,
                "execute-remote-python"
            )
            operationalInputs.putJsonElement(
                BluePrintConstants.PROPERTY_CURRENT_INTERFACE,
                "ComponentRemotePythonExecutor"
            )
            operationalInputs.putJsonElement(
                BluePrintConstants.PROPERTY_CURRENT_OPERATION, "process"
            )
            operationalInputs.putJsonElement("endpoint-selector", "aai")
            operationalInputs.putJsonElement("dynamic-properties", rootNode)
            operationalInputs.putJsonElement("command", "./run.sh")
            operationalInputs.putJsonElement("packages", "py")

            every {
                svc.resolveNodeTemplateInterfaceOperationInputs(
                    "execute-remote-python",
                    "ComponentRemotePythonExecutor", "process"
                )
            } returns operationalInputs

            val stepInputData = StepData().apply {
                name = "execute-remote-python"
                properties = stepMetaData
            }

            val executionServiceInput = JacksonUtils
                .readValueFromClassPathFile(
                    "payload/requests/sample-remote-python-request.json",
                    ExecutionServiceInput::class.java
                )!!
            executionServiceInput.stepData = stepInputData

            val operationOutputs = hashMapOf<String, JsonNode>()
            every {
                svc.resolveNodeTemplateInterfaceOperationOutputs(
                    "execute-remote-python",
                    "ComponentRemotePythonExecutor", "process"
                )
            } returns operationOutputs
            val bluePrintRuntimeService = BluePrintMetadataUtils.bluePrintRuntime(
                "123456-1000",
                "./../../../../components/model-" +
                    "catalog/blueprint-model/test-blueprint/" +
                    "remote_scripts"
            )
            every {
                svc.resolveNodeTemplateArtifactDefinition(
                    "execute-remote-python", "component-script"
                )
            } returns bluePrintRuntimeService.resolveNodeTemplateArtifactDefinition(
                "execute-remote-python", "component-script"
            )
            every {
                svc.setNodeTemplateAttributeValue(
                    "execute-remote-python", "prepare-environment-logs",
                    "prepared successfully".asJsonPrimitive()
                )
            } returns Unit
            every {
                svc.setNodeTemplateAttributeValue(
                    "execute-remote-python",
                    "execute-command-logs", "N/A".asJsonPrimitive()
                )
            } returns Unit
            every {
                svc.setNodeTemplateAttributeValue(
                    "execute-remote-python",
                    "execute-command-logs",
                    "processed successfully".asJsonPrimitive()
                )
            } returns Unit

            every {
                svc.resolveDSLExpression("aai")
            } returns """{"url" : "http://xxx.com"}""".asJsonType()

            every {
                svc.bluePrintContext()
            } returns bluePrintRuntimeService.bluePrintContext()
            return executionServiceInput
        }
}

class MockRemoteScriptExecutionService : RemoteScriptExecutionService {

    override suspend fun init(selector: Any) {
    }

    override suspend fun uploadBlueprint(uploadBpInput: RemoteScriptUploadBlueprintInput): RemoteScriptUploadBlueprintOutput {
        val uploadBpOutput = mockk<RemoteScriptUploadBlueprintOutput>()
        every { uploadBpOutput.payload } returns "[]".asJsonPrimitive()
        every { uploadBpOutput.status } returns StatusType.SUCCESS
        every { uploadBpOutput.requestId } returns "123456-1000"
        every { uploadBpOutput.subRequestId } returns "1234"
        return uploadBpOutput
    }

    override suspend fun prepareEnv(prepareEnvInput: PrepareRemoteEnvInput): RemoteScriptExecutionOutput {
        assertEquals(prepareEnvInput.requestId, "123456-1000", "failed to match request id")
        assertNotNull(prepareEnvInput.packages, "failed to get packages")

        val remoteScriptExecutionOutput = mockk<RemoteScriptExecutionOutput>()
        every { remoteScriptExecutionOutput.payload } returns "payload".asJsonPrimitive()
        every { remoteScriptExecutionOutput.response } returns listOf("prepared successfully")
        every { remoteScriptExecutionOutput.status } returns StatusType.SUCCESS
        return remoteScriptExecutionOutput
    }

    override suspend fun executeCommand(remoteExecutionInput: RemoteScriptExecutionInput): RemoteScriptExecutionOutput {
        assertEquals(remoteExecutionInput.requestId, "123456-1000", "failed to match request id")

        val remoteScriptExecutionOutput = mockk<RemoteScriptExecutionOutput>()
        every { remoteScriptExecutionOutput.payload } returns "payload".asJsonPrimitive()
        every { remoteScriptExecutionOutput.response } returns listOf("processed successfully")
        every { remoteScriptExecutionOutput.status } returns StatusType.SUCCESS
        return remoteScriptExecutionOutput
    }

    override suspend fun close() {
    }
}

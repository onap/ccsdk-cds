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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.*
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.RemoteScriptExecutionService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.putJsonElement
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class ComponentRemotePythonExecutorTest {

    @Test
    fun testComponentRemotePythonExecutor() {
        runBlocking {
            val remoteScriptExecutionService = MockRemoteScriptExecutionService()

            val componentRemotePythonExecutor = ComponentRemotePythonExecutor(remoteScriptExecutionService)

            val executionServiceInput = JacksonUtils.readValueFromClassPathFile("payload/requests/sample-activate-request.json",
                    ExecutionServiceInput::class.java)!!

            val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime("123456-1000",
                    "./../../../../components/model-catalog/blueprint-model/test-blueprint/remote_scripts")

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
}

class MockRemoteScriptExecutionService : RemoteScriptExecutionService {
    override suspend fun init(selector: String) {
    }

    override suspend fun prepareEnv(prepareEnvInput: PrepareRemoteEnvInput): RemoteScriptExecutionOutput {
        assertEquals(prepareEnvInput.requestId, "123456-1000", "failed to match request id")
        assertEquals(prepareEnvInput.remoteScriptType, RemoteScriptType.PYTHON, "failed to match script type")
        assertNotNull(prepareEnvInput.packages, "failed to get packages")

        val remoteScriptExecutionOutput = mockk<RemoteScriptExecutionOutput>()
        every { remoteScriptExecutionOutput.status } returns StatusType.SUCCESS
        return remoteScriptExecutionOutput
    }

    override suspend fun executeCommand(remoteExecutionInput: RemoteScriptExecutionInput): RemoteScriptExecutionOutput {
        assertEquals(remoteExecutionInput.requestId, "123456-1000", "failed to match request id")
        assertEquals(remoteExecutionInput.remoteScriptType, RemoteScriptType.PYTHON, "failed to match script type")
        
        val remoteScriptExecutionOutput = mockk<RemoteScriptExecutionOutput>()
        every { remoteScriptExecutionOutput.status } returns StatusType.SUCCESS
        return remoteScriptExecutionOutput
    }

    override suspend fun close() {

    }
}
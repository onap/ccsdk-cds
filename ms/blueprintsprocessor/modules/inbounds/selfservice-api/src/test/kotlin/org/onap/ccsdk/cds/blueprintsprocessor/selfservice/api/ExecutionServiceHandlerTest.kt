/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.coVerify
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.DatabaseStoreAuditService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.db.BlueprintAuditStatusRepository
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.db.BlueprintWorkflowAuditStatus
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractServiceFunction
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        MockServiceAction::class, SelfServiceApiTestConfiguration::class,
        ErrorCatalogTestConfiguration::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class ExecutionServiceHandlerTest {

    @MockBean
    lateinit var meterRegistry: MeterRegistry

    @Autowired
    lateinit var applicationContext: ApplicationContext

    private val blueprintAuditStatusRepository =
        mockk<BlueprintAuditStatusRepository>()

    private val testDatabaseStoreAuditService = DatabaseStoreAuditService(blueprintAuditStatusRepository)

    @Before
    fun init() {
        BluePrintDependencyService.inject(applicationContext)
    }

    @Test
    fun testExecuteServiceFunction() {
        val executionServiceInput = ExecutionServiceInput().apply {
            commonHeader = CommonHeader().apply {
                requestId = "1234"
                subRequestId = "1234-12"
                originatorId = "cds-test"
            }
            actionIdentifiers = ActionIdentifiers().apply {
                blueprintName = "default"
                blueprintVersion = "1.0.0"
                actionName = "mock-service-action"
            }
        }
        runBlocking {
            val executionServiceHandler = ExecutionServiceHandler(mockk(), mockk(), mockk(), mockk(), testDatabaseStoreAuditService, mockk(relaxed = true))
            val isServiceFunction = executionServiceHandler.checkServiceFunction(executionServiceInput)
            assertTrue(isServiceFunction, "failed to checkServiceFunction")
            val executionServiceOutput = executionServiceHandler.executeServiceFunction(executionServiceInput)
            assertNotNull(executionServiceOutput, "failed to get executionServiceOutput")
        }
    }

    @Test
    fun testPublishAuditFunction() {

        val jsonContent = JacksonUtils.getClassPathFileContent("execution-input/sample-payload.json")
        val json: ObjectNode = ObjectMapper().readTree(jsonContent) as ObjectNode

        val executionServiceInput = ExecutionServiceInput().apply {
            commonHeader = CommonHeader().apply {
                requestId = "1234"
                subRequestId = "1234-12"
                originatorId = "cds-test"
            }
            actionIdentifiers = ActionIdentifiers().apply {
                blueprintName = "default"
                blueprintVersion = "1.0.0"
                actionName = "mock-service-action"
                mode = "async"
            }
        }
        executionServiceInput.payload = json
        val publishAuditService = mockk<KafkaPublishAuditService>(relaxed = true)
        val wfAudit = createWorkflowAuditStatusRecord(1000)

        val executionServiceHandler = ExecutionServiceHandler(
            mockk(),
            mockk(),
            mockk(),
            publishAuditService,
            testDatabaseStoreAuditService,
            mockk(relaxed = true)
        )
        var testOutput: Long = 1000
        coEvery { publishAuditService.publishExecutionInput(ExecutionServiceInput()) } just Runs

        runBlocking {
            every { blueprintAuditStatusRepository.findById(testOutput) } returns wfAudit
            every { blueprintAuditStatusRepository.saveAndFlush(any<BlueprintWorkflowAuditStatus>()) } returns wfAudit
        }

        var executionServiceOutput: ExecutionServiceOutput? = null
        runBlocking {
            executionServiceOutput = executionServiceHandler.doProcess(executionServiceInput)
        }

        coVerify {
            publishAuditService.publishExecutionInput(executionServiceInput)
            publishAuditService.publishExecutionOutput(executionServiceInput.correlationUUID, executionServiceOutput!!)
            testOutput = testDatabaseStoreAuditService.storeExecutionInput(executionServiceInput)
        }
    }

    private fun createWorkflowAuditStatusRecord(
        id: Long
    ): BlueprintWorkflowAuditStatus {

        var blueprintWorkflowAuditStatus: BlueprintWorkflowAuditStatus =
            BlueprintWorkflowAuditStatus()
        blueprintWorkflowAuditStatus.id = id
        blueprintWorkflowAuditStatus.originatorId = "SDNC_DG"
        blueprintWorkflowAuditStatus.requestMode = "sync"
        blueprintWorkflowAuditStatus.requestId = "ab543-3asd4"
        blueprintWorkflowAuditStatus.subRequestId = "81c9-4910"
        blueprintWorkflowAuditStatus.status = "In progress"
        blueprintWorkflowAuditStatus.blueprintName = "multi-steps"
        blueprintWorkflowAuditStatus.blueprintVersion = "1.0.0"
        blueprintWorkflowAuditStatus.workflowName = "multi-steps-workflow"
        blueprintWorkflowAuditStatus.updatedBy = "CBA"
        blueprintWorkflowAuditStatus.requestMode = "sync"
        blueprintWorkflowAuditStatus.workflowTaskContent = "{\n" +
            "    \"multi-steps-workflow-request\": {\n" +
            "      \"multi-steps-workflow-properties\": {\n" +
            "        \"prop1\": \"testing\",\n" +
            "        \"prop2\": \"testing description\",\n" +
            "        \"prop3\": \"user name \",\n" +
            "        \"prop4\" : \"test project\"\n" +
            "      }\n" +
            "    }\n" +
            "  }"
        blueprintWorkflowAuditStatus.workflowResponseContent = " "
        return blueprintWorkflowAuditStatus
    }
}

@Service("mock-service-action")
class MockServiceAction : AbstractServiceFunction() {

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        val responsePayload = """{"answer" : "correct"}""".jsonAsJsonType()
        setResponsePayloadForAction(responsePayload)
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
    }
}

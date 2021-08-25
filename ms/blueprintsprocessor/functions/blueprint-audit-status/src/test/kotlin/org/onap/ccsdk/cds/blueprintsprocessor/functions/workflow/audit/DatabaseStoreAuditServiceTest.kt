package org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.db.BlueprintAuditStatusRepository
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.db.BlueprintWorkflowAuditStatus
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.util.Date
import kotlin.collections.ArrayList
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [TestDatabaseConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@EnableAutoConfiguration
class DatabaseStoreAuditServiceTest {

    private val blueprintAuditStatusRepository =
        mockk<BlueprintAuditStatusRepository>()

    private val databaseStoreAuditService = DatabaseStoreAuditService(blueprintAuditStatusRepository)

    @Test
    fun storeExecutionInputTest() {
        val executionServiceInput = JacksonUtils.readValueFromClassPathFile(
            "exec-serv-input/multistep-input.json",
            ExecutionServiceInput::class.java
        )!!
        val wfAudit1 = createWorkflowAuditStatusRecord(1000)
        var testOuput: Long = 0
        runBlocking {
            every { blueprintAuditStatusRepository.saveAndFlush(any<BlueprintWorkflowAuditStatus>()) } returns wfAudit1
            testOuput = databaseStoreAuditService.storeExecutionInput(executionServiceInput)
            assertNotNull(testOuput, "failed to resolve the resources")
        }
    }

    @Test
    fun storeExecutionOutputTest() {
        val executionServiceOutput = JacksonUtils.readValueFromClassPathFile(
            "exec-serv-output/multistep-output.json",
            ExecutionServiceOutput::class.java
        )!!
        val inputAudit = createWorkflowAuditStatusRecord(1001)
        val outputAudit = createWorkflowAuditStatusOutputRecord(1001)

        var testOutput: Long = 1001
        runBlocking {
            every { blueprintAuditStatusRepository.findById(testOutput) } returns inputAudit
            every { blueprintAuditStatusRepository.saveAndFlush(any<BlueprintWorkflowAuditStatus>()) } returns outputAudit
            databaseStoreAuditService.storeExecutionOutput(
                testOutput,
                "12345", executionServiceOutput
            )
        }
    }

    @Test(expected = Exception::class)
    fun storeExecutionOutputErrorTest() {
        val executionServiceOutput = JacksonUtils.readValueFromClassPathFile(
            "exec-serv-output/multistep-output.json",
            ExecutionServiceOutput::class.java
        )!!
        // val inputAudit = createWorkflowAuditStatusRecord(1001)
        val outputAudit = createWorkflowAuditStatusOutputRecord(1001)

        var testOutput: Long = -1
        runBlocking {
            every { blueprintAuditStatusRepository.findById(-1) } returns null
            every { blueprintAuditStatusRepository.saveAndFlush(any<BlueprintWorkflowAuditStatus>()) } returns outputAudit
            databaseStoreAuditService.storeExecutionOutput(
                testOutput,
                "12345", executionServiceOutput
            )
        }
    }

    @Test
    fun getWorkflowStatusByRequestIdAndSubRequestIdTest() {
        val inputAudit = createWorkflowAuditStatusList(1003)
        val testRequestId: String = "ab543-3asd4"
        val testSubRequestId: String = "81c9-4910"
        runBlocking {
            every {
                blueprintAuditStatusRepository.findByRequestIdAndSubRequestId(testRequestId, testSubRequestId)
            } returns inputAudit
            assertNotNull(
                inputAudit.get(0).blueprintName, "Blueprint Name should not be null"
            )
            assertNotNull(
                inputAudit.get(0).blueprintVersion, "Blueprint should not be null"
            )
            assertNotNull(
                inputAudit.get(0).requestId, "Request ID should not be null"
            )
            assertNotNull(
                inputAudit.get(0).subRequestId, "Subrequest ID should not be null"
            )
            assertNotNull(
                inputAudit.get(0).status, "Status should not be null"
            )
            assertNotNull(
                inputAudit.get(0).startDate, "Start Date should not be null"
            )
            assertNotNull(
                inputAudit.get(0).updatedBy, "Updatedby should not be null"
            )
            assertNotNull(
                inputAudit.get(0).updatedDate, "updated Date should not be null"
            )
            assertNotNull(
                inputAudit.get(0).originatorId, "Originator ID should not be null"
            )
            assertNotNull(
                inputAudit.get(0).requestMode, "Request Mode should not be null"
            )
            assertNotNull(
                inputAudit.get(0).id, "ID should not be null"
            )
            databaseStoreAuditService.getWorkflowStatusByRequestIdAndSubRequestId(testRequestId, testSubRequestId)
        }
    }

    @Test
    fun getWorkflowStatusByRequestIdTest() {
        val inputAudit = createWorkflowAuditStatusList(1004)
        val testRequestId: String = "ab543-3asd4"
        runBlocking {
            every { blueprintAuditStatusRepository.findByRequestId(testRequestId) } returns inputAudit
            databaseStoreAuditService.getWorkflowStatusByRequestId(testRequestId)
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
        blueprintWorkflowAuditStatus.updatedBy = DatabaseStoreAuditConstants.WORKFLOW_STATUS_UPDATEDBY
        blueprintWorkflowAuditStatus.endDate = Date()
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

    private fun createWorkflowAuditStatusOutputRecord(
        id: Long
    ): BlueprintWorkflowAuditStatus {

        var blueprintWorkflowAuditStatus: BlueprintWorkflowAuditStatus =
            BlueprintWorkflowAuditStatus()
        blueprintWorkflowAuditStatus.id = id
        blueprintWorkflowAuditStatus.originatorId = "SDNC_DG"
        blueprintWorkflowAuditStatus.requestMode = "sync"
        blueprintWorkflowAuditStatus.requestId = "ab543-3asd4"
        blueprintWorkflowAuditStatus.subRequestId = "81c9-4910"
        blueprintWorkflowAuditStatus.status = DatabaseStoreAuditConstants.WORKFLOW_STATUS_INPROGRESS
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
        blueprintWorkflowAuditStatus.workflowResponseContent = "{\n" +
            "  \"correlationUUID\": null,\n" +
            "  \"commonHeader\": {\n" +
            "    \"timestamp\": \"2021-08-05T08:18:35.690Z\",\n" +
            "    \"originatorId\": \"SDNC_DG\",\n" +
            "    \"requestId\": \"ab543-3asd4\",\n" +
            "    \"subRequestId\": \"81c9-4910\",\n" +
            "    \"flags\": null\n" +
            "  },\n" +
            "  \"actionIdentifiers\": {\n" +
            "    \"blueprintName\": \"multi-steps\",\n" +
            "    \"blueprintVersion\": \"1.0.0\",\n" +
            "    \"actionName\": \"multi-steps-workflow\",\n" +
            "    \"mode\": \"sync\"\n" +
            "  },\n" +
            "  \"status\": {\n" +
            "    \"code\": 200,\n" +
            "    \"eventType\": \"EVENT_COMPONENT_EXECUTED\",\n" +
            "    \"timestamp\": \"2021-08-05T08:18:35.727Z\",\n" +
            "    \"errorMessage\": null,\n" +
            "    \"message\": \"success\"\n" +
            "  },\n" +
            "  \"payload\": {\n" +
            "    \"multi-steps-workflow-response\": {}\n" +
            "  }\n" +
            "} "
        return blueprintWorkflowAuditStatus
    }

    private fun createWorkflowAuditStatusList(
        id: Long
    ): List<BlueprintWorkflowAuditStatus> {

        var blueprintWorkflowAuditStatus: BlueprintWorkflowAuditStatus =
            BlueprintWorkflowAuditStatus()
        blueprintWorkflowAuditStatus.id = id
        blueprintWorkflowAuditStatus.originatorId = "SDNC_DG"
        blueprintWorkflowAuditStatus.requestMode = "sync"
        blueprintWorkflowAuditStatus.requestId = "ab543-3asd4"
        blueprintWorkflowAuditStatus.subRequestId = "81c9-4910"
        blueprintWorkflowAuditStatus.status = DatabaseStoreAuditConstants.WORKFLOW_STATUS_INPROGRESS
        blueprintWorkflowAuditStatus.blueprintName = "multi-steps"
        blueprintWorkflowAuditStatus.blueprintVersion = "1.0.0"
        blueprintWorkflowAuditStatus.workflowName = "multi-steps-workflow"
        blueprintWorkflowAuditStatus.updatedBy = DatabaseStoreAuditConstants.WORKFLOW_STATUS_UPDATEDBY
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
        var testList: ArrayList<BlueprintWorkflowAuditStatus> = ArrayList<BlueprintWorkflowAuditStatus>()
        testList.add(blueprintWorkflowAuditStatus)
        return testList
    }
}

private infix fun Any.returns(nothing: Nothing?) {
}

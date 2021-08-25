package org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit

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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [TestDatabaseConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@EnableAutoConfiguration
class NoStoreAuditServiceTest {

    private val blueprintAuditStatusRepository =
        mockk<BlueprintAuditStatusRepository>()

    private val storeAuditService = NoStoreAuditService(blueprintAuditStatusRepository)

    @Test
    fun storeExecutionInputTest() {
        val executionServiceInput = JacksonUtils.readValueFromClassPathFile(
            "exec-serv-input/multistep-input.json",
            ExecutionServiceInput::class.java
        )!!
        var testOuput: Long = 0
        runBlocking {
            testOuput = storeAuditService.storeExecutionInput(executionServiceInput)
            assertEquals(-1, testOuput, "Failed to resolve the workflow")
        }
    }

    @Test
    fun storeExecutionOutputTest() {
        val executionServiceOutput = JacksonUtils.readValueFromClassPathFile(
            "exec-serv-output/multistep-output.json",
            ExecutionServiceOutput::class.java
        )!!
        var testOutput: Long = -1
        runBlocking {
            storeAuditService.storeExecutionOutput(
                testOutput, "12345", executionServiceOutput
            )
        }
    }

    @Test
    fun getWorkflowStatusByRequestIdAndSubRequestIdTest() {
        val testRequestId: String = "ab543-3asd4"
        val testSubRequestId: String = "81c9-4910"
        var testList: List<BlueprintWorkflowAuditStatus>? = null
        runBlocking {
            testList = storeAuditService.getWorkflowStatusByRequestIdAndSubRequestId(testRequestId, testSubRequestId)
            assertNotNull(testList, " Returned null instead of empty list ")
        }
    }

    @Test
    fun getWorkflowStatusByRequestIdTest() {
        val testRequestId: String = "ab543-3asd4"
        var testList: List<BlueprintWorkflowAuditStatus>? = null
        runBlocking {
            testList = storeAuditService.getWorkflowStatusByRequestId(testRequestId)
            assertNotNull(testList, " Returned null instead of empty list ")
        }
    }
}

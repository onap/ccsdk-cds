/*
 * Copyright © 2021 Aarna Networks, Inc.
 *           All rights reserved.
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

package org.onap.ccsdk.cds.blueprintsprocessor.workflow.api

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.DatabaseStoreAuditService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.db.BlueprintWorkflowAuditStatus
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import java.text.SimpleDateFormat
import java.util.List

@RunWith(SpringRunner::class)
@WebFluxTest
@ContextConfiguration(
    classes = [
        TestDatabaseConfiguration::class, ErrorCatalogTestConfiguration::class,
        WorkflowController::class, DatabaseStoreAuditService::class
    ]
)
@ComponentScan(
    basePackages = [
        "org.onap.ccsdk.cds.controllerblueprints.core.service",
        "org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit",
        "org.onap.ccsdk.cds.blueprintsprocessor.workflow.api"
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class WorkflowControllerTest {

    @Autowired
    lateinit var databaseStoreAuditService: DatabaseStoreAuditService
    @Autowired
    lateinit var testWebClient: WebTestClient

    @Test
    fun testWorkflowControllerHealthCheck() {
        runBlocking {
            testWebClient.get().uri("/api/v1/workflow/health-check")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .equals("Success")
        }
    }

    @Test
    fun testGetWorkFlowByRequestAndSubRequest() {

        val wfAudit1 = createWorkflowAuditStatusRecord(1000)
        val wfAudit2 = createWorkflowAuditStatusRecord(1001)
        runBlocking {

            storeToDB(wfAudit1)
            storeToDB(wfAudit2)
            val testRequestId: String = "ab543-3asd4"
            val testSubRequestId: String = "81c9-4910"
            testWebClient
                .get()
                .uri("/api/v1/workflow/audit-status/$testRequestId/$testSubRequestId")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .consumeWith {
                    val json = String(it.responseBody!!)
                    val typeFactory = JacksonUtils.objectMapper.typeFactory
                    val list: List<BlueprintWorkflowAuditStatus> = JacksonUtils.objectMapper.readValue(
                        json,
                        typeFactory.constructCollectionType(List::class.java, BlueprintWorkflowAuditStatus::class.java)
                    )
                    Assert.assertEquals(2, list.size)
                    assertEqual(list[0])
                    assertEqual(list[1])
                }
        }
    }

    @Test
    fun testEmptyRequestAndSubRequest() {

        val wfAudit1 = createWorkflowAuditStatusRecordSetTwo(1002)
        val wfAudit2 = createWorkflowAuditStatusRecordSetTwo(1003)
        runBlocking {

            storeToDB(wfAudit1)
            storeToDB(wfAudit2)
            val testRequestId: String = ""
            val testSubRequestId: String = ""
            testWebClient
                .get()
                .uri("/api/v1/workflow/audit-status/$testRequestId/$testSubRequestId")
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
        }
    }

    @Test
    @Throws(BluePrintException::class)
    fun testErrorRequestAndSubRequest() {

        val wfAudit1 = createWorkflowAuditStatusRecordSetTwo(1002)
        val wfAudit2 = createWorkflowAuditStatusRecordSetTwo(1003)
        runBlocking {

            storeToDB(wfAudit1)
            storeToDB(wfAudit2)
            val testRequestId: String = ""
            val testSubRequestId: String = ""
            testWebClient
                .get()
                .uri("/api/v1/workflow/audit-status/$testRequestId/$testSubRequestId")
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
                .consumeWith {
                    Assert.assertTrue(
                        "Cause: request Id and requesy sub Id is empty. \n " +
                            "Action : Please verify your request.",
                        it.status.is4xxClientError
                    )
                }
        }
    }

    private fun assertEqual(blueprintWorkflowAuditStatus: BlueprintWorkflowAuditStatus) {
        Assert.assertEquals(blueprintWorkflowAuditStatus.status, "In progress")
        Assert.assertEquals(blueprintWorkflowAuditStatus.requestId, "ab543-3asd4")
        Assert.assertEquals(blueprintWorkflowAuditStatus.subRequestId, "81c9-4910")
        Assert.assertEquals(blueprintWorkflowAuditStatus.blueprintName, "multi-steps")
        Assert.assertEquals(blueprintWorkflowAuditStatus.blueprintVersion, "1.0.0")
        Assert.assertEquals(blueprintWorkflowAuditStatus.requestMode, "sync")
        Assert.assertEquals(blueprintWorkflowAuditStatus.workflowName, "multi-steps-workflow")
        Assert.assertEquals(blueprintWorkflowAuditStatus.originatorId, "SDNC_DG")
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

    private fun createWorkflowAuditStatusRecordSetTwo(
        id: Long
    ): BlueprintWorkflowAuditStatus {

        var blueprintWorkflowAuditStatus: BlueprintWorkflowAuditStatus =
            BlueprintWorkflowAuditStatus()
        blueprintWorkflowAuditStatus.id = id
        blueprintWorkflowAuditStatus.originatorId = "SDNC_DG"
        blueprintWorkflowAuditStatus.requestMode = "sync"
        blueprintWorkflowAuditStatus.requestId = "ab543-3asd5"
        blueprintWorkflowAuditStatus.subRequestId = "81c9-4911"
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

    private suspend fun store(
        id: Long
    ) {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateString = "2021-08-08T18:25:43.511Z"
        val dateForTest = formatter.parse(dateString)
        databaseStoreAuditService.write(
            id, "SDNC_DG", "ab543-3asd4",
            "81c9-4910",
            "multi-steps-workflow", "multi-steps", "1.0.0",
            "{\n" +
                "    \"multi-steps-workflow-request\": {\n" +
                "      \"multi-steps-workflow-properties\": {\n" +
                "        \"prop1\": \"testing\",\n" +
                "        \"prop2\": \"testing description\",\n" +
                "        \"prop3\": \"user name \",\n" +
                "        \"prop4\" : \"test project\"\n" +
                "      }\n" +
                "    }\n" +
                "  }",
            "In progress", dateForTest, dateForTest, dateForTest, "CBA", "sync", " "
        )
    }

    private suspend fun storeToDB(
        blueprintWorkflowAuditStatus: BlueprintWorkflowAuditStatus
    ) {
        databaseStoreAuditService.write(
            blueprintWorkflowAuditStatus.id,
            blueprintWorkflowAuditStatus.originatorId,
            blueprintWorkflowAuditStatus.requestId,
            blueprintWorkflowAuditStatus.subRequestId,
            blueprintWorkflowAuditStatus.workflowName,
            blueprintWorkflowAuditStatus.blueprintName,
            blueprintWorkflowAuditStatus.blueprintVersion,
            blueprintWorkflowAuditStatus.workflowTaskContent,
            blueprintWorkflowAuditStatus.status,
            blueprintWorkflowAuditStatus.startDate,
            blueprintWorkflowAuditStatus.endDate,
            blueprintWorkflowAuditStatus.updatedDate,
            blueprintWorkflowAuditStatus.updatedBy,
            blueprintWorkflowAuditStatus.requestMode,
            blueprintWorkflowAuditStatus.workflowResponseContent
        )
    }
}

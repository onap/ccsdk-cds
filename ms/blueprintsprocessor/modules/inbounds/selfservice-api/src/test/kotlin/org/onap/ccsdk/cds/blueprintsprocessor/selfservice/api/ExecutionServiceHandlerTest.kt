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

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.coVerify
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractServiceFunction
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
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
            val executionServiceHandler = ExecutionServiceHandler(mockk(), mockk(), mockk(), mockk(), mockk(relaxed = true))
            val isServiceFunction = executionServiceHandler.checkServiceFunction(executionServiceInput)
            assertTrue(isServiceFunction, "failed to checkServiceFunction")
            val executionServiceOutput = executionServiceHandler.executeServiceFunction(executionServiceInput)
            assertNotNull(executionServiceOutput, "failed to get executionServiceOutput")
        }
    }

    @Test
    fun testPublishAuditFunction() {
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

        val publishAuditService = mockk<KafkaPublishAuditService>(relaxed = true)
        val executionServiceHandler = ExecutionServiceHandler(
            mockk(),
            mockk(),
            mockk(),
            publishAuditService,
            mockk(relaxed = true)
        )

        coEvery { publishAuditService.publishExecutionInput(ExecutionServiceInput()) } just Runs

        var executionServiceOutput: ExecutionServiceOutput? = null
        runBlocking {
            executionServiceOutput = executionServiceHandler.doProcess(executionServiceInput)
        }

        coVerify {
            publishAuditService.publishExecutionInput(executionServiceInput)
            publishAuditService.publishExecutionOutput(executionServiceInput.correlationUUID, executionServiceOutput!!)
        }
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

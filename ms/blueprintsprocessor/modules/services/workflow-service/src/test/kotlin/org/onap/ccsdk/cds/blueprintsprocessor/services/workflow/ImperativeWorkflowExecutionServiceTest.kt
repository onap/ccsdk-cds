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

package org.onap.ccsdk.cds.blueprintsprocessor.services.workflow

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.nodeTypeComponentScriptExecutor
import org.onap.ccsdk.cds.blueprintsprocessor.services.workflow.mock.MockComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.workflow.mock.mockNodeTemplateComponentScriptExecutor
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ImperativeWorkflowExecutionServiceTest {

    val log = logger(ImperativeWorkflowExecutionServiceTest::class)

    @Before
    fun init() {
        mockkObject(BluePrintDependencyService)
        every { BluePrintDependencyService.applicationContext.getBean(any()) } returns MockComponentFunction()
    }

    @After
    fun afterTests() {
        unmockkAll()
    }

    fun mockServiceTemplate(): ServiceTemplate {
        return serviceTemplate(
            "imperative-test", "1.0.0",
            "brindasanth@onap.com", "tosca"
        ) {

            topologyTemplate {
                nodeTemplate(
                    mockNodeTemplateComponentScriptExecutor(
                        "resolve-config",
                        "cba.wt.imperative.test.ResolveConfig"
                    )
                )
                nodeTemplate(
                    mockNodeTemplateComponentScriptExecutor(
                        "activate-config",
                        "cba.wt.imperative.test.ActivateConfig"
                    )
                )
                nodeTemplate(
                    mockNodeTemplateComponentScriptExecutor(
                        "activate-config-rollback",
                        "cba.wt.imperative.test.ActivateConfigRollback"
                    )
                )
                nodeTemplate(
                    mockNodeTemplateComponentScriptExecutor(
                        "activate-licence",
                        "cba.wt.imperative.test.ActivateLicence"
                    )
                )

                workflow("imperative-test-wf", "Test Imperative flow") {
                    step("resolve-config", "resolve-config", "") {
                        success("activate-config")
                    }
                    step("activate-config", "activate-config", "") {
                        success("activate-licence")
                        failure("activate-config-rollback")
                    }
                    step("activate-config-rollback", "activate-config-rollback", "")
                    step("activate-licence", "activate-licence", "")
                }
            }
            nodeType(BluePrintTypes.nodeTypeComponentScriptExecutor())
        }
    }

    @Test
    fun testImperativeExecutionService() {
        runBlocking {
            val serviceTemplate = mockServiceTemplate()
            val bluePrintContext = BluePrintContext(serviceTemplate)
            bluePrintContext.rootPath = normalizedPathName(".")
            bluePrintContext.entryDefinition = "cba.imperative.test.ImperativeTestDefinitions.kt"
            val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime("12345", bluePrintContext)

            val executionServiceInput = JacksonUtils
                .readValueFromClassPathFile(
                    "execution-input/imperative-test-input.json",
                    ExecutionServiceInput::class.java
                )!!

            val imperativeWorkflowExecutionService = ImperativeWorkflowExecutionService(NodeTemplateExecutionService(mockk()))
            val output = imperativeWorkflowExecutionService
                .executeBluePrintWorkflow(bluePrintRuntimeService, executionServiceInput, hashMapOf())
            assertNotNull(output, "failed to get imperative workflow output")
            assertNotNull(output.status, "failed to get imperative workflow output status")
            assertEquals(output.status.message, BluePrintConstants.STATUS_SUCCESS)
            assertEquals(output.status.eventType, EventType.EVENT_COMPONENT_EXECUTED.name)
        }
    }
}

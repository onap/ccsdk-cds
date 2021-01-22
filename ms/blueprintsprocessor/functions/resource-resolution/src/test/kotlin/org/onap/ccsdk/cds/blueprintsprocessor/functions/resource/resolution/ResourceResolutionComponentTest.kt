/*
 * Copyright © 2018 Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.NullNode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintError
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import kotlin.test.assertEquals
import kotlin.test.fail

class ResourceResolutionComponentTest {

    private val resourceResolutionService = mockk<ResourceResolutionService>()
    private val resourceResolutionComponent = ResourceResolutionComponent(resourceResolutionService)

    private val resolutionKey = "resolutionKey"
    private val resourceId = "1"
    private val resourceType = "ServiceInstance"
    private val occurrence = 1
    private val props = mutableMapOf<String, JsonNode>()
    private val bluePrintRuntimeService = mockk<BlueprintRuntimeService<*>>()
    private val artifactNames = listOf("template")
    private val nodeTemplateName = "nodeTemplateName"

    private val executionRequest = ExecutionServiceInput()

    @Before
    fun setup() {

        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT] = true.asJsonPrimitive()
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = resolutionKey.asJsonPrimitive()
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] = resourceId.asJsonPrimitive()
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = resourceType.asJsonPrimitive()
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] = occurrence.asJsonPrimitive()
        props[ResourceResolutionConstants.INPUT_ARTIFACT_PREFIX_NAMES] = JacksonUtils.jsonNodeFromObject(artifactNames)

        resourceResolutionComponent.operationInputs = props
        resourceResolutionComponent.bluePrintRuntimeService = bluePrintRuntimeService
        resourceResolutionComponent.nodeTemplateName = nodeTemplateName

        resourceResolutionComponent.executionServiceInput = executionRequest
        resourceResolutionComponent.processId = "12"
        resourceResolutionComponent.workflowName = "workflow"
        resourceResolutionComponent.stepName = "step"
        resourceResolutionComponent.interfaceName = "interfaceName"
        resourceResolutionComponent.operationName = "operationName"

        every { bluePrintRuntimeService.setNodeTemplateAttributeValue(any(), any(), any()) } returns Unit
    }

    @Test
    fun processNBWithResolutionKeyAndResourceIdAndResourceTypeTestException() {
        runBlocking {
            try {
                resourceResolutionComponent.processNB(executionRequest)
            } catch (e: BlueprintProcessorException) {
                assertEquals(
                    "Can't proceed with the resolution: either provide resolution-key OR combination of resource-id and resource-type.",
                    e.message
                )
                return@runBlocking
            }
            fail()
        }
    }

    @Test
    fun processNBWithResourceIdTestException() {
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = NullNode.getInstance()
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = NullNode.getInstance()

        runBlocking {
            try {
                resourceResolutionComponent.processNB(executionRequest)
            } catch (e: BlueprintProcessorException) {
                assertEquals(
                    "Can't proceed with the resolution: both resource-id and resource-type should be provided, one of them is missing.",
                    e.message
                )
                return@runBlocking
            }
            fail()
        }
    }

    @Test
    fun processNBWithEmptyResourceTypeResourceIdResolutionKeyTestException() {
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = MissingNode.getInstance()
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = NullNode.getInstance()
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] = NullNode.getInstance()

        runBlocking {
            try {
                resourceResolutionComponent.processNB(executionRequest)
            } catch (e: BlueprintProcessorException) {
                assertEquals(
                    "Can't proceed with the resolution: can't persist resolution without a correlation key. " +
                        "Either provide a resolution-key OR combination of resource-id and resource-type OR set `storeResult` to false.",
                    e.message
                )
                return@runBlocking
            }
            fail()
        }
    }

    @Test
    fun processNBTest() {
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = NullNode.getInstance()

        val properties = mutableMapOf<String, Any>()
        properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT] = true
        properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] = resourceId
        properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = resourceType
        properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] = occurrence

        coEvery {
            resourceResolutionService.resolveResources(
                any(),
                any(),
                any<List<String>>(),
                any<MutableMap<String, Any>>()
            )
        } returns ResourceResolutionResult(mutableMapOf(), mutableMapOf())

        runBlocking {
            resourceResolutionComponent.processNB(executionRequest)
        }

        // FIXME add verification
        //        coVerify {
        //            resourceResolutionService.resolveResources(eq(bluePrintRuntimeService),
        //                eq(nodeTemplateName), eq(artifactNames), eq(properties))
        //        }
    }

    @Test
    fun testRecover() {
        runBlocking {
            val blueprintError = BlueprintError()
            val exception = RuntimeException("message")
            every { bluePrintRuntimeService.getBlueprintError() } returns blueprintError
            resourceResolutionComponent.recoverNB(exception, executionRequest)

            assertEquals(1, blueprintError.errors.size)
        }
    }
}

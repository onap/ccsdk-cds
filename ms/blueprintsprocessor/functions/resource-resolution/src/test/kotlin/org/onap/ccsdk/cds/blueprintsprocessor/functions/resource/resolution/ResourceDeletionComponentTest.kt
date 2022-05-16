/*
 * Copyright © 2022 Bell Canada
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
import com.fasterxml.jackson.databind.node.NullNode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceDeletionComponent.Companion.ATTRIBUTE_RESULT
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceDeletionComponent.Companion.ATTRIBUTE_SUCCESS
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceDeletionComponent.Companion.INPUT_FAIL_ON_EMPTY
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceDeletionComponent.Companion.INPUT_LAST_N_OCCURRENCES
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionDBService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.TemplateResolutionService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils

class ResourceDeletionComponentTest {

    private val blueprintName = "testCBA"
    private val blueprintVersion = "1.0.0"
    private val artifactNames = listOf("artifact-a", "artifact-b")
    private val resolutionKey = "resolutionKey"
    private val resourceId = "1"
    private val resourceType = "ServiceInstance"
    private val nodetemplateName = "resource-deletion"
    private val executionRequest = ExecutionServiceInput()

    private lateinit var resourceResolutionDBService: ResourceResolutionDBService
    private lateinit var templateResolutionService: TemplateResolutionService
    private lateinit var resourceDeletionComponent: ResourceDeletionComponent
    private lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>

    private val props = mutableMapOf<String, JsonNode>()

    private var success = slot<JsonNode>()
    private var result = slot<JsonNode>()

    @Before
    fun setup() {
        bluePrintRuntimeService = spyk()
        every { bluePrintRuntimeService.bluePrintContext() }.returns(
            BluePrintContext(
                ServiceTemplate().apply {
                    this.metadata = mutableMapOf(
                        BluePrintConstants.METADATA_TEMPLATE_VERSION to blueprintVersion,
                        BluePrintConstants.METADATA_TEMPLATE_NAME to blueprintName
                    )
                }
            )
        )
        every { bluePrintRuntimeService.setNodeTemplateAttributeValue(nodetemplateName, ATTRIBUTE_SUCCESS, capture(success)) }
            .answers { }
        every { bluePrintRuntimeService.setNodeTemplateAttributeValue(nodetemplateName, ATTRIBUTE_RESULT, capture(result)) }
            .answers { }

        resourceResolutionDBService = mockk()
        templateResolutionService = mockk()
        resourceDeletionComponent = ResourceDeletionComponent(resourceResolutionDBService, templateResolutionService)
        resourceDeletionComponent.bluePrintRuntimeService = bluePrintRuntimeService
        resourceDeletionComponent.nodeTemplateName = nodetemplateName
        resourceDeletionComponent.executionServiceInput = executionRequest
        resourceDeletionComponent.processId = "12"
        resourceDeletionComponent.workflowName = "workflow"
        resourceDeletionComponent.stepName = "step"
        resourceDeletionComponent.interfaceName = "interfaceName"
        resourceDeletionComponent.operationName = "operationName"

        props[RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = NullNode.getInstance()
        props[RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] = NullNode.getInstance()
        props[RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = NullNode.getInstance()
        props[ResourceResolutionConstants.INPUT_ARTIFACT_PREFIX_NAMES] = artifactNames.asJsonType()
        props[INPUT_FAIL_ON_EMPTY] = NullNode.getInstance()
        props[INPUT_LAST_N_OCCURRENCES] = NullNode.getInstance()
        resourceDeletionComponent.operationInputs = props
    }

    @Test
    fun `using resolution-key`() {
        props[RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = resolutionKey.asJsonPrimitive()

        coEvery {
            templateResolutionService.deleteTemplates(blueprintName, blueprintVersion, any(), resolutionKey, null)
        }.returns(1)

        coEvery {
            resourceResolutionDBService.deleteResources(blueprintName, blueprintVersion, any(), resolutionKey, null)
        }.returns(2)

        runBlocking { resourceDeletionComponent.processNB(executionRequest) }

        val expected = ResourceDeletionComponent.DeletionResult(1, 2).asJsonType()
        val result: JsonNode = result.captured
        assertEquals(expected, result[artifactNames[0]])
        assertEquals(expected, result[artifactNames[1]])
        assertEquals(true.asJsonPrimitive(), success.captured)
    }

    @Test
    fun `using resource-type and resource-id`() {
        props[RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] = resourceId.asJsonPrimitive()
        props[RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = resourceType.asJsonPrimitive()

        coEvery {
            templateResolutionService.deleteTemplates(blueprintName, blueprintVersion, any(), resourceType, resourceId, null)
        }.returns(2)

        coEvery {
            resourceResolutionDBService.deleteResources(blueprintName, blueprintVersion, any(), resourceType, resourceId, null)
        }.returns(4)

        runBlocking { resourceDeletionComponent.processNB(executionRequest) }

        val expected = ResourceDeletionComponent.DeletionResult(2, 4).asJsonType()
        val result: JsonNode = result.captured
        assertEquals(expected, result[artifactNames[0]])
        assertEquals(expected, result[artifactNames[1]])
        assertEquals(true.asJsonPrimitive(), success.captured)
    }

    @Test(expected = BluePrintProcessorException::class)
    fun `using resource-type missing resource-id`() {
        props[RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = resourceType.asJsonPrimitive()
        runBlocking { resourceDeletionComponent.processNB(executionRequest) }
    }

    @Test(expected = BluePrintProcessorException::class)
    fun `using resource-id missing resource-type`() {
        props[RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] = resourceId.asJsonPrimitive()
        runBlocking { resourceDeletionComponent.processNB(executionRequest) }
    }

    @Test
    fun `attributes present when failing`() {
        val threwException = runBlocking {
            try {
                resourceDeletionComponent.processNB(executionRequest)
                false
            } catch (e: Exception) {
                true
            }
        }
        assertTrue(threwException)
        assertEquals(false.asJsonPrimitive(), success.captured)
        assertEquals(emptyMap<String, Any>().asJsonNode(), result.captured)
    }

    @Test
    fun `last-n-occurrences`() {
        props[RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = resolutionKey.asJsonPrimitive()
        props[INPUT_LAST_N_OCCURRENCES] = JacksonUtils.jsonNodeFromObject(3)

        coEvery {
            templateResolutionService.deleteTemplates(blueprintName, blueprintVersion, any(), resolutionKey, 3)
        }.returns(3)

        coEvery {
            resourceResolutionDBService.deleteResources(blueprintName, blueprintVersion, any(), resolutionKey, 3)
        }.returns(6)

        runBlocking { resourceDeletionComponent.processNB(executionRequest) }

        val expected = ResourceDeletionComponent.DeletionResult(3, 6).asJsonType()
        val result: JsonNode = result.captured
        assertEquals(expected, result[artifactNames[0]])
        assertEquals(expected, result[artifactNames[1]])
        assertEquals(true.asJsonPrimitive(), success.captured)
    }

    @Test
    fun `fail-on-empty nothing deleted`() {
        props[RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = resolutionKey.asJsonPrimitive()
        props[INPUT_FAIL_ON_EMPTY] = true.asJsonPrimitive()

        coEvery {
            templateResolutionService.deleteTemplates(blueprintName, blueprintVersion, any(), resolutionKey, null)
        }.returns(0)

        coEvery {
            resourceResolutionDBService.deleteResources(blueprintName, blueprintVersion, any(), resolutionKey, null)
        }.returns(0)

        val threwException = runBlocking {
            try {
                resourceDeletionComponent.processNB(executionRequest)
                false
            } catch (e: BluePrintProcessorException) {
                true
            }
        }

        val expected = ResourceDeletionComponent.DeletionResult(0, 0).asJsonType()
        val result: JsonNode = result.captured
        assertTrue(threwException)
        assertEquals(expected, result[artifactNames[0]])
        assertEquals(expected, result[artifactNames[1]])
        assertEquals(false.asJsonPrimitive(), success.captured)
    }

    @Test
    fun `fail-on-empty something deleted`() {
        props[RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = resolutionKey.asJsonPrimitive()
        props[INPUT_FAIL_ON_EMPTY] = true.asJsonPrimitive()

        coEvery {
            templateResolutionService.deleteTemplates(blueprintName, blueprintVersion, any(), resolutionKey, null)
        }.returns(1)

        coEvery {
            resourceResolutionDBService.deleteResources(blueprintName, blueprintVersion, any(), resolutionKey, null)
        }.returns(1)

        runBlocking { resourceDeletionComponent.processNB(executionRequest) }

        val expected = ResourceDeletionComponent.DeletionResult(1, 1).asJsonType()
        val result: JsonNode = result.captured
        assertEquals(expected, result[artifactNames[0]])
        assertEquals(expected, result[artifactNames[1]])
        assertEquals(true.asJsonPrimitive(), success.captured)
    }

    @Test
    fun `db throws exception`() {
        props[RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = resolutionKey.asJsonPrimitive()

        coEvery {
            templateResolutionService.deleteTemplates(blueprintName, blueprintVersion, any(), resolutionKey, null)
        }.throws(RuntimeException("DB failure!"))

        val threwException = runBlocking {
            try {
                resourceDeletionComponent.processNB(executionRequest)
                false
            } catch (e: Exception) {
                true
            }
        }

        assertTrue(threwException)
        assertEquals(false.asJsonPrimitive(), success.captured)
        assertEquals(emptyMap<String, Any>().asJsonNode(), result.captured)
    }
}

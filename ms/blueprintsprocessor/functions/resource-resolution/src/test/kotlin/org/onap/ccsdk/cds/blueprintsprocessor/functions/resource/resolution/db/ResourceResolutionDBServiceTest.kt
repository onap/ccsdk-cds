/*
 * Copyright (C) 2019 Bell Canada.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.dao.EmptyResultDataAccessException
import kotlin.test.assertEquals

open class ResourceResolutionDBServiceTest {

    private val resourceResolutionRepository = mockk<ResourceResolutionRepository>()

    private val resourceResolutionDBService = ResourceResolutionDBService(resourceResolutionRepository)

    private val resolutionKey = "resolutionKey"
    private val resourceId = "1"
    private val resourceType = "ServiceInstance"
    private val occurrence = 0
    private val artifactPrefix = "template"
    private val blueprintName = "blueprintName"
    private val blueprintVersion = "1.0.0"
    private val metadata = hashMapOf<String, String>()
    private val props = hashMapOf<String, Any>()
    private val bluePrintContext = mockk<BlueprintContext>()
    private val bluePrintRuntimeService = mockk<DefaultBlueprintRuntimeService>()

    @Before
    fun setup() {
        metadata[BlueprintConstants.METADATA_TEMPLATE_VERSION] = blueprintVersion
        metadata[BlueprintConstants.METADATA_TEMPLATE_NAME] = blueprintName

        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = resolutionKey
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] = resourceId
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = resourceType
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] = occurrence

        every { bluePrintContext.metadata } returns metadata

        every { bluePrintRuntimeService.bluePrintContext() } returns bluePrintContext
    }

    @Test
    fun findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyAndOccurrenceTest() {

        val rr1 = ResourceResolution()
        val rr2 = ResourceResolution()

        val list = listOf(rr1, rr2)
        every {
            resourceResolutionRepository.findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyAndOccurrence(
                any(), any(), any(), any(), any()
            )
        } returns list
        runBlocking {

            val res =
                resourceResolutionDBService.findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyAndOccurrence(
                    bluePrintRuntimeService, resolutionKey, occurrence, artifactPrefix
                )

            assertEquals(2, res.size)
        }
    }

    @Test
    fun findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyAndOccurrenceTestException() {
        every {
            resourceResolutionRepository.findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyAndOccurrence(
                any(), any(), any(), any(), any()
            )
        } throws EmptyResultDataAccessException(1)
        runBlocking {
            val res =
                resourceResolutionDBService.findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyAndOccurrence(
                    bluePrintRuntimeService, resolutionKey, occurrence, artifactPrefix
                )

            assert(res.isEmpty())
        }
    }

    @Test
    fun findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResourceIdAndResourceTypeAndOccurrenceTest() {

        val rr1 = ResourceResolution()
        val rr2 = ResourceResolution()
        val list = listOf(rr1, rr2)
        every {
            resourceResolutionRepository.findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResourceIdAndResourceTypeAndOccurrence(
                any(), any(), any(), any(), any(), any()
            )
        } returns list
        runBlocking {

            val res =
                resourceResolutionDBService.findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResourceIdAndResourceTypeAndOccurrence(
                    bluePrintRuntimeService, resourceId, resourceType, occurrence, artifactPrefix
                )

            assertEquals(2, res.size)
        }
    }

    @Test
    fun findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResourceIdAndResourceTypeAndOccurrenceTestException() {
        every {
            resourceResolutionRepository.findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResourceIdAndResourceTypeAndOccurrence(
                any(), any(), any(), any(), any(), any()
            )
        } throws EmptyResultDataAccessException(1)
        runBlocking {
            val res =
                resourceResolutionDBService.findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResourceIdAndResourceTypeAndOccurrence(
                    bluePrintRuntimeService, resourceId, resourceType, occurrence, artifactPrefix
                )

            assert(res.isEmpty())
        }
    }

    @Test
    fun readValueTest() {
        val rr = ResourceResolution()
        rr.name = "bob"
        rr.value = "testValue"
        every {
            resourceResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndName(
                any(), any(), any(), any(), any()
            )
        } returns rr
        runBlocking {
            val res =
                resourceResolutionDBService.readValue(
                    blueprintName, blueprintVersion, artifactPrefix, resolutionKey, "bob"
                )

            assertEquals(rr.name, res.name)
            assertEquals(rr.value, res.value)
        }
    }

    @Test
    fun readWithResolutionKeyTest() {
        val rr1 = ResourceResolution()
        val rr2 = ResourceResolution()
        val list = listOf(rr1, rr2)
        every {
            resourceResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
                any(), any(), any(), any()
            )
        } returns list
        runBlocking {
            val res =
                resourceResolutionDBService.readWithResolutionKey(
                    blueprintName, blueprintVersion, artifactPrefix, resolutionKey
                )
            assertEquals(2, res.size)
        }
    }

    @Test
    fun readWithResourceIdAndResourceTypeTest() {
        val rr1 = ResourceResolution()
        val rr2 = ResourceResolution()
        val list = listOf(rr1, rr2)
        every {
            resourceResolutionRepository.findByBlueprintNameAndBlueprintVersionAndResourceIdAndResourceType(
                any(), any(), any(), any()
            )
        } returns list
        runBlocking {
            val res =
                resourceResolutionDBService.readWithResourceIdAndResourceType(
                    blueprintName, blueprintVersion, resourceId, resourceType
                )
            assertEquals(2, res.size)
        }
    }

    @Test
    fun writeTest() {
        val resourceResolution = ResourceResolution()
        val resourceAssignment = ResourceAssignment()
        resourceAssignment.property?.status = BlueprintConstants.STATUS_SUCCESS
        resourceAssignment.property?.value = "result".asJsonPrimitive()
        resourceAssignment.dictionarySource = "ddSource"
        resourceAssignment.dictionaryName = "ddName"
        resourceAssignment.version = 1
        resourceAssignment.name = "test"
        every {
            resourceResolutionRepository.saveAndFlush(any<ResourceResolution>())
        } returns resourceResolution
        runBlocking {
            val res =
                resourceResolutionDBService.write(
                    props, bluePrintRuntimeService, artifactPrefix, resourceAssignment
                )

            assertEquals(resourceResolution, res)
        }
    }

    @Test
    fun writeWithNullValue() {
        val slot = slot<ResourceResolution>()
        val resourceAssignment = ResourceAssignment()
        resourceAssignment.status = BlueprintConstants.STATUS_SUCCESS
        resourceAssignment.dictionarySource = "ddSource"
        resourceAssignment.dictionaryName = "ddName"
        resourceAssignment.version = 1
        resourceAssignment.name = "test"
        every {
            resourceResolutionRepository.saveAndFlush(capture(slot))
        } returns ResourceResolution()
        runBlocking {
            resourceResolutionDBService.write(
                props, bluePrintRuntimeService, artifactPrefix, resourceAssignment
            )

            val res = slot.captured

            assertEquals("", res.value)
        }
    }

    @Test
    fun deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyTest() {
        every {
            resourceResolutionRepository.deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKey(any(), any(), any(), any())
        } returns Unit
        runBlocking {
            val res = resourceResolutionDBService.deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKey(
                blueprintName, blueprintVersion, artifactPrefix, resolutionKey
            )
            assertEquals(Unit, res)
        }
    }
}

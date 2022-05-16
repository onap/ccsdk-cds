package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBluePrintRuntimeService
import org.springframework.dao.EmptyResultDataAccessException
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TemplateResolutionServiceTest {

    private val templateResolutionRepository = mockk<TemplateResolutionRepository>()

    private val templateResolutionService = TemplateResolutionService(templateResolutionRepository)

    private val resolutionKey = "resolutionKey"
    private val resourceId = "1"
    private val resourceType = "ServiceInstance"
    private val occurrence = 0
    private val artifactPrefix = "template"
    private val blueprintName = "blueprintName"
    private val blueprintVersion = "1.0.0"
    private val result = "result"
    private val metadata = hashMapOf<String, String>()
    private val props = hashMapOf<String, Any>()
    private val bluePrintContext = mockk<BluePrintContext>()
    private val bluePrintRuntimeService = mockk<DefaultBluePrintRuntimeService>()

    @Before
    fun setup() {
        metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION] = blueprintVersion
        metadata[BluePrintConstants.METADATA_TEMPLATE_NAME] = blueprintName

        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = resolutionKey
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] = resourceId
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = resourceType
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] = occurrence

        every { bluePrintContext.metadata } returns metadata

        every { bluePrintRuntimeService.bluePrintContext() } returns bluePrintContext
    }

    @Test
    fun findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameTest() {
        val tr = TemplateResolution()
        tr.result = "res"
        runBlocking {
            every {
                templateResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                    any(), any(), any(), any(), any()
                )
            } returns tr
            val res =
                templateResolutionService.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
                    bluePrintRuntimeService, artifactPrefix, resolutionKey
                )
            assertEquals(tr.result, res)
        }
    }

    @Test(expected = EmptyResultDataAccessException::class)
    fun findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameTestException() {
        val tr = TemplateResolution()
        runBlocking {
            every {
                templateResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                    any(), any(), any(), any(), any()
                )
            } returns tr
            templateResolutionService.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
                bluePrintRuntimeService, artifactPrefix, resolutionKey
            )
        }
    }

    @Test
    fun writeWithResolutionKeyTest() {
        val tr = TemplateResolution()
        runBlocking {
            every { templateResolutionRepository.saveAndFlush(any<TemplateResolution>()) } returns tr
            every {
                templateResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                    any(), any(), any(), any(), any()
                )
            } returns null
            val res = templateResolutionService.write(props, result, bluePrintRuntimeService, artifactPrefix)
            assertEquals(tr, res)
        }
    }

    @Test
    fun findFirstNOccurrencesTest() {
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] = occurrence
        val tr1 = TemplateResolution()
        val tr2 = TemplateResolution()
        val list = listOf(tr1, tr2)
        every {
            templateResolutionRepository.findFirstNOccurrences(
                any(), any(), any(), any(), 1
            )
        } returns list
        runBlocking {
            val res =
                templateResolutionService.findFirstNOccurrences(
                    blueprintName, blueprintVersion, artifactPrefix, resolutionKey, 1
                )
            assertEquals(false, res.isEmpty(), "find first N occurrences test failed")
            assertEquals(1, res.size)
            assertNotEquals(null, res[1])
            res[1]?.let { assertEquals(2, it.size) }
        }
    }

    @Test
    fun findLastNOccurrencesTest() {
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] = occurrence
        val tr1 = TemplateResolution()
        val tr2 = TemplateResolution()
        val list = listOf(tr1, tr2)
        every {
            templateResolutionRepository.findLastNOccurrences(
                any(), any(), any(), any(), 1
            )
        } returns list
        runBlocking {
            val res =
                templateResolutionService.findLastNOccurrences(
                    blueprintName, blueprintVersion, artifactPrefix, resolutionKey, 1
                )
            assertEquals(false, res.isEmpty(), "find last N occurrences test failed")
            assertEquals(1, res.size)
            assertNotEquals(null, res[1])
            res[1]?.let { assertEquals(2, it.size) }
        }
    }

    @Test
    fun findOccurrencesWithinRangeTest() {
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] = occurrence
        val tr1 = TemplateResolution()
        val tr2 = TemplateResolution()
        val list = listOf(tr1, tr2)
        every {
            templateResolutionRepository.findOccurrencesWithinRange(
                any(), any(), any(), any(), 0, 1
            )
        } returns list
        runBlocking {
            val res =
                templateResolutionService.findOccurrencesWithinRange(
                    blueprintName, blueprintVersion, artifactPrefix, resolutionKey, 0, 1
                )
            assertEquals(false, res.isEmpty(), "find occurrences within a range test failed")
            assertEquals(1, res.size)
            assertNotEquals(null, res[1])
            res[1]?.let { assertEquals(2, it.size) }
        }
    }

    @Test
    fun writeWithResolutionKeyExistingTest() {
        val tr = TemplateResolution()
        runBlocking {
            every { templateResolutionRepository.saveAndFlush(any<TemplateResolution>()) } returns tr
            every {
                templateResolutionRepository.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                    any(), any(), any(), any(), any()
                )
            } returns tr
            every {
                templateResolutionRepository.deleteByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                    any(), any(), any(), any(), any()
                )
            } returns Unit
            val res = templateResolutionService.write(props, result, bluePrintRuntimeService, artifactPrefix)
            verify {
                templateResolutionRepository.deleteByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                    eq(resolutionKey), eq(blueprintName), eq(blueprintVersion), eq(artifactPrefix), eq(occurrence)
                )
            }
            assertEquals(tr, res)
        }
    }

    @Test
    fun writeWithResourceIdResourceTypeExistingTest() {
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = ""
        val tr = TemplateResolution()
        runBlocking {
            every { templateResolutionRepository.saveAndFlush(any<TemplateResolution>()) } returns tr
            every {
                templateResolutionRepository.findByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                    any(), any(), any(), any(), any(), any()
                )
            } returns tr
            every {
                templateResolutionRepository.deleteByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                    any(), any(), any(), any(), any(), any()
                )
            } returns Unit
            val res = templateResolutionService.write(props, result, bluePrintRuntimeService, artifactPrefix)
            verify {
                templateResolutionRepository.deleteByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
                    eq(resourceId),
                    eq(resourceType),
                    eq(blueprintName),
                    eq(blueprintVersion),
                    eq(artifactPrefix),
                    eq(occurrence)
                )
            }
            assertEquals(tr, res)
        }
    }

    @Test
    fun deleteTemplatesResolutionKeyAll() {
        every {
            templateResolutionRepository.deleteByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(resolutionKey, blueprintName, blueprintVersion, artifactPrefix)
        }.returns(1)
        runBlocking {
            val result = templateResolutionService.deleteTemplates(blueprintName, blueprintVersion, artifactPrefix, resolutionKey, null)
            assertEquals(1, result)
        }
    }

    @Test
    fun deleteTemplatesResolutionKeyLastN() {
        every {
            templateResolutionRepository.deleteTemplates(blueprintName, blueprintVersion, artifactPrefix, resolutionKey, 1)
        }.returns(1)
        runBlocking {
            val result = templateResolutionService.deleteTemplates(blueprintName, blueprintVersion, artifactPrefix, resolutionKey, 1)
            assertEquals(1, result)
        }
    }

    @Test
    fun deleteTemplatesResourceIdAndTypeAll() {
        every {
            templateResolutionRepository.deleteByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactName(resourceId, resourceType, blueprintName, blueprintVersion, artifactPrefix)
        }.returns(1)
        runBlocking {
            val result = templateResolutionService.deleteTemplates(blueprintName, blueprintVersion, artifactPrefix, resourceType, resourceId, null)
            assertEquals(1, result)
        }
    }

    @Test
    fun deleteTemplatesResourceIdAndTypeLastN() {
        every {
            templateResolutionRepository.deleteTemplates(blueprintName, blueprintVersion, artifactPrefix, resourceType, resourceType, 1)
        }.returns(1)
        runBlocking {
            val result = templateResolutionService.deleteTemplates(blueprintName, blueprintVersion, artifactPrefix, resourceType, resourceType, 1)
            assertEquals(1, result)
        }
    }
}

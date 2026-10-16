/*
 * Copyright © 2026 Deutsche Telekom AG.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.TestDatabaseConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.util.UUID
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [TestDatabaseConfiguration::class])
@TestPropertySource(locations = ["classpath:application-test.properties"])
class ResolutionRepositoryDeleteTest {

    @Autowired
    lateinit var resourceResolutionRepository: ResourceResolutionRepository

    @Autowired
    lateinit var templateResolutionRepository: TemplateResolutionRepository

    private val blueprintName = "delete-test"
    private val blueprintVersion = "1.0.0"
    private val artifactName = "config"

    @Before
    fun setUp() {
        resourceResolutionRepository.deleteAll()
        templateResolutionRepository.deleteAll()
    }

    @Test
    fun `deletes all resource resolutions of a resolution key`() {
        (1..3).forEach { resourceResolutionRepository.save(resourceResolution(it, resolutionKey = "key-1")) }
        resourceResolutionRepository.save(resourceResolution(1, resolutionKey = "key-2"))

        val deleted = resourceResolutionRepository.deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKey(
            blueprintName, blueprintVersion, artifactName, "key-1"
        )

        assertEquals(3, deleted)
        assertEquals(listOf("key-2"), resourceResolutionRepository.findAll().map { it.resolutionKey })
    }

    @Test
    fun `deletes all resource resolutions of a resource id and type`() {
        (1..3).forEach { resourceResolutionRepository.save(resourceResolution(it, resourceId = "id-1")) }
        resourceResolutionRepository.save(resourceResolution(1, resourceId = "id-2"))

        val deleted = resourceResolutionRepository.deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResourceTypeAndResourceId(
            blueprintName, blueprintVersion, artifactName, "vnf", "id-1"
        )

        assertEquals(3, deleted)
        assertEquals(listOf("id-2"), resourceResolutionRepository.findAll().map { it.resourceId })
    }

    @Test
    fun `deletes a single resource resolution`() {
        resourceResolutionRepository.save(resourceResolution(1, resolutionKey = "key-1"))

        val deleted = resourceResolutionRepository.deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKey(
            blueprintName, blueprintVersion, artifactName, "key-1"
        )

        assertEquals(1, deleted)
        assertEquals(0, resourceResolutionRepository.count())
    }

    @Test
    fun `deletes all template resolutions of a resolution key`() {
        (1..3).forEach { templateResolutionRepository.save(templateResolution(it, resolutionKey = "key-1")) }
        templateResolutionRepository.save(templateResolution(1, resolutionKey = "key-2"))

        val deleted = templateResolutionRepository.deleteByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
            "key-1", blueprintName, blueprintVersion, artifactName
        )

        assertEquals(3, deleted)
        assertEquals(listOf("key-2"), templateResolutionRepository.findAll().map { it.resolutionKey })
    }

    @Test
    fun `deletes all template resolutions of a resource id and type`() {
        (1..3).forEach { templateResolutionRepository.save(templateResolution(it, resourceId = "id-1")) }
        templateResolutionRepository.save(templateResolution(1, resourceId = "id-2"))

        val deleted = templateResolutionRepository.deleteByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactName(
            "id-1", "vnf", blueprintName, blueprintVersion, artifactName
        )

        assertEquals(3, deleted)
        assertEquals(listOf("id-2"), templateResolutionRepository.findAll().map { it.resourceId })
    }

    @Test
    fun `deletes a single template resolution`() {
        templateResolutionRepository.save(templateResolution(1, resolutionKey = "key-1"))

        val deleted = templateResolutionRepository.deleteByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
            "key-1", blueprintName, blueprintVersion, artifactName
        )

        assertEquals(1, deleted)
        assertEquals(0, templateResolutionRepository.count())
    }

    private fun resourceResolution(occurrence: Int, resolutionKey: String = "", resourceId: String = "") =
        ResourceResolution().also {
            it.id = UUID.randomUUID().toString()
            it.blueprintName = blueprintName
            it.blueprintVersion = blueprintVersion
            it.artifactName = artifactName
            it.resolutionKey = resolutionKey
            it.resourceType = if (resourceId.isEmpty()) "" else "vnf"
            it.resourceId = resourceId
            it.occurrence = occurrence
            it.name = "hostname"
            it.value = "value-$occurrence"
            it.status = "success"
            it.dictionaryName = "hostname"
            it.dictionarySource = "input"
            it.dictionaryVersion = 0
        }

    private fun templateResolution(occurrence: Int, resolutionKey: String = "", resourceId: String = "") =
        TemplateResolution().also {
            it.id = UUID.randomUUID().toString()
            it.blueprintName = blueprintName
            it.blueprintVersion = blueprintVersion
            it.artifactName = artifactName
            it.resolutionKey = resolutionKey
            it.resourceType = if (resourceId.isEmpty()) "" else "vnf"
            it.resourceId = resourceId
            it.occurrence = occurrence
            it.result = "result-$occurrence"
        }
}

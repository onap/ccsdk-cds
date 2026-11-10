/*
 * Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler

import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.samePropertyValuesAs
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ResourceDictionary
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.repository.ResourceDictionaryRepository
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class ResourceDictionaryHandlerTest {

    private val mockRepository = Mockito.mock(ResourceDictionaryRepository::class.java)
    private val resourceDictionaryHandler = ResourceDictionaryHandler(mockRepository)

    private fun sampleResourceDefinition(name: String = "sample-db-source"): ResourceDefinition {
        val resourceDefinition = JacksonUtils
            .readValueFromFile(
                "./../../../../../components/model-catalog/resource-dictionary/starter-dictionary/sample-db-source.json",
                ResourceDefinition::class.java
            )!!
        resourceDefinition.name = name
        return resourceDefinition
    }

    private fun assertInvalidRequest(exception: BluePrintProcessorException, expectedMessage: String) {
        assertEquals(400, exception.code)
        assertEquals(ErrorCatalogCodes.INVALID_REQUEST_FORMAT, exception.name)
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun testSaveResourceDictionary() {
        val resourceDefinition = sampleResourceDefinition()

        val expectedResourceDictionary = ResourceDictionary()
        expectedResourceDictionary.name = resourceDefinition.name
        expectedResourceDictionary.updatedBy = resourceDefinition.updatedBy
        expectedResourceDictionary.resourceDictionaryGroup = resourceDefinition.group
        expectedResourceDictionary.tags = resourceDefinition.tags!!
        expectedResourceDictionary.description = resourceDefinition.property.description!!
        expectedResourceDictionary.dataType = resourceDefinition.property.type
        expectedResourceDictionary.definition = resourceDefinition

        // Mock save success
        val mockReturnValue = ResourceDictionary()
        mockReturnValue.definition = ResourceDefinition()
        Mockito.`when`(mockRepository.save(any(ResourceDictionary::class.java)))
            .thenReturn(mockReturnValue)

        runBlocking {
            resourceDictionaryHandler.saveResourceDefinition(resourceDefinition)
        }

        val argumentCaptor = ArgumentCaptor.forClass(ResourceDictionary::class.java)
        Mockito.verify(mockRepository).save(argumentCaptor.capture())

        Assert.assertThat(argumentCaptor.value, samePropertyValuesAs(expectedResourceDictionary))
    }

    @Test
    fun testSaveAllResourceDefinition() {
        val resourceDefinitions = listOf(sampleResourceDefinition("first-entry"), sampleResourceDefinition("second-entry"))
        Mockito.`when`(mockRepository.saveAll(anyList<ResourceDictionary>())).thenAnswer { it.getArgument(0) }

        val saved = runBlocking {
            resourceDictionaryHandler.saveAllResourceDefinition(resourceDefinitions)
        }

        assertEquals(listOf("first-entry", "second-entry"), saved.map { it.name })
        Mockito.verify(mockRepository).saveAll(anyList<ResourceDictionary>())
    }

    @Test
    fun testSaveAllResourceDefinitionRejectsEntryWithoutUpdatedBy() {
        val invalid = sampleResourceDefinition("no-updated-by")
        invalid.updatedBy = ""
        val resourceDefinitions = listOf(sampleResourceDefinition("valid-entry"), invalid)

        val exception = assertThrows(BluePrintProcessorException::class.java) {
            runBlocking { resourceDictionaryHandler.saveAllResourceDefinition(resourceDefinitions) }
        }

        assertInvalidRequest(exception, "ResourceDictionary(no-updated-by) updatedBy is missing.")
        Mockito.verifyNoInteractions(mockRepository)
    }

    @Test
    fun testSaveAllResourceDefinitionRejectsEntryWithoutDescription() {
        val invalid = sampleResourceDefinition("no-description")
        invalid.property.description = null
        val resourceDefinitions = listOf(sampleResourceDefinition("valid-entry"), invalid)

        val exception = assertThrows(BluePrintProcessorException::class.java) {
            runBlocking { resourceDictionaryHandler.saveAllResourceDefinition(resourceDefinitions) }
        }

        assertInvalidRequest(exception, "ResourceDictionary(no-description) description is missing.")
        Mockito.verifyNoInteractions(mockRepository)
    }

    @Test
    fun testSaveResourceDefinitionRejectsEntryWithoutDescription() {
        val invalid = sampleResourceDefinition("no-description")
        invalid.property.description = null

        val exception = assertThrows(BluePrintProcessorException::class.java) {
            runBlocking { resourceDictionaryHandler.saveResourceDefinition(invalid) }
        }

        assertInvalidRequest(exception, "ResourceDictionary(no-description) description is missing.")
        Mockito.verifyNoInteractions(mockRepository)
    }

    @Test
    fun testSaveResourceDefinitionRejectsEntryWithoutName() {
        val invalid = sampleResourceDefinition("")

        val exception = assertThrows(BluePrintProcessorException::class.java) {
            runBlocking { resourceDictionaryHandler.saveResourceDefinition(invalid) }
        }

        assertInvalidRequest(exception, "ResourceDictionary name is missing.")
        Mockito.verifyNoInteractions(mockRepository)
    }

    @Test
    fun testSaveResourceDictionaryRejectsEntryWithoutDescription() {
        val resourceDefinition = sampleResourceDefinition("no-description")
        resourceDefinition.property.description = null
        val resourceDictionary = ResourceDictionary()
        resourceDictionary.name = resourceDefinition.name
        resourceDictionary.updatedBy = resourceDefinition.updatedBy
        resourceDictionary.definition = resourceDefinition

        val exception = assertThrows(BluePrintProcessorException::class.java) {
            runBlocking { resourceDictionaryHandler.saveResourceDictionary(resourceDictionary) }
        }

        assertInvalidRequest(exception, "ResourceDictionary(no-description) description is missing.")
        Mockito.verifyNoInteractions(mockRepository)
    }
}

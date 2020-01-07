package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler

import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.samePropertyValuesAs
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ResourceDictionary
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.repository.ResourceDictionaryRepository
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class ResourceDictionaryHandlerTest {

    private val mockRepository = Mockito.mock(ResourceDictionaryRepository::class.java)
    private val resourceDictionaryHandler = ResourceDictionaryHandler(mockRepository)

    @Test
    fun testSaveResourceDictionary() {
        val resourceDefinition : ResourceDefinition = JacksonUtils
                .readValueFromFile(
                        "./../../../../../components/model-catalog/resource-dictionary/starter-dictionary/sample-db-source.json",
                        ResourceDefinition::class.java
                )!!

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

}
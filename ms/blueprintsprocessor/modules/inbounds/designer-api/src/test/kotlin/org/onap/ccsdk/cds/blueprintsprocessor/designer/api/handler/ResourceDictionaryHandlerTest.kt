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
        val resourceDefinition: ResourceDefinition = JacksonUtils
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

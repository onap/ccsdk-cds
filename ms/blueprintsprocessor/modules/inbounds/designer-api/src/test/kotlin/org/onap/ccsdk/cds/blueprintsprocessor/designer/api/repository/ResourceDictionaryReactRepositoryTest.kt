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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.repository

import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.db.BluePrintDBLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.DesignerApiTestConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ResourceDictionary
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Commit
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [DesignerApiTestConfiguration::class,
        BluePrintPropertyConfiguration::class, BluePrintPropertiesService::class, BluePrintDBLibConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ResourceDictionaryReactRepositoryTest {

    private val sourceName = "test-source"

    @Autowired
    lateinit var resourceDictionaryRepository: ResourceDictionaryRepository

    @Test
    @Commit
    fun test01Save() {
        val resourceDefinition = JacksonUtils
            .readValueFromFile(
                "./../../../../../components/model-catalog/resource-dictionary/starter-dictionary/sample-db-source.json",
                ResourceDefinition::class.java
            )
        Assert.assertNotNull("Failed to get resourceDefinition from content", resourceDefinition)
        resourceDefinition!!.name = sourceName

        val resourceDictionary = transformResourceDictionary(resourceDefinition)
        val dbResourceDictionary = resourceDictionaryRepository.save(resourceDictionary)
        Assert.assertNotNull("Failed to save ResourceDictionary", dbResourceDictionary)
    }

    @Test
    fun test02FindByNameReact() {
        val dbResourceDictionary = resourceDictionaryRepository.findByName(sourceName)
        Assert.assertNotNull("Failed to query React Resource Dictionary by Name", dbResourceDictionary)
    }

    @Test
    fun test03FindByNameInReact() {
        val dbResourceDictionaries = resourceDictionaryRepository.findByNameIn(arrayListOf(sourceName))
        Assert.assertNotNull("Failed to query React Resource Dictionary by Names", dbResourceDictionaries)
    }

    @Test
    fun test04FindByTagsContainingIgnoreCaseReact() {
        val dbTagsResourceDictionaries = resourceDictionaryRepository.findByTagsContainingIgnoreCase(sourceName)
        Assert.assertNotNull("Failed to query React Resource Dictionary by Tags", dbTagsResourceDictionaries)
    }

    @Test
    @Transactional
    @Commit
    fun test05Delete() {
        resourceDictionaryRepository.deleteByName(sourceName)
    }

    private fun transformResourceDictionary(resourceDefinition: ResourceDefinition): ResourceDictionary {
        val resourceDictionary = ResourceDictionary()
        resourceDictionary.name = resourceDefinition.name
        resourceDictionary.dataType = resourceDefinition.property.type
        resourceDictionary.description = resourceDefinition.property.description!!
        resourceDictionary.tags = resourceDefinition.tags!!
        resourceDictionary.updatedBy = resourceDefinition.updatedBy
        resourceDictionary.definition = resourceDefinition
        return resourceDictionary
    }
}

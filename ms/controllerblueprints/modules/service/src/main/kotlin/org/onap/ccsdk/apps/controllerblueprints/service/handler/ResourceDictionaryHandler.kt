/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints.service.handler

import com.google.common.base.Preconditions
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceSourceMapping
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.factory.ResourceSourceMappingFactory
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.service.ResourceDefinitionValidationService
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ResourceDictionary
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ResourceDictionaryRepository
import org.onap.ccsdk.apps.controllerblueprints.service.validator.ResourceDictionaryValidator
import org.springframework.stereotype.Service

@Service
class ResourceDictionaryHandler(private val resourceDictionaryRepository: ResourceDictionaryRepository,
                                private val resourceDictionaryValidationService: ResourceDefinitionValidationService) {


    /**
     * This is a getDataDictionaryByName service
     *
     * @param name name
     * @return DataDictionary
     * @throws BluePrintException BluePrintException
     */
    @Throws(BluePrintException::class)
    fun getResourceDictionaryByName(name: String): ResourceDictionary {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "Resource dictionary Name Information is missing.")
        val resourceDictionaryDb = resourceDictionaryRepository.findByName(name)
        return if (resourceDictionaryDb.isPresent) {
            resourceDictionaryDb.get()
        } else {
            throw BluePrintException(String.format("couldn't get resource dictionary for name (%s)", name))
        }
    }

    /**
     * This is a searchResourceDictionaryByNames service
     *
     * @param names names
     * @return List<ResourceDictionary>
    </ResourceDictionary> */
    fun searchResourceDictionaryByNames(names: List<String>): List<ResourceDictionary> {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(names), "No Search Information provide")
        return resourceDictionaryRepository.findByNameIn(names)
    }

    /**
     * This is a searchResourceDictionaryByTags service
     *
     * @param tags tags
     * @return List<ResourceDictionary>
    </ResourceDictionary> */
    fun searchResourceDictionaryByTags(tags: String): List<ResourceDictionary> {
        Preconditions.checkArgument(StringUtils.isNotBlank(tags), "No search tag information provide")
        return resourceDictionaryRepository.findByTagsContainingIgnoreCase(tags)
    }

    /**
     * This is a saveDataDictionary service
     *
     * @param resourceDictionary resourceDictionary
     * @return DataDictionary
     */
    @Throws(BluePrintException::class)
    fun saveResourceDictionary(resourceDictionary: ResourceDictionary): ResourceDictionary {
        var resourceDictionary = resourceDictionary

        val resourceDefinition = resourceDictionary.definition
        Preconditions.checkNotNull(resourceDefinition, "failed to get resource definition from content")
        // Validate the Resource Definitions
        resourceDictionaryValidationService.validate(resourceDefinition)

        resourceDictionary.tags = resourceDefinition.tags
        resourceDefinition.updatedBy = resourceDictionary.updatedBy
        // Set the Property Definitions
        val propertyDefinition = resourceDefinition.property
        resourceDictionary.description = propertyDefinition.description
        resourceDictionary.dataType = propertyDefinition.type
        if (propertyDefinition.entrySchema != null) {
            resourceDictionary.entrySchema = propertyDefinition.entrySchema!!.type
        }

        ResourceDictionaryValidator.validateResourceDictionary(resourceDictionary)

        val dbResourceDictionaryData = resourceDictionaryRepository.findByName(resourceDictionary.name)
        if (dbResourceDictionaryData.isPresent) {
            val dbResourceDictionary = dbResourceDictionaryData.get()

            dbResourceDictionary.name = resourceDictionary.name
            dbResourceDictionary.definition = resourceDictionary.definition
            dbResourceDictionary.description = resourceDictionary.description
            dbResourceDictionary.tags = resourceDictionary.tags
            dbResourceDictionary.updatedBy = resourceDictionary.updatedBy
            dbResourceDictionary.dataType = resourceDictionary.dataType
            dbResourceDictionary.entrySchema = resourceDictionary.entrySchema
            resourceDictionary = resourceDictionaryRepository.save(dbResourceDictionary)
        } else {
            resourceDictionary = resourceDictionaryRepository.save(resourceDictionary)
        }

        return resourceDictionary
    }

    /**
     * This is a deleteResourceDictionary service
     *
     * @param name name
     */
    fun deleteResourceDictionary(name: String) {
        check(name.isNotBlank()) { "Resource dictionary name is missing." }
        resourceDictionaryRepository.deleteByName(name)
    }

    /**
     * This is a getResourceSourceMapping service
     */
    fun getResourceSourceMapping(): ResourceSourceMapping {
        return ResourceSourceMappingFactory.getRegisterSourceMapping()
    }
}
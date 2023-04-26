/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

import com.google.common.base.Preconditions
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.DesignerApiDomains
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ResourceDictionary
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.repository.ResourceDictionaryRepository
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.httpProcessorException
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceSourceMapping
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.factory.ResourceSourceMappingFactory
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.springframework.stereotype.Service
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Service
class ResourceDictionaryHandler(private val resourceDictionaryRepository: ResourceDictionaryRepository) {

    /**
     * This is a getResourceDictionaryByName service
     *
     * @param name name
     * @return ResourceDictionary
     * @throws BluePrintException BluePrintException
     */
    @Throws(BluePrintException::class)
    suspend fun getResourceDictionaryByName(name: String): ResourceDictionary {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "Resource dictionary Name Information is missing.")
        val resourceDictionaryDb = resourceDictionaryRepository.findByName(name)
        return if (resourceDictionaryDb != null) {
            resourceDictionaryDb
        } else {
            throw httpProcessorException(
                ErrorCatalogCodes.RESOURCE_NOT_FOUND, DesignerApiDomains.DESIGNER_API,
                String.format("couldn't get resource dictionary for name (%s)", name)
            )
        }
    }

    /**
     * This is a searchResourceDictionaryByNames service
     *
     * @param names names
     * @return List<ResourceDictionary>
     */
    suspend fun searchResourceDictionaryByNames(names: List<String>): List<ResourceDictionary> {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(names), "No Search Information provide")
        return resourceDictionaryRepository.findByNameIn(names)
    }

    /**
     * This is a searchResourceDictionaryByTags service
     *
     * @param tags tags
     * @return List<ResourceDictionary>
     */
    suspend fun searchResourceDictionaryByTags(tags: String): List<ResourceDictionary> {
        Preconditions.checkArgument(StringUtils.isNotBlank(tags), "No search tag information provide")
        return resourceDictionaryRepository.findByTagsContainingIgnoreCase(tags)
    }

    /**
     * This is a saveResourceDictionary service
     *
     * @param resourceDictionary resourceDictionary
     * @return ResourceDictionary
     */
    @Throws(BluePrintException::class)
    suspend fun saveResourceDictionary(resourceDictionary: ResourceDictionary): ResourceDictionary {
        var resourceDictionary = resourceDictionary

        val resourceDefinition = resourceDictionary.definition
        Preconditions.checkNotNull(resourceDefinition, "failed to get resource definition from content")
        // Validate the Resource Definitions
        // TODO( Save Validator)
        // validate(resourceDefinition)

        resourceDictionary.tags = resourceDefinition.tags!!
        resourceDefinition.updatedBy = resourceDictionary.updatedBy
        resourceDictionary.resourceDictionaryGroup = resourceDefinition.group
        // Set the Property Definitions
        val propertyDefinition = resourceDefinition.property
        resourceDictionary.description = propertyDefinition.description!!
        resourceDictionary.dataType = propertyDefinition.type
        if (propertyDefinition.entrySchema != null) {
            resourceDictionary.entrySchema = propertyDefinition.entrySchema!!.type
        }

        validateResourceDictionary(resourceDictionary)

        val dbResourceDictionaryData = resourceDictionaryRepository.findByName(resourceDictionary.name)
        if (dbResourceDictionaryData != null) {
            val dbResourceDictionary = dbResourceDictionaryData

            dbResourceDictionary.name = resourceDictionary.name
            dbResourceDictionary.definition = resourceDictionary.definition
            dbResourceDictionary.description = resourceDictionary.description
            dbResourceDictionary.tags = resourceDictionary.tags
            dbResourceDictionary.resourceDictionaryGroup = resourceDictionary.resourceDictionaryGroup
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
     * This is to save single ResourceDictionary from json content
     *
     * @param resourceDefinition ResourceDefinition
     * @return ResourceDictionary
     */
    @Throws(BluePrintException::class)
    suspend fun saveResourceDefinition(resourceDefinition: ResourceDefinition): ResourceDictionary {
        return resourceDictionaryRepository.save(enrichResourceDictionary(resourceDefinition))
    }

    /**
     * This is to save multiple ResourceDictionaries from json array content
     *
     * @param resourceDefinitionList List<ResourceDefinition>
     * @return MutableList<ResourceDictionary>
     */
    @Throws(BluePrintException::class)
    suspend fun saveAllResourceDefinition(resourceDefinitionList: List<ResourceDefinition>): MutableList<ResourceDictionary> {
        val dictionaryList: MutableList<ResourceDictionary> = mutableListOf()
        resourceDefinitionList.forEach {
            dictionaryList.add(enrichResourceDictionary(it))
        }
        return resourceDictionaryRepository.saveAll(dictionaryList)
    }

    private fun enrichResourceDictionary(resourceDefinition: ResourceDefinition): ResourceDictionary {
        val resourceDictionary = ResourceDictionary()
        resourceDictionary.name = resourceDefinition.name
        resourceDictionary.updatedBy = resourceDefinition.updatedBy
        resourceDictionary.resourceDictionaryGroup = resourceDefinition.group
        resourceDictionary.entrySchema = resourceDefinition.property.entrySchema?.type
        resourceDictionary.description = resourceDefinition.property.description!!
        resourceDictionary.dataType = resourceDefinition.property.type
        resourceDictionary.definition = resourceDefinition

        if (StringUtils.isBlank(resourceDefinition.tags)) {
            resourceDictionary.tags = resourceDefinition.name + ", " + resourceDefinition.updatedBy
        } else {
            resourceDictionary.tags = resourceDefinition.tags!!
        }

        validateResourceDictionary(resourceDictionary)
        return resourceDictionary
    }

    /**
     * This is a deleteResourceDictionary service
     *
     * @param name name
     */
    suspend fun deleteResourceDictionary(name: String) {
        check(name.isNotBlank()) { "Resource dictionary name is missing." }
        resourceDictionaryRepository.deleteByName(name)
    }

    /**
     * This is a getResourceSourceMapping service
     */
    suspend fun getResourceSourceMapping(): ResourceSourceMapping {
        return ResourceSourceMappingFactory.getRegisterSourceMapping()
    }

    /**
     * This is a getResourceDirectories service
     */
    suspend fun getAllDictionary(pageRequest: Pageable): Page<ResourceDictionary> {
        return resourceDictionaryRepository.findAll(pageRequest)
    }

    private fun validateResourceDictionary(resourceDictionary: ResourceDictionary): Boolean {
        checkNotEmpty(resourceDictionary.name) { "ResourceDictionary Definition name is missing." }
        checkNotNull(resourceDictionary.definition) { "ResourceDictionary Definition Information is missing." }
        checkNotEmpty(resourceDictionary.description) { "ResourceDictionary Definition Information is missing." }
        checkNotEmpty(resourceDictionary.tags) { "ResourceDictionary Definition tags is missing." }
        checkNotEmpty(resourceDictionary.updatedBy) { "ResourceDictionary Definition updatedBy is missing." }
        return true
    }

    fun getResourceDictionaryDistinct(): List<String> {
        return resourceDictionaryRepository.findDistinctByResourceDictionaryGroup()
    }
}

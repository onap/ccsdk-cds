/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package org.onap.ccsdk.cds.controllerblueprints.service

import com.google.common.base.Preconditions
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.utils.ResourceDictionaryUtils
import org.onap.ccsdk.cds.controllerblueprints.service.domain.ResourceDictionary
import org.onap.ccsdk.cds.controllerblueprints.service.model.AutoMapResponse
import org.onap.ccsdk.cds.controllerblueprints.service.repository.ResourceDictionaryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
open class AutoResourceMappingService(private val dataDictionaryRepository: ResourceDictionaryRepository) {

    private val log = LoggerFactory.getLogger(AutoResourceMappingService::class.java)

    @Throws(BluePrintException::class)
    fun autoMap(resourceAssignments: MutableList<ResourceAssignment>):
            AutoMapResponse {
        val autoMapResponse = AutoMapResponse()
        try {
            if (CollectionUtils.isNotEmpty(resourceAssignments)) {
                // Create the Dictionary definitions for the ResourceAssignment Names
                val dictionaryMap = getDictionaryDefinitions(resourceAssignments)

                for (resourceAssignment in resourceAssignments) {
                    if (StringUtils.isNotBlank(resourceAssignment.name)
                            && StringUtils.isBlank(resourceAssignment.dictionaryName)) {
                        populateDictionaryMapping(dictionaryMap, resourceAssignment)
                        log.info("Mapped Resource : {}", resourceAssignment)
                    }
                }
            }
            val dictionaries = getDictionaryDefinitionsList(resourceAssignments)
            val resourceAssignmentsFinal = getAllAutoMapResourceAssignments(resourceAssignments)
            autoMapResponse.dataDictionaries = dictionaries
            autoMapResponse.resourceAssignments = resourceAssignmentsFinal
        } catch (e: Exception) {
            log.error(String.format("Failed in auto process %s", e.message))
            throw BluePrintException(e, e.message!!)
        }

        return autoMapResponse
    }

    private fun populateDictionaryMapping(dictionaryMap: Map<String, ResourceDictionary>, resourceAssignment: ResourceAssignment) {
        val dbDataDictionary = dictionaryMap[resourceAssignment.name]
        if (dbDataDictionary != null && dbDataDictionary.definition != null) {

            val dictionaryDefinition = dbDataDictionary.definition

            if (dictionaryDefinition != null && StringUtils.isNotBlank(dictionaryDefinition.name)
                    && StringUtils.isBlank(resourceAssignment.dictionaryName)) {

                resourceAssignment.dictionaryName = dbDataDictionary.name
                ResourceDictionaryUtils.populateSourceMapping(resourceAssignment, dictionaryDefinition)
            }
        }
    }

    private fun getDictionaryDefinitions(resourceAssignments: List<ResourceAssignment>): Map<String, ResourceDictionary> {
        val dictionaryMap = HashMap<String, ResourceDictionary>()
        val names = ArrayList<String>()
        for (resourceAssignment in resourceAssignments) {
            if (StringUtils.isNotBlank(resourceAssignment.name)) {
                names.add(resourceAssignment.name)
            }
        }
        if (CollectionUtils.isNotEmpty(names)) {

            val dictionaries = dataDictionaryRepository.findByNameIn(names)
            if (CollectionUtils.isNotEmpty(dictionaries)) {
                for (dataDictionary in dictionaries) {
                    if (StringUtils.isNotBlank(dataDictionary.name)) {
                        dictionaryMap[dataDictionary.name] = dataDictionary
                    }
                }
            }
        }
        return dictionaryMap

    }
    private fun getDictionaryDefinitionsList(resourceAssignments: List<ResourceAssignment>): List<ResourceDictionary>? {
        var dictionaries: List<ResourceDictionary>? = null
        val names = ArrayList<String>()
        for (resourceAssignment in resourceAssignments) {
            if (StringUtils.isNotBlank(resourceAssignment.dictionaryName)) {

                if (!names.contains(resourceAssignment.dictionaryName)) {
                    names.add(resourceAssignment.dictionaryName!!)
                }

                if (resourceAssignment.dependencies != null && !resourceAssignment.dependencies!!.isEmpty()) {
                    val dependencyNames = resourceAssignment.dependencies
                    for (dependencyName in dependencyNames!!) {
                        if (StringUtils.isNotBlank(dependencyName) && !names.contains(dependencyName)) {
                            names.add(dependencyName)
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(names)) {
            dictionaries = dataDictionaryRepository.findByNameIn(names)
        }
        return dictionaries

    }

    private fun getAllAutoMapResourceAssignments(resourceAssignments: MutableList<ResourceAssignment>): List<ResourceAssignment> {
        var dictionaries: List<ResourceDictionary>? = null
        val names = ArrayList<String>()
        for (resourceAssignment in resourceAssignments) {
            if (StringUtils.isNotBlank(resourceAssignment.dictionaryName)) {
                if (resourceAssignment.dependencies != null && !resourceAssignment.dependencies!!.isEmpty()) {
                    val dependencyNames = resourceAssignment.dependencies
                    for (dependencyName in dependencyNames!!) {
                        if (StringUtils.isNotBlank(dependencyName) && !names.contains(dependencyName)
                                && !checkAssignmentsExists(resourceAssignments, dependencyName)) {
                            names.add(dependencyName)
                        }
                    }
                }
            }
        }

        if (!names.isEmpty()) {
            dictionaries = dataDictionaryRepository.findByNameIn(names)
        }
        if (dictionaries != null) {
            for (rscDictionary in dictionaries) {
                val dictionaryDefinition = rscDictionary.definition
                Preconditions.checkNotNull(dictionaryDefinition, "failed to get Resource Definition from dictionary definition")
                val property = PropertyDefinition()
                property.required = true
                val resourceAssignment = ResourceAssignment()
                resourceAssignment.name = rscDictionary.name
                resourceAssignment.dictionaryName = rscDictionary.name
                resourceAssignment.version = 0
                resourceAssignment.property = property
                ResourceDictionaryUtils.populateSourceMapping(resourceAssignment, dictionaryDefinition)
                resourceAssignments.add(resourceAssignment)
            }
        }
        return resourceAssignments
    }


    private fun checkAssignmentsExists(resourceAssignmentsWithDepencies: List<ResourceAssignment>, resourceName: String): Boolean {
        return resourceAssignmentsWithDepencies.stream().anyMatch { names -> names.name.equals(resourceName, ignoreCase = true) }
    }
}
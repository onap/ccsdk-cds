/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.service

import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.text.StrBuilder
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.utils.TopologicalSortingUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import java.io.Serializable

/**
 * ResourceAssignmentValidationService.
 *
 * @author Brinda Santh
 */
interface ResourceAssignmentValidationService : Serializable {

    @Throws(BluePrintException::class)
    fun validate(resourceAssignments: List<ResourceAssignment>): Boolean
}

/**
 * ResourceAssignmentValidationDefaultService.
 *
 * @author Brinda Santh
 */
open class ResourceAssignmentValidationDefaultService : ResourceAssignmentValidationService {
    private val log = LoggerFactory.getLogger(ResourceAssignmentValidationDefaultService::class.java)

    open var resourceAssignmentMap: Map<String, ResourceAssignment> = hashMapOf()
    open val validationMessage = StrBuilder()

    override fun validate(resourceAssignments: List<ResourceAssignment>): Boolean {
        try {
            validateSources(resourceAssignments)
            validateTemplateNDictionaryKeys(resourceAssignments)
            validateCyclicDependency(resourceAssignments)
            if (StringUtils.isNotBlank(validationMessage)) {
                throw BluePrintException("Resource Assignment Validation Failure")
            }
        } catch (e: Exception) {
            throw BluePrintException("Resource Assignment Validation :" + validationMessage.toString(), e)
        }
        return true
    }

    open fun validateSources(resourceAssignments: List<ResourceAssignment>) {
        log.info("validating resource assignment sources")
    }

    open fun validateTemplateNDictionaryKeys(resourceAssignments: List<ResourceAssignment>) {

        resourceAssignmentMap = resourceAssignments.map { it.name to it }.toMap()

        val duplicateKeyNames = resourceAssignments.groupBy { it.name }
                .filter { it.value.size > 1 }
                .map { it.key }

        if (duplicateKeyNames.isNotEmpty()) {
            validationMessage.appendln(String.format("Duplicate Assignment Template Keys (%s) is Present", duplicateKeyNames))
        }

        val duplicateDictionaryKeyNames = resourceAssignments.groupBy { it.dictionaryName }
                .filter { it.value.size > 1 }
                .map { it.key }
        if (duplicateDictionaryKeyNames.isNotEmpty()) {
            validationMessage.appendln(String.format("Duplicate Assignment Dictionary Keys (%s) is Present", duplicateDictionaryKeyNames))
        }

        val dependenciesNames = resourceAssignments.mapNotNull { it.dependencies }.flatten()

        log.info("Resource assignment definitions : {}", resourceAssignmentMap.keys)
        log.info("Resource assignment Dictionary dependencies : {}", dependenciesNames)

        val notPresentDictionaries = dependenciesNames.filter { !resourceAssignmentMap.containsKey(it) }.distinct()
        if (notPresentDictionaries.isNotEmpty()) {
            validationMessage.appendln(String.format("No assignments for Dictionary Keys (%s)", notPresentDictionaries))
        }

        if (StringUtils.isNotBlank(validationMessage)) {
            throw BluePrintException("Resource Assignment Validation Failure")
        }
    }

    open fun validateCyclicDependency(resourceAssignments: List<ResourceAssignment>) {
        val startResourceAssignment = ResourceAssignment()
        startResourceAssignment.name = "*"

        val topologySorting = TopologicalSortingUtils<ResourceAssignment>()

        resourceAssignmentMap.map { it.value }.map { resourceAssignment ->
            if (CollectionUtils.isNotEmpty(resourceAssignment.dependencies)) {
                resourceAssignment.dependencies!!.map {
                    log.info("Topological Graph link from {} to {}", it, resourceAssignment.name)
                    topologySorting.add(resourceAssignmentMap[it]!!, resourceAssignment)
                }
            } else {
                topologySorting.add(startResourceAssignment, resourceAssignment)
            }
        }

        if (!topologySorting.isDag) {
            val graph = getTopologicalGraph(topologySorting)
            validationMessage.appendln("Cyclic Dependency :$graph")
        }
    }

    open fun getTopologicalGraph(topologySorting: TopologicalSortingUtils<ResourceAssignment>): String {
        val s = StringBuilder()
        val neighbors = topologySorting.getNeighbors()

        neighbors.forEach { v, vs ->
            if (v.name == "*") {
                s.append("\n    * -> [")
                for (resourceAssignment in vs) {
                    s.append("(" + resourceAssignment.dictionaryName + ":" + resourceAssignment.name
                            + "),")
                }
                s.append("]")
            } else {
                s.append("\n    (" + v.dictionaryName + ":" + v.name + ") -> [")
                for (resourceAssignment in vs) {
                    s.append("(" + resourceAssignment.dictionaryName + ":" + resourceAssignment.name
                            + "),")
                }
                s.append("]")
            }
        }
        return s.toString()
    }


}
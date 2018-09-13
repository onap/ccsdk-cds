/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018 IBM.
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

import com.att.eelf.configuration.EELFLogger
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.text.StrBuilder
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.utils.TopologicalSortingUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.format
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.factory.ResourceSourceMappingFactory
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
    private val log: EELFLogger = EELFManager.getInstance().getLogger(ResourceAssignmentValidationDefaultService::class.java)

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
        // Check the Resource Assignment Source(Dynamic Instance) is valid.
        resourceAssignments.forEach { resourceAssignment ->
            try {
                ResourceSourceMappingFactory.getRegisterSourceMapping(resourceAssignment.dictionarySource!!)
            } catch (e: BluePrintException) {
                validationMessage.appendln(e.message + format(" for resource assignment({})", resourceAssignment.name))
            }
        }
    }

    open fun validateTemplateNDictionaryKeys(resourceAssignments: List<ResourceAssignment>) {

        resourceAssignmentMap = resourceAssignments.map { it.name to it }.toMap()

        // Check the Resource Assignment has Duplicate Key Names
        val duplicateKeyNames = resourceAssignments.groupBy { it.name }
                .filter { it.value.size > 1 }
                .map { it.key }

        if (duplicateKeyNames.isNotEmpty()) {
            validationMessage.appendln(String.format("Duplicate Assignment Template Keys (%s) is Present", duplicateKeyNames))
        }

        // Check the Resource Assignment has Duplicate Dictionary Names
        val duplicateDictionaryKeyNames = resourceAssignments.groupBy { it.dictionaryName }
                .filter { it.value.size > 1 }
                .map { it.key }
        if (duplicateDictionaryKeyNames.isNotEmpty()) {
            validationMessage.appendln(String.format("Duplicate Assignment Dictionary Keys (%s) is Present", duplicateDictionaryKeyNames))
        }

        // Collect all the dependencies as a single list
        val dependenciesNames = resourceAssignments.mapNotNull { it.dependencies }.flatten()

        // Check all the dependencies keys have Resource Assignment mappings.
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
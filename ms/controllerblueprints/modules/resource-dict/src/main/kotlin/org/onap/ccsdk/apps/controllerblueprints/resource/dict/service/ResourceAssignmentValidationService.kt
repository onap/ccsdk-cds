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
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.validator.ResourceAssignmentValidator
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
    private val log = LoggerFactory.getLogger(ResourceAssignmentValidator::class.java)
    open var resourceAssignments: List<ResourceAssignment> = arrayListOf()
    open var resourceAssignmentMap: MutableMap<String, ResourceAssignment> = hashMapOf()
    open val validationMessage = StrBuilder()

    override fun validate(resourceAssignments: List<ResourceAssignment>): Boolean {
        this.resourceAssignments = resourceAssignments
        validateSources(resourceAssignments)
        validateDuplicateDictionaryKeys()
        validateCyclicDependency()
        if (StringUtils.isNotBlank(validationMessage)) {
            throw BluePrintException("Resource Assignment Validation :" + validationMessage.toString())
        }
        return true
    }

    open fun validateSources(resourceAssignments: List<ResourceAssignment>) {
        log.info("validating resource assignment sources")
    }

    open fun validateDuplicateDictionaryKeys() {
        val uniqueDictionaryKeys = hashSetOf<String>()

        this.resourceAssignments.forEach { resourceAssignment ->
            // Check Duplicate Names
            if (!resourceAssignmentMap.containsKey(resourceAssignment.name)) {
                resourceAssignmentMap[resourceAssignment.name] = resourceAssignment
            } else {
                validationMessage.appendln(String.format("Duplicate Assignment Template Key (%s) is Present",
                        resourceAssignment.name))
            }
            // Check duplicate Dictionary Keys
            if (!uniqueDictionaryKeys.contains(resourceAssignment.dictionaryName!!)) {
                uniqueDictionaryKeys.add(resourceAssignment.dictionaryName!!)
            } else {
                validationMessage.appendln(
                        String.format("Duplicate Assignment Dictionary Key (%s) present with Template Key (%s)",
                                resourceAssignment.dictionaryName, resourceAssignment.name))
            }
        }
    }

    open fun validateCyclicDependency() {
        val startResourceAssignment = ResourceAssignment()
        startResourceAssignment.name = "*"

        val topologySorting = TopologicalSortingUtils<ResourceAssignment>()
        this.resourceAssignmentMap.forEach { assignmentKey, assignment ->
            if (CollectionUtils.isNotEmpty(assignment.dependencies)) {
                for (dependency in assignment.dependencies!!) {
                    topologySorting.add(resourceAssignmentMap[dependency]!!, assignment)
                }
            } else {
                topologySorting.add(startResourceAssignment, assignment)
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
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

package org.onap.ccsdk.cds.controllerblueprints.resource.dict.utils

import org.apache.commons.collections.CollectionUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asListOfString
import org.onap.ccsdk.cds.controllerblueprints.core.utils.TopologicalSortingUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import java.util.ArrayList

/**
 * BulkResourceSequencingUtils.
 *
 * @author Brinda Santh
 */
object BulkResourceSequencingUtils {

    private val log = LoggerFactory.getLogger(BulkResourceSequencingUtils::class.java)

    @JvmStatic
    fun process(resourceAssignments: MutableList<ResourceAssignment>): List<List<ResourceAssignment>> {
        val resourceAssignmentMap: MutableMap<String, ResourceAssignment> = hashMapOf()
        val sequenceBatchResourceAssignment = ArrayList<List<ResourceAssignment>>()
        log.trace("Assignments ({})", resourceAssignments)
        // Prepare Map
        resourceAssignments.forEach { resourceAssignment ->
            log.trace("Processing Key ({})", resourceAssignment.name)
            resourceAssignmentMap.put(resourceAssignment.name, resourceAssignment)
        }

        val startResourceAssignment = ResourceAssignment()
        startResourceAssignment.name = "*"

        // Preepare Sorting Map
        val topologySorting = TopologicalSortingUtils<ResourceAssignment>()
        resourceAssignmentMap.forEach { _, resourceAssignment ->
            // Get the dependencies from the assignment sources, if not get from the Resource Assignment dependencies
            if (resourceAssignment.dictionarySourceDefinition != null) {
                val dependencies =
                    resourceAssignment.dictionarySourceDefinition?.properties?.get("key-dependencies")?.asListOfString()
                dependencies?.forEach { dependency ->
                    topologySorting.add(resourceAssignmentMap[dependency]!!, resourceAssignment)
                }
            } else if (CollectionUtils.isNotEmpty(resourceAssignment.dependencies)) {
                for (dependency in resourceAssignment.dependencies!!) {
                    val ra = resourceAssignmentMap[dependency]
                        ?: throw BluePrintProcessorException(
                            "Couldn't get Resource Assignment dependency " +
                                "Key($dependency)"
                        )
                    topologySorting.add(ra, resourceAssignment)
                }
            } else {
                topologySorting.add(startResourceAssignment, resourceAssignment)
            }
        }

        val sequencedResourceAssignments: MutableList<ResourceAssignment> =
            topologySorting.topSort()!! as MutableList<ResourceAssignment>
        log.trace("Sorted Sequenced Assignments ({})", sequencedResourceAssignments)

        var batchResourceAssignment: MutableList<ResourceAssignment>? = null
        var batchAssignmentName: MutableList<String>? = null

        // Prepare Sorting
        sequencedResourceAssignments.forEachIndexed { index, resourceAssignment ->

            var previousResourceAssignment: ResourceAssignment? = null

            if (index > 0) {
                previousResourceAssignment = sequencedResourceAssignments[index - 1]
            }

            var dependencyPresence = false
            if (batchAssignmentName != null && resourceAssignment.dependencies != null) {
                dependencyPresence = CollectionUtils.containsAny(batchAssignmentName, resourceAssignment.dependencies)
            }

            log.trace(
                "({}) -> Checking ({}), with ({}), result ({})", resourceAssignment.name,
                batchAssignmentName, resourceAssignment.dependencies, dependencyPresence
            )

            if (previousResourceAssignment != null && resourceAssignment.dictionarySource != null &&
                resourceAssignment.dictionarySource!!.equals(previousResourceAssignment.dictionarySource, true) &&
                !dependencyPresence
            ) {
                batchResourceAssignment!!.add(resourceAssignment)
                batchAssignmentName!!.add(resourceAssignment.name)
            } else {
                if (batchResourceAssignment != null) {
                    sequenceBatchResourceAssignment.add(batchResourceAssignment!!)
                    log.trace("Created old Set ({})", batchAssignmentName)
                }
                batchResourceAssignment = arrayListOf()
                batchResourceAssignment!!.add(resourceAssignment)

                batchAssignmentName = arrayListOf()
                batchAssignmentName!!.add(resourceAssignment.name)
            }

            if (index == sequencedResourceAssignments.size - 1) {
                log.trace("Created old Set ({})", batchAssignmentName)
                sequenceBatchResourceAssignment.add(batchResourceAssignment!!)
            }
        }
        log.info("Batched Sequence : ({})", sequenceBatchResourceAssignment)

        return sequenceBatchResourceAssignment
    }
}

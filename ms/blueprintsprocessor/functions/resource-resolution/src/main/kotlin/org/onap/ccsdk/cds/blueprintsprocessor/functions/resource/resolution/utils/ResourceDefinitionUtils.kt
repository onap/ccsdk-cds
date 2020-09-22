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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.asListOfString
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition

object ResourceDefinitionUtils {

    fun definitionDependencies(definition: ResourceDefinition, sources: List<String>): Set<String> {
        val dependencies: MutableSet<String> = mutableSetOf()
        definition.sources.forEach { (sourceName, source) ->
            if (sources.contains(sourceName)) {
                val keyDependenciesExists = source.properties?.containsKey("key-dependencies") ?: false
                if (keyDependenciesExists) {
                    dependencies.addAll(source.properties!!["key-dependencies"]!!.asListOfString())
                }
            }
        }
        return dependencies
    }

    /** Create a processing resource assignments for the resource definition  */
    fun createResourceAssignments(
        resourceDefinitions: MutableMap<String, ResourceDefinition>,
        resolveDefinition: String,
        sources: List<String>
    ):
        MutableList<ResourceAssignment> {
            /** Check if resolve definition is defined in the resource definition Map */
            val resourceDefinition = resourceDefinitions[resolveDefinition]
                ?: throw BluePrintProcessorException("failed to get resolve definition($resolveDefinition)")

            val resourceAssignments: MutableList<ResourceAssignment> = arrayListOf()
            /** Get the dependency property fields for the the resource definition to resolve */
            val definitionDependencies = definitionDependencies(resourceDefinition, sources)
            definitionDependencies.forEach { definitionDependencyName ->
                val definitionDependency = resourceDefinitions[definitionDependencyName]
                    ?: throw BluePrintProcessorException("failed to get dependency definition($definitionDependencyName)")

                val resourceAssignment = ResourceAssignment().apply {
                    name = definitionDependency.name
                    dictionaryName = definitionDependency.name
                    /** The assumption is al resource are already resolved and shall get as input source */
                    dictionarySource = "input"
                    property = definitionDependency.property
                }
                resourceAssignments.add(resourceAssignment)
            }

            resourceDefinition.sources.forEach { (sourceName, source) ->
                if (sources.contains(sourceName)) {
                    val resourceAssignment = ResourceAssignment().apply {
                        name = "$sourceName:${resourceDefinition.name}"
                        dictionaryName = resourceDefinition.name
                        dictionarySource = sourceName
                        dictionarySourceDefinition = source
                        // Clone the PropertyDefinition, otherwise property value will be overridden
                        property = JacksonUtils
                            .readValue(resourceDefinition.property.asJsonString(), PropertyDefinition::class.java)
                        val keyDependenciesExists = source.properties?.containsKey("key-dependencies") ?: false
                        if (keyDependenciesExists) {
                            dependencies = source.properties!!["key-dependencies"]!!.asListOfString().toMutableList()
                        }
                    }
                    resourceAssignments.add(resourceAssignment)
                }
            }
            // Populate Resource Definition's dependencies as Input Resource Assignment
            return resourceAssignments
        }
}

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
import org.onap.ccsdk.cds.controllerblueprints.core.asListOfString
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition

object ResourceDefinitionUtils {

    fun definitionDependencies(definition: ResourceDefinition): Set<String> {
        val dependencies: MutableSet<String> = mutableSetOf()
        definition.sources.forEach { (_, source) ->
            val keyDependenciesExists = source.properties?.containsKey("key-dependencies") ?: false
            if (keyDependenciesExists) {
                dependencies.addAll(source.properties!!["key-dependencies"]!!.asListOfString())
            }
        }
        return dependencies
    }

    @Throws(BluePrintProcessorException::class)
    fun checkDependencyDataExists(blueprintRuntimeService: BluePrintRuntimeService<*>,
                                  definitionDependencies: Set<String>): Boolean {
        //TODO("Provide the Implementation")
        return true
    }

    fun createResourceAssignments(resourceDefinition: ResourceDefinition): MutableList<ResourceAssignment> {
        val resourceAssignments: MutableList<ResourceAssignment> = arrayListOf()
        resourceDefinition.sources.forEach { (sourceName, source) ->
            val resourceAssignment = ResourceAssignment().apply {
                name = "$source:${resourceDefinition.name}"
                dictionaryName = resourceDefinition.name
                dictionarySource = sourceName
                dictionarySourceDefinition = source
                property = resourceDefinition.property
                val keyDependenciesExists = source.properties?.containsKey("key-dependencies") ?: false
                if (keyDependenciesExists) {
                    dependencies = source.properties!!["key-dependencies"]!!.asListOfString().toMutableList()
                }
            }
            resourceAssignments.add(resourceAssignment)
        }
        return resourceAssignments
    }
}
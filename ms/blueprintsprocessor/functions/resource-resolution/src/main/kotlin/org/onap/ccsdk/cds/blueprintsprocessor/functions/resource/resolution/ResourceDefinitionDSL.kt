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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertyDefinitionBuilder
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition

/** Resource Definition DSL **/
fun BluePrintTypes.resourceDefinitions(block: ResourceDefinitionsBuilder.() -> Unit):
    MutableMap<String, ResourceDefinition> {
        return ResourceDefinitionsBuilder().apply(block).build()
    }

fun BluePrintTypes.resourceDefinition(
    name: String,
    description: String,
    block: ResourceDefinitionBuilder.() -> Unit
): ResourceDefinition {
    return ResourceDefinitionBuilder(name, description).apply(block).build()
}

/** Resource Mapping DSL **/
fun BluePrintTypes.resourceAssignments(block: ResourceAssignmentsBuilder.() -> Unit):
    MutableMap<String, ResourceAssignment> {
        return ResourceAssignmentsBuilder().apply(block).build()
    }

fun BluePrintTypes.resourceAssignment(
    name: String,
    dictionaryName: String,
    dictionarySource: String,
    block: ResourceAssignmentBuilder.() -> Unit
): ResourceAssignment {
    return ResourceAssignmentBuilder(name, dictionaryName, dictionarySource).apply(block).build()
}

class ResourceDefinitionsBuilder() {

    private val resourceDefinitions: MutableMap<String, ResourceDefinition> = hashMapOf()

    fun resourceDefinition(
        name: String,
        description: String,
        block: ResourceDefinitionBuilder.() -> Unit
    ) {
        val resourceDefinition = ResourceDefinitionBuilder(name, description).apply(block).build()
        resourceDefinitions[resourceDefinition.name] = resourceDefinition
    }

    fun resourceDefinition(resourceDefinition: ResourceDefinition) {
        resourceDefinitions[resourceDefinition.name] = resourceDefinition
    }

    fun build(): MutableMap<String, ResourceDefinition> {
        return resourceDefinitions
    }
}

class ResourceDefinitionBuilder(private val name: String, private val description: String) {

    private val resourceDefinition = ResourceDefinition()

    fun updatedBy(updatedBy: String) {
        resourceDefinition.updatedBy = updatedBy
    }

    fun tags(tags: String) {
        resourceDefinition.tags = tags
    }

    fun property(property: PropertyDefinition) {
        resourceDefinition.property = property
    }

    fun property(type: String, required: Boolean) {
        resourceDefinition.property = PropertyDefinitionBuilder(name, type, required, description).build()
    }

    fun property(
        type: String,
        required: Boolean,
        block: PropertyDefinitionBuilder.() -> Unit
    ) {
        resourceDefinition.property = PropertyDefinitionBuilder(name, type, required, description).apply(block).build()
    }

    fun sources(block: ResourceDefinitionSourcesBuilder.() -> Unit) {
        resourceDefinition.sources = ResourceDefinitionSourcesBuilder().apply(block).build()
    }

    fun sources(sources: MutableMap<String, NodeTemplate>) {
        resourceDefinition.sources = sources
    }

    fun build(): ResourceDefinition {
        resourceDefinition.name = name
        return resourceDefinition
    }
}

class ResourceDefinitionSourcesBuilder {

    var sources: MutableMap<String, NodeTemplate> = hashMapOf()

    fun source(source: NodeTemplate) {
        sources[source.id!!] = source
    }

    fun sourceInput(id: String, description: String, block: SourceInputNodeTemplateBuilder.() -> Unit) {
        sources[id] = SourceInputNodeTemplateBuilder(id, description).apply(block).build()
    }

    fun sourceDefault(id: String, description: String, block: SourceDefaultNodeTemplateBuilder.() -> Unit) {
        sources[id] = SourceDefaultNodeTemplateBuilder(id, description).apply(block).build()
    }

    fun sourceDb(id: String, description: String, block: SourceDbNodeTemplateBuilder.() -> Unit) {
        sources[id] = SourceDbNodeTemplateBuilder(id, description).apply(block).build()
    }

    fun sourceRest(id: String, description: String, block: SourceRestNodeTemplateBuilder.() -> Unit) {
        sources[id] = SourceRestNodeTemplateBuilder(id, description).apply(block).build()
    }

    fun sourceCapability(id: String, description: String, block: SourceCapabilityNodeTemplateBuilder.() -> Unit) {
        sources[id] = SourceCapabilityNodeTemplateBuilder(id, description).apply(block).build()
    }

    fun build(): MutableMap<String, NodeTemplate> {
        return sources
    }
}

class ResourceAssignmentsBuilder() {

    private val resourceAssignments: MutableMap<String, ResourceAssignment> = hashMapOf()

    fun resourceAssignment(
        name: String,
        dictionaryName: String,
        dictionarySource: String,
        block: ResourceAssignmentBuilder.() -> Unit
    ) {
        val resourceAssignment = ResourceAssignmentBuilder(name, dictionaryName, dictionarySource).apply(block).build()
        resourceAssignments[resourceAssignment.name] = resourceAssignment
    }

    fun resourceAssignment(resourceAssignment: ResourceAssignment) {
        resourceAssignments[resourceAssignment.name] = resourceAssignment
    }

    fun build(): MutableMap<String, ResourceAssignment> {
        return resourceAssignments
    }
}

class ResourceAssignmentBuilder(
    private val name: String,
    private val dictionaryName: String,
    private val dictionarySource: String
) {

    private val resourceAssignment = ResourceAssignment()

    fun inputParameter(inputParameter: Boolean) {
        resourceAssignment.inputParameter = inputParameter
    }

    fun property(type: String, required: Boolean, description: String? = "") {
        resourceAssignment.property = PropertyDefinitionBuilder(name, type, required, description).build()
    }

    fun property(
        type: String,
        required: Boolean,
        description: String? = "",
        block: PropertyDefinitionBuilder.() -> Unit
    ) {
        resourceAssignment.property = PropertyDefinitionBuilder(name, type, required, description).apply(block).build()
    }

    fun source(source: NodeTemplate) {
        resourceAssignment.dictionarySourceDefinition = source
    }

    fun sourceInput(block: SourceInputNodeTemplateBuilder.() -> Unit) {
        resourceAssignment.dictionarySourceDefinition = SourceInputNodeTemplateBuilder(dictionarySource, "")
            .apply(block).build()
    }

    fun sourceDefault(block: SourceDefaultNodeTemplateBuilder.() -> Unit) {
        resourceAssignment.dictionarySourceDefinition = SourceDefaultNodeTemplateBuilder(dictionarySource, "")
            .apply(block).build()
    }

    fun sourceDb(block: SourceDbNodeTemplateBuilder.() -> Unit) {
        resourceAssignment.dictionarySourceDefinition = SourceDbNodeTemplateBuilder(dictionarySource, "")
            .apply(block).build()
    }

    fun sourceRest(block: SourceRestNodeTemplateBuilder.() -> Unit) {
        resourceAssignment.dictionarySourceDefinition = SourceRestNodeTemplateBuilder(dictionarySource, "")
            .apply(block).build()
    }

    fun sourceCapability(block: SourceCapabilityNodeTemplateBuilder.() -> Unit) {
        resourceAssignment.dictionarySourceDefinition = SourceCapabilityNodeTemplateBuilder(dictionarySource, "")
            .apply(block).build()
    }

    fun dependencies(dependencies: MutableList<String>) {
        resourceAssignment.dependencies = dependencies
    }

    fun build(): ResourceAssignment {
        resourceAssignment.name = name
        resourceAssignment.dictionaryName = dictionaryName
        resourceAssignment.dictionarySource = dictionarySource
        return resourceAssignment
    }
}

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
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertyDefinitionBuilder
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition

/** Resource Definition DSL **/
fun BluePrintTypes.resourceDefinition(name: String,
                                      block: ResourceDefinitionBuilder.() -> Unit): ResourceDefinition {
    return ResourceDefinitionBuilder(name).apply(block).build()
}

class ResourceDefinitionBuilder(private val name: String) {
    private val resourceDefinition = ResourceDefinition()

    fun updatedBy(updatedBy: String) {
        resourceDefinition.updatedBy = updatedBy
    }

    fun tags(tags: String) {
        resourceDefinition.tags = tags
    }

    fun property(id: String, type: String, required: Boolean, description: String? = "") {
        resourceDefinition.property = PropertyDefinitionBuilder(id, type, required, description).build()
    }

    fun property(id: String, type: String, required: Boolean, description: String? = "",
                 block: PropertyDefinitionBuilder.() -> Unit) {
        resourceDefinition.property = PropertyDefinitionBuilder(id, type, required, description).apply(block).build()
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
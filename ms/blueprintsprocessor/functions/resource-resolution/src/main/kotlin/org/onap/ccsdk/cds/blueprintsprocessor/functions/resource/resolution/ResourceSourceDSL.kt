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

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentScriptExecutor
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.AbstractNodeTemplatePropertyImplBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertiesAssignmentBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.nodeType
import kotlin.reflect.KClass

fun BlueprintTypes.nodeTypeSourceInput(): NodeType {
    return nodeType(
        id = "source-input", version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
        description = "This is Input Resource Source Node Type"
    ) {}
}

fun BlueprintTypes.nodeTypeSourceDefault(): NodeType {
    return nodeType(
        id = "source-default", version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
        description = "This is Default Resource Source Node Type"
    ) {}
}

fun BlueprintTypes.nodeTypeSourceDb(): NodeType {
    return nodeType(
        id = "source-db", version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
        description = "This is Database Resource Source Node Type"
    ) {
        property(
            "type", BlueprintConstants.DATA_TYPE_STRING,
            true, ""
        ) {
            defaultValue("SQL".asJsonPrimitive())
            constrain {
                validValues(arrayListOf("SQL".asJsonPrimitive(), "PLSQL".asJsonPrimitive()))
            }
        }
        property(
            "endpoint-selector", BlueprintConstants.DATA_TYPE_STRING,
            false, ""
        )
        property(
            "query", BlueprintConstants.DATA_TYPE_STRING,
            true, ""
        )
        property(
            "input-key-mapping", BlueprintConstants.DATA_TYPE_MAP,
            true, ""
        ) {
            entrySchema(BlueprintConstants.DATA_TYPE_STRING)
        }
        property(
            "output-key-mapping", BlueprintConstants.DATA_TYPE_MAP,
            false, ""
        ) {
            entrySchema(BlueprintConstants.DATA_TYPE_STRING)
        }
        property(
            "key-dependencies", BlueprintConstants.DATA_TYPE_LIST,
            true, "Resource Resolution dependency dictionary names."
        ) {
            entrySchema(BlueprintConstants.DATA_TYPE_STRING)
        }
    }
}

fun BlueprintTypes.nodeTypeSourceRest(): NodeType {
    return nodeType(
        id = "source-rest", version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
        description = "This is Rest Resource Source Node Type"
    ) {
        property(
            "type", BlueprintConstants.DATA_TYPE_STRING,
            true, ""
        ) {
            defaultValue("JSON".asJsonPrimitive())
            constrain {
                validValues(arrayListOf("JSON".asJsonPrimitive(), "XML".asJsonPrimitive()))
            }
        }
        property(
            "verb", BlueprintConstants.DATA_TYPE_STRING,
            true, ""
        ) {
            defaultValue("GET".asJsonPrimitive())
            constrain {
                validValues(
                    arrayListOf(
                        "GET".asJsonPrimitive(), "POST".asJsonPrimitive(),
                        "DELETE".asJsonPrimitive(), "PUT".asJsonPrimitive()
                    )
                )
            }
        }
        property(
            "payload", BlueprintConstants.DATA_TYPE_STRING,
            false, ""
        ) {
            defaultValue("".asJsonPrimitive())
        }
        property(
            "endpoint-selector", BlueprintConstants.DATA_TYPE_STRING,
            false, ""
        )
        property(
            "url-path", BlueprintConstants.DATA_TYPE_STRING,
            true, ""
        )
        property(
            "path", BlueprintConstants.DATA_TYPE_STRING,
            true, ""
        )
        property(
            "expression-type", BlueprintConstants.DATA_TYPE_STRING,
            false, ""
        ) {
            defaultValue("JSON_PATH".asJsonPrimitive())
            constrain {
                validValues(arrayListOf("JSON_PATH".asJsonPrimitive(), "JSON_POINTER".asJsonPrimitive()))
            }
        }
        property(
            "input-key-mapping", BlueprintConstants.DATA_TYPE_MAP,
            true, ""
        ) {
            entrySchema(BlueprintConstants.DATA_TYPE_STRING)
        }
        property(
            "output-key-mapping", BlueprintConstants.DATA_TYPE_MAP,
            false, ""
        ) {
            entrySchema(BlueprintConstants.DATA_TYPE_STRING)
        }
        property(
            "key-dependencies", BlueprintConstants.DATA_TYPE_LIST,
            true, "Resource Resolution dependency dictionary names."
        ) {
            entrySchema(BlueprintConstants.DATA_TYPE_STRING)
        }
    }
}

fun BlueprintTypes.nodeTypeSourceCapability(): NodeType {
    return nodeType(
        id = "source-capability", version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
        description = "This is Component Resource Source Node Type"
    ) {
        property(
            ComponentScriptExecutor.INPUT_SCRIPT_TYPE, BlueprintConstants.DATA_TYPE_STRING,
            true, "Request Id, Unique Id for the request."
        ) {
            defaultValue(BlueprintConstants.SCRIPT_KOTLIN)
            constrain {
                validValues(
                    arrayListOf(
                        BlueprintConstants.SCRIPT_KOTLIN.asJsonPrimitive(),
                        BlueprintConstants.SCRIPT_INTERNAL.asJsonPrimitive(),
                        BlueprintConstants.SCRIPT_JYTHON.asJsonPrimitive()
                    )
                )
            }
        }
        property(
            ComponentScriptExecutor.INPUT_SCRIPT_CLASS_REFERENCE, BlueprintConstants.DATA_TYPE_STRING,
            true, "Kotlin Script class name or jython script name."
        )
        property(
            "key-dependencies", BlueprintConstants.DATA_TYPE_LIST,
            true, "Resource Resolution dependency dictionary names."
        ) {
            entrySchema(BlueprintConstants.DATA_TYPE_STRING)
        }
    }
}

/** Node Template Source Input **/
fun BlueprintTypes.nodeTemplateSourceInput(
    id: String,
    description: String,
    block: SourceInputNodeTemplateBuilder.() -> Unit
): NodeTemplate {
    return SourceInputNodeTemplateBuilder(id, description).apply(block).build()
}

class SourceInputNodeTemplateBuilder(id: String, description: String) :
    AbstractNodeTemplatePropertyImplBuilder<PropertiesAssignmentBuilder>(
        id,
        "source-input", description
    )

/** Node Template Source Default **/
fun BlueprintTypes.nodeTemplateSourceDefault(
    id: String,
    description: String,
    block: SourceDefaultNodeTemplateBuilder.() -> Unit
): NodeTemplate {
    return SourceDefaultNodeTemplateBuilder(id, description).apply(block).build()
}

class SourceDefaultNodeTemplateBuilder(id: String, description: String) :
    AbstractNodeTemplatePropertyImplBuilder<PropertiesAssignmentBuilder>(
        id,
        "source-default", description
    )

/** Node Template Source DB **/
fun BlueprintTypes.nodeTemplateSourceDb(
    id: String,
    description: String,
    block: SourceDbNodeTemplateBuilder.() -> Unit
): NodeTemplate {
    return SourceDbNodeTemplateBuilder(id, description).apply(block).build()
}

class SourceDbNodeTemplateBuilder(id: String, description: String) :
    AbstractNodeTemplatePropertyImplBuilder<SourceDbNodeTemplateBuilder.PropertiesBuilder>(
        id,
        "source-db", description
    ) {

    class PropertiesBuilder : PropertiesAssignmentBuilder() {

        fun type(type: String) = type(type.asJsonPrimitive())

        fun type(type: JsonNode) {
            property("type", type)
        }

        fun endpointSelector(endpointSelector: String) = endpointSelector(endpointSelector.asJsonPrimitive())

        fun endpointSelector(endpointSelector: JsonNode) {
            property("endpoint-selector", endpointSelector)
        }

        fun query(query: String) = query(query.asJsonPrimitive())

        fun query(query: JsonNode) {
            property("query", query)
        }

        fun inputKeyMapping(block: KeyMappingBuilder.() -> Unit) {
            val map = KeyMappingBuilder().apply(block).build()
            property("input-key-mapping", map.asJsonType())
        }

        fun outputKeyMapping(block: KeyMappingBuilder.() -> Unit) {
            val map = KeyMappingBuilder().apply(block).build()
            property("output-key-mapping", map.asJsonType())
        }

        fun keyDependencies(keyDependencies: String) = keyDependencies(keyDependencies.asJsonType())

        fun keyDependencies(keyDependencies: List<String>) = keyDependencies(keyDependencies.asJsonString())

        fun keyDependencies(keyDependencies: JsonNode) {
            property("key-dependencies", keyDependencies)
        }
    }
}

class KeyMappingBuilder() {

    val map: MutableMap<String, String> = hashMapOf()
    fun map(key: String, value: String) {
        map[key] = value
    }

    fun build(): MutableMap<String, String> {
        return map
    }
}

/** Node Template Source Rest **/
fun BlueprintTypes.nodeTemplateSourceRest(
    id: String,
    description: String,
    block: SourceRestNodeTemplateBuilder.() -> Unit
): NodeTemplate {
    return SourceRestNodeTemplateBuilder(id, description).apply(block).build()
}

class SourceRestNodeTemplateBuilder(id: String, description: String) :
    AbstractNodeTemplatePropertyImplBuilder<SourceRestNodeTemplateBuilder.PropertiesBuilder>(
        id,
        "source-rest", description
    ) {

    class PropertiesBuilder : PropertiesAssignmentBuilder() {

        fun type(type: String) = type(type.asJsonPrimitive())

        fun type(type: JsonNode) {
            property("type", type)
        }

        fun endpointSelector(endpointSelector: String) = endpointSelector(endpointSelector.asJsonPrimitive())

        fun endpointSelector(endpointSelector: JsonNode) {
            property("endpoint-selector", endpointSelector)
        }

        fun verb(verb: String) = verb(verb.asJsonPrimitive())

        fun verb(verb: JsonNode) {
            property("verb", verb)
        }

        fun payload(payload: String) = payload(payload.asJsonPrimitive())

        fun payload(payload: JsonNode) {
            property("payload", payload)
        }

        fun urlPath(urlPath: String) = urlPath(urlPath.asJsonPrimitive())

        fun urlPath(urlPath: JsonNode) {
            property("url-path", urlPath)
        }

        fun path(path: String) = path(path.asJsonPrimitive())

        fun path(path: JsonNode) {
            property("path", path)
        }

        fun expressionType(expressionType: String) = expressionType(expressionType.asJsonPrimitive())

        fun expressionType(expressionType: JsonNode) {
            property("expression-type", expressionType)
        }

        fun inputKeyMapping(block: KeyMappingBuilder.() -> Unit) {
            val map = KeyMappingBuilder().apply(block).build()
            property("input-key-mapping", map.asJsonType())
        }

        fun outputKeyMapping(block: KeyMappingBuilder.() -> Unit) {
            val map = KeyMappingBuilder().apply(block).build()
            property("output-key-mapping", map.asJsonType())
        }

        fun keyDependencies(keyDependencies: String) = keyDependencies(keyDependencies.asJsonType())

        fun keyDependencies(keyDependencies: List<String>) = keyDependencies(keyDependencies.asJsonString())

        fun keyDependencies(keyDependencies: JsonNode) {
            property("key-dependencies", keyDependencies)
        }
    }
}

/** Node Template Source Rest **/
fun BlueprintTypes.nodeTemplateSourceCapability(
    id: String,
    description: String,
    block: SourceCapabilityNodeTemplateBuilder.() -> Unit
): NodeTemplate {
    return SourceCapabilityNodeTemplateBuilder(id, description).apply(block).build()
}

class SourceCapabilityNodeTemplateBuilder(id: String, description: String) :
    AbstractNodeTemplatePropertyImplBuilder<SourceCapabilityNodeTemplateBuilder.PropertiesBuilder>(
        id,
        "source-capability", description
    ) {

    class PropertiesBuilder : PropertiesAssignmentBuilder() {

        fun type(type: String) = type(type.asJsonPrimitive())

        fun type(type: JsonNode) {
            property(ComponentScriptExecutor.INPUT_SCRIPT_TYPE, type)
        }

        fun scriptClassReference(scriptClassReference: KClass<*>) {
            scriptClassReference(scriptClassReference.qualifiedName!!)
        }

        fun scriptClassReference(scriptClassReference: String) = scriptClassReference(scriptClassReference.asJsonPrimitive())

        fun scriptClassReference(scriptClassReference: JsonNode) {
            property(ComponentScriptExecutor.INPUT_SCRIPT_CLASS_REFERENCE, scriptClassReference)
        }

        fun keyDependencies(keyDependencies: String) = keyDependencies(keyDependencies.asJsonType())

        fun keyDependencies(keyDependencies: List<String>) = keyDependencies(keyDependencies.asJsonString())

        fun keyDependencies(keyDependencies: JsonNode) {
            property("key-dependencies", keyDependencies)
        }
    }
}

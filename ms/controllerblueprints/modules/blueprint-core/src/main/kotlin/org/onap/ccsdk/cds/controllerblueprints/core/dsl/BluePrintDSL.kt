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

package org.onap.ccsdk.cds.controllerblueprints.core.dsl

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType

fun serviceTemplate(name: String, version: String, author: String, tags: String,
                    block: ServiceTemplateBuilder.() -> Unit): ServiceTemplate {
    return ServiceTemplateBuilder(name, version, author, tags).apply(block).build()
}

// Input Function

fun getInput(inputKey: String): JsonNode {
    return """{"get_input": "$inputKey"}""".jsonAsJsonType()
}

fun getAttribute(attributeName: String): JsonNode {
    return """{"get_attribute": ["SELF", "$attributeName"]}""".jsonAsJsonType()
}

fun getAttribute(attributeName: String, jsonPath: String): JsonNode {
    return """{"get_attribute": ["SELF", "$attributeName", "$jsonPath"]}""".jsonAsJsonType()
}

fun getNodeTemplateAttribute(nodeTemplateName: String, attributeName: String): JsonNode {
    return """{"get_attribute": ["${nodeTemplateName}", "$attributeName"]}""".jsonAsJsonType()
}

fun getNodeTemplateAttribute(nodeTemplateName: String, attributeName: String, jsonPath: String): JsonNode {
    return """{"get_attribute": ["${nodeTemplateName}", "$attributeName", "$jsonPath]}""".jsonAsJsonType()
}

// Property Function

fun getProperty(propertyName: String): JsonNode {
    return """{"get_property": ["SELF", "$propertyName"]}""".jsonAsJsonType()
}

fun getProperty(propertyName: String, jsonPath: String): JsonNode {
    return """{"get_property": ["SELF", "$propertyName", "$jsonPath"]}""".jsonAsJsonType()
}

fun getNodeTemplateProperty(nodeTemplateName: String, propertyName: String): JsonNode {
    return """{"get_property": ["${nodeTemplateName}", "$propertyName"]}""".jsonAsJsonType()
}

fun getNodeTemplateProperty(nodeTemplateName: String, propertyName: String, jsonPath: String): JsonNode {
    return """{"get_property": ["${nodeTemplateName}", "$propertyName", "$jsonPath]}""".jsonAsJsonType()
}

// Artifact Function

fun getArtifact(artifactName: String): JsonNode {
    return """{"get_artifact": ["SELF", "$artifactName"]}""".jsonAsJsonType()
}

fun getNodeTemplateArtifact(nodeTemplateName: String, artifactName: String): JsonNode {
    return """{"get_artifact": ["$nodeTemplateName", "$artifactName"]}""".jsonAsJsonType()
}
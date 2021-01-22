/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 - 2019 IBM, Bell Canada.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("unused")

package org.onap.ccsdk.cds.controllerblueprints.core.data

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

/**
 *
 *
 * @author Brinda Santh
 */
data class ExpressionData(
    var isExpression: Boolean = false,
    var valueNode: JsonNode,
    var expressionNode: ObjectNode? = null,
    var dslExpression: DSLExpression? = null,
    var inputExpression: InputExpression? = null,
    var propertyExpression: PropertyExpression? = null,
    var attributeExpression: AttributeExpression? = null,
    var artifactExpression: ArtifactExpression? = null,
    var operationOutputExpression: OperationOutputExpression? = null,
    var command: String? = null
)

data class InputExpression(
    var propertyName: String
)

data class PropertyExpression(
    var modelableEntityName: String = "SELF",
    var reqOrCapEntityName: String? = null,
    var propertyName: String,
    var subPropertyName: String? = null
)

data class AttributeExpression(
    var modelableEntityName: String = "SELF",
    var reqOrCapEntityName: String? = null,
    var attributeName: String,
    var subAttributeName: String? = null
)

data class ArtifactExpression(
    val modelableEntityName: String = "SELF",
    val artifactName: String,
    val location: String? = "LOCAL_FILE",
    val remove: Boolean? = false
)

data class OperationOutputExpression(
    val modelableEntityName: String = "SELF",
    val interfaceName: String,
    val operationName: String,
    val propertyName: String,
    var subPropertyName: String? = null
)

data class DSLExpression(
    val propertyName: String
)

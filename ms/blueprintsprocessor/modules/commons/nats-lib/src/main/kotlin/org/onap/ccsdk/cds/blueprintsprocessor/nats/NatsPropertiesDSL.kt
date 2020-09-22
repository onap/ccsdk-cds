/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.nats

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertiesAssignmentBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.RelationshipTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.ServiceTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.TopologyTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipType

/** Relationships Types DSL for NATS Producer */
fun ServiceTemplateBuilder.relationshipTypeConnectsToNats() {
    val relationshipType = BluePrintTypes.relationshipTypeConnectsToNats()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BluePrintTypes.relationshipTypeConnectsToNats(): RelationshipType {
    return relationshipType(
        id = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_NATS,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        description = "Relationship connects to through NATS Client."
    ) {
        property(
            BluePrintConstants.PROPERTY_CONNECTION_CONFIG,
            BluePrintConstants.DATA_TYPE_MAP,
            true,
            "Connection Config details."
        )
        validTargetTypes(arrayListOf(BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT))
    }
}

/** Relationships Templates for Nats */
fun TopologyTemplateBuilder.relationshipTemplateNats(
    name: String,
    description: String,
    block: NatsRelationshipTemplateBuilder.() -> Unit
) {
    if (relationshipTemplates == null) relationshipTemplates = hashMapOf()
    val relationshipTemplate = NatsRelationshipTemplateBuilder(name, description).apply(block).build()
    relationshipTemplates!![relationshipTemplate.id!!] = relationshipTemplate
}

class NatsRelationshipTemplateBuilder(name: String, description: String) :
    RelationshipTemplateBuilder(
        name,
        BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_NATS, description
    ) {

    fun tokenAuth(block: NatsTokenAuthPropertiesAssignmentBuilder.() -> Unit) {
        property(BluePrintConstants.PROPERTY_CONNECTION_CONFIG, BluePrintTypes.tokenAuthNatsProperties(block))
    }

    fun tlsAuth(block: NatsTLSAuthPropertiesAssignmentBuilder.() -> Unit) {
        property(BluePrintConstants.PROPERTY_CONNECTION_CONFIG, BluePrintTypes.tlsAuthNatsProperties(block))
    }
}

fun BluePrintTypes.tokenAuthNatsProperties(block: NatsTokenAuthPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = NatsTokenAuthPropertiesAssignmentBuilder().apply(block).build()
    assignments[NatsConnectionProperties::type.name] = NatsLibConstants.TYPE_TOKEN_AUTH.asJsonPrimitive()
    return assignments.asJsonNode()
}

fun BluePrintTypes.tlsAuthNatsProperties(block: NatsTLSAuthPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = NatsTLSAuthPropertiesAssignmentBuilder().apply(block).build()
    assignments[NatsConnectionProperties::type.name] = NatsLibConstants.TYPE_TLS_AUTH.asJsonPrimitive()
    return assignments.asJsonNode()
}

open class NatsConnectionPropertiesAssignmentBuilder : PropertiesAssignmentBuilder() {

    fun clusterId(clusterId: String) = clusterId(clusterId.asJsonPrimitive())

    fun clusterId(clusterId: JsonNode) = property(NatsConnectionProperties::clusterId, clusterId)

    fun clientId(clientId: String) = clientId(clientId.asJsonPrimitive())

    fun clientId(clientId: JsonNode) = property(NatsConnectionProperties::clientId, clientId)

    fun host(host: String) = host(host.asJsonPrimitive())

    fun host(host: JsonNode) = property(NatsConnectionProperties::host, host)

    fun monitoringSelector(monitoringSelector: String) = monitoringSelector(monitoringSelector.asJsonPrimitive())

    fun monitoringSelector(monitoringSelector: JsonNode) =
        property(NatsConnectionProperties::monitoringSelector, monitoringSelector)
}

class NatsTokenAuthPropertiesAssignmentBuilder : NatsConnectionPropertiesAssignmentBuilder() {

    fun token(selector: String) = token(selector.asJsonPrimitive())

    fun token(selector: JsonNode) = property(TokenAuthNatsConnectionProperties::token, selector)
}

class NatsTLSAuthPropertiesAssignmentBuilder : NatsConnectionPropertiesAssignmentBuilder()

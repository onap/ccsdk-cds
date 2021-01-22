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

package org.onap.ccsdk.cds.blueprintsprocessor.rest

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertiesAssignmentBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.RelationshipTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.ServiceTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.TopologyTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipType

/** Relationships Type DSL for Rest */
fun ServiceTemplateBuilder.relationshipTypeConnectsToRestClient() {
    val relationshipType = BlueprintTypes.relationshipTypeConnectsToRestClient()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BlueprintTypes.relationshipTypeConnectsToRestClient(): RelationshipType {
    return relationshipType(
        id = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_REST_CLIENT,
        version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        description = "Relationship connects to through"
    ) {
        property(
            BlueprintConstants.PROPERTY_CONNECTION_CONFIG,
            BlueprintConstants.DATA_TYPE_MAP,
            true,
            "Connection Config details."
        )
        validTargetTypes(arrayListOf(BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT))
    }
}

/** Relationships Templates DSL for Rest */
fun TopologyTemplateBuilder.relationshipTemplateRestClient(
    name: String,
    description: String,
    block: RestClientRelationshipTemplateBuilder.() -> Unit
) {
    if (relationshipTemplates == null) relationshipTemplates = hashMapOf()
    val relationshipTemplate = RestClientRelationshipTemplateBuilder(name, description).apply(block).build()
    relationshipTemplates!![relationshipTemplate.id!!] = relationshipTemplate
}

open class RestClientRelationshipTemplateBuilder(name: String, description: String) :
    RelationshipTemplateBuilder(
        name,
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_REST_CLIENT, description
    ) {

    fun basicAuth(block: BasicAuthRestClientPropertiesAssignmentBuilder.() -> Unit) {
        property(BlueprintConstants.PROPERTY_CONNECTION_CONFIG, BlueprintTypes.basicAuthRestClientProperties(block))
    }

    fun tokenAuth(block: TokenAuthRestClientPropertiesAssignmentBuilder.() -> Unit) {
        property(BlueprintConstants.PROPERTY_CONNECTION_CONFIG, BlueprintTypes.tokenAuthRestClientProperties(block))
    }

    fun sslAuth(block: SslAuthRestClientPropertiesAssignmentBuilder.() -> Unit) {
        property(BlueprintConstants.PROPERTY_CONNECTION_CONFIG, BlueprintTypes.sslRestClientProperties(block))
    }
}

fun BlueprintTypes.basicAuthRestClientProperties(block: BasicAuthRestClientPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = BasicAuthRestClientPropertiesAssignmentBuilder().apply(block).build()
    assignments[RestClientProperties::type.name] = RestLibConstants.TYPE_BASIC_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

fun BlueprintTypes.tokenAuthRestClientProperties(block: TokenAuthRestClientPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = TokenAuthRestClientPropertiesAssignmentBuilder().apply(block).build()
    assignments[RestClientProperties::type.name] = RestLibConstants.TYPE_TOKEN_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

fun BlueprintTypes.sslRestClientProperties(block: SslAuthRestClientPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = SslAuthRestClientPropertiesAssignmentBuilder().apply(block).build()
    assignments[RestClientProperties::type.name] = RestLibConstants.TYPE_SSL_NO_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

open class RestClientPropertiesAssignmentBuilder : PropertiesAssignmentBuilder() {

    open fun url(url: String) {
        url(url.asJsonPrimitive())
    }

    open fun url(url: JsonNode) {
        property(RestClientProperties::url, url)
    }
}

open class BasicAuthRestClientPropertiesAssignmentBuilder : RestClientPropertiesAssignmentBuilder() {

    open fun password(password: String) {
        password(password.asJsonPrimitive())
    }

    open fun password(password: JsonNode) {
        property(BasicAuthRestClientProperties::password, password)
    }

    open fun username(username: String) {
        username(username.asJsonPrimitive())
    }

    open fun username(username: JsonNode) {
        property(BasicAuthRestClientProperties::username, username)
    }
}

open class TokenAuthRestClientPropertiesAssignmentBuilder : RestClientPropertiesAssignmentBuilder() {

    open fun token(token: String) {
        token(token.asJsonPrimitive())
    }

    open fun token(token: JsonNode) {
        property(TokenAuthRestClientProperties::token, token)
    }
}

open class SslAuthRestClientPropertiesAssignmentBuilder : RestClientPropertiesAssignmentBuilder() {

    open fun keyStoreInstance(keyStoreInstance: String) {
        keyStoreInstance(keyStoreInstance.asJsonPrimitive())
    }

    open fun keyStoreInstance(keyStoreInstance: JsonNode) {
        property(SSLRestClientProperties::keyStoreInstance, keyStoreInstance)
    }

    open fun sslTrust(sslTrust: String) {
        sslTrust(sslTrust.asJsonPrimitive())
    }

    open fun sslTrust(sslTrust: JsonNode) {
        property(SSLRestClientProperties::sslTrust, sslTrust)
    }

    open fun sslTrustPassword(sslTrustPassword: String) {
        sslTrustPassword(sslTrustPassword.asJsonPrimitive())
    }

    open fun sslTrustPassword(sslTrustPassword: JsonNode) {
        property(SSLRestClientProperties::sslTrustPassword, sslTrustPassword)
    }

    open fun sslKey(sslKey: String) {
        sslKey(sslKey.asJsonPrimitive())
    }

    open fun sslKey(sslKey: JsonNode) {
        property(SSLRestClientProperties::sslKey, sslKey)
    }

    open fun sslKeyPassword(sslKeyPassword: String) {
        sslKeyPassword(sslKeyPassword.asJsonPrimitive())
    }

    open fun sslKeyPassword(sslKeyPassword: JsonNode) {
        property(SSLRestClientProperties::sslKeyPassword, sslKeyPassword)
    }
}

open class SSLBasicAuthRestClientPropertiesBuilder : SslAuthRestClientPropertiesAssignmentBuilder() {
    // TODO()
}

open class SSLTokenAuthRestClientPropertiesBuilder : SslAuthRestClientPropertiesAssignmentBuilder() {
    // TODO()
}

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

package org.onap.ccsdk.cds.blueprintsprocessor.grpc

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertiesAssignmentBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.RelationshipTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.ServiceTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.TopologyTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipType

/** Relationships Types DSL for GRPC Server Producer */
fun ServiceTemplateBuilder.relationshipTypeConnectsToGrpcServer() {
    val relationshipType = BlueprintTypes.relationshipTypeConnectsToGrpcServer()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BlueprintTypes.relationshipTypeConnectsToGrpcServer(): RelationshipType {
    return relationshipType(
        id = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_GRPC_SERVER,
        version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        description = "Relationship connects to through GRPC Server."
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

fun ServiceTemplateBuilder.relationshipTypeConnectsToGrpcClient() {
    val relationshipType = BlueprintTypes.relationshipTypeConnectsToGrpcClient()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BlueprintTypes.relationshipTypeConnectsToGrpcClient(): RelationshipType {
    return relationshipType(
        id = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_GRPC_CLIENT,
        version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        description = "Relationship connects to through GRPC Client."
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

/** Relationships Templates for GRPC Server */
fun TopologyTemplateBuilder.relationshipTemplateGrpcServer(
    name: String,
    description: String,
    block: GrpcServerRelationshipTemplateBuilder.() -> Unit
) {
    if (relationshipTemplates == null) relationshipTemplates = hashMapOf()
    val relationshipTemplate = GrpcServerRelationshipTemplateBuilder(name, description).apply(block).build()
    relationshipTemplates!![relationshipTemplate.id!!] = relationshipTemplate
}

class GrpcServerRelationshipTemplateBuilder(name: String, description: String) :
    RelationshipTemplateBuilder(
        name,
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_GRPC_SERVER, description
    ) {

    fun tokenAuth(block: GrpcServerTokenAuthPropertiesAssignmentBuilder.() -> Unit) {
        property(BlueprintConstants.PROPERTY_CONNECTION_CONFIG, BlueprintTypes.tokenAuthGrpcServerProperties(block))
    }

    fun tlsAuth(block: GrpcServerTLSAuthPropertiesAssignmentBuilder.() -> Unit) {
        property(BlueprintConstants.PROPERTY_CONNECTION_CONFIG, BlueprintTypes.tlsAuthGrpcServerProperties(block))
    }
}

fun BlueprintTypes.tokenAuthGrpcServerProperties(block: GrpcServerTokenAuthPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = GrpcServerTokenAuthPropertiesAssignmentBuilder().apply(block).build()
    assignments[GrpcServerProperties::type.name] = GRPCLibConstants.TYPE_TOKEN_AUTH.asJsonPrimitive()
    return assignments.asJsonNode()
}

fun BlueprintTypes.tlsAuthGrpcServerProperties(block: GrpcServerTLSAuthPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = GrpcServerTLSAuthPropertiesAssignmentBuilder().apply(block).build()
    assignments[GrpcServerProperties::type.name] = GRPCLibConstants.TYPE_TLS_AUTH.asJsonPrimitive()
    return assignments.asJsonNode()
}

open class GrpcServerPropertiesAssignmentBuilder : PropertiesAssignmentBuilder() {

    fun port(port: Int) = port(port.asJsonPrimitive())

    fun port(port: JsonNode) =
        property(GrpcServerProperties::port, port)
}

open class GrpcServerTokenAuthPropertiesAssignmentBuilder : GrpcServerPropertiesAssignmentBuilder() {

    fun token(selector: String) = token(selector.asJsonPrimitive())

    fun token(selector: JsonNode) = property(TokenAuthGrpcServerProperties::token, selector)
}

open class GrpcServerTLSAuthPropertiesAssignmentBuilder : GrpcServerPropertiesAssignmentBuilder() {

    fun certChain(certChain: String) = certChain(certChain.asJsonPrimitive())

    fun certChain(certChain: JsonNode) = property(TLSAuthGrpcServerProperties::certChain, certChain)

    fun privateKey(privateKey: String) = privateKey(privateKey.asJsonPrimitive())

    fun privateKey(privateKey: JsonNode) = property(TLSAuthGrpcServerProperties::privateKey, privateKey)

    fun trustCertCollection(trustCertCollection: String) = trustCertCollection(trustCertCollection.asJsonPrimitive())

    fun trustCertCollection(trustCertCollection: JsonNode) =
        property(TLSAuthGrpcServerProperties::trustCertCollection, trustCertCollection)
}

/** Relationships Templates for GRPC Client */
fun TopologyTemplateBuilder.relationshipTemplateGrpcClient(
    name: String,
    description: String,
    block: GrpcClientRelationshipTemplateBuilder.() -> Unit
) {
    if (relationshipTemplates == null) relationshipTemplates = hashMapOf()
    val relationshipTemplate = GrpcClientRelationshipTemplateBuilder(name, description).apply(block).build()
    relationshipTemplates!![relationshipTemplate.id!!] = relationshipTemplate
}

class GrpcClientRelationshipTemplateBuilder(name: String, description: String) :
    RelationshipTemplateBuilder(
        name,
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_GRPC_CLIENT, description
    ) {

    fun basicAuth(block: GrpcClientBasicAuthPropertiesAssignmentBuilder.() -> Unit) {
        property(BlueprintConstants.PROPERTY_CONNECTION_CONFIG, BlueprintTypes.basicAuthGrpcClientProperties(block))
    }

    fun tokenAuth(block: GrpcClientTokenAuthPropertiesAssignmentBuilder.() -> Unit) {
        property(BlueprintConstants.PROPERTY_CONNECTION_CONFIG, BlueprintTypes.tokenAuthGrpcClientProperties(block))
    }

    fun tlsAuth(block: GrpcClientTLSAuthPropertiesAssignmentBuilder.() -> Unit) {
        property(BlueprintConstants.PROPERTY_CONNECTION_CONFIG, BlueprintTypes.tlsAuthGrpcClientProperties(block))
    }
}

fun BlueprintTypes.basicAuthGrpcClientProperties(block: GrpcClientBasicAuthPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = GrpcClientBasicAuthPropertiesAssignmentBuilder().apply(block).build()
    assignments[GrpcClientProperties::type.name] = GRPCLibConstants.TYPE_BASIC_AUTH.asJsonPrimitive()
    return assignments.asJsonNode()
}

fun BlueprintTypes.tokenAuthGrpcClientProperties(block: GrpcClientTokenAuthPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = GrpcClientTokenAuthPropertiesAssignmentBuilder().apply(block).build()
    assignments[GrpcClientProperties::type.name] = GRPCLibConstants.TYPE_TOKEN_AUTH.asJsonPrimitive()
    return assignments.asJsonNode()
}

fun BlueprintTypes.tlsAuthGrpcClientProperties(block: GrpcClientTLSAuthPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = GrpcClientTLSAuthPropertiesAssignmentBuilder().apply(block).build()
    assignments[GrpcClientProperties::type.name] = GRPCLibConstants.TYPE_TLS_AUTH.asJsonPrimitive()
    return assignments.asJsonNode()
}

open class GrpcClientPropertiesAssignmentBuilder : PropertiesAssignmentBuilder() {

    fun host(host: String) = host(host.asJsonPrimitive())

    fun host(host: JsonNode) =
        property(GrpcClientProperties::host, host)

    fun port(port: Int) = port(port.asJsonPrimitive())

    fun port(port: JsonNode) =
        property(GrpcClientProperties::port, port)
}

open class GrpcClientBasicAuthPropertiesAssignmentBuilder : GrpcClientPropertiesAssignmentBuilder() {

    fun username(username: String) = username(username.asJsonPrimitive())

    fun username(username: JsonNode) = property(BasicAuthGrpcClientProperties::username, username)

    fun password(password: String) = password(password.asJsonPrimitive())

    fun password(password: JsonNode) = property(BasicAuthGrpcClientProperties::password, password)
}

open class GrpcClientTokenAuthPropertiesAssignmentBuilder : GrpcClientPropertiesAssignmentBuilder() {

    fun token(selector: String) = token(selector.asJsonPrimitive())

    fun token(selector: JsonNode) = property(TokenAuthGrpcClientProperties::token, selector)
}

open class GrpcClientTLSAuthPropertiesAssignmentBuilder : GrpcClientPropertiesAssignmentBuilder() {

    fun trustCertCollection(trustCertCollection: String) = trustCertCollection(trustCertCollection.asJsonPrimitive())

    fun trustCertCollection(trustCertCollection: JsonNode) =
        property(TLSAuthGrpcClientProperties::trustCertCollection, trustCertCollection)

    fun clientCertChain(clientCertChain: String) = clientCertChain(clientCertChain.asJsonPrimitive())

    fun clientCertChain(clientCertChain: JsonNode) =
        property(TLSAuthGrpcClientProperties::clientCertChain, clientCertChain)

    fun clientPrivateKey(clientPrivateKey: String) = clientPrivateKey(clientPrivateKey.asJsonPrimitive())

    fun clientPrivateKey(clientPrivateKey: JsonNode) =
        property(TLSAuthGrpcClientProperties::clientPrivateKey, clientPrivateKey)
}

/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
 *
 * Modifications Copyright © 2020 Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.ssh

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

/** Relationships Types DSL for Message Producer */
fun ServiceTemplateBuilder.relationshipTypeConnectsToSshClient() {
    val relationshipType = BlueprintTypes.relationshipTypeConnectsToSshClient()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BlueprintTypes.relationshipTypeConnectsToSshClient(): RelationshipType {
    return relationshipType(
        id = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_SSH_CLIENT,
        version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        description = "Relationship connects to through SSH Client."
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

/** Relationships Templates for Ssh */
fun TopologyTemplateBuilder.relationshipTemplateSshClient(
    name: String,
    description: String,
    block: SshRelationshipTemplateBuilder.() -> Unit
) {
    if (relationshipTemplates == null) relationshipTemplates = hashMapOf()
    val relationshipTemplate = SshRelationshipTemplateBuilder(name, description).apply(block).build()
    relationshipTemplates!![relationshipTemplate.id!!] = relationshipTemplate
}

open class SshRelationshipTemplateBuilder(name: String, description: String) :
    RelationshipTemplateBuilder(
        name,
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_SSH_CLIENT, description
    ) {

    fun basicAuth(block: BasicAuthSshClientPropertiesAssignmentBuilder.() -> Unit) {
        property(BlueprintConstants.PROPERTY_CONNECTION_CONFIG, BlueprintTypes.basicAuthSshProperties(block))
    }
}

fun BlueprintTypes.basicAuthSshProperties(block: BasicAuthSshClientPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val sshProperties = BasicAuthSshClientPropertiesAssignmentBuilder().apply(block).build()
    sshProperties[SshClientProperties::type.name] = SshLibConstants.TYPE_BASIC_AUTH.asJsonPrimitive()
    return sshProperties.asJsonType()
}

open class SshClientPropertiesAssignmentBuilder : PropertiesAssignmentBuilder() {

    fun connectionTimeOut(connectionTimeOut: Int) = connectionTimeOut(connectionTimeOut.asJsonPrimitive())

    fun connectionTimeOut(connectionTimeOut: JsonNode) =
        property(SshClientProperties::connectionTimeOut.name, connectionTimeOut)

    fun port(port: Int) = port(port.asJsonPrimitive())

    fun port(port: JsonNode) = property(SshClientProperties::port.name, port)

    fun host(host: String) = host(host.asJsonPrimitive())

    fun host(host: JsonNode) = property(SshClientProperties::host.name, host)

    fun logging(logging: Boolean) = logging(logging.asJsonPrimitive())

    fun logging(logging: JsonNode) = property(SshClientProperties::logging.name, logging)
}

class BasicAuthSshClientPropertiesAssignmentBuilder : SshClientPropertiesAssignmentBuilder() {

    fun username(username: String) = username(username.asJsonPrimitive())

    fun username(username: JsonNode) = property(BasicAuthSshClientProperties::username.name, username)

    fun password(password: String) = password(password.asJsonPrimitive())

    fun password(password: JsonNode) = property(BasicAuthSshClientProperties::password.name, password)
}

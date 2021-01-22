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

package org.onap.ccsdk.cds.blueprintsprocessor.db

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

/** Relationships Types DSL for Database Producer */
fun ServiceTemplateBuilder.relationshipTypeConnectsToDb() {
    val relationshipType = BlueprintTypes.relationshipTypeConnectsToDb()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BlueprintTypes.relationshipTypeConnectsToDb(): RelationshipType {
    return relationshipType(
        id = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_DB,
        version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        description = "Relationship connects to through Database."
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

/** Relationships Templates for Database Server */
fun TopologyTemplateBuilder.relationshipTemplateDb(
    name: String,
    description: String,
    block: DbRelationshipTemplateBuilder.() -> Unit
) {
    if (relationshipTemplates == null) relationshipTemplates = hashMapOf()
    val relationshipTemplate = DbRelationshipTemplateBuilder(name, description).apply(block).build()
    relationshipTemplates!![relationshipTemplate.id!!] = relationshipTemplate
}

class DbRelationshipTemplateBuilder(name: String, description: String) :
    RelationshipTemplateBuilder(
        name,
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_DB, description
    ) {

    fun mariaDb(block: DbMariaDataSourcePropertiesAssignmentBuilder.() -> Unit) {
        property(BlueprintConstants.PROPERTY_CONNECTION_CONFIG, BlueprintTypes.mariaDbProperties(block))
    }

    fun mySqlDb(block: DbMySqlDataSourcePropertiesAssignmentBuilder.() -> Unit) {
        property(BlueprintConstants.PROPERTY_CONNECTION_CONFIG, BlueprintTypes.mySqlDbProperties(block))
    }
}

fun BlueprintTypes.mariaDbProperties(block: DbMariaDataSourcePropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = DbMariaDataSourcePropertiesAssignmentBuilder().apply(block).build()
    assignments[DBDataSourceProperties::type.name] = DBLibConstants.MARIA_DB.asJsonPrimitive()
    return assignments.asJsonNode()
}

fun BlueprintTypes.mySqlDbProperties(block: DbMySqlDataSourcePropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = DbMySqlDataSourcePropertiesAssignmentBuilder().apply(block).build()
    assignments[DBDataSourceProperties::type.name] = DBLibConstants.MYSQL_DB.asJsonPrimitive()
    return assignments.asJsonNode()
}

open class DbPropertiesAssignmentBuilder : PropertiesAssignmentBuilder() {

    fun url(url: String) = url(url.asJsonPrimitive())

    fun url(url: JsonNode) =
        property(DBDataSourceProperties::url, url)

    fun username(username: String) = username(username.asJsonPrimitive())

    fun username(username: JsonNode) = property(DBDataSourceProperties::username, username)

    fun password(password: String) = password(password.asJsonPrimitive())

    fun password(password: JsonNode) = property(DBDataSourceProperties::password, password)
}

open class DbMariaDataSourcePropertiesAssignmentBuilder : DbPropertiesAssignmentBuilder() {

    fun hibernateHbm2ddlAuto(hibernateHbm2ddlAuto: String) =
        hibernateHbm2ddlAuto(hibernateHbm2ddlAuto.asJsonPrimitive())

    fun hibernateHbm2ddlAuto(hibernateHbm2ddlAuto: JsonNode) =
        property(MariaDataSourceProperties::hibernateHbm2ddlAuto, hibernateHbm2ddlAuto)

    fun hibernateDDLAuto(hibernateDDLAuto: String) =
        hibernateDDLAuto(hibernateDDLAuto.asJsonPrimitive())

    fun hibernateDDLAuto(hibernateDDLAuto: JsonNode) =
        property(MariaDataSourceProperties::hibernateDDLAuto, hibernateDDLAuto)

    fun hibernateNamingStrategy(hibernateNamingStrategy: String) =
        hibernateNamingStrategy(hibernateNamingStrategy.asJsonPrimitive())

    fun hibernateNamingStrategy(hibernateNamingStrategy: JsonNode) =
        property(MariaDataSourceProperties::hibernateNamingStrategy, hibernateNamingStrategy)
}

open class DbMySqlDataSourcePropertiesAssignmentBuilder : DbMariaDataSourcePropertiesAssignmentBuilder()

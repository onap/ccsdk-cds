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

package org.onap.ccsdk.cds.blueprintsprocessor.message

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertiesAssignmentBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.RelationshipTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.ServiceTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.TopologyTemplateBuilder
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipType

/** Relationships Types DSL for Message Producer */
fun ServiceTemplateBuilder.relationshipTypeConnectsToMessageProducer() {
    val relationshipType = BluePrintTypes.relationshipTypeConnectsToMessageProducer()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BluePrintTypes.relationshipTypeConnectsToMessageProducer(): RelationshipType {
    return relationshipType(
        id = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_PRODUCER,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        description = "Relationship connects to through message producer."
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

fun ServiceTemplateBuilder.relationshipTypeConnectsToMessageConsumer() {
    val relationshipType = BluePrintTypes.relationshipTypeConnectsToMessageConsumer()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BluePrintTypes.relationshipTypeConnectsToMessageConsumer(): RelationshipType {
    return relationshipType(
        id = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_CONSUMER,
        version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        description = "Relationship type connects to message consumer."
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

/** Relationships Templates DSL for Message Producer */
fun TopologyTemplateBuilder.relationshipTemplateMessageProducer(
    name: String,
    description: String,
    block: MessageProducerRelationshipTemplateBuilder.() -> Unit
) {
    if (relationshipTemplates == null) relationshipTemplates = hashMapOf()
    val relationshipTemplate =
        MessageProducerRelationshipTemplateBuilder(name, description).apply(block).build()
    relationshipTemplates!![relationshipTemplate.id!!] = relationshipTemplate
}

class MessageProducerRelationshipTemplateBuilder(name: String, description: String) :
    RelationshipTemplateBuilder(
        name,
        BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_PRODUCER, description
    ) {

    fun kafkaBasicAuth(block: KafkaBasicAuthMessageProducerPropertiesAssignmentBuilder.() -> Unit) {
        property(
            BluePrintConstants.PROPERTY_CONNECTION_CONFIG,
            BluePrintTypes.kafkaBasicAuthMessageProducerProperties(block)
        )
    }
}

fun BluePrintTypes.kafkaBasicAuthMessageProducerProperties(block: KafkaBasicAuthMessageProducerPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = KafkaBasicAuthMessageProducerPropertiesAssignmentBuilder().apply(block).build()
    assignments[KafkaBasicAuthMessageProducerProperties::type.name] =
        MessageLibConstants.TYPE_KAFKA_BASIC_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

open class MessageProducerPropertiesAssignmentBuilder : PropertiesAssignmentBuilder()

class KafkaBasicAuthMessageProducerPropertiesAssignmentBuilder : MessageProducerPropertiesAssignmentBuilder() {

    fun bootstrapServers(bootstrapServers: String) = bootstrapServers(bootstrapServers.asJsonPrimitive())

    fun bootstrapServers(bootstrapServers: JsonNode) =
        property(KafkaBasicAuthMessageProducerProperties::bootstrapServers, bootstrapServers)

    fun topic(topic: String) = topic(topic.asJsonPrimitive())

    fun topic(topic: JsonNode) =
        property(KafkaBasicAuthMessageProducerProperties::topic, topic)

    fun clientId(clientId: String) = bootstrapServers(clientId.asJsonPrimitive())

    fun clientId(clientId: JsonNode) =
        property(KafkaBasicAuthMessageProducerProperties::clientId, clientId)

    fun acks(acks: String) = acks(acks.asJsonPrimitive())

    fun acks(acks: JsonNode) = property(KafkaBasicAuthMessageProducerProperties::acks, acks)

    fun retries(retries: Int) = retries(retries.asJsonPrimitive())

    fun retries(retries: JsonNode) = property(KafkaBasicAuthMessageProducerProperties::retries, retries)

    fun enableIdempotence(enableIdempotence: Boolean) = enableIdempotence(enableIdempotence.asJsonPrimitive())

    fun enableIdempotence(enableIdempotence: JsonNode) =
        property(KafkaBasicAuthMessageProducerProperties::enableIdempotence, enableIdempotence)
}

/** Relationships Templates DSL for Message Consumer */
fun TopologyTemplateBuilder.relationshipTemplateMessageConsumer(
    name: String,
    description: String,
    block: MessageConsumerRelationshipTemplateBuilder.() -> Unit
) {
    if (relationshipTemplates == null) relationshipTemplates = hashMapOf()
    val relationshipTemplate =
        MessageConsumerRelationshipTemplateBuilder(name, description).apply(block).build()
    relationshipTemplates!![relationshipTemplate.id!!] = relationshipTemplate
}

class MessageConsumerRelationshipTemplateBuilder(name: String, description: String) :
    RelationshipTemplateBuilder(
        name,
        BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_CONSUMER, description
    ) {

    fun kafkaBasicAuth(block: KafkaBasicAuthMessageConsumerPropertiesAssignmentBuilder.() -> Unit) {
        property(
            BluePrintConstants.PROPERTY_CONNECTION_CONFIG,
            BluePrintTypes.kafkaBasicAuthMessageConsumerProperties(block)
        )
    }

    fun kafkaStreamsBasicAuth(block: KafkaStreamsBasicAuthConsumerPropertiesAssignmentBuilder.() -> Unit) {
        property(
            BluePrintConstants.PROPERTY_CONNECTION_CONFIG,
            BluePrintTypes.kafkaStreamsBasicAuthConsumerProperties(block)
        )
    }
}

fun BluePrintTypes.kafkaBasicAuthMessageConsumerProperties(block: KafkaBasicAuthMessageConsumerPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = KafkaBasicAuthMessageConsumerPropertiesAssignmentBuilder().apply(block).build()
    assignments[KafkaBasicAuthMessageConsumerProperties::type.name] =
        MessageLibConstants.TYPE_KAFKA_BASIC_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

fun BluePrintTypes.kafkaStreamsBasicAuthConsumerProperties(block: KafkaStreamsBasicAuthConsumerPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = KafkaStreamsBasicAuthConsumerPropertiesAssignmentBuilder().apply(block).build()
    assignments[KafkaStreamsBasicAuthConsumerProperties::type.name] =
        MessageLibConstants.TYPE_KAFKA_STREAMS_BASIC_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

open class MessageConsumerPropertiesAssignmentBuilder : PropertiesAssignmentBuilder()

open class KafkaMessageConsumerPropertiesAssignmentBuilder : MessageConsumerPropertiesAssignmentBuilder() {

    fun bootstrapServers(bootstrapServers: String) = bootstrapServers(bootstrapServers.asJsonPrimitive())

    fun bootstrapServers(bootstrapServers: JsonNode) =
        property(KafkaMessageConsumerProperties::bootstrapServers, bootstrapServers)

    fun groupId(groupId: String) = groupId(groupId.asJsonPrimitive())

    fun groupId(groupId: JsonNode) =
        property(KafkaMessageConsumerProperties::groupId, groupId)

    fun clientId(clientId: String) = clientId(clientId.asJsonPrimitive())

    fun clientId(clientId: JsonNode) =
        property(KafkaMessageConsumerProperties::clientId, clientId)

    fun topic(topic: String) = topic(topic.asJsonPrimitive())

    fun topic(topic: JsonNode) =
        property(KafkaMessageConsumerProperties::topic, topic)

    fun autoCommit(autoCommit: Boolean) = autoCommit(autoCommit.asJsonPrimitive())

    fun autoCommit(autoCommit: JsonNode) =
        property(KafkaMessageConsumerProperties::autoCommit, autoCommit)

    fun autoOffsetReset(autoOffsetReset: String) = autoOffsetReset(autoOffsetReset.asJsonPrimitive())

    fun autoOffsetReset(autoOffsetReset: JsonNode) =
        property(KafkaMessageConsumerProperties::autoOffsetReset, autoOffsetReset)

    fun pollMillSec(pollMillSec: Int) = pollMillSec(pollMillSec.asJsonPrimitive())

    fun pollMillSec(pollMillSec: JsonNode) =
        property(KafkaMessageConsumerProperties::pollMillSec, pollMillSec)

    fun pollRecords(pollRecords: Int) = pollRecords(pollRecords.asJsonPrimitive())

    fun pollRecords(pollRecords: JsonNode) =
        property(KafkaMessageConsumerProperties::pollRecords, pollRecords)
}

/** KafkaBasicAuthMessageConsumerProperties assignment builder */
class KafkaBasicAuthMessageConsumerPropertiesAssignmentBuilder : KafkaMessageConsumerPropertiesAssignmentBuilder()

/** KafkaStreamsConsumerProperties assignment builder */
open class KafkaStreamsConsumerPropertiesAssignmentBuilder : MessageConsumerPropertiesAssignmentBuilder() {

    fun bootstrapServers(bootstrapServers: String) = bootstrapServers(bootstrapServers.asJsonPrimitive())

    fun bootstrapServers(bootstrapServers: JsonNode) =
        property(KafkaStreamsConsumerProperties::bootstrapServers, bootstrapServers)

    fun applicationId(applicationId: String) = bootstrapServers(applicationId.asJsonPrimitive())

    fun applicationId(applicationId: JsonNode) =
        property(KafkaStreamsConsumerProperties::applicationId, applicationId)

    fun topic(topic: String) = topic(topic.asJsonPrimitive())

    fun topic(topic: JsonNode) =
        property(KafkaStreamsConsumerProperties::topic, topic)

    fun autoOffsetReset(autoOffsetReset: String) = autoOffsetReset(autoOffsetReset.asJsonPrimitive())

    fun autoOffsetReset(autoOffsetReset: JsonNode) =
        property(KafkaStreamsConsumerProperties::autoOffsetReset, autoOffsetReset)

    fun processingGuarantee(processingGuarantee: String) = processingGuarantee(processingGuarantee.asJsonPrimitive())

    fun processingGuarantee(processingGuarantee: JsonNode) =
        property(KafkaStreamsConsumerProperties::processingGuarantee, processingGuarantee)
}

class KafkaStreamsBasicAuthConsumerPropertiesAssignmentBuilder : KafkaStreamsConsumerPropertiesAssignmentBuilder()

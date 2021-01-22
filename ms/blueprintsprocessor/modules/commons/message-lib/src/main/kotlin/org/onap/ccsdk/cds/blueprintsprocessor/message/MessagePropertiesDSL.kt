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
fun ServiceTemplateBuilder.relationshipTypeConnectsToMessageProducer() {
    val relationshipType = BlueprintTypes.relationshipTypeConnectsToMessageProducer()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BlueprintTypes.relationshipTypeConnectsToMessageProducer(): RelationshipType {
    return relationshipType(
        id = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_PRODUCER,
        version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        description = "Relationship connects to through message producer."
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

fun ServiceTemplateBuilder.relationshipTypeConnectsToMessageConsumer() {
    val relationshipType = BlueprintTypes.relationshipTypeConnectsToMessageConsumer()
    if (this.relationshipTypes == null) this.relationshipTypes = hashMapOf()
    this.relationshipTypes!![relationshipType.id!!] = relationshipType
}

fun BlueprintTypes.relationshipTypeConnectsToMessageConsumer(): RelationshipType {
    return relationshipType(
        id = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_CONSUMER,
        version = BlueprintConstants.DEFAULT_VERSION_NUMBER,
        derivedFrom = BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        description = "Relationship type connects to message consumer."
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
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_PRODUCER, description
    ) {

    fun kafkaBasicAuth(block: KafkaBasicAuthMessageProducerPropertiesAssignmentBuilder.() -> Unit) {
        property(
            BlueprintConstants.PROPERTY_CONNECTION_CONFIG,
            BlueprintTypes.kafkaBasicAuthMessageProducerProperties(block)
        )
    }

    fun kafkaSslAuth(block: KafkaSslAuthMessageProducerPropertiesAssignmentBuilder.() -> Unit) {
        property(
            BlueprintConstants.PROPERTY_CONNECTION_CONFIG,
            BlueprintTypes.kafkaSslAuthMessageProducerProperties(block)
        )
    }

    fun kafkaScramSslAuth(block: KafkaScramSslAuthMessageProducerPropertiesAssignmentBuilder.() -> Unit) {
        property(
            BlueprintConstants.PROPERTY_CONNECTION_CONFIG,
            BlueprintTypes.kafkaScramSslAuthMessageProducerProperties(block)
        )
    }
}

fun BlueprintTypes.kafkaBasicAuthMessageProducerProperties(block: KafkaBasicAuthMessageProducerPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = KafkaBasicAuthMessageProducerPropertiesAssignmentBuilder().apply(block).build()
    assignments[KafkaBasicAuthMessageProducerProperties::type.name] =
        MessageLibConstants.TYPE_KAFKA_BASIC_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

fun BlueprintTypes.kafkaSslAuthMessageProducerProperties(block: KafkaSslAuthMessageProducerPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = KafkaSslAuthMessageProducerPropertiesAssignmentBuilder().apply(block).build()
    assignments[KafkaSslAuthMessageProducerProperties::type.name] =
        MessageLibConstants.TYPE_KAFKA_SSL_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

fun BlueprintTypes.kafkaScramSslAuthMessageProducerProperties(block: KafkaScramSslAuthMessageProducerPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = KafkaScramSslAuthMessageProducerPropertiesAssignmentBuilder().apply(block).build()
    assignments[KafkaScramSslAuthMessageProducerProperties::type.name] =
        MessageLibConstants.TYPE_KAFKA_SCRAM_SSL_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

open class MessageProducerPropertiesAssignmentBuilder : PropertiesAssignmentBuilder()

open class KafkaBasicAuthMessageProducerPropertiesAssignmentBuilder : MessageProducerPropertiesAssignmentBuilder() {

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

    fun maxBlockMs(maxBlockMs: Int) = maxBlockMs(maxBlockMs.asJsonPrimitive())

    fun maxBlockMs(maxBlockMs: JsonNode) = property(KafkaBasicAuthMessageProducerProperties::maxBlockMs, maxBlockMs)

    fun reconnectBackOffMs(reconnectBackOffMs: Int) = reconnectBackOffMs(reconnectBackOffMs.asJsonPrimitive())

    fun reconnectBackOffMs(reconnectBackOffMs: JsonNode) = property(KafkaBasicAuthMessageProducerProperties::reconnectBackOffMs, reconnectBackOffMs)

    fun enableIdempotence(enableIdempotence: Boolean) = enableIdempotence(enableIdempotence.asJsonPrimitive())

    fun enableIdempotence(enableIdempotence: JsonNode) =
        property(KafkaBasicAuthMessageProducerProperties::enableIdempotence, enableIdempotence)
}

open class KafkaSslAuthMessageProducerPropertiesAssignmentBuilder : KafkaBasicAuthMessageProducerPropertiesAssignmentBuilder() {

    fun truststore(truststore: String) = truststore(truststore.asJsonPrimitive())

    fun truststore(truststore: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::truststore, truststore)

    fun truststorePassword(truststorePassword: String) = truststorePassword(truststorePassword.asJsonPrimitive())

    fun truststorePassword(truststorePassword: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::truststorePassword, truststorePassword)

    fun truststoreType(truststoreType: String) = truststoreType(truststoreType.asJsonPrimitive())

    fun truststoreType(truststoreType: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::truststoreType, truststoreType)

    fun keystore(keystore: String) = keystore(keystore.asJsonPrimitive())

    fun keystore(keystore: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::keystore, keystore)

    fun keystorePassword(keystorePassword: String) = keystorePassword(keystorePassword.asJsonPrimitive())

    fun keystorePassword(keystorePassword: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::keystorePassword, keystorePassword)

    fun keystoreType(keystoreType: String) = keystoreType(keystoreType.asJsonPrimitive())

    fun keystoreType(keystoreType: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::keystoreType, keystoreType)

    fun sslEndpointIdentificationAlgorithm(sslEndpointIdentificationAlgorithm: String) =
        sslEndpointIdentificationAlgorithm(sslEndpointIdentificationAlgorithm.asJsonPrimitive())

    fun sslEndpointIdentificationAlgorithm(sslEndpointIdentificationAlgorithm: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::sslEndpointIdentificationAlgorithm, sslEndpointIdentificationAlgorithm)
}

class KafkaScramSslAuthMessageProducerPropertiesAssignmentBuilder : KafkaSslAuthMessageProducerPropertiesAssignmentBuilder() {

    fun saslMechanism(saslMechanism: String) = saslMechanism(saslMechanism.asJsonPrimitive())

    fun saslMechanism(saslMechanism: JsonNode) =
        property(KafkaScramSslAuthMessageProducerProperties::saslMechanism, saslMechanism)

    fun scramUsername(scramUsername: String) = scramUsername(scramUsername.asJsonPrimitive())

    fun scramUsername(scramUsername: JsonNode) =
        property(KafkaScramSslAuthMessageProducerProperties::scramUsername, scramUsername)

    fun scramPassword(scramPassword: String) = scramPassword(scramPassword.asJsonPrimitive())

    fun scramPassword(scramPassword: JsonNode) =
        property(KafkaScramSslAuthMessageProducerProperties::scramPassword, scramPassword)
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
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_CONSUMER, description
    ) {

    fun kafkaBasicAuth(block: KafkaBasicAuthMessageConsumerPropertiesAssignmentBuilder.() -> Unit) {
        property(
            BlueprintConstants.PROPERTY_CONNECTION_CONFIG,
            BlueprintTypes.kafkaBasicAuthMessageConsumerProperties(block)
        )
    }

    fun kafkaSslAuth(block: KafkaSslAuthMessageConsumerPropertiesAssignmentBuilder.() -> Unit) {
        property(
            BlueprintConstants.PROPERTY_CONNECTION_CONFIG,
            BlueprintTypes.kafkaSslAuthMessageConsumerProperties(block)
        )
    }

    fun kafkaScramSslAuth(block: KafkaScramSslAuthMessageConsumerPropertiesAssignmentBuilder.() -> Unit) {
        property(
            BlueprintConstants.PROPERTY_CONNECTION_CONFIG,
            BlueprintTypes.kafkaScramSslAuthMessageConsumerProperties(block)
        )
    }

    fun kafkaStreamsBasicAuth(block: KafkaStreamsBasicAuthConsumerPropertiesAssignmentBuilder.() -> Unit) {
        property(
            BlueprintConstants.PROPERTY_CONNECTION_CONFIG,
            BlueprintTypes.kafkaStreamsBasicAuthConsumerProperties(block)
        )
    }

    fun kafkaStreamsSslAuth(block: KafkaStreamsSslAuthConsumerPropertiesAssignmentBuilder.() -> Unit) {
        property(
            BlueprintConstants.PROPERTY_CONNECTION_CONFIG,
            BlueprintTypes.kafkaStreamsSslAuthConsumerProperties(block)
        )
    }

    fun kafkaStreamsScramSslAuth(block: KafkaStreamsScramSslAuthConsumerPropertiesAssignmentBuilder.() -> Unit) {
        property(
            BlueprintConstants.PROPERTY_CONNECTION_CONFIG,
            BlueprintTypes.kafkaStreamsScramSslAuthConsumerProperties(block)
        )
    }
}

fun BlueprintTypes.kafkaBasicAuthMessageConsumerProperties(block: KafkaBasicAuthMessageConsumerPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = KafkaBasicAuthMessageConsumerPropertiesAssignmentBuilder().apply(block).build()
    assignments[KafkaBasicAuthMessageConsumerProperties::type.name] =
        MessageLibConstants.TYPE_KAFKA_BASIC_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

fun BlueprintTypes.kafkaSslAuthMessageConsumerProperties(block: KafkaSslAuthMessageConsumerPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = KafkaSslAuthMessageConsumerPropertiesAssignmentBuilder().apply(block).build()
    assignments[KafkaSslAuthMessageConsumerProperties::type.name] =
        MessageLibConstants.TYPE_KAFKA_SSL_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

fun BlueprintTypes.kafkaScramSslAuthMessageConsumerProperties(block: KafkaScramSslAuthMessageConsumerPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = KafkaScramSslAuthMessageConsumerPropertiesAssignmentBuilder().apply(block).build()
    assignments[KafkaScramSslAuthMessageConsumerProperties::type.name] =
        MessageLibConstants.TYPE_KAFKA_SCRAM_SSL_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

fun BlueprintTypes.kafkaStreamsBasicAuthConsumerProperties(block: KafkaStreamsBasicAuthConsumerPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = KafkaStreamsBasicAuthConsumerPropertiesAssignmentBuilder().apply(block).build()
    assignments[KafkaStreamsBasicAuthConsumerProperties::type.name] =
        MessageLibConstants.TYPE_KAFKA_STREAMS_BASIC_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

fun BlueprintTypes.kafkaStreamsSslAuthConsumerProperties(block: KafkaStreamsSslAuthConsumerPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = KafkaStreamsSslAuthConsumerPropertiesAssignmentBuilder().apply(block).build()
    assignments[KafkaStreamsSslAuthConsumerProperties::type.name] =
        MessageLibConstants.TYPE_KAFKA_STREAMS_SSL_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

fun BlueprintTypes.kafkaStreamsScramSslAuthConsumerProperties(block: KafkaStreamsScramSslAuthConsumerPropertiesAssignmentBuilder.() -> Unit): JsonNode {
    val assignments = KafkaStreamsScramSslAuthConsumerPropertiesAssignmentBuilder().apply(block).build()
    assignments[KafkaStreamsScramSslAuthConsumerProperties::type.name] =
        MessageLibConstants.TYPE_KAFKA_STREAMS_SCRAM_SSL_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

open class MessageConsumerPropertiesAssignmentBuilder : PropertiesAssignmentBuilder()

/** KafkaBasicAuthMessageConsumerProperties assignment builder */
open class KafkaBasicAuthMessageConsumerPropertiesAssignmentBuilder : MessageConsumerPropertiesAssignmentBuilder() {

    fun bootstrapServers(bootstrapServers: String) = bootstrapServers(bootstrapServers.asJsonPrimitive())

    fun bootstrapServers(bootstrapServers: JsonNode) =
        property(KafkaBasicAuthMessageConsumerProperties::bootstrapServers, bootstrapServers)

    fun groupId(groupId: String) = groupId(groupId.asJsonPrimitive())

    fun groupId(groupId: JsonNode) =
        property(KafkaBasicAuthMessageConsumerProperties::groupId, groupId)

    fun clientId(clientId: String) = clientId(clientId.asJsonPrimitive())

    fun clientId(clientId: JsonNode) =
        property(KafkaBasicAuthMessageConsumerProperties::clientId, clientId)

    fun topic(topic: String) = topic(topic.asJsonPrimitive())

    fun topic(topic: JsonNode) =
        property(KafkaBasicAuthMessageConsumerProperties::topic, topic)

    fun autoCommit(autoCommit: Boolean) = autoCommit(autoCommit.asJsonPrimitive())

    fun autoCommit(autoCommit: JsonNode) =
        property(KafkaBasicAuthMessageConsumerProperties::autoCommit, autoCommit)

    fun autoOffsetReset(autoOffsetReset: String) = autoOffsetReset(autoOffsetReset.asJsonPrimitive())

    fun autoOffsetReset(autoOffsetReset: JsonNode) =
        property(KafkaBasicAuthMessageConsumerProperties::autoOffsetReset, autoOffsetReset)

    fun pollMillSec(pollMillSec: Int) = pollMillSec(pollMillSec.asJsonPrimitive())

    fun pollMillSec(pollMillSec: JsonNode) =
        property(KafkaBasicAuthMessageConsumerProperties::pollMillSec, pollMillSec)

    fun pollRecords(pollRecords: Int) = pollRecords(pollRecords.asJsonPrimitive())

    fun pollRecords(pollRecords: JsonNode) =
        property(KafkaBasicAuthMessageConsumerProperties::pollRecords, pollRecords)
}

open class KafkaSslAuthMessageConsumerPropertiesAssignmentBuilder : KafkaBasicAuthMessageConsumerPropertiesAssignmentBuilder() {

    fun truststore(truststore: String) = truststore(truststore.asJsonPrimitive())

    fun truststore(truststore: JsonNode) =
        property(KafkaSslAuthMessageConsumerProperties::truststore, truststore)

    fun truststorePassword(truststorePassword: String) = truststorePassword(truststorePassword.asJsonPrimitive())

    fun truststorePassword(truststorePassword: JsonNode) =
        property(KafkaSslAuthMessageConsumerProperties::truststorePassword, truststorePassword)

    fun truststoreType(truststoreType: String) = truststoreType(truststoreType.asJsonPrimitive())

    fun truststoreType(truststoreType: JsonNode) =
        property(KafkaSslAuthMessageConsumerProperties::truststoreType, truststoreType)

    fun keystore(keystore: String) = keystore(keystore.asJsonPrimitive())

    fun keystore(keystore: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::keystore, keystore)

    fun keystorePassword(keystorePassword: String) = keystorePassword(keystorePassword.asJsonPrimitive())

    fun keystorePassword(keystorePassword: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::keystorePassword, keystorePassword)

    fun keystoreType(keystoreType: String) = keystoreType(keystoreType.asJsonPrimitive())

    fun keystoreType(keystoreType: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::keystoreType, keystoreType)

    fun sslEndpointIdentificationAlgorithm(sslEndpointIdentificationAlgorithm: String) =
        sslEndpointIdentificationAlgorithm(sslEndpointIdentificationAlgorithm.asJsonPrimitive())

    fun sslEndpointIdentificationAlgorithm(sslEndpointIdentificationAlgorithm: JsonNode) =
        property(KafkaSslAuthMessageConsumerProperties::sslEndpointIdentificationAlgorithm, sslEndpointIdentificationAlgorithm)
}

class KafkaScramSslAuthMessageConsumerPropertiesAssignmentBuilder : KafkaSslAuthMessageConsumerPropertiesAssignmentBuilder() {

    fun saslMechanism(saslMechanism: String) = saslMechanism(saslMechanism.asJsonPrimitive())

    fun saslMechanism(saslMechanism: JsonNode) =
        property(KafkaScramSslAuthMessageConsumerProperties::saslMechanism, saslMechanism)

    fun scramUsername(scramUsername: String) = scramUsername(scramUsername.asJsonPrimitive())

    fun scramUsername(scramUsername: JsonNode) =
        property(KafkaScramSslAuthMessageConsumerProperties::scramUsername, scramUsername)

    fun scramPassword(scramPassword: String) = scramPassword(scramPassword.asJsonPrimitive())

    fun scramPassword(scramPassword: JsonNode) =
        property(KafkaScramSslAuthMessageConsumerProperties::scramPassword, scramPassword)
}

/** KafkaStreamsConsumerProperties assignment builder */
open class KafkaStreamsBasicAuthConsumerPropertiesAssignmentBuilder : MessageConsumerPropertiesAssignmentBuilder() {

    fun bootstrapServers(bootstrapServers: String) = bootstrapServers(bootstrapServers.asJsonPrimitive())

    fun bootstrapServers(bootstrapServers: JsonNode) =
        property(KafkaStreamsBasicAuthConsumerProperties::bootstrapServers, bootstrapServers)

    fun applicationId(applicationId: String) = bootstrapServers(applicationId.asJsonPrimitive())

    fun applicationId(applicationId: JsonNode) =
        property(KafkaStreamsBasicAuthConsumerProperties::applicationId, applicationId)

    fun topic(topic: String) = topic(topic.asJsonPrimitive())

    fun topic(topic: JsonNode) =
        property(KafkaStreamsBasicAuthConsumerProperties::topic, topic)

    fun autoOffsetReset(autoOffsetReset: String) = autoOffsetReset(autoOffsetReset.asJsonPrimitive())

    fun autoOffsetReset(autoOffsetReset: JsonNode) =
        property(KafkaStreamsBasicAuthConsumerProperties::autoOffsetReset, autoOffsetReset)

    fun processingGuarantee(processingGuarantee: String) = processingGuarantee(processingGuarantee.asJsonPrimitive())

    fun processingGuarantee(processingGuarantee: JsonNode) =
        property(KafkaStreamsBasicAuthConsumerProperties::processingGuarantee, processingGuarantee)
}

open class KafkaStreamsSslAuthConsumerPropertiesAssignmentBuilder : KafkaStreamsBasicAuthConsumerPropertiesAssignmentBuilder() {

    fun truststore(truststore: String) = truststore(truststore.asJsonPrimitive())

    fun truststore(truststore: JsonNode) =
        property(KafkaStreamsSslAuthConsumerProperties::truststore, truststore)

    fun truststorePassword(truststorePassword: String) = truststorePassword(truststorePassword.asJsonPrimitive())

    fun truststorePassword(truststorePassword: JsonNode) =
        property(KafkaStreamsSslAuthConsumerProperties::truststorePassword, truststorePassword)

    fun truststoreType(truststoreType: String) = truststoreType(truststoreType.asJsonPrimitive())

    fun truststoreType(truststoreType: JsonNode) =
        property(KafkaStreamsSslAuthConsumerProperties::truststoreType, truststoreType)

    fun keystore(keystore: String) = keystore(keystore.asJsonPrimitive())

    fun keystore(keystore: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::keystore, keystore)

    fun keystorePassword(keystorePassword: String) = keystorePassword(keystorePassword.asJsonPrimitive())

    fun keystorePassword(keystorePassword: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::keystorePassword, keystorePassword)

    fun keystoreType(keystoreType: String) = keystoreType(keystoreType.asJsonPrimitive())

    fun keystoreType(keystoreType: JsonNode) =
        property(KafkaSslAuthMessageProducerProperties::keystoreType, keystoreType)

    fun sslEndpointIdentificationAlgorithm(sslEndpointIdentificationAlgorithm: String) =
        sslEndpointIdentificationAlgorithm(sslEndpointIdentificationAlgorithm.asJsonPrimitive())

    fun sslEndpointIdentificationAlgorithm(sslEndpointIdentificationAlgorithm: JsonNode) =
        property(KafkaStreamsSslAuthConsumerProperties::sslEndpointIdentificationAlgorithm, sslEndpointIdentificationAlgorithm)
}

class KafkaStreamsScramSslAuthConsumerPropertiesAssignmentBuilder : KafkaStreamsSslAuthConsumerPropertiesAssignmentBuilder() {

    fun saslMechanism(saslMechanism: String) = saslMechanism(saslMechanism.asJsonPrimitive())

    fun saslMechanism(saslMechanism: JsonNode) =
        property(KafkaStreamsScramSslAuthConsumerProperties::saslMechanism, saslMechanism)

    fun scramUsername(scramUsername: String) = scramUsername(scramUsername.asJsonPrimitive())

    fun scramUsername(scramUsername: JsonNode) =
        property(KafkaStreamsScramSslAuthConsumerProperties::scramUsername, scramUsername)

    fun scramPassword(scramPassword: String) = scramPassword(scramPassword.asJsonPrimitive())

    fun scramPassword(scramPassword: JsonNode) =
        property(KafkaStreamsScramSslAuthConsumerProperties::scramPassword, scramPassword)
}

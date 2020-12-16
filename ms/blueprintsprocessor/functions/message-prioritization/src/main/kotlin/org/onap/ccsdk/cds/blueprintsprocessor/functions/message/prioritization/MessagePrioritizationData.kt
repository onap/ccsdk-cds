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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization

import java.io.Serializable

object MessageActionConstants {

    const val PRIORITIZE = "prioritize"
}

enum class MessageState(val id: String) {
    NEW("new"),
    WAIT("wait"),
    EXPIRED("expired"),
    PRIORITIZED("prioritized"),
    AGGREGATED("aggregated"),
    COMPLETED("completed"),
    ERROR("error")
}

open class PrioritizationConfiguration : Serializable {

    lateinit var expiryConfiguration: ExpiryConfiguration
    lateinit var shutDownConfiguration: ShutDownConfiguration
    lateinit var cleanConfiguration: CleanConfiguration
    var kafkaConfiguration: KafkaConfiguration? = null // Optional Kafka Consumer Configuration
    var natsConfiguration: NatsConfiguration? = null // Optional NATS Consumer Configuration
}

open class KafkaConfiguration : Serializable {

    lateinit var inputTopicSelector: String // Consumer Configuration Selector
    lateinit var expiredTopic: String // Publish Configuration Selector
    lateinit var outputTopic: String // Publish Configuration Selector
}

open class NatsConfiguration : Serializable {

    lateinit var connectionSelector: String // Consumer Configuration Selector
    lateinit var inputSubject: String // Publish Configuration Selector
    lateinit var expiredSubject: String // Publish Configuration Selector
    lateinit var outputSubject: String // Publish Configuration Selector
}

open class ExpiryConfiguration : Serializable {

    var frequencyMilli: Long = 30000L
    var maxPollRecord: Int = 1000
}

open class ShutDownConfiguration : Serializable {

    var waitMill: Long = 30000L
}

open class CleanConfiguration : Serializable {

    var frequencyMilli: Long = 30000L
    var expiredRecordsHoldDays: Int = 5
}

open class UpdateStateRequest : Serializable {

    lateinit var id: String
    var group: String? = null
    var state: String? = null
}

data class CorrelationCheckResponse(
    var message: String? = null,
    var correlated: Boolean = false
)

data class TypeCorrelationKey(val type: String, val correlationId: String)

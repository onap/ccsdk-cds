/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.message.service

import kotlinx.coroutines.channels.Channel
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.streams.Topology
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageConsumerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException

/** Consumer Function Interfaces */
interface ConsumerFunction

interface BlueprintMessageConsumerService {

    suspend fun subscribe(): Channel<ConsumerRecord<String, ByteArray>> {
        return subscribe(null)
    }

    /** Subscribe to the Kafka channel with [additionalConfig] */
    suspend fun subscribe(additionalConfig: Map<String, Any>?): Channel<ConsumerRecord<String, ByteArray>>

    /** Subscribe to the Kafka channel with [additionalConfig] for dynamic [topics]*/
    suspend fun subscribe(topics: List<String>, additionalConfig: Map<String, Any>? = null): Channel<ConsumerRecord<String, ByteArray>>

    /** Consume and execute dynamic function [consumerFunction] */
    suspend fun consume(consumerFunction: ConsumerFunction) {
        consume(null, consumerFunction)
    }

    /** Consume with [additionalConfig], so that we can execute dynamic function [consumerFunction] */
    suspend fun consume(additionalConfig: Map<String, Any>?, consumerFunction: ConsumerFunction) {
        throw BlueprintProcessorException("Not Implemented")
    }

    /** Consume the [topics] with [additionalConfig], so that we can execute dynamic function [consumerFunction] */
    suspend fun consume(
        topics: List<String>,
        additionalConfig: Map<String, Any>?,
        consumerFunction: ConsumerFunction
    ) {
        throw BlueprintProcessorException("Not Implemented")
    }

    /** close the channel, consumer and other resources */
    suspend fun shutDown()
}

/** Consumer dynamic implementation interface */
interface KafkaConsumerRecordsFunction : ConsumerFunction {

    suspend fun invoke(
        messageConsumerProperties: MessageConsumerProperties,
        consumer: Consumer<*, *>,
        consumerRecords: ConsumerRecords<*, *>
    )
}

interface KafkaStreamConsumerFunction : ConsumerFunction {

    suspend fun createTopology(
        messageConsumerProperties: MessageConsumerProperties,
        additionalConfig: Map<String, Any>?
    ): Topology
}

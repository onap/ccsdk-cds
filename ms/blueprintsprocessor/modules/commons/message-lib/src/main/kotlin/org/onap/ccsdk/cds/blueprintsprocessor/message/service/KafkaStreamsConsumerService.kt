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

package org.onap.ccsdk.cds.blueprintsprocessor.message.service

import kotlinx.coroutines.channels.Channel
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.streams.KafkaStreams
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageConsumerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import java.util.Properties

open class KafkaStreamsConsumerService(private val messageConsumerProperties: MessageConsumerProperties) :
    BlueprintMessageConsumerService {

    val log = logger(KafkaStreamsConsumerService::class)
    lateinit var kafkaStreams: KafkaStreams

    private fun streamsConfig(additionalConfig: Map<String, Any>? = null): Properties {
        val configProperties = Properties()
        /** set consumer properties */
        messageConsumerProperties.getConfig().let { configProperties.putAll(it) }
        /** add or override already set properties */
        additionalConfig?.let { configProperties.putAll(it) }
        /** Create Kafka consumer */
        return configProperties
    }

    override suspend fun subscribe(additionalConfig: Map<String, Any>?): Channel<ConsumerRecord<String, ByteArray>> {
        throw BlueprintProcessorException("not implemented")
    }

    override suspend fun subscribe(topics: List<String>, additionalConfig: Map<String, Any>?): Channel<ConsumerRecord<String, ByteArray>> {
        throw BlueprintProcessorException("not implemented")
    }

    override suspend fun consume(additionalConfig: Map<String, Any>?, consumerFunction: ConsumerFunction) {
        val streamsConfig = streamsConfig(additionalConfig)
        val kafkaStreamConsumerFunction = consumerFunction as KafkaStreamConsumerFunction
        val topology = kafkaStreamConsumerFunction.createTopology(messageConsumerProperties, additionalConfig)
        log.info("Kafka streams topology : ${topology.describe()}")
        kafkaStreams = KafkaStreams(topology, streamsConfig)
        kafkaStreams.cleanUp()
        kafkaStreams.start()
        kafkaStreams.localThreadsMetadata().forEach { data -> log.info("Topology : $data") }
    }

    override suspend fun shutDown() {
        if (kafkaStreams != null) {
            kafkaStreams.close()
        }
    }
}

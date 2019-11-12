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
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaStreamsBasicAuthConsumerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import java.util.*

open class KafkaStreamsBasicAuthConsumerService(private val messageConsumerProperties: KafkaStreamsBasicAuthConsumerProperties)
    : BlueprintMessageConsumerService {

    val log = logger(KafkaStreamsBasicAuthConsumerService::class)
    lateinit var kafkaStreams: KafkaStreams

    private fun streamsConfig(additionalConfig: Map<String, Any>? = null): Properties {
        val configProperties = Properties()
        configProperties[StreamsConfig.APPLICATION_ID_CONFIG] = messageConsumerProperties.applicationId
        configProperties[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = messageConsumerProperties.bootstrapServers
        configProperties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = messageConsumerProperties.autoOffsetReset
        configProperties[StreamsConfig.PROCESSING_GUARANTEE_CONFIG] = messageConsumerProperties.processingGuarantee
        // TODO("Security Implementation based on type")
        /** add or override already set properties */
        additionalConfig?.let { configProperties.putAll(it) }
        /** Create Kafka consumer */
        return configProperties
    }

    override suspend fun subscribe(additionalConfig: Map<String, Any>?): Channel<String> {
        throw BluePrintProcessorException("not implemented")
    }

    override suspend fun subscribe(topics: List<String>, additionalConfig: Map<String, Any>?): Channel<String> {
        throw BluePrintProcessorException("not implemented")
    }

    override suspend fun consume(additionalConfig: Map<String, Any>?, consumerFunction: ConsumerFunction) {
        val streamsConfig = streamsConfig(additionalConfig)
        val kafkaStreamConsumerFunction = consumerFunction as KafkaStreamConsumerFunction
        val topology = kafkaStreamConsumerFunction.createTopology(messageConsumerProperties, additionalConfig)
        log.info("Kafka streams topology : ${topology.describe()}")
        kafkaStreams = KafkaStreams(topology, streamsConfig)
        kafkaStreams.cleanUp()
        kafkaStreams.start()
    }

    override suspend fun shutDown() {
        if (kafkaStreams != null) {
            kafkaStreams.close()
        }
    }
}
/*
 * Copyright © 2019 Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.message.service.consumer

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaBasicAuthMessageConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.KafkaBasicAuthMessageProducerService
import org.slf4j.LoggerFactory
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate

class KafkaBasicAuthMessageConsumerService(private val messageConsumerProperties: KafkaBasicAuthMessageConsumerProperties):BlueprintMessageConsumerService {

    private val log = LoggerFactory.getLogger(KafkaBasicAuthMessageProducerService::class.java)!!

    private var kafkaTemplate: KafkaTemplate<String, Any>? = null

    companion object {
        // TODO This should come from messageConsumerProperties.
        const val CONSUMER_TOPIC = "topic"
    }

    override fun consume(record :ConsumerRecord<String, String>) {
        log.info("Message consumed: " + record.value())

        // TODO Process a message.
    }

    /**
     * Create a Kafka Consumer Factory.
     */
    private fun consumerFactory(additionalConfig: Map<String, Any>? = null): ConsumerFactory<String, Any>  {
        log.info("Consumer Properties : $messageConsumerProperties")
        val configProperties = hashMapOf<String, Any>()
        configProperties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = messageConsumerProperties.bootstrapServers
        configProperties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProperties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java

        // Add additional Properties
        if (additionalConfig != null) {
            configProperties.putAll(additionalConfig)
        }

        return DefaultKafkaConsumerFactory(configProperties)
    }

    /**
     * Create a Kafka MessageListener Container
     */
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String>  {
            val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
            factory.consumerFactory = consumerFactory()
            return factory
    }
}


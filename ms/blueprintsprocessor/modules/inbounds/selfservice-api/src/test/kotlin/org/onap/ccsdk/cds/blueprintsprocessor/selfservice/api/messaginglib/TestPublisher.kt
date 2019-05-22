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

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.messaginglib

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.ContainerTestUtils

class TestPublisher {
    private lateinit var template: KafkaTemplate<String, String>

    companion object {
        const val SERVER = "localhost:9092"
        const val RECEIVER_TOPIC = "topic-inbox"
    }

    fun setupKafkaForProducer(broker: EmbeddedKafkaBroker) {
        // Set up Kafka producer properties.
        val senderProperties = HashMap<String, Any>(3)
        senderProperties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = SERVER
        senderProperties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        senderProperties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java

        // Create a kafka producer factory
        val producerFactory = DefaultKafkaProducerFactory<String, String>(senderProperties)

        // Create a Kafka template
        template = kafkaTemplate(producerFactory)

        val kafkaListenerEndpointRegistry = KafkaListenerEndpointRegistry()

        // wait until the partitions are assigned
        for (messageListenerContainer in kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer,
                broker.getPartitionsPerTopic())
        }
    }

    fun publishEvent(event:String) {
        template.sendDefault(event)
    }

    private fun kafkaTemplate(producerFactory: DefaultKafkaProducerFactory<String, String>): KafkaTemplate<String, String> {
        val template = KafkaTemplate<String, String>(producerFactory)
        template.defaultTopic = RECEIVER_TOPIC
        return template
    }
}
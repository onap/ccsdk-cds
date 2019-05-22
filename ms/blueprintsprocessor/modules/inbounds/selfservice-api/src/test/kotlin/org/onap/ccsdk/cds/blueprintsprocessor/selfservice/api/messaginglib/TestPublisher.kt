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
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.util.*

class TestPublisher {
    private lateinit var template: KafkaTemplate<String, String>

    companion object {
        const val SERVER = "global-kafka:9092"
        const val RECEIVER_TOPIC = "topic-inbox"
    }

    fun setupKafkaForProducer(broker: EmbeddedKafkaBroker) {
        // Set up Kafka producer properties.
        val senderProperties = KafkaTestUtils.senderProps(broker.getBrokersAsString())

        // Create a kafka producer factory
        val producerFactory = DefaultKafkaProducerFactory<String, String>(senderProperties)

        // Create a Kafka template
        template = kafkaTemplateString(producerFactory)

        var kafkaListenerEndpointRegistry = KafkaListenerEndpointRegistry()

        // wait until the partitions are assigned
        for (messageListenerContainer in kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer,
                broker.getPartitionsPerTopic())
        }
    }

    fun publishEvent(event:String) {
        template.sendDefault(event)
    }

    private fun producerFactoryString(): ProducerFactory<String, String> {
        val props = HashMap<String, Any>(3)
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = SERVER
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return DefaultKafkaProducerFactory(props)
    }

    private fun kafkaTemplateString(producerFactory: DefaultKafkaProducerFactory<String, String>): KafkaTemplate<String, String> {
        val template = KafkaTemplate<String, String>(producerFactory)
        template.defaultTopic = RECEIVER_TOPIC
        return template
    }
}
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

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.MessagingController
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.PartitionOffset
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@SpringBootTest
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@TestPropertySource(locations = ["classpath:application-test.properties"])
@DirtiesContext
@EmbeddedKafka(ports = [9092])
class MessagingControllerTest {

    private val log = LoggerFactory.getLogger(MessagingControllerTest::class.java)!!

    @Autowired
    lateinit var controller: MessagingController

    @Value("\${blueprintsprocessor.messageclient.self-service-api.consumerTopic}")
    lateinit var topicUsedForConsumer: String

    @Autowired
    lateinit var kt: KafkaTemplate<String, String>

    var receivedEvent: String? = null

    @Test
    fun testReceive() {
        val greeting = "{\"message\":\"" + "message-test" + "\"}"
        kt.defaultTopic = topicUsedForConsumer
        kt.sendDefault(greeting)
        log.info("test-sender sent message='{}'", greeting)
        Thread.sleep(1000)

        assertEquals(greeting, receivedEvent)
    }

    @KafkaListener(topicPartitions = [TopicPartition(topic = "\${blueprintsprocessor.messageclient.self-service-api.topic}", partitionOffsets = [PartitionOffset(partition = "0", initialOffset = "0")])])
    fun receivedEventFromBluePrintProducer(event: String) {
        receivedEvent = event
    }

    @Configuration
    @EnableKafka
    open class ConsumerConfiguration {

        @Value("\${blueprintsprocessor.messageclient.self-service-api.bootstrapServers}")
        lateinit var bootstrapServers: String

        @Value("\${blueprintsprocessor.messageclient.self-service-api.groupId}")
        lateinit var groupId:String

        @Bean
        open fun consumerFactory2(): ConsumerFactory<String, String>? {
            val configProperties = hashMapOf<String, Any>()
            configProperties[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
            configProperties[ConsumerConfig.GROUP_ID_CONFIG] = groupId
            configProperties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
            configProperties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
            configProperties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            configProperties[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 1000

            return DefaultKafkaConsumerFactory(configProperties)
        }

        @Bean
        open fun listenerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
            val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
            factory.consumerFactory = consumerFactory2()
            return factory
        }
    }
}
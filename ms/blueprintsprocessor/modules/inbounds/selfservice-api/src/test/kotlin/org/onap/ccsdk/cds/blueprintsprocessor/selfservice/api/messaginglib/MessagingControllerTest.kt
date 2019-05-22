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
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.MessagingController
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@TestPropertySource(locations = ["classpath:application-test.properties"])
@DirtiesContext
@EmbeddedKafka
class MessagingControllerTest {

    private val log = LoggerFactory.getLogger(MessagingControllerTest::class.java)!!

    @Autowired
    lateinit var controller: MessagingController

    @Autowired
    lateinit var kt: KafkaTemplate<String, String>

    @Test
    fun testReceive() {
        val greeting = "Hello Spring Kafka Receiver!";
        kt.defaultTopic = "receiver.t"
        kt.sendDefault(greeting)
        log.info("test-sender sent message='{}'", greeting)

        Thread.sleep(1000)
    }

    @Configuration
    @EnableKafka
    open class Config {

        private val log = LoggerFactory.getLogger(MessagingControllerTest::class.java)!!


        @Value("\${" + EmbeddedKafkaBroker.SPRING_EMBEDDED_KAFKA_BROKERS + "}")
        private lateinit var brokerAddresses: String

        @Bean
        open fun kpf(): ProducerFactory<String, String> {
            val configs = HashMap<String, Any>()
            configs[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = this.brokerAddresses
            configs[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
            configs[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
            return DefaultKafkaProducerFactory(configs)
        }

        @Bean
        open fun kt(): KafkaTemplate<String, String> {
            return KafkaTemplate(kpf())
        }
    }
}
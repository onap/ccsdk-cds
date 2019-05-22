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

import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.MessagingController
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.test.rule.EmbeddedKafkaRule
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@TestPropertySource(locations = ["classpath:application-test.properties"])
@SpringBootTest
open class MessagingControllerTest {

    private val log = LoggerFactory.getLogger(MessagingControllerTest::class.java)!!

    lateinit var template: KafkaTemplate<String, String>

    @Autowired
    lateinit var controller: MessagingController

    companion object {
        const val RECEIVER_TOPIC = "receiver.t"
        @ClassRule @JvmField
        val embeddedKafka = EmbeddedKafkaRule(1, true,  RECEIVER_TOPIC)
    }

    @Before
    @Throws(Exception::class)
    fun setUp() {
        // set up the Kafka producer properties
        val senderProperties = KafkaTestUtils.senderProps(
            embeddedKafka.embeddedKafka.brokersAsString)

        // create a Kafka producer factory
        val producerFactory = DefaultKafkaProducerFactory<String, String>(
            senderProperties)

        // create a Kafka template
        template = KafkaTemplate(producerFactory)
        // set the default topic to send to
        template.setDefaultTopic(RECEIVER_TOPIC)
    }

    @Test
    fun testReceive() {
        // Send the message on the RECEIVER_TOPIC so kafka listener will listen onto it.
        val greeting = "Hello Spring Kafka Receiver!"

        template.sendDefault(greeting)

        log.info("test-sender sent message= $greeting}")
    }
}
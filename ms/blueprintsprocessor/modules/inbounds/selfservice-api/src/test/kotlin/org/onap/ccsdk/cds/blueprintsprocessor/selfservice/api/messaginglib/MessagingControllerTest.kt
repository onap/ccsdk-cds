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

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.BeforeTest

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@SpringBootTest(classes = [(MessagingControllerTest::class)])
class MessagingControllerTest {
    companion object {
        const val RECEIVER_TOPIC = "topic-inbox"
    }

    // To create Kafka server.
    val kafkaBroker = EmbeddedKafkaBroker(1, true, 1, RECEIVER_TOPIC)

    val publisher = TestPublisher()

    @BeforeTest
    @Throws(Exception::class)
    fun setUp() {
        publisher.setupKafkaForProducer(kafkaBroker)
    }

    @Test
    fun testReceive() {
        // Send the message on the RECEIVER_TOPIC so kafka listener will listen onto it.
        val event = "event-test"
        publisher.publishEvent(event)

        //TODO
        // call -> kafkaListener.listen() to consume kafka-test string.
    }
}
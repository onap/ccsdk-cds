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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization

import io.mockk.coEvery
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.PrioritizationMessageRepository
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.MessagePrioritizationStateService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.utils.MessagePrioritizationSample
import org.onap.ccsdk.cds.blueprintsprocessor.message.BluePrintMessageLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BluePrintMessageLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.KafkaBasicAuthMessageProducerService
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.ApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.Test
import kotlin.test.assertNotNull


@RunWith(SpringRunner::class)
@DataJpaTest
@DirtiesContext
@ContextConfiguration(classes = [BluePrintMessageLibConfiguration::class,
    BluePrintPropertyConfiguration::class, BluePrintPropertiesService::class,
    MessagePrioritizationConfiguration::class, TestDatabaseConfiguration::class])
@TestPropertySource(properties =
[
    "spring.jpa.show-sql=true",
    "spring.jpa.properties.hibernate.show_sql=true",
    "spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl",

    "blueprintsprocessor.messageconsumer.prioritize-input.type=kafka-streams-basic-auth",
    "blueprintsprocessor.messageconsumer.prioritize-input.bootstrapServers=127.0.0.1:9092",
    "blueprintsprocessor.messageconsumer.prioritize-input.applicationId=test-prioritize-application",
    "blueprintsprocessor.messageconsumer.prioritize-input.topic=prioritize-input-topic",

    // To send initial test message
    "blueprintsprocessor.messageproducer.prioritize-input.type=kafka-basic-auth",
    "blueprintsprocessor.messageproducer.prioritize-input.bootstrapServers=127.0.0.1:9092",
    "blueprintsprocessor.messageproducer.prioritize-input.topic=prioritize-input-topic"
])
open class MessagePrioritizationConsumerTest {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    lateinit var prioritizationMessageRepository: PrioritizationMessageRepository

    @Autowired
    lateinit var bluePrintMessageLibPropertyService: BluePrintMessageLibPropertyService

    @Before
    fun setup() {
        BluePrintDependencyService.inject(applicationContext)
    }

    @Test
    fun testBluePrintKafkaJDBCKeyStore() {
        runBlocking {
            assertNotNull(prioritizationMessageRepository, "failed to get prioritizationMessageRepository")

            val messagePrioritizationService: MessagePrioritizationStateService = BluePrintDependencyService
                    .instance(MessagePrioritizationStateService::class)
            assertNotNull(messagePrioritizationService, "failed to get messagePrioritizationService")

            MessagePrioritizationSample.sampleMessages(MessageState.NEW.name, 1).forEach {
                val message = messagePrioritizationService.saveMessage(it)
                val repoResult = messagePrioritizationService.getMessage(message.id)
                assertNotNull(repoResult, "failed to get inserted message.")
            }
        }
    }

    @Test
    fun testStartConsuming() {
        runBlocking {
            val configuration = MessagePrioritizationSample.samplePrioritizationConfiguration()

            val streamingConsumerService = bluePrintMessageLibPropertyService
                    .blueprintMessageConsumerService(configuration.inputTopicSelector)
            assertNotNull(streamingConsumerService, "failed to get blueprintMessageConsumerService")

            val spyStreamingConsumerService = spyk(streamingConsumerService)
            coEvery { spyStreamingConsumerService.consume(any(), any()) } returns Unit
            coEvery { spyStreamingConsumerService.shutDown() } returns Unit
            val messagePrioritizationConsumer = MessagePrioritizationConsumer(bluePrintMessageLibPropertyService)
            val spyMessagePrioritizationConsumer = spyk(messagePrioritizationConsumer)


            // Test Topology
            val kafkaStreamConsumerFunction = spyMessagePrioritizationConsumer.kafkaStreamConsumerFunction(configuration)
            val messageConsumerProperties = bluePrintMessageLibPropertyService
                    .messageConsumerProperties("blueprintsprocessor.messageconsumer.prioritize-input")
            val topology = kafkaStreamConsumerFunction.createTopology(messageConsumerProperties, null)
            assertNotNull(topology, "failed to get create topology")

            every { spyMessagePrioritizationConsumer.consumerService(any()) } returns spyStreamingConsumerService
            spyMessagePrioritizationConsumer.startConsuming(configuration)
            spyMessagePrioritizationConsumer.shutDown()
        }
    }

    /** Integration Kafka Testing, Enable and use this test case only for local desktop testing with real kafka broker */
    //@Test
    fun testMessagePrioritizationConsumer() {
        runBlocking {
            val messagePrioritizationConsumer = MessagePrioritizationConsumer(bluePrintMessageLibPropertyService)
            messagePrioritizationConsumer.startConsuming(MessagePrioritizationSample.samplePrioritizationConfiguration())

            /** Send sample message with every 1 sec */
            val blueprintMessageProducerService = bluePrintMessageLibPropertyService
                    .blueprintMessageProducerService("prioritize-input") as KafkaBasicAuthMessageProducerService
            launch {
             MessagePrioritizationSample.sampleMessages(MessageState.NEW.name, 2).forEach {
                    delay(100)
                    val headers: MutableMap<String, String> = hashMapOf()
                    headers["id"] = it.id
                    blueprintMessageProducerService.sendMessageNB(message = it.asJsonString(false),
                            headers = headers)
                }

                MessagePrioritizationSample
                        .sampleMessageWithSameCorrelation("same-group", MessageState.NEW.name, 2)
                        .forEach {
                            delay(100)
                            val headers: MutableMap<String, String> = hashMapOf()
                            headers["id"] = it.id
                            blueprintMessageProducerService.sendMessageNB(message = it.asJsonString(false),
                                    headers = headers)
                        }

                MessagePrioritizationSample
                        .sampleMessageWithDifferentTypeSameCorrelation("group-typed", MessageState.NEW.name, 3)
                        .forEach {
                            delay(2000)
                            val headers: MutableMap<String, String> = hashMapOf()
                            headers["id"] = it.id
                            blueprintMessageProducerService.sendMessageNB(message = it.asJsonString(false),
                                    headers = headers)
                        }
            }
            delay(10000)
            messagePrioritizationConsumer.shutDown()
        }
    }
}
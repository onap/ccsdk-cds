/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
 * Modifications Copyright © 2021 Bell Canada.
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

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.PrioritizationMessageRepository
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.kafka.DefaultMessagePrioritizeProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.kafka.KafkaMessagePrioritizationConsumer
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.nats.NatsMessagePrioritizationConsumer
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.MessagePrioritizationSchedulerService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.SampleKafkaMessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.SampleMessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.SampleNatsMessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.utils.MessagePrioritizationSample
import org.onap.ccsdk.cds.blueprintsprocessor.message.BlueprintMessageLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BlueprintMessageLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.KafkaMessageProducerService
import org.onap.ccsdk.cds.blueprintsprocessor.nats.BlueprintNatsLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.nats.service.BlueprintNatsLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.nats.utils.NatsClusterUtils
import org.onap.ccsdk.cds.controllerblueprints.core.asByteArray
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
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
@ContextConfiguration(
    classes = [
        BlueprintMessageLibConfiguration::class, BlueprintNatsLibConfiguration::class,
        BlueprintPropertyConfiguration::class, BlueprintPropertiesService::class,
        MessagePrioritizationConfiguration::class, TestDatabaseConfiguration::class
    ]
)
@TestPropertySource(
    properties =
        [
            "spring.jpa.show-sql=false",
            "spring.jpa.properties.hibernate.show_sql=false",
            "spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl",

            "blueprintsprocessor.messageconsumer.prioritize-input.type=kafka-streams-scram-ssl-auth",
            "blueprintsprocessor.messageconsumer.prioritize-input.bootstrapServers=127.0.0.1:9092",
            "blueprintsprocessor.messageconsumer.prioritize-input.applicationId=test-prioritize-application",
            "blueprintsprocessor.messageconsumer.prioritize-input.topic=prioritize-input-topic",
            "blueprintsprocessor.messageconsumer.prioritize-input.truststore=/path/to/truststore.jks",
            "blueprintsprocessor.messageconsumer.prioritize-input.truststorePassword=truststorePassword",
            "blueprintsprocessor.messageconsumer.prioritize-input.keystore=/path/to/keystore.jks",
            "blueprintsprocessor.messageconsumer.prioritize-input.keystorePassword=keystorePassword",
            "blueprintsprocessor.messageconsumer.prioritize-input.scramUsername=test-user",
            "blueprintsprocessor.messageconsumer.prioritize-input.scramPassword=testUserPassword",

            // To send initial test message
            "blueprintsprocessor.messageproducer.prioritize-input.type=kafka-scram-ssl-auth",
            "blueprintsprocessor.messageproducer.prioritize-input.bootstrapServers=127.0.0.1:9092",
            "blueprintsprocessor.messageproducer.prioritize-input.topic=prioritize-input-topic",
            "blueprintsprocessor.messageproducer.prioritize-input.truststore=/path/to/truststore.jks",
            "blueprintsprocessor.messageproducer.prioritize-input.truststorePassword=truststorePassword",
            "blueprintsprocessor.messageproducer.prioritize-input.keystore=/path/to/keystore.jks",
            "blueprintsprocessor.messageproducer.prioritize-input.keystorePassword=keystorePassword",
            "blueprintsprocessor.messageproducer.prioritize-input.scramUsername=test-user",
            "blueprintsprocessor.messageproducer.prioritize-input.scramPassword=testUserPassword",

            "blueprintsprocessor.nats.cds-controller.type=token-auth",
            "blueprintsprocessor.nats.cds-controller.host=nats://localhost:4222",
            "blueprintsprocessor.nats.cds-controller.token=tokenAuth"
        ]
)
open class MessagePrioritizationConsumerTest {

    private val log = logger(MessagePrioritizationConsumerTest::class)

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    lateinit var prioritizationMessageRepository: PrioritizationMessageRepository

    @Autowired
    lateinit var bluePrintMessageLibPropertyService: BlueprintMessageLibPropertyService

    @Autowired
    lateinit var bluePrintNatsLibPropertyService: BlueprintNatsLibPropertyService

    @Autowired
    lateinit var messagePrioritizationStateService: MessagePrioritizationStateService

    @MockBean
    lateinit var meterRegistry: MeterRegistry

    @Before
    fun setup() {
        BlueprintDependencyService.inject(applicationContext)
    }

    @Test
    fun testBlueprintKafkaJDBCKeyStore() {
        runBlocking {
            assertNotNull(prioritizationMessageRepository, "failed to get prioritizationMessageRepository")

            val messagePrioritizationService: MessagePrioritizationStateService = BlueprintDependencyService
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
    fun testMessagePrioritizationService() {
        runBlocking {
            val configuration = MessagePrioritizationSample.samplePrioritizationConfiguration()
            val messagePrioritizationService =
                SampleMessagePrioritizationService(messagePrioritizationStateService)
            messagePrioritizationService.setConfiguration(configuration)

            log.info("****************  without Correlation **************")
            /** Checking without correlation */
            MessagePrioritizationSample.sampleMessages(MessageState.NEW.name, 2).forEach {
                messagePrioritizationService.prioritize(it)
            }
            log.info("****************  Same Group , with Correlation **************")
            /** checking same group with correlation */
            MessagePrioritizationSample
                .sampleMessageWithSameCorrelation("same-group", MessageState.NEW.name, 2)
                .forEach {
                    delay(10)
                    messagePrioritizationService.prioritize(it)
                }
            log.info("****************  Different Type , with Correlation **************")
            /** checking different type, with correlation */
            MessagePrioritizationSample
                .sampleMessageWithDifferentTypeSameCorrelation("group-typed", MessageState.NEW.name, 3)
                .forEach {
                    delay(10)
                    messagePrioritizationService.prioritize(it)
                }
        }
    }

    @Test
    fun testStartConsuming() {
        runBlocking {
            val configuration = MessagePrioritizationSample.samplePrioritizationConfiguration()

            val streamingConsumerService = bluePrintMessageLibPropertyService
                .blueprintMessageConsumerService(configuration.kafkaConfiguration!!.inputTopicSelector)
            assertNotNull(streamingConsumerService, "failed to get blueprintMessageConsumerService")

            val spyStreamingConsumerService = spyk(streamingConsumerService)
            coEvery { spyStreamingConsumerService.consume(any(), any()) } returns Unit
            coEvery { spyStreamingConsumerService.shutDown() } returns Unit
            val messagePrioritizationConsumer = KafkaMessagePrioritizationConsumer(
                bluePrintMessageLibPropertyService, mockk()
            )
            val spyMessagePrioritizationConsumer = spyk(messagePrioritizationConsumer)

            // Test Topology
            val kafkaStreamConsumerFunction =
                spyMessagePrioritizationConsumer.kafkaStreamConsumerFunction(configuration)
            val messageConsumerProperties = bluePrintMessageLibPropertyService
                .messageConsumerProperties("blueprintsprocessor.messageconsumer.prioritize-input")
            val topology = kafkaStreamConsumerFunction.createTopology(messageConsumerProperties, null)
            assertNotNull(topology, "failed to get create topology")

            every { spyMessagePrioritizationConsumer.consumerService(any()) } returns spyStreamingConsumerService
            spyMessagePrioritizationConsumer.startConsuming(configuration)
            spyMessagePrioritizationConsumer.shutDown()
        }
    }

    @Test
    fun testSchedulerService() {
        runBlocking {
            val configuration = MessagePrioritizationSample.samplePrioritizationConfiguration()
            val messagePrioritizationService =
                SampleMessagePrioritizationService(messagePrioritizationStateService)
            messagePrioritizationService.setConfiguration(configuration)

            val messagePrioritizationSchedulerService =
                MessagePrioritizationSchedulerService(messagePrioritizationService)
            launch {
                messagePrioritizationSchedulerService.startScheduling()
            }
            launch {
                /** To debug increase the delay time */
                delay(20)
                messagePrioritizationSchedulerService.shutdownScheduling()
            }
        }
    }

    /** Integration Kafka Testing, Enable and use this test case only for local desktop testing with real kafka broker */
    // @Test
    fun testKafkaMessagePrioritizationConsumer() {
        runBlocking {

            val configuration = MessagePrioritizationSample.samplePrioritizationConfiguration()
            val kafkaMessagePrioritizationService =
                SampleKafkaMessagePrioritizationService(messagePrioritizationStateService)
            kafkaMessagePrioritizationService.setConfiguration(configuration)

            val defaultMessagePrioritizeProcessor = DefaultMessagePrioritizeProcessor(
                messagePrioritizationStateService,
                kafkaMessagePrioritizationService
            )

            // Register the processor
            BlueprintDependencyService.registerSingleton(
                MessagePrioritizationConstants.PROCESSOR_PRIORITIZE,
                defaultMessagePrioritizeProcessor
            )

            val messagePrioritizationConsumer = KafkaMessagePrioritizationConsumer(
                bluePrintMessageLibPropertyService,
                kafkaMessagePrioritizationService
            )
            messagePrioritizationConsumer.startConsuming(configuration)

            /** Send sample message with every 1 sec */
            val blueprintMessageProducerService = bluePrintMessageLibPropertyService
                .blueprintMessageProducerService("prioritize-input") as KafkaMessageProducerService
            launch {
                MessagePrioritizationSample.sampleMessages(MessageState.NEW.name, 2).forEach {
                    delay(100)
                    val headers: MutableMap<String, String> = hashMapOf()
                    headers["id"] = it.id
                    blueprintMessageProducerService.sendMessageNB(
                        key = "mykey",
                        message = it.asJsonString(false),
                        headers = headers
                    )
                }

                MessagePrioritizationSample
                    .sampleMessageWithSameCorrelation("same-group", MessageState.NEW.name, 2)
                    .forEach {
                        delay(100)
                        val headers: MutableMap<String, String> = hashMapOf()
                        headers["id"] = it.id
                        blueprintMessageProducerService.sendMessageNB(
                            key = "mykey",
                            message = it.asJsonString(false),
                            headers = headers
                        )
                    }

                MessagePrioritizationSample
                    .sampleMessageWithDifferentTypeSameCorrelation("group-typed", MessageState.NEW.name, 3)
                    .forEach {
                        delay(2000)
                        val headers: MutableMap<String, String> = hashMapOf()
                        headers["id"] = it.id
                        blueprintMessageProducerService.sendMessageNB(
                            key = "mykey",
                            message = it.asJsonString(false),
                            headers = headers
                        )
                    }
            }
            delay(10000)
            messagePrioritizationConsumer.shutDown()
        }
    }

    /** Integration Nats Testing, Enable and use this test case only for local desktop testing with real kafka broker
     *  Start :
     *  nats-streaming-server -cid cds-cluster --auth tokenAuth -m 8222 -V
     * */
    // @Test
    fun testNatsMessagePrioritizationConsumer() {
        runBlocking {
            val configuration = MessagePrioritizationSample.samplePrioritizationConfiguration()
            assertNotNull(configuration.natsConfiguration, "failed to get nats Configuration")

            val inputSubject =
                NatsClusterUtils.currentApplicationSubject(configuration.natsConfiguration!!.inputSubject)

            val natsMessagePrioritizationService =
                SampleNatsMessagePrioritizationService(messagePrioritizationStateService)
            natsMessagePrioritizationService.setConfiguration(configuration)

            val messagePrioritizationConsumer =
                NatsMessagePrioritizationConsumer(bluePrintNatsLibPropertyService, natsMessagePrioritizationService)
            messagePrioritizationConsumer.startConsuming()

            /** Send sample message with every 1 sec */
            val bluePrintNatsService = messagePrioritizationConsumer.bluePrintNatsService

            launch {
                MessagePrioritizationSample.sampleMessages(MessageState.NEW.name, 2).forEach {
                    delay(100)
                    bluePrintNatsService.publish(inputSubject, it.asJsonType().asByteArray())
                }

                MessagePrioritizationSample
                    .sampleMessageWithSameCorrelation("same-group", MessageState.NEW.name, 2)
                    .forEach {
                        delay(100)
                        bluePrintNatsService.publish(inputSubject, it.asJsonType().asByteArray())
                    }

                MessagePrioritizationSample
                    .sampleMessageWithDifferentTypeSameCorrelation("group-typed", MessageState.NEW.name, 3)
                    .forEach {
                        delay(200)
                        bluePrintNatsService.publish(inputSubject, it.asJsonType().asByteArray())
                    }
            }
            delay(3000)
            messagePrioritizationConsumer.shutDown()
        }
    }
}

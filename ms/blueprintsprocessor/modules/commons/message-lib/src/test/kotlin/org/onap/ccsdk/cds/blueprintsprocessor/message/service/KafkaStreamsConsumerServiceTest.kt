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

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.processor.Processor
import org.apache.kafka.streams.processor.ProcessorSupplier
import org.apache.kafka.streams.state.Stores
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.BlueprintMessageLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaStreamsBasicAuthConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageConsumerProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@DirtiesContext
@ContextConfiguration(
    classes = [
        BlueprintMessageLibConfiguration::class,
        BlueprintPropertyConfiguration::class, BlueprintPropertiesService::class
    ]
)
@TestPropertySource(
    properties =
        [
            "blueprintsprocessor.messageproducer.sample.type=kafka-scram-ssl-auth",
            "blueprintsprocessor.messageproducer.sample.bootstrapServers=127.0.0.1:9092",
            "blueprintsprocessor.messageproducer.sample.topic=default-stream-topic",
            "blueprintsprocessor.messageproducer.sample.clientId=default-client-id",
            "blueprintsprocessor.messageproducer.sample.truststore=/path/to/truststore.jks",
            "blueprintsprocessor.messageproducer.sample.truststorePassword=secretpassword",
            "blueprintsprocessor.messageproducer.sample.scramUsername=sample-user",
            "blueprintsprocessor.messageproducer.sample.scramPassword=secretpassword",

            "blueprintsprocessor.messageconsumer.stream-consumer.type=kafka-streams-scram-ssl-auth",
            "blueprintsprocessor.messageconsumer.stream-consumer.bootstrapServers=127.0.0.1:9092",
            "blueprintsprocessor.messageconsumer.stream-consumer.applicationId=test-streams-application",
            "blueprintsprocessor.messageconsumer.stream-consumer.topic=default-stream-topic",
            "blueprintsprocessor.messageproducer.stream-consumer.truststore=/path/to/truststore.jks",
            "blueprintsprocessor.messageproducer.stream-consumer.truststorePassword=secretpassword",
            "blueprintsprocessor.messageproducer.stream-consumer.scramUsername=sample-user",
            "blueprintsprocessor.messageproducer.stream-consumer.scramPassword=secretpassword"

        ]
)
class KafkaStreamsConsumerServiceTest {

    @Autowired
    lateinit var bluePrintMessageLibPropertyService: BlueprintMessageLibPropertyService

    @Test
    fun testProperties() {
        val blueprintMessageConsumerService = bluePrintMessageLibPropertyService.blueprintMessageConsumerService("stream-consumer")
        assertNotNull(blueprintMessageConsumerService, "failed to get blueprintMessageProducerService")
    }

    /** Integration Kafka Testing, Enable and use this test case only for local desktop testing with real kafka broker */
    // @Test
    fun testKafkaStreamingMessageConsumer() {
        runBlocking {
            val streamingConsumerService = bluePrintMessageLibPropertyService.blueprintMessageConsumerService("stream-consumer")

            // Dynamic Consumer Function to create Topology
            val consumerFunction = object : KafkaStreamConsumerFunction {
                override suspend fun createTopology(
                    messageConsumerProperties: MessageConsumerProperties,
                    additionalConfig: Map<String, Any>?
                ): Topology {
                    val topology = Topology()
                    val kafkaStreamsBasicAuthConsumerProperties = messageConsumerProperties
                        as KafkaStreamsBasicAuthConsumerProperties

                    val topics = kafkaStreamsBasicAuthConsumerProperties.topic.split(",")
                    topology.addSource("Source", *topics.toTypedArray())
                    // Processor Supplier
                    val firstProcessorSupplier = object : ProcessorSupplier<ByteArray, ByteArray> {
                        override fun get(): Processor<ByteArray, ByteArray> {
                            return FirstProcessor()
                        }
                    }
                    val changelogConfig: MutableMap<String, String> = hashMapOf()
                    changelogConfig.put("min.insync.replicas", "1")

                    // Store Buolder
                    val countStoreSupplier = Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore("PriorityMessageState"),
                        Serdes.String(),
                        PriorityMessageSerde()
                    )
                        .withLoggingEnabled(changelogConfig)

                    topology.addProcessor("FirstProcessor", firstProcessorSupplier, "Source")
                    topology.addStateStore(countStoreSupplier, "FirstProcessor")
                    topology.addSink(
                        "SINK", "default-stream-topic-out", Serdes.String().serializer(),
                        PriorityMessageSerde().serializer(), "FirstProcessor"
                    )
                    return topology
                }
            }

            /** Send message with every 1 sec */
            val blueprintMessageProducerService = bluePrintMessageLibPropertyService
                .blueprintMessageProducerService("sample") as KafkaMessageProducerService
            launch {
                repeat(5) {
                    delay(1000)
                    val headers: MutableMap<String, String> = hashMapOf()
                    headers["id"] = it.toString()
                    blueprintMessageProducerService.sendMessageNB(
                        key = "mykey",
                        message = "this is my message($it)",
                        headers = headers
                    )
                }
            }
            streamingConsumerService.consume(null, consumerFunction)
            delay(10000)
            streamingConsumerService.shutDown()
        }
    }
}

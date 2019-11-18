/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2018-2019 AT&T Intellectual Property.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.message.service

import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.*
import org.apache.kafka.common.TopicPartition
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.BluePrintMessageLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageConsumerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@RunWith(SpringRunner::class)
@DirtiesContext
@ContextConfiguration(classes = [BluePrintMessageLibConfiguration::class,
    BluePrintPropertyConfiguration::class, BluePrintPropertiesService::class])
@TestPropertySource(properties =
["blueprintsprocessor.messageconsumer.sample.type=kafka-basic-auth",
    "blueprintsprocessor.messageconsumer.sample.bootstrapServers=127.0.0.1:9092",
    "blueprintsprocessor.messageconsumer.sample.groupId=sample-group",
    "blueprintsprocessor.messageconsumer.sample.topic=default-topic",
    "blueprintsprocessor.messageconsumer.sample.clientId=default-client-id",
    "blueprintsprocessor.messageconsumer.sample.pollMillSec=10",
    "blueprintsprocessor.messageconsumer.sample.pollRecords=1",

    "blueprintsprocessor.messageproducer.sample.type=kafka-basic-auth",
    "blueprintsprocessor.messageproducer.sample.bootstrapServers=127.0.0.1:9092",
    "blueprintsprocessor.messageproducer.sample.topic=default-topic",
    "blueprintsprocessor.messageproducer.sample.clientId=default-client-id"
])
open class BlueprintMessageConsumerServiceTest {
    val log = logger(BlueprintMessageConsumerServiceTest::class)

    @Autowired
    lateinit var bluePrintMessageLibPropertyService: BluePrintMessageLibPropertyService

    @Test
    fun testKafkaBasicAuthConsumerService() {
        runBlocking {
            val blueprintMessageConsumerService = bluePrintMessageLibPropertyService
                    .blueprintMessageConsumerService("sample") as KafkaBasicAuthMessageConsumerService
            assertNotNull(blueprintMessageConsumerService, "failed to get blueprintMessageConsumerService")

            val spyBlueprintMessageConsumerService = spyk(blueprintMessageConsumerService, recordPrivateCalls = true)

            val topic = "default-topic"
            val partitions: MutableList<TopicPartition> = arrayListOf()
            val topicsCollection: MutableList<String> = arrayListOf()
            partitions.add(TopicPartition(topic, 1))
            val partitionsBeginningMap: MutableMap<TopicPartition, Long> = mutableMapOf()
            val partitionsEndMap: MutableMap<TopicPartition, Long> = mutableMapOf()

            val records: Long = 10
            partitions.forEach { partition ->
                partitionsBeginningMap[partition] = 0L
                partitionsEndMap[partition] = records
                topicsCollection.add(partition.topic())
            }
            val mockKafkaConsumer = MockConsumer<String, ByteArray>(OffsetResetStrategy.EARLIEST)
            mockKafkaConsumer.subscribe(topicsCollection)
            mockKafkaConsumer.rebalance(partitions)
            mockKafkaConsumer.updateBeginningOffsets(partitionsBeginningMap)
            mockKafkaConsumer.updateEndOffsets(partitionsEndMap)
            for (i in 1..10) {
                val record = ConsumerRecord<String, ByteArray>(topic, 1, i.toLong(), "key_$i",
                        "I am message $i".toByteArray())
                mockKafkaConsumer.addRecord(record)
            }

            every { spyBlueprintMessageConsumerService.kafkaConsumer(any()) } returns mockKafkaConsumer
            val channel = spyBlueprintMessageConsumerService.subscribe(null)
            launch {
                channel.consumeEach {
                    assertTrue(it.startsWith("I am message"), "failed to get the actual message")
                }
            }
            delay(10)
            spyBlueprintMessageConsumerService.shutDown()
        }
    }

    @Test
    fun testKafkaBasicAuthConsumerWithDynamicFunction() {
        runBlocking {
            val blueprintMessageConsumerService = bluePrintMessageLibPropertyService
                    .blueprintMessageConsumerService("sample") as KafkaBasicAuthMessageConsumerService
            assertNotNull(blueprintMessageConsumerService, "failed to get blueprintMessageConsumerService")

            val spyBlueprintMessageConsumerService = spyk(blueprintMessageConsumerService, recordPrivateCalls = true)

            val topic = "default-topic"
            val partitions: MutableList<TopicPartition> = arrayListOf()
            val topicsCollection: MutableList<String> = arrayListOf()
            partitions.add(TopicPartition(topic, 1))
            val partitionsBeginningMap: MutableMap<TopicPartition, Long> = mutableMapOf()
            val partitionsEndMap: MutableMap<TopicPartition, Long> = mutableMapOf()

            val records: Long = 10
            partitions.forEach { partition ->
                partitionsBeginningMap[partition] = 0L
                partitionsEndMap[partition] = records
                topicsCollection.add(partition.topic())
            }
            val mockKafkaConsumer = MockConsumer<String, ByteArray>(OffsetResetStrategy.EARLIEST)
            mockKafkaConsumer.subscribe(topicsCollection)
            mockKafkaConsumer.rebalance(partitions)
            mockKafkaConsumer.updateBeginningOffsets(partitionsBeginningMap)
            mockKafkaConsumer.updateEndOffsets(partitionsEndMap)
            for (i in 1..10) {
                val record = ConsumerRecord<String, ByteArray>(topic, 1, i.toLong(), "key_$i",
                        "I am message $i".toByteArray())
                mockKafkaConsumer.addRecord(record)
            }

            every { spyBlueprintMessageConsumerService.kafkaConsumer(any()) } returns mockKafkaConsumer
            /** Test Consumer Function implementation */
            val consumerFunction = object : KafkaConsumerRecordsFunction {
                override suspend fun invoke(messageConsumerProperties: MessageConsumerProperties,
                                            consumer: Consumer<*, *>, consumerRecords: ConsumerRecords<*, *>) {
                    val count = consumerRecords.count()
                    log.trace("Received Message count($count)")
                }
            }
            spyBlueprintMessageConsumerService.consume(consumerFunction)
            delay(10)
            spyBlueprintMessageConsumerService.shutDown()
        }
    }

    /** Integration Kafka Testing, Enable and use this test case only for local desktop testing with real kafka broker */
    //@Test
    fun testKafkaIntegration() {
        runBlocking {
            val blueprintMessageConsumerService = bluePrintMessageLibPropertyService
                    .blueprintMessageConsumerService("sample") as KafkaBasicAuthMessageConsumerService
            assertNotNull(blueprintMessageConsumerService, "failed to get blueprintMessageConsumerService")

            val channel = blueprintMessageConsumerService.subscribe(null)
            launch {
                channel.consumeEach {
                    log.info("Consumed Message : $it")
                }
            }

            /** Send message with every 1 sec */
            val blueprintMessageProducerService = bluePrintMessageLibPropertyService
                    .blueprintMessageProducerService("sample") as KafkaBasicAuthMessageProducerService
            launch {
                repeat(5) {
                    delay(100)
                    val headers: MutableMap<String, String> = hashMapOf()
                    headers["id"] = it.toString()
                    blueprintMessageProducerService.sendMessageNB(message = "this is my message($it)",
                            headers = headers)
                }
            }
            delay(5000)
            blueprintMessageConsumerService.shutDown()
        }
    }
}
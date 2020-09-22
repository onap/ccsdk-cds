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
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.security.scram.ScramLoginModule
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.BluePrintMessageLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageLibConstants
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.nio.charset.Charset
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@DirtiesContext
@ContextConfiguration(
    classes = [
        BluePrintMessageLibConfiguration::class,
        BluePrintPropertyConfiguration::class, BluePrintPropertiesService::class
    ]
)
@TestPropertySource(
    properties =
        [
            "blueprintsprocessor.messageconsumer.sample.type=kafka-scram-ssl-auth",
            "blueprintsprocessor.messageconsumer.sample.bootstrapServers=127.0.0.1:9092",
            "blueprintsprocessor.messageconsumer.sample.groupId=sample-group",
            "blueprintsprocessor.messageconsumer.sample.topic=default-topic",
            "blueprintsprocessor.messageconsumer.sample.clientId=default-client-id",
            "blueprintsprocessor.messageconsumer.sample.pollMillSec=10",
            "blueprintsprocessor.messageconsumer.sample.pollRecords=1",
            "blueprintsprocessor.messageconsumer.sample.truststore=/path/to/truststore.jks",
            "blueprintsprocessor.messageconsumer.sample.truststorePassword=secretpassword",
            "blueprintsprocessor.messageconsumer.sample.keystore=/path/to/keystore.jks",
            "blueprintsprocessor.messageconsumer.sample.keystorePassword=secretpassword",
            "blueprintsprocessor.messageconsumer.sample.scramUsername=sample-user",
            "blueprintsprocessor.messageconsumer.sample.scramPassword=secretpassword",

            "blueprintsprocessor.messageproducer.sample.type=kafka-scram-ssl-auth",
            "blueprintsprocessor.messageproducer.sample.bootstrapServers=127.0.0.1:9092",
            "blueprintsprocessor.messageproducer.sample.topic=default-topic",
            "blueprintsprocessor.messageproducer.sample.clientId=default-client-id",
            "blueprintsprocessor.messageproducer.sample.truststore=/path/to/truststore.jks",
            "blueprintsprocessor.messageproducer.sample.truststorePassword=secretpassword",
            "blueprintsprocessor.messageproducer.sample.keystore=/path/to/keystore.jks",
            "blueprintsprocessor.messageproducer.sample.keystorePassword=secretpassword",
            "blueprintsprocessor.messageproducer.sample.scramUsername=sample-user",
            "blueprintsprocessor.messageproducer.sample.scramPassword=secretpassword"
        ]
)
open class BlueprintMessageConsumerServiceTest {

    val log = logger(BlueprintMessageConsumerServiceTest::class)

    @Autowired
    lateinit var bluePrintMessageLibPropertyService: BluePrintMessageLibPropertyService

    @Test
    fun testKafkaBasicAuthConsumerService() {
        runBlocking {
            val blueprintMessageConsumerService = bluePrintMessageLibPropertyService
                .blueprintMessageConsumerService("sample") as KafkaMessageConsumerService
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
                val record = ConsumerRecord<String, ByteArray>(
                    topic, 1, i.toLong(), "key_$i",
                    "I am message $i".toByteArray()
                )
                mockKafkaConsumer.addRecord(record)
            }

            every { spyBlueprintMessageConsumerService.kafkaConsumer(any()) } returns mockKafkaConsumer
            val channel = spyBlueprintMessageConsumerService.subscribe(null)
            var i = 0
            launch {
                channel.consumeEach {
                    ++i
                    val key = it.key()
                    val value = String(it.value(), Charset.defaultCharset())
                    assertTrue(value.startsWith("I am message"), "failed to get the actual message")
                    assertEquals("key_$i", key)
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
                .blueprintMessageConsumerService("sample") as KafkaMessageConsumerService
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
                val record = ConsumerRecord<String, ByteArray>(
                    topic, 1, i.toLong(), "key_$i",
                    "I am message $i".toByteArray()
                )
                mockKafkaConsumer.addRecord(record)
            }

            every { spyBlueprintMessageConsumerService.kafkaConsumer(any()) } returns mockKafkaConsumer
            /** Test Consumer Function implementation */
            val consumerFunction = object : KafkaConsumerRecordsFunction {
                override suspend fun invoke(
                    messageConsumerProperties: MessageConsumerProperties,
                    consumer: Consumer<*, *>,
                    consumerRecords: ConsumerRecords<*, *>
                ) {
                    val count = consumerRecords.count()
                    log.trace("Received Message count($count)")
                }
            }
            spyBlueprintMessageConsumerService.consume(consumerFunction)
            delay(10)
            spyBlueprintMessageConsumerService.shutDown()
        }
    }

    @Test
    fun testKafkaScramSslAuthConfig() {

        val expectedConfig = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "127.0.0.1:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "sample-group",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to true,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java,
            ConsumerConfig.CLIENT_ID_CONFIG to "default-client-id",
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to SecurityProtocol.SASL_SSL.toString(),
            SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to "/path/to/truststore.jks",
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to "secretpassword",
            SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to "/path/to/keystore.jks",
            SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "JKS",
            SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to "secretpassword",
            SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to SslConfigs.DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM,
            SaslConfigs.SASL_MECHANISM to "SCRAM-SHA-512",
            SaslConfigs.SASL_JAAS_CONFIG to "${ScramLoginModule::class.java.canonicalName} required " +
                "username=\"sample-user\" " +
                "password=\"secretpassword\";"
        )

        val messageConsumerProperties = bluePrintMessageLibPropertyService
            .messageConsumerProperties("${MessageLibConstants.PROPERTY_MESSAGE_CONSUMER_PREFIX}sample")

        val configProps = messageConsumerProperties.getConfig()

        assertEquals(
            messageConsumerProperties.topic,
            "default-topic",
            "Topic doesn't match the expected value"
        )
        assertEquals(
            messageConsumerProperties.type,
            "kafka-scram-ssl-auth",
            "Authentication type doesn't match the expected value"
        )

        expectedConfig.forEach {
            assertTrue(
                configProps.containsKey(it.key),
                "Missing expected kafka config key : ${it.key}"
            )
            assertEquals(
                configProps[it.key],
                it.value,
                "Unexpected value for ${it.key} got ${configProps[it.key]} instead of ${it.value}"
            )
        }
    }

    /** Integration Kafka Testing, Enable and use this test case only for local desktop testing with real kafka broker */
    // @Test
    fun testKafkaIntegration() {
        runBlocking {
            val blueprintMessageConsumerService = bluePrintMessageLibPropertyService
                .blueprintMessageConsumerService("sample") as KafkaMessageConsumerService
            assertNotNull(blueprintMessageConsumerService, "failed to get blueprintMessageConsumerService")

            val channel = blueprintMessageConsumerService.subscribe(null)
            launch {
                channel.consumeEach {
                    log.info("Consumed Message : $it")
                }
            }

            /** Send message with every 1 sec */
            val blueprintMessageProducerService = bluePrintMessageLibPropertyService
                .blueprintMessageProducerService("sample") as KafkaMessageProducerService
            launch {
                repeat(5) {
                    delay(100)
                    val headers: MutableMap<String, String> = hashMapOf()
                    headers["id"] = it.toString()
                    blueprintMessageProducerService.sendMessageNB(
                        key = "mykey",
                        message = "this is my message($it)",
                        headers = headers
                    )
                }
            }
            delay(5000)
            blueprintMessageConsumerService.shutDown()
        }
    }
}

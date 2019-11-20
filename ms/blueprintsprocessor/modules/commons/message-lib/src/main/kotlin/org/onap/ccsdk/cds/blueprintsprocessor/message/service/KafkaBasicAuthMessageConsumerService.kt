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

import java.nio.charset.Charset
import java.time.Duration
import kotlin.concurrent.thread
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaBasicAuthMessageConsumerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.logger

open class KafkaBasicAuthMessageConsumerService(
    private val messageConsumerProperties: KafkaBasicAuthMessageConsumerProperties
) :
    BlueprintMessageConsumerService {

    val log = logger(KafkaBasicAuthMessageConsumerService::class)
    val channel = Channel<String>()
    var kafkaConsumer: Consumer<String, ByteArray>? = null

    @Volatile
    var keepGoing = true

    fun kafkaConsumer(additionalConfig: Map<String, Any>? = null): Consumer<String, ByteArray> {
        val configProperties = hashMapOf<String, Any>()
        configProperties[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = messageConsumerProperties.bootstrapServers
        configProperties[ConsumerConfig.GROUP_ID_CONFIG] = messageConsumerProperties.groupId
        configProperties[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = messageConsumerProperties.autoCommit
        /**
         * earliest: automatically reset the offset to the earliest offset
         * latest: automatically reset the offset to the latest offset
         */
        configProperties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = messageConsumerProperties.autoOffsetReset
        configProperties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProperties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java
        configProperties[ConsumerConfig.CLIENT_ID_CONFIG] = messageConsumerProperties.clientId

        /** To handle Back pressure, Get only configured record for processing */
        if (messageConsumerProperties.pollRecords > 0) {
            configProperties[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = messageConsumerProperties.pollRecords
        }
        // TODO("Security Implementation based on type")
        /** add or override already set properties */
        additionalConfig?.let { configProperties.putAll(it) }
        /** Create Kafka consumer */
        return KafkaConsumer(configProperties)
    }

    override suspend fun subscribe(additionalConfig: Map<String, Any>?): Channel<String> {
        /** get to topic names */
        val consumerTopic = messageConsumerProperties.topic?.split(",")?.map { it.trim() }
        check(!consumerTopic.isNullOrEmpty()) { "couldn't get topic information" }
        return subscribe(consumerTopic, additionalConfig)
    }

    override suspend fun subscribe(topics: List<String>, additionalConfig: Map<String, Any>?): Channel<String> {
        /** Create Kafka consumer */
        kafkaConsumer = kafkaConsumer(additionalConfig)

        checkNotNull(kafkaConsumer) {
            "failed to create kafka consumer for " +
                    "server(${messageConsumerProperties.bootstrapServers})'s " +
                    "topics(${messageConsumerProperties.bootstrapServers})"
        }

        kafkaConsumer!!.subscribe(topics)
        log.info("Successfully consumed topic($topics)")

        thread(start = true, name = "KafkaConsumer-${messageConsumerProperties.clientId}") {
            keepGoing = true
            kafkaConsumer!!.use { kc ->
                while (keepGoing) {
                    val consumerRecords = kc.poll(Duration.ofMillis(messageConsumerProperties.pollMillSec))
                    log.trace("Consumed Records : ${consumerRecords.count()}")
                    runBlocking {
                        consumerRecords?.forEach { consumerRecord ->
                            /** execute the command block */
                            consumerRecord.value()?.let {
                                launch {
                                    if (!channel.isClosedForSend) {
                                        channel.send(String(it, Charset.defaultCharset()))
                                    } else {
                                        log.error("Channel is closed to receive message")
                                    }
                                }
                            }
                        }
                    }
                }
                log.info("message listener shutting down.....")
            }
        }
        return channel
    }

    override suspend fun consume(additionalConfig: Map<String, Any>?, consumerFunction: ConsumerFunction) {
        /** get to topic names */
        val consumerTopic = messageConsumerProperties.topic?.split(",")?.map { it.trim() }
        check(!consumerTopic.isNullOrEmpty()) { "couldn't get topic information" }
        return consume(topics = consumerTopic, additionalConfig = additionalConfig, consumerFunction = consumerFunction)
    }

    override suspend fun consume(
        topics: List<String>,
        additionalConfig: Map<String, Any>?,
        consumerFunction: ConsumerFunction
    ) {

        val kafkaConsumerFunction = consumerFunction as KafkaConsumerRecordsFunction

        /** Create Kafka consumer */
        kafkaConsumer = kafkaConsumer(additionalConfig)

        checkNotNull(kafkaConsumer) {
            "failed to create kafka consumer for " +
                    "server(${messageConsumerProperties.bootstrapServers})'s " +
                    "topics(${messageConsumerProperties.bootstrapServers})"
        }

        kafkaConsumer!!.subscribe(topics)
        log.info("Successfully consumed topic($topics)")

        thread(start = true, name = "KafkaConsumer-${messageConsumerProperties.clientId}") {
            keepGoing = true
            kafkaConsumer!!.use { kc ->
                while (keepGoing) {
                    val consumerRecords = kc.poll(Duration.ofMillis(messageConsumerProperties.pollMillSec))
                    log.trace("Consumed Records : ${consumerRecords.count()}")
                    runBlocking {
                        /** Execute dynamic consumer Block substitution */
                        kafkaConsumerFunction.invoke(messageConsumerProperties, kc, consumerRecords)
                    }
                }
                log.info("message listener shutting down.....")
            }
        }
    }

    override suspend fun shutDown() {
        /** stop the polling loop */
        keepGoing = false
        /** Close the Channel */
        channel.cancel()
        /** TO shutdown gracefully, need to wait for the maximum poll time */
        delay(messageConsumerProperties.pollMillSec)
    }
}

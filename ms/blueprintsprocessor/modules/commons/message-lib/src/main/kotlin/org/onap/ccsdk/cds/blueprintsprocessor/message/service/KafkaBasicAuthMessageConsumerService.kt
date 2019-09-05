/*
 *  Copyright © 2019 IBM.
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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaBasicAuthMessageConsumerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import java.time.Duration
import kotlin.concurrent.thread

class KafkaBasicAuthMessageConsumerService(
        private val messageConsumerProperties: KafkaBasicAuthMessageConsumerProperties)
    : BlueprintMessageConsumerService {

    private val channel = Channel<String>()
    private var kafkaConsumer: Consumer<String, String>? = null
    val log = logger(KafkaBasicAuthMessageConsumerService::class)

    @Volatile
    var keepGoing = true

    fun kafkaConsumer(additionalConfig: Map<String, Any>? = null): Consumer<String, String> {
        val configProperties = hashMapOf<String, Any>()
        configProperties[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = messageConsumerProperties.bootstrapServers
        configProperties[ConsumerConfig.GROUP_ID_CONFIG] = messageConsumerProperties.groupId
        configProperties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "latest"
        configProperties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProperties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        /** add or override already set properties */
        additionalConfig?.let { configProperties.putAll(it) }
        /** Create Kafka consumer */
        return KafkaConsumer(configProperties)
    }

    override suspend fun subscribe(additionalConfig: Map<String, Any>?): Channel<String> {
        /** get to topic names */
        val consumerTopic = messageConsumerProperties.consumerTopic?.split(",")?.map { it.trim() }
        check(!consumerTopic.isNullOrEmpty()) { "couldn't get topic information" }
        return subscribe(consumerTopic, additionalConfig)
    }


    override suspend fun subscribe(consumerTopic: List<String>, additionalConfig: Map<String, Any>?): Channel<String> {
        /** Create Kafka consumer */
        kafkaConsumer = kafkaConsumer(additionalConfig)
        checkNotNull(kafkaConsumer) {
            "failed to create kafka consumer for " +
                    "server(${messageConsumerProperties.bootstrapServers})'s " +
                    "topics(${messageConsumerProperties.bootstrapServers})"
        }

        kafkaConsumer!!.subscribe(consumerTopic)
        log.info("Successfully consumed topic($consumerTopic)")

        val listenerThread = thread(start = true, name = "KafkaConsumer") {
            keepGoing = true
            kafkaConsumer!!.use { kc ->
                while (keepGoing) {
                    val consumerRecords = kc.poll(Duration.ofMillis(messageConsumerProperties.pollMillSec))
                    runBlocking {
                        consumerRecords?.forEach { consumerRecord ->
                            /** execute the command block */
                            consumerRecord.value()?.let {
                                launch {
                                    if (!channel.isClosedForSend) {
                                        channel.send(it)
                                    } else {
                                        log.error("Channel is closed to receive message")
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        log.info("Successfully consumed in thread(${listenerThread})")
        return channel
    }

    override suspend fun shutDown() {
        /** Close the Channel */
        channel.close()
        /** stop the polling loop */
        keepGoing = false
        if (kafkaConsumer != null) {
            /** sunsubscribe the consumer */
            kafkaConsumer!!.unsubscribe()
        }
    }
}

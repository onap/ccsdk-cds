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
package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BluePrintMessageLibPropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("app")
@EnableKafka
open class MessagingController {

    private val log = LoggerFactory.getLogger(MessagingController::class.java)!!

    companion object {
        const val KAFKA_SELECTOR = "message-lib"
    }

    @Value("\${blueprintsprocessor.messageclient.message-lib.bootstrapServers}")
    lateinit var bootstrapServers:String

    @Value("\${blueprintsprocessor.messageclient.message-lib.groupId}")
    lateinit var groupId:String

    @Autowired
    lateinit var property: BluePrintMessageLibPropertyService

    @Autowired
    lateinit var executionServiceHandler:ExecutionServiceHandler

    @KafkaListener(topics = ["\${blueprintsprocessor.messageclient.message-lib.consumerTopic}"], containerFactory = "kafkaListenerContainerFactory")
    fun listen(record:ConsumerRecord<String, String> , acknowledgment: Acknowledgment) {
        runBlocking {

            // acknowledging the processing of a ConsumerRecord
            acknowledgment.acknowledge();

            val messageToConsume = record.value()

            log.info("Successfully receieved a message: {}", messageToConsume)

            val executionServiceInput = ExecutionServiceInput().apply {
                payload = payload.putObject(messageToConsume)
            }

            // Process the message.
            processMessage(executionServiceInput)
        }
    }

    open fun consumerFactory(): ConsumerFactory<String, Any> {
        val configProperties = hashMapOf<String, Any>()
        configProperties[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProperties[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        configProperties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] ="org.apache.kafka.common.serialization.StringDeserializer"
        configProperties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"

        return DefaultKafkaConsumerFactory(configProperties)
    }

    /**
     *  Creation of a Kafka MessageListener Container
     *
     *  @return KafkaListener instance.
     */
    @Bean
    open fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
            val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
            factory.consumerFactory = consumerFactory()
            return factory
    }

    private suspend fun processMessage(executionServiceInput: ExecutionServiceInput) {
        val executionServiceOutput = executionServiceHandler.doProcess(executionServiceInput)

        val blueprintMessageProducerService = property.blueprintMessageClientService(KAFKA_SELECTOR)

        val response = blueprintMessageProducerService.sendMessage(executionServiceOutput)

        if (response.equals("SUCCESS")) {
            log.info("Successfully published the message")
        }
    }
}

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

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.common.serialization.StringDeserializer
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BluePrintMessageLibPropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

@Configuration
@EnableKafka
open class MessagingController(private val property: BluePrintMessageLibPropertyService,
                               private val executionServiceHandler: ExecutionServiceHandler) {

    private val log = LoggerFactory.getLogger(MessagingController::class.java)!!

    companion object {
        // TODO It should be retrieved from model or from request.
        const val KAFKA_SELECTOR = "message-lib"
    }

    @Value("\${blueprintsprocessor.messageclient.message-lib.groupId}")
    lateinit var groupId:String

    @Value("\${blueprintsprocessor.messageclient.message-lib.bootstrapServers}")
    lateinit var bootstrapServers: String


    @KafkaListener(topics = ["\${blueprintsprocessor.messageclient.message-lib.consumerTopic}"])
    open fun receive(event: String) {
        runBlocking {
            log.info("Successfully receieved a message: {}", event)

            val executionServiceInput = ExecutionServiceInput().apply {
                payload = payload.putObject(event)
            }

            // Process the message.
            val response = async {
                processMessage(executionServiceInput)
            }

            log.info("The result of publishing message on a given topic is  : $response")
        }
    }

    private suspend fun processMessage(executionServiceInput: ExecutionServiceInput): Boolean {
        val executionServiceOutput = executionServiceHandler.doProcess(executionServiceInput)

        val blueprintMessageProducerService = property.blueprintMessageClientService(KAFKA_SELECTOR)

        return blueprintMessageProducerService.sendMessage(executionServiceOutput)
    }

    @Bean
    open fun consumerFactory(): ConsumerFactory<String, String>? {
        val configProperties = hashMapOf<String, Any>()
        configProperties[BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProperties[GROUP_ID_CONFIG] = groupId
        configProperties[KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.getName()
        //TODO use JsonSerializer
        configProperties[VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.getName()

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
}

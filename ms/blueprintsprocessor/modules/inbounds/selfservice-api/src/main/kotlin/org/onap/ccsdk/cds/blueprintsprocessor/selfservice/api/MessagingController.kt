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
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BluePrintMessageLibPropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
@Configuration
@EnableKafka
open class MessagingController {

    private val log = LoggerFactory.getLogger(MessagingController::class.java)!!

    companion object {
        const val KAFKA_SELECTOR = "message-lib"
    }

    @Autowired
    lateinit var property: BluePrintMessageLibPropertyService

    @Autowired
    lateinit var executionServiceHandler:ExecutionServiceHandler

    @Autowired
    lateinit var executionServiceInput:ExecutionServiceInput

    @KafkaListener(topics = ["\${blueprintsprocessor.messageclient.message-lib.consumerTopic}"])
    fun listen(record:ConsumerRecord<String, String> , acknowledgment: Acknowledgment) {
        runBlocking {
            log.info("Successfully receieved a message: {}", record)

            // Process the message.
            executionServiceInput.payload.putObject(record.value());
        }
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

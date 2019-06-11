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
import org.apache.commons.lang3.builder.ToStringBuilder
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BluePrintMessageLibPropertyService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
open class MessagingController(private val propertyService: BluePrintMessageLibPropertyService,
                               private val executionServiceHandler: ExecutionServiceHandler) {

    private val log = LoggerFactory.getLogger(MessagingController::class.java)!!


    companion object {
        // TODO It should be retrieved from model or from request.
        const val PREFIX = "self-service-api"
    }

    @KafkaListener(topics = ["\${blueprintsprocessor.messageclient.self-service-api.consumerTopic}"])
    open fun receive(input: ExecutionServiceInput) {
        log.info("Successfully received a message: {}", ToStringBuilder.reflectionToString(input))

        runBlocking {
            log.info("Successfully received a message: {}", ToStringBuilder.reflectionToString(input))

            // Process the message.
            val response = async {
                processMessage(input)
            }

            log.info("The result of publishing event on a given topic is : ${response.await()}")
        }
    }

    private suspend fun processMessage(executionServiceInput: ExecutionServiceInput): Boolean {

        val executionServiceOutput = executionServiceHandler.doProcess(executionServiceInput)

        val bluePrintMessageClientService = propertyService
                .blueprintMessageClientService(PREFIX)

        log.info("The payload to publish is {}", executionServiceOutput.payload)

        return bluePrintMessageClientService.sendMessage(executionServiceOutput.payload)
    }
}

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@ConditionalOnProperty(name = ["blueprintsprocessor.messageclient.self-service-api.kafkaEnable"], havingValue = "true")
@Service
open class MessagingController(private val propertyService: BluePrintMessageLibPropertyService,
                               private val executionServiceHandler: ExecutionServiceHandler) {

    private val log = LoggerFactory.getLogger(MessagingController::class.java)!!

    companion object {
        // TODO PREFIX should be retrieved from model or from request.
        const val PREFIX = "self-service-api"
        const val EXECUTION_STATUS = 200
    }

    @KafkaListener(topics = ["\${blueprintsprocessor.messageclient.self-service-api.consumerTopic}"])
    open fun receive(input: ExecutionServiceInput) {

        log.info("Successfully received a message: {}", ToStringBuilder.reflectionToString(input))

        runBlocking {
            log.info("Successfully received a message: {}", ToStringBuilder.reflectionToString(input))

            // Process the message.
            async {
                processMessage(input)
            }
        }
    }

    private suspend fun processMessage(executionServiceInput: ExecutionServiceInput) {

        val executionServiceOutput = executionServiceHandler.doProcess(executionServiceInput)

       if (executionServiceOutput.status.code == EXECUTION_STATUS) {
           val bluePrintMessageClientService = propertyService
                   .blueprintMessageClientService(PREFIX)

           val payload = executionServiceOutput.payload

           log.info("The payload to publish is {}", payload)

            bluePrintMessageClientService.sendMessage(payload)
       }
        else {
           log.error("Fail to process the given event due to {}", executionServiceOutput.status.errorMessage)
       }
    }
}

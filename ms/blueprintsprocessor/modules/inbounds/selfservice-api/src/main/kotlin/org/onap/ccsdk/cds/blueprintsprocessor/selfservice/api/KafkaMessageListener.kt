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
package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api;

import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BluePrintMessageLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.KafkaBasicAuthMessageProducerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
@Configuration
open class KafkaMessageListener: ApplicationListener<ContextRefreshedEvent> {

    private val log = LoggerFactory.getLogger(KafkaMessageListener::class.java)!!

    companion object {
        const val KAFKA_SELECTOR = "message-lib"
    }

    @Autowired
    lateinit var property: BluePrintMessageLibPropertyService

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        consumeMessage(event);
    }

    fun consumeMessage(messageToConsumer:Any) {
        runBlocking {
            // TODO Call message-lib consumer api.
        }
    }

    // TODO At this moment not sure is this the right place for this method.
    fun sendMessage(messageToPublish: String) {
        runBlocking {
            val bluePrintMessageClientService = property
                .blueprintMessageClientService(KAFKA_SELECTOR) as KafkaBasicAuthMessageProducerService

            val response = bluePrintMessageClientService.sendMessage(messageToPublish)

            if(response.equals("SUCCESS")) {
                log.info("Successfully publish the message")
            }
        }
    }
}

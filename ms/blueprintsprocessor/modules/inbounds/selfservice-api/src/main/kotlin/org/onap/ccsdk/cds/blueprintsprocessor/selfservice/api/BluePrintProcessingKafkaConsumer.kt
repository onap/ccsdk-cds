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

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BluePrintMessageLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BlueprintMessageConsumerService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsType
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.Phaser
import javax.annotation.PreDestroy

@ConditionalOnProperty(name = ["blueprintsprocessor.messageconsumer.self-service-api.kafkaEnable"],
        havingValue = "true")
@Service
open class BluePrintProcessingKafkaConsumer(
        private val bluePrintMessageLibPropertyService: BluePrintMessageLibPropertyService,
        private val executionServiceHandler: ExecutionServiceHandler) {

    val log = logger(BluePrintProcessingKafkaConsumer::class)

    private val ph = Phaser(1)

    private lateinit var blueprintMessageConsumerService: BlueprintMessageConsumerService

    companion object {
        const val CONSUMER_SELECTOR = "self-service-api"
        const val PRODUCER_SELECTOR = "self-service-api"
    }

    @EventListener(ApplicationReadyEvent::class)
    fun setupMessageListener() = runBlocking {
        try {
            log.info("Setting up message consumer($CONSUMER_SELECTOR) and " +
                    "message producer($PRODUCER_SELECTOR)...")

            /** Get the Message Consumer Service **/
            blueprintMessageConsumerService = try {
                bluePrintMessageLibPropertyService
                        .blueprintMessageConsumerService(CONSUMER_SELECTOR)
            } catch (e: Exception) {
                throw BluePrintProcessorException("failed to create consumer service ${e.message}")
            }

            /** Get the Message Producer Service **/
            val blueprintMessageProducerService = try {
                bluePrintMessageLibPropertyService
                        .blueprintMessageProducerService(PRODUCER_SELECTOR)
            } catch (e: Exception) {
                throw BluePrintProcessorException("failed to create producer service ${e.message}")
            }

            launch {
                /** Subscribe to the consumer topics */
                val additionalConfig: MutableMap<String, Any> = hashMapOf()
                val channel = blueprintMessageConsumerService.subscribe(additionalConfig)
                channel.consumeEach { message ->
                    launch {
                        try {
                            ph.register()
                            log.trace("Consumed Message : $message")
                            val executionServiceInput = message.jsonAsType<ExecutionServiceInput>()
                            val executionServiceOutput = executionServiceHandler.doProcess(executionServiceInput)
                            //TODO("In future, Message publisher configuration vary with respect to request")
                            /** Send the response message */
                            blueprintMessageProducerService.sendMessage(executionServiceOutput)
                        } catch (e: Exception) {
                            log.error("failed in processing the consumed message : $message", e)
                        }
                        finally {
                            ph.arriveAndDeregister()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            log.error("failed to start message consumer($CONSUMER_SELECTOR) and " +
                    "message producer($PRODUCER_SELECTOR) ", e)
        }
    }

    @PreDestroy
    fun shutdownMessageListener() = runBlocking {
        try {
            log.info("Shutting down message consumer($CONSUMER_SELECTOR) and " +
                    "message producer($PRODUCER_SELECTOR)...")
            blueprintMessageConsumerService.shutDown()
            ph.arriveAndAwaitAdvance()
        } catch (e: Exception) {
            log.error("failed to shutdown message listener($CONSUMER_SELECTOR)", e)
        }
    }

}
/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2021 Bell Canada.
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

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.message.BlueprintMessageMetricConstants
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BlueprintMessageLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BlueprintMessageConsumerService
import org.onap.ccsdk.cds.blueprintsprocessor.message.utils.BlueprintMessageUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsType
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.updateErrorMessage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.util.UUID
import java.util.concurrent.Phaser
import javax.annotation.PreDestroy

@ConditionalOnProperty(
    name = ["blueprintsprocessor.messageconsumer.self-service-api.kafkaEnable"],
    havingValue = "true"
)
@Service
open class BlueprintProcessingKafkaConsumer(
    private val blueprintMessageLibPropertyService: BlueprintMessageLibPropertyService,
    private val executionServiceHandler: ExecutionServiceHandler,
    private val meterRegistry: MeterRegistry
) {

    val log = logger(BlueprintProcessingKafkaConsumer::class)

    private val ph = Phaser(1)

    private lateinit var blueprintMessageConsumerService: BlueprintMessageConsumerService

    companion object {

        const val CONSUMER_SELECTOR = "self-service-api"
        const val PRODUCER_SELECTOR = "self-service-api"
    }

    @EventListener(ApplicationReadyEvent::class)
    fun setupMessageListener() = GlobalScope.launch {
        try {
            log.info(
                "Setting up message consumer($CONSUMER_SELECTOR)" +
                    "message producer($PRODUCER_SELECTOR)..."
            )

            /** Get the Message Consumer Service **/
            blueprintMessageConsumerService = try {
                blueprintMessageLibPropertyService
                    .blueprintMessageConsumerService(CONSUMER_SELECTOR)
            } catch (e: BlueprintProcessorException) {
                val errorMsg = "Failed creating Kafka consumer message service."
                throw e.updateErrorMessage(
                    SelfServiceApiDomains.SELF_SERVICE_API, errorMsg,
                    "Wrong Kafka selector provided or internal error in Kafka service."
                )
            } catch (e: Exception) {
                throw BlueprintProcessorException("failed to create consumer service ${e.message}")
            }

            /** Get the Message Producer Service **/
            val blueprintMessageProducerService = try {
                blueprintMessageLibPropertyService
                    .blueprintMessageProducerService(PRODUCER_SELECTOR)
            } catch (e: BlueprintProcessorException) {
                val errorMsg = "Failed creating Kafka producer message service."
                throw e.updateErrorMessage(
                    SelfServiceApiDomains.SELF_SERVICE_API, errorMsg,
                    "Wrong Kafka selector provided or internal error in Kafka service."
                )
            } catch (e: Exception) {
                throw BlueprintProcessorException("failed to create producer service ${e.message}")
            }

            launch {
                /** Subscribe to the consumer topics */
                val additionalConfig: MutableMap<String, Any> = hashMapOf()
                val channel = blueprintMessageConsumerService.subscribe(additionalConfig)
                channel.consumeEach { message ->
                    launch {
                        try {
                            ph.register()
                            val key = message.key() ?: UUID.randomUUID().toString()
                            val value = String(message.value(), Charset.defaultCharset())
                            val executionServiceInput = value.jsonAsType<ExecutionServiceInput>()
                            log.info(
                                "Consumed Message : topic(${message.topic()}) " +
                                    "partition(${message.partition()}) " +
                                    "leaderEpoch(${message.leaderEpoch().get()}) " +
                                    "offset(${message.offset()}) " +
                                    "key(${message.key()}) " +
                                    "CBA(${executionServiceInput.actionIdentifiers.blueprintName}/${executionServiceInput.actionIdentifiers.blueprintVersion}/${executionServiceInput.actionIdentifiers.actionName})"
                            )
                            val executionServiceOutput = executionServiceHandler.doProcess(executionServiceInput)
                            blueprintMessageProducerService.sendMessage(key, executionServiceOutput)
                        } catch (e: Exception) {
                            meterRegistry.counter(
                                BlueprintMessageMetricConstants.KAFKA_CONSUMED_MESSAGES_ERROR_COUNTER,
                                BlueprintMessageUtils.kafkaMetricTag(message.topic())
                            ).increment()
                            log.error("failed in processing the consumed message : $message", e)
                        } finally {
                            ph.arriveAndDeregister()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            log.error(
                "failed to start message consumer($CONSUMER_SELECTOR) " +
                    "message producer($PRODUCER_SELECTOR) ",
                e
            )
        }
    }

    @PreDestroy
    fun shutdownMessageListener() = runBlocking {
        try {
            log.info(
                "Shutting down message consumer($CONSUMER_SELECTOR)" +
                    "message producer($PRODUCER_SELECTOR)..."
            )
            blueprintMessageConsumerService.shutDown()
            ph.arriveAndAwaitAdvance()
        } catch (e: Exception) {
            log.error("failed to shutdown message listener($CONSUMER_SELECTOR)", e)
        }
    }
}

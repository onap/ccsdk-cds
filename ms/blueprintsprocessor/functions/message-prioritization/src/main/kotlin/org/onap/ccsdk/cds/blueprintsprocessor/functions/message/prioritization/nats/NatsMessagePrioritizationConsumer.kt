/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.nats

import io.nats.streaming.MessageHandler
import io.nats.streaming.Subscription
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.nats.asJsonType
import org.onap.ccsdk.cds.blueprintsprocessor.nats.service.BlueprintNatsLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.nats.service.BlueprintNatsService
import org.onap.ccsdk.cds.blueprintsprocessor.nats.utils.NatsClusterUtils
import org.onap.ccsdk.cds.blueprintsprocessor.nats.utils.SubscriptionOptionsUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asType
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.utils.ClusterUtils

open class NatsMessagePrioritizationConsumer(
    private val bluePrintNatsLibPropertyService: BlueprintNatsLibPropertyService,
    private val natsMessagePrioritizationService: MessagePrioritizationService
) {

    private val log = logger(NatsMessagePrioritizationConsumer::class)

    lateinit var bluePrintNatsService: BlueprintNatsService
    private lateinit var subscription: Subscription

    suspend fun startConsuming() {
        val prioritizationConfiguration = natsMessagePrioritizationService.getConfiguration()
        val natsConfiguration = prioritizationConfiguration.natsConfiguration
            ?: throw BlueprintProcessorException("couldn't get NATS consumer configuration")

        check((natsMessagePrioritizationService is AbstractNatsMessagePrioritizationService)) {
            "messagePrioritizationService is not of type AbstractNatsMessagePrioritizationService."
        }
        bluePrintNatsService = consumerService(natsConfiguration.connectionSelector)
        natsMessagePrioritizationService.bluePrintNatsService = bluePrintNatsService
        val inputSubject = NatsClusterUtils.currentApplicationSubject(natsConfiguration.inputSubject)
        val loadBalanceGroup = ClusterUtils.applicationName()
        val messageHandler = createMessageHandler()
        val subscriptionOptions = SubscriptionOptionsUtils.durable(NatsClusterUtils.currentNodeDurable(inputSubject))
        subscription = bluePrintNatsService.loadBalanceSubscribe(
            inputSubject,
            loadBalanceGroup,
            messageHandler,
            subscriptionOptions
        )
        log.info(
            "Nats prioritization consumer listening on subject($inputSubject) on loadBalance group($loadBalanceGroup)."
        )
    }

    suspend fun shutDown() {
        if (::subscription.isInitialized) {
            subscription.unsubscribe()
        }
        log.info("Nats prioritization consumer listener shutdown complete")
    }

    private fun consumerService(selector: String): BlueprintNatsService {
        return bluePrintNatsLibPropertyService.bluePrintNatsService(selector)
    }

    private fun createMessageHandler(): MessageHandler {
        return MessageHandler { message ->
            try {
                val messagePrioritization = message.asJsonType().asType(MessagePrioritization::class.java)
                runBlocking {
                    natsMessagePrioritizationService.prioritize(messagePrioritization)
                }
            } catch (e: Exception) {
                log.error("failed to process prioritize message", e)
            }
        }
    }
}

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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.kafka

import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.To
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationStateService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessageState
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.ids
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.AbstractMessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.utils.MessageProcessorUtils
import org.onap.ccsdk.cds.controllerblueprints.core.logger

abstract class AbstractKafkaMessagePrioritizationService(
    private val messagePrioritizationStateService: MessagePrioritizationStateService
) : AbstractMessagePrioritizationService(messagePrioritizationStateService) {

    private val log = logger(AbstractKafkaMessagePrioritizationService::class)

    lateinit var processorContext: ProcessorContext

    fun setKafkaProcessorContext(processorContext: ProcessorContext) {
        this.processorContext = processorContext
    }

    override suspend fun output(messages: List<MessagePrioritization>) {
        log.info("$$$$$ received in output processor id(${messages.ids()})")
        checkNotNull(prioritizationConfiguration.kafkaConfiguration) { "failed to initialize kafka configuration" }
        check(::processorContext.isInitialized) { "failed to initialize kafka processor " }

        messages.forEach { message ->
            val updatedMessage =
                messagePrioritizationStateService.updateMessageState(message.id, MessageState.COMPLETED.name)
            processorContext.forward(
                updatedMessage.id,
                updatedMessage,
                To.child(MessagePrioritizationConstants.SINK_OUTPUT)
            )
        }
    }

    override suspend fun updateExpiredMessages() {
        checkNotNull(prioritizationConfiguration.kafkaConfiguration) { "failed to initialize kafka configuration" }
        check(::processorContext.isInitialized) { "failed to initialize kafka processor " }

        val expiryConfiguration = prioritizationConfiguration.expiryConfiguration
        val clusterLock = MessageProcessorUtils.prioritizationExpiryLock()
        try {
            val fetchMessages = messagePrioritizationStateService
                .getExpiryEligibleMessages(expiryConfiguration.maxPollRecord)
            val expiredIds = fetchMessages?.ids()
            if (expiredIds != null && expiredIds.isNotEmpty()) {
                messagePrioritizationStateService.updateMessagesState(expiredIds, MessageState.EXPIRED.name)
                fetchMessages.forEach { expiredMessage ->
                    expiredMessage.state = MessageState.EXPIRED.name
                    processorContext.forward(
                        expiredMessage.id, expiredMessage,
                        To.child(MessagePrioritizationConstants.SINK_OUTPUT)
                    )
                }
            }
        } catch (e: Exception) {
            log.error("failed in updating expired messages", e)
        } finally {
            MessageProcessorUtils.prioritizationUnLock(clusterLock)
        }
    }
}

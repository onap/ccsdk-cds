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

import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationStateService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessageState
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.ids
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.AbstractMessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.utils.MessageProcessorUtils
import org.onap.ccsdk.cds.blueprintsprocessor.nats.service.BlueprintNatsService
import org.onap.ccsdk.cds.blueprintsprocessor.nats.utils.NatsClusterUtils
import org.onap.ccsdk.cds.controllerblueprints.core.asByteArray
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.logger

abstract class AbstractNatsMessagePrioritizationService(
    private val messagePrioritizationStateService: MessagePrioritizationStateService
) : AbstractMessagePrioritizationService(messagePrioritizationStateService) {

    private val log = logger(AbstractNatsMessagePrioritizationService::class)

    lateinit var bluePrintNatsService: BlueprintNatsService

    override suspend fun output(messages: List<MessagePrioritization>) {
        log.info("$$$$$ received in output processor id(${messages.ids()})")
        checkNotNull(prioritizationConfiguration.natsConfiguration) { "failed to initialize NATS configuration" }
        check(::bluePrintNatsService.isInitialized) { "failed to initialize NATS services" }

        val outputSubject = prioritizationConfiguration.natsConfiguration!!.outputSubject
        messages.forEach { message ->
            val updatedMessage =
                messagePrioritizationStateService.updateMessageState(message.id, MessageState.COMPLETED.name)

            /** send to the output subject */
            bluePrintNatsService.publish(
                NatsClusterUtils.currentApplicationSubject(outputSubject),
                updatedMessage.asJsonType().asByteArray()
            )
        }
    }

    override suspend fun updateExpiredMessages() {
        checkNotNull(prioritizationConfiguration.natsConfiguration) { "failed to initialize NATS configuration" }
        check(::bluePrintNatsService.isInitialized) { "failed to initialize NATS services" }

        val expiryConfiguration = prioritizationConfiguration.expiryConfiguration
        val outputSubject = prioritizationConfiguration.natsConfiguration!!.expiredSubject
        val clusterLock = MessageProcessorUtils.prioritizationExpiryLock()
        try {
            val fetchMessages = messagePrioritizationStateService
                .getExpiryEligibleMessages(expiryConfiguration.maxPollRecord)
            val expiredIds = fetchMessages?.ids()
            if (!expiredIds.isNullOrEmpty()) {
                messagePrioritizationStateService.updateMessagesState(expiredIds, MessageState.EXPIRED.name)
                fetchMessages.forEach { expiredMessage ->
                    expiredMessage.state = MessageState.EXPIRED.name
                    /** send to the output subject */
                    bluePrintNatsService.publish(
                        NatsClusterUtils.currentApplicationSubject(outputSubject),
                        expiredMessage.asJsonType().asByteArray()
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

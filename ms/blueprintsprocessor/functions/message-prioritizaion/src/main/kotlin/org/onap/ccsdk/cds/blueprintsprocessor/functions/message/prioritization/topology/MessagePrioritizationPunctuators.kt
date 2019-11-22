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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.topology

import org.apache.kafka.streams.processor.To
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessageState
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.PrioritizationConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.MessagePrioritizationStateService
import org.onap.ccsdk.cds.blueprintsprocessor.message.kafka.AbstractBluePrintMessagePunctuator
import org.onap.ccsdk.cds.controllerblueprints.core.logger

class MessagePriorityExpiryPunctuator(private val messagePrioritizationStateService: MessagePrioritizationStateService) :
    AbstractBluePrintMessagePunctuator() {

    private val log = logger(MessagePriorityExpiryPunctuator::class)
    lateinit var configuration: PrioritizationConfiguration

    override suspend fun punctuateNB(timestamp: Long) {

        log.info(
            "**** executing expiry punctuator applicationId(${processorContext.applicationId()}), " +
                    "taskId(${processorContext.taskId()})"
        )
        val expiryConfiguration = configuration.expiryConfiguration
        val fetchMessages = messagePrioritizationStateService
            .getExpiryEligibleMessages(expiryConfiguration.maxPollRecord)

        val expiredIds = fetchMessages?.map { it.id }
        if (expiredIds != null && expiredIds.isNotEmpty()) {
            messagePrioritizationStateService.updateMessagesState(expiredIds, MessageState.EXPIRED.name)
            fetchMessages.forEach { expired ->
                processorContext.forward(
                    expired.id, expired,
                    To.child(MessagePrioritizationConstants.SINK_EXPIRED)
                )
            }
        }
    }
}

class MessagePriorityCleanPunctuator(private val messagePrioritizationStateService: MessagePrioritizationStateService) :
    AbstractBluePrintMessagePunctuator() {

    private val log = logger(MessagePriorityCleanPunctuator::class)
    lateinit var configuration: PrioritizationConfiguration

    override suspend fun punctuateNB(timestamp: Long) {
        log.info(
            "**** executing clean punctuator applicationId(${processorContext.applicationId()}), " +
                    "taskId(${processorContext.taskId()})"
        )
        // TODO
    }
}

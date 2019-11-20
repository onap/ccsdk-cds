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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.AbstractMessagePrioritizeProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessageState
import org.onap.ccsdk.cds.controllerblueprints.core.logger

open class MessageAggregateProcessor : AbstractMessagePrioritizeProcessor<String, String>() {

    private val log = logger(MessageAggregateProcessor::class)

    override suspend fun processNB(key: String, value: String) {

        log.info("@@@@@ received in aggregation processor key($key), value($value)")
        val ids = value.split(",").map { it.trim() }
        if (!ids.isNullOrEmpty()) {
            try {
                if (ids.size == 1) {
                    processorContext.forward(key, ids.first(), To.child(MessagePrioritizationConstants.PROCESSOR_OUTPUT))
                } else {
                    /** Implement Aggregation logic in overridden class, If necessary,
                    Populate New Message and Update status with Prioritized, Forward the message to next processor */
                    handleAggregation(ids)
                    /** Update all messages to Aggregated state */
                    messagePrioritizationStateService.setMessagesState(ids, MessageState.AGGREGATED.name)
                }
            } catch (e: Exception) {
                val error = "failed in Aggregate message($ids) : ${e.message}"
                log.error(error, e)
                val storeMessages = messagePrioritizationStateService.getMessages(ids)
                if (!storeMessages.isNullOrEmpty()) {
                    storeMessages.forEach { messagePrioritization ->
                        try {
                            /** Update the data store */
                            messagePrioritizationStateService.setMessageStateANdError(messagePrioritization.id,
                                MessageState.ERROR.name, error)
                            /** Publish to Error topic */
                            this.processorContext.forward(messagePrioritization.id, messagePrioritization,
                                To.child(MessagePrioritizationConstants.SINK_OUTPUT))
                        } catch (sendException: Exception) {
                            log.error("failed to update/publish error message(${messagePrioritization.id}) : " +
                                    "${sendException.message}", e)
                        }
                    }
                }
            }
        }
    }

    /** Child will override this implementation , if necessary */
    open suspend fun handleAggregation(messageIds: List<String>) {
        log.info("messages($messageIds) aggregated")
        messageIds.forEach { id ->
            processorContext.forward(id, id, To.child(MessagePrioritizationConstants.PROCESSOR_OUTPUT))
        }
    }
}

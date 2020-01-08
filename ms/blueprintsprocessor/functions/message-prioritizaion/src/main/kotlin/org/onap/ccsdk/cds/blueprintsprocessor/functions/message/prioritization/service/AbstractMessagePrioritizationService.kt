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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service

import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.To
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationStateService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessageState
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.utils.MessageCorrelationUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.utils.MessageProcessorUtils
import org.onap.ccsdk.cds.controllerblueprints.core.logger

/** Child should implement with sequencing & aggregation handling along with group type correlation mappings.*/
abstract class AbstractMessagePrioritizationService(
    private val messagePrioritizationStateService: MessagePrioritizationStateService
) : MessagePrioritizationService {

    private val log = logger(AbstractMessagePrioritizationService::class)

    var processorContext: ProcessorContext? = null

    override fun setKafkaProcessorContext(processorContext: ProcessorContext?) {
        this.processorContext = processorContext
    }

    override suspend fun prioritize(messagePrioritize: MessagePrioritization) {
        try {
            log.info("***** received in prioritize processor key(${messagePrioritize.id})")
            /** Get the cluster lock for message group */
            val clusterLock = MessageProcessorUtils.prioritizationGrouplock(messagePrioritize)
            // Save the Message
            messagePrioritizationStateService.saveMessage(messagePrioritize)
            handleCorrelationAndNextStep(messagePrioritize)
            /** Cluster unLock for message group */
            MessageProcessorUtils.prioritizationGroupUnLock(clusterLock)
        } catch (e: Exception) {
            messagePrioritize.error = "failed in Prioritize message(${messagePrioritize.id}) : ${e.message}"
            log.error(messagePrioritize.error)
            /** Update the data store */
            messagePrioritizationStateService.setMessageStateANdError(
                messagePrioritize.id, MessageState.ERROR.name,
                messagePrioritize.error!!
            )
        }
    }

    override suspend fun output(id: String) {
        log.info("$$$$$ received in output processor id($id)")
        val message = messagePrioritizationStateService.updateMessageState(id, MessageState.COMPLETED.name)
        /** Check for Kafka Processing, If yes, then send to the output topic */
        if (this.processorContext != null) {
            processorContext!!.forward(message.id, message, To.child(MessagePrioritizationConstants.SINK_OUTPUT))
        }
    }

    open suspend fun handleCorrelationAndNextStep(messagePrioritization: MessagePrioritization) {
        /** Check correlation enabled and correlation field has populated */
        if (!messagePrioritization.correlationId.isNullOrBlank()) {
            val id = messagePrioritization.id
            val group = messagePrioritization.group
            val correlationId = messagePrioritization.correlationId!!
            val types = getGroupCorrelationTypes(messagePrioritization)
            log.info(
                "checking correlation for message($id), group($group), types($types), " +
                    "correlation id($correlationId)"
            )

            /** Get all previously received messages from database for group and optional types and correlation Id */
            val waitingCorrelatedStoreMessages = messagePrioritizationStateService
                .getCorrelatedMessages(
                    group,
                    arrayListOf(MessageState.NEW.name, MessageState.WAIT.name), types, correlationId
                )

            /** If multiple records found, then check correlation */
            if (!waitingCorrelatedStoreMessages.isNullOrEmpty() && waitingCorrelatedStoreMessages.size > 1) {
                /** Check all correlation satisfies */
                val correlationResults = MessageCorrelationUtils
                    .correlatedMessagesWithTypes(waitingCorrelatedStoreMessages, types)

                if (correlationResults.correlated) {
                    /** Correlation  satisfied */
                    val correlatedIds = waitingCorrelatedStoreMessages.joinToString(",") { it.id }
                    /**  Send only correlated ids to aggregate processor */
                    aggregate(correlatedIds)
                } else {
                    /** Correlation not satisfied */
                    log.trace("correlation not matched : ${correlationResults.message}")
                    val waitMessageIds = waitingCorrelatedStoreMessages.map { it.id }
                    // Update the Message state to Wait
                    messagePrioritizationStateService.setMessagesState(waitMessageIds, MessageState.WAIT.name)
                }
            } else {
                /** received first message of group and correlation Id, update the message with wait state */
                messagePrioritizationStateService.setMessageState(messagePrioritization.id, MessageState.WAIT.name)
            }
        } else {
            // No Correlation check needed, simply forward to next processor.
            messagePrioritizationStateService.setMessageState(messagePrioritization.id, MessageState.PRIORITIZED.name)
            aggregate(messagePrioritization.id)
        }
    }

    open suspend fun aggregate(strIds: String) {
        log.info("@@@@@ received in aggregation processor ids($strIds)")
        val ids = strIds.split(",").map { it.trim() }
        if (!ids.isNullOrEmpty()) {
            try {
                if (ids.size == 1) {
                    /** No aggregation or sequencing needed, simpley forward to next processor */
                    output(ids.first())
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
                            messagePrioritizationStateService.setMessageStateANdError(
                                messagePrioritization.id,
                                MessageState.ERROR.name, error
                            )
                            /** Publish to output topic */
                            output(messagePrioritization.id)
                        } catch (sendException: Exception) {
                            log.error(
                                "failed to update/publish error message(${messagePrioritization.id}) : " +
                                    "${sendException.message}", e
                            )
                        }
                    }
                }
            }
        }
    }

    /** Child will override this implementation , if necessary
     *  Here the place child has to implement custom Sequencing and Aggregation logic.
     * */
    abstract suspend fun handleAggregation(messageIds: List<String>)

    /** If consumer wants specific correlation with respect to group and types, then populate the specific types,
     * otherwise correlation happens with group and correlationId */
    abstract fun getGroupCorrelationTypes(messagePrioritization: MessagePrioritization): List<String>?
}

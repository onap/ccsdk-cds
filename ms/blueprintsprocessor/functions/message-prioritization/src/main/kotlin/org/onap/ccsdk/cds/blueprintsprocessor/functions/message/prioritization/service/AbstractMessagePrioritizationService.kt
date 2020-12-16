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

import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationStateService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessageState
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.PrioritizationConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.ids
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.utils.MessageCorrelationUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.utils.MessageProcessorUtils
import org.onap.ccsdk.cds.controllerblueprints.core.logger

/** Child should implement with sequencing & aggregation handling along with group type correlation mappings.*/
abstract class AbstractMessagePrioritizationService(
    private val messagePrioritizationStateService: MessagePrioritizationStateService
) : MessagePrioritizationService {

    private val log = logger(AbstractMessagePrioritizationService::class)

    lateinit var prioritizationConfiguration: PrioritizationConfiguration

    override fun setConfiguration(prioritizationConfiguration: PrioritizationConfiguration) {
        this.prioritizationConfiguration = prioritizationConfiguration
    }

    override fun getConfiguration(): PrioritizationConfiguration {
        return this.prioritizationConfiguration
    }

    override suspend fun prioritize(messagePrioritize: MessagePrioritization) {
        try {
            log.info("***** received in prioritize processor key(${messagePrioritize.id})")
            check(::prioritizationConfiguration.isInitialized) { "failed to initialize prioritizationConfiguration " }

            /** Get the cluster lock for message group */
            val clusterLock = MessageProcessorUtils.prioritizationGrouplock(messagePrioritize)
            // Save the Message
            messagePrioritizationStateService.saveMessage(messagePrioritize)
            handleCorrelationAndNextStep(messagePrioritize)
            /** Cluster unLock for message group */
            MessageProcessorUtils.prioritizationUnLock(clusterLock)
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

    override suspend fun output(messages: List<MessagePrioritization>) {
        log.info("$$$$$ received in output processor id(${messages.ids()})")
        messages.forEach { message ->
            messagePrioritizationStateService.updateMessageState(message.id, MessageState.COMPLETED.name)
        }
    }

    override suspend fun updateExpiredMessages() {
        check(::prioritizationConfiguration.isInitialized) { "failed to initialize prioritizationConfiguration " }

        val expiryConfiguration = prioritizationConfiguration.expiryConfiguration
        val clusterLock = MessageProcessorUtils.prioritizationExpiryLock()
        try {
            val fetchMessages = messagePrioritizationStateService
                .getExpiryEligibleMessages(expiryConfiguration.maxPollRecord)
            val expiredIds = fetchMessages?.ids()
            if (!expiredIds.isNullOrEmpty()) {
                messagePrioritizationStateService.updateMessagesState(expiredIds, MessageState.EXPIRED.name)
            }
        } catch (e: Exception) {
            log.error("failed in updating expired messages", e)
        } finally {
            MessageProcessorUtils.prioritizationUnLock(clusterLock)
        }
    }

    override suspend fun cleanExpiredMessage() {
        check(::prioritizationConfiguration.isInitialized) { "failed to initialize prioritizationConfiguration " }

        val cleanConfiguration = prioritizationConfiguration.cleanConfiguration
        val clusterLock = MessageProcessorUtils.prioritizationCleanLock()
        try {
            messagePrioritizationStateService.deleteExpiredMessage(cleanConfiguration.expiredRecordsHoldDays)
        } catch (e: Exception) {
            log.error("failed in clean expired messages", e)
        } finally {
            MessageProcessorUtils.prioritizationUnLock(clusterLock)
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
                "checking correlation for message($id), group($group), type(${messagePrioritization.type}), " +
                    "correlation types($types), priority(${messagePrioritization.priority}), " +
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
                    /** Update all messages to Aggregated state */
                    messagePrioritizationStateService.setMessagesState(
                        waitingCorrelatedStoreMessages.ids(),
                        MessageState.PRIORITIZED.name
                    )
                    /** Correlation  satisfied, Send only correlated messages to aggregate processor */
                    aggregate(waitingCorrelatedStoreMessages)
                } else {
                    /** Correlation not satisfied */
                    log.trace("correlation not matched : ${correlationResults.message}")
                    // Update the Message state to Wait
                    messagePrioritizationStateService.setMessagesState(
                        waitingCorrelatedStoreMessages.ids(),
                        MessageState.WAIT.name
                    )
                }
            } else {
                /** received first message of group and correlation Id, update the message with wait state */
                messagePrioritizationStateService.setMessageState(messagePrioritization.id, MessageState.WAIT.name)
            }
        } else {
            /** No Correlation check needed, simply forward to next processor. */
            messagePrioritizationStateService.setMessageState(messagePrioritization.id, MessageState.PRIORITIZED.name)
            aggregate(arrayListOf(messagePrioritization))
        }
    }

    open suspend fun aggregate(messages: List<MessagePrioritization>) {
        log.info("@@@@@ received in aggregation processor ids(${messages.ids()}")
        if (!messages.isNullOrEmpty()) {
            try {
                /** Implement Aggregation logic in overridden class, If necessary,
                 Populate New Message and Update status with Prioritized, Forward the message to next processor */
                handleAggregation(messages)
            } catch (e: Exception) {
                val error = "failed in aggregate message(${messages.ids()}) : ${e.message}"
                if (!messages.isNullOrEmpty()) {
                    messages.forEach { messagePrioritization ->
                        try {
                            /** Update the data store */
                            messagePrioritizationStateService.setMessageStateANdError(
                                messagePrioritization.id,
                                MessageState.ERROR.name, error
                            )
                        } catch (sendException: Exception) {
                            log.error(
                                "failed to update/publish error message(${messagePrioritization.id}) : " +
                                    "${sendException.message}",
                                e
                            )
                        }
                    }
                    /** Publish to output topic */
                    output(messages)
                }
            }
        }
    }

    /** Child will override this implementation , if necessary
     *  Here the place child has to implement custom Sequencing and Aggregation logic.
     * */
    abstract suspend fun handleAggregation(messages: List<MessagePrioritization>)

    /** If consumer wants specific correlation with respect to group and types, then populate the specific types,
     * otherwise correlation happens with group and correlationId */
    abstract fun getGroupCorrelationTypes(messagePrioritization: MessagePrioritization): List<String>?
}

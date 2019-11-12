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

import org.apache.kafka.streams.processor.Cancellable
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.processor.To
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.AbstractMessagePrioritizeProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessageState
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.utils.MessageCorrelationUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import java.time.Duration
import java.util.*


open class MessagePrioritizeProcessor : AbstractMessagePrioritizeProcessor<ByteArray, ByteArray>() {

    private val log = logger(MessagePrioritizeProcessor::class)

    lateinit var expiryCancellable: Cancellable
    lateinit var cleanCancellable: Cancellable

    override suspend fun processNB(key: ByteArray, value: ByteArray) {
        log.info("***** received in prioritize processor key(${String(key)})")
        val data = JacksonUtils.readValue(String(value), MessagePrioritization::class.java)
                ?: throw BluePrintProcessorException("failed to convert")
        // Save the Message
        messagePrioritizationStateService.saveMessage(data)
        handleCorrelationAndNextStep(data)

    }

    override fun init(context: ProcessorContext) {
        super.init(context)
        /** set up expiry marking cron */
        initializeExpiryPunctuator()
        /** Set up cleaning records cron */
        initializeCleanPunctuator()
    }

    override fun close() {
        log.info("closing prioritization processor applicationId(${processorContext.applicationId()}), " +
                "taskId(${processorContext.taskId()})")
        expiryCancellable.cancel()
        cleanCancellable.cancel()
    }

    open fun initializeExpiryPunctuator() {
        val expiryPunctuator = MessagePriorityExpiryPunctuator(messagePrioritizationStateService)
        expiryPunctuator.processorContext = processorContext
        expiryPunctuator.configuration = prioritizationConfiguration
        val expiryConfiguration = prioritizationConfiguration.expiryConfiguration
        expiryCancellable = processorContext.schedule(Duration.ofMillis(expiryConfiguration.frequencyMilli),
                PunctuationType.WALL_CLOCK_TIME, expiryPunctuator)
        log.info("Expiry punctuator setup complete with frequency(${expiryConfiguration.frequencyMilli})mSec")
    }

    open fun initializeCleanPunctuator() {
        val cleanPunctuator = MessagePriorityCleanPunctuator(messagePrioritizationStateService)
        cleanPunctuator.processorContext = processorContext
        cleanPunctuator.configuration = prioritizationConfiguration
        val cleanConfiguration = prioritizationConfiguration.cleanConfiguration
        cleanCancellable = processorContext.schedule(Duration.ofDays(cleanConfiguration.expiredRecordsHoldDays.toLong()),
                PunctuationType.WALL_CLOCK_TIME, cleanPunctuator)
        log.info("Clean punctuator setup complete with expiry " +
                "hold(${cleanConfiguration.expiredRecordsHoldDays})days")
    }

    open suspend fun handleCorrelationAndNextStep(messagePrioritization: MessagePrioritization) {
        /** Check correlation enabled and correlation field has populated */
        if (!messagePrioritization.correlationId.isNullOrBlank()) {
            val id = messagePrioritization.id
            val group = messagePrioritization.group
            val correlationId = messagePrioritization.correlationId!!
            val types = getGroupCorrelationTypes(messagePrioritization)
            log.info("checking correlation for message($id), group($group), types($types), " +
                    "correlation id($correlationId)")

            /** Get all previously received messages from database for group and optional types and correlation Id */
            val storedCorrelatedMessage = messagePrioritizationStateService.getCorrelatedMessages(group,
                    types, correlationId)

            if (!storedCorrelatedMessage.isNullOrEmpty()) {
                /** Check all correlation satisfies */
                val correlationResults = MessageCorrelationUtils
                        .correlatedMessagesWithTypes(storedCorrelatedMessage, types)

                if (correlationResults.correlated) {
                    /** Correlation  satisfied */
                    val correlatedIds = storedCorrelatedMessage.map { it.id }.joinToString(",")
                    /**  Send only correlated ids to next processor */
                    this.processorContext.forward(UUID.randomUUID().toString(), correlatedIds,
                            To.child(MessagePrioritizationConstants.PROCESSOR_AGGREGATE))
                } else {
                    /** Correlation not satisfied */
                    val waitMessageIds = storedCorrelatedMessage.map { it.id }
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
            this.processorContext.forward(messagePrioritization.id, messagePrioritization.id,
                    To.child(MessagePrioritizationConstants.PROCESSOR_AGGREGATE))
        }
    }

    /** If consumer wants specific correlation with respect to group and types, then populate the specific types,
     * otherwise correlation happens with group and correlationId */
    open fun getGroupCorrelationTypes(messagePrioritization: MessagePrioritization): List<String>? {
        return null
    }
}
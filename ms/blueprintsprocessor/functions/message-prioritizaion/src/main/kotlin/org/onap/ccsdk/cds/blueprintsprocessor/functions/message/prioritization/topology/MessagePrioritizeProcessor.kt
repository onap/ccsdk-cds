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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import java.time.Duration


open class MessagePrioritizeProcessor : AbstractMessagePrioritizeProcessor<ByteArray, ByteArray>() {

    private val log = logger(MessagePrioritizeProcessor::class)

    lateinit var expiryCancellable: Cancellable
    lateinit var cleanCancellable: Cancellable

    override suspend fun processNB(key: ByteArray, value: ByteArray) {
        log.info("MessagePrioritizationProcessor key(${String(key)})")
        val data = JacksonUtils.readValue(String(value), MessagePrioritization::class.java)
                ?: throw BluePrintProcessorException("failed to convert")
        // Save the Message
        messagePrioritizationStateService.saveMessage(data)
        handleCorrelation(data)

    }

    override fun init(context: ProcessorContext) {
        super.init(context)

        val expiryPunctuator = MessagePriorityExpiryPunctuator(messagePrioritizationStateService)
        expiryPunctuator.processorContext = context
        expiryPunctuator.configuration = prioritizationConfiguration
        val expiryConfiguration = prioritizationConfiguration.expiryConfiguration
        expiryCancellable = context.schedule(Duration.ofMillis(expiryConfiguration.frequencyMilli),
                PunctuationType.WALL_CLOCK_TIME, expiryPunctuator)
        log.info("Expiry punctuator setup complete with frequency(${expiryConfiguration.frequencyMilli})mSec")

        val cleanPunctuator = MessagePriorityCleanPunctuator(messagePrioritizationStateService)
        cleanPunctuator.processorContext = context
        cleanPunctuator.configuration = prioritizationConfiguration
        val cleanConfiguration = prioritizationConfiguration.cleanConfiguration
        cleanCancellable = context.schedule(Duration.ofDays(cleanConfiguration.expiredRecordsHoldDays.toLong()),
                PunctuationType.WALL_CLOCK_TIME, cleanPunctuator)
        log.info("Clean punctuator setup complete with expiry " +
                "hold(${cleanConfiguration.expiredRecordsHoldDays})days")

    }

    override fun close() {
        log.info("closing prioritization processor applicationId(${processorContext.applicationId()}), " +
                "taskId(${processorContext.taskId()})")
        expiryCancellable.cancel()
        cleanCancellable.cancel()
    }

    open fun handleCorrelation(messagePrioritization: MessagePrioritization) {
        if (messagePrioritization.correlationId != null) {
            // Check the Correlation
            // Update Wait State
            // If correlation matched, then forward to next step, else Update the state to wait
        } else {
            // Send to next Node
            this.processorContext.forward(messagePrioritization.id, messagePrioritization,
                    To.child(MessagePrioritizationConstants.PROCESSOR_AGGREGATE))
        }
    }
}
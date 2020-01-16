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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationStateService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessageState
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils

open class DefaultMessagePrioritizeProcessor(
    private val messagePrioritizationStateService: MessagePrioritizationStateService,
    private val kafkaMessagePrioritizationService: MessagePrioritizationService
) : AbstractMessagePrioritizeProcessor<ByteArray, ByteArray>() {

    private val log = logger(DefaultMessagePrioritizeProcessor::class)

    override suspend fun processNB(key: ByteArray, value: ByteArray) {

        val messagePrioritize = JacksonUtils.readValue(String(value), MessagePrioritization::class.java)
            ?: throw BluePrintProcessorException("failed to convert")
        try {
            kafkaMessagePrioritizationService.prioritize(messagePrioritize)
        } catch (e: Exception) {
            messagePrioritize.error = "failed in Prioritize message(${messagePrioritize.id}) : ${e.message}"
            log.error(messagePrioritize.error)
            /** Update the data store */
            messagePrioritizationStateService.setMessageStateANdError(
                messagePrioritize.id, MessageState.ERROR.name,
                messagePrioritize.error!!
            )
            /** Publish to Output topic */
            this.processorContext.forward(
                messagePrioritize.id, messagePrioritize,
                To.child(MessagePrioritizationConstants.SINK_OUTPUT)
            )
        }
    }

    override fun init(context: ProcessorContext) {
        super.init(context)
        /** Set Configuration and Processor Context to messagePrioritizationService */
        if (kafkaMessagePrioritizationService is AbstractKafkaMessagePrioritizationService) {
            kafkaMessagePrioritizationService.setKafkaProcessorContext(processorContext)
        } else {
            throw BluePrintProcessorException(
                "messagePrioritizationService is not instance of " +
                    "AbstractKafkaMessagePrioritizationService, it is ${kafkaMessagePrioritizationService.javaClass}"
            )
        }
    }

    override fun close() {
        log.info(
            "closing prioritization processor applicationId(${processorContext.applicationId()}), " +
                "taskId(${processorContext.taskId()})"
        )
    }
}

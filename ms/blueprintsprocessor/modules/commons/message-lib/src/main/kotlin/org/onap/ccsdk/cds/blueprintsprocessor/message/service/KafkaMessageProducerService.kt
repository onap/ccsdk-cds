/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2018-2021 AT&T, Bell Canada Intellectual Property.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.message.service

import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang.builder.ToStringBuilder
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageProducerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.slf4j.LoggerFactory
import java.nio.charset.Charset

class KafkaMessageProducerService(
    private val messageProducerProperties: MessageProducerProperties
) :
    BlueprintMessageProducerService {

    private val log = LoggerFactory.getLogger(KafkaMessageProducerService::class.java)!!

    private var kafkaProducer: KafkaProducer<String, ByteArray>? = null

    private val messageLoggerService = MessageLoggerService()

    companion object {

        const val MAX_ERR_MSG_LEN = 128
    }

    override suspend fun sendMessageNB(key: String, message: Any, headers: MutableMap<String, String>?): Boolean {
        checkNotNull(messageProducerProperties.topic) { "default topic is not configured" }
        return sendMessageNB(key, messageProducerProperties.topic!!, message, headers)
    }

    override suspend fun sendMessageNB(
        key: String,
        topic: String,
        message: Any,
        headers: MutableMap<String, String>?
    ): Boolean {
        var clonedMessage = message
        if (clonedMessage is ExecutionServiceOutput) {
            clonedMessage = truncateResponse(clonedMessage)
        }

        val byteArrayMessage = when (clonedMessage) {
            is String -> clonedMessage.toByteArray(Charset.defaultCharset())
            else -> clonedMessage.asJsonString().toByteArray(Charset.defaultCharset())
        }

        val record = ProducerRecord<String, ByteArray>(topic, key, byteArrayMessage)
        val recordHeaders = record.headers()
        messageLoggerService.messageProducing(recordHeaders)
        headers?.let {
            headers.forEach { (key, value) -> recordHeaders.add(RecordHeader(key, value.toByteArray())) }
        }
        val callback = Callback { metadata, exception ->
            if (exception != null)
                log.error("Couldn't publish ${clonedMessage::class.simpleName} ${getMessageLogData(clonedMessage)}.", exception)
            else {
                val message = "${clonedMessage::class.simpleName} published : topic(${metadata.topic()}) " +
                    "partition(${metadata.partition()}) " +
                    "offset(${metadata.offset()}) ${getMessageLogData(clonedMessage)}."
                log.info(message)
            }
        }
        messageTemplate().send(record, callback)
        return true
    }

    fun messageTemplate(additionalConfig: Map<String, ByteArray>? = null): KafkaProducer<String, ByteArray> {
        log.trace("Producer client properties : ${ToStringBuilder.reflectionToString(messageProducerProperties)}")
        val configProps = messageProducerProperties.getConfig()

        /** Add additional Properties */
        if (additionalConfig != null)
            configProps.putAll(additionalConfig)

        if (kafkaProducer == null)
            kafkaProducer = KafkaProducer(configProps)

        return kafkaProducer!!
    }

    /**
     * Truncation of BP responses
     */
    private fun truncateResponse(executionServiceOutput: ExecutionServiceOutput): ExecutionServiceOutput {
        /** Truncation of error messages */
        var truncErrMsg = executionServiceOutput.status.errorMessage
        if (truncErrMsg != null && truncErrMsg.length > MAX_ERR_MSG_LEN) {
            truncErrMsg = truncErrMsg.substring(0, MAX_ERR_MSG_LEN) +
                " [...]. Check Blueprint Processor logs for more information."
        }
        /** Truncation for Command Executor responses */
        var truncPayload = executionServiceOutput.payload.deepCopy()
        val workflowName = executionServiceOutput.actionIdentifiers.actionName
        if (truncPayload.path("$workflowName-response").has("execute-command-logs")) {
            var cmdExecLogNode = truncPayload.path("$workflowName-response") as ObjectNode
            cmdExecLogNode.replace("execute-command-logs", "Check Command Executor logs for more information.".asJsonPrimitive())
        }
        return ExecutionServiceOutput().apply {
            correlationUUID = executionServiceOutput.correlationUUID
            commonHeader = executionServiceOutput.commonHeader
            actionIdentifiers = executionServiceOutput.actionIdentifiers
            status = Status().apply {
                code = executionServiceOutput.status.code
                eventType = executionServiceOutput.status.eventType
                timestamp = executionServiceOutput.status.timestamp
                errorMessage = truncErrMsg
                message = executionServiceOutput.status.message
            }
            payload = truncPayload
            stepData = executionServiceOutput.stepData
        }
    }

    private fun getMessageLogData(message: Any): String {
        return when (message) {
            is ExecutionServiceInput -> {
                val actionIdentifiers = message.actionIdentifiers
                "CBA(${actionIdentifiers.blueprintName}/${actionIdentifiers.blueprintVersion}/${actionIdentifiers.actionName})"
            }
            is ExecutionServiceOutput -> {
                val actionIdentifiers = message.actionIdentifiers
                "CBA(${actionIdentifiers.blueprintName}/${actionIdentifiers.blueprintVersion}/${actionIdentifiers.actionName})"
            }
            else -> "message($message)"
        }
    }
}

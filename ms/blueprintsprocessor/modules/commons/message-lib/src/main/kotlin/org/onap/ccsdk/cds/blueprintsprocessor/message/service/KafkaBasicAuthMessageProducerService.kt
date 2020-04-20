/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2018-2019 AT&T Intellectual Property.
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
import org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaBasicAuthMessageProducerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.defaultToUUID
import org.slf4j.LoggerFactory
import java.nio.charset.Charset

class KafkaBasicAuthMessageProducerService(
    private val messageProducerProperties: KafkaBasicAuthMessageProducerProperties
) :
    BlueprintMessageProducerService {

    private val log = LoggerFactory.getLogger(KafkaBasicAuthMessageProducerService::class.java)!!

    private var kafkaProducer: KafkaProducer<String, ByteArray>? = null

    private val messageLoggerService = MessageLoggerService()

    companion object {
        const val MAX_ERR_MSG_LEN = 128
    }

    override suspend fun sendMessageNB(message: Any): Boolean {
        checkNotNull(messageProducerProperties.topic) { "default topic is not configured" }
        return sendMessageNB(messageProducerProperties.topic!!, message)
    }

    override suspend fun sendMessageNB(message: Any, headers: MutableMap<String, String>?): Boolean {
        checkNotNull(messageProducerProperties.topic) { "default topic is not configured" }
        return sendMessageNB(messageProducerProperties.topic!!, message, headers)
    }

    override suspend fun sendMessageNB(
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

        val record = ProducerRecord<String, ByteArray>(topic, defaultToUUID(), byteArrayMessage)
        val recordHeaders = record.headers()
        messageLoggerService.messageProducing(recordHeaders)
        headers?.let {
            headers.forEach { (key, value) -> recordHeaders.add(RecordHeader(key, value.toByteArray())) }
        }
        val callback = Callback { metadata, exception ->
            log.trace("message published to(${metadata.topic()}), offset(${metadata.offset()}), headers :$headers")
        }
        messageTemplate().send(record, callback)
        return true
    }

    fun messageTemplate(additionalConfig: Map<String, ByteArray>? = null): KafkaProducer<String, ByteArray> {
        log.trace("Client Properties : ${ToStringBuilder.reflectionToString(messageProducerProperties)}")
        val configProps = hashMapOf<String, Any>()
        configProps[BOOTSTRAP_SERVERS_CONFIG] = messageProducerProperties.bootstrapServers
        configProps[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java
        configProps[ACKS_CONFIG] = messageProducerProperties.acks
        configProps[ENABLE_IDEMPOTENCE_CONFIG] = messageProducerProperties.enableIdempotence
        if (messageProducerProperties.clientId != null) {
            configProps[CLIENT_ID_CONFIG] = messageProducerProperties.clientId!!
        }
        // TODO("Security Implementation based on type")

        // Add additional Properties
        if (additionalConfig != null) {
            configProps.putAll(additionalConfig)
        }

        if (kafkaProducer == null) {
            kafkaProducer = KafkaProducer(configProps)
        }
        return kafkaProducer!!
    }

    /**
     * Truncation of BP responses
     */
    private fun truncateResponse(executionServiceOutput: ExecutionServiceOutput): ExecutionServiceOutput {
        /** Truncation of error messages */
        var truncErrMsg = executionServiceOutput.status.errorMessage
        if (truncErrMsg != null && truncErrMsg.length > MAX_ERR_MSG_LEN) {
            truncErrMsg = "${truncErrMsg.substring(0,MAX_ERR_MSG_LEN)}" +
                    " [...]. Check Blueprint Processor logs for more information."
        }
        /** Truncation for Command Executor responses */
        var truncPayload = executionServiceOutput.payload.deepCopy()
        if (truncPayload.path("execute-remote-python-response").has("execute-command-logs")) {
            var cmdExecLogNode = truncPayload.path("execute-remote-python-response") as ObjectNode
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
}

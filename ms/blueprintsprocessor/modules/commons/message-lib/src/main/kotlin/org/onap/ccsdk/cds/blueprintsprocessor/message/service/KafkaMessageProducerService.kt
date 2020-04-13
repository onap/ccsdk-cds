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

import org.apache.commons.lang.builder.ToStringBuilder
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageProducerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.defaultToUUID
import org.slf4j.LoggerFactory
import java.nio.charset.Charset

class KafkaMessageProducerService(
    private val messageProducerProperties: MessageProducerProperties
) :
    BlueprintMessageProducerService {

    private val log = LoggerFactory.getLogger(KafkaMessageProducerService::class.java)!!

    private var kafkaProducer: KafkaProducer<String, ByteArray>? = null

    private val messageLoggerService = MessageLoggerService()

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
        val byteArrayMessage = when (message) {
            is String -> message.toByteArray(Charset.defaultCharset())
            else -> message.asJsonString().toByteArray(Charset.defaultCharset())
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
        log.trace("Producer client properties : ${ToStringBuilder.reflectionToString(messageProducerProperties)}")
        val configProps = messageProducerProperties.getConfig()

        /** Add additional Properties */
        if (additionalConfig != null)
            configProps.putAll(additionalConfig)

        if (kafkaProducer == null)
            kafkaProducer = KafkaProducer(configProps)

        return kafkaProducer!!
    }
}

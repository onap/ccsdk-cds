/*
 *  Copyright © 2019 IBM.
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

import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.common.serialization.StringSerializer
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaBasicAuthMessageProducerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFutureCallback


class KafkaBasicAuthMessageProducerService(
        private val messageProducerProperties: KafkaBasicAuthMessageProducerProperties)
    : BlueprintMessageProducerService {

    private val log = LoggerFactory.getLogger(KafkaBasicAuthMessageProducerService::class.java)!!

    private var kafkaTemplate: KafkaTemplate<String, Any>? = null

    override suspend fun sendMessageNB(message: Any): String {
        checkNotNull(messageProducerProperties.topic) { "default topic is not configured" }
        return sendMessage(messageProducerProperties.topic!!, message)
    }

    override suspend fun sendMessageNB(topic: String, message: Any): String {
        val serializedMessage = when (message) {
            is String -> {
                message
            }
            else -> {
                message.asJsonType().toString()
            }
        }
        val future = messageTemplate().send(topic, serializedMessage)

        future.addCallback(object : ListenableFutureCallback<SendResult<String, Any>> {
            override fun onSuccess(result: SendResult<String, Any>) {
                log.info("message sent successfully with offset=[${result.recordMetadata.offset()}]")
            }

            override fun onFailure(ex: Throwable) {
                log.error("\"Unable to send message due to [${ex.message}]")
            }
        })
        return "Success"
    }


    private fun producerFactory(additionalConfig: Map<String, Any>? = null): ProducerFactory<String, Any> {
        log.info("Client Properties : $messageProducerProperties")
        val configProps = hashMapOf<String, Any>()
        configProps[BOOTSTRAP_SERVERS_CONFIG] = messageProducerProperties.bootstrapServers
        configProps[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        if (messageProducerProperties.clientId != null) {
            configProps[CLIENT_ID_CONFIG] = messageProducerProperties.clientId!!
        }
        // TODO("Security Implementation based on type")

        // Add additional Properties
        if (additionalConfig != null) {
            configProps.putAll(additionalConfig)
        }
        return DefaultKafkaProducerFactory(configProps)
    }

    fun messageTemplate(additionalConfig: Map<String, Any>? = null): KafkaTemplate<String, Any> {
        log.info("Prepering templates")
        if (kafkaTemplate == null) {
            kafkaTemplate = KafkaTemplate(producerFactory(additionalConfig))
        }
        return kafkaTemplate!!
    }
}


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

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaBasicAuthMessageProducerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageProducerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.stereotype.Service

@Service(MessageLibConstants.SERVICE_BLUEPRINT_MESSAGE_LIB_PROPERTY)
open class BluePrintMessageLibPropertyService(private var bluePrintProperties: BluePrintProperties) {

    fun blueprintMessageClientService(jsonNode: JsonNode): BlueprintMessageProducerService {
        val messageClientProperties = messageClientProperties(jsonNode)
        return blueprintMessageClientService(messageClientProperties)
    }

    fun blueprintMessageClientService(selector: String): BlueprintMessageProducerService {
        val prefix = "${MessageLibConstants.PROPERTY_MESSAGE_CLIENT_PREFIX}$selector"
        val messageClientProperties = messageClientProperties(prefix)
        return blueprintMessageClientService(messageClientProperties)
    }

    fun messageClientProperties(prefix: String): MessageProducerProperties {
        val type = bluePrintProperties.propertyBeanType("$prefix.type", String::class.java)
        return when (type) {
            MessageLibConstants.TYPE_KAFKA_BASIC_AUTH -> {
                kafkaBasicAuthMessageClientProperties(prefix)
            }
            else -> {
                throw BluePrintProcessorException("Message adaptor($type) is not supported")
            }
        }
    }

    fun messageClientProperties(jsonNode: JsonNode): MessageProducerProperties {
        val type = jsonNode.get("type").textValue()
        return when (type) {
            MessageLibConstants.TYPE_KAFKA_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode, KafkaBasicAuthMessageProducerProperties::class.java)!!
            }
            else -> {
                throw BluePrintProcessorException("Message adaptor($type) is not supported")
            }
        }
    }

    private fun blueprintMessageClientService(MessageProducerProperties: MessageProducerProperties)
            : BlueprintMessageProducerService {

        when (MessageProducerProperties) {
            is KafkaBasicAuthMessageProducerProperties -> {
                return KafkaBasicAuthMessageProducerService(MessageProducerProperties)
            }
            else -> {
                throw BluePrintProcessorException("couldn't get Message client service for")
            }
        }
    }

    private fun kafkaBasicAuthMessageClientProperties(prefix: String): KafkaBasicAuthMessageProducerProperties {
        return bluePrintProperties.propertyBeanType(
                prefix, KafkaBasicAuthMessageProducerProperties::class.java)
    }

}

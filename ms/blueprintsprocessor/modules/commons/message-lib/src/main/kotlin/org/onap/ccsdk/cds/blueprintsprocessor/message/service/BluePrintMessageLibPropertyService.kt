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

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaBasicAuthMessageConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaBasicAuthMessageProducerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaStreamsBasicAuthConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageProducerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.stereotype.Service

@Service(MessageLibConstants.SERVICE_BLUEPRINT_MESSAGE_LIB_PROPERTY)
open class BluePrintMessageLibPropertyService(private var bluePrintPropertiesService: BluePrintPropertiesService) {

    fun blueprintMessageProducerService(jsonNode: JsonNode): BlueprintMessageProducerService {
        val messageClientProperties = messageProducerProperties(jsonNode)
        return blueprintMessageProducerService(messageClientProperties)
    }

    fun blueprintMessageProducerService(selector: String): BlueprintMessageProducerService {
        val prefix = "${MessageLibConstants.PROPERTY_MESSAGE_PRODUCER_PREFIX}$selector"
        val messageClientProperties = messageProducerProperties(prefix)
        return blueprintMessageProducerService(messageClientProperties)
    }

    fun messageProducerProperties(prefix: String): MessageProducerProperties {
        val type = bluePrintPropertiesService.propertyBeanType("$prefix.type", String::class.java)
        return when (type) {
            MessageLibConstants.TYPE_KAFKA_BASIC_AUTH -> {
                kafkaBasicAuthMessageProducerProperties(prefix)
            }
            else -> {
                throw BluePrintProcessorException("Message adaptor($type) is not supported")
            }
        }
    }

    fun messageProducerProperties(jsonNode: JsonNode): MessageProducerProperties {
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

    private fun blueprintMessageProducerService(MessageProducerProperties: MessageProducerProperties):
            BlueprintMessageProducerService {

        when (MessageProducerProperties) {
            is KafkaBasicAuthMessageProducerProperties -> {
                return KafkaBasicAuthMessageProducerService(MessageProducerProperties)
            }
            else -> {
                throw BluePrintProcessorException("couldn't get Message client service for")
            }
        }
    }

    private fun kafkaBasicAuthMessageProducerProperties(prefix: String): KafkaBasicAuthMessageProducerProperties {
        return bluePrintPropertiesService.propertyBeanType(
            prefix, KafkaBasicAuthMessageProducerProperties::class.java)
    }

    /** Consumer Property Lib Service Implementation **/

    /** Return Message Consumer Service for [jsonNode] definitions. */
    fun blueprintMessageConsumerService(jsonNode: JsonNode): BlueprintMessageConsumerService {
        val messageConsumerProperties = messageConsumerProperties(jsonNode)
        return blueprintMessageConsumerService(messageConsumerProperties)
    }

    /** Return Message Consumer Service for [selector] definitions. */
    fun blueprintMessageConsumerService(selector: String): BlueprintMessageConsumerService {
        val prefix = "${MessageLibConstants.PROPERTY_MESSAGE_CONSUMER_PREFIX}$selector"
        val messageClientProperties = messageConsumerProperties(prefix)
        return blueprintMessageConsumerService(messageClientProperties)
    }

    /** Return Message Consumer Properties for [prefix] definitions. */
    fun messageConsumerProperties(prefix: String): MessageConsumerProperties {
        val type = bluePrintPropertiesService.propertyBeanType("$prefix.type", String::class.java)
        return when (type) {
            MessageLibConstants.TYPE_KAFKA_BASIC_AUTH -> {
                kafkaBasicAuthMessageConsumerProperties(prefix)
            }
            MessageLibConstants.TYPE_KAFKA_STREAMS_BASIC_AUTH -> {
                kafkaStreamsBasicAuthMessageConsumerProperties(prefix)
            }
            else -> {
                throw BluePrintProcessorException("Message adaptor($type) is not supported")
            }
        }
    }

    fun messageConsumerProperties(jsonNode: JsonNode): MessageConsumerProperties {
        val type = jsonNode.get("type").textValue()
        return when (type) {
            MessageLibConstants.TYPE_KAFKA_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode, KafkaBasicAuthMessageConsumerProperties::class.java)!!
            }
            MessageLibConstants.TYPE_KAFKA_STREAMS_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode, KafkaStreamsBasicAuthConsumerProperties::class.java)!!
            }
            else -> {
                throw BluePrintProcessorException("Message adaptor($type) is not supported")
            }
        }
    }

    private fun blueprintMessageConsumerService(messageConsumerProperties: MessageConsumerProperties):
            BlueprintMessageConsumerService {

        when (messageConsumerProperties) {
            is KafkaBasicAuthMessageConsumerProperties -> {
                return KafkaBasicAuthMessageConsumerService(messageConsumerProperties)
            }
            is KafkaStreamsBasicAuthConsumerProperties -> {
                return KafkaStreamsBasicAuthConsumerService(messageConsumerProperties)
            }
            else -> {
                throw BluePrintProcessorException("couldn't get Message client service for")
            }
        }
    }

    private fun kafkaBasicAuthMessageConsumerProperties(prefix: String): KafkaBasicAuthMessageConsumerProperties {
        return bluePrintPropertiesService.propertyBeanType(
            prefix, KafkaBasicAuthMessageConsumerProperties::class.java)
    }

    private fun kafkaStreamsBasicAuthMessageConsumerProperties(prefix: String): KafkaStreamsBasicAuthConsumerProperties {
        return bluePrintPropertiesService.propertyBeanType(
            prefix, KafkaStreamsBasicAuthConsumerProperties::class.java)
    }
}

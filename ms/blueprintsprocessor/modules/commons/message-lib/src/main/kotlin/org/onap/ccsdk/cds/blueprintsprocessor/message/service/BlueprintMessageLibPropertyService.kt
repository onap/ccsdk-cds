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
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaBasicAuthMessageConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaBasicAuthMessageProducerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaScramSslAuthMessageConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaScramSslAuthMessageProducerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaSslAuthMessageConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaSslAuthMessageProducerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaStreamsBasicAuthConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaStreamsScramSslAuthConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaStreamsSslAuthConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageProducerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.stereotype.Service

@Service(MessageLibConstants.SERVICE_BLUEPRINT_MESSAGE_LIB_PROPERTY)
open class BlueprintMessageLibPropertyService(private var bluePrintPropertiesService: BlueprintPropertiesService) {

    fun blueprintMessageProducerService(jsonNode: JsonNode): BlueprintMessageProducerService {
        val messageClientProperties = messageProducerProperties(jsonNode)
        return KafkaMessageProducerService(messageClientProperties)
    }

    fun blueprintMessageProducerService(selector: String): BlueprintMessageProducerService {
        val prefix = "${MessageLibConstants.PROPERTY_MESSAGE_PRODUCER_PREFIX}$selector"
        val messageClientProperties = messageProducerProperties(prefix)
        return KafkaMessageProducerService(messageClientProperties)
    }

    fun messageProducerProperties(prefix: String): MessageProducerProperties {
        val type = bluePrintPropertiesService.propertyBeanType("$prefix.type", String::class.java)
        return when (type) {
            MessageLibConstants.TYPE_KAFKA_BASIC_AUTH -> {
                bluePrintPropertiesService.propertyBeanType(
                    prefix, KafkaBasicAuthMessageProducerProperties::class.java
                )
            }
            MessageLibConstants.TYPE_KAFKA_SSL_AUTH -> {
                bluePrintPropertiesService.propertyBeanType(
                    prefix, KafkaSslAuthMessageProducerProperties::class.java
                )
            }
            MessageLibConstants.TYPE_KAFKA_SCRAM_SSL_AUTH -> {
                bluePrintPropertiesService.propertyBeanType(
                    prefix, KafkaScramSslAuthMessageProducerProperties::class.java
                )
            }
            else -> {
                throw BlueprintProcessorException("Message adaptor($type) is not supported")
            }
        }
    }

    fun messageProducerProperties(jsonNode: JsonNode): MessageProducerProperties {
        val type = jsonNode.get("type").textValue()
        return when (type) {
            MessageLibConstants.TYPE_KAFKA_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode, KafkaBasicAuthMessageProducerProperties::class.java)!!
            }
            MessageLibConstants.TYPE_KAFKA_SSL_AUTH -> {
                JacksonUtils.readValue(jsonNode, KafkaSslAuthMessageProducerProperties::class.java)!!
            }
            MessageLibConstants.TYPE_KAFKA_SCRAM_SSL_AUTH -> {
                JacksonUtils.readValue(jsonNode, KafkaScramSslAuthMessageProducerProperties::class.java)!!
            }
            else -> {
                throw BlueprintProcessorException("Message adaptor($type) is not supported")
            }
        }
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
            /** Message Consumer */
            MessageLibConstants.TYPE_KAFKA_BASIC_AUTH -> {
                bluePrintPropertiesService.propertyBeanType(
                    prefix, KafkaBasicAuthMessageConsumerProperties::class.java
                )
            }
            MessageLibConstants.TYPE_KAFKA_SSL_AUTH -> {
                bluePrintPropertiesService.propertyBeanType(
                    prefix, KafkaSslAuthMessageConsumerProperties::class.java
                )
            }
            MessageLibConstants.TYPE_KAFKA_SCRAM_SSL_AUTH -> {
                bluePrintPropertiesService.propertyBeanType(
                    prefix, KafkaScramSslAuthMessageConsumerProperties::class.java
                )
            }
            /** Stream Consumer */
            MessageLibConstants.TYPE_KAFKA_STREAMS_BASIC_AUTH -> {
                bluePrintPropertiesService.propertyBeanType(
                    prefix, KafkaStreamsBasicAuthConsumerProperties::class.java
                )
            }
            MessageLibConstants.TYPE_KAFKA_STREAMS_SSL_AUTH -> {
                bluePrintPropertiesService.propertyBeanType(
                    prefix, KafkaStreamsSslAuthConsumerProperties::class.java
                )
            }
            MessageLibConstants.TYPE_KAFKA_STREAMS_SCRAM_SSL_AUTH -> {
                bluePrintPropertiesService.propertyBeanType(
                    prefix, KafkaStreamsScramSslAuthConsumerProperties::class.java
                )
            }
            else -> {
                throw BlueprintProcessorException("Message adaptor($type) is not supported")
            }
        }
    }

    fun messageConsumerProperties(jsonNode: JsonNode): MessageConsumerProperties {
        val type = jsonNode.get("type").textValue()
        return when (type) {
            /** Message Consumer */
            MessageLibConstants.TYPE_KAFKA_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode, KafkaBasicAuthMessageConsumerProperties::class.java)!!
            }
            MessageLibConstants.TYPE_KAFKA_SSL_AUTH -> {
                JacksonUtils.readValue(jsonNode, KafkaSslAuthMessageConsumerProperties::class.java)!!
            }
            MessageLibConstants.TYPE_KAFKA_SCRAM_SSL_AUTH -> {
                JacksonUtils.readValue(jsonNode, KafkaScramSslAuthMessageConsumerProperties::class.java)!!
            }
            /** Stream Consumer */
            MessageLibConstants.TYPE_KAFKA_STREAMS_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode, KafkaStreamsBasicAuthConsumerProperties::class.java)!!
            }
            MessageLibConstants.TYPE_KAFKA_STREAMS_SSL_AUTH -> {
                JacksonUtils.readValue(jsonNode, KafkaStreamsSslAuthConsumerProperties::class.java)!!
            }
            MessageLibConstants.TYPE_KAFKA_STREAMS_SCRAM_SSL_AUTH -> {
                JacksonUtils.readValue(jsonNode, KafkaStreamsScramSslAuthConsumerProperties::class.java)!!
            }
            else -> {
                throw BlueprintProcessorException("Message adaptor($type) is not supported")
            }
        }
    }

    private fun blueprintMessageConsumerService(messageConsumerProperties: MessageConsumerProperties):
        BlueprintMessageConsumerService {

            when (messageConsumerProperties.type) {
                /** Message Consumer */
                MessageLibConstants.TYPE_KAFKA_BASIC_AUTH -> {
                    return KafkaMessageConsumerService(
                        messageConsumerProperties as KafkaBasicAuthMessageConsumerProperties
                    )
                }
                MessageLibConstants.TYPE_KAFKA_SSL_AUTH -> {
                    return KafkaMessageConsumerService(
                        messageConsumerProperties as KafkaSslAuthMessageConsumerProperties
                    )
                }
                MessageLibConstants.TYPE_KAFKA_SCRAM_SSL_AUTH -> {
                    return KafkaMessageConsumerService(
                        messageConsumerProperties as KafkaScramSslAuthMessageConsumerProperties
                    )
                }
                /** Stream Consumer */
                MessageLibConstants.TYPE_KAFKA_STREAMS_BASIC_AUTH -> {
                    return KafkaStreamsConsumerService(
                        messageConsumerProperties as KafkaStreamsBasicAuthConsumerProperties
                    )
                }
                MessageLibConstants.TYPE_KAFKA_STREAMS_SSL_AUTH -> {
                    return KafkaStreamsConsumerService(
                        messageConsumerProperties as KafkaStreamsSslAuthConsumerProperties
                    )
                }
                MessageLibConstants.TYPE_KAFKA_STREAMS_SCRAM_SSL_AUTH -> {
                    return KafkaStreamsConsumerService(
                        messageConsumerProperties as KafkaStreamsScramSslAuthConsumerProperties
                    )
                }
                else -> {
                    throw BlueprintProcessorException("couldn't get message client service for ${messageConsumerProperties.type}")
                }
            }
        }
}

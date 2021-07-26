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

package org.onap.ccsdk.cds.blueprintsprocessor.message

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BluePrintMessageLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BlueprintMessageConsumerService
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BlueprintMessageProducerService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
@EnableConfigurationProperties
open class BluePrintMessageLibConfiguration

/**
 * Exposed Dependency Service by this Message Lib Module
 */
fun BluePrintDependencyService.messageLibPropertyService(): BluePrintMessageLibPropertyService =
    instance(MessageLibConstants.SERVICE_BLUEPRINT_MESSAGE_LIB_PROPERTY)

/** Extension functions for message producer service **/
fun BluePrintDependencyService.messageProducerService(selector: String): BlueprintMessageProducerService {
    return messageLibPropertyService().blueprintMessageProducerService(selector)
}

fun BluePrintDependencyService.messageProducerService(jsonNode: JsonNode): BlueprintMessageProducerService {
    return messageLibPropertyService().blueprintMessageProducerService(jsonNode)
}

/** Extension functions for message consumer service **/
fun BluePrintDependencyService.messageConsumerService(selector: String): BlueprintMessageConsumerService {
    return messageLibPropertyService().blueprintMessageConsumerService(selector)
}

fun BluePrintDependencyService.messageConsumerService(jsonNode: JsonNode): BlueprintMessageConsumerService {
    return messageLibPropertyService().blueprintMessageConsumerService(jsonNode)
}

class MessageLibConstants {
    companion object {

        const val SERVICE_BLUEPRINT_MESSAGE_LIB_PROPERTY = "blueprint-message-lib-property-service"
        const val PROPERTY_MESSAGE_CONSUMER_PREFIX = "blueprintsprocessor.messageconsumer."
        const val PROPERTY_MESSAGE_PRODUCER_PREFIX = "blueprintsprocessor.messageproducer."
        const val TYPE_KAFKA_BASIC_AUTH = "kafka-basic-auth"
        const val TYPE_KAFKA_SCRAM_SSL_AUTH = "kafka-scram-ssl-auth"
        const val TYPE_KAFKA_SSL_AUTH = "kafka-ssl-auth"
        const val TYPE_KAFKA_STREAMS_BASIC_AUTH = "kafka-streams-basic-auth"
        const val TYPE_KAFKA_STREAMS_SSL_AUTH = "kafka-streams-ssl-auth"
        const val TYPE_KAFKA_STREAMS_SCRAM_SSL_AUTH = "kafka-streams-scram-ssl-auth"
    }
}

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

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.Topology
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.PrioritizationConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.utils.MessageProcessorUtils.bluePrintProcessorSupplier
import org.onap.ccsdk.cds.blueprintsprocessor.message.KafkaStreamsBasicAuthConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageConsumerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BlueprintMessageLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BlueprintMessageConsumerService
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.KafkaStreamConsumerFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.splitCommaAsList

open class KafkaMessagePrioritizationConsumer(
    private val bluePrintMessageLibPropertyService: BlueprintMessageLibPropertyService,
    private val kafkaMessagePrioritizationService: MessagePrioritizationService
) {

    private val log = logger(KafkaMessagePrioritizationConsumer::class)

    private lateinit var streamingConsumerService: BlueprintMessageConsumerService

    open fun consumerService(selector: String): BlueprintMessageConsumerService {
        return bluePrintMessageLibPropertyService
            .blueprintMessageConsumerService(selector)
    }

    open fun kafkaStreamConsumerFunction(prioritizationConfiguration: PrioritizationConfiguration):
        KafkaStreamConsumerFunction {
            return object : KafkaStreamConsumerFunction {

                val kafkaConsumerConfiguration = prioritizationConfiguration.kafkaConfiguration
                    ?: throw BlueprintProcessorException("failed to get kafka consumer configuration")

                override suspend fun createTopology(
                    messageConsumerProperties: MessageConsumerProperties,
                    additionalConfig: Map<String, Any>?
                ): Topology {

                    val topology = Topology()
                    val kafkaStreamsBasicAuthConsumerProperties = messageConsumerProperties
                        as KafkaStreamsBasicAuthConsumerProperties

                    val topics = kafkaStreamsBasicAuthConsumerProperties.topic.splitCommaAsList()
                    log.info("Consuming prioritization topics($topics)")

                    topology.addSource(MessagePrioritizationConstants.SOURCE_INPUT, *topics.toTypedArray())

                    topology.addProcessor(
                        MessagePrioritizationConstants.PROCESSOR_PRIORITIZE,
                        bluePrintProcessorSupplier<ByteArray, ByteArray>(
                            MessagePrioritizationConstants.PROCESSOR_PRIORITIZE
                        ),
                        MessagePrioritizationConstants.SOURCE_INPUT
                    )

                    /** To receive completed and error messages */
                    topology.addSink(
                        MessagePrioritizationConstants.SINK_OUTPUT,
                        kafkaConsumerConfiguration.outputTopic,
                        Serdes.String().serializer(), MessagePrioritizationSerde().serializer(),
                        MessagePrioritizationConstants.PROCESSOR_PRIORITIZE
                    )

                    // Output will be sent to the group-output topic from Processor API
                    return topology
                }
            }
        }

    suspend fun startConsuming(prioritizationConfiguration: PrioritizationConfiguration) {

        val kafkaConsumerConfiguration = prioritizationConfiguration.kafkaConfiguration
            ?: throw BlueprintProcessorException("failed to get kafka consumer configuration")

        streamingConsumerService = consumerService(kafkaConsumerConfiguration.inputTopicSelector)

        // Dynamic Consumer Function to create Topology
        val consumerFunction = kafkaStreamConsumerFunction(prioritizationConfiguration)
        streamingConsumerService.consume(null, consumerFunction)
    }

    suspend fun shutDown() {
        if (::streamingConsumerService.isInitialized) {
            streamingConsumerService.shutDown()
        }
    }
}

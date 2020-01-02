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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization

import org.apache.kafka.streams.processor.ProcessorContext
import org.onap.ccsdk.cds.blueprintsprocessor.atomix.clusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.MessagePrioritizationStateService
import org.onap.ccsdk.cds.blueprintsprocessor.message.kafka.AbstractBluePrintMessageProcessor
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService

/** CDS Message Prioritazation Kafka Stream Processor abstract class to implement */
abstract class AbstractMessagePrioritizeProcessor<K, V> : AbstractBluePrintMessageProcessor<K, V>() {

    private val log = logger(AbstractMessagePrioritizeProcessor::class)

    lateinit var prioritizationConfiguration: PrioritizationConfiguration
    lateinit var messagePrioritizationStateService: MessagePrioritizationStateService
    var clusterService: BluePrintClusterService? = null

    override fun init(context: ProcessorContext) {
        this.processorContext = context
        /** Get the State service to update in store */
        this.messagePrioritizationStateService = BluePrintDependencyService
            .messagePrioritizationStateService()
    }

    /** Cluster Service is not enabled by default for all processors, In needed initialize from processor init method */
    open fun initializeClusterService() {
        /** Get the Cluster service to update in store */
        if (BluePrintConstants.CLUSTER_ENABLED) {
            this.clusterService = BluePrintDependencyService.clusterService()
        }
    }
}

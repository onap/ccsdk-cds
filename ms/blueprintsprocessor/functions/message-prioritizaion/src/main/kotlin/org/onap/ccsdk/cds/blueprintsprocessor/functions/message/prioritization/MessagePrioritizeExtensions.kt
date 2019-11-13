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

import org.apache.kafka.streams.processor.ProcessorSupplier
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService


fun <K, V> bluePrintProcessorSupplier(name: String, prioritizationConfiguration: PrioritizationConfiguration)
        : ProcessorSupplier<K, V> {
    return ProcessorSupplier<K, V> {
        // Dynamically resolve the Prioritization Processor
        val processorInstance = BluePrintDependencyService.instance<AbstractMessagePrioritizeProcessor<K, V>>(name)
        processorInstance.prioritizationConfiguration = prioritizationConfiguration
        processorInstance
    }
}

fun MessagePrioritization.toFormatedCorrelation(): String {
    val ascendingKey = this.correlationId!!.split(",")
            .map { it.trim() }.sorted().joinToString(",")
    return ascendingKey
}

fun MessagePrioritization.toTypeNCorrelation(): TypeCorrelationKey {
    val ascendingKey = this.correlationId!!.split(",")
            .map { it.trim() }.sorted().joinToString(",")
    return TypeCorrelationKey(this.type, ascendingKey)
}

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

import org.apache.kafka.streams.processor.ProcessorContext
import org.onap.ccsdk.cds.blueprintsprocessor.message.kafka.AbstractBlueprintMessageProcessor

/** CDS Message Prioritization Kafka Stream Processor abstract class to implement */
abstract class AbstractMessagePrioritizeProcessor<K, V> : AbstractBlueprintMessageProcessor<K, V>() {

    override fun init(processorContext: ProcessorContext) {
        this.processorContext = processorContext
    }
}

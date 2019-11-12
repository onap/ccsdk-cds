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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.topology

import org.apache.kafka.streams.processor.To
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.AbstractBluePrintMessageProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.MessagePrioritizationService
import org.onap.ccsdk.cds.controllerblueprints.core.logger


open class MessageAggregateProcessor(private val messagePrioritizationService: MessagePrioritizationService)
    : AbstractBluePrintMessageProcessor<String, MessagePrioritization>() {

    private val log = logger(MessageAggregateProcessor::class)

    override suspend fun processNB(key: String, value: MessagePrioritization) {
        //TODO(Implement Aggregation logic in overridden class, If necessary )
        processorContext.forward(value.id, value, To.child(MessagePrioritizationConstants.PROCESSOR_OUTPUT))
    }
}
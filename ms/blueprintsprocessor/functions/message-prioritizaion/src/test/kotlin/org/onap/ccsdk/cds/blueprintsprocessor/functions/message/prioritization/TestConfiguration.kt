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

import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.MessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.topology.MessageAggregateProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.topology.MessageOutputProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.topology.MessagePrioritizeProcessor
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service

@Configuration
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db"])
@EnableAutoConfiguration
open class TestDatabaseConfiguration

@Service(MessagePrioritizationConstants.PROCESSOR_PRIORITIZE)
open class TestMessagePrioritizeProcessor(messagePrioritizationService: MessagePrioritizationService)
    : MessagePrioritizeProcessor(messagePrioritizationService)

@Service(MessagePrioritizationConstants.PROCESSOR_AGGREGATE)
open class DefaultMessageAggregateProcessor(messagePrioritizationService: MessagePrioritizationService)
    : MessageAggregateProcessor(messagePrioritizationService)

@Service(MessagePrioritizationConstants.PROCESSOR_OUTPUT)
open class DefaultMessageOutputProcessor(messagePrioritizationService: MessagePrioritizationService)
    : MessageOutputProcessor(messagePrioritizationService)
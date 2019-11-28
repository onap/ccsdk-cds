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

import org.onap.ccsdk.cds.blueprintsprocessor.db.PrimaryDBLibGenericService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.topology.MessageAggregateProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.topology.MessageOutputProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.topology.MessagePrioritizeProcessor
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import javax.sql.DataSource

@Configuration
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db"])
@EnableAutoConfiguration
open class TestDatabaseConfiguration {

    @Bean("primaryDBLibGenericService")
    open fun primaryDBLibGenericService(dataSource: DataSource): PrimaryDBLibGenericService {
        return PrimaryDBLibGenericService(
            NamedParameterJdbcTemplate(dataSource)
        )
    }
}

@Service(MessagePrioritizationConstants.PROCESSOR_PRIORITIZE)
open class TestMessagePrioritizeProcessor : MessagePrioritizeProcessor() {

    override fun getGroupCorrelationTypes(messagePrioritization: MessagePrioritization): List<String>? {
        return when (messagePrioritization.group) {
            "group-typed" -> arrayListOf("type-0", "type-1", "type-2")
            else -> null
        }
    }
}

@Service(MessagePrioritizationConstants.PROCESSOR_AGGREGATE)
open class DefaultMessageAggregateProcessor() : MessageAggregateProcessor()

@Service(MessagePrioritizationConstants.PROCESSOR_OUTPUT)
open class DefaultMessageOutputProcessor : MessageOutputProcessor()

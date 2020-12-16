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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.SampleMessagePrioritizationService
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

/* Sample Prioritization Listener, used during Application startup
@Component
open class SamplePrioritizationListeners(private val defaultMessagePrioritizationConsumer: MessagePrioritizationConsumer) {

    private val log = logger(SamplePrioritizationListeners::class)

    @EventListener(ApplicationReadyEvent::class)
    open fun init() = runBlocking {
        log.info("Starting PrioritizationListeners...")
        defaultMessagePrioritizationConsumer
            .startConsuming(MessagePrioritizationSample.samplePrioritizationConfiguration())
    }

    @PreDestroy
    open fun destroy() = runBlocking {
        log.info("Shutting down PrioritizationListeners...")
        defaultMessagePrioritizationConsumer.shutDown()
    }
}
 */

@Service
open class TestMessagePrioritizationService(messagePrioritizationStateService: MessagePrioritizationStateService) :
    SampleMessagePrioritizationService(messagePrioritizationStateService)

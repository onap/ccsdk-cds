/*
 * Copyright © 2020 Bell Canada
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
package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

/**
 * Default audit service when no audit publisher is defined, message aren't sent
 */
@ConditionalOnProperty(
        name = ["blueprintsprocessor.messageconsumer.self-service-api.kafkaEnable"],
        havingValue = "false"
)
@Service
class NoPublishAuditService : PublishAuditService {

    val log = logger(NoPublishAuditService::class)

    override suspend fun publish(executionServiceInput: ExecutionServiceInput, bluePrintRuntimeService: BluePrintRuntimeService<*>?) {
        log.info("No audit publisher defined")
    }
    override fun publish(executionServiceOutput: ExecutionServiceOutput) {
        log.info("No audit publisher defined")
    }
}

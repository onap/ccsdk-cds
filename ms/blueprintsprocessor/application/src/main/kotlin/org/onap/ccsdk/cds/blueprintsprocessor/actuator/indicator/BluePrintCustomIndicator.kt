/*
 * Copyright © 2019-2020 Orange.
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

package org.onap.ccsdk.cds.blueprintsprocessor.actuator.indicator

import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health.BluePrintProcessorHealthCheck
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.stereotype.Component

/**
 * Health Indicator for BluePrintProcessor.
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@Component
open class BluePrintCustomIndicator(private val bluePrintProcessorHealthCheck: BluePrintProcessorHealthCheck) :
    AbstractHealthIndicator() {

    private var logger = LoggerFactory.getLogger(BluePrintCustomIndicator::class.java)

    @Throws(Exception::class)
    override fun doHealthCheck(builder: Health.Builder) {
        runBlocking {
            var result: HealthApiResponse? = null
            try {
                result = bluePrintProcessorHealthCheck!!.retrieveEndpointExecutionStatus()
                if (result?.status == HealthCheckStatus.UP) {
                    builder.up()
                } else {
                    builder.down()
                }
                builder.withDetail("Services", result?.checks)
            } catch (exception: IllegalArgumentException) {
                logger.error(exception.message)
            }
        }
    }
}

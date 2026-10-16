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
package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration.HealthCheckProperties
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ApplicationHealth
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.actuate.health.CompositeHealth
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.boot.actuate.health.Status
import org.springframework.stereotype.Service

/**
 *Service for combined health (BluePrintProcessor and CDSListener)
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@Service
open class CombinedHealthService(
    private val endPointExecution: EndPointExecution,
    private val healthCheckProperties: HealthCheckProperties,
    private val healthEndpoint: ObjectProvider<HealthEndpoint>
) {

    open fun getCombinedHealthCheck(): List<ApplicationHealth?> {
        return listOf(getBluePrintProcessorHealth(), getCDSListenerHealth())
    }

    private fun getBluePrintProcessorHealth(): ApplicationHealth {
        val health = healthEndpoint.ifAvailable?.health()
            ?: return downHealth(healthCheckProperties.getBluePrintBaseURL() + "actuator/health")
        val details = when (health) {
            is CompositeHealth -> health.components
            is Health -> health.details
            else -> emptyMap()
        }
        return ApplicationHealth(health.status, details)
    }

    private fun getCDSListenerHealth(): ApplicationHealth? {
        val serviceEndpoint =
            ServiceEndpoint("CDSListener Health Check", healthCheckProperties.getCDSListenerBaseURL() + "actuator/health")
        val result = endPointExecution.retrieveWebClientResponse(serviceEndpoint)
        if (result?.response?.status == 200) {
            return endPointExecution.getHealthFromWebClientEnpointResponse(result)
        }
        return downHealth(serviceEndpoint.serviceLink)
    }

    private fun downHealth(serviceLink: String): ApplicationHealth {
        return ApplicationHealth(Status.DOWN, hashMapOf(serviceLink to serviceLink))
    }
}

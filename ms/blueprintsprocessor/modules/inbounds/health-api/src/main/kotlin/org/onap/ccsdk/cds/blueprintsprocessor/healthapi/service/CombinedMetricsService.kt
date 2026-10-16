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
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ActuatorCheckResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.Metrics
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.MetricsInfo
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.MetricsResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.utils.ObjectMappingUtils
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.actuate.metrics.MetricsEndpoint
import org.springframework.stereotype.Service

/**
 *Service for combined Metrics for CDS Listener and BluePrintProcessor
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@Service
open class CombinedMetricsService(
    private val endPointExecution: EndPointExecution,
    private val healthCheckProperties: HealthCheckProperties,
    private val objectMappingUtils: ObjectMappingUtils<Metrics>,
    private val metricsEndpoint: ObjectProvider<MetricsEndpoint>
) {

    open val metricsInfo: MetricsInfo
        get() = MetricsInfo(listOf(getBluePrintProcessorMetrics(), getCDSListenerMetrics()))

    private fun getBluePrintProcessorMetrics(): ActuatorCheckResponse {
        val serviceEndpoint =
            ServiceEndpoint("BluePrintProcessor metrics", healthCheckProperties.getBluePrintBaseURL() + "/actuator/metrics")
        val names = metricsEndpoint.ifAvailable?.listNames()?.names
            ?: return ActuatorCheckResponse(serviceEndpoint.serviceName, HealthCheckStatus.DOWN)
        return ActuatorCheckResponse(serviceEndpoint.serviceName, gettingCustomizedBody(serviceEndpoint, names))
    }

    private fun getCDSListenerMetrics(): ActuatorCheckResponse {
        val serviceEndpoint =
            ServiceEndpoint("CDS Listener metrics", healthCheckProperties.getCDSListenerBaseURL() + "/actuator/metrics")
        val webClientResponse = endPointExecution.retrieveWebClientResponse(serviceEndpoint)?.response
        if (webClientResponse?.status != 200) {
            return ActuatorCheckResponse(serviceEndpoint.serviceName, HealthCheckStatus.DOWN)
        }
        val metrics: Metrics = objectMappingUtils.getObjectFromBody(webClientResponse.body, Metrics::class.java)
        return ActuatorCheckResponse(serviceEndpoint.serviceName, gettingCustomizedBody(serviceEndpoint, metrics.names!!))
    }

    private fun gettingCustomizedBody(serviceEndpoint: ServiceEndpoint, names: Collection<Any>): MetricsResponse {
        val mapOfMetricsInfo = HashMap<String, String>()
        for (name in names) {
            mapOfMetricsInfo.put(name.toString(), serviceEndpoint.serviceLink + "/" + name)
        }
        return MetricsResponse(mapOfMetricsInfo)
    }
}

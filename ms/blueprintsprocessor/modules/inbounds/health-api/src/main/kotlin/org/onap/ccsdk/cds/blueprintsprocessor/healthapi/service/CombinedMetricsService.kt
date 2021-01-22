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
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.springframework.stereotype.Service

/**
 *Service for combined Metrics for CDS Listener and BlueprintProcessor
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@Service
open class CombinedMetricsService(
    private val endPointExecution: EndPointExecution,
    private val healthCheckProperties: HealthCheckProperties,
    private val objectMappingUtils: ObjectMappingUtils<Metrics>
) {

    private fun setupServiceEndpoint(): List<ServiceEndpoint> {
        return listOf(
            ServiceEndpoint("BlueprintProcessor metrics", healthCheckProperties.getBlueprintBaseURL() + "/actuator/metrics"),
            ServiceEndpoint("CDS Listener metrics", healthCheckProperties.getCDSListenerBaseURL() + "/actuator/metrics")
        )
    }

    open val metricsInfo: MetricsInfo
        get() {
            val containerHealthChecks = mutableListOf<ActuatorCheckResponse>()
            for (serviceEndpoint in setupServiceEndpoint().parallelStream()) {
                val webClientResponse = endPointExecution?.retrieveWebClientResponse(serviceEndpoint)
                var actuatorsHealthResponse: ActuatorCheckResponse? = null
                actuatorsHealthResponse = if (webClientResponse?.response != null &&
                    webClientResponse.response!!.status?.equals(200)!!
                ) {
                    var body = gettingCustomizedBody(serviceEndpoint, webClientResponse.response!!)
                    ActuatorCheckResponse(serviceEndpoint.serviceName, body)
                } else {
                    ActuatorCheckResponse(serviceEndpoint.serviceName, HealthCheckStatus.DOWN)
                }
                containerHealthChecks.add(actuatorsHealthResponse)
            }
            return MetricsInfo(containerHealthChecks)
        }

    private fun gettingCustomizedBody(
        serviceEndpoint: ServiceEndpoint?,
        webClientResponse: BlueprintWebClientService.WebClientResponse<String>
    ): Any {
        var body: Any
        val metrics: Metrics = objectMappingUtils.getObjectFromBody(webClientResponse.body, Metrics::class.java)
        val mapOfMetricsInfo = HashMap<String, String>()
        for (name in metrics.names!!) {
            mapOfMetricsInfo.put(name.toString(), serviceEndpoint?.serviceLink + "/" + name)
        }
        body = MetricsResponse(mapOfMetricsInfo)

        return body
    }
}

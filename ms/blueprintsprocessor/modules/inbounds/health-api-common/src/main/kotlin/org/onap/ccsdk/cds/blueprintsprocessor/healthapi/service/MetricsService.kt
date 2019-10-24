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

import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.*
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MetricsService {

    @Autowired
    private val endPointExecution: EndPointExecution? = null

    private fun setupServiceEndpoint(): List<ServiceEndpoint> {
        return listOf(
                ServiceEndpoint("BluePrintProcessor metrics", "http://cds-sdc-listener:8080/actuator/metrics")

        )
    }

    val metricsInfo: MetricsInfo
        get() {
            val containerHealthChecks = mutableListOf<ActuatorCheckResponse>()
            for (serviceEndpoint in setupServiceEndpoint().parallelStream()) {
                val webClientResponse = endPointExecution?.retrieveWebClientResponse(serviceEndpoint)
                var actuatorsHealthResponse: ActuatorCheckResponse? = null
                actuatorsHealthResponse = if (webClientResponse != null &&
                        webClientResponse.response!=null &&
                        webClientResponse.response.status?.equals(200)!!) {
                    var body = gettingCustomizedBody(serviceEndpoint, webClientResponse.response)
                    ActuatorCheckResponse(
                            serviceEndpoint.serviceName,
                            body)
                } else {
                    ActuatorCheckResponse(
                            serviceEndpoint.serviceName,
                            HealthCheckStatus.DOWN)
                }

                containerHealthChecks.add(actuatorsHealthResponse)
            }
            return MetricsInfo(containerHealthChecks)
        }

    private fun gettingCustomizedBody(serviceEndpoint: ServiceEndpoint?, webClientResponse: BlueprintWebClientService.WebClientResponse<String>): Any {
        var body: Any

        val metrics: Metrics = mappingMetricsToDTO(webClientResponse.body)
        val mapOfMetricsInfo = HashMap<String, String>()
        for (name in metrics.names!!) {
            mapOfMetricsInfo.put(name.toString(), serviceEndpoint?.serviceLink + "/" + name)
        }
        body = MetricsResponse(mapOfMetricsInfo)

        return body
    }

    private fun mappingMetricsToDTO(body: String): Metrics {
        return ObjectMapper().readValue(body, Metrics::class.java)
    }

}

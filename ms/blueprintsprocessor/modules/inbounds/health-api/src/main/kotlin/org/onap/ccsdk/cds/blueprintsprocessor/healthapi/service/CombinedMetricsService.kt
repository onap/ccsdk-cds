package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration.HealthCheckProperties
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.*
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.springframework.stereotype.Service

/**
 *Service for combined Metrics for CDS Listener and BluePrintProcessor
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@Service
open class CombinedMetricsService(private val endPointExecution: EndPointExecution
                                  , private val healthCheckProperties: HealthCheckProperties) {

    private fun setupServiceEndpoint(): List<ServiceEndpoint> {
        return listOf(
                ServiceEndpoint("BluePrintProcessor metrics", healthCheckProperties.getBluePrintBaseURL() + "/actuator/metrics")
                , ServiceEndpoint("CDS Listener metrics", healthCheckProperties.getCDSListenerBaseURL() + "/actuator/metrics")

        )
    }

    open val metricsInfo: MetricsInfo
        get() {
            val containerHealthChecks = mutableListOf<ActuatorCheckResponse>()
            for (serviceEndpoint in setupServiceEndpoint().parallelStream()) {
                val webClientResponse = endPointExecution?.retrieveWebClientResponse(serviceEndpoint)
                var actuatorsHealthResponse: ActuatorCheckResponse? = null
                actuatorsHealthResponse = if (webClientResponse != null &&
                        webClientResponse.response != null &&
                        webClientResponse.response!!.status?.equals(200)!!) {
                    var body = gettingCustomizedBody(serviceEndpoint, webClientResponse.response!!)
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

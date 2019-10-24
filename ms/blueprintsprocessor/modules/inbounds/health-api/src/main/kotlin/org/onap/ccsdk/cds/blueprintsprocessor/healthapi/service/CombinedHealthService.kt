package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration.HealthCheckProperties
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ApplicationHealth
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.WebClientEnpointResponse
import org.springframework.boot.actuate.health.Status
import org.springframework.stereotype.Service


/**
 *Service for combined health (BluePrintProcessor and CDSListener)
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@Service
open class CombinedHealthService(private val endPointExecution: EndPointExecution
                                 , private val healthCheckProperties: HealthCheckProperties) {

    private fun setupServiceEndpoint(): List<ServiceEndpoint> {
        return listOf(
                ServiceEndpoint("BluePrintProcessor Health Check ", healthCheckProperties.getBluePrintBaseURL() + "actuator/health")
                , ServiceEndpoint("CDSListener Health Check", healthCheckProperties.getCDSListenerBaseURL() + "actuator/health")
        )
    }


    open fun getCombinedHealthCheck(): MutableList<ApplicationHealth?> {
        val listOfResponse = mutableListOf<ApplicationHealth?>()
        for (serviceEndpoint in setupServiceEndpoint().parallelStream()) {
            val result: WebClientEnpointResponse? = endPointExecution?.retrieveWebClientResponse(serviceEndpoint)
            if (result != null) {
                listOfResponse.add(endPointExecution?.getHealthFromWebClientEnpointResponse(result))
            } else {
                val mapOfResult = kotlin.collections.HashMap<kotlin.String, kotlin.Any>()
                mapOfResult.put(serviceEndpoint.serviceLink, serviceEndpoint.serviceLink)
                listOfResponse.add(ApplicationHealth(Status.DOWN, mapOfResult))
            }
        }

        return listOfResponse
    }

}

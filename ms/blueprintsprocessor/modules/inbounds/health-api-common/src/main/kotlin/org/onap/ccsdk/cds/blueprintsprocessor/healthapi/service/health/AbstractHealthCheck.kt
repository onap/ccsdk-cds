package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.*
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.EndPointExecution
import org.slf4j.LoggerFactory

abstract class AbstractHealthCheck (private val  endPointExecution: EndPointExecution) {

    private var logger = LoggerFactory.getLogger(BluePrintProcessorHealthCheck::class.java)

    private fun retrieveSystemStatus(list: List<ServiceEndpoint>): HealthApiResponse {
        val healthApiResponse: HealthApiResponse
        val listOfResponse = mutableListOf<ServicesCheckResponse>()
        var systemStatus: HealthCheckStatus = HealthCheckStatus.UP

        for (serviceEndpoint in list) {
            val serviceStatus: HealthCheckStatus = retrieveServiceStatus(serviceEndpoint)
            if (serviceStatus.equals(HealthCheckStatus.DOWN))
                systemStatus = HealthCheckStatus.DOWN

            listOfResponse.add(ServicesCheckResponse(serviceEndpoint.serviceName, serviceStatus))
        }
        healthApiResponse = HealthApiResponse(systemStatus, listOfResponse)
        return healthApiResponse

    }


    private fun retrieveServiceStatus(serviceEndpoint: ServiceEndpoint): HealthCheckStatus {
        var serviceStatus: HealthCheckStatus = HealthCheckStatus.UP
        try {
            val result: WebClientEnpointResponse? = endPointExecution?.retrieveWebClientResponse(serviceEndpoint)
            if (result == null || result.response?.status != 200) {
                serviceStatus = HealthCheckStatus.DOWN

            }
        } catch (e: Exception) {
            logger.error("service name ${serviceEndpoint.serviceName} is down ${e.message}")
            serviceStatus = HealthCheckStatus.DOWN

        }
        return serviceStatus
    }


    open fun retrieveEndpointExecutionStatus(): HealthApiResponse {
        return retrieveSystemStatus(setupServiceEndpoint())
    }

    abstract fun setupServiceEndpoint(): List<ServiceEndpoint>

}

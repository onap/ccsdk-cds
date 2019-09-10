package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*


@Service
class HealthCheckService {

    @Autowired
    lateinit var restTemplate: RestTemplate

    fun setupServiceEndpoint(): List<ServiceEndpoint> {
        return Arrays.asList(ServiceEndpoint("execution-service-health-check", "http://cds-blueprints-processor-http:8080/api/v1/execution-service/health-check"),
                ServiceEndpoint("Template-health-check", "http://cds-blueprints-processor-http:8080/api/v1/template/health-check"),
                ServiceEndpoint("resources-health-check", "http://cds-blueprints-processor-http:8080/api/v1/resources/health-check"),
                ServiceEndpoint("sdclistener-health-check", "http://cds-sdc-listener:8080/api/v1/sdclistener/health-check")
        )
    }

    fun execute(): HealthApiResponse {
        var healthApiResponse: HealthApiResponse
        val listOfResponse = mutableListOf<HealthCheckResponse>()
        var systemStatus: HealthCheckStatus = HealthCheckStatus.UP

        for (serviceEndpoint in setupServiceEndpoint().parallelStream()) {
            var serviceStatus: HealthCheckStatus = checkServiceStatus(serviceEndpoint)

            if (serviceStatus.equals(HealthCheckStatus.DOWN))
                systemStatus = HealthCheckStatus.DOWN

            listOfResponse.add(HealthCheckResponse(serviceEndpoint.serviceName, serviceStatus))
        }
        healthApiResponse = HealthApiResponse(systemStatus, listOfResponse)

        return healthApiResponse
    }

    private fun checkServiceStatus(link: ServiceEndpoint): HealthCheckStatus {
        var serviceStatus: HealthCheckStatus = HealthCheckStatus.UP
        try {
            val result: ResponseEntity<String>? = restTemplate.exchange(link.serviceLink, HttpMethod.GET, null, String::class.java)
            if (result == null || !result.statusCode.is2xxSuccessful) {
                serviceStatus = HealthCheckStatus.DOWN

            }
        } catch (e: Exception) {
            serviceStatus = HealthCheckStatus.DOWN

        }
        return serviceStatus
    }


}

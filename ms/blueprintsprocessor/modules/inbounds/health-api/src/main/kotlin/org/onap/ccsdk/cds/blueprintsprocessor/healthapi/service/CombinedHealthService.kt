package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ApplicationHealth
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.WebClientEnpointResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Status
import org.springframework.stereotype.Service

@Service
open class CombinedHealthService {

    @Autowired
    private val endPointExecution: EndPointExecution? = null


    fun setupServiceEndpoint(): List<ServiceEndpoint> {

        return listOf(
                ServiceEndpoint("BluePrintProcessor Health Check ", "http://cds-blueprints-processor-http:8080/actuator/health")
                ,ServiceEndpoint("CDSListener Health Check", "http://cds-sdc-listener:8080/actuator/health")
        )
    }


    open fun getCombinedHealthCheck(): MutableList<ApplicationHealth?> {
        val listOfResponse = mutableListOf<ApplicationHealth?>()

        for (serviceEndpoint in setupServiceEndpoint().parallelStream()) {
            val result: WebClientEnpointResponse? = endPointExecution?.retrieveWebClientResponse(serviceEndpoint)

            if (result != null) {
                listOfResponse.add(endPointExecution?.getHealthFromWebClientEnpointResponse(result))
            }else{
                val mapOfResult = kotlin.collections.HashMap<kotlin.String, kotlin.Any>()
                mapOfResult.put(serviceEndpoint.serviceLink,serviceEndpoint.serviceLink)
                listOfResponse.add(ApplicationHealth(Status.DOWN, mapOfResult))
            }
        }

        return listOfResponse
    }

}

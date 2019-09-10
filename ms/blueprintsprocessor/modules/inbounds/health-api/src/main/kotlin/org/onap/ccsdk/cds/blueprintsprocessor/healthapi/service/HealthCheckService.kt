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

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service


@Service
class HealthCheckService {

    private var logger = LoggerFactory.getLogger(HealthCheckService::class.java)

    @Autowired
    lateinit var basicAuthRestClientService: BasicAuthRestClientService

    @Autowired
    lateinit var restClientProperties: BasicAuthRestClientProperties


    open fun setupServiceEndpoint(): List<ServiceEndpoint> {
        return listOf(ServiceEndpoint("Execution service", "http://cds-blueprints-processor-http:8080/api/v1/execution-service/health-check"),
                ServiceEndpoint("Template service", "http://cds-blueprints-processor-http:8080/api/v1/template/health-check"),
                ServiceEndpoint("Resources service", "http://cds-blueprints-processor-http:8080/api/v1/resources/health-check"),
                ServiceEndpoint("SDC Listener service", "http://cds-sdc-listener:8080/api/v1/sdclistener/health-check")
        )
    }

    fun retrieveSystemStatus(): HealthApiResponse {
        logger.info("Retrieve System Status")
        var healthApiResponse: HealthApiResponse
        val listOfResponse = mutableListOf<HealthCheckResponse>()
        var systemStatus: HealthCheckStatus = HealthCheckStatus.UP

        for (serviceEndpoint in setupServiceEndpoint().parallelStream()) {
            var serviceStatus: HealthCheckStatus = retrieveServiceStatus(serviceEndpoint)
            if (serviceStatus.equals(HealthCheckStatus.DOWN))
                systemStatus = HealthCheckStatus.DOWN

            listOfResponse.add(HealthCheckResponse(serviceEndpoint.serviceName, serviceStatus))
        }
        healthApiResponse = HealthApiResponse(systemStatus, listOfResponse)
        return healthApiResponse
    }

    private fun retrieveServiceStatus(serviceEndpoint: ServiceEndpoint): HealthCheckStatus {
        var serviceStatus: HealthCheckStatus = HealthCheckStatus.UP
        try {
            addClientPropertiesConfiguration(serviceEndpoint)
            val result: BlueprintWebClientService.WebClientResponse<String> = basicAuthRestClientService.exchangeResource(HttpMethod.GET.name, "", "")
            if (result == null || result.status != 200) {
                serviceStatus = HealthCheckStatus.DOWN

            }
        } catch (e: Exception) {
            logger.error("service is down" + e)
            serviceStatus = HealthCheckStatus.DOWN

        }
        return serviceStatus
    }

    private fun addClientPropertiesConfiguration(serviceEndpoint: ServiceEndpoint) {
        restClientProperties.url = serviceEndpoint.serviceLink
    }


}

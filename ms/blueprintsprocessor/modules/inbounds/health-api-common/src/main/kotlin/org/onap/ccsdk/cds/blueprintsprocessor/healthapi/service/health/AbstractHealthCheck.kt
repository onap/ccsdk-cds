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

package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServicesCheckResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.WebClientEnpointResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.EndPointExecution
import org.slf4j.LoggerFactory

/**
 *Abstract class to execute provided Service Endpoint from class that extends it
 * by implementing setupServiceEndpoint method
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
abstract class AbstractHealthCheck(private val endPointExecution: EndPointExecution) {

    private var logger = LoggerFactory.getLogger(BlueprintProcessorHealthCheck::class.java)

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

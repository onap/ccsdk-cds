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
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ApplicationHealth
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.WebClientEnpointResponse
import org.springframework.boot.actuate.health.Status
import org.springframework.stereotype.Service

/**
 *Service for combined health (BlueprintProcessor and CDSListener)
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@Service
open class CombinedHealthService(
    private val endPointExecution: EndPointExecution,
    private val healthCheckProperties: HealthCheckProperties
) {

    private fun setupServiceEndpoint(): List<ServiceEndpoint> {
        return listOf(
            ServiceEndpoint("BlueprintProcessor Health Check ", healthCheckProperties.getBlueprintBaseURL() + "actuator/health"),
            ServiceEndpoint("CDSListener Health Check", healthCheckProperties.getCDSListenerBaseURL() + "actuator/health")
        )
    }

    open fun getCombinedHealthCheck(): List<ApplicationHealth?> {
        val listOfResponse = ArrayList<ApplicationHealth?>()
        for (serviceEndpoint in setupServiceEndpoint().parallelStream()) {
            val result: WebClientEnpointResponse? = endPointExecution?.retrieveWebClientResponse(serviceEndpoint)
            if (result?.response != null &&
                result.response!!.status?.equals(200)!!
            ) {
                listOfResponse.add(endPointExecution?.getHealthFromWebClientEnpointResponse(result))
            } else {
                listOfResponse.add(
                    ApplicationHealth(
                        Status.DOWN,
                        hashMapOf(serviceEndpoint.serviceLink to serviceEndpoint.serviceLink)
                    )
                )
            }
        }
        return listOfResponse
    }
}

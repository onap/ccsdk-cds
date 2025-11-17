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
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ApplicationHealth
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.WebClientEnpointResponse
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service

/**
 * Service for executing services endpoint with rest-lib project .
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@Service
open class EndPointExecution(
    private val basicAuthRestClientService: BasicAuthRestClientService,
    private val restClientProperties: BasicAuthRestClientProperties
) {

    private var logger = LoggerFactory.getLogger(EndPointExecution::class.java)

    open fun retrieveWebClientResponse(serviceEndpoint: ServiceEndpoint): WebClientEnpointResponse? {
        try {
            addClientPropertiesConfiguration(serviceEndpoint)
            val result = basicAuthRestClientService.exchangeResource(HttpMethod.GET.name(), "", "")
            if (result.status == 200)
                return WebClientEnpointResponse(result)
        } catch (e: Exception) {
            logger.error("service name ${serviceEndpoint.serviceName} is down ${e.message}")
        }
        return WebClientEnpointResponse(BlueprintWebClientService.WebClientResponse(500, ""))
    }

    private fun addClientPropertiesConfiguration(serviceEndpoint: ServiceEndpoint) {
        restClientProperties.url = serviceEndpoint.serviceLink
    }

    open fun getHealthFromWebClientEnpointResponse(webClientEnpointResponse: WebClientEnpointResponse): ApplicationHealth? {
        return mappingMetricsToDTO(webClientEnpointResponse?.response?.body.toString())
    }

    private fun mappingMetricsToDTO(body: String): ApplicationHealth {
        return ObjectMapper().readValue(body, ApplicationHealth::class.java)
    }
}

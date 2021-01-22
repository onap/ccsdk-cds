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

package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceName
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:application.properties")
open class HealthCheckProperties {

    @Value("\${blueprintprocessor.healthcheck.baseUrl:}")
    private val bluePrintProcessorBaseURL: String? = null

    @Value("#{'\${blueprintprocessor.healthcheck.mapping-service-name-with-service-link:}'.split(']')}")
    private val blueprintprocessorServiceMapping: List<String>? = null

    @Value("\${cdslistener.healthcheck.baseUrl:}")
    private val cdsListenerBaseURL: String? = null

    @Value("#{'\${cdslistener.healthcheck.mapping-service-name-with-service-link:}'.split(']')}")
    private val cdsListenerServiceMapping: List<String>? = null

    open fun getBlueprintBaseURL(): String? {
        return bluePrintProcessorBaseURL
    }

    open fun getCDSListenerBaseURL(): String? {
        return cdsListenerBaseURL
    }

    open fun getBlueprintServiceInformation(): List<ServiceEndpoint> {
        val serviceName = ServiceName.BLUEPRINT
        return getListOfServiceEndPoints(blueprintprocessorServiceMapping, serviceName)
    }

    open fun getCDSListenerServiceInformation(): List<ServiceEndpoint> {
        val serviceName = ServiceName.CDSLISTENER
        return getListOfServiceEndPoints(cdsListenerServiceMapping, serviceName)
    }

    private fun getListOfServiceEndPoints(serviceMapping: List<String>?, serviceName: ServiceName): MutableList<ServiceEndpoint> {
        val serviceEndpoints = mutableListOf<ServiceEndpoint>()
        if (serviceMapping != null) {
            for (element in serviceMapping) {
                fillListOfService(serviceName, element, serviceEndpoints)
            }
        }
        return serviceEndpoints
    }

    private fun fillListOfService(serviceName: ServiceName, element: String, listOfCDSListenerServiceEndpoint: MutableList<ServiceEndpoint>) {
        val serviceEndpointInfo = element.split(",/")
        val serviceEndpoint = getServiceEndpoint(serviceEndpointInfo)
        if (serviceName.equals(ServiceName.CDSLISTENER))
            serviceEndpoint.serviceLink = cdsListenerBaseURL + serviceEndpoint.serviceLink
        else if (serviceName.equals(ServiceName.BLUEPRINT))
            serviceEndpoint.serviceLink = bluePrintProcessorBaseURL + serviceEndpoint.serviceLink
        listOfCDSListenerServiceEndpoint.add(serviceEndpoint)
    }

    private fun getServiceEndpoint(serviceEndpointInfo: List<String>): ServiceEndpoint {
        return ServiceEndpoint(
            removeSpecialCharacter(serviceEndpointInfo[0]), removeSpecialCharacter(serviceEndpointInfo[1])
        )
    }

    private fun removeSpecialCharacter(value: String): String {
        return value.replaceFirst(",[", "")
            .replace("[", "")
            .replace("]", "")
    }
}

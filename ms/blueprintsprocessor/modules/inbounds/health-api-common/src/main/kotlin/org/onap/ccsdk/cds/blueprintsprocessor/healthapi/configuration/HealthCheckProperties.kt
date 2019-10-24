package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:application.properties")
open class HealthCheckProperties {

    @Value("\${blueprintprocessor.healthcheck.baseUrl}")
    private val bluePrintProcessorBaseURL: String? = null

    @Value("#{'\${blueprintprocessor.healthcheck.mapping-service-name-with-service-link}'.split(']')}")
    private val blueprintprocessorServiceMapping: List<String>? = null

    @Value("\${cdslistener.healthcheck.baseUrl}")
    private val cdsListenerBaseURL: String? = null

    @Value("#{'\${cdslistener.healthcheck.mapping-service-name-with-service-link}'.split(']')}")
    private val cdsListenerServiceMapping: List<String>? = null

    open fun getBluePrintBaseURL(): String? {
        return bluePrintProcessorBaseURL
    }

    open fun getCDSListenerBaseURL(): String? {
        return cdsListenerBaseURL
    }

    open fun getBluePrintServiceInformation(): List<ServiceEndpoint> {
        return  getListOfBluePrintServicesEndpoint(blueprintprocessorServiceMapping)
    }

    open fun getCDSListenerServiceInformation(): List<ServiceEndpoint> {
        return getListOfCDSListenerServicesEndpoint(blueprintprocessorServiceMapping)
    }

    private fun getListOfBluePrintServicesEndpoint(blueprintprocessorServiceMapping: List<String>?)
            : MutableList<ServiceEndpoint> {
        val listOfBluePrintServiceEndpoint = mutableListOf<ServiceEndpoint>()
        if (blueprintprocessorServiceMapping != null) {
            for (element in blueprintprocessorServiceMapping) {
                val serviceEndpointInfo = element.split(",/")
                val serviceEndpoint = getBLuePrintProcessorServiceEndpoint(serviceEndpointInfo)
                listOfBluePrintServiceEndpoint.add(serviceEndpoint)
            }
        }
        return listOfBluePrintServiceEndpoint
    }

    private fun getListOfCDSListenerServicesEndpoint(blueprintprocessorServiceMapping: List<String>?)
            : MutableList<ServiceEndpoint> {
        val listOfCDSListenerServiceEndpoint = mutableListOf<ServiceEndpoint>()
        if (blueprintprocessorServiceMapping != null) {
            for (element in blueprintprocessorServiceMapping) {
                val serviceEndpointInfo = element.split(",/")
                val serviceEndpoint = getCDSListenerServiceEndpoint(serviceEndpointInfo)
                listOfCDSListenerServiceEndpoint.add(serviceEndpoint)
            }
        }
        return listOfCDSListenerServiceEndpoint
    }


    private fun getCDSListenerServiceEndpoint(serviceEndpointInfo: List<String>): ServiceEndpoint {
        return ServiceEndpoint(removeSpecialCharacter(serviceEndpointInfo.get(0))
                , cdsListenerBaseURL + removeSpecialCharacter(serviceEndpointInfo.get(1))
        )
    }

    private fun getBLuePrintProcessorServiceEndpoint(serviceEndpointInfo: List<String>): ServiceEndpoint {
        return ServiceEndpoint(removeSpecialCharacter(serviceEndpointInfo.get(0))
                , bluePrintProcessorBaseURL + removeSpecialCharacter(serviceEndpointInfo.get(1))
                )
    }

    private fun removeSpecialCharacter(value:String):String{
        return value.replaceFirst(",[","")
                .replace("[","")
                .replace("]","")
    }
}

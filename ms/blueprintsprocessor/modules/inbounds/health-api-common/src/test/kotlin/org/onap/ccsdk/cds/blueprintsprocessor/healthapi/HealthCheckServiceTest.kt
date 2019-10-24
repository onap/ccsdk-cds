package org.onap.ccsdk.cds.blueprintsprocessor.healthapi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock

import java.util.Arrays
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration.HealthCheckProperties
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.EndPointExecution
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health.BluePrintProcessorHealthCheck
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService.WebClientResponse
import org.springframework.http.HttpMethod

@RunWith(MockitoJUnitRunner::class)
class HealthCheckServiceTest {

    @Mock
    private val basicAuthRestClientService: BasicAuthRestClientService? = null

    @Mock
    private val restClientProperties: BasicAuthRestClientProperties? = null

    @Mock
    private val healthCheckProperties: HealthCheckProperties? = null

    @InjectMocks
    private var endPointExecution: EndPointExecution? = null

    private var bluePrintProcessorHealthCheck: BluePrintProcessorHealthCheck? = null


    @Before
    fun setup() {
        endPointExecution = Mockito.spy(endPointExecution!!)
        Mockito.`when`(healthCheckProperties!!.getBluePrintServiceInformation()).thenReturn(Arrays.asList(
                ServiceEndpoint("Execution service ", "http://cds-blueprints-processor-http:8080/api/v1/execution-service/health-check"),
                ServiceEndpoint("Resources service", "http://cds-blueprints-processor-http:8080/api/v1/resources/health-check"), ServiceEndpoint("Template service", "http://cds-blueprints-processor-http:8080/api/v1/template/health-check")
        ))

        bluePrintProcessorHealthCheck = BluePrintProcessorHealthCheck(endPointExecution!!, healthCheckProperties)
    }

  @Test
    fun testSystemIsCompletelyDown() {

        Mockito.`when`(basicAuthRestClientService!!.exchangeResource(
                        anyString(),
                        anyString(),
                        anyString())).thenThrow(RuntimeException())
        val healthApiResponse = bluePrintProcessorHealthCheck!!.retrieveEndpointExecutionStatus()
        assertNotNull(healthApiResponse)
        Assert.assertEquals(HealthCheckStatus.DOWN, healthApiResponse.status)
        healthApiResponse.checks.forEach { serviceEndpoint ->
            assertNotNull(serviceEndpoint)
            assertEquals(HealthCheckStatus.DOWN, serviceEndpoint.status)

        }

    }

    @Test
    fun testSystemIsUPAndRunning() {

        Mockito.`when`(basicAuthRestClientService!!
        .exchangeResource(
                        anyString(),
                        anyString(),
                        anyString())).thenReturn(BlueprintWebClientService.WebClientResponse(200, "Success"))
        val healthApiResponse = bluePrintProcessorHealthCheck!!.retrieveEndpointExecutionStatus()
        assertNotNull(healthApiResponse)
        assertEquals(HealthCheckStatus.UP, healthApiResponse.status)
        healthApiResponse.checks.forEach { serviceEndpoint ->
            assertNotNull(serviceEndpoint)
            assertEquals(HealthCheckStatus.UP, serviceEndpoint.status)

        }

    }

    @Test
    fun testServiceIsNotFound() {
        Mockito.`when`(basicAuthRestClientService!!.exchangeResource(
                        anyString(),
                        anyString(),
                        anyString())).thenReturn(BlueprintWebClientService.WebClientResponse(404, "failure"))
        val healthApiResponse = bluePrintProcessorHealthCheck!!.retrieveEndpointExecutionStatus()
        assertNotNull(healthApiResponse)
        assertEquals(HealthCheckStatus.DOWN, healthApiResponse.status)
        healthApiResponse.checks.forEach { serviceEndpoint ->
            assertNotNull(serviceEndpoint)
            assertEquals(HealthCheckStatus.DOWN, serviceEndpoint.status)

        }

    }


    @Test
    fun testServiceInternalServerError() {
        Mockito.`when`(basicAuthRestClientService!!.exchangeResource(
                        anyString(),
                        anyString(),
                       anyString()))
                .thenReturn(BlueprintWebClientService.WebClientResponse(500, "failure"))
        val healthApiResponse = bluePrintProcessorHealthCheck!!.retrieveEndpointExecutionStatus()
        assertNotNull(healthApiResponse)
        assertEquals(HealthCheckStatus.DOWN, healthApiResponse.status)
        healthApiResponse.checks.forEach { serviceEndpoint ->
            assertNotNull(serviceEndpoint)
            assertEquals(HealthCheckStatus.DOWN, serviceEndpoint.status)

        }

    }

    @Test
    fun testServiceIsRedirected() {
        Mockito.`when`(basicAuthRestClientService!!.
                exchangeResource(
                        anyString(),
                        anyString(),
                       anyString()))
                .thenReturn(BlueprintWebClientService.WebClientResponse(300, "failure"))
        val healthApiResponse = bluePrintProcessorHealthCheck!!.retrieveEndpointExecutionStatus()
        assertNotNull(healthApiResponse)
        assertEquals(HealthCheckStatus.DOWN, healthApiResponse.status)
        healthApiResponse.checks.forEach { serviceEndpoint ->
            assertNotNull(serviceEndpoint)
            assertEquals(HealthCheckStatus.DOWN, serviceEndpoint.status)

        }

    }

    private fun <T> anyObject(): T {
        return Mockito.anyObject<T>()
    }
    /*private fun  anyString(): String {
        return Mockito.anyString()
    }*/

}

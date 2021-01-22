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

package org.onap.ccsdk.cds.blueprintsprocessor.healthapi

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.anyString
import org.mockito.junit.MockitoJUnitRunner
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration.HealthCheckProperties
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.EndPointExecution
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health.BlueprintProcessorHealthCheck
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import java.util.Arrays

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

    private var bluePrintProcessorHealthCheck: BlueprintProcessorHealthCheck? = null

    @Before
    fun setup() {
        endPointExecution = Mockito.spy(endPointExecution!!)
        Mockito.`when`(healthCheckProperties!!.getBlueprintServiceInformation()).thenReturn(
            Arrays.asList(
                ServiceEndpoint("Execution service ", "http://cds-blueprints-processor-http:8080/api/v1/execution-service/health-check"),
                ServiceEndpoint("Resources service", "http://cds-blueprints-processor-http:8080/api/v1/resources/health-check"),
                ServiceEndpoint("Template service", "http://cds-blueprints-processor-http:8080/api/v1/template/health-check")
            )
        )

        bluePrintProcessorHealthCheck = BlueprintProcessorHealthCheck(endPointExecution!!, healthCheckProperties)
    }

    @Test
    fun testSystemIsCompletelyDown() {

        Mockito.`when`(
            basicAuthRestClientService!!.exchangeResource(
                anyString(),
                anyString(),
                anyString()
            )
        ).thenThrow(RuntimeException())
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

        Mockito.`when`(
            basicAuthRestClientService!!
                .exchangeResource(
                    anyString(),
                    anyString(),
                    anyString()
                )
        ).thenReturn(BlueprintWebClientService.WebClientResponse(200, "Success"))
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
        Mockito.`when`(
            basicAuthRestClientService!!.exchangeResource(
                anyString(),
                anyString(),
                anyString()
            )
        ).thenReturn(BlueprintWebClientService.WebClientResponse(404, "failure"))
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
        Mockito.`when`(
            basicAuthRestClientService!!.exchangeResource(
                anyString(),
                anyString(),
                anyString()
            )
        )
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
        Mockito.`when`(
            basicAuthRestClientService!!
                .exchangeResource(
                    anyString(),
                    anyString(),
                    anyString()
                )
        )
            .thenReturn(BlueprintWebClientService.WebClientResponse(300, "failure"))
        val healthApiResponse = bluePrintProcessorHealthCheck!!.retrieveEndpointExecutionStatus()
        assertNotNull(healthApiResponse)
        assertEquals(HealthCheckStatus.DOWN, healthApiResponse.status)
        healthApiResponse.checks.forEach { serviceEndpoint ->
            assertNotNull(serviceEndpoint)
            assertEquals(HealthCheckStatus.DOWN, serviceEndpoint.status)
        }
    }
}

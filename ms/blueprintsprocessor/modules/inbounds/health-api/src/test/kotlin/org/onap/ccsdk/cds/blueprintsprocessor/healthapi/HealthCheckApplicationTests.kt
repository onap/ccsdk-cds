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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration.HealthCheckProperties
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ApplicationHealth
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.WebClientEnpointResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.CombinedHealthService
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.EndPointExecution
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.springframework.boot.actuate.health.Status

/**
 * Unit tests for CombinedHealthService verifying health check aggregation logic.
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner::class)
class HealthCheckApplicationTests {

    @Mock
    private val endPointExecution: EndPointExecution? = null

    @Mock
    private val healthCheckProperties: HealthCheckProperties? = null

    @InjectMocks
    private var combinedHealthService: CombinedHealthService? = null

    @Before
    fun setup() {
        Mockito.`when`(healthCheckProperties!!.getBluePrintBaseURL())
            .thenReturn("http://cds-blueprints-processor-http:8080/")
        Mockito.`when`(healthCheckProperties.getCDSListenerBaseURL())
            .thenReturn("http://cds-sdc-listener:8080/")
    }

    @Test
    fun testGetCombinedHealthCheckWhenServicesAreDown() {
        Mockito.`when`(
            endPointExecution!!.retrieveWebClientResponse(
                Mockito.any(ServiceEndpoint::class.java)
            )
        ).thenReturn(WebClientEnpointResponse(BlueprintWebClientService.WebClientResponse(500, "")))

        val result = combinedHealthService!!.getCombinedHealthCheck()

        assertNotNull(result)
        assertEquals(2, result.size)
        result.forEach { health ->
            assertNotNull(health)
            assertEquals(Status.DOWN, health!!.status)
        }
    }

    @Test
    fun testGetCombinedHealthCheckWhenServicesAreUp() {
        val successResponse = WebClientEnpointResponse(
            BlueprintWebClientService.WebClientResponse(200, "")
        )
        val healthUp = ApplicationHealth(Status.UP, hashMapOf())

        Mockito.`when`(
            endPointExecution!!.retrieveWebClientResponse(
                Mockito.any(ServiceEndpoint::class.java)
            )
        ).thenReturn(successResponse)
        Mockito.`when`(
            endPointExecution.getHealthFromWebClientEnpointResponse(
                Mockito.any(WebClientEnpointResponse::class.java)
            )
        ).thenReturn(healthUp)

        val result = combinedHealthService!!.getCombinedHealthCheck()

        assertNotNull(result)
        assertEquals(2, result.size)
        result.forEach { health ->
            assertNotNull(health)
            assertEquals(Status.UP, health!!.status)
        }
    }
}

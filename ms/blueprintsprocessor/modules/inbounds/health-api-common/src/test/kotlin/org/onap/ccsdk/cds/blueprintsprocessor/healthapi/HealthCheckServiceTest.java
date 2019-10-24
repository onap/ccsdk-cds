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


package org.onap.ccsdk.cds.blueprintsprocessor.healthapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration.HealthCheckProperties;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.EndPointExecution;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health.BluePrintProcessorHealthCheck;
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties;
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService;
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService.WebClientResponse;
import org.springframework.http.HttpMethod;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckServiceTest {

  @Mock
  private BasicAuthRestClientService basicAuthRestClientService;

  @Mock
  private BasicAuthRestClientProperties restClientProperties;

  @Mock
  private HealthCheckProperties healthCheckProperties;

  @InjectMocks
  private EndPointExecution endPointExecution ;

  private BluePrintProcessorHealthCheck bluePrintProcessorHealthCheck;



  @Before
  public void setup() {
    endPointExecution = Mockito.spy(endPointExecution);
    Mockito.when(healthCheckProperties.getBluePrintServiceInformation()).thenReturn(Arrays.asList(
            new ServiceEndpoint("Execution service ", "http://cds-blueprints-processor-http:8080/api/v1/execution-service/health-check"),
            new ServiceEndpoint("Resources service", "http://cds-blueprints-processor-http:8080/api/v1/resources/health-check")
            , new ServiceEndpoint("Template service", "http://cds-blueprints-processor-http:8080/api/v1/template/health-check")
    ));

    bluePrintProcessorHealthCheck = new BluePrintProcessorHealthCheck(endPointExecution,healthCheckProperties);
  }

  @Test
  public void testSystemIsCompletelyDown() {

    Mockito.when(basicAuthRestClientService.exchangeResource(anyString(), anyString(), anyString())).
            thenThrow(new RuntimeException());
    HealthApiResponse healthApiResponse = bluePrintProcessorHealthCheck.retrieveEndpointExecutionStatus();
    assertNotNull(healthApiResponse);
    Assert.assertEquals(HealthCheckStatus.DOWN, healthApiResponse.getStatus());
    healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
      assertNotNull(serviceEndpoint);
      assertEquals(HealthCheckStatus.DOWN, serviceEndpoint.getStatus());

    });

  }

  @Test
  public void testSystemIsUPAndRunning() {

    Mockito.when(basicAuthRestClientService.exchangeResource(eq(HttpMethod.GET.name()), any(), anyString())).
            thenReturn(new WebClientResponse<>(200, "Success"));
    HealthApiResponse healthApiResponse = bluePrintProcessorHealthCheck.retrieveEndpointExecutionStatus();
    assertNotNull(healthApiResponse);
    assertEquals(HealthCheckStatus.UP, healthApiResponse.getStatus());
    healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
      assertNotNull(serviceEndpoint);
      assertEquals(HealthCheckStatus.UP, serviceEndpoint.getStatus());

    });

  }

  @Test public void testServiceIsNotFound() {
    Mockito.when(basicAuthRestClientService.exchangeResource(eq(HttpMethod.GET.name()), any(), anyString())).
            thenReturn(new WebClientResponse<>(404, "failure"));
    HealthApiResponse healthApiResponse = bluePrintProcessorHealthCheck.retrieveEndpointExecutionStatus();
    assertNotNull(healthApiResponse);
    assertEquals(HealthCheckStatus.DOWN, healthApiResponse.getStatus());
    healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
      assertNotNull(serviceEndpoint);
      assertEquals(HealthCheckStatus.DOWN, serviceEndpoint.getStatus());

    });

  }


  @Test public void testServiceInternalServerError() {
    Mockito.when(basicAuthRestClientService.exchangeResource(eq(HttpMethod.GET.name()), any(), anyString()))
            .thenReturn(new WebClientResponse<>(500, "failure"));
    HealthApiResponse healthApiResponse = bluePrintProcessorHealthCheck.retrieveEndpointExecutionStatus();
    assertNotNull(healthApiResponse);
    assertEquals(HealthCheckStatus.DOWN, healthApiResponse.getStatus());
    healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
      assertNotNull(serviceEndpoint);
      assertEquals(HealthCheckStatus.DOWN, serviceEndpoint.getStatus());

    });

  }

  @Test public void testServiceIsRedirected() {
    Mockito.when(basicAuthRestClientService.exchangeResource(eq(HttpMethod.GET.name()), any(), anyString()))
            .thenReturn(new WebClientResponse<>(300, "failure"));
    HealthApiResponse healthApiResponse = bluePrintProcessorHealthCheck.retrieveEndpointExecutionStatus();
    assertNotNull(healthApiResponse);
    assertEquals(HealthCheckStatus.DOWN, healthApiResponse.getStatus());
    healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
      assertNotNull(serviceEndpoint);
      assertEquals(HealthCheckStatus.DOWN, serviceEndpoint.getStatus());

    });

  }

}


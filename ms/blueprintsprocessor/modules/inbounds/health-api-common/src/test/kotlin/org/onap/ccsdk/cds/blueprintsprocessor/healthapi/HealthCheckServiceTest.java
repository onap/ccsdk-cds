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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.EndPointExecution;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health.BluePrintProcessorHealthCheck;
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties;
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService;
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService.WebClientResponse;
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentScriptExecutor;
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService;
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckServiceTest {

  @Mock
  private BasicAuthRestClientService basicAuthRestClientService;

  @Mock
  private BasicAuthRestClientProperties restClientProperties;

  @InjectMocks
  private EndPointExecution endPointExecution =new EndPointExecution();

  @InjectMocks
  private BluePrintProcessorHealthCheck bluePrintProcessorHealthCheck =
          new BluePrintProcessorHealthCheck();


  @Before
  public void setup() {
          bluePrintProcessorHealthCheck.setEndPointExecution(endPointExecution);
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


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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.HealthCheckService;
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties;
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService;
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService.WebClientResponse;
import org.springframework.http.HttpMethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckServiceTest {

  @Mock
  private BasicAuthRestClientService basicAuthRestClientService;

  @Mock
  private BasicAuthRestClientProperties restClientProperties;

  @InjectMocks
  private HealthCheckService healthCheckService = new HealthCheckService();

  @Before
  public void setup() {
  }

  @Test
  public void testSystemIsCompletelyDown() {

    Mockito.when(basicAuthRestClientService.exchangeResource(anyString(), anyString(), anyString())).
            thenThrow(new RuntimeException());
    HealthApiResponse healthApiResponse = healthCheckService.retrieveSystemStatus();
    assertNotNull(healthApiResponse);
    Assert.assertEquals(healthApiResponse.getStatus(), HealthCheckStatus.DOWN);
    healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
      assertNotNull(serviceEndpoint);
      assertEquals(serviceEndpoint.getStatus(), HealthCheckStatus.DOWN);

    });

  }


  @Test
  public void testSystemIsUPAndRunning() {

    Mockito.when(basicAuthRestClientService.exchangeResource(eq(HttpMethod.GET.name()), anyString(), anyString())).
            thenReturn(new WebClientResponse<>(200, "Success"));
    HealthApiResponse healthApiResponse = healthCheckService.retrieveSystemStatus();
    assertNotNull(healthApiResponse);
    assertEquals(healthApiResponse.getStatus(), HealthCheckStatus.UP);
    healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
      assertNotNull(serviceEndpoint);
      assertEquals(serviceEndpoint.getStatus(), HealthCheckStatus.UP);

    });

  }

  @Test
  public void testServiceIsNotFound() {
    Mockito.when(basicAuthRestClientService.exchangeResource(eq(HttpMethod.GET.name()), any(), anyString())).
            thenReturn(new WebClientResponse<>(404, "failure"));
    HealthApiResponse healthApiResponse = healthCheckService.retrieveSystemStatus();
    assertNotNull(healthApiResponse);
    assertEquals(healthApiResponse.getStatus(), HealthCheckStatus.DOWN);
    healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
      assertNotNull(serviceEndpoint);
      assertEquals(serviceEndpoint.getStatus(), HealthCheckStatus.DOWN);

    });

  }


  @Test
  public void testServiceInternalServerError() {
    Mockito.when(basicAuthRestClientService.exchangeResource(eq(HttpMethod.GET.name()), any(), anyString()))
            .thenReturn(new WebClientResponse<>(500, "failure"));
    HealthApiResponse healthApiResponse = healthCheckService.retrieveSystemStatus();
    assertNotNull(healthApiResponse);
    assertEquals(healthApiResponse.getStatus(), HealthCheckStatus.DOWN);
    healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
      assertNotNull(serviceEndpoint);
      assertEquals(serviceEndpoint.getStatus(), HealthCheckStatus.DOWN);

    });

  }

  @Test
  public void testServiceIsRedirected() {
    Mockito.when(basicAuthRestClientService.exchangeResource(eq(HttpMethod.GET.name()), any(), anyString()))
            .thenReturn(new WebClientResponse<>(300, "failure"));
    HealthApiResponse healthApiResponse = healthCheckService.retrieveSystemStatus();
    assertNotNull(healthApiResponse);
    assertEquals(healthApiResponse.getStatus(), HealthCheckStatus.DOWN);
    healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
      assertNotNull(serviceEndpoint);
      assertEquals(serviceEndpoint.getStatus(), HealthCheckStatus.DOWN);

    });

  }

}

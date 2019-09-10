package org.onap.ccsdk.cds.blueprintsprocessor.healthapi;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckResponse;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.HealthCheckService;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckResponse;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.HealthCheckService;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private HealthCheckService healthCheckService = new HealthCheckService();

    @Before
    public void setup() {
    }

    @Test
    public void testSystemIsCompletelyDown() {
        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).
                thenThrow(new RuntimeException());
        HealthApiResponse healthApiResponse = healthCheckService.execute();
        assertNotNull(healthApiResponse);
        Assert.assertEquals(healthApiResponse.getStatus(), HealthCheckStatus.DOWN);
        healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
            assertNotNull(serviceEndpoint);
            assertEquals(serviceEndpoint.getStatus(), HealthCheckStatus.DOWN);

        });

    }


    @Test
    public void testSystemIsUPAndRunning() {
        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).
                thenReturn(ResponseEntity.ok(null));
        HealthApiResponse healthApiResponse = healthCheckService.execute();
        assertNotNull(healthApiResponse);
        assertEquals(healthApiResponse.getStatus(), HealthCheckStatus.UP);
        healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
            assertNotNull(serviceEndpoint);
            assertEquals(serviceEndpoint.getStatus(), HealthCheckStatus.UP);

        });

    }

    @Test
    public void testSystemIsPartiallyDown() {

        for (int i = 0; i <healthCheckService.setupServiceEndpoint().size() ; i++) {
            if(i == 0){
                Mockito.when(restTemplate.exchange(ArgumentMatchers.eq(healthCheckService.setupServiceEndpoint().get(i).getServiceLink()), eq(HttpMethod.GET), any(), eq(String.class))).
                        thenReturn(ResponseEntity.ok(null));
            }else{
                Mockito.when(restTemplate.exchange(ArgumentMatchers.eq(healthCheckService.setupServiceEndpoint().get(i).getServiceLink()), eq(HttpMethod.GET), any(), eq(String.class))).
                        thenThrow(new RuntimeException());
            }

        }

        HealthApiResponse healthApiResponse = healthCheckService.execute();
        assertNotNull(healthApiResponse);
        assertEquals(healthApiResponse.getStatus(), HealthCheckStatus.DOWN);
       for (int i = 0; i <healthApiResponse.getChecks().size() ; i++) {
           HealthCheckResponse serviceEndpointReturn = healthApiResponse.getChecks().get(i);
            if(serviceEndpointReturn.getName().equals(healthCheckService.setupServiceEndpoint().get(0).getServiceName())){
                assertNotNull(serviceEndpointReturn);
                assertEquals(serviceEndpointReturn.getStatus(), HealthCheckStatus.UP);
            }else{
                assertNotNull(serviceEndpointReturn);
                assertEquals(serviceEndpointReturn.getStatus(), HealthCheckStatus.DOWN);
            }

        }

    }

    @Test
    public void testServiceIsNotFound() {
        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).
                thenReturn(ResponseEntity.notFound().build());
        HealthApiResponse healthApiResponse = healthCheckService.execute();
        assertNotNull(healthApiResponse);
        assertEquals(healthApiResponse.getStatus(), HealthCheckStatus.DOWN);
        healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
            assertNotNull(serviceEndpoint);
            assertEquals(serviceEndpoint.getStatus(), HealthCheckStatus.DOWN);

        });

    }


    @Test
    public void testServiceInternalServerError() {
        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).
                thenReturn(ResponseEntity.status(500).build());
        HealthApiResponse healthApiResponse = healthCheckService.execute();
        assertNotNull(healthApiResponse);
        assertEquals(healthApiResponse.getStatus(), HealthCheckStatus.DOWN);
        healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
            assertNotNull(serviceEndpoint);
            assertEquals(serviceEndpoint.getStatus(), HealthCheckStatus.DOWN);

        });

    }

    @Test
    public void testServiceIsRedirected() {
        Mockito.when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).
                thenReturn(ResponseEntity.status(300).build());
        HealthApiResponse healthApiResponse = healthCheckService.execute();
        assertNotNull(healthApiResponse);
        assertEquals(healthApiResponse.getStatus(), HealthCheckStatus.DOWN);
        healthApiResponse.getChecks().stream().forEach(serviceEndpoint -> {
            assertNotNull(serviceEndpoint);
            assertEquals(serviceEndpoint.getStatus(), HealthCheckStatus.DOWN);

        });

    }

}

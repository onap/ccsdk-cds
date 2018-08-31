package org.onap.ccsdk.config.rest.adaptor.service;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorException;
import org.onap.ccsdk.config.rest.adaptor.service.AbstractConfigRestClientAdapter;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorServiceImpl;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("unchecked")
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest({AbstractConfigRestClientAdapter.class})
public class GenericRestClientServiceTest {

    ConfigRestAdaptorService configRestAdaptorService;

    RestTemplate mockRestTemplate = mock(RestTemplate.class);

    String path = "path";

    @Before
    public void before() throws Exception {
        whenNew(RestTemplate.class).withAnyArguments().thenReturn(mockRestTemplate);

        String propertyDir = "src/test/resources";
        configRestAdaptorService = new ConfigRestAdaptorServiceImpl(propertyDir);
    }

    @Test
    public void testGetResource() throws Exception {
        String responseBody = "sampleBodyString";
        ResponseEntity<Object> response = new ResponseEntity<Object>(responseBody, HttpStatus.OK);
        when(mockRestTemplate.exchange(Matchers.endsWith(path), Mockito.eq(HttpMethod.GET), Mockito.any(),
                Mockito.any(Class.class))).thenReturn(response);

        String body = configRestAdaptorService.getResource("modelservice", path, String.class);

        Assert.assertEquals(responseBody, body);
    }

    @Test
    public void testPostResource() throws Exception {
        String responseBody = "sampleBodyString";
        ResponseEntity<Object> response = new ResponseEntity<Object>(responseBody, HttpStatus.OK);
        when(mockRestTemplate.exchange(Matchers.endsWith(path), Mockito.eq(HttpMethod.POST), Mockito.any(),
                Mockito.any(Class.class))).thenReturn(response);

        String body = configRestAdaptorService.postResource("modelservice", path, null, String.class);

        Assert.assertEquals(responseBody, body);
    }

    @Test
    public void testExchange() throws Exception {
        String responseBody = "sampleBodyString";
        ResponseEntity<Object> response = new ResponseEntity<Object>(responseBody, HttpStatus.OK);
        when(mockRestTemplate.exchange(Matchers.endsWith(path), Mockito.eq(HttpMethod.GET), Mockito.any(),
                Mockito.any(Class.class))).thenReturn(response);

        String body = configRestAdaptorService.exchangeResource("modelservice", path, null, String.class, "GET");

        Assert.assertEquals(responseBody, body);
    }

    @Test(expected = ConfigRestAdaptorException.class)
    public void testGetResourceError() throws Exception {
        ResponseEntity<Object> response = new ResponseEntity<Object>("", HttpStatus.INTERNAL_SERVER_ERROR);
        when(mockRestTemplate.getForEntity(Matchers.endsWith(path), Mockito.any())).thenReturn(response);

        configRestAdaptorService.getResource("modelservice", path, String.class);
    }

    @Test(expected = ConfigRestAdaptorException.class)
    public void testPostResourceError() throws Exception {
        ResponseEntity<Object> response = new ResponseEntity<Object>("", HttpStatus.INTERNAL_SERVER_ERROR);
        when(mockRestTemplate.postForEntity(Matchers.endsWith(path), Mockito.anyObject(), Mockito.any()))
                .thenReturn(response);

        configRestAdaptorService.postResource("modelservice", path, null, String.class);
    }

    @Test(expected = ConfigRestAdaptorException.class)
    public void testExchangeError() throws Exception {
        ResponseEntity<Object> response = new ResponseEntity<Object>("", HttpStatus.INTERNAL_SERVER_ERROR);
        when(mockRestTemplate.exchange(Matchers.endsWith(path), Mockito.eq(HttpMethod.GET), Mockito.any(),
                Mockito.any(Class.class))).thenReturn(response);

        configRestAdaptorService.exchangeResource("modelservice", path, null, String.class, "GET");
    }
}

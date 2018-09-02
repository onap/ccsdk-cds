/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

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
        when(mockRestTemplate.exchange(Matchers.endsWith(path), Matchers.eq(HttpMethod.GET), Matchers.any(),
                Matchers.any(Class.class))).thenReturn(response);
        
        String body = configRestAdaptorService.getResource("modelservice", path, String.class);
        
        Assert.assertEquals(responseBody, body);
    }
    
    @Test
    public void testPostResource() throws Exception {
        String responseBody = "sampleBodyString";
        ResponseEntity<Object> response = new ResponseEntity<Object>(responseBody, HttpStatus.OK);
        when(mockRestTemplate.exchange(Matchers.endsWith(path), Matchers.eq(HttpMethod.POST), Matchers.any(),
                Matchers.any(Class.class))).thenReturn(response);
        
        String body = configRestAdaptorService.postResource("modelservice", path, null, String.class);
        
        Assert.assertEquals(responseBody, body);
    }
    
    @Test
    public void testExchange() throws Exception {
        String responseBody = "sampleBodyString";
        ResponseEntity<Object> response = new ResponseEntity<Object>(responseBody, HttpStatus.OK);
        when(mockRestTemplate.exchange(Matchers.endsWith(path), Matchers.eq(HttpMethod.GET), Matchers.any(),
                Matchers.any(Class.class))).thenReturn(response);
        
        String body = configRestAdaptorService.exchangeResource("modelservice", path, null, String.class, "GET");
        
        Assert.assertEquals(responseBody, body);
    }
    
    @Test(expected = ConfigRestAdaptorException.class)
    public void testGetResourceError() throws Exception {
        ResponseEntity<Object> response = new ResponseEntity<Object>("", HttpStatus.INTERNAL_SERVER_ERROR);
        when(mockRestTemplate.getForEntity(Matchers.endsWith(path), Matchers.any())).thenReturn(response);
        
        configRestAdaptorService.getResource("modelservice", path, String.class);
    }
    
    @Test(expected = ConfigRestAdaptorException.class)
    public void testPostResourceError() throws Exception {
        ResponseEntity<Object> response = new ResponseEntity<Object>("", HttpStatus.INTERNAL_SERVER_ERROR);
        when(mockRestTemplate.postForEntity(Matchers.endsWith(path), Matchers.anyObject(), Matchers.any()))
                .thenReturn(response);
        
        configRestAdaptorService.postResource("modelservice", path, null, String.class);
    }
    
    @Test(expected = ConfigRestAdaptorException.class)
    public void testExchangeError() throws Exception {
        ResponseEntity<Object> response = new ResponseEntity<Object>("", HttpStatus.INTERNAL_SERVER_ERROR);
        when(mockRestTemplate.exchange(Matchers.endsWith(path), Matchers.eq(HttpMethod.GET), Matchers.any(),
                Matchers.any(Class.class))).thenReturn(response);
        
        configRestAdaptorService.exchangeResource("modelservice", path, null, String.class, "GET");
    }
}

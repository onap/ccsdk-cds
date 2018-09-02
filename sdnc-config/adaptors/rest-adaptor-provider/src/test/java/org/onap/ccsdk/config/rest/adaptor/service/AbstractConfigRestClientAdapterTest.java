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

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AbstractConfigRestClientAdapterTest {
    
    Map<String, String> properties = new HashMap<>();
    
    @Before
    public void setup() throws Exception {
        String propertyfile = "src/test/resources/config-rest-adaptor.properties";
        
        Properties restProperties = new Properties();
        restProperties.load(new FileInputStream(propertyfile));
        
        properties.putAll(restProperties.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString())));
    }
    
    @Test
    public void testInitGenericRestClient() throws Exception {
        ConfigRestClientServiceAdapter genericRestClient = new GenericRestClientAdapterImpl(properties, "modelservice");
        Assert.assertNotNull(genericRestClient);
    }
    
    @Test
    public void testInitSSLClient() throws Exception {
        ConfigRestClientServiceAdapter sslClient = new SSLRestClientAdapterImpl(properties, "aai");
        Assert.assertNotNull(sslClient);
    }
    
}

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

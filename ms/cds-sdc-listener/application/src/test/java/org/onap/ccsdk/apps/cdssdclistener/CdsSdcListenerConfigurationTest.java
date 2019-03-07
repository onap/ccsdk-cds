/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.apps.cdssdclistener;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(CdsSdcListenerConfiguration.class)
@SpringBootTest(classes = {CdsSdcListenerConfigurationTest.class})
public class CdsSdcListenerConfigurationTest {

    @Autowired
    private CdsSdcListenerConfiguration listenerConfiguration;

    @Test
    public void testCdsSdcListenerConfiguration() {
        assertEquals(listenerConfiguration.getAsdcAddress(), "localhost:8443");
        assertEquals(listenerConfiguration.getMsgBusAddress().stream().findFirst().get(), "localhost");
        assertEquals(listenerConfiguration.getUser(), "vid");
        assertEquals(listenerConfiguration.getPassword(), "Kp8bJ4SXszM0WXlhak3eHlcse2gAw84vaoGGmJvUy2U");
        assertEquals(listenerConfiguration.getPollingInterval(), 15);
        assertEquals(listenerConfiguration.getPollingTimeout(), 15);
        assertEquals(listenerConfiguration.getRelevantArtifactTypes().stream().findFirst().get(), "TOSCA_CSAR");
        assertEquals(listenerConfiguration.getConsumerGroup(), "cds-id-local");
        assertEquals(listenerConfiguration.getEnvironmentName(), "AUTO");
        assertEquals(listenerConfiguration.getConsumerID(), "cds-id-local");
        assertEquals(listenerConfiguration.activateServerTLSAuth(), false);
    }
}

/*
 * Copyright © 2019 Bell Canada
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
package org.onap.ccsdk.cds.cdssdclistener;

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
        assertEquals(listenerConfiguration.getPollingTimeout(), 60);
        assertEquals(listenerConfiguration.getRelevantArtifactTypes().stream().findFirst().get(), "TOSCA_CSAR");
        assertEquals(listenerConfiguration.getConsumerGroup(), "cds-id-local");
        assertEquals(listenerConfiguration.getEnvironmentName(), "AUTO");
        assertEquals(listenerConfiguration.getConsumerID(), "cds-id-local");
        assertEquals(listenerConfiguration.activateServerTLSAuth(), false);
    }
}

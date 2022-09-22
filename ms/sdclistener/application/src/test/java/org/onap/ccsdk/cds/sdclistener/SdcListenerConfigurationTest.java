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

package org.onap.ccsdk.cds.sdclistener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(SdcListenerConfiguration.class)
@SpringBootTest(classes = {SdcListenerConfigurationTest.class})
public class SdcListenerConfigurationTest {

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Autowired
    private SdcListenerConfiguration listenerConfiguration;

    @Test
    public void testCdsSdcListenerConfiguration() {
        environmentVariables.set("SASL_JAAS_CONFIG",
                "org.apache.kafka.common.security.scram.ScramLoginModule required username=admin password=admin-secret;");
        assertEquals("localhost:8443", listenerConfiguration.getSdcAddress());
        assertEquals(
                "org.apache.kafka.common.security.scram.ScramLoginModule required username=admin password=admin-secret;",
                listenerConfiguration.getKafkaSaslJaasConfig());
        assertEquals("cds", listenerConfiguration.getUser());
        assertEquals("Kp8bJ4SXszM0WXlhak3eHlcse2gAw84vaoGGmJvUy2U", listenerConfiguration.getPassword());
        assertEquals(15, listenerConfiguration.getPollingInterval());
        assertEquals(60, listenerConfiguration.getPollingTimeout());
        assertEquals("TOSCA_CSAR", listenerConfiguration.getRelevantArtifactTypes().stream().findFirst().get());
        assertEquals("cds-id-local", listenerConfiguration.getConsumerGroup());
        assertEquals("AUTO", listenerConfiguration.getEnvironmentName());
        assertEquals("cds-id-local", listenerConfiguration.getConsumerID());
        assertFalse(listenerConfiguration.activateServerTLSAuth());
        assertEquals(true, listenerConfiguration.isUseHttpsWithSDC());
    }

}

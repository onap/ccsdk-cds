/*-
 * ============LICENSE_START==========================================
 * ONAP Portal
 * ===================================================================
 * Copyright (C) 2020 IBM Intellectual Property. All rights reserved.
 * ===================================================================
 *
 * Unless otherwise specified, all software contained herein is licensed
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Unless otherwise specified, all documentation contained herein is licensed
 * under the Creative Commons License, Attribution 4.0 Intl. (the "License");
 * you may not use this documentation except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             https://creativecommons.org/licenses/by/4.0/
 *
 * Unless required by applicable law or agreed to in writing, documentation
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ============LICENSE_END============================================
 *
 *
 */

package org.onap.ccsdk.cds.sdclistener.dto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.cds.sdclistener.dto.SdcListenerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.onap.ccsdk.cds.sdclistener.client.SdcListenerAuthClientInterceptor;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties({SdcListenerDto.class, SdcListenerAuthClientInterceptor.class})
@SpringBootTest(classes = {SdcListenerDtoTest.class})
public class SdcListenerDtoTest {

    @Autowired
    private SdcListenerDto listenerConfiguration;

    @Test
    public void testCdsSdcListenerDto() {
        listenerConfiguration.setDistributionId("1234");
        listenerConfiguration.setArtifactUrl("/sdc/v1/artifact/");
        assertEquals(listenerConfiguration.getDistributionId(), "1234");
        assertEquals(listenerConfiguration.getArtifactUrl(), "/sdc/v1/artifact/");
    }


}

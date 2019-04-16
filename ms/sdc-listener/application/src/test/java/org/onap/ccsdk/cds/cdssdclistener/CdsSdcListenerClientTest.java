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

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;
import mockit.VerificationsInOrder;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.cds.cdssdclistener.client.CdsSdcListenerClient;
import org.onap.ccsdk.cds.cdssdclistener.dto.CdsSdcListenerDto;
import org.onap.ccsdk.cds.cdssdclistener.exceptions.CdsSdcListenerException;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientFactory;
import org.onap.sdc.impl.DistributionClientResultImpl;
import org.onap.sdc.utils.DistributionActionResultEnum;

@RunWith(JMockit.class)
public class CdsSdcListenerClientTest {

    @Tested
    private CdsSdcListenerClient cdsSdcListenerClient;

    @Test
    public void testInitCdsClientSuccesfully(@Injectable IDistributionClient distributionClient,
        @Injectable CdsSdcListenerConfiguration configuration,
        @Injectable CdsSdcListenerNotificationCallback notification,
        @Injectable CdsSdcListenerDto cdsSdcListenerDto) throws CdsSdcListenerException {

         //Arrange
        new MockUp<DistributionClientFactory>() {
            @Mock
            public IDistributionClient createDistributionClient() {
                return distributionClient;
            }
        };

        new Expectations() {{
            distributionClient.init(configuration, notification);
            result = getResult();
        }};

        new Expectations() {{
            distributionClient.start();
            result = getResult();
        }};

        // Act
        cdsSdcListenerClient.initSdcClient();

        // Verify
        new VerificationsInOrder() {{
            distributionClient.init(configuration, notification);
            distributionClient.start();
        }};
    }

    public IDistributionClientResult getResult() {
        return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,
            DistributionActionResultEnum.SUCCESS.name());
    }
}

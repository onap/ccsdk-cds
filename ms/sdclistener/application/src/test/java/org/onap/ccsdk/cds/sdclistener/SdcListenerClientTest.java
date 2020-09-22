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

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;
import mockit.VerificationsInOrder;
import org.junit.Test;
import org.onap.ccsdk.cds.sdclistener.client.SdcListenerClient;
import org.onap.ccsdk.cds.sdclistener.dto.SdcListenerDto;
import org.onap.ccsdk.cds.sdclistener.exceptions.SdcListenerException;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientFactory;
import org.onap.sdc.impl.DistributionClientResultImpl;
import org.onap.sdc.utils.DistributionActionResultEnum;

public class SdcListenerClientTest {

    @Tested
    private SdcListenerClient sdcListenerClient;

    @Test
    public void testInitCdsClientSuccesfully(@Injectable IDistributionClient distributionClient,
            @Injectable SdcListenerConfiguration configuration,
            @Injectable SdcListenerNotificationCallback notification, @Injectable SdcListenerDto sdcListenerDto)
            throws SdcListenerException {

        // Arrange
        new MockUp<DistributionClientFactory>() {
            @Mock
            public IDistributionClient createDistributionClient() {
                return distributionClient;
            }
        };

        new Expectations() {
            {
                distributionClient.init(configuration, notification);
                result = getResult();
            }
        };

        new Expectations() {
            {
                distributionClient.start();
                result = getResult();
            }
        };

        // Act
        sdcListenerClient.initSdcClient();

        // Verify
        new VerificationsInOrder() {
            {
                distributionClient.init(configuration, notification);
                distributionClient.start();
            }
        };
    }

    public IDistributionClientResult getResult() {
        return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,
                DistributionActionResultEnum.SUCCESS.name());
    }

}
